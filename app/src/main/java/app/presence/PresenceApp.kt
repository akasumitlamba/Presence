package app.presence

import android.app.Application
import app.presence.core.Node
import app.presence.data.Repo
import app.presence.sound.Player
import app.presence.sync.Lan
import app.presence.timer.Notifier
import app.presence.timer.Reminders
import app.presence.timer.TimerCenter
import app.presence.widget.HomeWidgets
import app.presence.timer.reactTo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PresenceApp : Application() {
    lateinit var repo: Repo
    lateinit var center: TimerCenter
    lateinit var player: Player
    lateinit var notifier: Notifier
    lateinit var node: Node
    lateinit var lan: Lan
    val ready = CompletableDeferred<Unit>()
    val startupFailed = kotlinx.coroutines.flow.MutableStateFlow(false)
    private var startupJob: kotlinx.coroutines.Job? = null

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        if (ready.isCompleted) HomeWidgets.update(this)
    }

    fun retryStartup() {
        if (ready.isCompleted || startupJob?.isActive == true) return
        startupFailed.value = false
        startupJob = scope.launch {
            try { initialize() } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                android.util.Log.e("Presence", "Could not open local data", error)
                startupFailed.value = true
            }
        }
    }

    fun hasLan(): Boolean = ::lan.isInitialized

    fun hasRuntime(): Boolean = ::repo.isInitialized && ::center.isInitialized
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        player = Player(this)
        notifier = Notifier(this)
        notifier.ensureChannels()
        repo = Repo(this)
        center = TimerCenter(repo) { step -> reactTo(step) }
        retryStartup()
    }

    private fun initialize() {
        repo.open()
        center.catchUp()
        Reminders.reschedule(this@PresenceApp)
        HomeWidgets.update(this@PresenceApp)
        try {
            val started = Node(repo, newSocket = {
                val network = app.presence.sync.localNetwork(this@PresenceApp)
                    ?: throw java.io.IOException("No local network")
                network.socketFactory.createSocket()
            }) { System.currentTimeMillis() }
            started.start()
            node = started
            val link = Lan(this@PresenceApp, repo, started)
            lan = link
            link.start()
        } catch (_: Exception) {
            // Local discovery being unavailable must not disable the offline timer.
        }
        ready.complete(Unit)
    }
}
