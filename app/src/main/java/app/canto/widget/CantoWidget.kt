package app.canto.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import app.canto.CantoApp
import app.canto.MainActivity
import app.canto.R
import app.canto.core.KIND_SESSION
import app.canto.core.clockDigits
import app.canto.core.dayKey
import app.canto.core.displayMillis
import app.canto.core.elapsedWork
import app.canto.core.sessionCountedMillis
import app.canto.timer.TimerService
import app.canto.ui.T
import app.canto.ui.resolveLang
import java.time.ZoneId

class CantoWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        update(context)
    }

    companion object {
        fun update(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, CantoWidget::class.java))
            if (ids.isEmpty()) return
            val app = context.applicationContext as? CantoApp ?: return
            if (!app.hasRuntime()) return
            val views = render(context, app)
            ids.forEach { manager.updateAppWidget(it, views) }
        }

        private fun render(context: Context, app: CantoApp): RemoteViews {
            val t = T(resolveLang(app.repo.flags()["language"] ?: "system"))
            val snap = app.center.snap()
            val premium = app.repo.premium()
            val views = RemoteViews(context.packageName, R.layout.widget_canto)
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
                views.setTextViewText(R.id.widget_sub, t.openCanto)
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

        private fun todayMillis(app: CantoApp): Long {
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
