package app.presence.ui

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import app.presence.R

private val Light = lightColorScheme(
    primary = Color(0xFF0E5C42),
    onPrimary = Color(0xFFF4FBF7),
    primaryContainer = Color(0xFFC9EBDD),
    onPrimaryContainer = Color(0xFF062016),
    inversePrimary = Color(0xFF8FE0C4),
    secondary = Color(0xFF1A4A3A),
    onSecondary = Color(0xFFF4FBF7),
    secondaryContainer = Color(0xFFD5EDE3),
    onSecondaryContainer = Color(0xFF0C241C),
    tertiary = Color(0xFF1A4554),
    onTertiary = Color(0xFFF4FBFD),
    tertiaryContainer = Color(0xFFD3EAF1),
    onTertiaryContainer = Color(0xFF062028),
    background = Color(0xFFF3F6F4),
    onBackground = Color(0xFF10241C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF10241C),
    surfaceVariant = Color(0xFFE4EEE9),
    onSurfaceVariant = Color(0xFF24382E),
    surfaceTint = Color(0xFFFFFFFF),
    inverseSurface = Color(0xFF172420),
    inverseOnSurface = Color(0xFFF4F8F6),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFF2A463C),
    outlineVariant = Color(0xFF3E5C50),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFE7EFEC),
    surfaceContainer = Color(0xFFEEF4F1),
    surfaceContainerHigh = Color(0xFFE4EEE9),
    surfaceContainerHighest = Color(0xFFD9E6E0),
    surfaceContainerLow = Color(0xFFF7FAF8),
    surfaceContainerLowest = Color(0xFFFFFFFF),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF145C48),
    onPrimary = Color(0xFFF4FBF7),
    primaryContainer = Color(0xFF1B5C46),
    onPrimaryContainer = Color(0xFFE9FFF6),
    inversePrimary = Color(0xFF0E5C42),
    secondary = Color(0xFF1E4A3C),
    onSecondary = Color(0xFFE7F7F0),
    secondaryContainer = Color(0xFF1E4A3C),
    onSecondaryContainer = Color(0xFFE7F7F0),
    tertiary = Color(0xFF1A4554),
    onTertiary = Color(0xFFE7F6FB),
    tertiaryContainer = Color(0xFF1A4554),
    onTertiaryContainer = Color(0xFFE7F6FB),
    background = Color(0xFF101816),
    onBackground = Color(0xFFF4F8F6),
    surface = Color(0xFF172420),
    onSurface = Color(0xFFF4F8F6),
    surfaceVariant = Color(0xFF24342E),
    onSurfaceVariant = Color(0xFFD5E6DE),
    surfaceTint = Color(0xFF172420),
    inverseSurface = Color(0xFFE7F2EC),
    inverseOnSurface = Color(0xFF10241C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFFC9E0D6),
    outlineVariant = Color(0xFFA8C4B8),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF2C4038),
    surfaceDim = Color(0xFF0C1412),
    surfaceContainer = Color(0xFF1A2622),
    surfaceContainerHigh = Color(0xFF20302A),
    surfaceContainerHighest = Color(0xFF283C34),
    surfaceContainerLow = Color(0xFF141E1A),
    surfaceContainerLowest = Color(0xFF0C1412),
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
    val scheme = if (dark) Dark else Light
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }
    MaterialTheme(colorScheme = scheme, typography = Type) {
        CompositionLocalProvider(LocalContentColor provides scheme.onSurface, content = content)
    }
}

@Composable
fun presenceButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContentColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
fun presenceTonalColors() = ButtonDefaults.filledTonalButtonColors(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContentColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
fun presenceChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surface,
    labelColor = MaterialTheme.colorScheme.onSurface,
    iconColor = MaterialTheme.colorScheme.onSurface,
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
    disabledSelectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun presenceChipBorder(enabled: Boolean, selected: Boolean): BorderStroke =
    FilterChipDefaults.filterChipBorder(
        enabled = enabled,
        selected = selected,
        borderColor = MaterialTheme.colorScheme.outline,
        selectedBorderColor = MaterialTheme.colorScheme.outline,
        disabledBorderColor = MaterialTheme.colorScheme.outline,
        disabledSelectedBorderColor = MaterialTheme.colorScheme.outline,
        borderWidth = 1.dp,
        selectedBorderWidth = 1.dp,
    )

@Composable
fun presenceSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedBorderColor = MaterialTheme.colorScheme.outline,
    checkedIconColor = MaterialTheme.colorScheme.primary,
    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    uncheckedBorderColor = MaterialTheme.colorScheme.outline,
    uncheckedIconColor = MaterialTheme.colorScheme.onSurface,
    disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurface,
    disabledCheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
    disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledUncheckedBorderColor = MaterialTheme.colorScheme.outline,
)

@Composable
fun presenceRadioColors() = RadioButtonDefaults.colors(
    selectedColor = MaterialTheme.colorScheme.primary,
    unselectedColor = MaterialTheme.colorScheme.outline,
)

@Composable
fun presenceFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = MaterialTheme.colorScheme.outline,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    disabledBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
)

@Composable
fun presenceNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedTextColor = MaterialTheme.colorScheme.onSurface,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledIconColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
fun presenceRailColors() = NavigationRailItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedTextColor = MaterialTheme.colorScheme.onSurface,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledIconColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
)

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
