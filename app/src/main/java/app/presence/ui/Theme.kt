package app.presence.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import app.presence.R

private val Light = lightColorScheme(
    primary = Color(0xFF14614A),
    onPrimary = Color(0xFFF4FBF7),
    primaryContainer = Color(0xFFD3EDE2),
    onPrimaryContainer = Color(0xFF062016),
    secondary = Color(0xFF8A451F),
    onSecondary = Color(0xFFFFFBF5),
    secondaryContainer = Color(0xFFF6D8C6),
    onSecondaryContainer = Color(0xFF2A140C),
    tertiary = Color(0xFF8A6410),
    onTertiary = Color(0xFFFFFBF5),
    tertiaryContainer = Color(0xFFF8E6BE),
    onTertiaryContainer = Color(0xFF2A1C04),
    background = Color(0xFFF6F1E8),
    onBackground = Color(0xFF1C2420),
    surface = Color(0xFFFFFBF5),
    onSurface = Color(0xFF1C2420),
    surfaceVariant = Color(0xFFE4D9C8),
    onSurfaceVariant = Color(0xFF3A463F),
    outline = Color(0xFF5C6A62),
    outlineVariant = Color(0xFFC9BBA8),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF9EE0C0),
    onPrimary = Color(0xFF043024),
    primaryContainer = Color(0xFF1F6F54),
    onPrimaryContainer = Color(0xFFE7FFF4),
    secondary = Color(0xFFFFC7A6),
    onSecondary = Color(0xFF3A1A0C),
    secondaryContainer = Color(0xFF6B3A22),
    onSecondaryContainer = Color(0xFFFFEDE4),
    tertiary = Color(0xFFFFE08A),
    onTertiary = Color(0xFF3A2A04),
    tertiaryContainer = Color(0xFF6A5414),
    onTertiaryContainer = Color(0xFFFFF3CC),
    background = Color(0xFF121816),
    onBackground = Color(0xFFF3F6F4),
    surface = Color(0xFF1C2622),
    onSurface = Color(0xFFF3F6F4),
    surfaceVariant = Color(0xFF2C3A34),
    onSurfaceVariant = Color(0xFFD5E3DB),
    outline = Color(0xFFB7CCC2),
    outlineVariant = Color(0xFF6E857B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val SourceSans = FontFamily(
    Font(R.font.source_sans_3_regular, FontWeight.Normal),
    Font(R.font.source_sans_3_medium, FontWeight.Medium),
    Font(R.font.source_sans_3_semibold, FontWeight.SemiBold),
)

private fun face(weight: FontWeight, size: Int, line: Int) = TextStyle(
    fontFamily = SourceSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
)

private val Type = Typography(
    displayLarge = face(FontWeight.SemiBold, 56, 64),
    displayMedium = face(FontWeight.SemiBold, 44, 52),
    displaySmall = face(FontWeight.SemiBold, 36, 44),
    headlineLarge = face(FontWeight.SemiBold, 32, 40),
    headlineMedium = face(FontWeight.SemiBold, 28, 36),
    headlineSmall = face(FontWeight.Medium, 24, 32),
    titleLarge = face(FontWeight.SemiBold, 22, 28),
    titleMedium = face(FontWeight.Medium, 16, 24),
    titleSmall = face(FontWeight.Medium, 14, 20),
    bodyLarge = face(FontWeight.Normal, 16, 24),
    bodyMedium = face(FontWeight.Normal, 14, 20),
    bodySmall = face(FontWeight.Normal, 12, 16),
    labelLarge = face(FontWeight.Medium, 14, 20),
    labelMedium = face(FontWeight.Medium, 12, 16),
    labelSmall = face(FontWeight.Medium, 11, 16),
)

@Composable
fun PresenceTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }
    MaterialTheme(colorScheme = if (dark) Dark else Light, typography = Type, content = content)
}

val Swatches = listOf(
    0x14614A, 0xC56A3A, 0xC8922A, 0x3E6B8A, 0x6E4A7A, 0x8C4A4A, 0x3F6F62, 0x4E5A3A,
)

fun colorOf(hex: String): Color {
    val value = hex.removePrefix("#").toLongOrNull(16) ?: Swatches.first().toLong()
    return Color(0xFF000000 or value)
}

fun chartColor(hex: String, dark: Boolean): Color {
    val base = colorOf(hex)
    if (!dark) return base
    return Color(
        red = base.red + (1f - base.red) * 0.42f,
        green = base.green + (1f - base.green) * 0.42f,
        blue = base.blue + (1f - base.blue) * 0.42f,
    )
}

fun Color.isDark(): Boolean = luminance() < 0.45f
