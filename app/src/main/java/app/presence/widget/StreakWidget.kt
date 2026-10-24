package app.presence.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import app.presence.PresenceApp
import app.presence.R

class StreakWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        update(context)
    }

    companion object {
        fun update(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, StreakWidget::class.java))
            if (ids.isEmpty()) return
            val app = context.applicationContext as? PresenceApp ?: return
            if (!app.hasRuntime()) return
            val t = app.strings()
            val stats = app.focusStats()
            val views = RemoteViews(context.packageName, R.layout.widget_streak)
            views.setTextViewText(R.id.streak_count, stats.streak.toString())
            views.setTextViewText(R.id.streak_label, t.streakLine(stats.streak, stats.logged))
            views.setOnClickPendingIntent(R.id.widget_root, openApp(context, 13, "stats"))
            ids.forEach { manager.updateAppWidget(it, views) }
        }
    }
}
