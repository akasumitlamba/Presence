package app.presence.ui

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme

/** One resolver keeps the app and RemoteViews on the same wallpaper palette. */
internal fun platformColorScheme(context: Context, dark: Boolean, palette: String): ColorScheme =
    if (palette == "system" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else presenceColorScheme(dark, palette)
