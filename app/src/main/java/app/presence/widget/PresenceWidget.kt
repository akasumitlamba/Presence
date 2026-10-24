package app.presence.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import app.presence.PresenceApp
import app.presence.MainActivity
import app.presence.R
import app.presence.core.KIND_SESSION
import app.presence.core.clockDigits
import app.presence.core.dayKey
import app.presence.core.displayMillis
import app.presence.core.elapsedWork
import app.presence.core.sessionCountedMillis
import app.presence.timer.TimerService
import app.presence.ui.T
import app.presence.ui.resolveLang
import java.time.ZoneId

class PresenceWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        update(context)
    }

    companion object {
        fun update(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PresenceWidget::class.java))
            if (ids.isEmpty()) return
            val app = context.applicationContext as? PresenceApp ?: return
            if (!app.hasRuntime()) return
            val views = render(context, app)
            ids.forEach { manager.updateAppWidget(it, views) }
        }

        private fun render(context: Context, app: PresenceApp): RemoteViews {
            val t = T(resolveLang(app.repo.flags()["language"] ?: "system"))
            val snap = app.center.snap()
            val premium = app.repo.premium()
            val views = RemoteViews(context.packageName, R.layout.widget_presence)
            val open = PendingIntent.getActivity(
                context,
                11,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            views.setOnClickPendingIntent(R.id.widget_root, open)
            val live = premium && (snap.status == "running" || snap.status == "paused")
            if (!live) {
                val today = todayMillis(app)
                views.setViewVisibility(R.id.widget_time, View.GONE)
                views.setViewVisibility(R.id.widget_plain, View.VISIBLE)
                views.setViewVisibility(R.id.widget_action, View.GONE)
                views.setTextViewText(R.id.widget_kicker, t.widgetToday)
                views.setTextViewText(R.id.widget_plain, clockDigits(today))
                views.setTextViewText(R.id.widget_sub, t.openPresence)
                return views
            }
            val nowE = System.currentTimeMillis()
            val nowL = SystemClock.elapsedRealtime()
            val phase = when (snap.kind) {
                "short" -> t.shortRest
                "long" -> t.longRest
                "track" -> t.track
                else -> t.focus
            }
            views.setTextViewText(R.id.widget_kicker, phase)
            views.setTextViewText(R.id.widget_sub, snap.projectName.ifBlank { t.unlabeled })
            views.setViewVisibility(R.id.widget_action, View.VISIBLE)
            val action = if (snap.status == "paused") TimerService.RESUME else TimerService.PAUSE
            val label = if (snap.status == "paused") t.resume else t.pause
            views.setTextViewText(R.id.widget_action, label)
            views.setOnClickPendingIntent(
                R.id.widget_action,
                PendingIntent.getService(
                    context,
                    action.hashCode(),
                    Intent(context, TimerService::class.java).setAction(action),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
            if (snap.status == "running" && snap.kind != "track" && !snap.countUp) {
                views.setViewVisibility(R.id.widget_plain, View.GONE)
                views.setViewVisibility(R.id.widget_time, View.VISIBLE)
                views.setChronometer(R.id.widget_time, SystemClock.elapsedRealtime() + displayMillis(snap, nowE, nowL), null, true)
                views.setChronometerCountDown(R.id.widget_time, true)
            } else if (snap.status == "running") {
                views.setViewVisibility(R.id.widget_plain, View.GONE)
                views.setViewVisibility(R.id.widget_time, View.VISIBLE)
                views.setChronometer(R.id.widget_time, SystemClock.elapsedRealtime() - elapsedWork(snap, nowE, nowL), null, true)
                views.setChronometerCountDown(R.id.widget_time, false)
            } else {
                views.setViewVisibility(R.id.widget_time, View.GONE)
                views.setViewVisibility(R.id.widget_plain, View.VISIBLE)
                views.setTextViewText(R.id.widget_plain, clockDigits(displayMillis(snap, nowE, nowL)))
            }
            return views
        }

        private fun todayMillis(app: PresenceApp): Long {
            val zone = ZoneId.systemDefault()
            val startHour = app.repo.globals().dayStart
            val today = dayKey(System.currentTimeMillis(), zone, startHour)
            return app.repo.snapshot().filter { it.kind == KIND_SESSION }.sumOf { rec ->
                val start = rec.body["startedAt"]?.toLongOrNull() ?: return@sumOf 0L
                if (dayKey(start, zone, startHour) != today) 0L else sessionCountedMillis(rec)
            }
        }
    }
}
