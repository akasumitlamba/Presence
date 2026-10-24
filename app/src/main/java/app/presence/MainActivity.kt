package app.presence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.presence.ui.PresenceRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PresenceRoot() }
    }

    override fun onStart() {
        super.onStart()
        val app = application as PresenceApp
        app.ready.invokeOnCompletion {
            if (app.hasLan()) app.lan.auto()
            app.center.catchUp()
        }
    }
}
