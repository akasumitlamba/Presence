package app.canto.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Leaf = Color(0xFF1F6F54)
private val LeafDark = Color(0xFF7DCAAA)
private val Clay = Color(0xFFC56A3A)
private val Gold = Color(0xFFC8922A)
private val Paper = Color(0xFFF6F1E8)
private val PaperCard = Color(0xFFFFFBF5)
private val Ink = Color(0xFF1C2420)
private val Night = Color(0xFF101816)
private val NightCard = Color(0xFF1A2420)

private val Light = lightColorScheme(
    primary = Leaf,
    onPrimary = Paper,
    secondary = Clay,
    onSecondary = Paper,
    tertiary = Gold,
    background = Paper,
    onBackground = Ink,
    surface = PaperCard,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE7E0D4),
    outline = Color(0xFFD9D0C3),
)

private val Dark = darkColorScheme(
    primary = LeafDark,
    onPrimary = Color(0xFF10241C),
    secondary = Color(0xFFE39A6E),
    onSecondary = Color(0xFF2A140C),
    tertiary = Color(0xFFE6C36A),
    background = Night,
    onBackground = Paper,
    surface = NightCard,
    onSurface = Paper,
    surfaceVariant = Color(0xFF24302B),
    outline = Color(0xFF3A4942),
)

private val Type = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 64.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 28.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp),
)

@Composable
fun CantoTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, typography = Type, content = content)
}

val Swatches = listOf(
    0x1F6F54, 0xC56A3A, 0xC8922A, 0x3E6B8A, 0x6E4A7A, 0x8C4A4A, 0x3F6F62, 0x4E5A3A,
)

fun colorOf(hex: String): Color {
    val value = hex.removePrefix("#").toLongOrNull(16) ?: Swatches.first().toLong()
    return Color(0xFF000000 or value)
}
