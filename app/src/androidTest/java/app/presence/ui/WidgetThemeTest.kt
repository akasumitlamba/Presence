package app.presence.ui

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

    @Test fun wallpaperSchemeMatchesPlatformAndFixedPalettesRemainSelectable() {
        val system = platformColorScheme(context, false, "system")
        val expected = if (Build.VERSION.SDK_INT >= 31) dynamicLightColorScheme(context) else presenceColorScheme(false, "grove")
        assertEquals(expected.primary, system.primary)
        listOf("grove", "harbor", "dusk", "slate", "berry", "amber", "moss", "iris", "copper", "tide", "ink").forEach {
            assertEquals(presenceColorScheme(false, it).primary, platformColorScheme(context, false, it).primary)
        }
    }

    @Test fun allWidgetLayoutsInflateWithAnOuterInsetAndLaunchBackground() {
        listOf(R.layout.widget_presence, R.layout.widget_stats, R.layout.widget_streak, R.layout.widget_todos).forEach {
            val view = LayoutInflater.from(context).inflate(it, null)
            val padding = (8 * context.resources.displayMetrics.density + 0.5f).toInt()
            assertEquals(padding, view.paddingLeft)
            assertEquals(padding, view.paddingTop)
            assertEquals(padding, view.paddingRight)
            assertEquals(padding, view.paddingBottom)
            assertNotNull(view.findViewById<android.view.View>(android.R.id.background))
        }
    }

    @Test fun widgetTextUsesWallpaperResourceRatherThanAStoredSnapshot() {
        if (Build.VERSION.SDK_INT < 31) return
        val view = LayoutInflater.from(context).inflate(R.layout.widget_presence, null)
        assertEquals(context.getColor(R.color.widget_ink), view.findViewById<TextView>(R.id.widget_plain).currentTextColor)
        val dark = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
        assertEquals(context.getColor(if (dark) android.R.color.system_accent1_200 else android.R.color.system_accent1_600), context.getColor(R.color.widget_accent))
    }
}
