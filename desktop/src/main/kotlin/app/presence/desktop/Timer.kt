package app.presence.desktop

import app.presence.core.*
import java.util.UUID

class DesktopTimer(private val store: Store) {
    private fun epoch() = System.currentTimeMillis()
    private fun elapsed() = System.nanoTime() / 1_000_000
    private fun id() = UUID.randomUUID().toString()
    init {
        // Monotonic anchors cannot survive restarting the JVM or rebooting.
        val s = store.snap()
        if (s.status == "running") {
            val e = epoch(); val m = elapsed()
            val delta = (e - s.stretchEpoch).coerceAtLeast(0)
            store.apply(Step(s.copy(stretchElapsed = m - delta, alarmAtElapsed = m + (s.alarmAtEpoch - e)), emptyList(), false, false))
        }
    }
    @Synchronized fun start(kind: String, project: Rec?, task: Rec?) {
        val current = store.snap()
        if (current.status == "paused") { store.apply(resumeSnap(current, epoch(), elapsed(), id())); return }
        if (current.status == "running") return
        fun number(key: String, fallback: String) = store.pref(key, fallback).toLongOrNull() ?: fallback.toLong()
        fun duration(projectKey: String, prefKey: String, fallback: String) =
            (project?.f(projectKey)?.toLongOrNull() ?: number(prefKey, fallback)).coerceIn(1, 356400) * 1000
        val base = withSettings(current, duration("workSec", "work_sec", "3600"), duration("shortSec", "short_sec", "300"),
            duration("longSec", "long_sec", "900"), (project?.f("shortCount")?.toIntOrNull() ?: number("short_count", "2").toInt()).coerceIn(0,24),
            (project?.f("longCount")?.toIntOrNull() ?: number("long_count", "0").toInt()).coerceIn(0,24))
        val tuned = if (current.status == "awaiting") base else base.copy(stretchIndex = 0, focusDone = 0,
            countUp = store.pref("count_up", "0") == "1", autoStart = store.pref("auto_start", "0") == "1")
        val hint = if (current.status == "awaiting") StartHint(current.projectId,current.projectName,current.groupName,current.taskId,current.taskTitle,store.deviceId(),id(),id())
            else StartHint(project?.id, project?.f("name").orEmpty(), "", task?.id, task?.f("title").orEmpty(), store.deviceId(), id(), id())
        store.apply(arm(tuned, if (current.status == "awaiting") current.awaitingKind else kind, epoch(), elapsed(), hint))
    }
    @Synchronized fun pause() { store.apply(pauseSnap(store.snap(), epoch(), elapsed())) }
    @Synchronized fun finish(mode: String) { store.apply(finishSnap(store.snap(), epoch(), elapsed(), mode, id(), id())) }
    @Synchronized fun tick(): Boolean {
        if (!due(store.snap(), epoch(), elapsed())) return false
        store.apply(finishSnap(store.snap(), epoch(), elapsed(), "due", id(), id()))
        return true
    }
    fun display(): Long {
        val s = store.snap()
        return if (s.status == "idle") stretchLengths(store.pref("work_sec", "3600").toLongOrNull()?.times(1000) ?: DEFAULT_WORK_MS,
            stretchCount(store.pref("short_count", "2").toIntOrNull() ?: 2, store.pref("long_count", "0").toIntOrNull() ?: 0)).first()
            else displayMillis(s, epoch(), elapsed())
    }
}
