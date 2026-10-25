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

/** App-owned window controls, with the same palette as the screen below. */
@Composable
internal fun WindowScope.PresenceTitleBar(state: WindowState, close: () -> Unit, showIcon: Boolean = true) {
    val t = LocalUiText.current
    val maximized = state.placement == WindowPlacement.Maximized
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            WindowDraggableArea(Modifier.weight(1f).fillMaxHeight()) {
                Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (showIcon) Image(painterResource("presence.png"), null, Modifier.size(24.dp))
                    Text("Presence", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                }
            }
            IconButton({ state.isMinimized = true }, Modifier.size(48.dp)) { Icon(Icons.Outlined.Remove, t.minimizeWindow, Modifier.size(18.dp)) }
            IconButton({ state.placement = if (maximized) WindowPlacement.Floating else WindowPlacement.Maximized }, Modifier.size(48.dp)) {
                Icon(if (maximized) Icons.Outlined.FilterNone else Icons.Outlined.CropSquare, if (maximized) t.restoreWindow else t.maximizeWindow, Modifier.size(18.dp))
            }
            IconButton(close, Modifier.size(48.dp), colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Outlined.Close, t.closeWindow, Modifier.size(20.dp)) }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
