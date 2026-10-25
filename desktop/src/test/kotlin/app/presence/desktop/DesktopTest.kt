package app.presence.desktop

import app.presence.core.*
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files

class DesktopTest {
    private fun store() = Store(Files.createTempDirectory("presence-test"))
    @Test fun persistsIdentityRecordsAndPairingCredentials() {
        val a = store()
        a.write(KIND_PROJECT, "project", mapOf("name" to "Research", "goalMinutes" to "120", "goalDeadline" to "99999"))
        a.rememberPeer("phone", "Android", "secret")
        val reopened = Store(a.directory)
        assertEquals(a.deviceId(), reopened.deviceId())
        assertEquals("secret", reopened.tokenFor("phone"))
        assertEquals("120", reopened.snapshot().alive(KIND_PROJECT).single().f("goalMinutes"))
    }
    @Test fun desktopPairsAndSyncsBidirectionallyWithAndroidCoreProtocol() {
        val desktop = store()
        val android = MemoryCatalog("android", "Phone")
        val phone = Node(android, clock = System::currentTimeMillis)
        val pc = Node(desktop, clock = System::currentTimeMillis)
        phone.start()
        try {
            android.put(Rec(KIND_PROJECT, "phone-project", 100, "android", false, mapOf("name" to "Phone project")))
            desktop.write(KIND_TASK, "pc-task", mapOf("title" to "Desktop task", "projectId" to "phone-project"))
            desktop.write(KIND_LOCAL, "todo:local", mapOf("title" to "Private"))
            desktop.write(KIND_PREF, "work_sec", mapOf("value" to "7200"))
            desktop.write(KIND_PREF, "alarm", mapOf("value" to "off"))
            val code = phone.beginOffer(emptySet(), { 1234 })!!
            assertFalse(pc.pair("127.0.0.1", phone.port, "9999").ok)
            assertTrue(pc.pair("127.0.0.1", phone.port, code).ok)
            assertEquals("Phone project", desktop.snapshot().alive(KIND_PROJECT).single().f("name"))
            assertEquals("Desktop task", android.snapshot().alive(KIND_TASK).single().f("title"))
            assertEquals("7200", android.snapshot().find { it.kind == KIND_PREF && it.id == "work_sec" }?.f("value"))
            assertFalse(android.snapshot().any { it.kind == KIND_LOCAL || it.id == "alarm" })
            assertNull(phone.offerCode)
            android.put(Rec(KIND_PROJECT,"phone-project",System.currentTimeMillis()+10000,"android",true,emptyMap()))
            assertTrue(pc.sync("127.0.0.1", phone.port, "android").ok)
            assertTrue(desktop.snapshot().alive(KIND_PROJECT).isEmpty())
            assertFalse(pc.pair("127.0.0.1",phone.port,code).ok)
        } finally { phone.close(); pc.close() }
    }
    @Test fun incomingSyncCannotOverwriteAnActiveTimerOrLocalPreferences() {
        val a = store()
        val timer = DesktopTimer(a)
        timer.start("focus", null, null)
        val snap = a.snap()
        a.setFlag("palette", "harbor")
        a.mergeFrom(listOf(Rec(KIND_SESSION,snap.sessionId!!,Long.MAX_VALUE,"other",true,emptyMap()),
            Rec(KIND_LOCAL,"flags",Long.MAX_VALUE,"other",false,mapOf("palette" to "berry"))))
        assertEquals(snap, a.snap())
        assertFalse(a.snapshot().find { it.id == snap.sessionId }!!.deleted)
        assertEquals("harbor", a.flag("palette", "grove"))
        timer.pause()
        assertEquals("paused",a.snap().status)
        val reopened = Store(a.directory)
        assertEquals("paused",reopened.snap().status)
        DesktopTimer(reopened).start("focus",null,null)
        assertEquals("running",reopened.snap().status)
    }
    @Test fun movingStoragePreservesIdentityPeersAndTimerWithoutOverwritingExistingData() {
        val a = store()
        val id = a.deviceId()
        a.rememberPeer("phone","Phone","token")
        DesktopTimer(a).start("track",null,null)
        val target = Files.createTempDirectory("presence-removable")
        a.relocate(target,rememberLocation = false)
        assertEquals(id,Store(target).deviceId())
        assertEquals("token",Store(target).tokenFor("phone"))
        assertEquals("running",Store(target).snap().status)
        val other = store()
        val original = other.directory
        try { other.relocate(target,rememberLocation = false); fail("Existing data must not be overwritten") }
        catch(_: IllegalArgumentException) { assertEquals(original,other.directory) }
        assertEquals(id,Store(target).deviceId())
    }
    @Test fun backupImportRestoresLocalPreferencesButDoesNotClaimMissingCustomAudio() {
        val a = store()
        a.setPref("daily_on","1")
        a.setPref("alarm","custom")
        val b = store()
        val imported = decodeBackup(encodeBackup(a.snapshot()))!!
        val newer = b.snapshot().maxOf { it.updatedAt } + 1000
        b.mergeFrom(imported.map { it.copy(updatedAt = newer) })
        assertTrue(b.globals().dailyOn)
        assertEquals("chime",b.globals().alarm)
    }
    @Test fun projectPlanAndGlobalDefaultsMatchAndroidAndRemainFixedWhileRunning() {
        val a = store()
        a.setPref("work_sec","3600")
        val project = Rec(KIND_PROJECT,"p",1,"a",false,mapOf("name" to "Work", "workSec" to "1800","shortCount" to "1","longCount" to "0"))
        a.put(project)
        val center = TimerCenter(a) { }
        center.start("focus",project,null)
        assertEquals(900000L,a.snap().phaseMillis)
        a.setPref("work_sec","7200")
        center.refreshSettings()
        assertEquals(1800000L,a.snap().workMillis)
        center.pause()
        assertEquals("paused",a.snap().status)
        center.resume()
        assertEquals("running",a.snap().status)
    }
    @Test fun incompleteSessionsAreExcludedAndBackupsRemainAndroidCompatible() {
        val a = store()
        DesktopTimer(a).start("track", null, null)
        val records = decodeBackup(encodeBackup(a.snapshot()))!!
        assertTrue(records.any { it.kind == KIND_SESSION })
        assertTrue(records.forSync().none { it.kind == KIND_SESSION })
        val b = store()
        b.mergeFrom(records.forSync())
        assertTrue(b.snapshot().alive(KIND_SESSION).isEmpty())
        assertNotEquals(a.deviceId(), b.deviceId())
    }
}
