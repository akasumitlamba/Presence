package app.presence.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import app.presence.core.*
import java.nio.file.*
import java.awt.*
import java.awt.event.*
import javax.imageio.ImageIO
import kotlinx.coroutines.delay

fun main() {
    val directory = Store.dataDirectory()
    Files.createDirectories(directory)
    val channel = java.nio.channels.FileChannel.open(directory.resolve("presence.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE)
    val lock = channel.tryLock()
    if (lock == null) { showError("Presence is already running."); channel.close(); return }
    val store = try { Store() } catch(e: Exception) { showError("Could not open Presence data: ${e.message}\nYour data has been left in place."); lock.release(); channel.close(); return }
    val app = PresenceApp(store)
    application {
        var visible by remember { mutableStateOf(true) }
        var exiting by remember { mutableStateOf(false) }
        val widgets = remember { mutableStateListOf<String>() }
        var traySupported by remember { mutableStateOf(false) }
        SideEffect { app.openWidget = { id -> if(id !in widgets) widgets.add(id) } }
        val exit = { app.close(); lock.release(); channel.close(); exitApplication() }
        DisposableEffect(Unit) {
            var icon: TrayIcon? = null
            if(SystemTray.isSupported()) {
                val image = ImageIO.read(object {}.javaClass.getResourceAsStream("/presence.png"))
                icon = TrayIcon(image,"Presence").apply {
                    isImageAutoSize = true
                    val menu = PopupMenu()
                    menu.add(MenuItem("Presence").apply { addActionListener { visible = true } })
                    menu.add(MenuItem("Continue / Pause").apply { addActionListener { val s = app.center.snap(); if(s.status == "running") app.center.pause() else if(s.status == "paused") app.center.resume() else visible = true } })
                    menu.add(MenuItem("Stop").apply { addActionListener { app.center.stop() } })
                    val widgetMenu = Menu("Widgets")
                    listOf("timer" to "Timer", "stats" to "Statistics", "streak" to "Streak", "todos" to "Todos").forEach { (id,title) ->
                        widgetMenu.add(MenuItem(title).apply { addActionListener { if(id !in widgets) widgets.add(id) } })
                    }
                    menu.add(widgetMenu)
                    menu.addSeparator()
                    menu.add(MenuItem("Quit Presence").apply { addActionListener { exiting = true; visible = true } })
                    popupMenu = menu
                    addActionListener { visible = true }
                }
                runCatching { SystemTray.getSystemTray().add(icon); app.notifier.tray = icon; traySupported = true }.onFailure { icon = null }
            }
            onDispose { icon?.let { SystemTray.getSystemTray().remove(it) } }
        }
        widgets.toList().forEach { widget -> key(widget) {
            val widgetState = rememberWindowState(width = 340.dp,height = 348.dp)
            Window(onCloseRequest = { widgets.remove(widget) },title = "Presence · $widget", icon = painterResource("presence.png"), state = widgetState, alwaysOnTop = true, undecorated = true) {
                window.minimumSize = Dimension(280,240)
                WidgetContent(widget,app,open = { visible = true }, chrome = { PresenceTitleBar(widgetState, { widgets.remove(widget) }) })
            }
        } }
        val mainState = rememberWindowState(width = 1180.dp,height = 848.dp)
        val closeMain = { if(traySupported) visible = false else exiting = true }
        Window(onCloseRequest = closeMain, visible = visible, undecorated = true,
            title = "Presence", icon = painterResource("presence.png"), state = mainState,
            onKeyEvent = { event -> if(event.type == KeyEventType.KeyUp && event.key == Key.Escape && app.back != null) { app.back?.invoke(); true } else false }) {
            window.minimumSize = Dimension(700,608)
            PresenceRoot(app) { PresenceTitleBar(mainState, closeMain, showIcon = false) }
            if(exiting) {
                val t = T(resolveLang(store.flag("language","system")))
                AlertDialog(onDismissRequest = { exiting = false }, title = { Text("Quit Presence?") },
                    text = { Text("Timers, reminders and Wi-Fi sync keep working when you close to the tray. Quitting stops background activity until you open Presence again.") },
                    confirmButton = { TextButton(exit) { Text("Quit") } }, dismissButton = { TextButton({ exiting = false }) { Text(t.cancel) } })
            }
            LaunchedEffect(Unit) {
                val destination = System.getenv("PRESENCE_PREVIEW_DIR")
                if(destination != null) {
                    Files.createDirectories(Paths.get(destination))
                    while(true) {
                        delay(2500)
                        if(window.isShowing) runCatching {
                            fun findLayer(component: Component): org.jetbrains.skiko.SkiaLayer? =
                                if(component is org.jetbrains.skiko.SkiaLayer) component else
                                    (component as? Container)?.components?.firstNotNullOfOrNull { findLayer(it) }
                            findLayer(window)?.screenshot()?.use { bitmap ->
                                org.jetbrains.skia.Image.makeFromBitmap(bitmap).use { image ->
                                    image.encodeToData()?.use { data -> Files.write(Paths.get(destination,"presence-preview.png"),data.bytes) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
