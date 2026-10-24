@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package app.presence.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import app.presence.core.Catalog
import app.presence.core.KIND_GROUP
import app.presence.core.KIND_LOCAL
import app.presence.core.KIND_PREF
import app.presence.core.Rec
import app.presence.core.Snap
import app.presence.core.f
import app.presence.core.mergeRecords
import app.presence.core.snapFrom
import app.presence.core.toFields
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.json.JSONObject

@Entity(tableName = "records", primaryKeys = ["kind", "id"])
data class RecEntity(
    val kind: String,
    val id: String,
    val updatedAt: Long,
    val writerId: String,
    val deleted: Boolean,
    val body: String,
)

@Dao
interface RecDao {
    @Query("SELECT * FROM records")
    fun observe(): Flow<List<RecEntity>>

    @Query("SELECT * FROM records")
    fun all(): List<RecEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(items: List<RecEntity>)
}

@Database(entities = [RecEntity::class], version = 1, exportSchema = false)
abstract class PresenceDb : RoomDatabase() {
    abstract fun recs(): RecDao
}

fun RecEntity.toRec(): Rec {
    val obj = JSONObject(body.ifEmpty { "{}" })
    val map = linkedMapOf<String, String>()
    val keys = obj.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        map[key] = obj.optString(key)
    }
    return Rec(kind, id, updatedAt, writerId, deleted, map)
}

fun Rec.toEntity(): RecEntity {
    val obj = JSONObject()
    body.forEach { (key, value) -> obj.put(key, value) }
    return RecEntity(kind, id, updatedAt, writerId, deleted, obj.toString())
}

data class Peer(val id: String, val name: String, val token: String, val lastSync: Long)

data class Globals(
    val focus: Int,
    val short: Int,
    val long: Int,
    val cycles: Int,
    val countUp: Boolean,
    val auto: Boolean,
    val dayStart: Int,
    val alarm: String,
    val background: String,
    val volume: Int,
    val dailyOn: Boolean,
    val dailyHour: Int,
    val dailyMinute: Int,
    val streakOn: Boolean,
    val streakHour: Int,
    val streakMinute: Int,
    val workMillis: Long = app.presence.core.DEFAULT_WORK_MS,
    val shortCount: Int = app.presence.core.DEFAULT_SHORT_BREAKS,
    val longCount: Int = app.presence.core.DEFAULT_LONG_BREAKS,
    val shortMillis: Long = app.presence.core.DEFAULT_SHORT_MS,
    val longMillis: Long = app.presence.core.DEFAULT_LONG_MS,
)

data class Todo(val id: String, val title: String, val done: Boolean, val sort: Long)

class Repo(private val context: Context) : Catalog {
    @Volatile private var db: PresenceDb? = null
    @Volatile private var dao: RecDao? = null
    private val lock = Any()
    private val generation = MutableStateFlow(0)
    private val storageState = MutableStateFlow(StorageStatus())
    private val rowsFlow: Flow<List<Rec>> = generation.flatMapLatest {
        val current = dao
        if (current == null) {
            flowOf(emptyList())
        } else {
            current.observe().map { rows -> rows.map { it.toRec() } }.catch { emit(emptyList()) }
        }
    }

    fun observe(): Flow<List<Rec>> = rowsFlow

    fun storage(): StateFlow<StorageStatus> = storageState

    fun open() {
        synchronized(lock) {
            if (!attachLocked()) {
                publishLocked()
                generation.value = generation.value + 1
                return
            }
            seedLocked()
            publishLocked()
            generation.value = generation.value + 1
        }
    }

    fun refreshStorage() {
        synchronized(lock) {
            val missing = readStatus().cardMissing
            if (missing && attachLocked()) seedLocked()
            publishLocked()
            if (missing && dao != null) generation.value = generation.value + 1
        }
    }

    fun moveToSd(): String = relocate(true)

    fun moveToInternal(): String = relocate(false)

    fun resetSettings() {
        synchronized(lock) {
            val dao = dao ?: return
            val now = System.currentTimeMillis()
            val writer = deviceIdLocked()
            val prefs = listOf(
                "focus_min" to "25",
                "short_min" to "5",
                "long_min" to "15",
                "cycles" to "4",
                "work_sec" to "3600",
                "short_sec" to "300",
                "long_sec" to "900",
                "short_count" to "2",
                "long_count" to "0",
                "count_up" to "0",
                "auto_start" to "0",
                "day_start" to "0",
                "daily_hour" to "9",
                "daily_minute" to "0",
                "streak_hour" to "20",
                "streak_minute" to "0",
            )
            dao.upsert(
                prefs.map { (key, value) ->
                    Rec(KIND_PREF, key, now, writer, false, mapOf("value" to value)).toEntity()
                },
            )
            val map = (dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "flags" }?.toRec()?.body ?: emptyMap()).toMutableMap()
            map["theme"] = "system"
            map["palette"] = "grove"
            dao.upsert(listOf(Rec(KIND_LOCAL, "flags", now, writer, false, map).toEntity()))
        }
    }

    private fun seedLocked() {
        val dao = dao ?: return
            val rows = dao.all().map { it.toRec() }
            val now = System.currentTimeMillis()
            if (rows.none { it.kind == KIND_LOCAL && it.id == "device" }) {
                val id = UUID.randomUUID().toString()
                dao.upsert(
                    listOf(
                        Rec(KIND_LOCAL, "device", now, id, false, mapOf("id" to id, "name" to "Presence")).toEntity(),
                    ),
                )
            }
            val writer = deviceIdLocked()
            val staleGroups = dao.all().filter { it.kind == KIND_GROUP && !it.deleted }
            if (staleGroups.isNotEmpty()) {
                dao.upsert(staleGroups.map { it.copy(deleted = true, updatedAt = now) })
            }
            val have = dao.all().filter { it.kind == KIND_PREF }.map { it.id }.toSet()
            val defaults = mapOf(
                "focus_min" to "25",
                "short_min" to "5",
                "long_min" to "15",
                "cycles" to "4",
                "count_up" to "0",
                "work_sec" to "3600",
                "short_sec" to "300",
                "long_sec" to "900",
                "short_count" to "2",
                "long_count" to "0",
                "auto_start" to "0",
                "day_start" to "0",
                "alarm" to "chime",
                "background" to "off",
                "bg_volume" to "40",
                "daily_on" to "0",
                "daily_hour" to "9",
                "daily_minute" to "0",
                "streak_on" to "0",
                "streak_hour" to "20",
                "streak_minute" to "0",
            )
            val missing = defaults.filterKeys { it !in have }.map { (key, value) ->
                Rec(KIND_PREF, key, now, writer, false, mapOf("value" to value)).toEntity()
            }
            if (missing.isNotEmpty()) dao.upsert(missing)
            if (dao.all().none { it.kind == KIND_LOCAL && it.id == "flags" }) {
                dao.upsert(
                    listOf(
                        Rec(
                            KIND_LOCAL,
                            "flags",
                            now,
                            writer,
                            false,
                            mapOf("premium" to "0", "theme" to "system", "text" to "standard", "language" to "system", "palette" to "grove"),
                        ).toEntity(),
                    ),
                )
            }
            val flagsRow = dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "flags" }?.toRec()
            if (flagsRow != null && flagsRow.f("palette").isEmpty()) {
                val body = flagsRow.body.toMutableMap()
                body["palette"] = "grove"
                dao.upsert(listOf(flagsRow.copy(updatedAt = now, body = body).toEntity()))
            }
    }

    override fun deviceId(): String = synchronized(lock) { deviceIdLocked() }

    override fun deviceName(): String = synchronized(lock) {
        val dao = dao ?: return "Presence"
        dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "device" }?.toRec()?.f("name")?.ifEmpty { "Presence" } ?: "Presence"
    }

    fun setDeviceName(name: String) {
        synchronized(lock) {
            val dao = dao ?: return
            val id = deviceIdLocked()
            dao.upsert(listOf(Rec(KIND_LOCAL, "device", System.currentTimeMillis(), id, false, mapOf("id" to id, "name" to name)).toEntity()))
        }
    }

    override fun snapshot(): List<Rec> = synchronized(lock) {
        val dao = dao ?: return emptyList()
        dao.all().map { it.toRec() }
    }

    override fun tokenFor(deviceId: String): String? = synchronized(lock) {
        val dao = dao ?: return null
        val row = dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "peer:$deviceId" && !it.deleted }?.toRec()
        row?.f("token")?.ifEmpty { null }
    }

    override fun rememberPeer(deviceId: String, name: String, token: String) {
        synchronized(lock) {
            val dao = dao ?: return
            val now = System.currentTimeMillis()
            dao.upsert(
                listOf(
                    Rec(
                        KIND_LOCAL,
                        "peer:$deviceId",
                        now,
                        deviceIdLocked(),
                        false,
                        mapOf("name" to name.ifEmpty { "Presence" }, "token" to token, "lastSync" to now.toString()),
                    ).toEntity(),
                ),
            )
        }
    }

    fun touchPeer(deviceId: String) {
        synchronized(lock) {
            val dao = dao ?: return
            val row = dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "peer:$deviceId" && !it.deleted }?.toRec() ?: return
            val now = System.currentTimeMillis()
            val body = row.body.toMutableMap()
            body["lastSync"] = now.toString()
            dao.upsert(listOf(row.copy(updatedAt = now, body = body).toEntity()))
        }
    }

    fun forgetPeer(deviceId: String) {
        synchronized(lock) {
            val dao = dao ?: return
            dao.upsert(
                listOf(
                    Rec(KIND_LOCAL, "peer:$deviceId", System.currentTimeMillis(), deviceIdLocked(), true, emptyMap()).toEntity(),
                ),
            )
        }
    }

    fun peers(): List<Peer> = synchronized(lock) {
        val dao = dao ?: return emptyList()
        dao.all().map { it.toRec() }
            .filter { it.kind == KIND_LOCAL && it.id.startsWith("peer:") && !it.deleted && it.f("token").isNotEmpty() }
            .map { Peer(it.id.removePrefix("peer:"), it.f("name").ifEmpty { "Presence" }, it.f("token"), it.f("lastSync").toLongOrNull() ?: 0L) }
    }

    override fun mergeFrom(incoming: List<Rec>) {
        synchronized(lock) {
            val dao = dao ?: return
            val local = dao.all().map { it.toRec() }
            val snap = local.firstOrNull { it.kind == KIND_LOCAL && it.id == "snapshot" && !it.deleted }?.let { snapFrom(it.body) } ?: Snap()
            val busy = snap.status == "running" || snap.status == "paused"
            var merged = mergeRecords(
                local,
                incoming,
                if (busy) setOfNotNull(snap.sessionId) else emptySet(),
                emptySet(),
            )
            val alarm = merged.find { it.kind == KIND_PREF && it.id == "alarm" }
            if (alarm?.f("value") == "custom" && !File(AppStorage.privateDir(context), "alarm-custom").exists()) {
                val previous = local.find { it.kind == KIND_PREF && it.id == "alarm" }
                    ?: Rec(KIND_PREF, "alarm", 0, deviceIdLocked(), false, mapOf("value" to "chime"))
                merged = merged.map { if (it.kind == KIND_PREF && it.id == "alarm") previous else it }
            }
            val changed = merged.filter { item ->
                local.find { it.kind == item.kind && it.id == item.id } != item
            }
            if (changed.isNotEmpty()) dao.upsert(changed.map { it.toEntity() })
        }
    }

    fun putAll(records: List<Rec>) {
        if (records.isEmpty()) return
        synchronized(lock) {
            val dao = dao ?: return
            dao.upsert(records.map { it.toEntity() })
        }
    }

    fun put(rec: Rec) = putAll(listOf(rec))

    fun currentSnap(): Snap = synchronized(lock) {
        val dao = dao ?: return Snap()
        val row = dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "snapshot" && !it.deleted } ?: return Snap()
        snapFrom(row.toRec().body)
    }

    fun saveSnap(snap: Snap) {
        synchronized(lock) {
            val dao = dao ?: return
            dao.upsert(listOf(Rec(KIND_LOCAL, "snapshot", System.currentTimeMillis(), deviceIdLocked(), false, snap.toFields()).toEntity()))
        }
    }

    fun flags(): Map<String, String> = synchronized(lock) {
        val dao = dao ?: return emptyMap()
        dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "flags" }?.toRec()?.body ?: emptyMap()
    }

    fun updateFlags(block: (MutableMap<String, String>) -> Unit) {
        synchronized(lock) {
            val dao = dao ?: return
            val map = (dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "flags" }?.toRec()?.body ?: emptyMap()).toMutableMap()
            block(map)
            dao.upsert(listOf(Rec(KIND_LOCAL, "flags", System.currentTimeMillis(), deviceIdLocked(), false, map).toEntity()))
        }
    }

    fun premium(): Boolean = true

    fun todos(): List<Todo> = synchronized(lock) {
        val dao = dao ?: return emptyList()
        dao.all().map { it.toRec() }
            .filter { it.kind == KIND_LOCAL && it.id.startsWith("todo:") && !it.deleted }
            .map {
                Todo(
                    id = it.id.removePrefix("todo:"),
                    title = it.f("title"),
                    done = it.f("done") == "1",
                    sort = it.f("sort").toLongOrNull() ?: it.updatedAt,
                )
            }
            .sortedBy { it.sort }
    }

    fun addTodo(title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        put(Rec(KIND_LOCAL, "todo:$id", now, deviceId(), false, mapOf("title" to clean, "done" to "0", "sort" to now.toString())))
    }

    fun setTodoDone(id: String, done: Boolean) {
        synchronized(lock) {
            val dao = dao ?: return
            val row = dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "todo:$id" }?.toRec() ?: return
            val body = row.body.toMutableMap()
            body["done"] = if (done) "1" else "0"
            dao.upsert(listOf(row.copy(updatedAt = System.currentTimeMillis(), writerId = deviceIdLocked(), body = body).toEntity()))
        }
    }

    fun deleteTodo(id: String) {
        synchronized(lock) {
            val dao = dao ?: return
            val row = dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "todo:$id" }?.toRec() ?: return
            dao.upsert(listOf(row.copy(deleted = true, updatedAt = System.currentTimeMillis(), writerId = deviceIdLocked()).toEntity()))
        }
    }

    fun globals(): Globals = synchronized(lock) {
        val dao = dao ?: return Globals(25, 5, 15, 4, false, false, 0, "chime", "off", 40, false, 9, 0, false, 20, 0)
        val prefs = dao.all().filter { it.kind == KIND_PREF }.associate { it.id to it.toRec().f("value") }
        fun n(key: String, fallback: Int) = prefs[key]?.toIntOrNull() ?: fallback
        Globals(
            focus = n("focus_min", 25).coerceIn(1, 180),
            short = n("short_min", 5).coerceIn(1, 60),
            long = n("long_min", 15).coerceIn(1, 120),
            cycles = n("cycles", 4).coerceIn(1, 12),
            countUp = prefs["count_up"] == "1",
            auto = prefs["auto_start"] == "1",
            dayStart = n("day_start", 0).coerceIn(0, 6),
            alarm = prefs["alarm"] ?: "chime",
            background = prefs["background"] ?: "off",
            volume = n("bg_volume", 40).coerceIn(0, 100),
            workMillis = (prefs["work_sec"]?.toLongOrNull() ?: 3_600L).coerceIn(1L, 99L * 3_600L) * 1_000L,
            shortCount = n("short_count", 2).coerceIn(0, 24),
            longCount = n("long_count", 0).coerceIn(0, 24),
            shortMillis = (prefs["short_sec"]?.toLongOrNull() ?: 300L).coerceIn(1L, 99L * 3_600L) * 1_000L,
            longMillis = (prefs["long_sec"]?.toLongOrNull() ?: 900L).coerceIn(1L, 99L * 3_600L) * 1_000L,
            dailyOn = prefs["daily_on"] == "1",
            dailyHour = n("daily_hour", 9).coerceIn(0, 23),
            dailyMinute = n("daily_minute", 0).coerceIn(0, 59),
            streakOn = prefs["streak_on"] == "1",
            streakHour = n("streak_hour", 20).coerceIn(0, 23),
            streakMinute = n("streak_minute", 0).coerceIn(0, 59),
        )
    }

    fun setPref(key: String, value: String) {
        synchronized(lock) {
            val dao = dao ?: return
            dao.upsert(listOf(Rec(KIND_PREF, key, System.currentTimeMillis(), deviceIdLocked(), false, mapOf("value" to value)).toEntity()))
        }
    }

    fun writerNow(kind: String, id: String, body: Map<String, String>, deleted: Boolean = false): Rec {
        return Rec(kind, id, System.currentTimeMillis(), deviceId(), deleted, body)
    }

    private fun deviceIdLocked(): String {
        val dao = dao ?: return "device"
        return dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "device" }?.toRec()?.f("id") ?: "device"
    }

    private fun attachLocked(): Boolean {
        val stored = AppStorage.prefs(context)
        val location = stored.getString(AppStorage.LOCATION, "internal")
        if (location == "sd") {
            val path = stored.getString(AppStorage.DB_PATH, null)
            val file = if (path.isNullOrEmpty()) null else File(path)
            if (file == null || !file.exists()) {
                closeLocked()
                return false
            }
            if (db != null && dao != null) return true
            closeLocked()
            openDatabase(file.absolutePath)
            return dao != null
        }
        if (db != null && dao != null) return true
        openDatabase(internalDb().absolutePath)
        return dao != null
    }

    private fun relocate(toCard: Boolean): String {
        synchronized(lock) {
            val card = AppStorage.removableDir(context)
            if (toCard && card == null) {
                publishLocked()
                return "no-card"
            }
            val database = db
            val current = dao
            if (database == null || current == null) {
                publishLocked()
                return "missing"
            }
            val source = activeDbFile()
            val destRoot = if (toCard) card else null
            if (toCard && destRoot == null) return "no-card"
            val dest = if (destRoot != null) File(destRoot, "presence.db") else internalDb()
            val destFiles = if (destRoot != null) File(destRoot, "files") else context.filesDir
            if (source.absolutePath == dest.absolutePath) return "same"
            dest.parentFile?.mkdirs()
            destFiles.mkdirs()
            try {
                database.openHelper.writableDatabase.query(androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)")).close()
            } catch (_: Exception) {
            }
            closeLocked()
            if (!copyDb(source, dest)) {
                openDatabase(source.absolutePath)
                publishLocked()
                generation.value = generation.value + 1
                return "failed"
            }
            try {
                openDatabase(dest.absolutePath)
                dao?.all() ?: error("closed")
            } catch (_: Exception) {
                closeLocked()
                dest.delete()
                File(dest.absolutePath + "-wal").delete()
                File(dest.absolutePath + "-shm").delete()
                openDatabase(source.absolutePath)
                publishLocked()
                generation.value = generation.value + 1
                return "failed"
            }
            deleteDb(source)
            copyPrivateFiles(AppStorage.privateDir(context), destFiles)
            AppStorage.prefs(context).edit()
                .putString(AppStorage.LOCATION, if (toCard) "sd" else "internal")
                .putString(AppStorage.DB_PATH, dest.absolutePath)
                .putString(AppStorage.FILES_PATH, destFiles.absolutePath)
                .apply()
            publishLocked()
            generation.value = generation.value + 1
            return "moved"
        }
    }

    private fun activeDbFile(): File {
        val path = AppStorage.prefs(context).getString(AppStorage.DB_PATH, null)
        if (!path.isNullOrEmpty() && File(path).exists()) return File(path)
        return internalDb()
    }

    private fun internalDb(): File = context.getDatabasePath("presence.db")

    private fun openDatabase(path: String) {
        val next = Room.databaseBuilder(context.applicationContext, PresenceDb::class.java, path)
            .allowMainThreadQueries()
            .build()
        db = next
        dao = next.recs()
    }

    private fun closeLocked() {
        try {
            db?.close()
        } catch (_: Exception) {
        }
        db = null
        dao = null
    }

    private fun copyDb(source: File, dest: File): Boolean {
        if (!source.exists()) return false
        return try {
            if (dest.exists()) dest.delete()
            source.copyTo(dest, overwrite = true)
            listOf("-wal", "-shm").forEach { suffix ->
                val extra = File(source.absolutePath + suffix)
                val target = File(dest.absolutePath + suffix)
                if (target.exists()) target.delete()
                if (extra.exists()) extra.copyTo(target, overwrite = true)
            }
            dest.exists() && dest.length() > 0L
        } catch (_: Exception) {
            false
        }
    }

    private fun deleteDb(file: File) {
        file.delete()
        File(file.absolutePath + "-wal").delete()
        File(file.absolutePath + "-shm").delete()
    }

    private fun copyPrivateFiles(from: File, to: File) {
        if (from.absolutePath == to.absolutePath || !from.exists()) return
        to.mkdirs()
        from.listFiles()?.forEach { file ->
            if (!file.isFile) return@forEach
            val target = File(to, file.name)
            try {
                file.copyTo(target, overwrite = true)
                if (target.exists() && target.length() == file.length()) file.delete()
            } catch (_: Exception) {
            }
        }
    }

    private fun readStatus(): StorageStatus {
        val stored = AppStorage.prefs(context)
        val location = stored.getString(AppStorage.LOCATION, "internal")
        val path = stored.getString(AppStorage.DB_PATH, null)
        val file = if (path.isNullOrEmpty()) null else File(path)
        val missing = location == "sd" && (file == null || !file.exists())
        return StorageStatus(
            cardPresent = AppStorage.removableDir(context) != null,
            onCard = location == "sd" && !missing,
            cardMissing = missing,
        )
    }

    private fun publishLocked() {
        storageState.value = readStatus()
    }
}
