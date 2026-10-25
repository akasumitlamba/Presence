package app.presence.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import javax.swing.SwingUtilities
import kotlin.coroutines.CoroutineContext

class MenuLayoutTest {
    /** A positioned dropdown must appear below its field after layout, in the app's theme. */
    @OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test(timeout = 15000) fun dropdownAppearsAtItsAnchorWithThemeColors() = runBlocking {
        val ui = object : CoroutineDispatcher() { override fun dispatch(context: CoroutineContext, block: Runnable) { SwingUtilities.invokeLater(block) } }
        var expected = 0
        val scene = withContext(ui) {
            ImageComposeScene(480,360,coroutineContext = ui) {
                PresenceTheme(dark = false) {
                    expected = MaterialTheme.colorScheme.surfaceContainerHigh.toArgb()
                    Surface {
                        Box(Modifier.fillMaxSize()) {
                            Box(Modifier.offset(32.dp,40.dp).size(200.dp,56.dp)) {
                                DesktopDropdownMenu(true,{}) { DropdownMenuItem(text = { Text("Choice") },onClick = {}) }
                            }
                            DesktopMenuHost()
                        }
                    }
                }
            }
        }
        try {
            repeat(12) { withContext(ui) { scene.render(System.nanoTime()).close() }; delay(20) }
            withContext(ui) {
                scene.render(System.nanoTime()).use { frame -> frame.encodeToData()!!.use { bytes ->
                    val image = ImageIO.read(ByteArrayInputStream(bytes.bytes))
                    assertEquals("Menu must render below the 200×56 field, rather than disappear or use native colors",expected,image.getRGB(215,120))
                } }
            }
        } finally { withContext(ui) { scene.close() } }
    }
}
