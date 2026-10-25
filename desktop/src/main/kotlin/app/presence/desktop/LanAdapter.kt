package app.presence.desktop

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

/** Exposes Android's observable LAN surface to the shared Pair screen. */
class DesktopLanAdapter(private val lan: DesktopLan?,private val repo: Store) : AutoCloseable {
    val available get() = lan != null
    val offer = MutableStateFlow<String?>(null)
    val offerUntil = MutableStateFlow(0L)
    val message = MutableStateFlow("")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    init { scope.launch { while(isActive) {
        if(lan == null) { message.value = "dropped"; delay(1000); continue }
        offer.value = lan.code
        offerUntil.value = lan.offerUntil()
        val status = lan.message
        message.value = when {
            status.startsWith("Paired") -> "paired"
            status.startsWith("Synced") -> "synced"
            status == "Pairing…" || status == "Syncing…" -> "syncing"
            status == "not_visible" -> "not_visible"
            status.startsWith("Enter the code from") -> "self"
            status.startsWith("Enter a four-digit") -> "wrong_code"
            status.startsWith("Pairing failed:") -> status.substringAfter("Pairing failed: ") .let { if(it in setOf("wrong_code","expired","self","not_offering")) it else if(it == "io") "dropped" else "no_code" }
            status.startsWith("Could not sync") || status.startsWith("Local discovery unavailable") -> "dropped"
            status == "expired" -> "expired"
            else -> ""
        }
        delay(300)
    } } }
    fun showCode() = lan?.showCode()
    fun hideCode() = lan?.hideCode()
    fun enter(code: String) = lan?.pair(code)
    fun syncNow(id: String) = lan?.sync(id)
    override fun close() { scope.cancel(); lan?.close() }
}
