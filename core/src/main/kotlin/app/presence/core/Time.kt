package app.presence.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

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
    val lines = mutableListOf("Presence · $title", rangeLabel, clockWords(total), "")
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

fun stretchCount(shortBreaks: Int, longBreaks: Int): Int =
    shortBreaks.coerceAtLeast(0) + longBreaks.coerceAtLeast(0) + 1

fun stretchLengths(durationMs: Long, stretches: Int): List<Long> {
    val count = stretches.coerceAtLeast(1)
    val total = durationMs.coerceAtLeast(0L)
    val base = total / count
    val extra = (total % count).toInt()
    return List(count) { index -> base + if (index < extra) 1L else 0L }
}

fun breakKinds(shortBreaks: Int, longBreaks: Int): List<String> {
    val gaps = shortBreaks.coerceAtLeast(0) + longBreaks.coerceAtLeast(0)
    if (gaps == 0) return emptyList()
    val longs = longBreaks.coerceIn(0, gaps)
    if (longs == 0) return List(gaps) { "short" }
    if (longs == gaps) return List(gaps) { "long" }
    val marks = BooleanArray(gaps)
    if (longs == 1) {
        marks[(gaps - 1) / 2] = true
    } else {
        for (slot in 0 until longs) {
            val at = round(slot.toDouble() * (gaps - 1) / (longs - 1)).toInt().coerceIn(0, gaps - 1)
            marks[at] = true
        }
    }
    var placed = marks.count { it }
    var cursor = 0
    while (placed < longs && cursor < gaps) {
        if (!marks[cursor]) {
            marks[cursor] = true
            placed += 1
        }
        cursor += 1
    }
    return List(gaps) { if (marks[it]) "long" else "short" }
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
    val workMillis: Long = DEFAULT_WORK_MS,
    val shortCount: Int = DEFAULT_SHORT_BREAKS,
    val longCount: Int = DEFAULT_LONG_BREAKS,
    val shortMillis: Long = DEFAULT_SHORT_MS,
    val longMillis: Long = DEFAULT_LONG_MS,
    val stretchIndex: Int = 0,
    val pauseCount: Int = 0,
    val pauseTotalMs: Long = 0,
    val pauseStartedEpoch: Long = 0,
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
    "workMillis" to workMillis.toString(),
    "shortCount" to shortCount.toString(),
    "longCount" to longCount.toString(),
    "shortMillis" to shortMillis.toString(),
    "longMillis" to longMillis.toString(),
    "stretchIndex" to stretchIndex.toString(),
    "pauseCount" to pauseCount.toString(),
    "pauseTotalMs" to pauseTotalMs.toString(),
    "pauseStartedEpoch" to pauseStartedEpoch.toString(),
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
    workMillis = fields["workMillis"]?.toLongOrNull() ?: DEFAULT_WORK_MS,
    shortCount = fields["shortCount"]?.toIntOrNull() ?: DEFAULT_SHORT_BREAKS,
    longCount = fields["longCount"]?.toIntOrNull() ?: DEFAULT_LONG_BREAKS,
    shortMillis = fields["shortMillis"]?.toLongOrNull() ?: DEFAULT_SHORT_MS,
    longMillis = fields["longMillis"]?.toLongOrNull() ?: DEFAULT_LONG_MS,
    stretchIndex = fields["stretchIndex"]?.toIntOrNull() ?: 0,
    pauseCount = fields["pauseCount"]?.toIntOrNull() ?: 0,
    pauseTotalMs = fields["pauseTotalMs"]?.toLongOrNull() ?: 0L,
    pauseStartedEpoch = fields["pauseStartedEpoch"]?.toLongOrNull() ?: 0L,
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
    val worked = snap.workedMs + stretch
    return if (snap.kind == "track") worked else worked.coerceAtMost(snap.phaseMillis)
}

fun displayMillis(snap: Snap, nowEpoch: Long, nowElapsed: Long): Long {
    if (snap.kind == "track") return elapsedWork(snap, nowEpoch, nowElapsed)
    return if (snap.countUp) elapsedWork(snap, nowEpoch, nowElapsed) else timeLeft(snap, nowEpoch, nowElapsed)
}

fun pauseNoteMillis(snap: Snap, nowEpoch: Long): Long {
    val live = if (snap.status == "paused" && snap.pauseStartedEpoch > 0L) {
        (nowEpoch - snap.pauseStartedEpoch).coerceAtLeast(0L)
    } else {
        0L
    }
    return (snap.pauseTotalMs + live).coerceAtLeast(0L)
}

fun sessionOpen(snap: Snap): Boolean =
    snap.status == "running" || snap.status == "paused" || snap.status == "awaiting"

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
    "short" -> snap.shortMillis.coerceAtLeast(1_000L)
    "long" -> snap.longMillis.coerceAtLeast(1_000L)
    "track" -> 0L
    else -> {
        val count = stretchCount(snap.shortCount, snap.longCount)
        val lengths = stretchLengths(snap.workMillis, count)
        lengths[snap.stretchIndex.coerceIn(0, lengths.lastIndex)]
    }
}

private fun hintOf(snap: Snap, sessionId: String, token: String) = StartHint(
    snap.projectId,
    snap.projectName,
    snap.groupName,
    snap.taskId,
    snap.taskTitle,
    snap.writerId,
    sessionId,
    token,
)

private fun foldPause(snap: Snap, nowEpoch: Long): Snap {
    if (snap.status != "paused" || snap.pauseStartedEpoch <= 0L) return snap.copy(pauseStartedEpoch = 0)
    val added = (nowEpoch - snap.pauseStartedEpoch).coerceAtLeast(0L)
    return snap.copy(pauseTotalMs = snap.pauseTotalMs + added, pauseStartedEpoch = 0)
}

private fun idleSnap(snap: Snap): Snap {
    val lengths = stretchLengths(snap.workMillis, stretchCount(snap.shortCount, snap.longCount))
    return snap.copy(
        status = "idle",
        kind = "focus",
        awaitingKind = "",
        focusDone = 0,
        stretchIndex = 0,
        sessionId = null,
        workedMs = 0,
        phaseMillis = lengths.first(),
        alarmAtEpoch = 0,
        alarmAtElapsed = 0,
        completionToken = "",
        sessionStartedEpoch = 0,
        pauseCount = 0,
        pauseTotalMs = 0,
        pauseStartedEpoch = 0,
    )
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
    // A late alarm must not log the delay as work or as a pause.
    val overtime = if (snap.status == "running" && snap.kind != "track") {
        val gap = if (skewLarge(nowEpoch, nowElapsed, snap.stretchEpoch, snap.stretchElapsed)) {
            nowEpoch - snap.stretchEpoch
        } else nowElapsed - snap.stretchElapsed
        (snap.workedMs + gap.coerceAtLeast(0) - snap.phaseMillis).coerceAtLeast(0)
    } else 0L
    val endedAt = nowEpoch - overtime
    val paused = (endedAt - snap.sessionStartedEpoch - worked).coerceAtLeast(0)
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
        endedAt = endedAt.toString(),
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
        snap.copy(
            status = "paused",
            workedMs = worked,
            alarmAtEpoch = 0,
            alarmAtElapsed = 0,
            pauseCount = snap.pauseCount + 1,
            pauseStartedEpoch = nowEpoch,
        ),
        emptyList(),
        alarm = false,
        chime = false,
    )
}

fun resumeSnap(snap: Snap, nowEpoch: Long, nowElapsed: Long, token: String): Step {
    if (snap.status != "paused") return Step(snap, emptyList(), false, false)
    val left = (snap.phaseMillis - snap.workedMs).coerceAtLeast(0)
    val added = if (snap.pauseStartedEpoch > 0L) (nowEpoch - snap.pauseStartedEpoch).coerceAtLeast(0L) else 0L
    val running = snap.copy(
        status = "running",
        stretchEpoch = nowEpoch,
        stretchElapsed = nowElapsed,
        alarmAtEpoch = if (snap.kind == "track") 0 else nowEpoch + left,
        alarmAtElapsed = if (snap.kind == "track") 0 else nowElapsed + left,
        completionToken = token,
        pauseTotalMs = snap.pauseTotalMs + added,
        pauseStartedEpoch = 0,
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
    if (mode == "stop" && snap.status == "awaiting") return Step(idleSnap(snap), emptyList(), false, false)
    if (snap.status != "running" && snap.status != "paused") return Step(snap, emptyList(), false, false)
    if (snap.sessionId == null) return Step(snap, emptyList(), false, false)
    val worked = elapsedWork(snap, nowEpoch, nowElapsed)
    val keep = worked >= MIN_KEEP_MS
    val closed = closeOf(snap, nowEpoch, nowElapsed, keep)
    val chime = mode == "due"
    if (mode == "stop" || snap.kind == "track") {
        return Step(idleSnap(snap), listOf(closed), alarm = false, chime = false)
    }
    val carried = foldPause(snap, nowEpoch)
    val count = stretchCount(snap.shortCount, snap.longCount)
    val plan = breakKinds(snap.shortCount, snap.longCount)
    if (snap.kind == "focus") {
        val next = snap.stretchIndex + 1
        if (next >= count || snap.stretchIndex !in plan.indices) {
            return Step(idleSnap(snap), listOf(closed), alarm = false, chime = chime)
        }
        val hint = hintOf(snap, nextSessionId, nextToken)
        val base = carried.copy(
            status = "idle",
            awaitingKind = "",
            stretchIndex = next,
            focusDone = next,
            sessionId = null,
        )
        val armed = arm(base, plan[snap.stretchIndex], nowEpoch, nowElapsed, hint)
        return Step(armed.snap, listOf(closed) + armed.writes, alarm = true, chime = chime)
    }
    if (snap.kind == "short" || snap.kind == "long") {
        val lengths = stretchLengths(snap.workMillis, count)
        val index = snap.stretchIndex.coerceIn(0, lengths.lastIndex)
        val waiting = carried.copy(
            status = "awaiting",
            kind = "focus",
            awaitingKind = "focus",
            focusDone = index,
            phaseMillis = lengths[index],
            sessionId = null,
            workedMs = 0,
            alarmAtEpoch = 0,
            alarmAtElapsed = 0,
            completionToken = "",
            sessionStartedEpoch = 0,
            pauseStartedEpoch = 0,
        )
        if (snap.autoStart && mode == "due") {
            val armed = arm(waiting, "focus", nowEpoch, nowElapsed, hintOf(snap, nextSessionId, nextToken))
            return Step(armed.snap, listOf(closed) + armed.writes, alarm = true, chime = chime)
        }
        return Step(waiting, listOf(closed), alarm = false, chime = chime)
    }
    return Step(idleSnap(snap), listOf(closed), alarm = false, chime = false)
}

fun beginRest(
    snap: Snap,
    restKind: String,
    nowEpoch: Long,
    nowElapsed: Long,
    nextSessionId: String,
    nextToken: String,
): Step {
    if (restKind != "short" && restKind != "long") return Step(snap, emptyList(), false, false)
    if (snap.kind == restKind) return Step(snap, emptyList(), false, false)
    if (snap.status != "running" && snap.status != "paused") return Step(snap, emptyList(), false, false)
    if (snap.sessionId == null) return Step(snap, emptyList(), false, false)
    val worked = elapsedWork(snap, nowEpoch, nowElapsed)
    val keep = worked >= MIN_KEEP_MS
    val closed = closeOf(snap, nowEpoch, nowElapsed, keep)
    val done = when {
        snap.kind == "focus" && restKind == "long" -> 0
        snap.kind == "focus" -> snap.focusDone + 1
        else -> snap.focusDone
    }
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
    val armed = arm(
        snap.copy(status = "idle", awaitingKind = "", focusDone = done, sessionId = null),
        restKind,
        nowEpoch,
        nowElapsed,
        hint,
    )
    return Step(armed.snap.copy(focusDone = done), listOf(closed) + armed.writes, alarm = true, chime = false)
}

fun withSettings(
    snap: Snap,
    workMillis: Long,
    shortMillis: Long,
    longMillis: Long,
    shortCount: Int,
    longCount: Int,
): Snap {
    if (snap.status == "running" || snap.status == "paused" || snap.status == "awaiting") return snap
    val work = workMillis.coerceAtLeast(1_000L)
    val shorts = shortCount.coerceIn(0, 24)
    val longs = longCount.coerceIn(0, 24)
    val next = snap.copy(
        workMillis = work,
        shortMillis = shortMillis.coerceAtLeast(1_000L),
        longMillis = longMillis.coerceAtLeast(1_000L),
        shortCount = shorts,
        longCount = longs,
        stretchIndex = 0,
        kind = "focus",
    )
    return next.copy(phaseMillis = phaseLength(next, "focus"))
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

fun streakDays(records: List<Rec>, nowEpoch: Long, zone: ZoneId, dayStartHour: Int): Int {
    val today = dayKey(nowEpoch, zone, dayStartHour)
    val days = records.mapNotNull { rec ->
        if (sessionCountedMillis(rec) <= 0L) return@mapNotNull null
        val start = rec.f("startedAt").toLongOrNull() ?: return@mapNotNull null
        dayKey(start, zone, dayStartHour)
    }.toSet()
    var cursor = if (today in days) today else today.minusDays(1)
    var count = 0
    while (cursor in days) {
        count++
        cursor = cursor.minusDays(1)
    }
    return count
}

fun loggedToday(records: List<Rec>, nowEpoch: Long, zone: ZoneId, dayStartHour: Int): Boolean {
    val today = dayKey(nowEpoch, zone, dayStartHour)
    return records.any { rec ->
        sessionCountedMillis(rec) > 0L &&
            rec.f("startedAt").toLongOrNull()?.let { dayKey(it, zone, dayStartHour) == today } == true
    }
}

fun nextClockMillis(hour: Int, minute: Int, nowEpoch: Long, zone: ZoneId): Long {
    val now = Instant.ofEpochMilli(nowEpoch).atZone(zone)
    var next = now.withHour(hour.coerceIn(0, 23)).withMinute(minute.coerceIn(0, 59)).withSecond(0).withNano(0)
    if (!next.isAfter(now)) next = next.plusDays(1)
    return next.toInstant().toEpochMilli()
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

fun overlapsClosed(records: List<Rec>, startedAt: Long, durationMs: Long): Boolean {
    val end = startedAt + durationMs
    return records.any { rec ->
        if (sessionCountedMillis(rec) <= 0) return@any false
        val start = rec.f("startedAt").toLongOrNull() ?: return@any false
        val finish = rec.f("endedAt").toLongOrNull() ?: return@any false
        startedAt < finish && end > start
    }
}
