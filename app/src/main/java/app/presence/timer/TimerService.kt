package app.presence.timer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import app.presence.PresenceApp
import app.presence.core.Snap
import app.presence.core.Step
import app.presence.widget.HomeWidgets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tick: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val app = application as PresenceApp
        app.notifier.ensureChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as PresenceApp
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
                START -> {
                    val snap = app.center.snap()
                    when (snap.status) {
                        "paused" -> app.center.resume()
                        "running" -> Unit
                        "awaiting" -> app.center.start(snap.awaitingKind.ifEmpty { "focus" }, null, null)
                        else -> app.center.start("focus", null, null)
                    }
                }
                DISMISS -> app.notifier.cancelDone()
                DUE -> app.center.due(intent.getStringExtra(EXTRA_TOKEN))
                CATCHUP -> app.center.catchUp()
                else -> if (intent == null) app.center.catchUp()
            }
            val snap = app.center.snap()
            if (snap.status == "running" || snap.status == "paused") {
                startAsForeground(app.notifier.ongoing(snap, force = true))
                startTick()
            } else {
                tick?.cancel()
                ServiceCompat.stopForeground(this@TimerService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startTick() {
        if (tick?.isActive == true) return
        val app = application as PresenceApp
        tick = scope.launch {
            while (isActive) {
                delay(1000)
                app.ready.await()
                val before = app.center.snap()
                if (before.status != "running" && before.status != "paused") {
                    ServiceCompat.stopForeground(this@TimerService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    break
                }
                app.center.due(null)
                HomeWidgets.update(this@TimerService)
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
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val PAUSE = "app.presence.PAUSE"
        const val RESUME = "app.presence.RESUME"
        const val STOP = "app.presence.STOP"
        const val NEXT = "app.presence.NEXT"
        const val DISMISS = "app.presence.DISMISS"
        const val DUE = "app.presence.DUE"
        const val ENSURE = "app.presence.ENSURE"
        const val CATCHUP = "app.presence.CATCHUP"
        const val START = "app.presence.START"
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

fun PresenceApp.reactTo(step: Step) {
    notifier.schedule(step.snap)
    val globals = repo.globals()
    val alarmKind = when {
        globals.alarm == "custom" && !java.io.File(app.presence.data.AppStorage.privateDir(this), "alarm-custom").exists() -> "chime"
        else -> globals.alarm
    }
    if (step.chime) player.alarm(alarmKind)
    val sound = globals.background
    val running = step.snap.status == "running" && (step.snap.kind == "focus" || step.snap.kind == "track")
    if (running && sound != "off") player.loop(sound, globals.volume / 100f) else player.stopLoop()
    if (step.snap.status == "running" || step.snap.status == "paused") {
        notifier.cancelDone()
        try {
            TimerService.ensure(this)
        } catch (_: Exception) {
        }
    } else {
        notifier.cancelOngoing()
        if (step.chime) notifier.finished(step.snap) else notifier.cancelDone()
    }
    HomeWidgets.update(this)
}
