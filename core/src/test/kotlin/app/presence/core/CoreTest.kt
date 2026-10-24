package app.presence.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class CoreTest {
    private val zone: ZoneId = ZoneId.of("UTC")

    @Test
    fun codeAvoidsCodesAlreadyOnOffer() {
        var n = 0
        assertEquals("0000", chooseCode(setOf("0001")) { n++ })
        var r = 2
        assertEquals("0004", rotateCode("0002", setOf("0002", "0003")) { r++ })
        assertEquals("0002", rotateCode("0002", setOf("0009")) { 0 })
    }

    @Test
    fun badAndExpiredCodesAreDistinct() {
        assertEquals(CodeStatus.Match, codeStatus("0042", "0042", 1_000, 500))
        assertEquals(CodeStatus.Wrong, codeStatus("0001", "0042", 1_000, 500))
        assertEquals(CodeStatus.Expired, codeStatus("0042", "0042", 1_000, 1_000))
        assertEquals(CodeStatus.Wrong, codeStatus("0001", "0042", 1_000, 5_000))
        assertEquals(CodeStatus.NotOffering, codeStatus("0042", null, 0, 10))
    }

    @Test
    fun lastWriteWinsAndTiesUseWriterId() {
        val older = Rec(KIND_PROJECT, "p", 10, "a", false, mapOf("name" to "Old"))
        val newer = Rec(KIND_PROJECT, "p", 11, "b", false, mapOf("name" to "New"))
        val tieLow = Rec(KIND_PROJECT, "p", 11, "a", false, mapOf("name" to "A"))
        val tieHigh = Rec(KIND_PROJECT, "p", 11, "m", false, mapOf("name" to "M"))
        assertEquals("New", mergeRecords(listOf(older), listOf(newer), emptySet(), emptySet()).single().f("name"))
        assertEquals("M", mergeRecords(listOf(tieLow), listOf(tieHigh), emptySet(), emptySet()).single().f("name"))
        val tomb = newer.copy(deleted = true, updatedAt = 12)
        assertTrue(mergeRecords(listOf(newer), listOf(tomb), emptySet(), emptySet()).single().deleted)
    }

    @Test
    fun openSessionIsProtectedAndNotCountedTwice() {
        val open = Rec(
            KIND_SESSION, "s", 5, "phone-a", false,
            mapOf("kind" to "focus", "startedAt" to "1000", "endedAt" to "", "pausedMs" to "0", "projectId" to "p"),
        )
        val clobber = open.copy(updatedAt = 99, writerId = "phone-b", body = open.body + ("endedAt" to "9000"))
        val kept = mergeRecords(listOf(open), listOf(clobber), setOf("s"), emptySet()).single()
        assertEquals("", kept.f("endedAt"))
        assertEquals(0L, sessionCountedMillis(kept))
        val closed = open.copy(updatedAt = 20, body = open.body + ("endedAt" to "61000") + ("pausedMs" to "0"))
        assertEquals(60_000L, sessionCountedMillis(closed))
        val twice = listOf(closed, closed.copy(updatedAt = 21))
        val merged = mergeRecords(listOf(closed), listOf(closed.copy(updatedAt = 21, body = closed.body)), emptySet(), emptySet())
        assertEquals(1, merged.count { it.id == "s" })
        assertEquals(60_000L, merged.sumOf { sessionCountedMillis(it) })
        assertEquals(60_000L, twice.distinctBy { it.id }.sumOf { sessionCountedMillis(it) })
    }

    @Test
    fun runningTimerDefersLengthChanges() {
        val local = Rec(KIND_PREF, "focus_min", 10, "a", false, mapOf("value" to "25"))
        val remote = Rec(KIND_PREF, "focus_min", 50, "b", false, mapOf("value" to "50"))
        val held = mergeRecords(listOf(local), listOf(remote), emptySet(), TIMER_PREF_IDS).single()
        assertEquals("25", held.f("value"))
        val applied = mergeRecords(listOf(local), listOf(remote), emptySet(), emptySet()).single()
        assertEquals("50", applied.f("value"))
    }

    @Test
    fun dailyTargetShrinksAsWorkIsLogged() {
        val now = ZonedDateTime.of(2026, 10, 23, 9, 0, 0, 0, zone).toInstant().toEpochMilli()
        val deadline = ZonedDateTime.of(2026, 11, 1, 18, 0, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(60, dailyTargetMinutes(600, 0, deadline, now, zone, 0))
        assertEquals(50, dailyTargetMinutes(600, 100 * 60_000L, deadline, now, zone, 0))
        val yesterday = ZonedDateTime.of(2026, 10, 22, 12, 0, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(600, dailyTargetMinutes(600, 0, yesterday, now, zone, 0))
    }

    @Test
    fun streakCountsClosedDaysAndSkipsAGap() {
        val day = LocalDate.of(2026, 10, 23)
        fun kept(date: LocalDate) = manualSession(
            date.toString(), "phone", "p", "Read", "Personal", "focus",
            date.atTime(10, 0).atZone(zone).toInstant().toEpochMilli(),
            60_000,
            date.atTime(11, 0).atZone(zone).toInstant().toEpochMilli(),
        )
        val now = day.atTime(18, 0).atZone(zone).toInstant().toEpochMilli()
        assertEquals(3, streakDays(listOf(kept(day), kept(day.minusDays(1)), kept(day.minusDays(2))), now, zone, 0))
        assertEquals(2, streakDays(listOf(kept(day.minusDays(1)), kept(day.minusDays(2))), now, zone, 0))
        assertEquals(0, streakDays(listOf(kept(day.minusDays(2))), now, zone, 0))
        assertTrue(loggedToday(listOf(kept(day)), now, zone, 0))
        val morning = day.atTime(8, 0).atZone(zone).toInstant().toEpochMilli()
        val next = nextClockMillis(9, 30, morning, zone)
        assertEquals(day.atTime(9, 30).atZone(zone).toInstant().toEpochMilli(), next)
        val evening = day.atTime(21, 0).atZone(zone).toInstant().toEpochMilli()
        assertEquals(day.plusDays(1).atTime(9, 30).atZone(zone).toInstant().toEpochMilli(), nextClockMillis(9, 30, evening, zone))
    }

    @Test
    fun dayStartsAtTwoInTheMorning() {
        val oneThirty = ZonedDateTime.of(2026, 10, 23, 1, 30, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(LocalDate.of(2026, 10, 22), dayKey(oneThirty, zone, 2))
        assertEquals(LocalDate.of(2026, 10, 23), dayKey(oneThirty, zone, 0))
    }

    @Test
    fun cycleReachesALongRestOnTheFourthFocus() {
        var done = 0
        var kind = "focus"
        val seen = mutableListOf<String>()
        repeat(4) {
            val next = nextAfter(kind, done, 4)
            kind = next.first
            done = next.second
            seen += kind
            if (kind != "focus") {
                val back = nextAfter(kind, done, 4)
                kind = back.first
                done = back.second
            }
        }
        assertEquals(listOf("short", "short", "short", "long"), seen)
        assertEquals(0, done)
    }

    @Test
    fun pauseKeepsTheRemainderAndStopResetsTheCycle() {
        val hint = StartHint("p", "Calculus", "Study", null, "", "phone", "sess", "tok")
        val armed = arm(Snap(focusMin = 25, cycles = 4), "focus", 1_000, 1_000, hint)
        val later = pauseSnap(armed.snap, 1_000 + 1_000, 1_000 + 1_000)
        assertEquals("paused", later.snap.status)
        assertEquals(25 * 60_000L - 1_000, timeLeft(later.snap, 1_000 + 1_000, 1_000 + 1_000))
        val resumed = resumeSnap(later.snap, 1_000 + 2_000, 1_000 + 2_000, "tok2")
        assertEquals(25 * 60_000L - 1_000, timeLeft(resumed.snap, 1_000 + 2_000, 1_000 + 2_000))
        val stopped = finishSnap(resumed.snap, 1_000 + 2_400, 1_000 + 2_400, "stop", "n", "t")
        assertEquals("idle", stopped.snap.status)
        assertEquals(0, stopped.snap.focusDone)
        assertTrue(stopped.writes.single().deleted)
    }

    @Test
    fun skipCountsAndAFullPhaseCanAutoContinue() {
        val hint = StartHint("p", "Calculus", "Study", null, "", "phone", "sess", "tok")
        val armed = arm(Snap(focusMin = 1, cycles = 4, autoStart = false), "focus", 0, 0, hint)
        val end = armed.snap.alarmAtEpoch
        val done = finishSnap(armed.snap, end, end, "due", "next", "tok2")
        assertTrue(done.chime)
        assertEquals("awaiting", done.snap.status)
        assertEquals("short", done.snap.awaitingKind)
        assertEquals(1, done.snap.focusDone)
        assertFalse(done.writes.single().deleted)
        val auto = arm(Snap(focusMin = 1, cycles = 4, autoStart = true), "focus", 0, 0, hint)
        val jumped = finishSnap(auto.snap, auto.snap.alarmAtEpoch, auto.snap.alarmAtElapsed, "due", "next", "tok3")
        assertEquals("running", jumped.snap.status)
        assertEquals("short", jumped.snap.kind)
        assertEquals(2, jumped.writes.size)
    }

    @Test
    fun rebootUsesTheWallClock() {
        val hint = StartHint(null, "", "", null, "", "phone", "sess", "tok")
        val armed = arm(Snap(focusMin = 25), "focus", 5_000, 5_000, hint)
        val left = timeLeft(armed.snap, 5_000 + 10_000, 80)
        assertEquals(25 * 60_000L - 10_000, left)
        assertTrue(due(armed.snap, armed.snap.alarmAtEpoch + 5, 10))
    }

    @Test
    fun percentsAddToOneHundred() {
        assertEquals(100, wholePercents(listOf(1, 1, 1)).sum())
        assertEquals(listOf(34, 33, 33), wholePercents(listOf(1, 1, 1)))
    }

    @Test
    fun csvEscapesCommas() {
        val rec = manualSession("m", "w", "p", "Design, UI", "Work", "focus", 60_000, 120_000, 200_000)
        val csv = sessionsCsv(listOf(rec), zone, 0)
        assertTrue(csv.contains("\"Design, UI\""))
        assertTrue(csv.lineSequence().count() == 2)
    }

    @Test
    fun snapRoundTrip() {
        val hint = StartHint("p", "Read", "Personal", "t", "Chapter", "phone", "sess", "tok")
        val snap = arm(Snap(countUp = true), "focus", 50, 80, hint).snap
        assertEquals(snap, snapFrom(snap.toFields()))
    }

    @Test
    fun pairingCodesStayWesternDigits() {
        val previous = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("ar"))
        try {
            assertEquals("0042", chooseCode(emptySet()) { 42 })
            assertEquals("0007", chooseCode(setOf("0042")) { 7 })
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun clockDigitsStayWesternDigits() {
        val previous = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("ar"))
        try {
            assertEquals("1:02:03", clockDigits((3600 + 120 + 3) * 1000L))
            assertEquals("4:05", clockDigits((4 * 60 + 5) * 1000L))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun pausedStretchStillBlocksManualOverlap() {
        val hint = StartHint("p", "Calculus", "Study", null, "", "phone", "sess", "tok")
        val armed = arm(Snap(focusMin = 25), "focus", 0L, 0L, hint)
        val runningNow = 10 * 60_000L
        assertTrue(overlapsOpen(armed.snap, 10 * 60_000L, 60_000L, runningNow))
        assertFalse(overlapsOpen(armed.snap, 26 * 60_000L, 60_000L, runningNow))
        val paused = pauseSnap(armed.snap, 60_000L, 60_000L).snap
        val now = 2 * 60 * 60_000L
        assertTrue(overlapsOpen(paused, 30 * 60_000L, 60_000L, now))
        assertFalse(overlapsOpen(paused, now + 25 * 60_000L, 60_000L, now))
    }

    @Test
    fun tonesAreAudibleLength() {
        assertTrue(Tones.chime().any { it != 0.toShort() })
        assertEquals(Tones.RATE, Tones.rain().size)
        assertEquals(Tones.RATE, Tones.brook().size)
        assertEquals(Tones.RATE, Tones.hearth().size)
    }

    @Test
    fun pairWrongExpiredAndThenSyncWithoutClobber() {
        val host = MemoryCatalog("host", "Host")
        val guest = MemoryCatalog("guest", "Guest")
        val hostNode = Node(host) { 1_000 }
        val guestNode = Node(guest) { 1_000 }
        hostNode.start()
        guestNode.start()
        try {
            hostNode.beginOffer(emptySet(), { 42 }, now = 1_000)
            val wrong = guestNode.pair("127.0.0.1", hostNode.port, "0000")
            assertEquals("wrong_code", wrong.reason)
            assertTrue(host.peers.isEmpty())
            hostNode.offerUntil = 1_000
            val expired = guestNode.pair("127.0.0.1", hostNode.port, hostNode.offerCode!!)
            assertEquals("expired", expired.reason)
            hostNode.beginOffer(emptySet(), { 7 }, now = 5_000)
            val guestClock = Node(guest) { 5_100 }
            val ok = guestClock.pair("127.0.0.1", hostNode.port, hostNode.offerCode!!)
            assertTrue(ok.ok)
            assertEquals(host.tokenFor("guest"), guest.tokenFor("host"))
            host.put(Rec(KIND_PROJECT, "p", 10, "host", false, mapOf("name" to "Calculus", "createdAt" to "1")))
            val open = Rec(
                KIND_SESSION, "live", 11, "guest", false,
                mapOf("kind" to "focus", "startedAt" to "5000", "endedAt" to "", "pausedMs" to "0", "projectId" to "p", "projectName" to "Calculus"),
            )
            guest.put(open)
            guest.protectedIds = setOf("live")
            val again = guestNode.sync("127.0.0.1", hostNode.port, "host")
            assertTrue(again.ok)
            assertEquals("Calculus", guest.snapshot().first { it.id == "p" }.f("name"))
            val hostile = open.copy(updatedAt = 99, writerId = "host", body = open.body + ("endedAt" to "99999"))
            host.put(hostile)
            guestNode.sync("127.0.0.1", hostNode.port, "host")
            val kept = guest.snapshot().first { it.id == "live" }
            assertEquals("", kept.f("endedAt"))
            assertEquals(0L, guest.snapshot().filter { it.id == "live" }.sumOf { sessionCountedMillis(it) })
        } finally {
            hostNode.close()
            guestNode.close()
        }
    }

    @Test
    fun droppedLinkDoesNotApplyAPartialMessage() {
        val host = MemoryCatalog("host", "Host")
        val guest = MemoryCatalog("guest", "Guest")
        host.put(Rec(KIND_PROJECT, "p", 3, "host", false, mapOf("name" to "Before")))
        val hostNode = Node(host) { 2_000 }
        hostNode.start()
        try {
            hostNode.beginOffer(emptySet(), { 9 }, now = 2_000)
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress("127.0.0.1", hostNode.port), 4000)
            val out = java.io.DataOutputStream(socket.getOutputStream())
            val hello = org.json.JSONObject()
                .put("t", "hello")
                .put("mode", "pair")
                .put("deviceId", "guest")
                .put("name", "Guest")
                .put("code", hostNode.offerCode)
            Frame.write(out, hello)
            val input = java.io.DataInputStream(socket.getInputStream())
            val reply = Frame.read(input)
            assertEquals("hello_ok", reply?.optString("t"))
            socket.close()
            Thread.sleep(100)
            assertEquals("Before", host.snapshot().single { it.id == "p" }.f("name"))
            assertNotEquals(null, guest.deviceId())
        } finally {
            hostNode.close()
        }
    }
}
