package app.presence.data

import android.content.Context
import android.os.Environment
import java.io.File

data class StorageStatus(
    val cardPresent: Boolean = false,
    val onCard: Boolean = false,
    val cardMissing: Boolean = false,
)

object AppStorage {
    const val PREFS = "presence_storage"
    const val LOCATION = "location"
    const val DB_PATH = "db_path"
    const val FILES_PATH = "files_path"

    fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun privateDir(context: Context): File {
        val stored = prefs(context)
        val location = stored.getString(LOCATION, "internal")
        val path = stored.getString(FILES_PATH, null)
        if (location == "sd" && !path.isNullOrEmpty()) return File(path)
        return context.filesDir
    }

    fun removableDir(context: Context): File? {
        val dirs = context.getExternalFilesDirs(null) ?: return null
        for (dir in dirs) {
            if (dir == null) continue
            val removable = try {
                Environment.isExternalStorageRemovable(dir)
            } catch (_: IllegalArgumentException) {
                false
            }
            if (!removable) continue
            if (!dir.exists()) dir.mkdirs()
            return dir
        }
        return null
    }
}
