package app.presence.desktop

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import app.presence.core.*
import kotlinx.coroutines.delay
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.nio.file.Files
import java.time.*
import java.util.UUID

fun main() {
    val directory = Store.dataDirectory()
    Files.createDirectories(directory)
    val channel = java.nio.channels.FileChannel.open(directory.resolve("presence.lock"), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.WRITE)
    val lock = channel.tryLock()
    if (lock == null) { javax.swing.JOptionPane.showMessageDialog(null, "Presence is already running."); channel.close(); return }
    val store = try { Store() } catch (e: Exception) {
        javax.swing.JOptionPane.showMessageDialog(null, "Could not open Presence data: ${e.message}\nYour data has been left in place.", "Presence", javax.swing.JOptionPane.ERROR_MESSAGE)
        lock.release(); channel.close()
        return
    }
    val timer = DesktopTimer(store)
    val lan = DesktopLan(store)
    application {
        Window(onCloseRequest = {
            if (sessionOpen(store.snap())) {
                val answer = javax.swing.JOptionPane.showConfirmDialog(null, "The timer will resume when you reopen Presence. Close the app?", "Close Presence", javax.swing.JOptionPane.YES_NO_OPTION)
                if (answer != javax.swing.JOptionPane.YES_OPTION) return@Window
            }
            lan.close(); lock.release(); channel.close(); exitApplication()
        }, title = "Presence", icon = painterResource("presence.png"), state = rememberWindowState(width = 1120.dp, height = 780.dp)) {
            window.minimumSize = java.awt.Dimension(700, 560)
            App(store, timer, lan)
        }
    }
}

@Composable private fun App(store: Store, timer: DesktopTimer, lan: DesktopLan) {
    var pulse by remember { mutableStateOf(0L) }
    var page by remember { mutableStateOf("Timer") }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            try { if (timer.tick()) Toolkit.getDefaultToolkit().beep() }
            catch (e: Exception) { error = "Could not save timer: ${e.message}" }
            pulse++; delay(250)
        }
    }
    val rows = remember(pulse, store.revision) { store.snapshot() }
    val dark = when (store.flag("theme", "system")) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    PresenceTheme(dark, store.flag("palette", "grove")) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Row {
                Column(Modifier.width(180.dp).fillMaxHeight().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Presence", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 20.dp))
                    listOf("Timer", "Projects", "Statistics", "Todos", "Settings").forEach { item ->
                        if (page == item) FilledTonalButton({ page = item }, Modifier.fillMaxWidth()) { Text(item) }
                        else TextButton({ page = item }, Modifier.fillMaxWidth()) { Text(item) }
                    }
                    Spacer(Modifier.weight(1f))
                    Text("Make time for\none thing.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Offline · On your devices", style = MaterialTheme.typography.bodySmall)
                }
                VerticalDivider()
                Column(Modifier.weight(1f).fillMaxHeight().padding(28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(page, style = MaterialTheme.typography.headlineLarge)
                    if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                    when (page) {
                        "Timer" -> TimerScreen(store, timer, rows)
                        "Projects" -> ProjectsScreen(store, rows)
                        "Statistics" -> StatsScreen(store, rows)
                        "Todos" -> TodosScreen(store, rows)
                        else -> SettingsScreen(store, lan)
                    }
                }
            }
        }
    }
}

@Composable private fun TimerScreen(store: Store, timer: DesktopTimer, rows: List<Rec>) {
    var projectId by remember { mutableStateOf<String?>(null) }
    var taskId by remember { mutableStateOf<String?>(null) }
    var tracking by remember { mutableStateOf(false) }
    val snap = store.snap()
    val busy = sessionOpen(snap)
    val projects = rows.alive(KIND_PROJECT).filter { it.f("archived") != "1" }
    val project = projects.find { it.id == projectId }
    val task = rows.alive(KIND_TASK).find { it.id == taskId }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(!tracking, { if (!busy) tracking = false }, { Text("Focus plan") }, enabled = !busy)
            FilterChip(tracking, { if (!busy) tracking = true }, { Text("Free tracking") }, enabled = !busy)
        }
        if (busy) Text(snap.projectName.ifEmpty { "No project" }, style = MaterialTheme.typography.titleLarge)
        else {
            Picker("Project", projects.map { it.id to it.f("name") }, projectId) { projectId = it; taskId = null }
            if (projectId != null) Picker("Task", rows.alive(KIND_TASK).filter { it.f("projectId") == projectId && it.f("done") != "1" }.map { it.id to it.f("title") }, taskId) { taskId = it }
        }
        val value = timer.display()
        val progress = if (snap.phaseMillis > 0 && busy) (1f - value.toFloat() / snap.phaseMillis).coerceIn(0f,1f) else 0f
        Box(Modifier.size(280.dp).padding(8.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator({ if (tracking) 0f else progress }, Modifier.fillMaxSize(), strokeWidth = 9.dp, trackColor = MaterialTheme.colorScheme.surfaceVariant, strokeCap = StrokeCap.Round)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(clockDigits(value), style = MaterialTheme.typography.displayLarge)
                Text(when { snap.status == "awaiting" -> "Ready for ${snap.awaitingKind}"
                    snap.status == "paused" -> "Paused"
                    busy -> when(snap.kind) { "short" -> "Short break"; "long" -> "Long break"; "track" -> "Tracking"; else -> "Focus stretch ${snap.stretchIndex + 1}" }
                    else -> "Ready when you are" })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button({ if (snap.status == "running") timer.pause() else timer.start(if (tracking) "track" else "focus", project, task) }) {
                Text(if (snap.status == "running") "Pause" else "Continue")
            }
            OutlinedButton({ timer.finish("stop") }, enabled = busy) { Text("Stop") }
            OutlinedButton({ timer.finish("skip") }, enabled = busy && snap.kind != "track") { Text("Skip") }
        }
        if (!tracking) Text("${clockWords(store.pref("work_sec", "3600").toLongOrNull()?.times(1000) ?: DEFAULT_WORK_MS)} of work · ${store.pref("short_count", "2")} short breaks · ${store.pref("long_count", "0")} long breaks", style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Text("Today", style = MaterialTheme.typography.titleLarge)
        val today = LocalDate.now()
        val todayRows = rows.alive(KIND_SESSION).filter { dayKey(it.f("startedAt").toLongOrNull() ?: 0, ZoneId.systemDefault(), 0) == today }
        Text("${clockWords(todayRows.sumOf(::sessionCountedMillis))} focused", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable private fun Picker(label: String, items: List<Pair<String,String>>, selected: String?, change: (String?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton({ open = true }) { Text("$label: ${items.find { it.first == selected }?.second ?: "None"}") }
        DropdownMenu(open, { open = false }) {
            DropdownMenuItem({ Text("None") }, { change(null); open = false })
            items.forEach { (id, name) -> DropdownMenuItem({ Text(name) }, { change(id); open = false }) }
        }
    }
}

@Composable private fun ProjectsScreen(store: Store, rows: List<Rec>) {
    var edit by remember { mutableStateOf<Rec?>(null) }
    var creating by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>(null) }
    var taskTitle by remember { mutableStateOf("") }
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button({ creating = true }) { Text("New project") }
        if (rows.alive(KIND_PROJECT).isEmpty()) Text("Create a project to give your focus time a home.")
        rows.alive(KIND_PROJECT).forEach { project ->
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(project.f("name"), style = MaterialTheme.typography.titleLarge)
                            Text("${clockWords(workedMillisFor(rows, project.id))} worked${project.f("goalMinutes").takeIf { it.isNotBlank() }?.let { " · Goal: $it minutes" } ?: ""}")
                        }
                        TextButton({ selected = if (selected == project.id) null else project.id }) { Text("Tasks") }
                        TextButton({ edit = project }) { Text("Edit") }
                        TextButton({ store.write(project.kind, project.id, project.body, true) }) { Text("Delete") }
                    }
                    if (selected == project.id) {
                        rows.alive(KIND_TASK).filter { it.f("projectId") == project.id }.forEach { task ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(task.f("done") == "1", { store.write(KIND_TASK, task.id, task.body + ("done" to if (it) "1" else "0")) })
                                Text(task.f("title"), Modifier.weight(1f))
                                TextButton({ store.write(KIND_TASK, task.id, task.body, true) }) { Text("Delete") }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(taskTitle, { taskTitle = it }, label = { Text("New task") }, modifier = Modifier.weight(1f), singleLine = true)
                            Button({ store.write(KIND_TASK, body = mapOf("projectId" to project.id, "title" to taskTitle.trim(), "done" to "0")); taskTitle = "" }, enabled = taskTitle.isNotBlank()) { Text("Add") }
                        }
                    }
                }
            }
        }
    }
    if (creating || edit != null) ProjectDialog(edit, { creating = false; edit = null }) { id, body ->
        store.write(KIND_PROJECT, id, body); creating = false; edit = null
    }
}

@Composable private fun ProjectDialog(project: Rec?, close: () -> Unit, save: (String, Map<String,String>) -> Unit) {
    var name by remember { mutableStateOf(project?.f("name").orEmpty()) }
    var goal by remember { mutableStateOf(project?.f("goalMinutes").orEmpty()) }
    var deadline by remember { mutableStateOf(project?.f("goalDeadline")?.toLongOrNull()?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toString() }.orEmpty()) }
    val date = if (deadline.isBlank()) null else runCatching { LocalDate.parse(deadline).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
    AlertDialog(onDismissRequest = close, title = { Text(if (project == null) "New project" else "Edit project") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
            OutlinedTextField(goal, { goal = it }, label = { Text("Goal in minutes (optional)") }, singleLine = true)
            OutlinedTextField(deadline, { deadline = it }, label = { Text("Deadline: YYYY-MM-DD (optional)") }, singleLine = true, isError = deadline.isNotBlank() && date == null)
        }
    }, confirmButton = { Button({ save(project?.id ?: UUID.randomUUID().toString(), (project?.body ?: emptyMap()) + mapOf("name" to name.trim(), "goalMinutes" to goal, "goalDeadline" to (date?.toString() ?: ""), "color" to (project?.f("color")?.ifEmpty { "14614A" } ?: "14614A"))) }, enabled = name.isNotBlank() && (goal.isBlank() || (goal.toIntOrNull() ?: -1) >= 0) && (deadline.isBlank() || date != null)) { Text("Save") } }, dismissButton = { TextButton(close) { Text("Cancel") } })
}

@Composable private fun TodosScreen(store: Store, rows: List<Rec>) {
    var title by remember { mutableStateOf("") }
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("What needs doing?") }, modifier = Modifier.weight(1f), singleLine = true)
            Button({ store.write(KIND_LOCAL, "todo:${UUID.randomUUID()}", mapOf("title" to title.trim(), "done" to "0")); title = "" }, enabled = title.isNotBlank()) { Text("Add todo") }
        }
        Text("Todos stay on this device, just like on Android.", style = MaterialTheme.typography.bodyMedium)
        rows.alive(KIND_LOCAL).filter { it.id.startsWith("todo:") }.forEach { todo ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(todo.f("done") == "1", { store.write(KIND_LOCAL, todo.id, todo.body + ("done" to if (it) "1" else "0")) })
                Text(todo.f("title"), Modifier.weight(1f))
                TextButton({ store.write(KIND_LOCAL, todo.id, todo.body, true) }) { Text("Delete") }
            }
        }
    }
}

@Composable private fun StatsScreen(store: Store, rows: List<Rec>) {
    var days by remember { mutableStateOf(7) }
    val zone = ZoneId.systemDefault()
    val sessions = rows.alive(KIND_SESSION).filter { sessionCountedMillis(it) > 0 && dayKey(it.f("startedAt").toLongOrNull() ?: 0, zone, 0) >= LocalDate.now().minusDays(days - 1L) }
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { listOf(7,30,365).forEach { n -> FilterChip(days == n, { days = n }, { Text("$n days") }) } }
        Text(clockWords(sessions.sumOf(::sessionCountedMillis)), style = MaterialTheme.typography.displayMedium)
        Text("${sessions.size} completed focus and tracking sessions")
        sessions.groupBy { it.f("projectName").ifEmpty { "No project" } }.entries.sortedByDescending { it.value.sumOf(::sessionCountedMillis) }.forEach { (name, entries) ->
            val total = entries.sumOf(::sessionCountedMillis)
            Text("$name  ·  ${clockWords(total)}", style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator({ total.toFloat() / sessions.sumOf(::sessionCountedMillis).coerceAtLeast(1) }, Modifier.fillMaxWidth().height(8.dp))
        }
        OutlinedButton({ chooseFile("Export sessions", true, "presence-sessions.csv")?.let { Files.writeString(it, sessionsCsv(rows, zone, 0)) } }) { Text("Export all sessions as CSV") }
        HorizontalDivider()
        Text("Recent sessions", style = MaterialTheme.typography.titleLarge)
        sessions.sortedByDescending { it.f("startedAt").toLongOrNull() ?: 0 }.take(50).forEach {
            Text("${Instant.ofEpochMilli(it.f("startedAt").toLong()).atZone(zone).toLocalDate()}   ${it.f("projectName").ifEmpty { "No project" }}   ${clockWords(sessionCountedMillis(it))}")
        }
    }
}

@Composable private fun SettingsScreen(store: Store, lan: DesktopLan) {
    var code by remember { mutableStateOf("") }
    var direct by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Appearance", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("system", "light", "dark").forEach { theme -> FilterChip(store.flag("theme", "system") == theme, { store.setFlag("theme", theme) }, { Text(theme.replaceFirstChar(Char::uppercase)) }) } }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("grove","harbor","dusk","slate","berry","amber","moss","iris","copper","tide","ink").forEach { palette ->
                FilterChip(store.flag("palette", "grove") == palette, { store.setFlag("palette", palette) }, { Text(palette.replaceFirstChar(Char::uppercase)) })
            }
        }
        HorizontalDivider()
        Text("Focus plan", style = MaterialTheme.typography.titleLarge)
        Text("Changes apply to your next plan. Work is divided equally between focus stretches.")
        NumberSetting(store, "Total work (minutes)", "work_sec", "3600", 60, 1..5940)
        NumberSetting(store, "Short break (minutes)", "short_sec", "300", 60, 1..5940)
        NumberSetting(store, "Long break (minutes)", "long_sec", "900", 60, 1..5940)
        NumberSetting(store, "Short breaks", "short_count", "2", 1, 0..24)
        NumberSetting(store, "Long breaks", "long_count", "0", 1, 0..24)
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(store.pref("count_up", "0") == "1", { store.write(KIND_PREF, "count_up", mapOf("value" to if (it) "1" else "0")) }); Text("Count up") }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(store.pref("auto_start", "0") == "1", { store.write(KIND_PREF, "auto_start", mapOf("value" to if (it) "1" else "0")) }); Text("Automatically continue to the next phase") }
        HorizontalDivider()
        Text("Pair on Wi-Fi", style = MaterialTheme.typography.titleLarge)
        Text("Connect your devices to the same network. Projects, tasks, tags, goals, completed sessions and timer settings sync automatically every 30 seconds while Presence is open.")
        Text(lan.message, color = MaterialTheme.colorScheme.primary)
        if (lan.code != null) Text(lan.code!!, style = MaterialTheme.typography.displayMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton({ lan.showCode() }) { Text("Show code") }
            TextButton({ lan.hideCode() }) { Text("Hide code") }
            OutlinedButton({ lan.sync() }) { Text("Sync now") }
        }
        OutlinedTextField(code, { code = it.filter(Char::isDigit).take(4) }, label = { Text("Code from your other device") }, singleLine = true)
        OutlinedTextField(direct, { direct = it }, label = { Text("Address:port (optional fallback)") }, singleLine = true)
        Button({ lan.pair(code, direct) }, enabled = code.length == 4) { Text("Pair and sync") }
        Text("This computer: ${lan.address()}", style = MaterialTheme.typography.bodySmall)
        lan.peers().forEach { Text("Nearby: ${it.name}") }
        store.snapshot().alive(KIND_LOCAL).filter { it.id.startsWith("peer:") }.forEach { peer ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Paired: ${peer.f("name")}", Modifier.weight(1f))
                TextButton({ store.write(KIND_LOCAL, peer.id, peer.body, true) }) { Text("Forget") }
            }
        }
        HorizontalDivider()
        Text("Your data", style = MaterialTheme.typography.titleLarge)
        Text(message)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton({ runCatching { chooseFile("Export backup", true, "presence-backup.json")?.let { Files.writeString(it, encodeBackup(store.snapshot())); message = "Backup saved" } }.onFailure { message = "Export failed: ${it.message}" } }) { Text("Export backup") }
            OutlinedButton({ runCatching { chooseFile("Import backup", false)?.let { path -> val incoming = decodeBackup(Files.readString(path)) ?: error("Invalid Presence backup"); store.mergeFrom(incoming); message = "Backup merged" } }.onFailure { message = "Import failed: ${it.message}" } }) { Text("Import backup") }
        }
        Text("Presence 2.2.1 · Apache 2.0\n${store.directory}", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun NumberSetting(store: Store, label: String, key: String, fallback: String, multiplier: Int, range: IntRange) {
    val current = store.pref(key, fallback)
    var text by remember(current) { mutableStateOf(((current.toIntOrNull() ?: fallback.toInt()) / multiplier).toString()) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(text, { text = it }, label = { Text(label) }, singleLine = true, modifier = Modifier.width(260.dp), isError = text.toIntOrNull() !in range)
        TextButton({ store.write(KIND_PREF, key, mapOf("value" to (text.toInt() * multiplier).toString())) }, enabled = text.toIntOrNull() in range) { Text("Save") }
    }
}

private fun chooseFile(title: String, save: Boolean, name: String = ""): java.nio.file.Path? {
    val dialog = FileDialog(null as Frame?, title, if (save) FileDialog.SAVE else FileDialog.LOAD)
    return try { dialog.file = name; dialog.isVisible = true; dialog.file?.let { java.nio.file.Paths.get(dialog.directory, it) } } finally { dialog.dispose() }
}
