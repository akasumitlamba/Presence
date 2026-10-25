package app.presence.desktop

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.presence.core.*
import kotlinx.coroutines.delay
import java.time.ZoneId

/** Desktop equivalents of Android's four home-screen widgets, sharing the same records. */
@Composable fun WidgetContent(kind: String, app: PresenceApp, open: () -> Unit) {
    val rows by app.repo.observe().collectAsState()
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while(true) { delay(250); tick++ } }
    val t = T(resolveLang(app.repo.flag("language","system")))
    val dark = when(app.repo.flag("theme","system")) { "dark" -> true; "light" -> false; else -> androidx.compose.foundation.isSystemInDarkTheme() }
    PresenceTheme(dark,app.repo.flag("palette","grove")) {
        Surface {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when(kind) {
                    "timer" -> {
                        val s = app.center.snap()
                        Text(s.projectName.ifBlank { "Presence" },style = MaterialTheme.typography.titleLarge)
                        val g = app.repo.globals()
                        val shown = if(s.status == "idle") { if(g.countUp) 0L else stretchLengths(g.workMillis,stretchCount(g.shortCount,g.longCount)).first() } else displayMillis(s,System.currentTimeMillis(),monotonicMillis())
                        Text(clockDigits(shown),style = MaterialTheme.typography.displayMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button({ when(s.status) { "running" -> app.center.pause(); "paused" -> app.center.resume(); "awaiting" -> app.center.start(s.awaitingKind,null,null); else -> open() } }) { Text(if(s.status == "running") t.pause else t.play) }
                            TextButton({ app.center.stop() }, enabled = sessionOpen(s)) { Text(t.stop) }
                        }
                    }
                    "stats" -> {
                        Text(t.today,style = MaterialTheme.typography.titleLarge)
                        val zone = ZoneId.systemDefault(); val hour = app.repo.globals().dayStart
                        val today = dayKey(System.currentTimeMillis(),zone,hour)
                        Text(clockWords(dayTotals(rows,listOf(today),zone,hour).single()),style = MaterialTheme.typography.displayMedium)
                        Text(t.week + " · " + clockDigits(dayTotals(rows,(0..6).map { today.minusDays(it.toLong()) },zone,hour).sum()))
                    }
                    "streak" -> {
                        val zone = ZoneId.systemDefault(); val hour = app.repo.globals().dayStart
                        val count = streakDays(rows,System.currentTimeMillis(),zone,hour)
                        Text(t.streak,style = MaterialTheme.typography.titleLarge)
                        Text(count.toString(),style = MaterialTheme.typography.displayMedium)
                        Text(t.streakLine(count,loggedToday(rows,System.currentTimeMillis(),zone,hour)))
                    }
                    "todos" -> {
                        Text(t.todos,style = MaterialTheme.typography.titleLarge)
                        rows.alive(KIND_LOCAL).filter { it.id.startsWith("todo:") }.sortedBy { it.f("sort").toLongOrNull() ?: it.updatedAt }.forEach { todo ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(todo.f("done") == "1", { app.repo.setTodoDone(todo.id.removePrefix("todo:"),it) })
                                Text(todo.f("title"))
                            }
                        }
                    }
                }
                TextButton(open) { Text("Presence") }
                if(tick < 0) Text("")
            }
        }
    }
}
