package app.presence.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

private data class MenuEntry(val token: Any, val anchor: Rect, val dismiss: State<() -> Unit>, val content: State<@Composable ColumnScope.() -> Unit>)
private object MenuLayer { var current by mutableStateOf<MenuEntry?>(null) }

/** Render in the app's coordinate space, rather than a separate OS popup window. */
@Composable
internal fun DesktopDropdownMenu(expanded: Boolean, onDismissRequest: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val token = remember { Any() }
    var anchor by remember { mutableStateOf<Rect?>(null) }
    val dismiss = rememberUpdatedState(onDismissRequest)
    val body = rememberUpdatedState(content)
    Box(Modifier.size(1.dp).onPlaced { anchor = it.parentLayoutCoordinates?.boundsInRoot() })
    val placedAnchor = anchor
    SideEffect {
        if (expanded && placedAnchor != null) {
            if (MenuLayer.current?.token !== token || MenuLayer.current?.anchor != placedAnchor) MenuLayer.current = MenuEntry(token, placedAnchor, dismiss, body)
        } else if (MenuLayer.current?.token === token) MenuLayer.current = null
    }
    DisposableEffect(token) { onDispose { if (MenuLayer.current?.token === token) MenuLayer.current = null } }
}

@Composable
internal fun DesktopMenuHost() {
    val menu = MenuLayer.current ?: return
    val density = LocalDensity.current
    val focus = remember { FocusRequester() }
    var menuHeight by remember(menu.token) { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val gap = with(density) { 4.dp.toPx() }
        val edge = with(density) { 8.dp.toPx() }
        val minimum = with(density) { 180.dp.toPx() }
        val menuWidth = menu.anchor.width.coerceAtLeast(minimum).coerceAtMost((widthPx - edge * 2).coerceAtLeast(1f))
        val x = (if (menu.anchor.width < minimum) menu.anchor.right - menuWidth else menu.anchor.left).coerceIn(edge, (widthPx - menuWidth - edge).coerceAtLeast(edge))
        val below = heightPx - menu.anchor.bottom - gap - edge
        val above = menu.anchor.top - gap - edge
        val maxHeightPx = maxOf(below, above).coerceAtLeast(48f)
        val y = (if (menuHeight <= below || below >= above) menu.anchor.bottom + gap else menu.anchor.top - gap - menuHeight).coerceIn(edge, (heightPx - menuHeight - edge).coerceAtLeast(edge))
        Box(Modifier.fillMaxSize().background(Color.Transparent).clickable { menu.dismiss.value() })
        Surface(Modifier.offset { IntOffset(x.toInt(), y.toInt()) }.width(with(density) { menuWidth.toDp() })
            .heightIn(max = with(density) { maxHeightPx.toDp() }).onSizeChanged { menuHeight = it.height }
            .focusRequester(focus).onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.key == Key.Escape) { menu.dismiss.value(); true } else false
            }.focusable(), shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shadowElevation = 8.dp) {
            ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(vertical = 4.dp), content = menu.content.value)
            }
        }
        LaunchedEffect(menu.token) { focus.requestFocus() }
    }
}

internal object DesktopDialogs { var content by mutableStateOf<(@Composable () -> Unit)?>(null) }
