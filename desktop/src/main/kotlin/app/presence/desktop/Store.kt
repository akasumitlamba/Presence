package app.presence.desktop

import app.presence.core.*
import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.*
import java.util.UUID

/** One atomic file, including local state and pairing credentials. Never sync local records. */
class Store(val directory: Path = dataDirectory()) : Catalog {
    private var rows: List<Rec>
    @Volatile var revision = 0L
        private set
    private val file = directory.resolve("presence.json")
    init {
        Files.createDirectories(directory)
        rows = if (Files.exists(file)) {
            val array = JSONObject(Files.readString(file)).getJSONArray("items")
            List(array.length()) { recFromJson(array.getJSONObject(it)) }
        } else emptyList()
        if (rows.none { it.kind == KIND_LOCAL && it.id == "device" }) {
            val id = UUID.randomUUID().toString()
            put(Rec(KIND_LOCAL, "device", System.currentTimeMillis(), id, false,
                mapOf("id" to id, "name" to "Presence • ${System.getProperty("os.name")}")))
        }
    }
    @Synchronized override fun snapshot() = rows.toList()
    @Synchronized override fun deviceId() = rows.first { it.kind == KIND_LOCAL && it.id == "device" }.f("id")
    @Synchronized override fun deviceName() = rows.first { it.kind == KIND_LOCAL && it.id == "device" }.f("name")
    @Synchronized override fun tokenFor(deviceId: String) = rows.find { it.kind == KIND_LOCAL && it.id == "peer:$deviceId" && !it.deleted }?.f("token")
    @Synchronized override fun rememberPeer(deviceId: String, name: String, token: String) {
        write(KIND_LOCAL, "peer:$deviceId", mapOf("name" to name, "token" to token))
    }
    @Synchronized override fun mergeFrom(incoming: List<Rec>) {
        val snap = snap()
        persist(mergeRecords(rows, incoming.forSync(), if (sessionOpen(snap)) setOfNotNull(snap.sessionId) else emptySet(), emptySet()))
    }
    @Synchronized fun put(vararg records: Rec) {
        val keys = records.map { it.kind to it.id }.toSet()
        persist(rows.filter { it.kind to it.id !in keys } + records)
    }
    @Synchronized fun write(kind: String, id: String = UUID.randomUUID().toString(), body: Map<String, String>, deleted: Boolean = false) {
        put(Rec(kind, id, maxOf(System.currentTimeMillis(), (rows.maxOfOrNull { it.updatedAt } ?: 0) + 1), deviceId(), deleted, body))
    }
    @Synchronized fun pref(key: String, fallback: String) = rows.find { it.kind == KIND_PREF && it.id == key && !it.deleted }?.f("value") ?: fallback
    @Synchronized fun flag(key: String, fallback: String) = rows.find { it.kind == KIND_LOCAL && it.id == "flags" }?.f(key)?.ifEmpty { fallback } ?: fallback
    @Synchronized fun setFlag(key: String, value: String) {
        write(KIND_LOCAL, "flags", (rows.find { it.kind == KIND_LOCAL && it.id == "flags" }?.body ?: emptyMap()) + (key to value))
    }
    @Synchronized fun snap() = rows.find { it.kind == KIND_LOCAL && it.id == "snapshot" }?.let { snapFrom(it.body) } ?: Snap()
    @Synchronized fun apply(step: Step) {
        val rec = Rec(KIND_LOCAL, "snapshot", System.currentTimeMillis(), deviceId(), false, step.snap.toFields())
        put(*(step.writes + rec).toTypedArray())
    }
    private fun persist(next: List<Rec>) {
        val array = JSONArray()
        next.forEach { array.put(recToJson(it)) }
        val temp = Files.createTempFile(directory, "presence-", ".tmp")
        try {
            Files.writeString(temp, JSONObject().put("items", array).toString())
            try { Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING) }
            catch (_: AtomicMoveNotSupportedException) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING) }
            rows = next
            revision++
        } finally { Files.deleteIfExists(temp) }
    }
    companion object {
        fun dataDirectory(): Path {
            val os = System.getProperty("os.name").lowercase()
            val base = if (os.contains("win")) System.getenv("APPDATA") ?: System.getProperty("user.home")
                else System.getenv("XDG_DATA_HOME") ?: "${System.getProperty("user.home")}/.local/share"
            return Paths.get(base, "presence")
        }
    }
}
