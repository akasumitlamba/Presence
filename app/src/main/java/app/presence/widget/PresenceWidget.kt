package app.presence.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import app.presence.PresenceApp
import app.presence.R
import app.presence.core.clockDigits
import app.presence.core.elapsedWork
import app.presence.core.timeLeft
import app.presence.core.stretchCount
import app.presence.core.stretchLengths
import app.presence.timer.TimerService

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
            val t = app.strings()
            val snap = app.center.snap()
            val views = RemoteViews(context.packageName, R.layout.widget_presence)
            views.setOnClickPendingIntent(R.id.widget_root, openApp(context, 11, "timer"))
            val phase = when (snap.kind) {
                "short" -> t.shortRest
                "long" -> t.longRest
                "track" -> t.track
                else -> t.focus
            }
            val (action, label) = if (snap.status == "running") {
                TimerService.PAUSE to t.pause
            } else {
                TimerService.START to t.play
            }
            views.setTextViewText(R.id.widget_kicker, if (snap.status == "idle") t.focus else phase)
            views.setTextViewText(R.id.widget_sub, snap.projectName.ifBlank { t.unlabeled })
            views.setTextViewText(R.id.widget_action, label)
            views.setOnClickPendingIntent(R.id.widget_action, serviceClick(context, action))
            val nowE = System.currentTimeMillis()
            val nowL = SystemClock.elapsedRealtime()
            if (snap.status == "running" && snap.kind != "track") {
                views.setViewVisibility(R.id.widget_plain, View.GONE)
                views.setViewVisibility(R.id.widget_time, View.VISIBLE)
                views.setChronometer(R.id.widget_time, SystemClock.elapsedRealtime() + timeLeft(snap, nowE, nowL), null, true)
                views.setChronometerCountDown(R.id.widget_time, true)
            } else if (snap.status == "running") {
                views.setViewVisibility(R.id.widget_plain, View.GONE)
                views.setViewVisibility(R.id.widget_time, View.VISIBLE)
                views.setChronometer(R.id.widget_time, SystemClock.elapsedRealtime() - elapsedWork(snap, nowE, nowL), null, true)
                views.setChronometerCountDown(R.id.widget_time, false)
            } else {
                views.setViewVisibility(R.id.widget_time, View.GONE)
                views.setViewVisibility(R.id.widget_plain, View.VISIBLE)
                val globals = app.repo.globals()
                val idle = stretchLengths(globals.workMillis, stretchCount(globals.shortCount, globals.longCount)).first()
                val shown = if (snap.status == "idle") idle else if (snap.kind == "track") elapsedWork(snap, nowE, nowL) else timeLeft(snap, nowE, nowL)
                views.setTextViewText(R.id.widget_plain, clockDigits(shown))
            }
            return views
        }
    }
}
