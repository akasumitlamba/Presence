package app.presence.desktop

import app.presence.core.*
import javax.jmdns.*
import java.net.*
import java.util.concurrent.*
import kotlin.random.Random

data class Peer(val id: String, val name: String, val host: String, val port: Int, val offering: Boolean)

/** Same DNS-SD service and TXT attributes as Android's NsdManager. */
class DesktopLan(private val store: Store) : AutoCloseable {
    private val node = Node(store, clock = System::currentTimeMillis)
    private val worker = Executors.newSingleThreadScheduledExecutor { r -> Thread(r, "presence-lan").apply { isDaemon = true } }
    private val discoveries = ConcurrentHashMap<String, Peer>()
    private val interfaces = ConcurrentHashMap<InetAddress, JmDNS>()
    @Volatile var message = "Looking for devices on your local network"
    @Volatile var code: String? = null
    @Volatile private var closed = false
    private var advertised: String? = "initial"
    init {
        node.start()
        worker.scheduleWithFixedDelay({
            try {
                refreshInterfaces()
                val offered = node.offerCode
                if (offered != null && System.currentTimeMillis() >= node.offerUntil) { node.retireOffer(); message = "expired" }
                else if(code != null && offered == null) message = "Paired. Data synced."
                code = node.offerCode
                if (advertised != code) advertise()
            } catch (e: Exception) { message = "Local discovery unavailable: ${e.message}. Try an address below." }
        }, 0, 2, TimeUnit.SECONDS)
        worker.scheduleWithFixedDelay({ syncKnown() }, 8, 30, TimeUnit.SECONDS)
    }
    fun offerUntil() = node.offerUntil
    fun peers() = discoveries.values.distinctBy { it.id }.sortedBy { it.name }
    fun address() = "${interfaces.keys.firstOrNull()?.hostAddress ?: "127.0.0.1"}:${node.port}"
    private fun refreshInterfaces() {
        val current = NetworkInterface.getNetworkInterfaces().toList().filter { it.isUp && !it.isLoopback }
            .flatMap { it.inetAddresses.toList() }.filter { it is Inet4Address && isLocalAddress(it) }.toSet()
        for (gone in interfaces.keys.toSet() - current) interfaces.remove(gone)?.close()
        for (address in current - interfaces.keys) {
            val dns = JmDNS.create(address, "presence-${store.deviceId().take(8)}")
            dns.addServiceListener(TYPE, object : ServiceListener {
                override fun serviceAdded(event: ServiceEvent) { event.dns.requestServiceInfo(event.type, event.name, true) }
                override fun serviceRemoved(event: ServiceEvent) { discoveries.remove("${event.dns.inetAddress}:${event.name}") }
                override fun serviceResolved(event: ServiceEvent) {
                    val info = event.info
                    val id = info.getPropertyString("id") ?: return
                    if (id == store.deviceId()) return
                    val host = info.inet4Addresses.firstOrNull { isLocalAddress(it) }?.hostAddress ?: return
                    discoveries["${event.dns.inetAddress}:${event.name}"] = Peer(id, info.getPropertyString("nm") ?: "Presence", host, info.port, info.getPropertyString("code") != null)
                }
            })
            interfaces[address] = dns
            advertised = "refresh"
        }
    }
    private fun advertise() {
        interfaces.values.forEach { dns ->
            dns.unregisterAllServices()
            val txt = mutableMapOf("id" to store.deviceId(), "nm" to store.deviceName().take(32))
            if (code != null) txt["code"] = "offered" // Never publish the secret code in mDNS.
            dns.registerService(ServiceInfo.create(TYPE, "Presence-${store.deviceId().take(8)}", node.port, 0, 0, txt))
        }
        advertised = code
    }
    fun showCode() = execute {
        code = node.beginOffer(emptySet(), Random::nextInt)
        advertise()
        message = "Enter this code on your other device. It expires in 10 minutes."
    }
    fun hideCode() = execute { node.retireOffer(); code = null; advertise() }
    fun pair(typed: String, direct: String = "") = execute {
        if (!typed.matches(Regex("[0-9]{4}"))) { message = "Enter a four-digit code"; return@execute }
        if (typed == node.offerCode) { message = "Enter the code from your other device"; return@execute }
        message = "Pairing…"
        val candidates = if (direct.isNotBlank()) {
            val parts = direct.trim().split(':')
            val port = parts.getOrNull(1)?.toIntOrNull()
            require(parts.size == 2 && port != null && port in 1..65535) { "Use IPv4-address:port" }
            listOf(Peer("", "Presence", parts[0], port!!, true))
        } else peers().filter { it.offering }
        var result = SyncResult(false, "No device is showing a code. Check Wi-Fi and firewall settings.")
        for (peer in candidates) {
            result = node.pair(peer.host, peer.port, typed)
            if (result.ok) break
        }
        message = if (result.ok) "Paired with ${result.peerName}. Data synced." else "Pairing failed: ${result.reason}"
    }
    fun sync(peerId: String? = null) = execute { message = "Syncing…"; syncKnown(peerId) }
    private fun syncKnown(peerId: String? = null) {
        if (closed) return
        val paired = peers().filter { store.tokenFor(it.id) != null && (peerId == null || it.id == peerId) }
        if(peerId != null && paired.isEmpty()) { message = "not_visible"; return }
        paired.forEach {
            val result = node.sync(it.host, it.port, it.id)
            if(result.ok) store.touchPeer(it.id)
            message = if (result.ok) "Synced with ${it.name}" else "Could not sync with ${it.name}: ${result.reason}"
        }
    }
    private fun execute(block: () -> Unit) { if (!closed) worker.execute { try { block() } catch (e: Exception) { message = e.message ?: "Local sync failed" } } }
    override fun close() {
        closed = true
        worker.shutdownNow()
        node.close()
        interfaces.values.forEach { runCatching { it.close() } }
    }
    companion object { const val TYPE = "_presence._tcp.local." }
}
