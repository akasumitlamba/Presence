package app.presence.ui

import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import kotlinx.coroutines.launch
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.presence.core.Rec
import app.presence.core.recFromJson
import app.presence.core.recToJson
import java.time.LocalDate
import java.time.YearMonth
import org.json.JSONObject

internal data class UiLayout(val width: Dp = 360.dp, val height: Dp = 800.dp, val keyboard: Boolean = false) {
    val short get() = height < 480.dp
    val rail get() = width >= 600.dp || width > height
    val twoPanes get() = width >= 840.dp && !short
    val margin get() = if (twoPanes) 24.dp else 16.dp
}
internal val LocalUiText = staticCompositionLocalOf { T("en") }
internal val LocalUiLayout = staticCompositionLocalOf { UiLayout() }
internal val DateSaver = Saver<LocalDate, Long>({ it.toEpochDay() }, { LocalDate.ofEpochDay(it) })
internal val MonthSaver = Saver<YearMonth, String>({ it.toString() }, { YearMonth.parse(it) })

@Composable
internal fun rememberRecordState(): MutableState<Rec?> = rememberSaveable(saver = Saver(
    save = { state: MutableState<Rec?> -> state.value?.let { recToJson(it).toString() }.orEmpty() },
    restore = { text: String -> mutableStateOf(if (text.isEmpty()) null else recFromJson(JSONObject(text))) },
)) { mutableStateOf(null) }

@Composable
internal fun PageHeader(title: String, backLabel: String, onBack: (() -> Unit)? = null) {
    val compact = LocalUiLayout.current.short
    Row(
        Modifier.fillMaxWidth().heightIn(min = if (compact) 48.dp else 72.dp)
            .padding(horizontal = LocalUiLayout.current.margin, vertical = if (compact) 0.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, backLabel) }
        Text(title, style = MaterialTheme.typography.headlineMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun SectionCard(title: String? = null, modifier: Modifier = Modifier, spacing: Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(spacing)) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            content()
        }
    }
}

@Composable
internal fun Metric(label: String, value: String, modifier: Modifier = Modifier, centered: Boolean = false) {
    val alignment = if (centered) androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Start
    Column(modifier, horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = alignment)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, textAlign = alignment)
    }
}

@Composable
internal fun ActionRow(title: String, detail: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (detail.isNotEmpty()) Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun PresenceDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
) {
    val available = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
    AlertDialog(
        onDismissRequest = onDismissRequest, title = title,
        text = {
            Column(Modifier.heightIn(max = (available * 0.58f).coerceAtLeast(80.dp)).presenceScroll(androidx.compose.foundation.rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) { text() }
        },
        confirmButton = confirmButton, dismissButton = dismissButton,
        modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 16.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        shape = MaterialTheme.shapes.extraLarge, containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    )
}

/** Android Compose does not draw a scrollbar by default; keep a small, non-interactive cue. */
@Composable
internal fun Modifier.presenceScroll(state: androidx.compose.foundation.ScrollState): Modifier {
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    return this.drawWithContent {
        drawContent()
        if (state.maxValue > 0 && state.maxValue < Int.MAX_VALUE && size.height > 0) {
            val thumb = (size.height * size.height / (size.height + state.maxValue)).coerceIn(minOf(24.dp.toPx(), size.height), size.height)
            val y = state.value.toFloat() / state.maxValue * (size.height - thumb)
            drawRoundRect(color, androidx.compose.ui.geometry.Offset(size.width - 3.dp.toPx(), y),
                androidx.compose.ui.geometry.Size(2.dp.toPx(), thumb), androidx.compose.ui.geometry.CornerRadius(1.dp.toPx()))
        }
    }.verticalScroll(state)
}

@Composable
internal fun PresenceList(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    val state = androidx.compose.foundation.lazy.rememberLazyListState()
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    androidx.compose.foundation.lazy.LazyColumn(state = state, modifier = modifier.drawWithContent {
        drawContent()
        val info = state.layoutInfo
        if ((state.canScrollBackward || state.canScrollForward) && info.totalItemsCount > 0 && size.height > 0) {
            val visible = info.visibleItemsInfo.sumOf { item ->
                val overlap = (minOf(item.offset + item.size, info.viewportEndOffset) - maxOf(item.offset, info.viewportStartOffset)).coerceAtLeast(0)
                overlap.toDouble() / item.size.coerceAtLeast(1)
            }.toFloat().coerceAtLeast(0.1f)
            val thumb = (size.height * visible / info.totalItemsCount).coerceIn(minOf(24.dp.toPx(),size.height),size.height)
            val first = info.visibleItemsInfo.firstOrNull()
            val progress = if (first == null) 0f else (first.index + (-first.offset).coerceAtLeast(0).toFloat() / first.size.coerceAtLeast(1)) /
                (info.totalItemsCount - visible).coerceAtLeast(1f)
            drawRoundRect(color, androidx.compose.ui.geometry.Offset(size.width - 3.dp.toPx(), progress.coerceIn(0f,1f) * (size.height-thumb)),
                androidx.compose.ui.geometry.Size(2.dp.toPx(),thumb), androidx.compose.ui.geometry.CornerRadius(1.dp.toPx()))
        }
    }, verticalArrangement = verticalArrangement, contentPadding = contentPadding, content = content)
}

/** Equal cells, centered incomplete rows, and no label-dependent sizing. */
@Composable
internal fun BalancedChoices(options: List<Pair<String?, String>>, selected: String?, maxColumns: Int = 2, onSelect: (String?) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val minimum = if (maxColumns == 4) 72.dp else 140.dp
        val capacity = ((maxWidth + 8.dp) / (minimum + 8.dp)).toInt().coerceAtLeast(1)
        val columns = minOf(maxColumns, options.size.coerceAtLeast(1), if (maxColumns == 4 && capacity == 3) 2 else capacity)
        val cellWidth = (maxWidth - 8.dp * (columns - 1)) / columns
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                    row.forEach { (id, title) ->
                        Surface(onClick = { onSelect(id) }, modifier = Modifier.width(cellWidth).fillMaxHeight(), shape = RoundedCornerShape(20.dp),
                            color = motionColor(if (selected == id) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow),
                            border = BorderStroke(1.dp, if (selected == id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                            Box(Modifier.heightIn(min = 56.dp).padding(12.dp), contentAlignment = Alignment.Center) {
                                Text(title, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Touch/trackpad scrolling plus explicit controls for pointer and keyboard users. */
@Composable
internal fun TagStrip(options: List<Pair<String?, String>>, selected: String?, previous: String, next: String, onSelect: (String?) -> Unit) {
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton({ scope.launch { scroll.animateScrollTo((scroll.value - scroll.viewportSize).coerceAtLeast(0)) } }, enabled = scroll.canScrollBackward) {
            Icon(Icons.Rounded.ChevronLeft, previous)
        }
        Row(Modifier.weight(1f).horizontalScroll(scroll), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            options.forEach { (id, title) -> PresenceChip(selected == id, { onSelect(id) }, { Text(title, maxLines = 1) }) }
        }
        IconButton({ scope.launch { scroll.animateScrollTo((scroll.value + scroll.viewportSize).coerceAtMost(scroll.maxValue)) } }, enabled = scroll.canScrollForward) {
            Icon(Icons.Rounded.ChevronRight, next)
        }
    }
}

/** A small, bounded response to a press, alongside Material's built-in ripple. */
@Composable
internal fun Modifier.materialPress(source: androidx.compose.foundation.interaction.MutableInteractionSource): Modifier {
    val pressed by source.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        if (pressed) 0.97f else 1f,
        MaterialTheme.motionScheme.fastSpatialSpec(), label = "Material press",
    )
    return this.then(Modifier.graphicsLayerMaterial(scale))
}

private fun Modifier.graphicsLayerMaterial(scale: Float): Modifier =
    this.then(Modifier.graphicsLayer(scaleX = scale, scaleY = scale))
