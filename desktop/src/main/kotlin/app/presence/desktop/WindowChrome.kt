package app.presence.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.FilterNone
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState

/** Window controls live in the page header area, without separate window chrome. */
@Composable
internal fun PresenceWindowControls(state: WindowState, close: () -> Unit, compact: Boolean = false) {
    val t = LocalUiText.current
    val maximized = state.placement == WindowPlacement.Maximized
    val size = if (compact) 36.dp else 44.dp
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton({ state.isMinimized = true }, Modifier.size(size)) { Icon(Icons.Outlined.Remove, t.minimizeWindow, Modifier.size(18.dp)) }
        IconButton({ state.placement = if (maximized) WindowPlacement.Floating else WindowPlacement.Maximized }, Modifier.size(size)) {
            Icon(if (maximized) Icons.Outlined.FilterNone else Icons.Outlined.CropSquare, if (maximized) t.restoreWindow else t.maximizeWindow, Modifier.size(18.dp))
        }
        IconButton(close, Modifier.size(size), colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Outlined.Close, t.closeWindow, Modifier.size(20.dp)) }
    }
}

internal val LocalHeaderDrag = androidx.compose.runtime.staticCompositionLocalOf<@Composable (Modifier, @Composable () -> Unit) -> Unit> { { modifier, content -> Box(modifier) { content() } } }

@Composable internal fun PresenceBrand(modifier: Modifier, colorFilter: androidx.compose.ui.graphics.ColorFilter? = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary)) {
    val mark = androidx.compose.runtime.remember { androidx.compose.ui.res.loadImageBitmap(object {}.javaClass.getResourceAsStream("/presence-mark.png")!!) }
    Image(mark, "Presence", modifier, colorFilter = colorFilter, filterQuality = androidx.compose.ui.graphics.FilterQuality.High)
}
