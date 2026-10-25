package app.presence.desktop

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import app.presence.core.*
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.nio.file.*
import java.time.*
import java.util.Date
import javax.swing.*

data class StorageStatus(val cardPresent: Boolean = false, val onCard: Boolean = false, val cardMissing: Boolean = false)
data class DesktopContext(val applicationContext: PresenceApp) { val contentResolver = DesktopFiles() }
val LocalContext = staticCompositionLocalOf<DesktopContext> { error("Missing desktop context") }
class DesktopFiles {
    fun openOutputStream(path: Path) = Files.newOutputStream(path)
    fun openInputStream(path: Path) = Files.newInputStream(path)
}

sealed class FileContract<I> {
    class Create(val mime: String) : FileContract<String>()
    class Open : FileContract<Array<String>>()
}
object FileContracts {
    fun CreateDocument(mime: String) = FileContract.Create(mime)
    fun OpenDocument() = FileContract.Open()
}
class DesktopFileLauncher<I>(private val contract: FileContract<I>, private val callback: (Path?) -> Unit) {
    fun launch(input: I) {
        val save = contract is FileContract.Create
        val dialog = FileDialog(null as java.awt.Frame?, if (save) "Presence — Save" else "Presence — Open", if(save) FileDialog.SAVE else FileDialog.LOAD)
        val documents = Paths.get(System.getProperty("user.home"),"Documents")
        if (Files.isDirectory(documents)) dialog.directory = documents.toString()
        try {
            if (save) dialog.file = input as String
            dialog.isVisible = true
            callback(dialog.file?.let { Paths.get(dialog.directory,it) })
        } finally { dialog.dispose() }
    }
}
@Composable fun <I> rememberFileLauncher(contract: FileContract<I>, callback: (Path?) -> Unit): DesktopFileLauncher<I> {
    val current by rememberUpdatedState(callback)
    return remember { DesktopFileLauncher(contract) { current(it) } }
}
@Composable fun BackHandler(enabled: Boolean, action: () -> Unit) {
    val app = LocalContext.current.applicationContext
    val latest by rememberUpdatedState(action)
    SideEffect { app.back = if(enabled) ({ latest() }) else null }
    DisposableEffect(app) { onDispose { app.back = null } }
}
fun showError(message: String) { SwingUtilities.invokeLater { JOptionPane.showMessageDialog(null,message,"Presence",JOptionPane.ERROR_MESSAGE) } }
fun shareText(text: String, title: String) {
    val area = JTextArea(text,12,48).apply { isEditable = false; lineWrap = true; wrapStyleWord = true }
    val answer = JOptionPane.showOptionDialog(null,JScrollPane(area),title,JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,arrayOf("Copy","Save as text","Close"),"Copy")
    if (answer == 0) Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text),null)
    if (answer == 1) DesktopFileLauncher(FileContracts.CreateDocument("text/plain")) { path -> path?.let { Files.writeString(it,text) }; Unit }.launch("presence-summary.txt")
}
class DesktopDatePicker(private val context: DesktopContext, private val chosen: (Any?,Int,Int,Int) -> Unit, year: Int, month: Int, day: Int) {
    private val initial = LocalDate.of(year,month+1,day)
    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    fun show() {
        DesktopDialogs.content = {
            val t = LocalUiText.current
            val state = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = initial.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli())
            androidx.compose.material3.DatePickerDialog(onDismissRequest = { DesktopDialogs.content = null },
                confirmButton = { TextButton({ state.selectedDateMillis?.let { stamp ->
                    val d = java.time.Instant.ofEpochMilli(stamp).atZone(java.time.ZoneOffset.UTC).toLocalDate()
                    chosen(null,d.year,d.monthValue-1,d.dayOfMonth); DesktopDialogs.content = null
                } }, enabled = state.selectedDateMillis != null) { Text(t.save) } },
                dismissButton = { TextButton({ DesktopDialogs.content = null }) { Text(t.cancel) } }) { androidx.compose.material3.DatePicker(state) }
        }
    }
}
class DesktopTimePicker(private val context: DesktopContext, private val chosen: (Any?,Int,Int) -> Unit, private val hour: Int, private val minute: Int, private val twentyFour: Boolean) {
    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    fun show() {
        DesktopDialogs.content = {
            val t = LocalUiText.current
            val state = androidx.compose.material3.rememberTimePickerState(hour,minute,twentyFour)
            PresenceDialog(onDismissRequest = { DesktopDialogs.content = null }, title = {},
                text = { androidx.compose.material3.TimeInput(state) },
                confirmButton = { TextButton({ chosen(null,state.hour,state.minute); DesktopDialogs.content = null }) { Text(t.save) } },
                dismissButton = { TextButton({ DesktopDialogs.content = null }) { Text(t.cancel) } })
        }
    }
}
@Composable fun DesktopStoragePane(app: PresenceApp, t: T) {
    SectionCard(t.storage) {
        val location by app.repo.storage().collectAsState()
        Text(app.repo.directory.toString(), style = MaterialTheme.typography.bodyMedium)
        Text("Keep data on this computer or choose a folder on a removable drive.", style = MaterialTheme.typography.bodyMedium)
        TextButton({
            val chooser = JFileChooser().apply { fileSelectionMode = JFileChooser.DIRECTORIES_ONLY; dialogTitle = "Presence — Data folder" }
            if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                runCatching { app.repo.relocate(chooser.selectedFile.toPath()) }.onFailure { showError(it.message ?: t.fileError) }
            }
        }) { Text("Choose data folder") }
        HorizontalDivider()
        Text("Widgets",style = MaterialTheme.typography.titleMedium)
        Row { listOf("timer" to t.timer,"stats" to t.stats,"streak" to t.streak,"todos" to t.todos).forEach { (id,label) -> TextButton({ app.openWidget?.invoke(id) }) { Text(label) } } }
    }
}

// Desktop fullscreen hides app navigation; it does not alter Android system bars.
@androidx.compose.runtime.Composable internal fun TimerImmersive(enabled: Boolean) {}
