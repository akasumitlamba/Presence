package app.presence.desktop

import app.presence.core.*

fun monotonicMillis() = System.nanoTime() / 1_000_000

fun reanchorTimer(store: Store) {
    val s = store.snap()
    if (s.status == "running") {
        val e = System.currentTimeMillis(); val m = monotonicMillis()
        val delta = (e - s.stretchEpoch).coerceAtLeast(0)
        store.saveSnap(s.copy(stretchElapsed = m - delta, alarmAtElapsed = m + (s.alarmAtEpoch - e)))
    }
}

/** Test-friendly facade around Android's generated, otherwise unchanged TimerCenter. */
class DesktopTimer(private val store: Store) {
    init { reanchorTimer(store) }
    private val center = TimerCenter(store) { }
    fun start(kind: String, project: Rec?, task: Rec?) { if (store.snap().status == "paused") center.resume() else center.start(kind,project,task) }
    fun pause() = center.pause()
    fun finish(mode: String) { if (mode == "skip") center.skip() else center.stop() }
    fun tick() = center.due(null)
    fun display() = displayMillis(store.snap(),System.currentTimeMillis(),monotonicMillis())
}
