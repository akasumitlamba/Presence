package app.presence.timer

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import app.presence.MainActivity
import app.presence.R
import app.presence.core.Snap
import app.presence.core.clockDigits
import app.presence.core.displayMillis
import app.presence.core.elapsedWork
import app.presence.ui.T
import app.presence.ui.resolveLang

class Notifier(private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)
    private var shownToken = ""
    private var shownStatus = ""

    fun ensureChannels() {
        val ongoing = NotificationChannel(ONGOING, "Focus in progress", NotificationManager.IMPORTANCE_LOW).apply {
            setSound(null, null)
            enableVibration(false)
        }
        val done = NotificationChannel(DONE, "Focus finished", NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(null, null)
        }
        val remind = NotificationChannel(REMIND, "Reminders", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(ongoing)
        manager.createNotificationChannel(done)
        manager.createNotificationChannel(remind)
    }

    fun ongoing(snap: Snap, force: Boolean = false): Notification {
        if (!force && shownToken == snap.completionToken && shownStatus == snap.status && snap.status == "running") {
            return buildOngoing(snap)
        }
        shownToken = snap.completionToken
        shownStatus = snap.status
        return buildOngoing(snap)
    }

    fun buildOngoing(snap: Snap): Notification {
        val t = T(language())
        val phase = phaseName(t, snap.kind)
        val nowE = System.currentTimeMillis()
        val nowL = android.os.SystemClock.elapsedRealtime()
        val title = if (snap.projectName.isBlank()) phase else "$phase · ${snap.projectName}"
        val builder = NotificationCompat.Builder(context, ONGOING)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(title)
            .setContentIntent(openIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        if (snap.status == "running" && snap.kind != "track") {
            builder.setWhen(snap.alarmAtEpoch)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setContentText(t.remaining)
        } else if (snap.status == "running") {
            val elapsed = elapsedWork(snap, nowE, nowL)
            builder.setWhen(nowE - elapsed)
                .setUsesChronometer(true)
                .setContentText(phase)
        } else {
            builder.setUsesChronometer(false)
                .setContentText(t.paused + " " + clockDigits(displayMillis(snap, nowE, nowL)))
        }
        if (snap.status == "running") {
            builder.addAction(0, t.pause, serviceIntent(TimerService.PAUSE))
        } else if (snap.status == "paused") {
            builder.addAction(0, t.play, serviceIntent(TimerService.RESUME))
        }
        builder.addAction(0, t.stop, serviceIntent(TimerService.STOP))
        return builder.build()
    }

    fun finished(snap: Snap) {
        val t = T(language())
        val phase = phaseName(t, snap.awaitingKind.ifEmpty { "focus" })
        val text = t.ready.replace("{phase}", phase)
        val builder = NotificationCompat.Builder(context, DONE)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(t.finished)
            .setContentText(text)
            .setContentIntent(openIntent())
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(0, t.play, serviceIntent(TimerService.NEXT))
            .addAction(0, t.dismiss, dismissIntent())
        manager.notify(DONE_ID, builder.build())
    }

    fun cancelDone() {
        manager.cancel(DONE_ID)
    }

    fun schedule(snap: Snap) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val pending = alarmIntent(snap.completionToken)
        if (snap.status != "running" || snap.kind == "track" || snap.alarmAtEpoch <= 0L) {
            alarm.cancel(pending)
            return
        }
        val show = PendingIntent.getActivity(
            context,
            3,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val info = AlarmManager.AlarmClockInfo(snap.alarmAtEpoch, show)
        try {
            alarm.setAlarmClock(info, pending)
        } catch (_: SecurityException) {
            try {
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snap.alarmAtEpoch, pending)
            } catch (_: SecurityException) {
                alarm.set(AlarmManager.RTC_WAKEUP, snap.alarmAtEpoch, pending)
            }
        }
    }

    private fun alarmIntent(token: String): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).putExtra(TimerService.EXTRA_TOKEN, token)
        return PendingIntent.getBroadcast(context, 42, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun serviceIntent(action: String): PendingIntent {
        val intent = Intent(context, TimerService::class.java).setAction(action)
        return PendingIntent.getService(context, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun dismissIntent(): PendingIntent {
        val intent = Intent(context, TimerService::class.java).setAction(TimerService.DISMISS)
        return PendingIntent.getService(context, 9, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun openIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun language(): String {
        val pref = (context.applicationContext as app.presence.PresenceApp).repo.flags()["language"] ?: "system"
        return resolveLang(pref)
    }

    private fun phaseName(t: T, kind: String): String = when (kind) {
        "short" -> t.shortRest
        "long" -> t.longRest
        "track" -> t.track
        else -> t.focus
    }

    companion object {
        const val ONGOING = "presence.ongoing"
        const val DONE = "presence.done"
        const val REMIND = "presence.remind"
        const val ONGOING_ID = 7
        const val DONE_ID = 8
    }
}

fun foregroundType(): Int {
    return if (Build.VERSION.SDK_INT >= 34) android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
}
