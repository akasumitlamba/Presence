package app.presence.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import app.presence.PresenceApp
import app.presence.R
import app.presence.core.clockDigits

class StatsWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        update(context)
    }

    companion object {
        fun update(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, StatsWidget::class.java))
            if (ids.isEmpty()) return
            val app = context.applicationContext as? PresenceApp ?: return
            if (!app.hasRuntime()) return
            val t = app.strings()
            val stats = app.focusStats()
            val views = RemoteViews(context.packageName, R.layout.widget_stats)
            views.applyTheme(context, app, intArrayOf(R.id.stats_today), intArrayOf(R.id.stats_week), intArrayOf(R.id.stats_kicker), false)
            views.setTextViewText(R.id.stats_kicker, t.today)
            views.setTextViewText(R.id.stats_today, clockDigits(stats.today))
            views.setTextViewText(R.id.stats_week, t.week + " · " + clockDigits(stats.week))
            views.setOnClickPendingIntent(R.id.widget_root, openApp(context, 12, "stats"))
            ids.forEach { manager.updateAppWidget(it, views) }
        }
    }
}
