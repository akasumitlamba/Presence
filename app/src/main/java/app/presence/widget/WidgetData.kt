package app.presence.widget

import androidx.compose.ui.graphics.toArgb
import app.presence.ui.presenceColorScheme
import app.presence.R
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import app.presence.MainActivity
import app.presence.PresenceApp
import app.presence.core.dayKey
import app.presence.core.dayTotals
import app.presence.core.loggedToday
import app.presence.core.streakDays
import app.presence.timer.TimerService
import app.presence.ui.T
import app.presence.ui.resolveLang
import java.time.ZoneId

internal fun PresenceApp.strings(): T = T(resolveLang(repo.flags()["language"] ?: "system"))

internal data class FocusStats(val today: Long, val week: Long, val streak: Int, val logged: Boolean)

internal fun PresenceApp.focusStats(): FocusStats {
    val zone = ZoneId.systemDefault()
    val hour = repo.globals().dayStart
    val now = System.currentTimeMillis()
    val records = repo.snapshot()
    val today = dayKey(now, zone, hour)
    val week = (0..6).map { today.minusDays(it.toLong()) }
    val totals = dayTotals(records, week, zone, hour)
    return FocusStats(
        today = totals.firstOrNull() ?: 0L,
        week = totals.sum(),
        streak = streakDays(records, now, zone, hour),
        logged = loggedToday(records, now, zone, hour),
    )
}

internal fun openApp(context: Context, request: Int, tab: String? = null): PendingIntent {
    val intent = Intent(context, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    if (tab != null) intent.putExtra(MainActivity.EXTRA_TAB, tab)
    return PendingIntent.getActivity(context, request, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
}

internal fun serviceClick(context: Context, action: String): PendingIntent {
    val intent = Intent(context, TimerService::class.java).setAction(action)
    return PendingIntent.getService(context, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
}

object HomeWidgets {
    fun update(context: Context) {
        PresenceWidget.update(context)
        StatsWidget.update(context)
        StreakWidget.update(context)
        TodosWidget.update(context)
    }
}

/** Widgets share the same palette and theme preference as the Compose app. */
internal fun android.widget.RemoteViews.applyTheme(
    context: Context, app: PresenceApp, text: IntArray, secondary: IntArray, accent: IntArray, timer: Boolean,
) {
    val flags = app.repo.flags()
    val dark = when (flags["theme"]) {
        "dark" -> true
        "light" -> false
        else -> context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
    val colors = presenceColorScheme(dark, flags["palette"] ?: "grove")
    setInt(R.id.widget_background, "setColorFilter", colors.surfaceContainer.toArgb())
    setInt(R.id.widget_icon, "setColorFilter", colors.primary.toArgb())
    text.forEach { setTextColor(it, colors.onSurface.toArgb()) }
    secondary.forEach { setTextColor(it, colors.onSurfaceVariant.toArgb()) }
    accent.forEach { setTextColor(it, colors.primary.toArgb()) }
    if (timer) {
        setInt(R.id.widget_action_background, "setColorFilter", colors.primary.toArgb())
        setTextColor(R.id.widget_action, colors.onPrimary.toArgb())
    }
}
