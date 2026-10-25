package app.presence.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import javax.swing.SwingUtilities
import java.nio.file.Files
import java.nio.file.Path

/** Opt-in offscreen layout review, without opening another app window; the caller supplies an isolated data directory. */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
internal fun renderDesktopPreview(app: PresenceApp, destination: Path, width: Int, height: Int) = runBlocking {
    val ui = object : CoroutineDispatcher() { override fun dispatch(context: CoroutineContext, block: Runnable) { SwingUtilities.invokeLater(block) } }
    val scene = withContext(ui) {
        ImageComposeScene(width, height, coroutineContext = ui) {
            val state = rememberWindowState(width = width.dp, height = height.dp)
            PresenceRoot(app, chrome = { PresenceWindowControls(state, {}) }, dragArea = { modifier, content -> Box(modifier) { content() } })
        }
    }
    try {
        repeat(12) {
            withContext(ui) { scene.render(System.nanoTime()).close() }
            delay(20)
        }
        withContext(ui) {
            Files.createDirectories(destination.parent)
            scene.render(System.nanoTime()).use { image -> image.encodeToData()?.use { Files.write(destination, it.bytes) } }
        }
    } finally { withContext(ui) { scene.close() } }
}
