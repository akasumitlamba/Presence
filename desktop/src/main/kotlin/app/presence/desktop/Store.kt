package app.presence.desktop

import app.presence.core.*
import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.*
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** One atomic file, including local state and pairing credentials. Never sync local records. */
class Store(initialDirectory: Path = activeDataDirectory()) : Catalog {
    var directory: Path = initialDirectory
        private set
    private var rows: List<Rec>
    private val changes = MutableStateFlow<List<Rec>>(emptyList())
    private val storageState = MutableStateFlow(StorageStatus())
    @Volatile var revision = 0L
        private set
    private val file get() = directory.resolve("presence.json")
    init {
        if (initialDirectory != dataDirectory() && !Files.exists(initialDirectory.resolve("presence.json"))) {
            // Explicit test/new stores are allowed; a configured removable location must exist.
            val pointer = dataDirectory().resolve("storage-location")
            if (Files.exists(pointer) && Files.readString(pointer).trim() == initialDirectory.toString()) error("The selected data drive is unavailable. Reconnect it and reopen Presence.")
        }
        Files.createDirectories(directory)
        rows = if (Files.exists(file)) {
            val array = JSONObject(Files.readString(file)).getJSONArray("items")
            List(array.length()) { recFromJson(array.getJSONObject(it)) }
        } else emptyList()
        if (rows.none { it.kind == KIND_LOCAL && it.id == "device" }) {
            val id = UUID.randomUUID().toString()
            put(Rec(KIND_LOCAL, "device", System.currentTimeMillis(), id, false,
                mapOf("id" to id, "name" to "Presence • ${System.getProperty("os.name")}")))
        }
        val existing = rows.filter { it.kind == KIND_PREF }.map { it.id }.toSet()
        val now = System.currentTimeMillis()
        val missing = ANDROID_DEFAULT_PREFS.filterKeys { it !in existing }.map { (key,value) -> Rec(KIND_PREF,key,now,deviceId(),false,mapOf("value" to value)) }
        if(missing.isNotEmpty()) put(*missing.toTypedArray())
        if(rows.none { it.kind == KIND_LOCAL && it.id == "flags" }) write(KIND_LOCAL,"flags",mapOf("theme" to "system","palette" to "grove","language" to "system","text" to "standard"))
        changes.value = rows
    }
    fun observe(): StateFlow<List<Rec>> = changes
    fun storage(): StateFlow<StorageStatus> = storageState
    fun refreshStorage() { storageState.value = StorageStatus(cardMissing = !Files.exists(directory)) }
    @Synchronized override fun snapshot() = rows.toList()
    @Synchronized override fun deviceId() = rows.first { it.kind == KIND_LOCAL && it.id == "device" }.f("id")
    @Synchronized override fun deviceName() = rows.first { it.kind == KIND_LOCAL && it.id == "device" }.f("name")
    @Synchronized override fun tokenFor(deviceId: String) = rows.find { it.kind == KIND_LOCAL && it.id == "peer:$deviceId" && !it.deleted }?.f("token")
    @Synchronized override fun rememberPeer(deviceId: String, name: String, token: String) {
        write(KIND_LOCAL, "peer:$deviceId", mapOf("name" to name, "token" to token, "lastSync" to System.currentTimeMillis().toString()))
    }
    @Synchronized override fun mergeFrom(incoming: List<Rec>) {
        val snap = snap()
        var merged = mergeRecords(rows, incoming, if (sessionOpen(snap)) setOfNotNull(snap.sessionId) else emptySet(), emptySet())
        if(merged.find { it.kind == KIND_PREF && it.id == "alarm" }?.f("value") == "custom" && !Files.exists(directory.resolve("alarm-custom"))) {
            val previous = rows.find { it.kind == KIND_PREF && it.id == "alarm" } ?: Rec(KIND_PREF,"alarm",0,deviceId(),false,mapOf("value" to "chime"))
            merged = merged.map { if(it.kind == KIND_PREF && it.id == "alarm") previous else it }
        }
        persist(merged)
    }
    @Synchronized fun put(vararg records: Rec) {
        val keys = records.map { it.kind to it.id }.toSet()
        persist(rows.filter { it.kind to it.id !in keys } + records)
    }
    @Synchronized fun write(kind: String, id: String = UUID.randomUUID().toString(), body: Map<String, String>, deleted: Boolean = false) {
        put(Rec(kind, id, maxOf(System.currentTimeMillis(), (rows.maxOfOrNull { it.updatedAt } ?: 0) + 1), deviceId(), deleted, body))
    }
    @Synchronized fun pref(key: String, fallback: String) = rows.find { it.kind == KIND_PREF && it.id == key && !it.deleted }?.f("value") ?: fallback
    @Synchronized fun flag(key: String, fallback: String) = rows.find { it.kind == KIND_LOCAL && it.id == "flags" }?.f(key)?.ifEmpty { fallback } ?: fallback
    @Synchronized fun setFlag(key: String, value: String) {
        write(KIND_LOCAL, "flags", (rows.find { it.kind == KIND_LOCAL && it.id == "flags" }?.body ?: emptyMap()) + (key to value))
    }
    @Synchronized fun snap() = rows.find { it.kind == KIND_LOCAL && it.id == "snapshot" }?.let { snapFrom(it.body) } ?: Snap()
    @Synchronized fun apply(step: Step) {
        val rec = Rec(KIND_LOCAL, "snapshot", System.currentTimeMillis(), deviceId(), false, step.snap.toFields())
        put(*(step.writes + rec).toTypedArray())
    }
    private fun persist(next: List<Rec>) {
        val array = JSONArray()
        next.forEach { array.put(recToJson(it)) }
        val temp = Files.createTempFile(directory, "presence-", ".tmp")
        try {
            Files.writeString(temp, JSONObject().put("items", array).toString())
            try { Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING) }
            catch (_: AtomicMoveNotSupportedException) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING) }
            rows = next
            revision++
            changes.value = next
        } finally { Files.deleteIfExists(temp) }
    }

    fun currentSnap() = snap()
    fun saveSnap(snap: Snap) { write(KIND_LOCAL, "snapshot", snap.toFields()) }
    fun putAll(records: List<Rec>) { if (records.isNotEmpty()) put(*records.toTypedArray()) }
    fun setPref(key: String, value: String) { write(KIND_PREF, key, mapOf("value" to value)) }
    @Synchronized fun updateFlags(block: (MutableMap<String,String>) -> Unit) {
        val flags = snapshot().find { it.kind == KIND_LOCAL && it.id == "flags" }?.body.orEmpty().toMutableMap()
        block(flags); write(KIND_LOCAL, "flags", flags)
    }
    fun writerNow(kind: String, id: String, body: Map<String,String>, deleted: Boolean = false) = Rec(kind,id,System.currentTimeMillis(),deviceId(),deleted,body)
    fun addTodo(title: String) { if (title.isNotBlank()) write(KIND_LOCAL,"todo:${UUID.randomUUID()}",mapOf("title" to title.trim(), "done" to "0", "sort" to System.currentTimeMillis().toString())) }
    fun setTodoDone(id: String, done: Boolean) { snapshot().find { it.kind == KIND_LOCAL && it.id == "todo:$id" }?.let { write(it.kind,it.id,it.body + ("done" to if (done) "1" else "0")) } }
    fun deleteTodo(id: String) { snapshot().find { it.kind == KIND_LOCAL && it.id == "todo:$id" }?.let { write(it.kind,it.id,it.body,true) } }
    fun forgetPeer(id: String) { snapshot().find { it.kind == KIND_LOCAL && it.id == "peer:$id" }?.let { write(it.kind,it.id,it.body,true) } }
    fun touchPeer(id: String) { snapshot().find { it.kind == KIND_LOCAL && it.id == "peer:$id" && !it.deleted }?.let { write(it.kind,it.id,it.body + ("lastSync" to System.currentTimeMillis().toString())) } }
    @Synchronized fun resetSettings() {
        val now = System.currentTimeMillis()
        put(*ANDROID_RESET_PREFS.map { (key,value) -> Rec(KIND_PREF,key,now,deviceId(),false,mapOf("value" to value)) }.toTypedArray())
        updateFlags { it["theme"] = "system"; it["palette"] = "grove" }
    }
    @Synchronized fun globals(): DesktopGlobals {
        fun n(key: String, fallback: Int) = pref(key,fallback.toString()).toIntOrNull() ?: fallback
        fun sec(key: String, fallback: Long) = (pref(key,fallback.toString()).toLongOrNull() ?: fallback).coerceIn(1,356400) * 1000
        return DesktopGlobals(n("focus_min",25).coerceIn(1,180),n("short_min",5).coerceIn(1,60),n("long_min",15).coerceIn(1,120),n("cycles",4).coerceIn(1,12),pref("count_up","0") == "1",pref("auto_start","0") == "1",n("day_start",0).coerceIn(0,6),pref("alarm","chime"),pref("background","off"),n("bg_volume",40).coerceIn(0,100),pref("daily_on","0") == "1",n("daily_hour",9).coerceIn(0,23),n("daily_minute",0).coerceIn(0,59),pref("streak_on","0") == "1",n("streak_hour",20).coerceIn(0,23),n("streak_minute",0).coerceIn(0,59),sec("work_sec",3600),n("short_count",2).coerceIn(0,24),n("long_count",0).coerceIn(0,24),sec("short_sec",300),sec("long_sec",900))
    }


    /** Move data only into an empty folder; switch the pointer after copying and verifying. */
    @Synchronized fun relocate(destination: Path, rememberLocation: Boolean = true) {
        val target = destination.toAbsolutePath().normalize()
        if (target == directory.toAbsolutePath().normalize()) return
        Files.createDirectories(target)
        val next = target.resolve("presence.json")
        require(!Files.exists(next)) { "This folder already contains Presence data. Choose an empty folder or import its backup." }
        val source = directory
        val bytes = Files.readAllBytes(file)
        val temporary = Files.createTempFile(target,"presence-move-",".tmp")
        try {
            Files.write(temporary,bytes)
            check(Files.readAllBytes(temporary).contentEquals(bytes)) { "Could not verify copied data" }
            try { Files.move(temporary,next,StandardCopyOption.ATOMIC_MOVE) }
            catch(_: AtomicMoveNotSupportedException) { Files.move(temporary,next) }
            val custom = source.resolve("alarm-custom")
            if(Files.exists(custom)) Files.copy(custom,target.resolve("alarm-custom"),StandardCopyOption.REPLACE_EXISTING)
            if(rememberLocation) {
                val base = dataDirectory(); Files.createDirectories(base)
                val pointer = Files.createTempFile(base,"storage-",".tmp")
                try { Files.writeString(pointer,target.toString()); try { Files.move(pointer,base.resolve("storage-location"),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE) } catch(_: AtomicMoveNotSupportedException) { Files.move(pointer,base.resolve("storage-location"),StandardCopyOption.REPLACE_EXISTING) } }
                finally { Files.deleteIfExists(pointer) }
            }
            directory = target
            // Keep the previous copy as a recovery backup; it is no longer opened automatically.
            revision++; changes.value = rows.toList()
            storageState.value = StorageStatus(onCard = target != dataDirectory())
        } finally { Files.deleteIfExists(temporary) }
    }

    companion object {
        fun activeDataDirectory(): Path {
            val base = dataDirectory(); val pointer = base.resolve("storage-location")
            return if(Files.exists(pointer)) Paths.get(Files.readString(pointer).trim()) else base
        }
        fun dataDirectory(): Path {
            val os = System.getProperty("os.name").lowercase()
            val base = if (os.contains("win")) System.getenv("APPDATA") ?: System.getProperty("user.home")
                else System.getenv("XDG_DATA_HOME") ?: "${System.getProperty("user.home")}/.local/share"
            return Paths.get(base, "presence")
        }
    }
}
