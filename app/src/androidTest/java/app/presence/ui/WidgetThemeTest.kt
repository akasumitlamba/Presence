package app.presence.ui

import androidx.compose.ui.unit.dp
import android.os.Build
import android.view.LayoutInflater
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.presence.R
import androidx.compose.material3.dynamicLightColorScheme
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetThemeTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun tabletTimerStacksInTallWindowsAndUsesColumnsInWideWindows() {
        assertFalse(timerUsesColumns(688.dp, 1120.dp))
        assertTrue(timerUsesColumns(1100.dp, 640.dp))
        assertFalse(timerUsesColumns(540.dp, 380.dp))
    }

    @Test fun wallpaperSchemeMatchesPlatformAndFixedPalettesRemainSelectable() {
        val system = platformColorScheme(context, false, "system")
        val expected = if (Build.VERSION.SDK_INT >= 31) dynamicLightColorScheme(context) else presenceColorScheme(false, "grove")
        assertEquals(expected.primary, system.primary)
        listOf("grove", "harbor", "dusk", "slate", "berry", "amber", "moss", "iris", "copper", "tide", "ink").forEach {
            assertEquals(presenceColorScheme(false, it).primary, platformColorScheme(context, false, it).primary)
        }
    }

    @Test fun allWidgetLayoutsInflateWithAnOuterInsetAndLaunchBackground() {
        val density = context.resources.displayMetrics.density
        val cellWidth = (280 * density).toInt()
        val cellHeight = (160 * density).toInt()
        val bitmap = android.graphics.Bitmap.createBitmap(cellWidth * 2, cellHeight * 2, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.rgb(92, 94, 103))
        listOf(R.layout.widget_presence, R.layout.widget_stats, R.layout.widget_streak, R.layout.widget_todos).forEachIndexed { index, layout ->
            val view = LayoutInflater.from(context).inflate(layout, null)
            val padding = (8 * context.resources.displayMetrics.density + 0.5f).toInt()
            assertEquals(padding, view.paddingLeft)
            assertEquals(padding, view.paddingTop)
            assertEquals(padding, view.paddingRight)
            assertEquals(padding, view.paddingBottom)
            assertNotNull(view.findViewById<android.view.View>(android.R.id.background))
            // Visual QA of the real XML layouts, with the empty app's normal values.
            fun text(id: Int, value: String) { view.findViewById<TextView>(id)?.text = value }
            text(R.id.widget_kicker, "Focus"); text(R.id.widget_sub, "Unlabeled")
            text(R.id.widget_plain, "20:00"); text(R.id.widget_action, "Continue")
            view.findViewById<android.view.View>(R.id.widget_time)?.visibility = android.view.View.GONE
            text(R.id.stats_kicker, "Today"); text(R.id.stats_today, "0:00"); text(R.id.stats_week, "Week · 0:00")
            text(R.id.streak_count, "0"); text(R.id.streak_label, "No streak yet")
            text(R.id.todos_title, "To-dos"); text(R.id.todos_body, "No to-dos yet")
            view.measure(android.view.View.MeasureSpec.makeMeasureSpec(cellWidth, android.view.View.MeasureSpec.EXACTLY),
                android.view.View.MeasureSpec.makeMeasureSpec(cellHeight, android.view.View.MeasureSpec.EXACTLY))
            view.layout(0, 0, cellWidth, cellHeight)
            view.findViewById<TextView>(R.id.widget_action)?.let {
                assertEquals("Timer action must fit on one line", 1, it.lineCount)
                assertEquals("Timer action must remain readable", 0, it.layout.getEllipsisCount(0))
            }
            canvas.save(); canvas.translate((index % 2 * cellWidth).toFloat(), (index / 2 * cellHeight).toFloat())
            view.draw(canvas); canvas.restore()

        }
        val dark = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
        java.io.File(context.cacheDir, "widget-layouts-${if (dark) "dark" else "light"}.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }

    @Test fun widgetTextUsesWallpaperResourceRatherThanAStoredSnapshot() {
        if (Build.VERSION.SDK_INT < 31) return
        val view = LayoutInflater.from(context).inflate(R.layout.widget_presence, null)
        assertEquals(context.getColor(R.color.widget_ink), view.findViewById<TextView>(R.id.widget_plain).currentTextColor)
        val dark = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
        assertEquals(context.getColor(if (dark) android.R.color.system_accent1_200 else android.R.color.system_accent1_600), context.getColor(R.color.widget_accent))
    }
}
