package app.canto.timer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import app.canto.CantoApp
import app.canto.core.Snap
import app.canto.core.Step
import app.canto.widget.CantoWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tick: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val app = application as CantoApp
        app.notifier.ensureChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as CantoApp
        startAsForeground(app.notifier.buildOngoing(Snap()))
        scope.launch {
            app.ready.await()
            when (intent?.action) {
                PAUSE -> app.center.pause()
                RESUME -> app.center.resume()
                STOP -> app.center.stop()
                NEXT -> {
                    val snap = app.center.snap()
                    if (snap.status == "awaiting") app.center.start(snap.awaitingKind.ifEmpty { "focus" }, null, null)
                }
                DISMISS -> app.notifier.cancelDone()
                DUE -> app.center.due(intent.getStringExtra(EXTRA_TOKEN))
                CATCHUP -> app.center.catchUp()
                else -> Unit
            }
            val snap = app.center.snap()
            if (snap.status == "running" || snap.status == "paused") {
                startAsForeground(app.notifier.ongoing(snap, force = true))
                startTick()
            } else {
                tick?.cancel()
                ServiceCompat.stopForeground(this@TimerService, ServiceCompat.STOP_FOREGROUND_DETACH)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startTick() {
        if (tick?.isActive == true) return
        val app = application as CantoApp
        tick = scope.launch {
            while (isActive) {
                delay(1000)
                app.ready.await()
                val before = app.center.snap()
                if (before.status != "running" && before.status != "paused") {
                    ServiceCompat.stopForeground(this@TimerService, ServiceCompat.STOP_FOREGROUND_DETACH)
                    stopSelf()
                    break
                }
                app.center.due(null)
                CantoWidget.update(this@TimerService)
            }
        }
    }

    private fun startAsForeground(notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(Notifier.ONGOING_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(Notifier.ONGOING_ID, notification)
        }
    }

    override fun onDestroy() {
        tick?.cancel()
        super.onDestroy()
    }

    companion object {
        const val PAUSE = "app.canto.PAUSE"
        const val RESUME = "app.canto.RESUME"
        const val STOP = "app.canto.STOP"
        const val NEXT = "app.canto.NEXT"
        const val DISMISS = "app.canto.DISMISS"
        const val DUE = "app.canto.DUE"
        const val ENSURE = "app.canto.ENSURE"
        const val CATCHUP = "app.canto.CATCHUP"
        const val EXTRA_TOKEN = "token"

        fun ensure(context: Context) {
            val intent = Intent(context, TimerService::class.java).setAction(ENSURE)
            ContextCompat.startForegroundService(context, intent)
        }

        fun send(context: Context, action: String) {
            val intent = Intent(context, TimerService::class.java).setAction(action)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}

fun CantoApp.reactTo(step: Step) {
    notifier.schedule(step.snap)
    val globals = repo.globals()
    val premium = repo.premium()
    val alarmKind = when {
        !premium && globals.alarm != "chime" -> "chime"
        globals.alarm == "custom" && !java.io.File(filesDir, "alarm-custom").exists() -> "chime"
        else -> globals.alarm
    }
    if (step.chime) player.alarm(alarmKind)
    val sound = if (premium) globals.background else "off"
    val running = step.snap.status == "running" && (step.snap.kind == "focus" || step.snap.kind == "track")
    if (running && sound != "off") player.loop(sound, globals.volume / 100f) else player.stopLoop()
    if (step.snap.status == "running" || step.snap.status == "paused") {
        notifier.cancelDone()
        try {
            TimerService.ensure(this)
        } catch (_: Exception) {
        }
    } else if (step.chime && step.snap.status == "awaiting") {
        notifier.finished(step.snap)
    }
    CantoWidget.update(this)
}
