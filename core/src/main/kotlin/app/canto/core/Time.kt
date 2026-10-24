package app.canto.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

fun dayKey(epochMs: Long, zone: ZoneId, dayStartHour: Int): LocalDate {
    val shift = dayStartHour.coerceIn(0, 6).toLong()
    return Instant.ofEpochMilli(epochMs).atZone(zone).minusHours(shift).toLocalDate()
}

fun daysLeftInclusive(nowEpoch: Long, deadlineEpoch: Long, zone: ZoneId, dayStartHour: Int): Int {
    val today = dayKey(nowEpoch, zone, dayStartHour)
    val end = dayKey(deadlineEpoch, zone, dayStartHour)
    val span = ChronoUnit.DAYS.between(today, end) + 1
    return if (span < 1) 1 else span.toInt()
}

fun dailyTargetMinutes(
    goalMinutes: Int,
    workedMillis: Long,
    deadlineEpoch: Long,
    nowEpoch: Long,
    zone: ZoneId,
    dayStartHour: Int,
): Int {
    val remaining = goalMinutes - (workedMillis / 60_000L)
    val left = if (remaining < 0) 0 else remaining
    val days = daysLeftInclusive(nowEpoch, deadlineEpoch, zone, dayStartHour)
    return ((left + days - 1) / days).toInt()
}

fun sessionCountedMillis(rec: Rec): Long {
    if (rec.deleted || rec.kind != KIND_SESSION) return 0
    val end = rec.f("endedAt")
    if (end.isEmpty()) return 0
    val kind = rec.f("kind")
    if (kind != "focus" && kind != "track") return 0
    val start = rec.f("startedAt").toLongOrNull() ?: return 0
    val finish = end.toLongOrNull() ?: return 0
    val paused = rec.f("pausedMs").toLongOrNull() ?: 0L
    return (finish - start - paused).coerceAtLeast(0)
}

fun workedMillisFor(records: List<Rec>, projectId: String): Long =
    records.filter { it.f("projectId") == projectId }.sumOf { sessionCountedMillis(it) }

fun wholePercents(weights: List<Long>): List<Int> {
    if (weights.isEmpty()) return emptyList()
    val total = weights.sum()
    if (total <= 0L) return weights.map { 0 }
    val exact = weights.map { it * 100.0 / total }
    val floors = exact.map { floor(it).toInt() }.toMutableList()
    var left = 100 - floors.sum()
    val order = exact.indices.sortedByDescending { exact[it] - floor(exact[it]) }
    var cursor = 0
    while (left > 0 && order.isNotEmpty()) {
        floors[order[cursor % order.size]] += 1
        left -= 1
        cursor += 1
    }
    return floors
}

fun csvCell(value: String): String {
    val needs = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
    if (!needs) return value
    return "\"" + value.replace("\"", "\"\"") + "\""
}

fun sessionsCsv(records: List<Rec>, zone: ZoneId, dayStartHour: Int): String {
    val header = "day,project,group,task,kind,started_at,ended_at,duration_seconds,manual"
    val lines = records
        .filter { it.kind == KIND_SESSION && !it.deleted && it.f("endedAt").isNotEmpty() }
        .sortedBy { it.f("startedAt").toLongOrNull() ?: 0L }
        .map { rec ->
            val start = rec.f("startedAt").toLongOrNull() ?: 0L
            val end = rec.f("endedAt").toLongOrNull() ?: start
            val paused = rec.f("pausedMs").toLongOrNull() ?: 0L
            val seconds = ((end - start - paused).coerceAtLeast(0)) / 1000L
            listOf(
                dayKey(start, zone, dayStartHour).toString(),
                rec.f("projectName"),
                rec.f("groupName"),
                rec.f("taskTitle"),
                rec.f("kind"),
                start.toString(),
                end.toString(),
                seconds.toString(),
                if (rec.f("manual") == "1") "1" else "0",
            ).joinToString(",") { csvCell(it) }
        }
    return (listOf(header) + lines).joinToString("\n")
}

fun shareSummary(
    title: String,
    rangeLabel: String,
    rows: List<Pair<String, Long>>,
    groupOf: (String) -> String,
): String {
    val total = rows.sumOf { it.second }
    val lines = mutableListOf("Canto · $title", rangeLabel, clockWords(total), "")
    val grouped = rows.groupBy { groupOf(it.first) }
    for ((group, items) in grouped) {
        if (group.isNotEmpty()) lines += group
        for ((name, ms) in items.sortedByDescending { it.second }) {
            lines += "  $name — ${clockWords(ms)}"
        }
    }
    return lines.joinToString("\n").trimEnd()
}

fun clockWords(ms: Long): String {
    val minutes = (ms / 60_000L).toInt()
    val h = minutes / 60
    val m = minutes % 60
    return if (h <= 0) "${m}m" else "${h}h ${m.toString().padStart(2, '0')}m"
}

fun clockDigits(ms: Long): String {
    val safe = ms.coerceAtLeast(0) / 1000L
    val h = safe / 3600
    val m = (safe % 3600) / 60
    val s = safe % 60
    return if (h > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.ROOT, "%d:%02d", m, s)
    }
}

data class Snap(
    val status: String = "idle",
    val kind: String = "focus",
    val countUp: Boolean = false,
    val phaseMillis: Long = 25 * 60_000L,
    val sessionId: String? = null,
    val projectId: String? = null,
    val projectName: String = "",
    val groupName: String = "",
    val taskId: String? = null,
    val taskTitle: String = "",
    val focusDone: Int = 0,
    val cycles: Int = 4,
    val focusMin: Int = 25,
    val shortMin: Int = 5,
    val longMin: Int = 15,
    val autoStart: Boolean = false,
    val sessionStartedEpoch: Long = 0,
    val workedMs: Long = 0,
    val stretchEpoch: Long = 0,
    val stretchElapsed: Long = 0,
    val alarmAtEpoch: Long = 0,
    val alarmAtElapsed: Long = 0,
    val completionToken: String = "",
    val awaitingKind: String = "",
    val writerId: String = "",
)

fun Snap.toFields(): Map<String, String> = mapOf(
    "status" to status,
    "kind" to kind,
    "countUp" to if (countUp) "1" else "0",
    "phaseMillis" to phaseMillis.toString(),
    "sessionId" to sessionId.orEmpty(),
    "projectId" to projectId.orEmpty(),
    "projectName" to projectName,
    "groupName" to groupName,
    "taskId" to taskId.orEmpty(),
    "taskTitle" to taskTitle,
    "focusDone" to focusDone.toString(),
    "cycles" to cycles.toString(),
    "focusMin" to focusMin.toString(),
    "shortMin" to shortMin.toString(),
    "longMin" to longMin.toString(),
    "autoStart" to if (autoStart) "1" else "0",
    "sessionStartedEpoch" to sessionStartedEpoch.toString(),
    "workedMs" to workedMs.toString(),
    "stretchEpoch" to stretchEpoch.toString(),
    "stretchElapsed" to stretchElapsed.toString(),
    "alarmAtEpoch" to alarmAtEpoch.toString(),
    "alarmAtElapsed" to alarmAtElapsed.toString(),
    "completionToken" to completionToken,
    "awaitingKind" to awaitingKind,
    "writerId" to writerId,
)

fun snapFrom(fields: Map<String, String>): Snap = Snap(
    status = fields["status"] ?: "idle",
    kind = fields["kind"] ?: "focus",
    countUp = fields["countUp"] == "1",
    phaseMillis = fields["phaseMillis"]?.toLongOrNull() ?: 25 * 60_000L,
    sessionId = fields["sessionId"].orEmpty().ifEmpty { null },
    projectId = fields["projectId"].orEmpty().ifEmpty { null },
    projectName = fields["projectName"].orEmpty(),
    groupName = fields["groupName"].orEmpty(),
    taskId = fields["taskId"].orEmpty().ifEmpty { null },
    taskTitle = fields["taskTitle"].orEmpty(),
    focusDone = fields["focusDone"]?.toIntOrNull() ?: 0,
    cycles = fields["cycles"]?.toIntOrNull() ?: 4,
    focusMin = fields["focusMin"]?.toIntOrNull() ?: 25,
    shortMin = fields["shortMin"]?.toIntOrNull() ?: 5,
    longMin = fields["longMin"]?.toIntOrNull() ?: 15,
    autoStart = fields["autoStart"] == "1",
    sessionStartedEpoch = fields["sessionStartedEpoch"]?.toLongOrNull() ?: 0L,
    workedMs = fields["workedMs"]?.toLongOrNull() ?: 0L,
    stretchEpoch = fields["stretchEpoch"]?.toLongOrNull() ?: 0L,
    stretchElapsed = fields["stretchElapsed"]?.toLongOrNull() ?: 0L,
    alarmAtEpoch = fields["alarmAtEpoch"]?.toLongOrNull() ?: 0L,
    alarmAtElapsed = fields["alarmAtElapsed"]?.toLongOrNull() ?: 0L,
    completionToken = fields["completionToken"].orEmpty(),
    awaitingKind = fields["awaitingKind"].orEmpty(),
    writerId = fields["writerId"].orEmpty(),
)

fun skewLarge(nowEpoch: Long, nowElapsed: Long, anchorEpoch: Long, anchorElapsed: Long): Boolean {
    val epochGap = nowEpoch - anchorEpoch
    val elapsedGap = nowElapsed - anchorElapsed
    return abs(epochGap - elapsedGap) > 8_000L
}

fun timeLeft(snap: Snap, nowEpoch: Long, nowElapsed: Long): Long {
    if (snap.kind == "track") return 0
    if (snap.status == "paused" || snap.status == "awaiting") {
        return (snap.phaseMillis - snap.workedMs).coerceAtLeast(0)
    }
    if (snap.status != "running") return 0
    val byEpoch = snap.alarmAtEpoch - nowEpoch
    val byElapsed = snap.alarmAtElapsed - nowElapsed
    val useEpoch = skewLarge(nowEpoch, nowElapsed, snap.stretchEpoch, snap.stretchElapsed)
    return (if (useEpoch) byEpoch else byElapsed).coerceAtLeast(0)
}

fun elapsedWork(snap: Snap, nowEpoch: Long, nowElapsed: Long): Long {
    if (snap.status == "paused") return snap.workedMs.coerceAtLeast(0)
    if (snap.status != "running") return snap.workedMs.coerceAtLeast(0)
    val byEpoch = nowEpoch - snap.stretchEpoch
    val byElapsed = nowElapsed - snap.stretchElapsed
    val useEpoch = skewLarge(nowEpoch, nowElapsed, snap.stretchEpoch, snap.stretchElapsed)
    val stretch = (if (useEpoch) byEpoch else byElapsed).coerceAtLeast(0)
    return snap.workedMs + stretch
}

fun displayMillis(snap: Snap, nowEpoch: Long, nowElapsed: Long): Long {
    if (snap.kind == "track") return elapsedWork(snap, nowEpoch, nowElapsed)
    if (snap.countUp) {
        val done = if (snap.status == "running" || snap.status == "paused") {
            elapsedWork(snap, nowEpoch, nowElapsed)
        } else {
            0L
        }
        return done.coerceAtMost(snap.phaseMillis)
    }
    return timeLeft(snap, nowEpoch, nowElapsed)
}

fun due(snap: Snap, nowEpoch: Long, nowElapsed: Long): Boolean {
    if (snap.status != "running" || snap.kind == "track") return false
    return timeLeft(snap, nowEpoch, nowElapsed) <= 0L
}

data class StartHint(
    val projectId: String?,
    val projectName: String,
    val groupName: String,
    val taskId: String?,
    val taskTitle: String,
    val writerId: String,
    val sessionId: String,
    val token: String,
)

data class Step(
    val snap: Snap,
    val writes: List<Rec>,
    val alarm: Boolean,
    val chime: Boolean,
)

private fun phaseLength(snap: Snap, kind: String): Long = when (kind) {
    "short" -> snap.shortMin.coerceAtLeast(1) * 60_000L
    "long" -> snap.longMin.coerceAtLeast(1) * 60_000L
    "track" -> 0L
    else -> snap.focusMin.coerceAtLeast(1) * 60_000L
}

private fun sessionRec(
    snap: Snap,
    hint: StartHint,
    nowEpoch: Long,
    kind: String,
    endedAt: String,
    pausedMs: Long,
    deleted: Boolean,
): Rec = Rec(
    kind = KIND_SESSION,
    id = hint.sessionId,
    updatedAt = nowEpoch,
    writerId = hint.writerId,
    deleted = deleted,
    body = mapOf(
        "projectId" to hint.projectId.orEmpty(),
        "projectName" to hint.projectName,
        "groupName" to hint.groupName,
        "taskId" to hint.taskId.orEmpty(),
        "taskTitle" to hint.taskTitle,
        "kind" to kind,
        "startedAt" to snap.sessionStartedEpoch.toString(),
        "endedAt" to endedAt,
        "pausedMs" to pausedMs.toString(),
        "manual" to "0",
    ),
)

fun arm(
    snap: Snap,
    kind: String,
    nowEpoch: Long,
    nowElapsed: Long,
    hint: StartHint,
): Step {
    val length = phaseLength(snap, kind)
    val running = snap.copy(
        status = "running",
        kind = kind,
        phaseMillis = length,
        sessionId = hint.sessionId,
        projectId = hint.projectId,
        projectName = hint.projectName,
        groupName = hint.groupName,
        taskId = hint.taskId,
        taskTitle = hint.taskTitle,
        writerId = hint.writerId,
        sessionStartedEpoch = nowEpoch,
        workedMs = 0,
        stretchEpoch = nowEpoch,
        stretchElapsed = nowElapsed,
        alarmAtEpoch = if (kind == "track") 0 else nowEpoch + length,
        alarmAtElapsed = if (kind == "track") 0 else nowElapsed + length,
        completionToken = hint.token,
        awaitingKind = "",
    )
    val open = sessionRec(running, hint, nowEpoch, kind, endedAt = "", pausedMs = 0, deleted = false)
    return Step(running, listOf(open), alarm = kind != "track", chime = false)
}

private fun closeOf(snap: Snap, nowEpoch: Long, nowElapsed: Long, keep: Boolean): Rec {
    val worked = elapsedWork(snap, nowEpoch, nowElapsed)
    val paused = (nowEpoch - snap.sessionStartedEpoch - worked).coerceAtLeast(0)
    val hint = StartHint(
        snap.projectId,
        snap.projectName,
        snap.groupName,
        snap.taskId,
        snap.taskTitle,
        snap.writerId,
        snap.sessionId ?: "missing",
        snap.completionToken,
    )
    val base = snap.copy(sessionStartedEpoch = snap.sessionStartedEpoch)
    return sessionRec(
        base,
        hint,
        nowEpoch,
        snap.kind,
        endedAt = nowEpoch.toString(),
        pausedMs = paused,
        deleted = !keep,
    )
}

fun nextAfter(kind: String, focusDone: Int, cycles: Int): Pair<String, Int> = when (kind) {
    "focus" -> {
        val done = focusDone + 1
        if (done >= cycles.coerceAtLeast(1)) "long" to 0 else "short" to done
    }
    "short", "long" -> "focus" to focusDone
    else -> "focus" to 0
}

fun pauseSnap(snap: Snap, nowEpoch: Long, nowElapsed: Long): Step {
    if (snap.status != "running") return Step(snap, emptyList(), false, false)
    val worked = elapsedWork(snap, nowEpoch, nowElapsed)
    return Step(
        snap.copy(status = "paused", workedMs = worked, alarmAtEpoch = 0, alarmAtElapsed = 0),
        emptyList(),
        alarm = false,
        chime = false,
    )
}

fun resumeSnap(snap: Snap, nowEpoch: Long, nowElapsed: Long, token: String): Step {
    if (snap.status != "paused") return Step(snap, emptyList(), false, false)
    val left = (snap.phaseMillis - snap.workedMs).coerceAtLeast(0)
    val running = snap.copy(
        status = "running",
        stretchEpoch = nowEpoch,
        stretchElapsed = nowElapsed,
        alarmAtEpoch = if (snap.kind == "track") 0 else nowEpoch + left,
        alarmAtElapsed = if (snap.kind == "track") 0 else nowElapsed + left,
        completionToken = token,
    )
    return Step(running, emptyList(), alarm = snap.kind != "track", chime = false)
}

fun finishSnap(
    snap: Snap,
    nowEpoch: Long,
    nowElapsed: Long,
    mode: String,
    nextSessionId: String,
    nextToken: String,
): Step {
    if (snap.status != "running" && snap.status != "paused") return Step(snap, emptyList(), false, false)
    if (snap.sessionId == null) return Step(snap, emptyList(), false, false)
    val worked = elapsedWork(snap, nowEpoch, nowElapsed)
    val keep = worked >= MIN_KEEP_MS
    val closed = closeOf(snap, nowEpoch, nowElapsed, keep)
    val count = mode != "stop" && snap.kind == "focus"
    val (awaitKind, done) = if (mode == "stop" || snap.kind == "track") {
        "focus" to 0
    } else if (count || snap.kind == "short" || snap.kind == "long") {
        if (snap.kind == "focus" && !count) "focus" to snap.focusDone else nextAfter(snap.kind, snap.focusDone, snap.cycles)
    } else {
        "focus" to snap.focusDone
    }
    val idleBase = snap.copy(
        status = if (mode == "stop" || snap.kind == "track") "idle" else "awaiting",
        kind = if (mode == "stop" || snap.kind == "track") "focus" else awaitKind,
        awaitingKind = if (mode == "stop" || snap.kind == "track") "" else awaitKind,
        focusDone = done,
        sessionId = null,
        workedMs = 0,
        phaseMillis = phaseLength(snap, if (mode == "stop" || snap.kind == "track") "focus" else awaitKind),
        alarmAtEpoch = 0,
        alarmAtElapsed = 0,
        completionToken = "",
        sessionStartedEpoch = 0,
    )
    val chime = mode == "due"
    if (idleBase.status == "awaiting" && snap.autoStart) {
        val hint = StartHint(
            snap.projectId,
            snap.projectName,
            snap.groupName,
            snap.taskId,
            snap.taskTitle,
            snap.writerId,
            nextSessionId,
            nextToken,
        )
        val armed = arm(idleBase.copy(status = "idle", awaitingKind = ""), awaitKind, nowEpoch, nowElapsed, hint)
        return Step(armed.snap.copy(focusDone = done), listOf(closed) + armed.writes, alarm = armed.alarm, chime = chime)
    }
    return Step(idleBase, listOf(closed), alarm = false, chime = chime)
}

fun withSettings(snap: Snap, focus: Int, short: Int, long: Int, cycles: Int, countUp: Boolean, auto: Boolean): Snap {
    if (snap.status == "running" || snap.status == "paused") {
        return snap.copy(autoStart = auto)
    }
    return snap.copy(
        focusMin = focus.coerceIn(1, 180),
        shortMin = short.coerceIn(1, 60),
        longMin = long.coerceIn(1, 120),
        cycles = cycles.coerceIn(1, 12),
        countUp = countUp,
        autoStart = auto,
        phaseMillis = if (snap.status == "awaiting") {
            phaseLength(
                snap.copy(
                    focusMin = focus.coerceIn(1, 180),
                    shortMin = short.coerceIn(1, 60),
                    longMin = long.coerceIn(1, 120),
                ),
                snap.awaitingKind.ifEmpty { "focus" },
            )
        } else {
            snap.phaseMillis
        },
    )
}

fun manualSession(
    id: String,
    writerId: String,
    projectId: String,
    projectName: String,
    groupName: String,
    kind: String,
    startedAt: Long,
    durationMs: Long,
    now: Long,
): Rec = Rec(
    kind = KIND_SESSION,
    id = id,
    updatedAt = now,
    writerId = writerId,
    deleted = false,
    body = mapOf(
        "projectId" to projectId,
        "projectName" to projectName,
        "groupName" to groupName,
        "taskId" to "",
        "taskTitle" to "",
        "kind" to kind,
        "startedAt" to startedAt.toString(),
        "endedAt" to (startedAt + durationMs).toString(),
        "pausedMs" to "0",
        "manual" to "1",
    ),
)

fun overlapsOpen(snap: Snap, startedAt: Long, durationMs: Long, nowEpoch: Long): Boolean {
    if (snap.status != "running" && snap.status != "paused") return false
    val openStart = snap.sessionStartedEpoch
    val remaining = (snap.phaseMillis - snap.workedMs).coerceAtLeast(0)
    val openEnd = when {
        snap.kind == "track" -> Long.MAX_VALUE / 4
        snap.status == "paused" -> nowEpoch + remaining
        snap.alarmAtEpoch > 0 -> snap.alarmAtEpoch
        else -> openStart + snap.phaseMillis
    }
    val end = startedAt + durationMs
    return startedAt < openEnd && end > openStart
}

data class Slice(val id: String, val name: String, val millis: Long)

fun slicesFor(records: List<Rec>, fromDay: LocalDate, toDay: LocalDate, zone: ZoneId, dayStartHour: Int): List<Slice> {
    val bucket = linkedMapOf<String, Slice>()
    for (rec in records) {
        val ms = sessionCountedMillis(rec)
        if (ms <= 0) continue
        val start = rec.f("startedAt").toLongOrNull() ?: continue
        val day = dayKey(start, zone, dayStartHour)
        if (day < fromDay || day > toDay) continue
        val id = rec.f("projectId").ifEmpty { "none" }
        val name = rec.f("projectName").ifEmpty { "" }
        val cur = bucket[id]
        bucket[id] = if (cur == null) Slice(id, name, ms) else cur.copy(millis = cur.millis + ms, name = cur.name.ifEmpty { name })
    }
    return bucket.values.sortedByDescending { it.millis }
}

fun dayTotals(records: List<Rec>, days: List<LocalDate>, zone: ZoneId, dayStartHour: Int): List<Long> {
    val map = days.associateWith { 0L }.toMutableMap()
    for (rec in records) {
        val ms = sessionCountedMillis(rec)
        if (ms <= 0) continue
        val start = rec.f("startedAt").toLongOrNull() ?: continue
        val day = dayKey(start, zone, dayStartHour)
        if (day in map) map[day] = map.getValue(day) + ms
    }
    return days.map { map.getValue(it) }
}

fun freeProjectIds(projects: List<Rec>): Set<String> =
    projects.filter { !it.deleted }
        .sortedWith(compareBy({ it.f("createdAt").toLongOrNull() ?: it.updatedAt }, { it.id }))
        .take(FREE_PROJECTS)
        .map { it.id }
        .toSet()

fun premiumGate(feature: String, premium: Boolean): Boolean = premium || feature in FREE_FEATURES

val FREE_FEATURES = setOf(
    "timer", "track", "today", "groups", "notify", "widget-today", "sync", "theme", "language",
)
