package app.canto.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.content.ContextCompat
import app.canto.CantoApp
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val token = intent.getStringExtra(TimerService.EXTRA_TOKEN)
        val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wake = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "canto:due")
        wake.acquire(12_000L)
        Thread {
            try {
                val app = context.applicationContext as CantoApp
                runBlocking { app.ready.await() }
                app.center.due(token)
                try {
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context, TimerService::class.java).setAction(TimerService.ENSURE),
                    )
                } catch (_: Exception) {
                }
            } finally {
                if (wake.isHeld) wake.release()
                pending.finish()
            }
        }.start()
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        Thread {
            try {
                val app = context.applicationContext as CantoApp
                runBlocking { app.ready.await() }
                app.center.catchUp()
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, TimerService::class.java).setAction(TimerService.CATCHUP),
                )
            } catch (_: Exception) {
            } finally {
                pending.finish()
            }
        }.start()
    }
}
