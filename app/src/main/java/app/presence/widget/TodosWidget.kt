package app.presence.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import app.presence.PresenceApp
import app.presence.R

class TodosWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        update(context)
    }

    companion object {
        fun update(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TodosWidget::class.java))
            if (ids.isEmpty()) return
            val app = context.applicationContext as? PresenceApp ?: return
            if (!app.hasRuntime()) return
            val t = app.strings()
            val open = app.repo.todos().filter { !it.done }.take(4)
            val body = if (open.isEmpty()) t.emptyTodos else open.joinToString("\n") { "· ${it.title}" }
            val views = RemoteViews(context.packageName, R.layout.widget_todos)
            views.applyTheme(context, app, intArrayOf(R.id.todos_body), intArrayOf(), intArrayOf(R.id.todos_title), false)
            views.setTextViewText(R.id.todos_title, t.todos)
            views.setTextViewText(R.id.todos_body, body)
            views.setOnClickPendingIntent(R.id.widget_root, openApp(context, 14, "todos"))
            ids.forEach { manager.updateAppWidget(it, views) }
        }
    }
}
