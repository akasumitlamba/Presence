package app.presence.timer

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import app.presence.MainActivity
import app.presence.PresenceApp
import app.presence.R
import app.presence.core.loggedToday
import app.presence.core.nextClockMillis
import app.presence.core.streakDays
import app.presence.ui.T
import app.presence.ui.resolveLang
import java.time.ZoneId
import kotlinx.coroutines.runBlocking

object Reminders {
    const val ACTION = "app.presence.REMIND"
    const val EXTRA_KIND = "kind"
    const val DAILY = "daily"
    const val STREAK = "streak"

    fun reschedule(context: Context) {
        val app = context.applicationContext as? PresenceApp ?: return
        if (!app.hasRuntime()) return
        val globals = app.repo.globals()
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        cancel(context, DAILY)
        cancel(context, STREAK)
        if (globals.dailyOn) {
            schedule(context, DAILY, nextClockMillis(globals.dailyHour, globals.dailyMinute, now, zone))
        }
        if (globals.streakOn) {
            schedule(context, STREAK, nextClockMillis(globals.streakHour, globals.streakMinute, now, zone))
        }
    }

    fun show(app: PresenceApp, kind: String) {
        app.notifier.ensureChannels()
        val t = T(resolveLang(app.repo.flags()["language"] ?: "system"))
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val hour = app.repo.globals().dayStart
        val records = app.repo.snapshot()
        val title: String
        val text: String
        val id: Int
        if (kind == STREAK) {
            val count = streakDays(records, now, zone, hour)
            val logged = loggedToday(records, now, zone, hour)
            title = t.streak
            text = t.streakLine(count, logged)
            id = 22
        } else {
            title = t.dailyReminder
            text = t.timeToFocus
            id = 21
        }
        val open = PendingIntent.getActivity(
            app,
            15,
            Intent(app, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(app, Notifier.REMIND)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        app.getSystemService(NotificationManager::class.java).notify(id, notification)
    }

    private fun schedule(context: Context, kind: String, whenMs: Long) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val pending = pending(context, kind)
        try {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMs, pending)
        } catch (_: SecurityException) {
            try {
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMs, pending)
            } catch (_: Exception) {
                alarm.set(AlarmManager.RTC_WAKEUP, whenMs, pending)
            }
        }
    }

    private fun cancel(context: Context, kind: String) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        alarm.cancel(pending(context, kind))
    }

    private fun pending(context: Context, kind: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(ACTION)
            .putExtra(EXTRA_KIND, kind)
        val code = if (kind == STREAK) 82 else 81
        return PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Reminders.ACTION) return
        val kind = intent.getStringExtra(Reminders.EXTRA_KIND) ?: return
        val pending = goAsync()
        val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wake = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "presence:remind")
        wake.acquire(12_000L)
        Thread {
            try {
                val app = context.applicationContext as PresenceApp
                runBlocking { app.ready.await() }
                Reminders.show(app, kind)
                Reminders.reschedule(app)
            } catch (_: Exception) {
            } finally {
                if (wake.isHeld) wake.release()
                pending.finish()
            }
        }.start()
    }
}
