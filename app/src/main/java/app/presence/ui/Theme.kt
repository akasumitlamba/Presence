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

private fun ink(hex: Long) = Color(0xFF000000 or hex)

private data class Palette(
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val inversePrimary: Long,
    val secondary: Long,
    val onSecondary: Long,
    val secondaryContainer: Long,
    val onSecondaryContainer: Long,
    val tertiary: Long,
    val onTertiary: Long,
    val tertiaryContainer: Long,
    val onTertiaryContainer: Long,
    val background: Long,
    val onBackground: Long,
    val surface: Long,
    val onSurface: Long,
    val surfaceVariant: Long,
    val onSurfaceVariant: Long,
    val inverseSurface: Long,
    val inverseOnSurface: Long,
    val outline: Long,
    val outlineVariant: Long,
    val surfaceBright: Long,
    val surfaceDim: Long,
    val surfaceContainer: Long,
    val surfaceContainerHigh: Long,
    val surfaceContainerHighest: Long,
    val surfaceContainerLow: Long,
    val surfaceContainerLowest: Long,
)

private fun scheme(dark: Boolean, palette: Palette) = if (dark) {
    darkColorScheme(
        primary = ink(palette.primary),
        onPrimary = ink(palette.onPrimary),
        primaryContainer = ink(palette.primaryContainer),
        onPrimaryContainer = ink(palette.onPrimaryContainer),
        inversePrimary = ink(palette.inversePrimary),
        secondary = ink(palette.secondary),
        onSecondary = ink(palette.onSecondary),
        secondaryContainer = ink(palette.secondaryContainer),
        onSecondaryContainer = ink(palette.onSecondaryContainer),
        tertiary = ink(palette.tertiary),
        onTertiary = ink(palette.onTertiary),
        tertiaryContainer = ink(palette.tertiaryContainer),
        onTertiaryContainer = ink(palette.onTertiaryContainer),
        background = ink(palette.background),
        onBackground = ink(palette.onBackground),
        surface = ink(palette.surface),
        onSurface = ink(palette.onSurface),
        surfaceVariant = ink(palette.surfaceVariant),
        onSurfaceVariant = ink(palette.onSurfaceVariant),
        surfaceTint = ink(palette.surface),
        inverseSurface = ink(palette.inverseSurface),
        inverseOnSurface = ink(palette.inverseOnSurface),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = ink(palette.outline),
        outlineVariant = ink(palette.outlineVariant),
        scrim = Color(0xFF000000),
        surfaceBright = ink(palette.surfaceBright),
        surfaceDim = ink(palette.surfaceDim),
        surfaceContainer = ink(palette.surfaceContainer),
        surfaceContainerHigh = ink(palette.surfaceContainerHigh),
        surfaceContainerHighest = ink(palette.surfaceContainerHighest),
        surfaceContainerLow = ink(palette.surfaceContainerLow),
        surfaceContainerLowest = ink(palette.surfaceContainerLowest),
    )
} else {
    lightColorScheme(
        primary = ink(palette.primary),
        onPrimary = ink(palette.onPrimary),
        primaryContainer = ink(palette.primaryContainer),
        onPrimaryContainer = ink(palette.onPrimaryContainer),
        inversePrimary = ink(palette.inversePrimary),
        secondary = ink(palette.secondary),
        onSecondary = ink(palette.onSecondary),
        secondaryContainer = ink(palette.secondaryContainer),
        onSecondaryContainer = ink(palette.onSecondaryContainer),
        tertiary = ink(palette.tertiary),
        onTertiary = ink(palette.onTertiary),
        tertiaryContainer = ink(palette.tertiaryContainer),
        onTertiaryContainer = ink(palette.onTertiaryContainer),
        background = ink(palette.background),
        onBackground = ink(palette.onBackground),
        surface = ink(palette.surface),
        onSurface = ink(palette.onSurface),
        surfaceVariant = ink(palette.surfaceVariant),
        onSurfaceVariant = ink(palette.onSurfaceVariant),
        surfaceTint = ink(palette.surface),
        inverseSurface = ink(palette.inverseSurface),
        inverseOnSurface = ink(palette.inverseOnSurface),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color(0xFF410E0B),
        outline = ink(palette.outline),
        outlineVariant = ink(palette.outlineVariant),
        scrim = Color(0xFF000000),
        surfaceBright = ink(palette.surfaceBright),
        surfaceDim = ink(palette.surfaceDim),
        surfaceContainer = ink(palette.surfaceContainer),
        surfaceContainerHigh = ink(palette.surfaceContainerHigh),
        surfaceContainerHighest = ink(palette.surfaceContainerHighest),
        surfaceContainerLow = ink(palette.surfaceContainerLow),
        surfaceContainerLowest = ink(palette.surfaceContainerLowest),
    )
}

private val GroveLight = Palette(
    0x0E5C42, 0xF4FBF7, 0xC9EBDD, 0x062016, 0x8FE0C4,
    0x1A4A3A, 0xF4FBF7, 0xD5EDE3, 0x0C241C,
    0x1A4554, 0xF4FBFD, 0xD3EAF1, 0x062028,
    0xF3F6F4, 0x10241C, 0xFFFFFF, 0x10241C, 0xE4EEE9, 0x24382E,
    0x172420, 0xF4F8F6, 0x2A463C, 0x3E5C50,
    0xFFFFFF, 0xE7EFEC, 0xEEF4F1, 0xE4EEE9, 0xD9E6E0, 0xF7FAF8, 0xFFFFFF,
)

private val GroveDark = Palette(
    0x145C48, 0xF4FBF7, 0x1B5C46, 0xE9FFF6, 0x0E5C42,
    0x1E4A3C, 0xE7F7F0, 0x1E4A3C, 0xE7F7F0,
    0x1A4554, 0xE7F6FB, 0x1A4554, 0xE7F6FB,
    0x101816, 0xF4F8F6, 0x172420, 0xF4F8F6, 0x24342E, 0xD5E6DE,
    0xE7F2EC, 0x10241C, 0xC9E0D6, 0xA8C4B8,
    0x2C4038, 0x0C1412, 0x1A2622, 0x20302A, 0x283C34, 0x141E1A, 0x0C1412,
)

private val HarborLight = Palette(
    0x1E518E, 0xF4F8FC, 0xD4E6F6, 0x061828, 0x9CC4EE,
    0x163E6E, 0xF4F8FC, 0xD5E4F2, 0x0C1824,
    0x1A4554, 0xF4FBFD, 0xD3EAF1, 0x062028,
    0xF3F5F8, 0x102030, 0xFFFFFF, 0x102030, 0xE4EAF0, 0x243038,
    0x172026, 0xF4F7FA, 0x2A3C4E, 0x3E5264,
    0xFFFFFF, 0xE7EEF3, 0xEEF2F6, 0xE4EAF0, 0xD9E2EA, 0xF7F9FB, 0xFFFFFF,
)

private val HarborDark = Palette(
    0x1A4E86, 0xF4F8FC, 0x1A4A7A, 0xE8F3FF, 0x1E518E,
    0x163E6E, 0xE7F1FA, 0x163E6E, 0xE7F1FA,
    0x1A4554, 0xE7F6FB, 0x1A4554, 0xE7F6FB,
    0x101418, 0xF4F7FA, 0x171E26, 0xF4F7FA, 0x24303A, 0xD5DEE6,
    0xE7F0F6, 0x102030, 0xC9D6E4, 0xA8B8C8,
    0x2C3846, 0x0C1014, 0x1A222C, 0x202A36, 0x283442, 0x141A22, 0x0C1014,
)

private val DuskLight = Palette(
    0x683996, 0xF7F4FC, 0xE6D8F4, 0x1A1024, 0xD2B4EE,
    0x4E2A72, 0xF7F4FC, 0xE8DCF2, 0x140C1C,
    0x1A4554, 0xF4FBFD, 0xD3EAF1, 0x062028,
    0xF5F3F8, 0x1A1020, 0xFFFFFF, 0x1A1020, 0xEAE4F0, 0x2C2438,
    0x1E1726, 0xF6F4FA, 0x3C2A4E, 0x523E64,
    0xFFFFFF, 0xEFE7F3, 0xF2EEF6, 0xEAE4F0, 0xE4D9EA, 0xF9F7FB, 0xFFFFFF,
)

private val DuskDark = Palette(
    0x5C3484, 0xF7F4FC, 0x542E78, 0xF3E8FF, 0x683996,
    0x4E2A72, 0xF3E8FF, 0x4E2A72, 0xF3E8FF,
    0x1A4554, 0xE7F6FB, 0x1A4554, 0xE7F6FB,
    0x141018, 0xF6F4FA, 0x1E1726, 0xF6F4FA, 0x32243A, 0xE0D5E8,
    0xF0E7F6, 0x1A1020, 0xD6C9E4, 0xB8A8C8,
    0x3C2E48, 0x100C14, 0x241C2C, 0x2C2236, 0x342848, 0x1A1422, 0x100C14,
)

private val SlateLight = Palette(
    0x455260, 0xF4F6F8, 0xE2E6EA, 0x161A1E, 0xC5CED6,
    0x343C46, 0xF4F6F8, 0xE4E8EC, 0x12161A,
    0x1A4554, 0xF4FBFD, 0xD3EAF1, 0x062028,
    0xF3F4F6, 0x14181C, 0xFFFFFF, 0x14181C, 0xE6E8EC, 0x2A3036,
    0x1A1E22, 0xF4F6F8, 0x3A424A, 0x4E565E,
    0xFFFFFF, 0xE8EAEE, 0xF0F2F4, 0xE6E8EC, 0xE0E4E8, 0xF7F8FA, 0xFFFFFF,
)

private val SlateDark = Palette(
    0x3E4C5C, 0xF4F6F8, 0x3A4654, 0xE8EEF4, 0x455260,
    0x343C46, 0xE8EEF4, 0x343C46, 0xE8EEF4,
    0x1A4554, 0xE7F6FB, 0x1A4554, 0xE7F6FB,
    0x121416, 0xF4F6F8, 0x1A1E22, 0xF4F6F8, 0x2A3036, 0xD8DEE4,
    0xE8EAEE, 0x14181C, 0xD0D6DC, 0xB0B6BC,
    0x32383E, 0x0E1012, 0x1E2226, 0x262A30, 0x2C3238, 0x16181C, 0x0E1012,
)

private val BerryLight = Palette(
    0x912C3D, 0xFCF4F5, 0xF6D8DC, 0x2A1014, 0xF0B4BC,
    0x6E2230, 0xFCF4F5, 0xF4DCE0, 0x241014,
    0x1A4554, 0xF4FBFD, 0xD3EAF1, 0x062028,
    0xF8F3F4, 0x241014, 0xFFFFFF, 0x241014, 0xF0E4E6, 0x3A2830,
    0x26171C, 0xFAF4F6, 0x4E2A34, 0x623E48,
    0xFFFFFF, 0xF3E7EA, 0xF6EEEF, 0xF0E4E6, 0xEAD9DC, 0xFBF7F8, 0xFFFFFF,
)

private val BerryDark = Palette(
    0x8A3040, 0xFCF4F5, 0x7A2C3A, 0xFFE8EC, 0x912C3D,
    0x6E2230, 0xFFE8EC, 0x6E2230, 0xFFE8EC,
    0x1A4554, 0xE7F6FB, 0x1A4554, 0xE7F6FB,
    0x181012, 0xFAF4F6, 0x26171C, 0xFAF4F6, 0x3A2830, 0xE8D5DA,
    0xF6E7EA, 0x241014, 0xE4C9D0, 0xC8A8B0,
    0x463038, 0x120C0E, 0x22181C, 0x2C1E24, 0x422830, 0x1C1418, 0x120C0E,
)

private fun paletteOf(name: String, dark: Boolean): Palette = when (name) {
    "harbor" -> if (dark) HarborDark else HarborLight
    "dusk" -> if (dark) DuskDark else DuskLight
    "slate" -> if (dark) SlateDark else SlateLight
    "berry" -> if (dark) BerryDark else BerryLight
    else -> if (dark) GroveDark else GroveLight
}

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
fun PresenceTheme(dark: Boolean = isSystemInDarkTheme(), palette: String = "grove", content: @Composable () -> Unit) {
    val scheme = scheme(dark, paletteOf(palette, dark))
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
