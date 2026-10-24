@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package app.canto.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.canto.CantoApp
import app.canto.core.FREE_PROJECTS
import app.canto.core.FREE_TASKS
import app.canto.core.GROUP_PERSONAL
import app.canto.core.GROUP_STUDY
import app.canto.core.GROUP_WORK
import app.canto.core.KIND_GROUP
import app.canto.core.KIND_LOCAL
import app.canto.core.KIND_PROJECT
import app.canto.core.KIND_SESSION
import app.canto.core.KIND_TAG
import app.canto.core.KIND_TASK
import app.canto.core.Rec
import app.canto.core.Snap
import app.canto.core.alive
import app.canto.core.clockDigits
import app.canto.core.clockWords
import app.canto.core.dailyTargetMinutes
import app.canto.core.dayKey
import app.canto.core.dayTotals
import app.canto.core.decodeBackup
import app.canto.core.displayMillis
import app.canto.core.encodeBackup
import app.canto.core.f
import app.canto.core.freeProjectIds
import app.canto.core.manualSession
import app.canto.core.overlapsOpen
import app.canto.core.sessionsCsv
import app.canto.core.SYNC_KINDS
import app.canto.core.sessionCountedMillis
import app.canto.core.shareSummary
import app.canto.core.slicesFor
import app.canto.core.snapFrom
import app.canto.core.wholePercents
import app.canto.core.workedMillisFor
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun CantoRoot() {
    val context = LocalContext.current
    val app = context.applicationContext as CantoApp
    var ready by remember { mutableStateOf(app.ready.isCompleted) }
    LaunchedEffect(Unit) {
        app.ready.await()
        ready = true
    }
    if (!ready) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Text("Canto", style = MaterialTheme.typography.headlineMedium)
        }
        return
    }
    val rows by app.repo.observe().collectAsState(initial = emptyList())
    val flags = rows.firstOrNull { it.kind == KIND_LOCAL && it.id == "flags" }?.body.orEmpty()
    val t = remember(flags["language"]) { T(resolveLang(flags["language"] ?: "system")) }
    val premium = flags["premium"] == "1"
    val dark = when (flags["theme"]) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val scale = when (flags["text"]) {
        "compact" -> 0.9f
        "large" -> 1.16f
        else -> 1f
    }
    val density = LocalDensity.current
    val wide = calculateWindowSizeClass(context as android.app.Activity).widthSizeClass != WindowWidthSizeClass.Compact
    androidx.compose.runtime.CompositionLocalProvider(
        LocalDensity provides Density(density.density * scale, density.fontScale * scale),
    ) {
        CantoTheme(dark) {
            Shell(app, rows, t, premium, wide)
        }
    }
}

@Composable
private fun Shell(app: CantoApp, rows: List<Rec>, t: T, premium: Boolean, wide: Boolean) {
    val context = LocalContext.current
    var tab by remember { mutableStateOf("timer") }
    var projectId by remember { mutableStateOf<String?>(null) }
    var pairing by remember { mutableStateOf(false) }
    var gate by remember { mutableStateOf(false) }
    var tick by remember { mutableIntStateOf(0) }
    val snap = rows.firstOrNull { it.kind == KIND_LOCAL && it.id == "snapshot" && !it.deleted }?.let { snapFrom(it.body) } ?: Snap()
    LaunchedEffect(snap.status, snap.completionToken) {
        while (snap.status == "running") {
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
                    tab == "timer" -> TimerPage(app, t, rows, premium, wide, snap, shown) { gate = true }
                    tab == "projects" -> ProjectsPage(app, t, rows, premium, wide, projectId, { projectId = it }) { gate = true }
                    tab == "stats" -> StatsPage(app, t, rows, premium, wide, snap) { gate = true }
                    else -> SettingsPage(app, t, rows, premium, wide, { pairing = true }) { gate = true }
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
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = tab == "timer", onClick = { tab = "timer"; pairing = false }, icon = { Icon(Icons.Filled.Timer, t.timer) }, label = { Text(t.timer) })
                    NavigationBarItem(selected = tab == "projects", onClick = { tab = "projects"; pairing = false }, icon = { Icon(Icons.Filled.Folder, t.projects) }, label = { Text(t.projects) })
                    NavigationBarItem(selected = tab == "stats", onClick = { tab = "stats"; pairing = false }, icon = { Icon(Icons.Filled.BarChart, t.stats) }, label = { Text(t.stats) })
                    NavigationBarItem(selected = tab == "settings", onClick = { tab = "settings"; pairing = false }, icon = { Icon(Icons.Filled.Settings, t.settings) }, label = { Text(t.settings) })
                }
            },
        ) { padding ->
            page(Modifier.padding(padding).fillMaxSize())
        }
    }
    if (gate) {
        AlertDialog(
            onDismissRequest = { gate = false },
            title = { Text(t.locked) },
            text = { Text(t.premiumIntro) },
            confirmButton = {
                TextButton(onClick = { gate = false; tab = "settings"; pairing = false }) { Text(t.openSwitch) }
            },
            dismissButton = { TextButton(onClick = { gate = false }) { Text(t.cancel) } },
        )
    }
}

@Composable
private fun Rail(t: T, tab: String, onTab: (String) -> Unit) {
    NavigationRail {
        NavigationRailItem(selected = tab == "timer", onClick = { onTab("timer") }, icon = { Icon(Icons.Filled.Timer, t.timer) }, label = { Text(t.timer) })
        NavigationRailItem(selected = tab == "projects", onClick = { onTab("projects") }, icon = { Icon(Icons.Filled.Folder, t.projects) }, label = { Text(t.projects) })
        NavigationRailItem(selected = tab == "stats", onClick = { onTab("stats") }, icon = { Icon(Icons.Filled.BarChart, t.stats) }, label = { Text(t.stats) })
        NavigationRailItem(selected = tab == "settings", onClick = { onTab("settings") }, icon = { Icon(Icons.Filled.Settings, t.settings) }, label = { Text(t.settings) })
    }
}

@Composable
private fun Mini(t: T, snap: Snap, shown: Long, onOpen: () -> Unit) {
    val app = LocalContext.current.applicationContext as CantoApp
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(phase(t, snap.kind), style = MaterialTheme.typography.labelLarge)
                Text(snap.projectName.ifBlank { t.unlabeled } + " · " + clockDigits(shown))
            }
            TextButton(onClick = { if (snap.status == "paused") app.center.resume() else app.center.pause() }) {
                Text(if (snap.status == "paused") t.resume else t.pause)
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
    app: CantoApp,
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
    var taskId by remember { mutableStateOf(snap.taskId) }
    var pending by remember { mutableStateOf<Rec?>(null) }
    val project = projects.find { it.id == selected }
    val tasks = rows.alive(KIND_TASK).filter { it.f("projectId") == project?.id && it.f("done") != "1" }
    val task = tasks.find { it.id == taskId }
    val length = if (snap.phaseMillis <= 0L) 1f else (shown.toFloat() / snap.phaseMillis).coerceIn(0f, 1f)
    val progress = when {
        snap.kind == "track" -> 1f
        snap.countUp -> length
        snap.status == "idle" || snap.status == "awaiting" -> 1f
        else -> length
    }
    val label = when (snap.status) {
        "awaiting" -> phase(t, snap.awaitingKind.ifEmpty { "focus" })
        "idle" -> t.focus
        else -> phase(t, snap.kind)
    }
    val primary = when (snap.status) {
        "running" -> t.pause
        "paused" -> t.resume
        "awaiting" -> t.startNext
        else -> t.startFocus
    }
    val notificationsOn = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
    val controls: @Composable () -> Unit = {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                projects.forEach { item ->
                    val locked = !premium && item.id !in free
                    FilterChip(
                        selected = item.id == selected,
                        onClick = {
                            if (locked) onGate() else {
                                selected = item.id
                                taskId = null
                            }
                        },
                        label = { Text(if (locked) item.f("name") + " · " + t.locked else item.f("name")) },
                    )
                }
            }
            if (tasks.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { item ->
                        FilterChip(selected = item.id == taskId, onClick = { taskId = if (taskId == item.id) null else item.id }, label = { Text(item.f("title")) })
                    }
                }
            }
            Button(onClick = {
                when (snap.status) {
                    "running" -> app.center.pause()
                    "paused" -> app.center.resume()
                    "awaiting" -> app.center.start(snap.awaitingKind.ifEmpty { "focus" }, project, task)
                    else -> {
                        if (project != null && !premium && project.id !in free) onGate()
                        else app.center.start("focus", project, task)
                    }
                }
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(primary) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (snap.status == "idle") {
                    FilledTonalButton(onClick = {
                        if (project != null && !premium && project.id !in free) onGate() else app.center.start("track", project, task)
                    }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text(t.trackTime) }
                }
                if (snap.status == "running" || snap.status == "paused") {
                    FilledTonalButton(onClick = { app.center.stop() }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text(t.stop) }
                    if (snap.kind != "track") {
                        FilledTonalButton(onClick = { app.center.skip() }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text(t.skip) }
                    }
                }
            }
            if (snap.status == "running" || snap.status == "paused" || snap.status == "awaiting") {
                Text(t.cycleLine(snap.focusDone, snap.cycles), style = MaterialTheme.typography.bodyMedium)
            }
            if (snap.status == "awaiting") Text(t.waiting)
            if (!notificationsOn) Text(t.notificationsOff, style = MaterialTheme.typography.bodyMedium)
        }
    }
    val ring = @Composable {
        Box(Modifier.size(280.dp), contentAlignment = Alignment.Center) {
            val color = MaterialTheme.colorScheme.primary
            Canvas(Modifier.fillMaxSize().padding(12.dp)) {
                drawArc(color.copy(alpha = 0.18f), 0f, 360f, false, style = Stroke(22f, cap = StrokeCap.Round))
                drawArc(color, -90f, 360f * progress, false, style = Stroke(22f, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(clockDigits(if (snap.status == "idle") (app.repo.globals().focus * 60_000L) else shown), style = MaterialTheme.typography.displayLarge)
            }
        }
    }
    if (wide) {
        Row(Modifier.fillMaxSize().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { ring() }
            Column(Modifier.weight(1f)) { controls() }
        }
    } else {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(12.dp))
            ring()
            controls()
        }
    }
    if (pending != null) {
        Confirm(t.switchTitle, t.switchBody, t.save, t.cancel, {
            val next = pending
            pending = null
            app.center.stop()
            if (next != null) app.center.start("track", next, null)
        }, { pending = null })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectsPage(
    app: CantoApp,
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
    val groups = rows.alive(KIND_GROUP).sortedBy { it.f("sort").toIntOrNull() ?: 0 }
    val tags = rows.alive(KIND_TAG)
    val free = freeProjectIds(projects)
    var filter by remember { mutableStateOf<String?>(null) }
    var menuFor by remember { mutableStateOf<String?>(null) }
    var creating by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<Rec?>(null) }
    var renameGroup by remember { mutableStateOf<Rec?>(null) }
    var newGroup by remember { mutableStateOf(false) }
    var pendingPlay by remember { mutableStateOf<Rec?>(null) }
    val visible = projects.filter { filter == null || it.f("tags").split(",").contains(filter) }
    val list: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text(t.all) })
                    tags.forEach { tag ->
                        FilterChip(selected = filter == tag.id, onClick = { filter = if (filter == tag.id) null else tag.id }, label = { Text(tag.f("name")) })
                    }
                }
            }
            if (visible.isEmpty()) {
                Text(t.emptyProjects, style = MaterialTheme.typography.titleLarge)
                Button(onClick = {
                    if (!premium && projects.size >= FREE_PROJECTS) onGate() else creating = true
                }) { Text(t.newProject) }
            }
            groups.forEach { group ->
                val mine = visible.filter { it.f("groupId") == group.id }
                if (mine.isEmpty() && filter != null) return@forEach
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(group.f("name"), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    TextButton(onClick = { renameGroup = group }) { Text(t.rename) }
                    if (group.id != GROUP_WORK && group.id != GROUP_STUDY && group.id != GROUP_PERSONAL) {
                        TextButton(onClick = { confirmDelete = group }) { Text(t.delete) }
                    }
                }
                mine.forEach { project ->
                    val locked = !premium && project.id !in free
                    val todayMs = rows.filter { it.kind == KIND_SESSION && it.f("projectId") == project.id }.sumOf { rec ->
                        val start = rec.f("startedAt").toLongOrNull() ?: return@sumOf 0L
                        if (dayKey(start, zone, dayStart) != today) 0L else sessionCountedMillis(rec)
                    }
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { onSelect(project.id) },
                            onLongClick = { menuFor = project.id },
                        ),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(18.dp).clip(CircleShape).background(colorOf(project.f("color"))))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(project.f("name"), style = MaterialTheme.typography.titleMedium)
                                Text(if (locked) t.locked else clockWords(todayMs), style = MaterialTheme.typography.bodyMedium)
                            }
                            TextButton(onClick = {
                                if (locked) onGate() else {
                                    val snap = app.center.snap()
                                    if ((snap.status == "running" || snap.status == "paused") && !(snap.kind == "track" && snap.projectId == project.id)) {
                                        pendingPlay = project
                                    } else if (snap.kind == "track" && snap.projectId == project.id && (snap.status == "running" || snap.status == "paused")) {
                                        app.center.stop()
                                    } else {
                                        app.center.start("track", project, null)
                                    }
                                }
                            }) { Text(if (app.center.snap().projectId == project.id && app.center.snap().kind == "track" && app.center.snap().status != "idle") t.stop else t.track) }
                            DropdownMenu(expanded = menuFor == project.id, onDismissRequest = { menuFor = null }) {
                                DropdownMenuItem(text = { Text(t.moveUp) }, onClick = { menuFor = null; move(app, projects, project, -1) })
                                DropdownMenuItem(text = { Text(t.moveDown) }, onClick = { menuFor = null; move(app, projects, project, 1) })
                                DropdownMenuItem(text = { Text(t.delete) }, onClick = { menuFor = null; confirmDelete = project })
                            }
                        }
                    }
                }
            }
            Button(onClick = {
                if (!premium && projects.size >= FREE_PROJECTS) onGate() else creating = true
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(t.newProject) }
            TextButton(onClick = { if (premium) newGroup = true else onGate() }) { Text(t.newGroup) }
        }
    }
    val detail: @Composable (Modifier) -> Unit = { modifier ->
        val project = projects.find { it.id == selectedId }
        if (project == null) {
            Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) { Text(t.chooseProject, style = MaterialTheme.typography.titleLarge) }
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
    if (creating) ProjectDialog(t, groups, { creating = false }) { name, color, groupId ->
        creating = false
        val id = UUID.randomUUID().toString()
        app.repo.put(
            app.repo.writerNow(
                KIND_PROJECT,
                id,
                mapOf(
                    "name" to name,
                    "color" to color,
                    "groupId" to groupId,
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
        onSelect(id)
    }
    confirmDelete?.let { item ->
        val project = item.kind == KIND_PROJECT
        Confirm(
            if (project) t.deleteProject else t.deleteGroup,
            if (project) t.deleteProjectBody else t.deleteGroupBody,
            t.delete,
            t.cancel,
            {
                if (project) {
                    app.repo.put(item.copy(deleted = true, updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId()))
                    rows.alive(KIND_TASK).filter { it.f("projectId") == item.id }.forEach {
                        app.repo.put(it.copy(deleted = true, updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId()))
                    }
                    if (selectedId == item.id) onSelect(null)
                } else {
                    projects.filter { it.f("groupId") == item.id }.forEach {
                        val body = it.body.toMutableMap()
                        body["groupId"] = GROUP_PERSONAL
                        app.repo.put(it.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = body))
                    }
                    app.repo.put(item.copy(deleted = true, updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId()))
                }
                confirmDelete = null
            },
            { confirmDelete = null },
        )
    }
    if (newGroup) {
        NameDialog(t.newGroup, "", t, { newGroup = false }) { name ->
            newGroup = false
            app.repo.put(
                app.repo.writerNow(
                    KIND_GROUP,
                    UUID.randomUUID().toString(),
                    mapOf("name" to name, "sort" to "9"),
                ),
            )
        }
    }
    renameGroup?.let { group ->
        NameDialog(t.rename, group.f("name"), t, { renameGroup = null }) { name ->
            val body = group.body.toMutableMap()
            body["name"] = name
            app.repo.put(group.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = body))
            renameGroup = null
        }
    }
    pendingPlay?.let { project ->
        Confirm(t.switchTitle, t.switchBody, t.save, t.cancel, {
            pendingPlay = null
            app.center.stop()
            app.center.start("track", project, null)
        }, { pendingPlay = null })
    }
}

private fun move(app: CantoApp, projects: List<Rec>, project: Rec, direction: Int) {
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
    app: CantoApp,
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
    var newTag by remember { mutableStateOf(false) }
    var editTag by remember { mutableStateOf<Rec?>(null) }
    var name by remember(project.id, project.f("name")) { mutableStateOf(project.f("name")) }
    val zone = ZoneId.systemDefault()
    val globals = app.repo.globals()
    val worked = workedMillisFor(rows, project.id)
    val shownTasks = tasks.filter { taskFilter == null || it.f("tags").split(",").contains(taskFilter) }
    Column(modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(name, { name = it }, label = { Text(t.name) }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            if (name.isBlank()) return@Button
            val body = project.body.toMutableMap()
            body["name"] = name.trim()
            app.repo.put(project.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = body))
        }) { Text(t.save) }
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
            Button(onClick = { if (premium) goalOpen = true else onGate() }) { Text(t.setGoal) }
        }
        Text(t.ownTimer, style = MaterialTheme.typography.titleLarge)
        if (!premium) {
            TextButton(onClick = onGate) { Text(t.locked) }
        } else {
            OwnTimer(t, project) { body ->
                app.repo.put(project.copy(updatedAt = System.currentTimeMillis(), writerId = app.repo.deviceId(), body = body))
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
        Button(onClick = { if (premium) newTag = true else onGate() }) { Text(t.newTag) }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                val on = project.f("tags").split(",").contains(tag.id)
                FilterChip(selected = on, onClick = {
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
                FilterChip(selected = taskFilter == null, onClick = { taskFilter = null }, label = { Text(t.all) })
                tags.forEach { tag ->
                    FilterChip(selected = taskFilter == tag.id, onClick = { taskFilter = if (taskFilter == tag.id) null else tag.id }, label = { Text(tag.f("name")) })
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
                Text(task.f("title") + if (locked) " · " + t.locked else "", modifier = Modifier.weight(1f))
                TextButton(onClick = { deleteTask = task }) { Text(t.delete) }
            }
        }
        Button(onClick = { if (!premium && tasks.size >= FREE_TASKS) onGate() else newTask = true }) { Text(t.newTask) }
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
private fun OwnTimer(t: T, project: Rec, onSave: (Map<String, String>) -> Unit) {
    var focus by remember(project.id) { mutableStateOf(project.f("focus")) }
    var short by remember(project.id) { mutableStateOf(project.f("short")) }
    var long by remember(project.id) { mutableStateOf(project.f("long")) }
    var cycles by remember(project.id) { mutableStateOf(project.f("cycles")) }
    var up by remember(project.id) { mutableStateOf(project.f("countUp") == "1") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberRow(t.focusLen, focus) { focus = it }
        NumberRow(t.shortLen, short) { short = it }
        NumberRow(t.longLen, long) { long = it }
        NumberRow(t.cycles, cycles) { cycles = it }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(t.countUp, modifier = Modifier.weight(1f))
            Switch(checked = up, onCheckedChange = { up = it })
        }
        Button(onClick = {
            val body = project.body.toMutableMap()
            body["focus"] = focus
            body["short"] = short
            body["long"] = long
            body["cycles"] = cycles
            body["countUp"] = if (up) "1" else "0"
            onSave(body)
        }) { Text(t.save) }
    }
}

@Composable
private fun NumberRow(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun StatsPage(app: CantoApp, t: T, rows: List<Rec>, premium: Boolean, wide: Boolean, snap: Snap, onGate: () -> Unit) {
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
                        color = if (index == pickedBar) Color(0xFFC56A3A) else Color(0xFF1F6F54),
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
                        drawArc(colorOf(rows.alive(KIND_PROJECT).find { it.id == slice.id }?.f("color") ?: "C8922A"), start, sweep, true)
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
                FilterChip(selected = range == index, onClick = {
                    if (index > 0 && !premium) onGate() else range = index
                }, label = { Text(label) })
            }
        }
        Text(clockWords(total), style = MaterialTheme.typography.displayLarge)
        if (slices.isEmpty()) Text(t.emptyRange) else slices.forEach { slice ->
            Text((slice.name.ifBlank { t.unlabeled }) + " · " + clockWords(slice.millis))
        }
        if (!premium) {
            Surface(tonalElevation = 1.dp, shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t.charts, style = MaterialTheme.typography.titleMedium)
                    Button(onClick = onGate) { Text(t.openSwitch) }
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
            Button(onClick = { share() }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(t.share) }
            val logicalToday = today
            Button(onClick = {
                if (selectedDay > logicalToday) note = t.future else manual = true
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(t.manual) }
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
        manual = false
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
            if (it.f("endedAt").isEmpty()) null else dayKey(start, zone, dayStart)
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
    val app = LocalContext.current.applicationContext as CantoApp
    val projects = rows.alive(KIND_PROJECT)
    var project by remember { mutableStateOf(projects.firstOrNull()?.id.orEmpty()) }
    var hours by remember { mutableIntStateOf(0) }
    var mins by remember { mutableIntStateOf(25) }
    var kind by remember { mutableStateOf("focus") }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(t.manual) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                projects.forEach { item ->
                    FilterChip(selected = project == item.id, onClick = { project = item.id }, label = { Text(item.f("name")) })
                }
                Stepper(t.hours, hours, 0, 12) { hours = it }
                Stepper(t.minutes, mins, 0, 59) { mins = it }
                Row {
                    FilterChip(selected = kind == "focus", onClick = { kind = "focus" }, label = { Text(t.focus) })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = kind == "track", onClick = { kind = "track" }, label = { Text(t.track) })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val duration = (hours * 60L + mins) * 60_000L
                val zone = ZoneId.systemDefault()
                val start = day.atTime(dayStart.coerceAtLeast(0), 30).atZone(zone).toInstant().toEpochMilli()
                when {
                    duration < 60_000L -> onDone(t.atLeast)
                    overlapsOpen(snap, start, duration, System.currentTimeMillis()) -> onDone(t.overlap)
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
private fun SettingsPage(app: CantoApp, t: T, rows: List<Rec>, premium: Boolean, wide: Boolean, onPair: () -> Unit, onGate: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val globals = app.repo.globals()
    val flags = rows.firstOrNull { it.kind == KIND_LOCAL && it.id == "flags" }?.body.orEmpty()
    var note by remember { mutableStateOf("") }
    val exportJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = encodeBackup(app.repo.snapshot().filter { it.kind in SYNC_KINDS })
            context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
        }
    }
    val exportCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = sessionsCsv(app.repo.snapshot(), ZoneId.systemDefault(), app.repo.globals().dayStart)
            context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
        }
    }
    val importJson = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            val parsed = text?.let { decodeBackup(it) }
            if (parsed == null) note = t.badFile else {
                app.repo.mergeFrom(parsed)
                note = t.imported
            }
        }
    }
    val sound = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            context.contentResolver.openInputStream(uri)?.use { input ->
                java.io.File(context.filesDir, "alarm-custom").outputStream().use { output -> input.copyTo(output) }
            }
            app.repo.setPref("alarm", "custom")
        }
    }
    val scroll = rememberScrollState()
    val body: @Composable () -> Unit = {
        Column(Modifier.verticalScroll(scroll).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 2.dp) {
                Column(Modifier.padding(16.dp)) {
                    Text(t.testerTitle, style = MaterialTheme.typography.titleLarge)
                    Text(t.testerBody)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (premium) t.premiumOn else t.premiumOff, modifier = Modifier.weight(1f))
                        Switch(checked = premium, onCheckedChange = { app.repo.setPremium(it) })
                    }
                }
            }
            Text(t.timerSettings, style = MaterialTheme.typography.titleLarge)
            Stepper(t.focusLen, globals.focus, 1, 180) { app.repo.setPref("focus_min", it.toString()); app.center.refreshSettings() }
            Stepper(t.shortLen, globals.short, 1, 60) { app.repo.setPref("short_min", it.toString()); app.center.refreshSettings() }
            Stepper(t.longLen, globals.long, 1, 120) { app.repo.setPref("long_min", it.toString()); app.center.refreshSettings() }
            Stepper(t.cycles, globals.cycles, 1, 12) { app.repo.setPref("cycles", it.toString()); app.center.refreshSettings() }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (globals.countUp) t.countUp else t.countDown, modifier = Modifier.weight(1f))
                Switch(checked = globals.countUp, onCheckedChange = { app.repo.setPref("count_up", if (it) "1" else "0"); app.center.refreshSettings() })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(t.autoContinue, modifier = Modifier.weight(1f))
                Switch(checked = globals.auto, onCheckedChange = { app.repo.setPref("auto_start", if (it) "1" else "0"); app.center.refreshSettings() })
            }
            Stepper(t.dayStarts, globals.dayStart, 0, 6) { app.repo.setPref("day_start", it.toString()) }
            Text(t.appearance, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("system" to t.system, "light" to t.light, "dark" to t.dark).forEach { (id, label) ->
                    FilterChip(selected = (flags["theme"] ?: "system") == id, onClick = { app.repo.updateFlags { it["theme"] = id } }, label = { Text(label) })
                }
            }
            Text(t.textSize)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("compact" to t.compact, "standard" to t.standard, "large" to t.large).forEach { (id, label) ->
                    FilterChip(selected = (flags["text"] ?: "standard") == id, onClick = { app.repo.updateFlags { it["text"] = id } }, label = { Text(label) })
                }
            }
            Text(t.language)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("system", "en", "tr", "es", "pt", "fr", "ja", "ko").forEach { id ->
                    FilterChip(selected = (flags["language"] ?: "system") == id, onClick = { app.repo.updateFlags { it["language"] = id } }, label = { Text(id) })
                }
            }
            Text(t.sounds, style = MaterialTheme.typography.titleLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("chime" to t.chime, "bell" to t.bell, "pulse" to t.pulse, "custom" to t.custom).forEach { (id, label) ->
                    FilterChip(selected = globals.alarm == id, onClick = {
                        val paid = id != "chime"
                        if (paid && !premium) onGate() else if (id == "custom") sound.launch(arrayOf("audio/*")) else app.repo.setPref("alarm", id)
                    }, label = { Text(label) })
                }
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("off" to t.off, "rain" to t.rain, "brook" to t.brook, "hearth" to t.hearth).forEach { (id, label) ->
                    FilterChip(selected = globals.background == id, onClick = {
                        if (id != "off" && !premium) onGate() else app.repo.setPref("background", id)
                    }, label = { Text(label) })
                }
            }
            Stepper(t.volume, globals.volume, 0, 100) { app.repo.setPref("bg_volume", it.toString()) }
            Text(t.data, style = MaterialTheme.typography.titleLarge)
            Button(onClick = { if (premium) exportJson.launch("canto-backup.json") else onGate() }, modifier = Modifier.fillMaxWidth()) { Text(t.exportJson) }
            Button(onClick = { if (premium) exportCsv.launch("canto-sessions.csv") else onGate() }, modifier = Modifier.fillMaxWidth()) { Text(t.exportCsv) }
            Button(onClick = { if (premium) importJson.launch(arrayOf("application/json", "text/*", "*/*")) else onGate() }, modifier = Modifier.fillMaxWidth()) { Text(t.importJson) }
            if (note.isNotEmpty()) Text(note)
            Text(t.devices, style = MaterialTheme.typography.titleLarge)
            Button(onClick = onPair, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(t.pair) }
            Text(t.about, style = MaterialTheme.typography.titleLarge)
            Text(t.aboutBody)
        }
    }
    if (wide) {
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
                Text(t.timerSettings, style = MaterialTheme.typography.headlineMedium)
                Text(t.aboutBody)
            }
            Box(Modifier.weight(1f)) { body() }
        }
    } else {
        body()
    }
}

@Composable
private fun PairPage(app: CantoApp, t: T, rows: List<Rec>) {
    val context = LocalContext.current
    val offer by app.lan.offer.collectAsState()
    val until by app.lan.offerUntil.collectAsState()
    val message by app.lan.message.collectAsState()
    var digits by remember { mutableStateOf("") }
    var now by remember { mutableIntStateOf(0) }
    var afterGrant by remember { mutableStateOf<(() -> Unit)?>(null) }
    val nearbyPermission = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.NEARBY_WIFI_DEVICES
    } else {
        Manifest.permission.ACCESS_FINE_LOCATION
    }
    val askNearby = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val next = afterGrant
        afterGrant = null
        if (granted) next?.invoke()
    }
    val withLink: (() -> Unit) -> Unit = { block ->
        val granted = ContextCompat.checkSelfPermission(context, nearbyPermission) == PackageManager.PERMISSION_GRANTED
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
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (now < 0) return@Column
        Button(onClick = { withLink { app.lan.showCode() } }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(t.showCode) }
        if (offer != null) {
            Text(offer!!, style = MaterialTheme.typography.displayLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text(t.expiresIn(clockDigits(leftMs)), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text(t.offerHelp)
            TextButton(onClick = { app.lan.hideCode() }) { Text(t.cancel) }
        } else if (message == "expired") {
            Text(t.expired)
            Button(onClick = { withLink { app.lan.showCode() } }) { Text(t.newCode) }
        }
        Text(t.enterHelp)
        OutlinedTextField(digits, { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) digits = it }, label = { Text(t.enterCode) }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { withLink { app.lan.enter(digits) } }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(t.pairAction) }
        if (message.isNotEmpty() && message != "expired") Text(t.pairMessage(message))
        Text(t.devices, style = MaterialTheme.typography.titleLarge)
        if (peers.isEmpty()) Text(t.noPeers)
        peers.forEach { peer ->
            val id = peer.id.removePrefix("peer:")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(peer.f("name").ifBlank { "Canto" })
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
private fun Stepper(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        FilledTonalButton(onClick = { if (value > min) onChange(value - 1) }, enabled = value > min, modifier = Modifier.size(48.dp)) { Text("–") }
        Text(value.toString(), modifier = Modifier.widthIn(min = 36.dp), textAlign = TextAlign.Center)
        FilledTonalButton(onClick = { if (value < max) onChange(value + 1) }, enabled = value < max, modifier = Modifier.size(48.dp)) { Text("+") }
    }
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
                OutlinedTextField(name, { name = it; error = false }, label = { Text(t.name) })
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
private fun ProjectDialog(
    t: T,
    groups: List<Rec>,
    onClose: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("1F6F54") }
    var group by remember { mutableStateOf(groups.firstOrNull()?.id ?: GROUP_PERSONAL) }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(t.newProject) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it; error = false }, label = { Text(t.name) })
                if (error) Text(t.giveName)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Swatches.forEach { swatch ->
                        val hex = "%06X".format(swatch)
                        Box(
                            Modifier.size(32.dp).clip(CircleShape).background(colorOf(hex)).clickable { color = hex },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    groups.forEach { item ->
                        FilterChip(selected = group == item.id, onClick = { group = item.id }, label = { Text(item.f("name")) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isBlank()) error = true else onSave(name.trim(), color, group) }) { Text(t.save) }
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
                Stepper(t.hours, hours, 0, 10000) { hours = it }
                Stepper(t.minutes, mins, 0, 59) { mins = it }
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
