package app.presence.core

import org.junit.Assert.*
import org.junit.Test
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TimerRegressionTest {
    private val hint = StartHint("p", "Read", "Study", "t", "Chapter", "a", "s", "token")
    private val plan = Snap(workMillis = 120_000, shortCount = 1, longCount = 0, shortMillis = 10_000)

    @Test fun lateAlarmDoesNotInventWorkOrPauses() {
        val started = arm(plan, "focus", 1_000, 1_000, hint).snap
        val finished = finishSnap(started, 3_601_000, 3_601_000, "due", "b", "bt")
        val closed = finished.writes.first()
        assertEquals(60_000L, sessionCountedMillis(closed))
        assertEquals("61000", closed.f("endedAt"))
        assertEquals("0", closed.f("pausedMs"))
        assertEquals(60_000L, elapsedWork(started, 3_601_000, 3_601_000))
        // The new phase begins now, rather than claiming unattended future focus.
        assertEquals(3_601_000L, finished.snap.sessionStartedEpoch)
    }

    @Test fun lateAlarmPreservesRealPauseDuration() {
        val started = arm(plan, "focus", 1_000, 1_000, hint).snap
        val paused = pauseSnap(started, 11_000, 11_000).snap
        val resumed = resumeSnap(paused, 31_000, 31_000, "resume").snap
        val done = finishSnap(resumed, 100_000, 100_000, "due", "b", "bt")
        assertEquals(60_000L, sessionCountedMillis(done.writes.first()))
        assertEquals("20000", done.writes.first().f("pausedMs"))
        assertEquals("81000", done.writes.first().f("endedAt"))
    }

    @Test fun autoContinueStartsFocusAfterRestAndStopsAtPlanEnd() {
        val started = arm(plan.copy(autoStart = true), "focus", 1_000, 1_000, hint).snap
        val rest = finishSnap(started, 61_000, 61_000, "due", "rest", "rt").snap
        val focus = finishSnap(rest, 71_000, 71_000, "due", "focus2", "ft")
        assertEquals("running", focus.snap.status)
        assertEquals("focus", focus.snap.kind)
        assertEquals(1, focus.snap.stretchIndex)
        assertEquals("t", focus.snap.taskId)
        val end = finishSnap(focus.snap, 131_000, 131_000, "due", "unused", "unused")
        assertEquals("idle", end.snap.status)
        assertEquals(1, end.writes.size)
        assertTrue(end.chime)
    }

    @Test fun stopCancelsWaitingCycleAndNextStartBeginsAtFirstStretch() {
        val started = arm(plan, "focus", 1_000, 1_000, hint).snap
        val rest = finishSnap(started, 61_000, 61_000, "due", "rest", "rt").snap
        val waiting = finishSnap(rest, 71_000, 71_000, "due", "f2", "ft").snap
        val stop = finishSnap(waiting, 80_000, 80_000, "stop", "unused", "unused")
        assertEquals("idle", stop.snap.status)
        assertEquals(0, stop.snap.stretchIndex)
        assertTrue(stop.writes.isEmpty())
    }

    @Test fun countUpDoesNotChangeCompletionOrRemainingTime() {
        val started = arm(plan.copy(countUp = true), "focus", 1_000, 1_000, hint).snap
        assertEquals(10_000L, displayMillis(started, 11_000, 11_000))
        assertEquals(50_000L, timeLeft(started, 11_000, 11_000))
        val paused = pauseSnap(started, 11_000, 11_000).snap
        assertEquals(10_000L, displayMillis(paused, 31_000, 31_000))
        assertTrue(due(started, 61_000, 61_000))
    }

    @Test fun syncIncludesOnlySharedSettingsAndClosedSessions() {
        fun pref(id: String) = Rec(KIND_PREF, id, 1, "a", false, mapOf("value" to "1"))
        val opened = arm(plan, "focus", 1_000, 1_000, hint).writes.single()
        val closed = manualSession("closed", "a", "p", "Read", "", "focus", 1_000, 60_000, 61_000)
        val shared = listOf(pref("work_sec"), pref("auto_start"), pref("daily_on"), pref("alarm"), opened, closed).forSync()
        assertEquals(setOf("work_sec", "auto_start", "closed"), shared.map { it.id }.toSet())
    }

    @Test fun publicAddressesAreRejected() {
        assertFalse(isLocalAddress(InetAddress.getByName("8.8.8.8")))
        assertTrue(isLocalAddress(InetAddress.getByName("192.168.1.10")))
        assertTrue(isLocalAddress(InetAddress.getByName("127.0.0.1")))
        assertTrue(isLocalAddress(InetAddress.getByName("fd00::1")))
    }

    @Test fun simultaneousSyncsDoNotDeadlock() {
        val a = MemoryCatalog("a", "A")
        val b = MemoryCatalog("b", "B")
        val an = Node(a) { 1_000 }
        val bn = Node(b) { 1_000 }
        val pool = Executors.newFixedThreadPool(2)
        an.start(); bn.start()
        try {
            an.beginOffer(emptySet(), { 42 })
            assertTrue(bn.pair("127.0.0.1", an.port, "0042").ok)
            a.put(Rec(KIND_PROJECT, "pa", 1, "a", false, mapOf("name" to "A")))
            b.put(Rec(KIND_PROJECT, "pb", 1, "b", false, mapOf("name" to "B")))
            val first = pool.submit<SyncResult> { an.sync("127.0.0.1", bn.port, "b") }
            val second = pool.submit<SyncResult> { bn.sync("127.0.0.1", an.port, "a") }
            assertTrue(first.get(4, TimeUnit.SECONDS).ok)
            assertTrue(second.get(4, TimeUnit.SECONDS).ok)
            assertEquals(2, a.snapshot().size)
            assertEquals(2, b.snapshot().size)
        } finally { an.close(); bn.close(); pool.shutdownNow() }
    }
    @Test fun ambientSoundsHaveNoLargeDcOffset() {
        for (pcm in listOf(Tones.rain(), Tones.brook(), Tones.hearth())) {
            assertTrue(kotlin.math.abs(pcm.map { it.toDouble() }.average()) < 1_000)
            assertTrue(pcm.any { it > 0 })
            assertTrue(pcm.any { it < 0 })
        }
    }

    @Test fun malformedFramesAreRejectedWithoutChangingRecords() {
        try {
            recordsFromJson(org.json.JSONObject().put("t", "unrelated"))
            fail("An unrelated message must not be accepted as a sync snapshot")
        } catch (_: java.io.IOException) { }
    }

    @Test fun manualEntriesCannotDoubleCountExistingFocus() {
        val rec = manualSession("m", "a", "p", "Read", "", "focus", 10_000, 60_000, 70_000)
        assertTrue(overlapsClosed(listOf(rec), 20_000, 10_000))
        assertTrue(overlapsClosed(listOf(rec), 0, 80_000))
        assertFalse(overlapsClosed(listOf(rec), 70_000, 10_000))
        assertFalse(overlapsClosed(listOf(rec.copy(deleted = true)), 20_000, 10_000))
        assertFalse(overlapsClosed(listOf(rec.copy(body = rec.body + ("kind" to "short"))), 20_000, 10_000))
    }

}
