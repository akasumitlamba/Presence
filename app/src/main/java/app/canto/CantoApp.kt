package app.canto

import android.app.Application
import app.canto.core.Node
import app.canto.data.Repo
import app.canto.sound.Player
import app.canto.sync.Lan
import app.canto.timer.Notifier
import app.canto.timer.TimerCenter
import app.canto.timer.reactTo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CantoApp : Application() {
    lateinit var repo: Repo
    lateinit var center: TimerCenter
    lateinit var player: Player
    lateinit var notifier: Notifier
    lateinit var node: Node
    lateinit var lan: Lan
    val ready = CompletableDeferred<Unit>()

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
        scope.launch {
            repo.open()
            val started = Node(repo) { System.currentTimeMillis() }
            started.start()
            node = started
            val link = Lan(this@CantoApp, repo, started)
            lan = link
            link.start()
            ready.complete(Unit)
            center.catchUp()
        }
    }
}
