package app.presence.core

import org.json.JSONArray
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException
import java.net.InetSocketAddress
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID

interface Catalog {
    fun deviceId(): String
    fun deviceName(): String
    fun snapshot(): List<Rec>
    fun mergeFrom(incoming: List<Rec>)
    fun rememberPeer(deviceId: String, name: String, token: String)
    fun tokenFor(deviceId: String): String?
}

data class SyncResult(val ok: Boolean, val reason: String, val peerId: String = "", val peerName: String = "")

object Frame {
    fun write(out: DataOutputStream, obj: JSONObject) {
        val data = obj.toString().toByteArray(Charsets.UTF_8)
        out.writeInt(data.size)
        out.write(data)
        out.flush()
    }

    fun read(input: DataInputStream): JSONObject? {
        val n = try {
            input.readInt()
        } catch (_: EOFException) {
            return null
        }
        if (n < 2 || n > 8_000_000) throw IOException("length")
        val data = ByteArray(n)
        try {
            input.readFully(data)
        } catch (_: EOFException) {
            return null
        }
        return JSONObject(String(data, Charsets.UTF_8))
    }
}

fun recToJson(rec: Rec): JSONObject {
    val body = JSONObject()
    rec.body.forEach { (k, v) -> body.put(k, v) }
    return JSONObject()
        .put("kind", rec.kind)
        .put("id", rec.id)
        .put("updatedAt", rec.updatedAt)
        .put("writerId", rec.writerId)
        .put("deleted", rec.deleted)
        .put("body", body)
}

fun recFromJson(obj: JSONObject): Rec {
    val bodyObj = obj.getJSONObject("body")
    val body = linkedMapOf<String, String>()
    for (key in bodyObj.keys()) body[key] = bodyObj.optString(key)
    return Rec(
        kind = obj.getString("kind"),
        id = obj.getString("id"),
        updatedAt = obj.getLong("updatedAt"),
        writerId = obj.getString("writerId"),
        deleted = obj.optBoolean("deleted"),
        body = body,
    )
}

fun recordsToJson(records: List<Rec>): JSONObject {
    val array = JSONArray()
    records.filter { it.kind in SYNC_KINDS }.forEach { array.put(recToJson(it)) }
    return JSONObject().put("t", "records").put("items", array)
}

fun recordsFromJson(obj: JSONObject): List<Rec> {
    if (obj.optString("t") != "records") throw IOException("records")
    val array = obj.optJSONArray("items") ?: throw IOException("items")
    return buildList {
        for (i in 0 until array.length()) add(recFromJson(array.getJSONObject(i)))
    }
}

class Node(
    private val catalog: Catalog,
    private val newSocket: () -> Socket = { Socket() },
    private val clock: () -> Long,
) {
    @Volatile var offerCode: String? = null
    @Volatile var offerUntil: Long = 0L
    @Volatile var port: Int = 0
        private set

    private var server: ServerSocket? = null
    private val gate = Any()

    fun start(bindPort: Int = 0) {
        val ss = ServerSocket(bindPort)
        server = ss
        port = ss.localPort
        Thread {
            while (!ss.isClosed) {
                try {
                    val socket = ss.accept()
                    Thread { handle(socket) }.apply { isDaemon = true; start() }
                } catch (_: IOException) {
                    break
                }
            }
        }.apply { isDaemon = true; name = "presence-listen"; start() }
    }

    fun close() {
        try {
            server?.close()
        } catch (_: IOException) {
        }
    }

    fun beginOffer(others: Set<String>, draw: (Int) -> Int, now: Long = clock()): String? {
        val code = rotateCode(null, others, draw) ?: return null
        offerCode = code
        offerUntil = now + CODE_TTL_MS
        return code
    }

    fun refreshOffer(others: Set<String>, draw: (Int) -> Int, now: Long = clock()): String? {
        val current = offerCode ?: return null
        if (now >= offerUntil) {
            offerCode = null
            return null
        }
        if (current !in others) return current
        val next = chooseCode(others, draw) ?: return current
        offerCode = next
        offerUntil = now + CODE_TTL_MS
        return next
    }

    fun retireOffer() {
        offerCode = null
        offerUntil = 0L
    }

    fun pair(host: String, port: Int, code: String): SyncResult {
        val hello = JSONObject()
            .put("t", "hello")
            .put("mode", "pair")
            .put("deviceId", catalog.deviceId())
            .put("name", catalog.deviceName())
            .put("code", code)
        return exchange(host, port, hello, pairing = true)
    }

    fun sync(host: String, port: Int, peerId: String): SyncResult {
        val token = catalog.tokenFor(peerId) ?: return SyncResult(false, "no_token", peerId)
        val hello = JSONObject()
            .put("t", "hello")
            .put("mode", "sync")
            .put("peerId", peerId)
            .put("deviceId", catalog.deviceId())
            .put("name", catalog.deviceName())
            .put("token", token)
        return exchange(host, port, hello, pairing = false)
    }

    private fun exchange(host: String, port: Int, hello: JSONObject, pairing: Boolean): SyncResult {
        var socket: Socket? = null
        try {
            val connection = newSocket()
            socket = connection
            val address = InetAddress.getByName(host)
            if (!isLocalAddress(address)) return SyncResult(false, "not_local")
            connection.connect(InetSocketAddress(address, port), 4_000)
            connection.soTimeout = 8_000
            val input = DataInputStream(connection.getInputStream())
            val output = DataOutputStream(connection.getOutputStream())
            Frame.write(output, hello)
            val reply = Frame.read(input) ?: return SyncResult(false, "io")
            if (reply.optString("t") == "hello_fail") {
                return SyncResult(false, reply.optString("reason", "io"))
            }
            if (reply.optString("t") != "hello_ok") return SyncResult(false, "io")
            val peerId = reply.optString("deviceId")
            val peerName = reply.optString("name")
            val token = reply.optString("token")
            if (peerId.isEmpty() || peerId == catalog.deviceId() || token.isEmpty()) return SyncResult(false, "io")
            if (!pairing && (peerId != hello.optString("peerId", peerId) || token != hello.optString("token"))) return SyncResult(false, "no_token")
            run {
                val mine = catalog.snapshot().forSync()
                Frame.write(output, recordsToJson(mine))
                val theirs = Frame.read(input) ?: return SyncResult(false, "io")
                catalog.mergeFrom(recordsFromJson(theirs).forSync())
            }
            if (pairing) catalog.rememberPeer(peerId, peerName, token)
            return SyncResult(true, "ok", peerId, peerName)
        } catch (_: Exception) {
            return SyncResult(false, "io")
        } finally {
            try {
                socket?.close()
            } catch (_: IOException) {
            }
        }
    }

    private fun handle(socket: Socket) {
        try {
            if (!isLocalAddress(socket.inetAddress)) return
            socket.soTimeout = 8_000
            val input = DataInputStream(socket.getInputStream())
            val output = DataOutputStream(socket.getOutputStream())
            val hello = Frame.read(input) ?: return
            if (hello.optString("t") != "hello") return
            val remoteId = hello.optString("deviceId")
            if (remoteId.isEmpty() || remoteId == catalog.deviceId()) {
                Frame.write(output, fail("self"))
                return
            }
            val mode = hello.optString("mode")
            var pairedToken: String? = null
            if (mode == "pair") {
                when (val status = codeStatus(hello.optString("code"), offerCode, offerUntil, clock())) {
                    CodeStatus.Match -> {
                        pairedToken = UUID.randomUUID().toString()
                        Frame.write(
                            output,
                            JSONObject()
                                .put("t", "hello_ok")
                                .put("deviceId", catalog.deviceId())
                                .put("name", catalog.deviceName())
                                .put("token", pairedToken),
                        )
                    }
                    CodeStatus.Expired -> {
                        Frame.write(output, fail("expired"))
                        return
                    }
                    CodeStatus.Wrong -> {
                        Frame.write(output, fail("wrong_code"))
                        return
                    }
                    CodeStatus.NotOffering -> {
                        Frame.write(output, fail("not_offering"))
                        return
                    }
                }
            } else if (mode == "sync") {
                val known = catalog.tokenFor(remoteId)
                val token = hello.optString("token")
                if (known.isNullOrEmpty() || known != token) {
                    Frame.write(output, fail("no_token"))
                    return
                }
                Frame.write(
                    output,
                    JSONObject()
                        .put("t", "hello_ok")
                        .put("deviceId", catalog.deviceId())
                        .put("name", catalog.deviceName())
                        .put("token", known),
                )
            } else return
            val incomingMsg = Frame.read(input) ?: return
            val incoming = recordsFromJson(incomingMsg).forSync()
            val mine = synchronized(gate) {
                val snapshot = catalog.snapshot().forSync()
                catalog.mergeFrom(incoming)
                snapshot
            }
            val tokenToKeep = pairedToken
            if (mode == "pair" && tokenToKeep != null) {
                catalog.rememberPeer(remoteId, hello.optString("name"), tokenToKeep)
                retireOffer()
            }
            Frame.write(output, recordsToJson(mine))
        } catch (_: Exception) {
        } finally {
            try {
                socket.close()
            } catch (_: IOException) {
            }
        }
    }

    private fun fail(reason: String) = JSONObject().put("t", "hello_fail").put("reason", reason)
}

fun List<Rec>.forSync(): List<Rec> = filter {
    it.kind in SYNC_KINDS &&
        (it.kind != KIND_PREF || it.id in TIMER_PREF_IDS) &&
        (it.kind != KIND_SESSION || it.deleted || it.f("endedAt").isNotEmpty())
}

fun encodeBackup(records: List<Rec>): String {
    val items = recordsToJson(records).getJSONArray("items")
    return JSONObject().put("presence", 1).put("items", items).toString()
}

fun decodeBackup(text: String): List<Rec>? = try {
    val obj = JSONObject(text)
    if (obj.optInt("presence") != 1) null
    else recordsFromJson(JSONObject().put("t", "records").put("items", obj.getJSONArray("items")))
} catch (_: Exception) {
    null
}

class MemoryCatalog(
    private val id: String,
    name: String,
    var protectedIds: Set<String> = emptySet(),
    var deferredPrefs: Set<String> = emptySet(),
) : Catalog {
    var label: String = name
    val rows = mutableListOf<Rec>()
    val peers = mutableMapOf<String, Pair<String, String>>()
    val lock = Any()

    override fun deviceId() = id
    override fun deviceName() = label
    override fun snapshot(): List<Rec> = synchronized(lock) { rows.toList() }

    override fun mergeFrom(incoming: List<Rec>) {
        synchronized(lock) {
            val merged = mergeRecords(rows, incoming, protectedIds, deferredPrefs)
            rows.clear()
            rows.addAll(merged)
        }
    }

    override fun rememberPeer(deviceId: String, name: String, token: String) {
        synchronized(lock) { peers[deviceId] = name to token }
    }

    override fun tokenFor(deviceId: String): String? = synchronized(lock) { peers[deviceId]?.second }

    fun put(rec: Rec) {
        synchronized(lock) {
            rows.removeAll { it.kind == rec.kind && it.id == rec.id }
            rows += rec
        }
    }
}

// Android needs INTERNET permission for LAN sockets; Presence accepts only local addresses.
fun isLocalAddress(address: InetAddress): Boolean =
    address.isLoopbackAddress || address.isLinkLocalAddress || address.isSiteLocalAddress ||
        (address is java.net.Inet6Address && (address.address[0].toInt() and 0xfe) == 0xfc)
