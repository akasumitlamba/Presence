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
    fun show() {
        val spinner = JSpinner(SpinnerDateModel(Date.from(initial.atStartOfDay(ZoneId.systemDefault()).toInstant()),null,null,java.util.Calendar.DAY_OF_MONTH))
        spinner.editor = JSpinner.DateEditor(spinner,"yyyy-MM-dd")
        if (JOptionPane.showConfirmDialog(null,spinner,"Presence",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            spinner.commitEdit()
            val d = (spinner.value as Date).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            chosen(null,d.year,d.monthValue-1,d.dayOfMonth)
        }
    }
}
class DesktopTimePicker(private val context: DesktopContext, private val chosen: (Any?,Int,Int) -> Unit, private val hour: Int, private val minute: Int, private val twentyFour: Boolean) {
    fun show() {
        val h = JSpinner(SpinnerNumberModel(hour,0,23,1)); val m = JSpinner(SpinnerNumberModel(minute,0,59,1))
        val panel = JPanel().apply { add(h); add(JLabel(":")); add(m) }
        if (JOptionPane.showConfirmDialog(null,panel,"Presence",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) { h.commitEdit(); m.commitEdit(); chosen(null,h.value as Int,m.value as Int) }
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
