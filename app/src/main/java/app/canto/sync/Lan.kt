package app.canto.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import app.canto.core.Node
import app.canto.data.Repo
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class Seen(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val code: String?,
)

class Lan(
    private val context: Context,
    private val repo: Repo,
    private val node: Node,
) {
    val offer = MutableStateFlow<String?>(null)
    val offerUntil = MutableStateFlow(0L)
    val message = MutableStateFlow("")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val main = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val nsd = context.getSystemService(NsdManager::class.java)
    private val wifi = context.getSystemService(WifiManager::class.java)
    private val multicast = wifi.createMulticastLock("canto").apply { setReferenceCounted(false) }
    private val seen = ConcurrentHashMap<String, Seen>()
    private val names = ConcurrentHashMap<String, String>()
    private val queue = ArrayDeque<NsdServiceInfo>()
    private var resolving = false
    private var browsing = false
    private var advertised: String? = "\u0000"
    private var desiredCode: String? = null
    private var retiring = false
    private var registration: NsdManager.RegistrationListener? = null

    fun start() {
        main.launch {
            if (!multicast.isHeld) multicast.acquire()
            browse()
            advertise(null)
        }
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()
        cm.registerNetworkCallback(
            request,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    auto()
                }
            },
        )
    }

    fun showCode() {
        scope.launch {
            if (!onLocalLink(context)) {
                message.value = "dropped"
                return@launch
            }
            message.value = ""
            val code = node.beginOffer(otherCodes(), { bound -> Random.nextInt(bound) }) ?: return@launch
            offer.value = code
            offerUntil.value = node.offerUntil
            main.launch { advertise(code) }
            while (node.offerCode != null) {
                delay(1000)
                if (System.currentTimeMillis() >= node.offerUntil) {
                    node.retireOffer()
                    offer.value = null
                    offerUntil.value = 0L
                    main.launch { advertise(null) }
                    message.value = "expired"
                    break
                }
                val before = node.offerCode
                val next = node.refreshOffer(otherCodes(), { bound -> Random.nextInt(bound) })
                if (next != null && next != before) {
                    offer.value = next
                    offerUntil.value = node.offerUntil
                    main.launch { advertise(next) }
                } else {
                    offerUntil.value = node.offerUntil
                }
            }
        }
    }

    fun hideCode() {
        node.retireOffer()
        offer.value = null
        offerUntil.value = 0L
        main.launch { advertise(null) }
    }

    fun enter(code: String) {
        scope.launch {
            if (!onLocalLink(context)) {
                message.value = "dropped"
                return@launch
            }
            val typed = code.trim()
            if (typed.isNotEmpty() && typed == node.offerCode) {
                message.value = "self"
                return@launch
            }
            message.value = "syncing"
            val deadline = System.currentTimeMillis() + 3500
            var offers = offers()
            while (offers.isEmpty() && System.currentTimeMillis() < deadline) {
                delay(200)
                offers = offers()
            }
            if (offers.isEmpty()) {
                message.value = "no_code"
                return@launch
            }
            var expired = false
            for (peer in offers) {
                if (peer.id == repo.deviceId()) {
                    message.value = "self"
                    return@launch
                }
                val result = node.pair(peer.host, peer.port, code.trim())
                when (result.reason) {
                    "ok" -> {
                        repo.touchPeer(result.peerId)
                        message.value = "paired"
                        offer.value = null
                        return@launch
                    }
                    "expired" -> expired = true
                    "self" -> {
                        message.value = "self"
                        return@launch
                    }
                    "io" -> {
                        message.value = if (!onLocalLink(context)) "dropped" else "left"
                        return@launch
                    }
                }
            }
            message.value = if (expired) "expired" else "wrong_code"
        }
    }

    fun syncNow(peerId: String) {
        scope.launch {
            if (!onLocalLink(context)) {
                message.value = "dropped"
                return@launch
            }
            message.value = "syncing"
            val deadline = System.currentTimeMillis() + 3500
            var peer = seen[peerId]
            while (peer == null && System.currentTimeMillis() < deadline) {
                delay(200)
                peer = seen[peerId]
            }
            if (peer == null) {
                message.value = "not_visible"
                return@launch
            }
            val result = node.sync(peer.host, peer.port, peerId)
            if (result.ok) {
                repo.touchPeer(peerId)
                message.value = "synced"
            } else {
                message.value = if (!onLocalLink(context)) "dropped" else "left"
            }
        }
    }

    fun auto() {
        scope.launch {
            if (!onLocalLink(context)) return@launch
            val me = repo.deviceId()
            for (peer in repo.peers()) {
                if (me <= peer.id) continue
                val found = seen[peer.id] ?: continue
                val result = node.sync(found.host, found.port, peer.id)
                if (result.ok) repo.touchPeer(peer.id)
            }
        }
    }

    private fun otherCodes(): Set<String> = seen.values.mapNotNull { it.code }.toSet()

    private fun offers(): List<Seen> = seen.values.filter { !it.code.isNullOrEmpty() }

    private fun browse() {
        if (browsing) return
        browsing = true
        nsd.discoverServices(
            "_canto._tcp.",
            NsdManager.PROTOCOL_DNS_SD,
            object : NsdManager.DiscoveryListener {
                override fun onStartDiscoveryFailed(type: String, error: Int) { browsing = false }
                override fun onStopDiscoveryFailed(type: String, error: Int) {}
                override fun onDiscoveryStarted(type: String) {}
                override fun onDiscoveryStopped(type: String) { browsing = false }
                override fun onServiceFound(info: NsdServiceInfo) = enqueue(info)
                override fun onServiceLost(info: NsdServiceInfo) {
                    names.remove(info.serviceName)?.let { seen.remove(it) }
                }
            },
        )
    }

    private fun enqueue(info: NsdServiceInfo) {
        synchronized(queue) {
            queue.add(info)
            if (!resolving) drain()
        }
    }

    private fun drain() {
        val next = if (queue.isEmpty()) null else queue.removeAt(0)
        if (next == null) {
            resolving = false
            return
        }
        resolving = true
        nsd.resolveService(
            next,
            object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    synchronized(queue) { drain() }
                }

                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                    val id = serviceInfo.attributes?.get("id")?.toString(Charsets.UTF_8)
                    val host = serviceInfo.host?.hostAddress
                    if (id != null && host != null && id != repo.deviceId()) {
                        val code = serviceInfo.attributes?.get("code")?.toString(Charsets.UTF_8)
                        val name = serviceInfo.attributes?.get("nm")?.toString(Charsets.UTF_8) ?: "Canto"
                        seen[id] = Seen(id, name, host, serviceInfo.port, code)
                        names[serviceInfo.serviceName] = id
                    }
                    synchronized(queue) { drain() }
                }
            },
        )
    }

    private fun advertise(code: String?) {
        desiredCode = code
        if (retiring) return
        if (code == advertised && registration != null) return
        val previous = registration
        if (previous != null) {
            retiring = true
            try {
                nsd.unregisterService(previous)
            } catch (_: Exception) {
                retiring = false
                registration = null
                publish(desiredCode)
            }
            return
        }
        publish(desiredCode)
    }

    private fun publish(code: String?) {
        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                advertised = code
            }

            override fun onRegistrationFailed(info: NsdServiceInfo, error: Int) {
                if (registration === this) registration = null
            }

            override fun onServiceUnregistered(info: NsdServiceInfo) {
                if (registration === this) registration = null
                retiring = false
                advertised = "\u0000"
                publish(desiredCode)
            }

            override fun onUnregistrationFailed(info: NsdServiceInfo, error: Int) {
                if (registration === this) registration = null
                retiring = false
                advertised = "\u0000"
                publish(desiredCode)
            }
        }
        val info = NsdServiceInfo().apply {
            serviceName = "Canto" + repo.deviceId().take(8)
            serviceType = "_canto._tcp."
            port = node.port
            setAttribute("id", repo.deviceId())
            setAttribute("nm", repo.deviceName().take(32))
            if (!code.isNullOrEmpty()) setAttribute("code", code)
        }
        registration = listener
        try {
            nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (_: Exception) {
            registration = null
        }
    }
}

fun onLocalLink(context: Context): Boolean {
    val cm = context.getSystemService(ConnectivityManager::class.java)
    return cm.allNetworks.any { network ->
        val caps = cm.getNetworkCapabilities(network) ?: return@any false
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}
