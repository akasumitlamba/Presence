package app.presence.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.view.WindowManager
import app.presence.MainActivity

@Composable
internal fun TimerImmersive(enabled: Boolean) {
    val window = (LocalContext.current as? MainActivity)?.window
    DisposableEffect(window, enabled) {
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        val behavior = controller?.systemBarsBehavior
        val keptAwake = window?.attributes?.flags?.and(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
        if (enabled && window != null) {
            controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.systemBars())
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            if (enabled) {
                controller?.show(WindowInsetsCompat.Type.systemBars())
                behavior?.let { controller?.systemBarsBehavior = it }
                if (!keptAwake) window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
}
