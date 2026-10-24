@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package app.presence.ui

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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun PresenceRoot() {
    val context = LocalContext.current
    val app = context.applicationContext as PresenceApp
    var ready by remember { mutableStateOf(app.ready.isCompleted) }
    LaunchedEffect(Unit) {
        app.ready.await()
        ready = true
    }
    if (!ready) {
        PresenceTheme(androidx.compose.foundation.isSystemInDarkTheme()) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                Text("Presence", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium)
            }
        }
        return
    }
    val rows by app.repo.observe().collectAsState(initial = emptyList())
    val flags = rows.firstOrNull { it.kind == KIND_LOCAL && it.id == "flags" }?.body.orEmpty()
    val t = remember(flags["language"]) { T(resolveLang(flags["language"] ?: "system")) }
    val premium = true
    val dark = when (flags["theme"]) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val palette = flags["palette"] ?: "grove"
    val scale = when (flags["text"]) {
        "compact" -> 0.9f
        "large" -> 1.16f
        else -> 1f
    }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val tall = configuration.orientation == Configuration.ORIENTATION_PORTRAIT ||
        configuration.screenHeightDp > configuration.screenWidthDp
    val wide = !tall && calculateWindowSizeClass(context as android.app.Activity).widthSizeClass != WindowWidthSizeClass.Compact
    androidx.compose.runtime.CompositionLocalProvider(
        LocalDensity provides Density(density.density * scale, density.fontScale * scale),
    ) {
        PresenceTheme(dark, palette) {
            Shell(app, rows, t, premium, wide)
        }
    }
}

@Composable
private fun Shell(app: PresenceApp, rows: List<Rec>, t: T, premium: Boolean, wide: Boolean) {
    val context = LocalContext.current
    var tab by remember { mutableStateOf("timer") }
    var projectId by remember { mutableStateOf<String?>(null) }
    var pairing by remember { mutableStateOf(false) }
    val activity = context as? MainActivity
    val requested = activity?.requestedTab
    LaunchedEffect(requested) {
        if (!requested.isNullOrEmpty()) {
            tab = requested
            pairing = false
            activity?.requestedTab = null
        }
    }
    var tick by remember { mutableIntStateOf(0) }
    val snap = rows.firstOrNull { it.kind == KIND_LOCAL && it.id == "snapshot" && !it.deleted }?.let { snapFrom(it.body) } ?: Snap()
    LaunchedEffect(snap.status, snap.completionToken) {
        while (snap.status == "running" || snap.status == "paused") {
            delay(200)
            tick++
        }
    }
    val nowE = System.currentTimeMillis()
    val nowL = SystemClock.elapsedRealtime()
    val shown = displayMillis(snap, nowE, nowL)
    if (tick < 0) return
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    BackHandler(enabled = pairing || (!wide && projectId != null && tab == "projects")) {
        if (pairing) pairing = false else projectId = null
    }
    val page: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier) {
            if ((snap.status == "running" || snap.status == "paused") && tab != "timer") {
                Mini(t, snap, shown) { tab = "timer"; pairing = false }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    pairing -> PairPage(app, t, rows)
                    tab == "timer" -> TimerPage(app, t, rows, premium, wide, snap, shown) { }
                    tab == "projects" -> ProjectsPage(app, t, rows, premium, wide, projectId, { projectId = it }) { }
                    tab == "todos" -> TodosPage(app, t, rows, wide)
                    tab == "stats" -> StatsPage(app, t, rows, premium, wide, snap) { }
                    else -> SettingsPage(app, t, rows, premium, wide, { pairing = true }) { }
                }
            }
        }
    }
    if (wide) {
        Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Rail(t, tab) { tab = it; pairing = false }
            page(Modifier.weight(1f).fillMaxSize())
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    NavigationBarItem(selected = tab == "timer", onClick = { tab = "timer"; pairing = false }, icon = { Icon(Icons.Filled.Timer, t.timer) }, label = { Text(t.timer) }, colors = presenceNavColors())
                    NavigationBarItem(selected = tab == "projects", onClick = { tab = "projects"; pairing = false }, icon = { Icon(Icons.Filled.Folder, t.projects) }, label = { Text(t.projects) }, colors = presenceNavColors())
                    NavigationBarItem(selected = tab == "todos", onClick = { tab = "todos"; pairing = false }, icon = { Icon(Icons.Filled.Check, t.todos) }, label = { Text(t.todos) }, colors = presenceNavColors())
                    NavigationBarItem(selected = tab == "stats", onClick = { tab = "stats"; pairing = false }, icon = { Icon(Icons.Filled.BarChart, t.stats) }, label = { Text(t.stats) }, colors = presenceNavColors())
                    NavigationBarItem(selected = tab == "settings", onClick = { tab = "settings"; pairing = false }, icon = { Icon(Icons.Filled.Settings, t.settings) }, label = { Text(t.settings) }, colors = presenceNavColors())
                }
            },
        ) { padding ->
            page(Modifier.padding(padding).fillMaxSize())
        }
    }
}

@Composable
private fun Rail(t: T, tab: String, onTab: (String) -> Unit) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        NavigationRailItem(selected = tab == "timer", onClick = { onTab("timer") }, icon = { Icon(Icons.Filled.Timer, t.timer) }, label = { Text(t.timer) }, colors = presenceRailColors())
        NavigationRailItem(selected = tab == "projects", onClick = { onTab("projects") }, icon = { Icon(Icons.Filled.Folder, t.projects) }, label = { Text(t.projects) }, colors = presenceRailColors())
        NavigationRailItem(selected = tab == "todos", onClick = { onTab("todos") }, icon = { Icon(Icons.Filled.Check, t.todos) }, label = { Text(t.todos) }, colors = presenceRailColors())
        NavigationRailItem(selected = tab == "stats", onClick = { onTab("stats") }, icon = { Icon(Icons.Filled.BarChart, t.stats) }, label = { Text(t.stats) }, colors = presenceRailColors())
        NavigationRailItem(selected = tab == "settings", onClick = { onTab("settings") }, icon = { Icon(Icons.Filled.Settings, t.settings) }, label = { Text(t.settings) }, colors = presenceRailColors())
    }
}

@Composable
private fun Mini(t: T, snap: Snap, shown: Long, onOpen: () -> Unit) {
    val app = LocalContext.current.applicationContext as PresenceApp
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(phase(t, snap.kind), style = MaterialTheme.typography.labelLarge)
                Text(snap.projectName.ifBlank { t.unlabeled } + " · " + clockDigits(shown))
            }
            TextButton(onClick = { if (snap.status == "paused") app.center.resume() else app.center.pause() }) {
                Text(if (snap.status == "paused") t.play else t.pause)
            }
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
private fun TimerPage(
    app: PresenceApp,
    t: T,
    rows: List<Rec>,
    premium: Boolean,
    wide: Boolean,
    snap: Snap,
    shown: Long,
    onGate: () -> Unit,
) {
    val context = LocalContext.current
    val projects = rows.alive(KIND_PROJECT).sortedBy { it.f("sort").toIntOrNull() ?: 0 }
    val free = freeProjectIds(rows.alive(KIND_PROJECT))
    var selected by remember { mutableStateOf(snap.projectId) }
    var creating by remember { mutableStateOf(false) }
    var projectMenu by remember { mutableStateOf(false) }
    var taskId by remember { mutableStateOf(snap.taskId) }
    var pending by remember { mutableStateOf<Rec?>(null) }
    val project = projects.find { it.id == selected }
    val tasks = rows.alive(KIND_TASK).filter { it.f("projectId") == project?.id && it.f("done") != "1" }
    val task = tasks.find { it.id == taskId }
    LaunchedEffect(snap.projectId, snap.taskId) {
        if (sessionOpen(snap)) { selected = snap.projectId; taskId = snap.taskId }
    }
    val globals = app.repo.globals()
    val open = sessionOpen(snap)
    val planWork = if (open) snap.workMillis else project?.f("workSec")?.toLongOrNull()?.times(1_000L) ?: globals.workMillis
    val planShorts = if (open) snap.shortCount else project?.f("shortCount")?.toIntOrNull() ?: globals.shortCount
    val planLongs = if (open) snap.longCount else project?.f("longCount")?.toIntOrNull() ?: globals.longCount
    val stretches = stretchCount(planShorts, planLongs)
    val lengths = stretchLengths(planWork, stretches)
    val label = when {
        snap.status == "idle" || snap.status == "awaiting" -> t.focus
        else -> phase(t, snap.kind)
    }
    val face = if (snap.status == "idle") { if (globals.countUp) 0L else lengths.first() } else shown
    val phaseWorked = elapsedWork(snap, System.currentTimeMillis(), SystemClock.elapsedRealtime())
    val loggedId = if (open) snap.projectId else project?.id
    val logged = if (loggedId == null) 0L else workedMillisFor(rows, loggedId)
    val liveWork = if (open && snap.kind in setOf("focus", "track")) phaseWorked else 0L
    val spent = logged + liveWork
    val notificationsOn = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
    val controls: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = project?.f("name").orEmpty().ifEmpty { t.chooseProject },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(t.projects) },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = presenceFieldColors(),
                )
                Box(Modifier.matchParentSize().semantics { contentDescription = t.projects }.clickable { projectMenu = true })
                DropdownMenu(expanded = projectMenu, onDismissRequest = { projectMenu = false }) {
                    projects.forEach { item ->
                        val locked = !premium && item.id !in free
                        DropdownMenuItem(
                            text = { Text(if (locked) item.f("name") + " · " + t.locked else item.f("name")) },
                            onClick = {
                                projectMenu = false
                                if (locked) onGate() else {
                                    if (open && item.id != snap.projectId) pending = item else {
                                        selected = item.id
                                        taskId = null
                                    }
                                }
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(t.newProject) },
                        onClick = {
                            projectMenu = false
                            creating = true
                        },
                    )
                }
            }
            if (tasks.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { item ->
                        PresenceChip(selected = item.id == taskId, onClick = { if (!open) taskId = if (taskId == item.id) null else item.id }, label = { Text(item.f("title")) })
                    }
                }
            }
            val canBegin = project != null
            val playMod = if (wide) Modifier.heightIn(min = 56.dp) else Modifier.weight(1f).heightIn(min = 56.dp)
            val stopMod = if (wide) Modifier.heightIn(min = 48.dp) else Modifier.weight(1f).heightIn(min = 48.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PresenceButton(onClick = {
                    when (snap.status) {
                        "running" -> app.center.pause()
                        "paused" -> app.center.resume()
                        "awaiting" -> app.center.start("focus", project, task)
                        else -> {
                            val chosen = project
                            if (chosen != null) {
                                if (!premium && chosen.id !in free) onGate()
                                else app.center.start("focus", chosen, task)
                            }
                        }
                    }
                }, enabled = open || canBegin, modifier = playMod) {
                    Text(if (snap.status == "running") t.pause else t.play)
                }
                if (open) {
                    TonalButton(onClick = { app.center.stop() }, modifier = stopMod) { Text(t.stop) }
                }
            }
            if (!open && canBegin) {
                TonalButton(onClick = { app.center.start("track", project, task) }, modifier = Modifier.fillMaxWidth()) {
                    Text(t.trackTime)
                }
            }
            if (snap.status == "running" || snap.status == "paused") {
                if (snap.kind != "track") TextButton(onClick = { app.center.skip() }) { Text(t.skip) }
            }
            if (snap.status == "awaiting") Text(t.waiting)
            if (!canBegin && !open) TonalButton(onClick = { creating = true }, modifier = Modifier.fillMaxWidth()) { Text(t.newProject) }
            if (!notificationsOn) Text(t.notificationsOff, style = MaterialTheme.typography.bodyMedium)
        }
    }
    val ring = @Composable {
        Box(Modifier.size(280.dp), contentAlignment = Alignment.Center) {
            val color = MaterialTheme.colorScheme.primary
            val track = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
            val dot = MaterialTheme.colorScheme.onSurface
            Canvas(Modifier.fillMaxSize().padding(12.dp)) {
                val gapDeg = minOf(16f, 120f / stretches)
                val sweep = (360f - gapDeg * stretches) / stretches
                val stroke = Stroke(22f, cap = StrokeCap.Round)
                val onStretch = snap.kind == "focus" && (snap.status == "running" || snap.status == "paused")
                val doneCount = when {
                    snap.status == "idle" -> 0
                    onStretch -> snap.stretchIndex.coerceIn(0, stretches)
                    else -> snap.stretchIndex.coerceIn(0, stretches)
                }
                val partial = if (onStretch && snap.phaseMillis > 0L) {
                    (phaseWorked.toFloat() / snap.phaseMillis.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                for (index in 0 until stretches) {
                    val start = -90f + index * (sweep + gapDeg)
                    drawArc(track, start, sweep, false, style = stroke)
                    val fill = when {
                        index < doneCount -> sweep
                        index == snap.stretchIndex && onStretch -> sweep * partial
                        else -> 0f
                    }
                    if (fill > 0.5f) drawArc(color, start, fill, false, style = stroke)
                }
                val dotAngle = when {
                    snap.status == "idle" -> -90f
                    onStretch -> -90f + snap.stretchIndex.coerceIn(0, stretches - 1) * (sweep + gapDeg) + sweep * partial
                    doneCount <= 0 -> -90f
                    else -> -90f + (doneCount - 1) * (sweep + gapDeg) + sweep + gapDeg / 2f
                }
                val radius = size.minDimension / 2f - 11f
                val rad = Math.toRadians(dotAngle.toDouble())
                drawCircle(
                    color = dot,
                    radius = 7f,
                    center = Offset(center.x + cos(rad).toFloat() * radius, center.y + sin(rad).toFloat() * radius),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                if (open && snap.kind != "track") Text(t.cycleLine((snap.stretchIndex + 1).coerceAtMost(stretches), stretches), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(clockDigits(face), style = MaterialTheme.typography.displayLarge)
                Text("${t.loggedTime} · ${clockDigits(spent)}", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        if (wide) {
            Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { ring() }
                Column(Modifier.weight(1f)) { controls() }
            }
        } else {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = if (open) 40.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(12.dp))
                ring()
                controls()
            }
        }
        if (open) {
            Text(
                t.pauses(snap.pauseCount, clockDigits(pauseNoteMillis(snap, System.currentTimeMillis()))),
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
            )
        }
    }
    if (pending != null) {
        Confirm(t.switchTitle, t.switchBody, t.save, t.cancel, {
            val next = pending
            selected = next?.id
            taskId = null
            pending = null
            app.center.stop()
            if (next != null) app.center.start("focus", next, null)
        }, { pending = null })
    }
    if (creating) {
        NameDialog(t.newProject, "", t, { creating = false }) { name ->
            creating = false
            selected = addProject(app, projects, name)
            taskId = null
        }
    }
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
    var filter by remember { mutableStateOf<String?>(null) }
    var menuFor by remember { mutableStateOf<String?>(null) }
    var creating by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<Rec?>(null) }
    var pendingPlay by remember { mutableStateOf<Rec?>(null) }
    val visible = projects.filter { filter == null || it.f("tags").split(",").contains(filter) }
    val list: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresenceChip(selected = filter == null, onClick = { filter = null }, label = { Text(t.all) })
                    tags.forEach { tag ->
                        PresenceChip(selected = filter == tag.id, onClick = { filter = if (filter == tag.id) null else tag.id }, label = { Text(tag.f("name")) })
                    }
                }
            }
            if (visible.isEmpty()) {
                Text(t.emptyProjects, style = MaterialTheme.typography.titleLarge)
            }
            visible.forEach { project ->
                val locked = !premium && project.id !in free
                val todayMs = rows.filter { it.kind == KIND_SESSION && it.f("projectId") == project.id }.sumOf { rec ->
                    val start = rec.f("startedAt").toLongOrNull() ?: return@sumOf 0L
                    if (dayKey(start, zone, dayStart) != today) 0L else sessionCountedMillis(rec)
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth().combinedClickable(
                        onClick = { onSelect(project.id) },
                        onLongClick = { menuFor = project.id },
                    ),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(18.dp).clip(CircleShape).background(colorOf(project.f("color"))))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(project.f("name"), style = MaterialTheme.typography.titleMedium)
                            Text(if (locked) t.locked else clockWords(todayMs), style = MaterialTheme.typography.bodyMedium)
                        }
                        TextButton(onClick = {
                            if (locked) onGate() else {
                                val snap = app.center.snap()
                                val mine = sessionOpen(snap) && snap.projectId == project.id
                                if (mine) app.center.stop()
                                else if (sessionOpen(snap)) pendingPlay = project
                                else app.center.start("focus", project, null)
                            }
                        }) {
                            val snap = app.center.snap()
                            Text(if (sessionOpen(snap) && snap.projectId == project.id) t.stop else t.play)
                        }
                        TextButton(onClick = { confirmDelete = project }) { Text(t.delete) }
                        DropdownMenu(expanded = menuFor == project.id, onDismissRequest = { menuFor = null }) {
                            DropdownMenuItem(text = { Text(t.moveUp) }, onClick = { menuFor = null; move(app, projects, project, -1) })
                            DropdownMenuItem(text = { Text(t.moveDown) }, onClick = { menuFor = null; move(app, projects, project, 1) })
                            DropdownMenuItem(text = { Text(t.delete) }, onClick = { menuFor = null; confirmDelete = project })
                        }
                    }
                }
            }
            PresenceButton(onClick = {
                if (!premium && projects.size >= FREE_PROJECTS) onGate() else creating = true
            }, modifier = Modifier.heightIn(min = 52.dp)) { Text(t.newProject) }
        }
    }
    val detail: @Composable (Modifier) -> Unit = { modifier ->
        val project = projects.find { it.id == selectedId }
        if (project == null) {
            Box(modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text(t.chooseProject, style = MaterialTheme.typography.titleLarge) }
        } else {
            ProjectDetail(app, t, rows, project, premium, modifier, onGate)
        }
    }
    if (wide) {
        Row(Modifier.fillMaxSize()) {
            list(Modifier.weight(0.42f).fillMaxSize())
            detail(Modifier.weight(0.58f).fillMaxSize())
        }
    } else if (selectedId == null) {
        list(Modifier.fillMaxSize())
    } else {
        Column(Modifier.fillMaxSize()) {
            TextButton(onClick = { onSelect(null) }) { Text(t.back) }
            detail(Modifier.weight(1f))
        }
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
    var taskFilter by remember { mutableStateOf<String?>(null) }
    var newTask by remember { mutableStateOf(false) }
    var goalOpen by remember { mutableStateOf(false) }
    var deleteTask by remember { mutableStateOf<Rec?>(null) }
    var editTask by remember { mutableStateOf<Rec?>(null) }
    var newGroup by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf(false) }
    var editTag by remember { mutableStateOf<Rec?>(null) }
    var name by remember(project.id) { mutableStateOf(project.f("name")) }
    val zone = ZoneId.systemDefault()
    val globals = app.repo.globals()
    val worked = workedMillisFor(rows, project.id)
    val shownTasks = tasks.filter { taskFilter == null || it.f("tags").split(",").contains(taskFilter) }
    Column(modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            name,
            { value ->
                name = value
                val trimmed = value.trim()
                if (trimmed.isNotEmpty() && trimmed != project.f("name")) {
                    val body = project.body.toMutableMap()
                    body["name"] = trimmed
                    app.repo.put(project.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = body))
                }
            },
            label = { Text(t.name) },
            modifier = Modifier.fillMaxWidth(),
            colors = presenceFieldColors(),
        )
        val groups = rows.alive(KIND_GROUP)
        MenuField(t.groups, t.groups, groups.find { it.id == project.f("groupId") }?.f("name") ?: t.noGroup,
            listOf("" to t.noGroup) + groups.map { it.id to it.f("name") }) { groupId ->
            app.repo.put(project.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = project.body + ("groupId" to groupId)))
        }
        TextButton(onClick = { newGroup = true }) { Text(t.newGroup) }
        Text(t.goal, style = MaterialTheme.typography.titleLarge)
        val goalMinutes = project.f("goalMinutes").toIntOrNull()
        val deadline = project.f("goalDeadline").toLongOrNull()
        if (goalMinutes != null && deadline != null) {
            val daily = dailyTargetMinutes(goalMinutes, worked, deadline, System.currentTimeMillis(), zone, globals.dayStart)
            val today = dayKey(System.currentTimeMillis(), zone, globals.dayStart)
            val todayMs = rows.filter { it.kind == KIND_SESSION && it.f("projectId") == project.id }.sumOf { rec ->
                val start = rec.f("startedAt").toLongOrNull() ?: return@sumOf 0L
                if (dayKey(start, zone, globals.dayStart) != today) 0L else sessionCountedMillis(rec)
            }
            Text(t.daily + " · " + clockWords(daily * 60_000L))
            Text(t.today + " · " + clockWords(todayMs))
            Text(t.remainder + " · " + clockWords(((goalMinutes * 60_000L) - worked).coerceAtLeast(0)))
            Text(t.deadline + " · " + dayKey(deadline, zone, globals.dayStart))
            TextButton(onClick = {
                if (!premium) onGate() else {
                    val body = project.body.toMutableMap()
                    body["goalMinutes"] = ""
                    body["goalDeadline"] = ""
                    app.repo.put(project.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = body))
                }
            }) { Text(t.clearGoal) }
        } else {
            Text(t.noGoal)
            PresenceButton(onClick = { if (premium) goalOpen = true else onGate() }) { Text(t.setGoal) }
        }
        Text(t.ownTimer, style = MaterialTheme.typography.titleLarge)
        if (!premium) {
            TextButton(onClick = onGate) { Text(t.locked) }
        } else {
            ProjectPlanFields(t, project, globals) { key, value ->
                val latest = app.repo.snapshot().find { it.kind == KIND_PROJECT && it.id == project.id } ?: project
                val body = latest.body.toMutableMap()
                body[key] = value
                app.repo.put(latest.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = body))
                app.center.refreshSettings()
            }
        }
        Text(t.tags, style = MaterialTheme.typography.titleLarge)
        tags.forEach { tag ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tag.f("name"), modifier = Modifier.weight(1f))
                TextButton(onClick = { if (premium) editTag = tag else onGate() }) { Text(t.rename) }
                TextButton(onClick = {
                    if (!premium) onGate() else app.repo.put(tag.copy(deleted = true, updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId()))
                }) { Text(t.delete) }
            }
        }
        PresenceButton(onClick = { if (premium) newTag = true else onGate() }) { Text(t.newTag) }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                val on = project.f("tags").split(",").contains(tag.id)
                PresenceChip(selected = on, onClick = {
                    if (!premium) onGate() else {
                        val set = project.f("tags").split(",").filter { it.isNotBlank() }.toMutableSet()
                        if (on) set.remove(tag.id) else set.add(tag.id)
                        val body = project.body.toMutableMap()
                        body["tags"] = set.joinToString(",")
                        app.repo.put(project.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = body))
                    }
                }, label = { Text(tag.f("name")) })
            }
        }
        Text(t.tasks, style = MaterialTheme.typography.titleLarge)
        if (tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PresenceChip(selected = taskFilter == null, onClick = { taskFilter = null }, label = { Text(t.all) })
                tags.forEach { tag ->
                    PresenceChip(selected = taskFilter == tag.id, onClick = { taskFilter = if (taskFilter == tag.id) null else tag.id }, label = { Text(tag.f("name")) })
                }
            }
        }
        if (shownTasks.isEmpty()) Text(t.emptyTasks)
        shownTasks.forEachIndexed { index, task ->
            val locked = !premium && index >= FREE_TASKS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = task.f("done") == "1", onCheckedChange = {
                    if (locked) onGate() else {
                        val body = task.body.toMutableMap()
                        body["done"] = if (it) "1" else "0"
                        app.repo.put(task.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = body))
                    }
                })
                Text(task.f("title") + if (locked) " · " + t.locked else "", modifier = Modifier.weight(1f).clickable { editTask = task })
                TextButton(onClick = { deleteTask = task }) { Text(t.delete) }
            }
        }
        PresenceButton(onClick = { if (!premium && tasks.size >= FREE_TASKS) onGate() else newTask = true }) { Text(t.newTask) }
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
    if (newGroup) {
        NameDialog(t.newGroup, "", t, { newGroup = false }) { title ->
            app.repo.put(app.repo.writerNow(KIND_GROUP, UUID.randomUUID().toString(), mapOf("name" to title)))
            newGroup = false
        }
    }
    editTask?.let { item ->
        var title by remember(item.id) { mutableStateOf(item.f("title")) }
        var picked by remember(item.id) { mutableStateOf(item.f("tags").split(",").filter { it.isNotBlank() }.toSet()) }
        AlertDialog(
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
                        "tags" to "",
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
    val lengths = stretchLengths(workSec.coerceAtLeast(1L) * 1_000L, stretchCount(shortCount, longCount))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TonalButton(onClick = {
            onWork(100 * 60L)
            onShortCount(3)
            onLongCount(0)
            onShort(5 * 60L)
            onLong(15 * 60L)
        }, modifier = Modifier.fillMaxWidth()) { Text(t.pomodoroPreset) }
        FieldSection(t.workTime, t.workHelp) {
            HmsFields(t, workSec, onWork)
            Text(t.stretchSummary(stretchCount(shortCount, longCount), clockDigits(lengths.first())), color = MaterialTheme.colorScheme.onSurface)
        }
        FieldSection(t.shortBreaks, t.shortCountHelp) {
            CountStepper(t, t.shortBreaks, shortCount, 0, 12, onShortCount)
            HmsFields(t, shortSec, onShort)
        }
        FieldSection(t.longBreaks, t.longCountHelp) {
            CountStepper(t, t.longBreaks, longCount, 0, 12, onLongCount)
            HmsFields(t, longSec, onLong)
        }
    }
}

@Composable
private fun FieldSection(title: String, help: String, content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Explain(help)
            }
            content()
        }
    }
}

@Composable
private fun HmsFields(t: T, totalSec: Long, onSec: (Long) -> Unit) {
    val parts = splitSeconds(totalSec)
    var hours by remember(totalSec) { mutableStateOf(parts.first.toString()) }
    var minutes by remember(totalSec) { mutableStateOf(parts.second.toString()) }
    var seconds by remember(totalSec) { mutableStateOf(parts.third.toString()) }
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
    OutlinedTextField(
        value = value,
        onValueChange = { raw -> onValue(raw.filter { it.isDigit() }.take(2)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        colors = presenceFieldColors(),
    )
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
    var range by remember { mutableIntStateOf(0) }
    var weekShift by remember { mutableIntStateOf(0) }
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDay by remember { mutableStateOf(today) }
    var pickedBar by remember { mutableIntStateOf(-1) }
    var pickedSlice by remember { mutableIntStateOf(-1) }
    var manual by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    val drag = remember { mutableFloatStateOf(0f) }
    val fromTo = when (range) {
        1 -> today.minusDays((6 + weekShift * 7).toLong()) to today.minusDays((weekShift * 7).toLong())
        2 -> month.atDay(1) to month.atEndOfMonth()
        3 -> LocalDate.of(today.year, 1, 1) to LocalDate.of(today.year, 12, 31)
        else -> today to today
    }
    val slices = slicesFor(rows, fromTo.first, fromTo.second, zone, dayStart)
    val total = slices.sumOf { it.millis }
    val weekStart = today.minusDays((6 + weekShift * 7).toLong())
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }
    val bars = dayTotals(rows, weekDays, zone, dayStart)
    val percents = wholePercents(slices.map { it.millis })
    fun share() {
        val text = shareSummary(t.stats, listOf(t.today, t.week, t.month, t.year)[range], slices.map { it.name.ifBlank { t.unlabeled } to it.millis }) { name ->
            val project = rows.alive(KIND_PROJECT).find { it.f("name") == name }
            rows.alive(KIND_GROUP).find { it.id == project?.f("groupId") }?.f("name").orEmpty()
        }
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), t.share))
    }
    val charts: @Composable () -> Unit = {
        val bar = MaterialTheme.colorScheme.primary
        val hot = MaterialTheme.colorScheme.tertiary
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (pickedBar in bars.indices) weekDays[pickedBar].toString() + " · " + clockWords(bars[pickedBar]) else t.week, style = MaterialTheme.typography.titleMedium)
            Canvas(
                Modifier.fillMaxWidth().height(160.dp)
                    .pointerInput(weekShift) {
                        detectHorizontalDragGestures { _, dx ->
                            drag.floatValue += dx
                            if (drag.floatValue > 80f) {
                                weekShift += 1
                                drag.floatValue = 0f
                            }
                            if (drag.floatValue < -80f && weekShift > 0) {
                                weekShift -= 1
                                drag.floatValue = 0f
                            }
                        }
                    }
                    .pointerInput(bars) {
                        detectTapGestures { offset ->
                            pickedBar = ((offset.x / (size.width / 7f)).toInt()).coerceIn(0, 6)
                        }
                    },
            ) {
                val max = bars.maxOrNull()?.coerceAtLeast(1L) ?: 1L
                val gap = 12f
                val w = (size.width - gap * 6) / 7f
                bars.forEachIndexed { index, value ->
                    val h = (value.toFloat() / max.toFloat()) * size.height
                    drawRoundRect(
                        color = if (index == pickedBar) hot else bar,
                        topLeft = Offset(index * (w + gap), size.height - h),
                        size = Size(w, h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                    )
                }
            }
            Row {
                TextButton(onClick = { weekShift += 1 }) { Text(t.back) }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { if (weekShift > 0) weekShift -= 1 }, enabled = weekShift > 0) { Text(t.today) }
            }
        }
    }
    val pie: @Composable () -> Unit = {
        val darkChart = MaterialTheme.colorScheme.background.isDark()
        val sliceColors = slices.map { slice ->
            chartColor(rows.alive(KIND_PROJECT).find { it.id == slice.id }?.f("color") ?: "C8922A", darkChart)
        }
        val center = if (pickedSlice in slices.indices) {
            slices[pickedSlice].name.ifBlank { t.unlabeled } + "\n" + clockWords(slices[pickedSlice].millis) + " · " + percents.getOrElse(pickedSlice) { 0 } + "%"
        } else if (slices.isEmpty()) {
            t.emptyRange
        } else {
            clockWords(total)
        }
        Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
            Canvas(
                Modifier.size(200.dp)
                    .pointerInput(slices, total) {
                        detectTapGestures { offset ->
                            if (total <= 0) return@detectTapGestures
                            val angle = (Math.toDegrees(atan2((offset.y - size.height / 2f).toDouble(), (offset.x - size.width / 2f).toDouble())) + 450.0) % 360.0
                            var cursor = 0.0
                            slices.forEachIndexed { index, slice ->
                                cursor += slice.millis * 360.0 / total
                                if (angle <= cursor) {
                                    pickedSlice = index
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
                    .pointerInput(range) {
                        detectHorizontalDragGestures { _, dx ->
                            drag.floatValue += dx
                            if (drag.floatValue > 80f || drag.floatValue < -80f) {
                                val step = if (drag.floatValue > 0) -1 else 1
                                range = (range + step).mod(4)
                                drag.floatValue = 0f
                            }
                        }
                    },
            ) {
                if (total > 0) {
                    var start = -90f
                    slices.forEachIndexed { index, slice ->
                        val sweep = slice.millis.toFloat() / total * 360f
                        drawArc(sliceColors[index], start, sweep, true)
                        start += sweep
                    }
                }
            }
            Text(center, textAlign = TextAlign.Center, modifier = Modifier.padding(48.dp))
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(t.today, t.week, t.month, t.year).forEachIndexed { index, label ->
                PresenceChip(selected = range == index, onClick = {
                    if (index > 0 && !premium) onGate() else range = index
                }, label = { Text(label) })
            }
        }
        Text(clockWords(total), style = MaterialTheme.typography.displayLarge)
        if (slices.isEmpty()) Text(t.emptyRange) else slices.forEach { slice ->
            Text((slice.name.ifBlank { t.unlabeled }) + " · " + clockWords(slice.millis))
        }
        if (!premium) {
            Surface(color = MaterialTheme.colorScheme.surfaceContainer, contentColor = MaterialTheme.colorScheme.onSurface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t.charts, style = MaterialTheme.typography.titleMedium)
                    PresenceButton(onClick = onGate) { Text(t.openSwitch) }
                }
            }
        } else if (wide) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) { charts() }
                Column(Modifier.weight(1f)) { pie() }
            }
            CalendarBlock(t, rows, month, selectedDay, zone, dayStart, { month = it }, { selectedDay = it })
        } else {
            charts()
            pie()
            CalendarBlock(t, rows, month, selectedDay, zone, dayStart, { month = it }, { selectedDay = it })
        }
        if (premium) {
            val logicalToday = today
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PresenceButton(onClick = { share() }, modifier = Modifier.heightIn(min = 52.dp)) { Text(t.share) }
                    PresenceButton(onClick = {
                        if (selectedDay > logicalToday) note = t.future else manual = true
                    }, modifier = Modifier.heightIn(min = 52.dp)) { Text(t.manual) }
                }
            } else {
                PresenceButton(onClick = { share() }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(t.share) }
                PresenceButton(onClick = {
                    if (selectedDay > logicalToday) note = t.future else manual = true
                }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(t.manual) }
            }
        }
        if (note.isNotEmpty()) Text(note)
        rows.alive(KIND_SESSION).filter {
            val start = it.f("startedAt").toLongOrNull() ?: return@filter false
            dayKey(start, zone, dayStart) == selectedDay && it.f("endedAt").isNotEmpty()
        }.take(12).forEach { session ->
            Text(session.f("projectName").ifBlank { t.unlabeled } + " · " + phase(t, session.f("kind")) + " · " + clockWords(sessionCountedMillis(session)))
        }
    }
    if (manual) ManualDialog(t, rows, selectedDay, dayStart, snap, { manual = false }) { message ->
        note = message
        if (message.isEmpty()) manual = false
    }
}

@Composable
private fun CalendarBlock(
    t: T,
    rows: List<Rec>,
    month: YearMonth,
    selected: LocalDate,
    zone: ZoneId,
    dayStart: Int,
    onMonth: (YearMonth) -> Unit,
    onDay: (LocalDate) -> Unit,
) {
    var drag by remember { mutableFloatStateOf(0f) }
    val first = month.atDay(1)
    val pad = (first.dayOfWeek.value + 6) % 7
    Column(Modifier.pointerInput(month) {
        detectHorizontalDragGestures { _, dx ->
            drag += dx
            if (drag > 80f) {
                onMonth(month.minusMonths(1))
                drag = 0f
            }
            if (drag < -80f) {
                onMonth(month.plusMonths(1))
                drag = 0f
            }
        }
    }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onMonth(month.minusMonths(1)) }) { Text(t.back) }
            Text(month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + month.year, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { onMonth(month.plusMonths(1)) }) { Text("→") }
        }
        val marked = rows.alive(KIND_SESSION).mapNotNull {
            val start = it.f("startedAt").toLongOrNull() ?: return@mapNotNull null
            if (sessionCountedMillis(it) <= 0) null else dayKey(start, zone, dayStart)
        }.toSet()
        Column {
            for (week in 0 until 6) {
                Row(Modifier.fillMaxWidth()) {
                    for (dow in 0 until 7) {
                        val index = week * 7 + dow - pad
                        val date = if (index in 0 until month.lengthOfMonth()) month.atDay(index + 1) else null
                        Box(
                            Modifier.weight(1f).height(44.dp).clickable(enabled = date != null) { date?.let(onDay) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (date != null) {
                                Text(
                                    date.dayOfMonth.toString(),
                                    color = when {
                                        date == selected -> MaterialTheme.colorScheme.primary
                                        date in marked -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                )
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
    var startHour by remember { mutableIntStateOf(dayStart) }
    var startMinute by remember { mutableIntStateOf(0) }
    var project by remember { mutableStateOf(projects.firstOrNull()?.id.orEmpty()) }
    var hours by remember { mutableIntStateOf(0) }
    var mins by remember { mutableIntStateOf(25) }
    var kind by remember { mutableStateOf("focus") }
    var error by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(t.manual) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                projects.forEach { item ->
                    PresenceChip(selected = project == item.id, onClick = { project = item.id }, label = { Text(item.f("name")) })
                }
                TextButton(onClick = {
                    TimePickerDialog(context, { _, h, m -> startHour = h; startMinute = m }, startHour, startMinute, true).show()
                }) { Text("${t.sessionStart}: ${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')}") }
                CountStepper(t, t.hours, hours, 0, 12) { hours = it }
                CountStepper(t, t.minutes, mins, 0, 59) { mins = it }
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
                val calendarDay = if (startHour < dayStart) day.plusDays(1) else day
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
    var draft by remember { mutableStateOf("") }
    val items = rows.filter { it.kind == KIND_LOCAL && it.id.startsWith("todo:") && !it.deleted }
        .sortedBy { it.f("sort").toLongOrNull() ?: it.updatedAt }
    val editor: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(t.todos, style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(colors = presenceFieldColors(), 
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(t.todoHint) },
                singleLine = true,
            )
            PresenceButton(
                onClick = {
                    app.repo.addTodo(draft)
                    draft = ""
                    HomeWidgets.update(context)
                },
                enabled = draft.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text(t.addItem) }
        }
    }
    val list: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (items.isEmpty()) {
                Text(t.emptyTodos, color = MaterialTheme.colorScheme.onSurface)
            }
            items.forEach { item ->
                val id = item.id.removePrefix("todo:")
                val done = item.f("done") == "1"
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = done, onCheckedChange = {
                        app.repo.setTodoDone(id, it)
                        HomeWidgets.update(context)
                    })
                    Text(
                        item.f("title"),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    TextButton(onClick = {
                        app.repo.deleteTodo(id)
                        HomeWidgets.update(context)
                    }) {
                        Icon(Icons.Filled.Delete, t.delete)
                    }
                }
            }
        }
    }
    if (wide) {
        Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) { editor() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { list() }
        }
    } else {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            editor()
            list()
        }
    }
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
                modifier = Modifier.clickable {
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
    var note by remember { mutableStateOf("") }
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
    val storagePane: @Composable () -> Unit = {
        Text(t.sdCard, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
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
    }
    val timerPane: @Composable () -> Unit = {
        Text(t.timerSettings, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
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
        Text(t.reminders, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
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
        Setting(t.resetSettings, t.resetHelp) {
            PresenceButton(onClick = {
                app.repo.resetSettings()
                app.center.refreshSettings()
                Reminders.reschedule(context)
                note = t.resetDone
            }, modifier = Modifier.heightIn(min = 48.dp)) { Text(t.resetSettings) }
        }
    }
    val otherPane: @Composable () -> Unit = {
        val themeLabel = when (theme) {
            "light" -> t.light
            "dark" -> t.dark
            else -> t.followSystem
        }
        MenuField(t.appearance, t.themeMenuHelp, themeLabel, listOf("light" to t.light, "dark" to t.dark, "system" to t.followSystem)) {
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
        Text(t.sounds, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
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
        Stepper(t, t.volume, t.volumeHelp, globals.volume, 0, 100) { app.repo.setPref("bg_volume", it.toString()); app.center.catchUp() }
        Text(t.data, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
        Setting(t.exportJson, t.exportJsonHelp) {
            PresenceButton(onClick = { exportJson.launch("presence-backup.json") }) { Text(t.exportJson) }
        }
        Setting(t.exportCsv, t.exportCsvHelp) {
            PresenceButton(onClick = { exportCsv.launch("presence-sessions.csv") }) { Text(t.exportCsv) }
        }
        Setting(t.importJson, t.importHelp) {
            PresenceButton(onClick = { importJson.launch(arrayOf("application/json", "text/*", "*/*")) }) { Text(t.importJson) }
        }
        if (note.isNotEmpty()) Text(note, color = MaterialTheme.colorScheme.onSurface)
        Text(t.devices, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
        Setting(t.pair, t.pairHelp) {
            PresenceButton(onClick = onPair, modifier = Modifier.heightIn(min = 52.dp)) { Text(t.pair) }
        }
        Text(t.about, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
        Text(t.aboutBody, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
    }
    if (wide) {
        Row(Modifier.fillMaxSize()) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                storagePane()
                timerPane()
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) { otherPane() }
        }
    } else {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            storagePane()
            timerPane()
            otherPane()
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
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                Icons.Filled.Visibility,
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
    var open by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
            Explain(help)
        }
        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = presenceFieldColors(),
            )
            Box(Modifier.matchParentSize().semantics { contentDescription = title }.clickable { open = true })
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                options.forEach { (id, label) ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { open = false; onPick(id) })
                }
            }
        }
    }
}

@Composable
private fun Setting(title: String, help: String, control: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
        Explain(help)
        control()
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
    var digits by remember { mutableStateOf("") }
    var now by remember { mutableIntStateOf(0) }
    var afterGrant by remember { mutableStateOf<(() -> Unit)?>(null) }
    val nearbyPermission = Manifest.permission.NEARBY_WIFI_DEVICES
    val askNearby = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val next = afterGrant
        afterGrant = null
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
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (now < 0) return@Column
        PresenceButton(onClick = { withLink { app.lan.showCode() } }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(t.showCode) }
        if (offer != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(offer!!, style = MaterialTheme.typography.displayLarge, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Explain(t.offerHelp)
            }
            Text(t.expiresIn(clockDigits(leftMs)), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            TextButton(onClick = { app.lan.hideCode() }) { Text(t.cancel) }
        } else if (message == "expired") {
            Text(t.expired)
            PresenceButton(onClick = { withLink { app.lan.showCode() } }) { Text(t.newCode) }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(digits, { if (it.length <= 4 && it.all { ch -> ch in '0'..'9' }) digits = it }, label = { Text(t.enterCode) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.weight(1f), colors = presenceFieldColors())
            Explain(t.enterHelp)
        }
        PresenceButton(onClick = { withLink { app.lan.enter(digits) } }, enabled = digits.length == 4 && message != "syncing", modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(t.pairAction) }
        if (message.isNotEmpty() && message != "expired") Text(t.pairMessage(message))
        Text(t.devices, style = MaterialTheme.typography.titleLarge)
        if (peers.isEmpty()) Text(t.noPeers)
        peers.forEach { peer ->
            val id = peer.id.removePrefix("peer:")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(peer.f("name").ifBlank { "Presence" })
                    val last = peer.f("lastSync").toLongOrNull() ?: 0L
                    if (last > 0) Text(clockDigits(System.currentTimeMillis() - last))
                }
                TextButton(onClick = { withLink { app.lan.syncNow(id) } }) { Text(t.syncNow) }
                TextButton(onClick = { app.repo.forgetPeer(id) }) { Text(t.forget) }
            }
        }
    }
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
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = presenceButtonColors(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        content = content,
    )
}

@Composable
private fun TonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        colors = presenceTonalColors(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        content = content,
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun PresenceChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
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
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onOk) { Text(ok) } },
        dismissButton = { TextButton(onClick = onCancel) { Text(cancel) } },
    )
}

@Composable
private fun NameDialog(title: String, initial: String, t: T, onClose: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(name, { name = it; error = false }, label = { Text(t.name) }, colors = presenceFieldColors())
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
    var hours by remember { mutableIntStateOf(10) }
    var mins by remember { mutableIntStateOf(0) }
    var deadline by remember { mutableStateOf(LocalDate.now().plusMonths(1)) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(t.setGoal) },
        text = {
            Column {
                CountStepper(t, t.hours, hours, 0, 10000) { hours = it }
                CountStepper(t, t.minutes, mins, 0, 59) { mins = it }
                TextButton(onClick = {
                    DatePickerDialog(context, { _, y, m, d -> deadline = LocalDate.of(y, m + 1, d) }, deadline.year, deadline.monthValue - 1, deadline.dayOfMonth).show()
                }) { Text(t.deadline + " · " + deadline) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val total = hours * 60 + mins
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
