@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package app.presence.ui

import androidx.compose.ui.semantics.liveRegion
import app.presence.core.streakDays
import app.presence.core.loggedToday
import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import app.presence.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.presence.MainActivity
import app.presence.PresenceApp
import app.presence.data.AppStorage
import app.presence.timer.Reminders
import app.presence.widget.HomeWidgets
import app.presence.core.FREE_PROJECTS
import app.presence.core.FREE_TASKS
import app.presence.core.KIND_GROUP
import app.presence.core.KIND_LOCAL
import app.presence.core.KIND_PROJECT
import app.presence.core.KIND_SESSION
import app.presence.core.KIND_TAG
import app.presence.core.KIND_TASK
import app.presence.core.Rec
import app.presence.core.Snap
import app.presence.core.alive
import app.presence.core.clockDigits
import app.presence.core.clockWords
import app.presence.core.dailyTargetMinutes
import app.presence.core.dayKey
import app.presence.core.dayTotals
import app.presence.core.decodeBackup
import app.presence.core.elapsedWork
import app.presence.core.displayMillis
import app.presence.core.encodeBackup
import app.presence.core.f
import app.presence.core.freeProjectIds
import app.presence.core.manualSession
import app.presence.core.overlapsClosed
import app.presence.core.overlapsOpen
import app.presence.core.periodBounds
import app.presence.core.shiftPeriod
import app.presence.core.pauseNoteMillis
import app.presence.core.sessionOpen
import app.presence.core.stretchCount
import app.presence.core.stretchLengths
import app.presence.core.sessionsCsv
import app.presence.core.SYNC_KINDS
import app.presence.core.sessionCountedMillis
import app.presence.core.shareSummary
import app.presence.core.slicesFor
import app.presence.core.snapFrom
import app.presence.core.wholePercents
import app.presence.core.workedMillisFor
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PresenceRoot() {
    val context = LocalContext.current
    val app = context.applicationContext as PresenceApp
    var ready by remember { mutableStateOf(app.ready.isCompleted) }
    val startupFailed by app.startupFailed.collectAsState()
    LaunchedEffect(Unit) { app.ready.await(); ready = true }
    if (!ready) {
        PresenceTheme {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                if (startupFailed) Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val t = T(resolveLang("system"))
                    Text(t.actionFailed, color = MaterialTheme.colorScheme.error)
                    Button({ app.retryStartup() }) { Text(t.retry) }
                } else StartupContent()
            }
        }
        return
    }
    val initialRows = remember(app.repo) { app.repo.snapshot() }
    val rows by app.repo.observe().collectAsState(initial = initialRows)
    val flags = rows.firstOrNull { it.kind == KIND_LOCAL && it.id == "flags" }?.body.orEmpty()
    val configuration = LocalConfiguration.current
    val t = remember(flags["language"], configuration.locales.toLanguageTags()) { T(resolveLang(flags["language"] ?: "system")) }
    val dark = when (flags["theme"]) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val scale = when (flags["text"]) { "compact" -> 0.9f; "large" -> 1.16f; else -> 1f }
    LaunchedEffect(flags) { withContext(Dispatchers.IO) { HomeWidgets.update(context) } }
    PresenceTheme(dark, flags["palette"] ?: "grove", scale) {
        // Background owns edge-to-edge drawing. Only this root owns system safe areas.
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            val systemInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
            BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(systemInsets).consumeWindowInsets(systemInsets)) {
                val keyboard = WindowInsets.ime.getBottom(LocalDensity.current) > 0
                CompositionLocalProvider(LocalUiLayout provides UiLayout(maxWidth, maxHeight, keyboard), LocalUiText provides t) {
                    Shell(app, rows, t, true, LocalUiLayout.current.twoPanes)
                }
            }
        }
    }
}

@Composable
private fun Shell(app: PresenceApp, rows: List<Rec>, t: T, premium: Boolean, wide: Boolean) {
    val context = LocalContext.current
    val layout = LocalUiLayout.current
    val storage by app.repo.storage().collectAsState()
    var fullScreen by rememberSaveable { mutableStateOf(false) }
    TimerImmersive(fullScreen)
    var tab by rememberSaveable { mutableStateOf("timer") }
    var projectId by rememberSaveable { mutableStateOf<String?>(null) }
    var pairing by rememberSaveable { mutableStateOf(false) }
    val stateHolder = rememberSaveableStateHolder()
    val introPrefs = remember { context.getSharedPreferences("introduction", android.content.Context.MODE_PRIVATE) }
    var guide by rememberSaveable { mutableStateOf(!introPrefs.getBoolean("completed", false)) }
    if (guide) Introduction(t) { introPrefs.edit().putBoolean("completed", true).apply(); guide = false }
    val activity = context as? MainActivity
    val requested = activity?.requestedTab
    LaunchedEffect(requested) {
        if (!requested.isNullOrEmpty()) { fullScreen = false; tab = requested; pairing = false; activity?.requestedTab = null }
    }
    var tick by rememberSaveable { mutableIntStateOf(0) }
    val snap = rows.firstOrNull { it.kind == KIND_LOCAL && it.id == "snapshot" && !it.deleted }?.let { snapFrom(it.body) } ?: Snap()
    LaunchedEffect(snap.status, snap.completionToken) {
        while (sessionOpen(snap)) { delay(200); tick++ }
    }
    val shown = displayMillis(snap, System.currentTimeMillis(), SystemClock.elapsedRealtime())
    if (tick < 0) return
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(guide) {
        if (!guide && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    val back: (() -> Unit)? = when {
        fullScreen -> { { fullScreen = false } }
        pairing -> { { pairing = false } }
        tab == "projects" && projectId != null && !wide -> { { projectId = null } }
        else -> null
    }
    BackHandler(enabled = back != null) { back?.invoke() }
    val title = when {
        pairing -> t.pair
        tab == "projects" && projectId != null && !wide -> rows.alive(KIND_PROJECT).find { it.id == projectId }?.f("name") ?: t.projects
        tab == "projects" -> t.projects
        tab == "todos" -> t.todos
        tab == "stats" -> t.stats
        tab == "settings" -> t.settings
        else -> t.timer
    }
    val page: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier.imePadding()) {
            if (!fullScreen && !(layout.short && layout.keyboard)) Box(Modifier.widthIn(max = 1200.dp).fillMaxWidth().align(Alignment.CenterHorizontally)) { PageHeader(title, t.back, back) }
            if (storage.cardMissing) Text(t.sdMissing, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().clickable { tab = "settings"; pairing = false }.padding(horizontal = layout.margin, vertical = 8.dp))
            if (sessionOpen(snap) && tab != "timer" && !(layout.short && layout.keyboard)) {
                Box(Modifier.widthIn(max = 1200.dp).fillMaxWidth().padding(horizontal = layout.margin, vertical = 4.dp).align(Alignment.CenterHorizontally)) {
                    Mini(t, snap, shown) { tab = "timer"; pairing = false }
                }
            }
            Box(Modifier.weight(1f).widthIn(max = 1200.dp).fillMaxWidth().align(Alignment.CenterHorizontally)) {
                stateHolder.SaveableStateProvider(if (pairing) "pairing" else tab) {
                    when {
                        pairing -> PairPage(app, t, rows)
                        tab == "timer" -> TimerPage(app, t, rows, premium, wide, snap, shown, fullScreen, { fullScreen = !fullScreen }) { }
                        tab == "projects" -> ProjectsPage(app, t, rows, premium, wide, projectId, { projectId = it }) { }
                        tab == "todos" -> TodosPage(app, t, rows, wide)
                        tab == "stats" -> StatsPage(app, t, rows, premium, wide, snap) { }
                        else -> SettingsPage(app, t, rows, premium, wide, { pairing = true }) { }
                    }
                }
            }
        }
    }
    // Keep the content/provider at one composition position when navigation changes.
    // Otherwise auto-generated saveable keys differ between the rail and bottom-bar branches.
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (!fullScreen && !layout.rail) Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow), contentAlignment = Alignment.Center) {
                NavigationBar(modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(), containerColor = MaterialTheme.colorScheme.surfaceContainerLow, windowInsets = WindowInsets(0)) {
                    destinations(t).forEach { item ->
                        NavigationBarItem(modifier = Modifier.semantics { contentDescription = item.title }, selected = tab == item.id, onClick = { tab = item.id; pairing = false }, icon = { Icon(item.icon, null) },
                            label = { Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall) }, colors = presenceNavColors())
                    }
                }
            }
        },
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            if (!fullScreen && layout.rail) Rail(t, tab) { tab = it; pairing = false }
            page(Modifier.weight(1f).fillMaxHeight())
        }
    }
}

private data class Destination(val id: String, val title: String, val icon: ImageVector)
private fun destinations(t: T) = listOf(
    Destination("timer", t.timer, Icons.Outlined.Timer), Destination("projects", t.projects, Icons.Outlined.Folder),
    Destination("todos", t.todos, Icons.Outlined.CheckBox), Destination("stats", t.stats, Icons.Outlined.BarChart),
    Destination("settings", t.settings, Icons.Outlined.Settings),
)

@Composable
private fun Rail(t: T, tab: String, onTab: (String) -> Unit) {
    val short = LocalUiLayout.current.short
    Column(Modifier.width(if (short) 72.dp else 80.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceContainerLow), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(R.drawable.ic_launcher_fg), "Presence", Modifier.padding(vertical = 12.dp).size(if (short) 32.dp else 40.dp))
    BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().presenceScroll(rememberScrollState()).heightIn(min = maxHeight).padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (short) 0.dp else 8.dp, Alignment.CenterVertically)) {
            destinations(t).forEach { item ->
                val selected = tab == item.id
                Surface(onClick = { onTab(item.id) }, shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth().heightIn(min = 48.dp)) {
                    Column(Modifier.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(item.icon, null, Modifier.size(22.dp))
                        Text(item.title, style = MaterialTheme.typography.labelSmall, maxLines = 2, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun Mini(t: T, snap: Snap, shown: Long, onOpen: () -> Unit) {
    val app = LocalContext.current.applicationContext as PresenceApp
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f).clickable(onClick = onOpen).heightIn(min = 48.dp), verticalArrangement = Arrangement.Center) {
                Text(phase(t, snap.kind), style = MaterialTheme.typography.labelLarge)
                Text(snap.projectName.ifBlank { t.unlabeled }, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(clockDigits(shown), style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"))
            IconButton(onClick = { when(snap.status) { "running" -> app.center.pause(); "paused" -> app.center.resume(); else -> app.center.start("focus", null, null) } }) {
                Icon(if (snap.status == "running") Icons.Outlined.PauseCircle else Icons.Outlined.PlayCircle, if (snap.status == "running") t.pause else t.play)
            }
            IconButton(onClick = onOpen) { Icon(Icons.Outlined.OpenInFull, t.openTimer) }
        }
    }
}

private fun phase(t: T, kind: String) = when (kind) {
    "short" -> t.shortRest
    "long" -> t.longRest
    "track" -> t.track
    else -> t.focus
}

@Composable
private fun TimerPage(app: PresenceApp, t: T, rows: List<Rec>, premium: Boolean, wide: Boolean, snap: Snap, shown: Long, immersive: Boolean, onFullscreen: () -> Unit, onGate: () -> Unit) {
    val context = LocalContext.current
    val projects = rows.alive(KIND_PROJECT).sortedBy { it.f("sort").toIntOrNull() ?: 0 }
    var selected by rememberSaveable { mutableStateOf(snap.projectId) }
    var taskId by rememberSaveable { mutableStateOf(snap.taskId) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var projectMenu by rememberSaveable { mutableStateOf(false) }
    var pending by rememberRecordState()
    val open = sessionOpen(snap)
    var actionError by remember { mutableStateOf<String?>(null) }
    fun act(action: () -> Unit) {
        actionError = null
        try { action() } catch (error: Exception) {
            if (error is kotlinx.coroutines.CancellationException) throw error
            android.util.Log.e("Presence", "Timer action failed", error)
            actionError = t.actionFailed
        }
    }
    LaunchedEffect(snap.projectId, snap.taskId, projects.map { it.id }) {
        if (open) { selected = snap.projectId; taskId = snap.taskId }
        else if (projects.none { it.id == selected }) { selected = projects.firstOrNull()?.id; taskId = null }
    }
    val project = projects.find { it.id == selected }
    val tasks = rows.alive(KIND_TASK).filter { it.f("projectId") == project?.id && it.f("done") != "1" }
    val task = tasks.find { it.id == taskId }
    val globals = app.repo.globals()
    val planWork = if (open) snap.workMillis else project?.f("workSec")?.toLongOrNull()?.coerceIn(1, 356400)?.times(1000) ?: globals.workMillis
    val shorts = if (open) snap.shortCount else project?.f("shortCount")?.toIntOrNull()?.coerceIn(0,24) ?: globals.shortCount
    val longs = if (open) snap.longCount else project?.f("longCount")?.toIntOrNull()?.coerceIn(0,24) ?: globals.longCount
    val stretches = stretchCount(shorts, longs)
    val face = if (!open) { if (globals.countUp) 0L else stretchLengths(planWork, stretches).first() } else shown
    val worked = elapsedWork(snap, System.currentTimeMillis(), SystemClock.elapsedRealtime())
    val logged = (if (open) snap.projectId else project?.id)?.let { workedMillisFor(rows, it) } ?: 0L
    val spent = logged + if (snap.status in setOf("running", "paused") && snap.kind in setOf("focus", "track")) worked else 0L
    val controls: @Composable () -> Unit = {
        SectionCard(spacing = 8.dp) {
            if (!immersive) {
            Box {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(t.projects, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { contentDescription = t.chooseProject }
                        .clickable(role = androidx.compose.ui.semantics.Role.Button) { projectMenu = true },
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text(project?.f("name") ?: snap.projectName.takeIf { open && it.isNotBlank() } ?: t.chooseProject,
                            modifier = Modifier.weight(1f, fill = false), style = MaterialTheme.typography.headlineMedium,
                            maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Outlined.ExpandMore, null, Modifier.size(24.dp))
                    }
                }
                DropdownMenu(expanded = projectMenu, onDismissRequest = { projectMenu = false }) {
                    projects.forEach { item -> DropdownMenuItem(text = { Text(item.f("name")) }, onClick = {
                        projectMenu = false
                        if (open && item.id != snap.projectId) pending = item else { selected = item.id; taskId = null }
                    }) }
                    DropdownMenuItem(text = { Text(t.newProject) }, onClick = { projectMenu = false; creating = true })
                }
            }
            if (!open && tasks.isNotEmpty()) BalancedChoices(tasks.map { it.id to it.f("title") }, taskId) { id ->
                taskId = if (taskId == id) null else id
            }
            if (open && snap.taskTitle.isNotBlank()) Text(snap.taskTitle, style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(project?.f("name") ?: snap.projectName.ifBlank { t.chooseProject }, Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
                if (open && snap.taskTitle.isNotBlank()) Text(snap.taskTitle, Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PresenceButton(onClick = { act {
                    when (snap.status) {
                        "running" -> app.center.pause()
                        "paused" -> app.center.resume()
                        "awaiting" -> app.center.start(snap.awaitingKind.ifBlank { "focus" }, project, task)
                        else -> if (project != null) app.center.start("focus", project, task) else { actionError = t.needProject; creating = projects.isEmpty() }
                    }
                } }, enabled = true, modifier = Modifier.weight(1f).heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)) {
                    Text(if (snap.status == "running") t.pause else t.play, maxLines = 2, textAlign = TextAlign.Center)
                }
                if (!open && project != null) TonalButton({ act { app.center.start("track", project, task) } }, Modifier.weight(1f).heightIn(min = 48.dp)) { Text(t.trackTime) }
                if (open) TonalButton({ act { app.center.stop() } }, Modifier.weight(1f).heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)) {
                    Text(t.stop)
                }
                if (snap.status in setOf("running", "paused") && snap.kind != "track") TonalButton({ act { app.center.skip() } }, Modifier.weight(1f).heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)) {
                    Text(t.skip, maxLines = 2)
                }
            }
            if (snap.status == "awaiting") Text(t.waiting, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!open && project == null) {
                Text(t.needProject, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TonalButton({ creating = true }) { Text(t.newProject) }
            }
            actionError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite }) }
            if (!immersive) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Metric(t.loggedTime, if (spent < 60000) clockDigits(spent) else clockWords(spent), Modifier.weight(1f), centered = true)
                Metric(t.pausesLabel, "${snap.pauseCount} · ${clockDigits(pauseNoteMillis(snap, System.currentTimeMillis()))}", Modifier.weight(1f), centered = true)
                Metric(t.streak, streakDays(rows, System.currentTimeMillis(), ZoneId.systemDefault(), globals.dayStart).toString(), Modifier.weight(1f), centered = true)
            }
            }
            if (!immersive && !androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()) Text(t.notificationsOff, style = MaterialTheme.typography.bodyMedium)
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().padding(LocalUiLayout.current.margin)) {
        val columns = maxWidth >= 620.dp
        val ringSize = if (columns) minOf(360.dp, maxHeight - 56.dp, maxWidth * 0.45f) else minOf(320.dp, maxWidth, (maxHeight - 48.dp) * 0.5f)
        val ring: @Composable () -> Unit = {
            Box(Modifier.size(ringSize.coerceAtLeast(120.dp)), contentAlignment = Alignment.Center) {
                val accent = MaterialTheme.colorScheme.primary
                val track = MaterialTheme.colorScheme.outlineVariant
                val progress = if (open && snap.kind != "track" && snap.phaseMillis > 0 && snap.status != "awaiting") (worked.toFloat() / snap.phaseMillis).coerceIn(0f,1f) else 0f
                Canvas(Modifier.fillMaxSize().padding(8.dp).semantics { contentDescription = "${phase(t, snap.kind)} ${clockDigits(face)}" }) {
                    val stroke = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                    val inset = 16.dp.toPx()
                    val dialSize = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2)
                    drawArc(track, -90f, 360f, false, topLeft = Offset(inset, inset), size = dialSize, style = stroke)
                    if (progress > 0) drawArc(accent, -90f, progress * 360f, false, topLeft = Offset(inset, inset), size = dialSize, style = stroke)
                    if (snap.kind != "track") {
                        // A concentric lower arc, with a segment for every computed stretch.
                        val step = 120f / stretches
                        val gap = minOf(3f, step * 0.3f)
                        val radius = size.width / 2
                        val markerWidth = minOf(6.dp.toPx(), radius * Math.toRadians(step.toDouble()).toFloat() * 0.4f)
                        repeat(stretches) { index ->
                            val done = open && index < snap.stretchIndex
                            val current = index == (if (open) snap.stretchIndex else 0)
                            drawArc(if (done || current) accent.copy(alpha = if (done) 1f else 0.6f) else track,
                                150f - index * step - gap / 2, -(step - gap), false,
                                style = Stroke(markerWidth, cap = StrokeCap.Round))
                        }
                    }

                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text(if (snap.status == "idle") t.focus else phase(t, snap.kind), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val digitBase = (if (face >= 3_600_000L) (ringSize.value * 0.17f).coerceIn(28f, 60f) else (ringSize.value * 0.23f).coerceIn(36f, 84f)).sp
                    val digitStyle = MaterialTheme.typography.displayLarge.copy(fontSize = digitBase * (MaterialTheme.typography.displayLarge.fontSize.value / 56f))
                    val measure = androidx.compose.ui.text.rememberTextMeasurer().measure(clockDigits(face), digitStyle, softWrap = false, maxLines = 1)
                    val available = with(LocalDensity.current) { (ringSize - 88.dp).coerceAtLeast(70.dp).toPx() }
                    val fit = minOf(1f, available / measure.size.width.coerceAtLeast(1))
                    Text(clockDigits(face), style = digitStyle.copy(fontSize = digitStyle.fontSize * fit, lineHeight = digitStyle.fontSize * fit * 1.15f), maxLines = 1, softWrap = false)
                    if (snap.kind != "track") Text(t.cycleLine(if (open) (snap.stretchIndex + 1).coerceAtMost(stretches) else 1, stretches),
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (snap.status == "paused") Text(t.paused, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        if (maxHeight < 180.dp) Column(Modifier.fillMaxSize().padding(bottom = 48.dp).presenceScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${phase(t, snap.kind)} · ${clockDigits(face)}", style = MaterialTheme.typography.titleLarge)
            controls()
        } else if (columns) Row(Modifier.fillMaxSize().padding(bottom = 48.dp), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { ring() }
            Column(Modifier.weight(1f).widthIn(max = 440.dp).presenceScroll(rememberScrollState())) { controls() }
        } else Column(Modifier.fillMaxSize().padding(bottom = 48.dp).presenceScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp, if (immersive) Alignment.CenterVertically else Alignment.Top)) {
            ring(); controls(); Spacer(Modifier.height(8.dp))
        }
        IconButton(onFullscreen, Modifier.align(Alignment.BottomEnd)) {
            Icon(if (immersive) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen, if (immersive) t.exitFullscreen else t.fullscreen)
        }
    }
    pending?.let { item -> Confirm(t.switchTitle, t.switchBody, t.save, t.cancel, {
        selected = item.id; taskId = null; pending = null; app.center.stop(); app.center.start("focus", item, null)
    }, { pending = null }) }
    if (creating) NameDialog(t.newProject, "", t, { creating = false }) { name -> selected = addProject(app, projects, name); taskId = null; creating = false }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectsPage(
    app: PresenceApp,
    t: T,
    rows: List<Rec>,
    premium: Boolean,
    wide: Boolean,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    onGate: () -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val dayStart = app.repo.globals().dayStart
    val today = dayKey(System.currentTimeMillis(), zone, dayStart)
    val projects = rows.alive(KIND_PROJECT).sortedBy { it.f("sort").toIntOrNull() ?: 0 }
    val tags = rows.alive(KIND_TAG)
    val free = freeProjectIds(projects)
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var menuFor by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberRecordState()
    var pendingPlay by rememberRecordState()
    val visible = projects.filter { filter == null || it.f("tags").split(",").contains(filter) }
    val list: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier.padding(LocalUiLayout.current.margin), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PresenceButton(onClick = { creating = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text(t.newProject)
            }
            if (tags.isNotEmpty()) TagStrip(listOf(null to t.all) + tags.map { it.id to it.f("name") }, filter, t.previous, t.next) { id ->
                filter = if (filter == id) null else id
            }
            if (visible.isEmpty()) Text(t.emptyProjects, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PresenceList(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(visible, key = { it.id }) { project ->
                    val selected = project.id == selectedId
                    val todayMs = rows.filter { it.kind == KIND_SESSION && it.f("projectId") == project.id }.sumOf { rec ->
                        val start = rec.f("startedAt").toLongOrNull() ?: return@sumOf 0L
                        if (dayKey(start, zone, dayStart) != today) 0L else sessionCountedMillis(rec)
                    }
                    Surface(color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { onSelect(project.id) }, onLongClick = { menuFor = project.id })) {
                        Row(Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(chartColor(project.f("color"), MaterialTheme.colorScheme.background.isDark())))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(project.f("name"), style = MaterialTheme.typography.titleMedium)
                                Text(clockWords(todayMs), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val current = app.center.snap()
                            val active = sessionOpen(current) && current.projectId == project.id
                            IconButton(onClick = {
                                if (active) app.center.stop() else if (sessionOpen(current)) pendingPlay = project else app.center.start("focus", project, null)
                            }) { Icon(if (active) Icons.Outlined.Stop else Icons.Outlined.PlayArrow, if (active) t.stop else t.play) }
                            Box {
                                IconButton({ menuFor = project.id }) { Icon(Icons.Outlined.MoreVert, t.actions) }
                                DropdownMenu(menuFor == project.id, { menuFor = null }) {
                                    DropdownMenuItem(text = { Text(t.moveUp) }, onClick = { menuFor = null; move(app, projects, project, -1) })
                                    DropdownMenuItem(text = { Text(t.moveDown) }, onClick = { menuFor = null; move(app, projects, project, 1) })
                                    DropdownMenuItem(text = { Text(t.delete) }, onClick = { menuFor = null; confirmDelete = project })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    val detail: @Composable (Modifier) -> Unit = { modifier ->
        val project = projects.find { it.id == selectedId }
        if (project == null) {
            Box(modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text(t.chooseProject, style = MaterialTheme.typography.titleLarge) }
        } else {
            key(project.id) { ProjectDetail(app, t, rows, project, premium, modifier, onGate) }
        }
    }
    Row(Modifier.fillMaxSize()) {
        if (wide || selectedId == null) list(if (wide) Modifier.width(320.dp).fillMaxHeight() else Modifier.weight(1f).fillMaxHeight())
        if (wide || selectedId != null) detail(Modifier.weight(1f).fillMaxHeight())
    }
    if (creating) {
        NameDialog(t.newProject, "", t, { creating = false }) { name ->
            creating = false
            onSelect(addProject(app, projects, name))
        }
    }
    confirmDelete?.let { item ->
        Confirm(
            t.deleteProject,
            t.deleteProjectBody,
            t.delete,
            t.cancel,
            {
                app.repo.put(item.copy(deleted = true, updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId()))
                rows.alive(KIND_TASK).filter { it.f("projectId") == item.id }.forEach {
                    app.repo.put(it.copy(deleted = true, updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId()))
                }
                if (selectedId == item.id) onSelect(null)
                confirmDelete = null
            },
            { confirmDelete = null },
        )
    }
    pendingPlay?.let { project ->
        Confirm(t.switchTitle, t.switchBody, t.save, t.cancel, {
            pendingPlay = null
            app.center.stop()
            app.center.start("focus", project, null)
        }, { pendingPlay = null })
    }
}

private fun addProject(app: PresenceApp, projects: List<Rec>, name: String): String {
    val id = UUID.randomUUID().toString()
    val color = "%06X".format(Swatches[projects.size % Swatches.size])
    app.repo.put(
        app.repo.writerNow(
            KIND_PROJECT,
            id,
            mapOf(
                "name" to name,
                "color" to color,
                "groupId" to "",
                "goalMinutes" to "",
                "goalDeadline" to "",
                "focus" to "",
                "short" to "",
                "long" to "",
                "cycles" to "",
                "countUp" to "",
                "sort" to ((projects.maxOfOrNull { it.f("sort").toIntOrNull() ?: 0 } ?: 0) + 1).toString(),
                "tags" to "",
                "createdAt" to System.currentTimeMillis().toString(),
            ),
        ),
    )
    return id
}

private fun move(app: PresenceApp, projects: List<Rec>, project: Rec, direction: Int) {
    val index = projects.indexOfFirst { it.id == project.id }
    val other = projects.getOrNull(index + direction) ?: return
    val a = project.body.toMutableMap()
    val b = other.body.toMutableMap()
    val sortA = a["sort"] ?: "0"
    a["sort"] = b["sort"] ?: "0"
    b["sort"] = sortA
    val writer = app.repo.deviceId()
    val now = System.currentTimeMillis()
    app.repo.put(project.copy(updatedAt = now, writerId = writer, body = a))
    app.repo.put(other.copy(updatedAt = now + 1, writerId = writer, body = b))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectDetail(
    app: PresenceApp,
    t: T,
    rows: List<Rec>,
    project: Rec,
    premium: Boolean,
    modifier: Modifier,
    onGate: () -> Unit,
) {
    val tasks = rows.alive(KIND_TASK).filter { it.f("projectId") == project.id }.sortedBy { it.f("sort").toIntOrNull() ?: 0 }
    val tags = rows.alive(KIND_TAG)
    var taskFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var newTask by rememberSaveable { mutableStateOf(false) }
    var goalOpen by rememberSaveable { mutableStateOf(false) }
    var deleteTask by rememberRecordState()
    var editTask by rememberRecordState()
    var newTag by rememberSaveable { mutableStateOf(false) }
    var editTag by rememberRecordState()
    var name by rememberSaveable(project.id) { mutableStateOf(project.f("name")) }
    val zone = ZoneId.systemDefault()
    val globals = app.repo.globals()
    val worked = workedMillisFor(rows, project.id)
    val shownTasks = tasks.filter { taskFilter == null || it.f("tags").split(",").contains(taskFilter) }
    fun saveProject(changes: Map<String, String>) {
        val current = app.repo.snapshot().find { it.kind == KIND_PROJECT && it.id == project.id && !it.deleted } ?: return
        app.repo.put(current.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = current.body + changes))
    }
    var tagMenu by rememberSaveable(project.id) { mutableStateOf<String?>(null) }
    PresenceList(modifier.padding(horizontal = LocalUiLayout.current.margin), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
        item(key = "name") {
            SectionCard {
                OutlinedTextField(name, { value -> name = value; if (value.isNotBlank()) saveProject(mapOf("name" to value.trim())) },
                    label = { Text(t.name) }, colors = presenceFieldColors(), modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text(t.savedAutomatically, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item(key = "goal") {
            SectionCard(t.goal) {
                val goalMinutes = project.f("goalMinutes").toIntOrNull()
                val deadline = project.f("goalDeadline").toLongOrNull()
                if (goalMinutes != null && deadline != null) {
                    val daily = dailyTargetMinutes(goalMinutes, worked, deadline, System.currentTimeMillis(), zone, globals.dayStart)
                    val today = dayKey(System.currentTimeMillis(), zone, globals.dayStart)
                    val todayMs = dayTotals(rows.filter { it.f("projectId") == project.id }, listOf(today), zone, globals.dayStart).single()
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Metric(t.daily, clockWords(daily * 60_000L)); Metric(t.today, clockWords(todayMs))
                        Metric(t.remainder, clockWords((goalMinutes * 60_000L - worked).coerceAtLeast(0)))
                    }
                    Text("${t.deadline}: ${dayKey(deadline, zone, globals.dayStart)}", style = MaterialTheme.typography.bodyMedium)
                    TextButton({ saveProject(mapOf("goalMinutes" to "", "goalDeadline" to "")) }) { Text(t.clearGoal) }
                } else {
                    Text(t.noGoal, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TonalButton({ goalOpen = true }) { Text(t.setGoal) }
                }
            }
        }
        item(key = "plan") {
            SectionCard(t.focusPlan) {
                ProjectPlanFields(t, project, globals) { key, value -> saveProject(mapOf(key to value)); app.center.refreshSettings() }
                Text(t.nextSitting, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item(key = "tags") {
            SectionCard(t.tags) {
                TonalButton({ newTag = true }) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text(t.newTag) }
                tags.forEach { tag ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tag.f("name"), Modifier.weight(1f))
                        Box {
                            IconButton({ tagMenu = tag.id }) { Icon(Icons.Outlined.MoreVert, t.actions) }
                            DropdownMenu(tagMenu == tag.id, { tagMenu = null }) {
                                DropdownMenuItem(text = { Text(t.rename) }, onClick = { tagMenu = null; editTag = tag })
                                DropdownMenuItem(text = { Text(t.delete) }, onClick = {
                                    tagMenu = null; app.repo.put(tag.copy(deleted = true, updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId()))
                                })
                            }
                        }
                    }
                }
                if (tags.isNotEmpty()) {
                    Text(t.assignedTags, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.forEach { tag ->
                            val picked = project.f("tags").split(",").filter { it.isNotBlank() }.toSet()
                            PresenceChip(tag.id in picked, { saveProject(mapOf("tags" to (if (tag.id in picked) picked - tag.id else picked + tag.id).joinToString(","))) }, { Text(tag.f("name")) })
                        }
                    }
                }
            }
        }
        item(key = "task-header") {
            SectionCard(t.tasks) {
                TonalButton({ newTask = true }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text(t.newTask) }
                if (tags.isNotEmpty()) TagStrip(listOf(null to t.all) + tags.map { it.id to it.f("name") }, taskFilter, t.previous, t.next) { id ->
                    taskFilter = if (taskFilter == id) null else id
                }
                if (shownTasks.isEmpty()) Text(t.emptyTasks, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(shownTasks, key = { "task-${it.id}" }) { task ->
            Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(task.f("done") == "1", {
                        val latest = app.repo.snapshot().find { row -> row.kind == KIND_TASK && row.id == task.id } ?: task
                        app.repo.put(latest.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = latest.body + ("done" to if (it) "1" else "0")))
                    })
                    Column(Modifier.weight(1f).heightIn(min = 48.dp).clickable { editTask = task }, verticalArrangement = Arrangement.Center) {
                        Text(task.f("title"), style = MaterialTheme.typography.bodyLarge.copy(textDecoration = if (task.f("done") == "1") TextDecoration.LineThrough else null))
                        val tagNames = tags.filter { it.id in task.f("tags").split(",") }.joinToString(", ") { it.f("name") }
                        if (tagNames.isNotEmpty()) Text(tagNames, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton({ deleteTask = task }) { Icon(Icons.Outlined.DeleteOutline, t.deleteTask) }
                }
            }
        }
    }
    if (newTag) {
        NameDialog(t.newTag, "", t, { newTag = false }) { title ->
            newTag = false
            app.repo.put(app.repo.writerNow(KIND_TAG, UUID.randomUUID().toString(), mapOf("name" to title, "color" to "1F6F54")))
        }
    }
    editTag?.let { tag ->
        NameDialog(t.rename, tag.f("name"), t, { editTag = null }) { title ->
            val body = tag.body.toMutableMap()
            body["name"] = title
            app.repo.put(tag.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = body))
            editTag = null
        }
    }
    editTask?.let { item ->
        var title by rememberSaveable(item.id) { mutableStateOf(item.f("title")) }
        var picked by rememberSaveable(item.id) { mutableStateOf(item.f("tags").split(",").filter { it.isNotBlank() }.toSet()) }
        PresenceDialog(
            onDismissRequest = { editTask = null },
            title = { Text(t.tasks) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(title, { title = it }, label = { Text(t.name) }, modifier = Modifier.fillMaxWidth(), colors = presenceFieldColors())
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.forEach { tag ->
                            PresenceChip(selected = tag.id in picked, onClick = {
                                picked = if (tag.id in picked) picked - tag.id else picked + tag.id
                            }, label = { Text(tag.f("name")) })
                        }
                    }
                }
            },
            confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = {
                val latest = app.repo.snapshot().find { it.kind == KIND_TASK && it.id == item.id } ?: item
                app.repo.put(latest.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = latest.body + mapOf("title" to title.trim(), "tags" to picked.joinToString(","))))
                editTask = null
            }) { Text(t.save) } },
            dismissButton = { TextButton(onClick = { editTask = null }) { Text(t.cancel) } },
        )
    }
    if (newTask) {
        NameDialog(t.newTask, "", t, { newTask = false }) { title ->
            newTask = false
            app.repo.put(
                app.repo.writerNow(
                    KIND_TASK,
                    UUID.randomUUID().toString(),
                    mapOf(
                        "projectId" to project.id,
                        "title" to title,
                        "done" to "0",
                        "tags" to taskFilter.orEmpty(),
                        "sort" to tasks.size.toString(),
                    ),
                ),
            )
        }
    }
    deleteTask?.let { task ->
        Confirm(t.deleteTask, t.deleteTaskBody, t.delete, t.cancel, {
            app.repo.put(task.copy(deleted = true, updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId()))
            deleteTask = null
        }, { deleteTask = null })
    }
    if (goalOpen) GoalDialog(t, { goalOpen = false }) { minutes, deadline ->
        goalOpen = false
        val body = project.body.toMutableMap()
        body["goalMinutes"] = minutes.toString()
        body["goalDeadline"] = deadline.toString()
        app.repo.put(project.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = body))
    }
}

@Composable
private fun PlanSections(
    t: T,
    workSec: Long,
    shortCount: Int,
    longCount: Int,
    shortSec: Long,
    longSec: Long,
    onWork: (Long) -> Unit,
    onShortCount: (Int) -> Unit,
    onLongCount: (Int) -> Unit,
    onShort: (Long) -> Unit,
    onLong: (Long) -> Unit,
) {
    val count = stretchCount(shortCount, longCount)
    val lengths = stretchLengths(workSec.coerceAtLeast(1L) * 1000L, count)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(t.workTime, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Explain(t.workHelp)
        }
        HmsFields(t, workSec, onWork)
        Text(t.stretchSummary(count, clockDigits(lengths.first())), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        CountStepper(t, t.shortBreaks, shortCount, 0, 12, onShortCount)
        HmsFields(t, shortSec, onShort)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        CountStepper(t, t.longBreaks, longCount, 0, 12, onLongCount)
        HmsFields(t, longSec, onLong)
    }
}

@Composable
private fun HmsFields(t: T, totalSec: Long, onSec: (Long) -> Unit) {
    val parts = splitSeconds(totalSec)
    var hours by rememberSaveable(totalSec) { mutableStateOf(parts.first.toString()) }
    var minutes by rememberSaveable(totalSec) { mutableStateOf(parts.second.toString()) }
    var seconds by rememberSaveable(totalSec) { mutableStateOf(parts.third.toString()) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        UnitBox(t.hours, hours, Modifier.weight(1f)) {
            hours = it
            onSec(joinSeconds(it, minutes, seconds))
        }
        UnitBox(t.minutes, minutes, Modifier.weight(1f)) {
            minutes = it
            onSec(joinSeconds(hours, it, seconds))
        }
        UnitBox(t.seconds, seconds, Modifier.weight(1f)) {
            seconds = it
            onSec(joinSeconds(hours, minutes, it))
        }
    }
}

@Composable
private fun UnitBox(label: String, value: String, modifier: Modifier, onValue: (String) -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(value, { raw -> onValue(raw.filter { it in '0'..'9' }.take(2)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label }, colors = presenceFieldColors())
    }
}

private fun splitSeconds(total: Long): Triple<Int, Int, Int> {
    val safe = total.coerceAtLeast(0L)
    return Triple((safe / 3600L).toInt(), ((safe % 3600L) / 60L).toInt(), (safe % 60L).toInt())
}

private fun joinSeconds(hours: String, minutes: String, seconds: String): Long {
    val h = (hours.toIntOrNull() ?: 0).coerceIn(0, 99)
    val m = (minutes.toIntOrNull() ?: 0).coerceIn(0, 59)
    val s = (seconds.toIntOrNull() ?: 0).coerceIn(0, 59)
    return h * 3600L + m * 60L + s
}

@Composable
private fun ProjectPlanFields(
    t: T,
    project: Rec,
    globals: app.presence.data.Globals,
    onField: (String, String) -> Unit,
) {
    val workSec = project.f("workSec").toLongOrNull() ?: globals.workMillis / 1000L
    val shortCount = project.f("shortCount").toIntOrNull() ?: globals.shortCount
    val longCount = project.f("longCount").toIntOrNull() ?: globals.longCount
    val shortSec = project.f("shortSec").toLongOrNull() ?: globals.shortMillis / 1000L
    val longSec = project.f("longSec").toLongOrNull() ?: globals.longMillis / 1000L
    PlanSections(
        t = t,
        workSec = workSec,
        shortCount = shortCount,
        longCount = longCount,
        shortSec = shortSec,
        longSec = longSec,
        onWork = { onField("workSec", it.coerceAtLeast(1L).toString()) },
        onShortCount = { onField("shortCount", it.toString()) },
        onLongCount = { onField("longCount", it.toString()) },
        onShort = { onField("shortSec", it.coerceAtLeast(1L).toString()) },
        onLong = { onField("longSec", it.coerceAtLeast(1L).toString()) },
    )
}

@Composable
private fun StatsPage(app: PresenceApp, t: T, rows: List<Rec>, premium: Boolean, wide: Boolean, snap: Snap, onGate: () -> Unit) {
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()
    val dayStart = app.repo.globals().dayStart
    val today = dayKey(System.currentTimeMillis(), zone, dayStart)
    var range by rememberSaveable { mutableIntStateOf(0) }
    var anchor by rememberSaveable(stateSaver = DateSaver) { mutableStateOf(today) }
    var month by rememberSaveable(stateSaver = MonthSaver) { mutableStateOf(YearMonth.from(today)) }
    var selectedDay by rememberSaveable(stateSaver = DateSaver) { mutableStateOf(today) }
    var pickedBar by rememberSaveable { mutableIntStateOf(-1) }
    var pickedSlice by rememberSaveable { mutableIntStateOf(-1) }
    var manual by rememberSaveable { mutableStateOf(false) }
    var note by rememberSaveable { mutableStateOf("") }
    val bounds = periodBounds(range, anchor)
    val slices = slicesFor(rows, bounds.first, bounds.second, zone, dayStart)
    val total = slices.sumOf { it.millis }
    val closed = rows.filter { sessionCountedMillis(it) > 0 }
    val count = closed.count { rec -> rec.f("startedAt").toLongOrNull()?.let { dayKey(it, zone, dayStart) in bounds.first..bounds.second } == true }
    val week = periodBounds(1, anchor)
    val days = (0..6).map { week.first.plusDays(it.toLong()) }
    val bars = dayTotals(rows, days, zone, dayStart)
    val percents = wholePercents(slices.map { it.millis })
    val dateFormat = java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
    fun label(first: LocalDate, last: LocalDate) = if (first == last) first.format(dateFormat) else "${first.format(dateFormat)} – ${last.format(dateFormat)}"
    fun navigate(amount: Long) {
        anchor = shiftPeriod(range, anchor, amount).coerceAtMost(today)
        selectedDay = anchor; month = YearMonth.from(anchor); pickedBar = -1; pickedSlice = -1
    }
    fun share() {
        val text = shareSummary(t.stats, label(bounds.first, bounds.second), slices.map { it.name.ifBlank { t.unlabeled } to it.millis }) { name ->
            val project = rows.alive(KIND_PROJECT).find { it.f("name") == name }
            rows.alive(KIND_GROUP).find { it.id == project?.f("groupId") }?.f("name").orEmpty()
        }
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), t.share))
    }
    fun add() { if (selectedDay > today) note = t.future else { manual = true; note = "" } }
    val chart: @Composable (Modifier) -> Unit = { modifier ->
        SectionCard(t.byDay, modifier = modifier) {
            Text("${t.week}: ${label(week.first, week.second)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val maxMinutes = (kotlin.math.ceil((bars.maxOrNull() ?: 0L) / 600_000.0).toInt() * 10).coerceAtLeast(10)
            val accent = MaterialTheme.colorScheme.primary
            val outline = MaterialTheme.colorScheme.outlineVariant
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.height(160.dp).widthIn(min = 32.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Text("$maxMinutes", style = MaterialTheme.typography.labelSmall)
                    Text("${maxMinutes / 2}", style = MaterialTheme.typography.labelSmall)
                    Text("0", style = MaterialTheme.typography.labelSmall)
                }
                Column(Modifier.weight(1f)) {
                    Canvas(Modifier.fillMaxWidth().height(160.dp).semantics {
                        contentDescription = days.indices.joinToString("; ") { "${days[it]}: ${clockWords(bars[it])}" }
                    }.pointerInput(bars) { detectTapGestures { offset -> pickedBar = (offset.x / (size.width / 7f)).toInt().coerceIn(0,6) } }) {
                        repeat(3) { i -> val y = size.height * i / 2; drawLine(outline, Offset(0f,y), Offset(size.width,y), 1.dp.toPx()) }
                        val slot = size.width / 7f
                        bars.forEachIndexed { index, ms ->
                            val h = (ms.toFloat() / (maxMinutes * 60_000f)).coerceIn(0f,1f) * (size.height - 4.dp.toPx())
                            if (h > 0) drawRoundRect(accent.copy(alpha = if (pickedBar == index || pickedBar == -1) 1f else 0.5f),
                                Offset(index * slot + slot * 0.2f, size.height - h), Size(slot * 0.6f, h), androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
                        }
                    }
                    Row(Modifier.fillMaxWidth()) { days.forEach { Text(it.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 1) } }
                }
            }
            Text(if (bars.all { it == 0L }) t.emptyRange else t.minutes, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (pickedBar in days.indices) Text("${days[pickedBar].format(dateFormat)}: ${clockWords(bars[pickedBar])}", style = MaterialTheme.typography.bodyMedium)
        }
    }
    val projectChart: @Composable (Modifier) -> Unit = { modifier ->
        SectionCard(t.byProject, modifier = modifier) {
            val colors = slices.map { slice -> chartColor(rows.alive(KIND_PROJECT).find { it.id == slice.id }?.f("color") ?: "14614A", MaterialTheme.colorScheme.background.isDark()) }
            val track = MaterialTheme.colorScheme.outlineVariant
            Box(Modifier.fillMaxWidth().height(196.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(176.dp).padding(12.dp).pointerInput(slices) {
                    detectTapGestures { offset ->
                        if (total <= 0) return@detectTapGestures
                        val angle = (Math.toDegrees(atan2((offset.y-size.height/2).toDouble(), (offset.x-size.width/2).toDouble())) + 450) % 360
                        var edge = 0.0
                        for (i in slices.indices) { edge += slices[i].millis * 360.0 / total; if (angle <= edge) { pickedSlice = i; break } }
                    }
                }.semantics { contentDescription = slices.joinToString("; ") { "${it.name}: ${clockWords(it.millis)}" }.ifBlank { t.emptyRange } }) {
                    val stroke = Stroke(18.dp.toPx())
                    if (total <= 0) drawArc(track, -90f, 360f, false, style = stroke)
                    else {
                        var start = -90f
                        slices.forEachIndexed { i, slice -> val sweep = slice.millis.toFloat() / total * 360f; drawArc(colors[i], start, sweep, false, style = stroke); start += sweep }
                    }
                }
                Text(clockWords(total), style = MaterialTheme.typography.titleLarge)
            }
            if (slices.isEmpty()) Text(t.emptyRange, color = MaterialTheme.colorScheme.onSurfaceVariant)
            slices.forEachIndexed { i, slice ->
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { pickedSlice = i }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(colors[i]))
                    Text(slice.name.ifBlank { t.unlabeled }, Modifier.weight(1f))
                    Text(clockWords(slice.millis), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (pickedSlice in slices.indices) Text("${slices[pickedSlice].name.ifBlank { t.unlabeled }}: ${percents[pickedSlice]}%", style = MaterialTheme.typography.bodyMedium)
        }
    }
    val selectedSessions = closed.filter { it.f("startedAt").toLongOrNull()?.let { dayKey(it, zone, dayStart) == selectedDay } == true }.sortedBy { it.f("startedAt").toLongOrNull() }
    val dayDetail: @Composable (Modifier) -> Unit = { modifier ->
        SectionCard(modifier = modifier) {
            Text(selectedDay.format(dateFormat), style = MaterialTheme.typography.titleLarge)
            Metric(t.focusTime, clockWords(dayTotals(rows, listOf(selectedDay), zone, dayStart).single()))
            PresenceButton({ add() }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text(t.manual) }
            TextButton({ share() }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Share, null); Spacer(Modifier.width(8.dp)); Text("${t.share} · ${listOf(t.today,t.week,t.month,t.year)[range]}") }
            Text(t.onlyFocus, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (note.isNotEmpty()) Text(note, color = MaterialTheme.colorScheme.error)
        }
    }
    PresenceList(Modifier.fillMaxSize().padding(horizontal = LocalUiLayout.current.margin), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item(key = "period") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BalancedChoices(listOf(t.today,t.week,t.month,t.year).mapIndexed { i, title -> i.toString() to title }, range.toString(), maxColumns = 4) { id ->
                    range = id!!.toInt(); pickedSlice = -1
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton({ navigate(-1) }) { Icon(Icons.Outlined.ChevronLeft, t.previous) }
                    Text(label(bounds.first,bounds.second), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                    IconButton({ navigate(1) }, enabled = bounds.second < today) { Icon(Icons.Outlined.ChevronRight, t.next) }
                }
            }
        }
        item(key = "charts") {
            if (wide) Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                chart(Modifier.weight(1f).fillMaxHeight())
                projectChart(Modifier.weight(1f).fillMaxHeight())
            } else Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { chart(Modifier); projectChart(Modifier) }
        }
        item(key = "streak") { SectionCard(t.streak) {
            Text(t.streakLine(streakDays(rows, System.currentTimeMillis(), zone, dayStart), loggedToday(rows, System.currentTimeMillis(), zone, dayStart)), style = MaterialTheme.typography.titleMedium)
        } }
        item(key = "actions") {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TonalButton({ share() }, Modifier.weight(1f).fillMaxHeight()) { Icon(Icons.Outlined.Share, null); Spacer(Modifier.width(8.dp)); Text("${t.share} · ${listOf(t.today,t.week,t.month,t.year)[range]}") }
                TonalButton({ add() }, Modifier.weight(1f).fillMaxHeight()) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text(t.manual) }
            }
        }
        item(key = "totals") {
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionCard(modifier = Modifier.weight(1f).fillMaxHeight()) { Metric(t.focusTime, clockWords(total), Modifier.fillMaxWidth(), centered = true) }
                SectionCard(modifier = Modifier.weight(1f).fillMaxHeight()) { Metric(t.sessions, count.toString(), Modifier.fillMaxWidth(), centered = true) }
            }
        }
        item(key = "calendar") {
            val calendar: @Composable (Modifier) -> Unit = { modifier -> SectionCard(modifier = modifier) { CalendarBlock(t, rows, month, selectedDay, zone, dayStart, { month = it }, { selectedDay = it; anchor = it; pickedBar = -1 }) } }
            if (wide) Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                calendar(Modifier.weight(1f).fillMaxHeight())
                dayDetail(Modifier.weight(1f).fillMaxHeight())
            } else Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { calendar(Modifier); dayDetail(Modifier) }
        }
        items(selectedSessions, key = { "session-${it.id}" }) { rec ->
            SectionCard { Text(rec.f("projectName").ifBlank { t.unlabeled }); Text("${phase(t,rec.f("kind"))}: ${clockWords(sessionCountedMillis(rec))}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
    if (manual) ManualDialog(t, rows, selectedDay, dayStart, snap, { manual = false }) { message -> note = message; if (message.isEmpty()) manual = false }
}

@Composable
private fun CalendarBlock(t: T, rows: List<Rec>, month: YearMonth, selected: LocalDate, zone: ZoneId, dayStart: Int, onMonth: (YearMonth) -> Unit, onDay: (LocalDate) -> Unit) {
    val pad = month.atDay(1).dayOfWeek.value - 1
    val marked = rows.filter { sessionCountedMillis(it) > 0 }.mapNotNull { it.f("startedAt").toLongOrNull()?.let { start -> dayKey(start, zone, dayStart) } }.toSet()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton({ onMonth(month.minusMonths(1)) }) { Icon(Icons.Outlined.ChevronLeft,t.previous) }
            Text(month.atDay(1).format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            IconButton({ onMonth(month.plusMonths(1)) }) { Icon(Icons.Outlined.ChevronRight,t.next) }
        }
        Row {
            (1..7).forEach { Text(java.time.DayOfWeek.of(it).getDisplayName(TextStyle.NARROW,Locale.getDefault()), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        repeat((pad + month.lengthOfMonth() + 6) / 7) { week ->
            Row {
                repeat(7) { dow ->
                    val number = week * 7 + dow - pad + 1
                    val date = if (number in 1..month.lengthOfMonth()) month.atDay(number) else null
                    Box(Modifier.weight(1f).heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
                        if (date != null) Surface(onClick = { onDay(date) }, shape = CircleShape,
                            color = if (date == selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics { contentDescription = date.toString() }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(number.toString(), style = MaterialTheme.typography.bodyMedium)
                                Box(Modifier.size(4.dp).clip(CircleShape).background(if (date in marked) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualDialog(t: T, rows: List<Rec>, day: LocalDate, dayStart: Int, snap: Snap, onClose: () -> Unit, onDone: (String) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as PresenceApp
    val projects = rows.alive(KIND_PROJECT)
    var entryDay by rememberSaveable(stateSaver = DateSaver) { mutableStateOf(day) }
    var startHour by rememberSaveable { mutableIntStateOf(dayStart) }
    var startMinute by rememberSaveable { mutableIntStateOf(0) }
    var project by rememberSaveable { mutableStateOf(projects.firstOrNull()?.id.orEmpty()) }
    var hours by rememberSaveable { mutableIntStateOf(0) }
    var mins by rememberSaveable { mutableIntStateOf(25) }
    var kind by rememberSaveable { mutableStateOf("focus") }
    var error by rememberSaveable { mutableStateOf("") }
    PresenceDialog(
        onDismissRequest = onClose,
        title = { Text(t.manual) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MenuField(t.projects, t.chooseProject, projects.find { it.id == project }?.f("name") ?: t.unlabeled,
                    listOf("" to t.unlabeled) + projects.map { it.id to it.f("name") }) { project = it }
                TextButton(onClick = {
                    DatePickerDialog(context, { _, y, m, d -> entryDay = LocalDate.of(y, m + 1, d) }, entryDay.year, entryDay.monthValue - 1, entryDay.dayOfMonth).show()
                }) { Text(entryDay.toString()) }
                TextButton(onClick = {
                    TimePickerDialog(context, { _, h, m -> startHour = h; startMinute = m }, startHour, startMinute, true).show()
                }) { Text("${t.sessionStart}: ${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')}") }
                CountStepper(t, t.hours, hours, 0, 12) { hours = it }
                CountStepper(t, t.minutes, mins, 0, 59) { mins = it }
                Text(t.pastEntryHelp, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
                Row {
                    PresenceChip(selected = kind == "focus", onClick = { kind = "focus" }, label = { Text(t.focus) })
                    Spacer(Modifier.width(8.dp))
                    PresenceChip(selected = kind == "track", onClick = { kind = "track" }, label = { Text(t.track) })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val duration = (hours * 60L + mins) * 60_000L
                val zone = ZoneId.systemDefault()
                val calendarDay = if (startHour < dayStart) entryDay.plusDays(1) else entryDay
                val start = calendarDay.atTime(startHour, startMinute).atZone(zone).toInstant().toEpochMilli()
                when {
                    duration < 60_000L -> run { error = t.atLeast }
                    start + duration > System.currentTimeMillis() -> run { error = t.future }
                    overlapsClosed(rows, start, duration) -> run { error = t.recordedOverlap }
                    overlapsOpen(snap, start, duration, System.currentTimeMillis()) -> run { error = t.overlap }
                    else -> {
                val chosen = projects.find { it.id == project }
                val group = rows.alive(KIND_GROUP).find { it.id == chosen?.f("groupId") }?.f("name").orEmpty()
                    app.repo.put(
                        manualSession(
                            UUID.randomUUID().toString(),
                            app.repo.deviceId(),
                            chosen?.id.orEmpty(),
                            chosen?.f("name").orEmpty(),
                            group,
                            kind,
                            start,
                            duration,
                            System.currentTimeMillis(),
                        ),
                    )
                    onDone("")
                }
            }
            }) { Text(t.save) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(t.cancel) } },
    )
}

@Composable
private fun TodosPage(app: PresenceApp, t: T, rows: List<Rec>, wide: Boolean) {
    val context = LocalContext.current
    var draft by rememberSaveable { mutableStateOf("") }
    var menu by rememberSaveable { mutableStateOf<String?>(null) }
    var deleting by rememberRecordState()
    val todos = rows.filter { it.kind == KIND_LOCAL && it.id.startsWith("todo:") && !it.deleted }.sortedBy { it.f("sort").toLongOrNull() ?: it.updatedAt }
    fun add() {
        if (draft.isBlank()) return
        app.repo.addTodo(draft); draft = ""; HomeWidgets.update(context)
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 800.dp).fillMaxSize().padding(horizontal = LocalUiLayout.current.margin, vertical = if (LocalUiLayout.current.short && LocalUiLayout.current.keyboard) 0.dp else LocalUiLayout.current.margin), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(draft, { draft = it }, modifier = Modifier.weight(1f), label = { Text(t.todoHint) }, singleLine = true,
                    colors = presenceFieldColors(), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { add() }))
                PresenceButton({ add() }, enabled = draft.isNotBlank(), modifier = Modifier.size(56.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Outlined.Add, t.addItem) }
            }
            if (todos.isEmpty()) SectionCard { Text(t.emptyTodos, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            PresenceList(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(todos, key = { it.id }) { item ->
                    val id = item.id.removePrefix("todo:")
                    val done = item.f("done") == "1"
                    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(done, { app.repo.setTodoDone(id, it); HomeWidgets.update(context) })
                            Text(item.f("title"), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge.copy(textDecoration = if (done) TextDecoration.LineThrough else null),
                                color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                            Box {
                                IconButton({ menu = id }) { Icon(Icons.Outlined.MoreVert, t.actions) }
                                DropdownMenu(menu == id, { menu = null }) {
                                    DropdownMenuItem(text = { Text(t.delete) }, onClick = { menu = null; deleting = item })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    deleting?.let { item -> Confirm(t.delete, item.f("title"), t.delete, t.cancel, {
        app.repo.deleteTodo(item.id.removePrefix("todo:")); HomeWidgets.update(context); deleting = null
    }, { deleting = null }) }
}

@Composable
private fun ReminderRow(
    title: String,
    help: String,
    on: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onTime: (Int, Int) -> Unit,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.surface,
        contentColor = scheme.onSurface,
        border = BorderStroke(1.dp, scheme.outline),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, modifier = Modifier.weight(1f), color = scheme.onSurface, style = MaterialTheme.typography.titleMedium)
                Explain(help)
                PresenceSwitch(checked = on, onCheckedChange = onToggle)
            }
            Surface(
                color = scheme.surface,
                contentColor = scheme.onSurface,
                border = BorderStroke(1.dp, scheme.outline),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.heightIn(min = 48.dp).clickable {
                    TimePickerDialog(context, { _, h, m -> onTime(h, m) }, hour, minute, true).show()
                },
            ) {
                Text(
                    String.format(Locale.ROOT, "%02d:%02d", hour, minute),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = scheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

@Composable
private fun SettingsPage(app: PresenceApp, t: T, rows: List<Rec>, premium: Boolean, wide: Boolean, onPair: () -> Unit, onGate: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val globals = app.repo.globals()
    val flags = rows.firstOrNull { it.kind == KIND_LOCAL && it.id == "flags" }?.body.orEmpty()
    var note by rememberSaveable { mutableStateOf("") }
    fun fileOperation(block: () -> Unit) {
        scope.launch {
            try { withContext(Dispatchers.IO) { block() } }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { note = t.fileError }
        }
    }
    val exportJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        fileOperation {
            val text = encodeBackup(app.repo.snapshot().filter { it.kind in SYNC_KINDS })
            (context.contentResolver.openOutputStream(uri) ?: throw java.io.IOException("Cannot write file")).use { it.write(text.toByteArray()) }
        }
    }
    val exportCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        fileOperation {
            val text = sessionsCsv(app.repo.snapshot(), ZoneId.systemDefault(), app.repo.globals().dayStart)
            (context.contentResolver.openOutputStream(uri) ?: throw java.io.IOException("Cannot write file")).use { it.write(text.toByteArray()) }
        }
    }
    val importJson = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        fileOperation {
            val text = (context.contentResolver.openInputStream(uri) ?: throw java.io.IOException("Cannot read file")).bufferedReader().use { it.readText() }
            val parsed = decodeBackup(text)
            if (parsed == null) note = t.badFile else {
                app.repo.mergeFrom(parsed)
                note = t.imported
            }
        }
    }
    val sound = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        fileOperation {
            val directory = AppStorage.privateDir(context)
            val pending = java.io.File(directory, "alarm-custom.pending")
            try {
                (context.contentResolver.openInputStream(uri) ?: throw java.io.IOException("Cannot read sound")).use { input ->
                    pending.outputStream().use { output -> input.copyTo(output) }
                }
                if (pending.length() == 0L || !pending.renameTo(java.io.File(directory, "alarm-custom"))) {
                    throw java.io.IOException("Cannot save sound")
                }
                app.repo.setPref("alarm", "custom")
            } finally { pending.delete() }
        }
    }
    val notifyPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val askNotify: () -> Unit = {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notifyPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val theme = flags["theme"] ?: "system"
    val palette = flags["palette"] ?: "grove"
    val textSize = flags["text"] ?: "standard"
    val language = flags["language"] ?: "system"
    val storage by app.repo.storage().collectAsState()
    LaunchedEffect(Unit) { app.repo.refreshStorage() }
    val timerPane: @Composable () -> Unit = {
        SectionCard(t.timerDefaults) {
            Text(t.savedAutomatically, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PlanSections(
            t = t,
            workSec = globals.workMillis / 1000L,
            shortCount = globals.shortCount,
            longCount = globals.longCount,
            shortSec = globals.shortMillis / 1000L,
            longSec = globals.longMillis / 1000L,
            onWork = { app.repo.setPref("work_sec", it.coerceAtLeast(1L).toString()); app.center.refreshSettings() },
            onShortCount = { app.repo.setPref("short_count", it.toString()); app.center.refreshSettings() },
            onLongCount = { app.repo.setPref("long_count", it.toString()); app.center.refreshSettings() },
            onShort = { app.repo.setPref("short_sec", it.coerceAtLeast(1L).toString()); app.center.refreshSettings() },
            onLong = { app.repo.setPref("long_sec", it.coerceAtLeast(1L).toString()); app.center.refreshSettings() },
        )
        SwitchSetting(t.countUp, t.countUpHelp, globals.countUp) {
            app.repo.setPref("count_up", if (it) "1" else "0")
            app.center.refreshSettings()
        }
        SwitchSetting(t.autoContinue, t.autoHelp, globals.auto) {
            app.repo.setPref("auto_start", if (it) "1" else "0")
            app.center.refreshSettings()
        }
        Stepper(t, t.dayStarts, t.dayHelp, globals.dayStart, 0, 6) { app.repo.setPref("day_start", it.toString()) }
            Text(t.nextSitting, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { app.repo.resetSettings(); app.center.refreshSettings(); Reminders.reschedule(context); note = t.resetDone }) { Text(t.resetSettings) }
        }
    }
    val reminderPane: @Composable () -> Unit = { SectionCard(t.reminders) {
        ReminderRow(t.dailyReminder, t.dailyHelp, globals.dailyOn, globals.dailyHour, globals.dailyMinute, { on ->
            if (on) askNotify()
            app.repo.setPref("daily_on", if (on) "1" else "0")
            Reminders.reschedule(context)
        }) { hour, minute ->
            app.repo.setPref("daily_hour", hour.toString())
            app.repo.setPref("daily_minute", minute.toString())
            Reminders.reschedule(context)
        }
        ReminderRow(t.streakReminder, t.streakHelp, globals.streakOn, globals.streakHour, globals.streakMinute, { on ->
            if (on) askNotify()
            app.repo.setPref("streak_on", if (on) "1" else "0")
            Reminders.reschedule(context)
        }) { hour, minute ->
            app.repo.setPref("streak_hour", hour.toString())
            app.repo.setPref("streak_minute", minute.toString())
            Reminders.reschedule(context)
        }
    } }
    val appearancePane: @Composable () -> Unit = { SectionCard(t.appearance) {
        val themeLabel = when (theme) {
            "light" -> t.light
            "dark" -> t.dark
            else -> t.followSystem
        }
        MenuField(t.themeChoice, t.themeMenuHelp, themeLabel, listOf("light" to t.light, "dark" to t.dark, "system" to t.followSystem)) {
            app.repo.updateFlags { flags -> flags["theme"] = it }
        }
        val paletteNames = listOf(
            "grove" to t.paletteGrove,
            "harbor" to t.paletteHarbor,
            "dusk" to t.paletteDusk,
            "slate" to t.paletteSlate,
            "berry" to t.paletteBerry,
            "amber" to t.paletteAmber,
            "moss" to t.paletteMoss,
            "iris" to t.paletteIris,
            "copper" to t.paletteCopper,
            "tide" to t.paletteTide,
            "ink" to t.paletteInk,
        )
        MenuField(t.colorTheme, t.paletteMenuHelp, paletteNames.find { it.first == palette }?.second ?: t.paletteGrove, paletteNames) {
            app.repo.updateFlags { flags -> flags["palette"] = it }
        }
        val textLabel = when (textSize) {
            "compact" -> t.compact
            "large" -> t.large
            else -> t.standard
        }
        MenuField(t.textSize, t.textMenuHelp, textLabel, listOf("compact" to t.compact, "standard" to t.standard, "large" to t.large)) {
            app.repo.updateFlags { flags -> flags["text"] = it }
        }
        val languages = listOf(
            "system" to t.system,
            "en" to t.langEnglish,
            "tr" to t.langTurkish,
            "es" to t.langSpanish,
            "pt" to t.langPortuguese,
            "fr" to t.langFrench,
            "ja" to t.langJapanese,
            "ko" to t.langKorean,
        )
        MenuField(t.language, t.languageMenuHelp, languages.find { it.first == language }?.second ?: t.system, languages) {
            app.repo.updateFlags { flags -> flags["language"] = it }
        }
    } }
    val soundPane: @Composable () -> Unit = { SectionCard(t.sounds) {
        val alarmLabel = when (globals.alarm) {
            "bell" -> t.bell
            "pulse" -> t.pulse
            "custom" -> t.custom
            else -> t.chime
        }
        MenuField(t.alarm, t.alarmMenuHelp, alarmLabel, listOf("chime" to t.chime, "bell" to t.bell, "pulse" to t.pulse, "custom" to t.custom)) {
            if (it == "custom") sound.launch(arrayOf("audio/*")) else app.repo.setPref("alarm", it)
        }
        val backgroundLabel = when (globals.background) {
            "rain" -> t.rain
            "brook" -> t.brook
            "hearth" -> t.hearth
            else -> t.off
        }
        MenuField(t.background, t.backgroundMenuHelp, backgroundLabel, listOf("off" to t.off, "rain" to t.rain, "brook" to t.brook, "hearth" to t.hearth)) {
            app.repo.setPref("background", it)
            app.center.catchUp()
        }
        Text("${t.volume}: ${globals.volume}%", style = MaterialTheme.typography.bodyMedium)
        Slider(value = globals.volume.toFloat(), onValueChange = { app.repo.setPref("bg_volume", it.toInt().toString()); app.center.catchUp() }, valueRange = 0f..100f, modifier = Modifier.semantics { contentDescription = t.volume })
    } }
    val dataPane: @Composable () -> Unit = { SectionCard(t.data) {
        ActionRow(t.exportJson, "JSON", Icons.Outlined.FileUpload) { exportJson.launch("presence-backup.json") }
        ActionRow(t.exportCsv, "CSV", Icons.Outlined.TableChart) { exportCsv.launch("presence-sessions.csv") }
        ActionRow(t.importJson, "JSON", Icons.Outlined.FileDownload) { importJson.launch(arrayOf("application/json", "text/*", "*/*")) }
        if (note.isNotEmpty()) Text(note, color = MaterialTheme.colorScheme.primary)
    } }
    val devicesPane: @Composable () -> Unit = { SectionCard(t.devices) { ActionRow(t.pair, t.pairHelp, Icons.Outlined.Wifi, onPair) } }
    val storagePane: @Composable () -> Unit = { SectionCard(t.storage) {
        val storageBody = when {
            storage.cardMissing -> t.sdMissing
            storage.onCard -> t.sdOnCard
            storage.cardPresent -> t.sdPresent
            else -> t.sdNone
        }
        Text(storageBody, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
        if (storage.cardPresent && !storage.onCard && !storage.cardMissing) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PresenceButton(onClick = {
                    note = when (app.repo.moveToSd()) {
                        "moved" -> t.sdOnCard
                        "no-card" -> t.sdNone
                        "missing" -> t.sdMissing
                        else -> t.sdFailed
                    }
                }, modifier = Modifier.heightIn(min = 48.dp)) { Text(t.sdMove) }
                Explain(t.sdMoveHelp)
            }
        }
        if (storage.onCard && storage.cardPresent) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PresenceButton(onClick = {
                    note = when (app.repo.moveToInternal()) {
                        "moved" -> t.sdPresent
                        else -> t.sdFailed
                    }
                }, modifier = Modifier.heightIn(min = 48.dp)) { Text(t.sdInternal) }
                Explain(t.sdInternalHelp)
            }
        }
        if (storage.cardMissing) {
            PresenceButton(onClick = { app.repo.refreshStorage() }, modifier = Modifier.heightIn(min = 48.dp)) { Text(t.sdCheck) }
        }
    } }
    val aboutPane: @Composable () -> Unit = { SectionCard(t.about) {
        Text("Presence", style = MaterialTheme.typography.titleMedium)
        Text(t.aboutBody, color = MaterialTheme.colorScheme.onSurfaceVariant)
        var help by rememberSaveable { mutableStateOf(false) }
        TextButton({ help = true }) { Text(t.howTo) }
        if (help) Introduction(t) { help = false }
        Text("Apache License 2.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } }
    PresenceList(Modifier.fillMaxSize().padding(horizontal = LocalUiLayout.current.margin), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (wide) {
            item(key = "top") {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) { timerPane() }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) { reminderPane(); appearancePane() }
                }
            }
            item(key = "bottom") {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) { soundPane(); dataPane() }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) { devicesPane(); storagePane(); aboutPane() }
                }
            }
        } else {
            item(key = "timer") { timerPane() }; item(key = "reminders") { reminderPane() }
            item(key = "appearance") { appearancePane() }; item(key = "sounds") { soundPane() }
            item(key = "data") { dataPane() }; item(key = "devices") { devicesPane() }
            item(key = "storage") { storagePane() }; item(key = "about") { aboutPane() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Explain(text: String) {
    val state = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(text) } },
        state = state,
        enableUserInput = false,
    ) {
        IconButton(
            onClick = { scope.launch { state.show() } },
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = text,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun MenuField(
    title: String,
    help: String,
    value: String,
    options: List<Pair<String, String>>,
    onPick: (String) -> Unit,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    Box {
        Surface(onClick = { open = true }, color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.heightIn(min = 56.dp).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.bodyLarge)
                }
                Icon(Icons.Outlined.ExpandMore, title)
            }
        }
        DropdownMenu(open, { open = false }) {
            options.forEach { (id, label) -> DropdownMenuItem(text = { Text(label) }, onClick = { open = false; onPick(id) }) }
        }
    }
}

@Composable
private fun SwitchSetting(title: String, help: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
        Explain(help)
        PresenceSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}



@Composable
private fun PairPage(app: PresenceApp, t: T, rows: List<Rec>) {
    val context = LocalContext.current
    if (!app.hasLan()) {
        Text(t.pairMessage("dropped"), modifier = Modifier.padding(16.dp))
        return
    }
    val offer by app.lan.offer.collectAsState()
    val until by app.lan.offerUntil.collectAsState()
    val message by app.lan.message.collectAsState()
    var digits by rememberSaveable { mutableStateOf("") }
    var now by rememberSaveable { mutableIntStateOf(0) }
    var denied by rememberSaveable { mutableStateOf(false) }
    var afterGrant by remember { mutableStateOf<(() -> Unit)?>(null) }
    val nearbyPermission = Manifest.permission.NEARBY_WIFI_DEVICES
    val askNearby = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val next = afterGrant
        afterGrant = null
        denied = !granted
        if (granted) next?.invoke()
    }
    val withLink: (() -> Unit) -> Unit = { block ->
        val granted = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, nearbyPermission) == PackageManager.PERMISSION_GRANTED
        if (granted) block() else {
            afterGrant = block
            askNearby.launch(nearbyPermission)
        }
    }
    LaunchedEffect(offer) {
        while (offer != null) {
            delay(500)
            now++
        }
    }
    val leftMs = (until - System.currentTimeMillis()).coerceAtLeast(0)
    val peers = rows.filter { it.kind == KIND_LOCAL && it.id.startsWith("peer:") && !it.deleted && it.f("token").isNotEmpty() }
    var forgetting by rememberRecordState()
    val show: @Composable () -> Unit = {
        SectionCard(t.showCode) {
            Text(t.offerHelp, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (offer != null) {
                Text(offer!!, style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum", letterSpacing = 6.sp), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text(t.expiresIn(clockDigits(leftMs)), color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton({ app.lan.hideCode() }) { Text(t.cancel) }
            } else {
                if (message == "expired") Text(t.expired)
                PresenceButton({ withLink { app.lan.showCode() } }) { Text(if (message == "expired") t.newCode else t.showCode) }
            }
        }
    }
    val enter: @Composable () -> Unit = {
        SectionCard(t.enterCode) {
            Text(t.enterHelp, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(digits, { if (it.length <= 4 && it.all { ch -> ch in '0'..'9' }) digits = it },
                label = { Text(t.enterCode) }, singleLine = true, colors = presenceFieldColors(), modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (digits.length == 4) withLink { app.lan.enter(digits) } }))
            PresenceButton({ withLink { app.lan.enter(digits) } }, enabled = digits.length == 4 && message != "syncing") { Text(t.pairAction) }
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 960.dp).fillMaxSize().presenceScroll(rememberScrollState()).padding(LocalUiLayout.current.margin), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (now < 0) return@Column
            Text(t.pairHelp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (denied) SectionCard {
                Text(t.nearbyDenied, color = MaterialTheme.colorScheme.error)
                TextButton({ context.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.fromParts("package", context.packageName, null))) }) { Text(t.androidSettings) }
            }
            if (LocalUiLayout.current.twoPanes) Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.weight(1f)) { show() }; Box(Modifier.weight(1f)) { enter() }
            } else { show(); enter() }
            if (message.isNotEmpty() && message != "expired") Text(t.pairMessage(message), color = MaterialTheme.colorScheme.primary)
            SectionCard(t.devices) {
                if (peers.isEmpty()) Text(t.noPeers, color = MaterialTheme.colorScheme.onSurfaceVariant)
                peers.forEach { peer -> key(peer.id) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(peer.f("name").ifBlank { "Presence" }, style = MaterialTheme.typography.titleMedium)
                        val last = peer.f("lastSync").toLongOrNull() ?: 0
                        if (last > 0) Text(java.time.Instant.ofEpochMilli(last).atZone(ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofLocalizedDateTime(java.time.format.FormatStyle.SHORT)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TonalButton({ withLink { app.lan.syncNow(peer.id.removePrefix("peer:")) } }, enabled = message != "syncing") { Text(t.syncNow) }
                            TextButton({ forgetting = peer }) { Text(t.forget) }
                        }
                    }
                } }
            }
            Text(t.localNetwork, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    forgetting?.let { peer -> Confirm(t.forget, peer.f("name"), t.forget, t.cancel, { app.repo.forgetPeer(peer.id.removePrefix("peer:")); forgetting = null }, { forgetting = null }) }
}

@Composable
private fun Stepper(t: T, title: String, help: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
            Explain(help)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TonalButton(
                onClick = { if (value > min) onChange(value - 1) },
                enabled = value > min,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp),
            ) { Icon(Icons.Filled.Remove, contentDescription = t.decrease) }
            Text(
                value.toString(),
                modifier = Modifier.widthIn(min = 36.dp),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )
            TonalButton(
                onClick = { if (value < max) onChange(value + 1) },
                enabled = value < max,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp),
            ) { Icon(Icons.Filled.Add, contentDescription = t.increase) }
        }
    }
}

@Composable
private fun CountStepper(t: T, label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        TonalButton(
            onClick = { if (value > min) onChange(value - 1) },
            enabled = value > min,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp),
        ) { Icon(Icons.Filled.Remove, contentDescription = t.decrease) }
        Text(
            value.toString(),
            modifier = Modifier.widthIn(min = 36.dp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        TonalButton(
            onClick = { if (value < max) onChange(value + 1) },
            enabled = value < max,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp),
        ) { Icon(Icons.Filled.Add, contentDescription = t.increase) }
    }
}


@Composable
private fun PresenceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val context = LocalContext.current
    val failure = LocalUiText.current.actionFailed
    Button(
        onClick = {
            try { onClick() } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                android.util.Log.e("Presence", "Action failed", error)
                android.widget.Toast.makeText(context, failure, android.widget.Toast.LENGTH_LONG).show()
            }
        },
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
        colors = presenceButtonColors(),
        shape = RoundedCornerShape(12.dp),
        content = content,
    )
}

@Composable
private fun TonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val context = LocalContext.current
    val failure = LocalUiText.current.actionFailed
    FilledTonalButton(
        onClick = {
            try { onClick() } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                android.util.Log.e("Presence", "Action failed", error)
                android.widget.Toast.makeText(context, failure, android.widget.Toast.LENGTH_LONG).show()
            }
        },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        colors = presenceTonalColors(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        content = content,
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun PresenceChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier.heightIn(min = 48.dp),
        colors = presenceChipColors(),
        border = presenceChipBorder(enabled = true, selected = selected),
    )
}

@Composable
private fun PresenceSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(checked = checked, onCheckedChange = onCheckedChange, colors = presenceSwitchColors())
}

@Composable
private fun Confirm(title: String, body: String, ok: String, cancel: String, onOk: () -> Unit, onCancel: () -> Unit) {
    PresenceDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onOk) { Text(ok) } },
        dismissButton = { TextButton(onClick = onCancel) { Text(cancel) } },
    )
}

@Composable
private fun NameDialog(title: String, initial: String, t: T, onClose: () -> Unit, onSave: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial) }
    var error by rememberSaveable { mutableStateOf(false) }
    PresenceDialog(
        onDismissRequest = onClose,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(name, { name = it; error = false }, label = { Text(t.name) }, colors = presenceFieldColors(), modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { if (name.isNotBlank()) onSave(name.trim()) else error = true }))
                if (error) Text(t.giveName)
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isBlank()) error = true else onSave(name.trim()) }) { Text(t.save) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(t.cancel) } },
    )
}

@Composable
private fun GoalDialog(t: T, onClose: () -> Unit, onSave: (Int, Long) -> Unit) {
    val context = LocalContext.current
    var hours by rememberSaveable { mutableStateOf("10") }
    var mins by rememberSaveable { mutableStateOf("0") }
    var deadline by rememberSaveable(stateSaver = DateSaver) { mutableStateOf(LocalDate.now().plusMonths(1)) }
    PresenceDialog(
        onDismissRequest = onClose,
        title = { Text(t.setGoal) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(hours, { hours = it.filter { c -> c in '0'..'9' }.take(5) }, label = { Text(t.hours) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = presenceFieldColors(), modifier = Modifier.weight(1f))
                    OutlinedTextField(mins, { mins = it.filter { c -> c in '0'..'9' }.take(2) }, label = { Text(t.minutes) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = presenceFieldColors(), modifier = Modifier.weight(1f))
                }
                Text(t.goalTargetHelp, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = {
                    DatePickerDialog(context, { _, y, m, d -> deadline = LocalDate.of(y, m + 1, d) }, deadline.year, deadline.monthValue - 1, deadline.dayOfMonth).show()
                }) { Text(t.deadline + " · " + deadline) }
            }
        },
        confirmButton = {
            TextButton(enabled = (hours.toIntOrNull() ?: 0) > 0 || (mins.toIntOrNull() ?: 0) > 0, onClick = {
                val total = (hours.toIntOrNull() ?: 0).coerceIn(0,10000) * 60 + (mins.toIntOrNull() ?: 0).coerceIn(0,59)
                if (total > 0) {
                    val capped = min(total, 10000 * 60)
                    val end = deadline.atTime(18, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    onSave(capped, end)
                }
            }) { Text(t.save) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(t.cancel) } },
    )
}
