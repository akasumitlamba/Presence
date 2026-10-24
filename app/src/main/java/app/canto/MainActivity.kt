package app.canto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.canto.ui.CantoRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CantoRoot() }
    }

    override fun onStart() {
        super.onStart()
        val app = application as CantoApp
        app.ready.invokeOnCompletion {
            if (app.hasLan()) app.lan.auto()
            app.center.catchUp()
        }
    }
}
