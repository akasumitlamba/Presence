package app.presence.timer

import app.presence.core.KIND_GROUP
import app.presence.core.KIND_PROJECT
import app.presence.core.Rec
import app.presence.core.Snap
import app.presence.core.StartHint
import app.presence.core.Step
import app.presence.core.arm
import app.presence.core.due
import app.presence.core.f
import app.presence.core.finishSnap
import app.presence.core.pauseSnap
import app.presence.core.resumeSnap
import app.presence.core.withSettings
import app.presence.data.Repo
import java.util.UUID

class TimerCenter(
    private val repo: Repo,
    private val react: (Step) -> Unit,
) {
    private val lock = Any()

    fun snap(): Snap = repo.currentSnap()

    fun start(kind: String, project: Rec?, task: Rec?) {
        synchronized(lock) {
            val current = repo.currentSnap()
            if (current.status == "running" || current.status == "paused") return
            val resolved = project ?: current.projectId?.let { id ->
                repo.snapshot().find { it.kind == KIND_PROJECT && it.id == id && !it.deleted }
            }
            val tuned = tune(current, resolved)
            val nowE = System.currentTimeMillis()
            val nowL = android.os.SystemClock.elapsedRealtime()
            val hint = if (resolved == null && current.projectId != null) {
                StartHint(
                    current.projectId,
                    current.projectName,
                    current.groupName,
                    current.taskId,
                    current.taskTitle,
                    repo.deviceId(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                )
            } else {
                hint(resolved, task)
            }
            val step = arm(tuned, kind, nowE, nowL, hint)
            apply(step)
        }
    }

    fun pause() = synchronized(lock) {
        val nowE = System.currentTimeMillis()
        val nowL = android.os.SystemClock.elapsedRealtime()
        apply(pauseSnap(repo.currentSnap(), nowE, nowL))
    }

    fun resume() = synchronized(lock) {
        val nowE = System.currentTimeMillis()
        val nowL = android.os.SystemClock.elapsedRealtime()
        apply(resumeSnap(repo.currentSnap(), nowE, nowL, UUID.randomUUID().toString()))
    }

    fun stop() = end("stop")

    fun skip() = end("skip")

    fun due(token: String?): Boolean = synchronized(lock) {
        val current = repo.currentSnap()
        val nowE = System.currentTimeMillis()
        val nowL = android.os.SystemClock.elapsedRealtime()
        if (!due(current, nowE, nowL)) return false
        if (!token.isNullOrEmpty() && token != current.completionToken) return false
        apply(finishSnap(current, nowE, nowL, "due", UUID.randomUUID().toString(), UUID.randomUUID().toString()))
        true
    }

    fun catchUp() {
        synchronized(lock) {
            val current = repo.currentSnap()
            val nowE = System.currentTimeMillis()
            val nowL = android.os.SystemClock.elapsedRealtime()
            if (current.status == "running" && due(current, nowE, nowL)) {
                apply(finishSnap(current, nowE, nowL, "due", UUID.randomUUID().toString(), UUID.randomUUID().toString()))
            } else {
                react(Step(current, emptyList(), alarm = current.status == "running" && current.kind != "track", chime = false))
            }
        }
    }

    fun refreshSettings() {
        synchronized(lock) {
            val current = repo.currentSnap()
            val globals = repo.globals()
            val next = withSettings(current, globals.focus, globals.short, globals.long, globals.cycles, globals.countUp, globals.auto)
            if (next != current) {
                repo.saveSnap(next)
                react(Step(next, emptyList(), alarm = false, chime = false))
            }
        }
    }

    private fun end(mode: String) {
        synchronized(lock) {
            val nowE = System.currentTimeMillis()
            val nowL = android.os.SystemClock.elapsedRealtime()
            apply(finishSnap(repo.currentSnap(), nowE, nowL, mode, UUID.randomUUID().toString(), UUID.randomUUID().toString()))
        }
    }

    private fun apply(step: Step) {
        if (step.writes.isNotEmpty()) repo.putAll(step.writes)
        repo.saveSnap(step.snap)
        react(step)
    }

    private fun tune(snap: Snap, project: Rec?): Snap {
        val globals = repo.globals()
        var focus = globals.focus
        var short = globals.short
        var long = globals.long
        var cycles = globals.cycles
        var countUp = globals.countUp
        if (repo.premium() && project != null) {
            project.f("focus").toIntOrNull()?.let { focus = it.coerceIn(1, 180) }
            project.f("short").toIntOrNull()?.let { short = it.coerceIn(1, 60) }
            project.f("long").toIntOrNull()?.let { long = it.coerceIn(1, 120) }
            project.f("cycles").toIntOrNull()?.let { cycles = it.coerceIn(1, 12) }
            if (project.f("countUp").isNotEmpty()) countUp = project.f("countUp") == "1"
        }
        return withSettings(snap, focus, short, long, cycles, countUp, globals.auto)
    }

    private fun hint(project: Rec?, task: Rec?): StartHint {
        val groupName = project?.let { item ->
            repo.snapshot().find { it.kind == KIND_GROUP && it.id == item.f("groupId") }?.f("name")
        }.orEmpty()
        return StartHint(
            projectId = project?.id,
            projectName = project?.f("name").orEmpty(),
            groupName = groupName,
            taskId = task?.id,
            taskTitle = task?.f("title").orEmpty(),
            writerId = repo.deviceId(),
            sessionId = UUID.randomUUID().toString(),
            token = UUID.randomUUID().toString(),
        )
    }
}
