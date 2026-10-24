package app.canto.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import app.canto.core.Catalog
import app.canto.core.GROUP_PERSONAL
import app.canto.core.GROUP_STUDY
import app.canto.core.GROUP_WORK
import app.canto.core.KIND_GROUP
import app.canto.core.KIND_LOCAL
import app.canto.core.KIND_PREF
import app.canto.core.Rec
import app.canto.core.Snap
import app.canto.core.TIMER_PREF_IDS
import app.canto.core.f
import app.canto.core.mergeRecords
import app.canto.core.snapFrom
import app.canto.core.toFields
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.Flow
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
abstract class CantoDb : RoomDatabase() {
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
)

class Repo(private val context: Context) : Catalog {
    private val db = Room.databaseBuilder(context.applicationContext, CantoDb::class.java, "canto.db")
        .allowMainThreadQueries()
        .build()
    private val dao = db.recs()
    private val lock = Any()

    fun observe(): Flow<List<Rec>> = dao.observe().map { rows -> rows.map { it.toRec() } }

    fun open() {
        synchronized(lock) {
            val rows = dao.all().map { it.toRec() }
            val now = System.currentTimeMillis()
            if (rows.none { it.kind == KIND_LOCAL && it.id == "device" }) {
                val id = UUID.randomUUID().toString()
                dao.upsert(
                    listOf(
                        Rec(KIND_LOCAL, "device", now, id, false, mapOf("id" to id, "name" to "Canto")).toEntity(),
                    ),
                )
            }
            val writer = deviceIdLocked()
            if (rows.none { it.kind == KIND_GROUP && it.id == GROUP_WORK }) {
                dao.upsert(
                    listOf(
                        Rec(KIND_GROUP, GROUP_WORK, now, writer, false, mapOf("name" to "Work", "sort" to "0")),
                        Rec(KIND_GROUP, GROUP_STUDY, now, writer, false, mapOf("name" to "Study", "sort" to "1")),
                        Rec(KIND_GROUP, GROUP_PERSONAL, now, writer, false, mapOf("name" to "Personal", "sort" to "2")),
                    ).map { it.toEntity() },
                )
            }
            val have = dao.all().filter { it.kind == KIND_PREF }.map { it.id }.toSet()
            val defaults = mapOf(
                "focus_min" to "25",
                "short_min" to "5",
                "long_min" to "15",
                "cycles" to "4",
                "count_up" to "0",
                "auto_start" to "0",
                "day_start" to "0",
                "alarm" to "chime",
                "background" to "off",
                "bg_volume" to "40",
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
                            mapOf("premium" to "0", "theme" to "system", "text" to "standard", "language" to "system"),
                        ).toEntity(),
                    ),
                )
            }
        }
    }

    override fun deviceId(): String = synchronized(lock) { deviceIdLocked() }

    override fun deviceName(): String = synchronized(lock) {
        dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "device" }?.toRec()?.f("name")?.ifEmpty { "Canto" } ?: "Canto"
    }

    fun setDeviceName(name: String) {
        synchronized(lock) {
            val id = deviceIdLocked()
            dao.upsert(listOf(Rec(KIND_LOCAL, "device", System.currentTimeMillis(), id, false, mapOf("id" to id, "name" to name)).toEntity()))
        }
    }

    override fun snapshot(): List<Rec> = synchronized(lock) { dao.all().map { it.toRec() } }

    override fun tokenFor(deviceId: String): String? = synchronized(lock) {
        val row = dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "peer:$deviceId" && !it.deleted }?.toRec()
        row?.f("token")?.ifEmpty { null }
    }

    override fun rememberPeer(deviceId: String, name: String, token: String) {
        synchronized(lock) {
            val now = System.currentTimeMillis()
            dao.upsert(
                listOf(
                    Rec(
                        KIND_LOCAL,
                        "peer:$deviceId",
                        now,
                        deviceIdLocked(),
                        false,
                        mapOf("name" to name.ifEmpty { "Canto" }, "token" to token, "lastSync" to now.toString()),
                    ).toEntity(),
                ),
            )
        }
    }

    fun touchPeer(deviceId: String) {
        synchronized(lock) {
            val row = dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "peer:$deviceId" && !it.deleted }?.toRec() ?: return
            val now = System.currentTimeMillis()
            val body = row.body.toMutableMap()
            body["lastSync"] = now.toString()
            dao.upsert(listOf(row.copy(updatedAt = now, body = body).toEntity()))
        }
    }

    fun forgetPeer(deviceId: String) {
        synchronized(lock) {
            dao.upsert(
                listOf(
                    Rec(KIND_LOCAL, "peer:$deviceId", System.currentTimeMillis(), deviceIdLocked(), true, emptyMap()).toEntity(),
                ),
            )
        }
    }

    fun peers(): List<Peer> = synchronized(lock) {
        dao.all().map { it.toRec() }
            .filter { it.kind == KIND_LOCAL && it.id.startsWith("peer:") && !it.deleted && it.f("token").isNotEmpty() }
            .map { Peer(it.id.removePrefix("peer:"), it.f("name").ifEmpty { "Canto" }, it.f("token"), it.f("lastSync").toLongOrNull() ?: 0L) }
    }

    override fun mergeFrom(incoming: List<Rec>) {
        synchronized(lock) {
            val local = dao.all().map { it.toRec() }
            val snap = local.firstOrNull { it.kind == KIND_LOCAL && it.id == "snapshot" && !it.deleted }?.let { snapFrom(it.body) } ?: Snap()
            val busy = snap.status == "running" || snap.status == "paused"
            var merged = mergeRecords(
                local,
                incoming,
                if (busy) setOfNotNull(snap.sessionId) else emptySet(),
                if (busy) TIMER_PREF_IDS else emptySet(),
            )
            val alarm = merged.find { it.kind == KIND_PREF && it.id == "alarm" }
            if (alarm?.f("value") == "custom" && !File(context.filesDir, "alarm-custom").exists()) {
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
        synchronized(lock) { dao.upsert(records.map { it.toEntity() }) }
    }

    fun put(rec: Rec) = putAll(listOf(rec))

    fun currentSnap(): Snap = synchronized(lock) {
        val row = dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "snapshot" && !it.deleted } ?: return Snap()
        snapFrom(row.toRec().body)
    }

    fun saveSnap(snap: Snap) {
        synchronized(lock) {
            dao.upsert(listOf(Rec(KIND_LOCAL, "snapshot", System.currentTimeMillis(), deviceIdLocked(), false, snap.toFields()).toEntity()))
        }
    }

    fun flags(): Map<String, String> = synchronized(lock) {
        dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "flags" }?.toRec()?.body ?: emptyMap()
    }

    fun updateFlags(block: (MutableMap<String, String>) -> Unit) {
        synchronized(lock) {
            val map = (dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "flags" }?.toRec()?.body ?: emptyMap()).toMutableMap()
            block(map)
            dao.upsert(listOf(Rec(KIND_LOCAL, "flags", System.currentTimeMillis(), deviceIdLocked(), false, map).toEntity()))
        }
    }

    fun premium(): Boolean = flags()["premium"] == "1"

    fun setPremium(on: Boolean) = updateFlags { it["premium"] = if (on) "1" else "0" }

    fun globals(): Globals = synchronized(lock) {
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
        )
    }

    fun setPref(key: String, value: String) {
        synchronized(lock) {
            dao.upsert(listOf(Rec(KIND_PREF, key, System.currentTimeMillis(), deviceIdLocked(), false, mapOf("value" to value)).toEntity()))
        }
    }

    fun writerNow(kind: String, id: String, body: Map<String, String>, deleted: Boolean = false): Rec {
        return Rec(kind, id, System.currentTimeMillis(), deviceId(), deleted, body)
    }

    private fun deviceIdLocked(): String {
        return dao.all().firstOrNull { it.kind == KIND_LOCAL && it.id == "device" }?.toRec()?.f("id") ?: "device"
    }
}
