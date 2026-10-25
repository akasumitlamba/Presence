package app.presence

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.presence.ui.PresenceRoot

class MainActivity : ComponentActivity() {
    var requestedTab by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(app.presence.R.style.Theme_Presence)
        super.onCreate(savedInstanceState)
        requestedTab = if (savedInstanceState == null) intent?.getStringExtra(EXTRA_TAB) else null
        enableEdgeToEdge()
        setContent { PresenceRoot() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedTab = intent.getStringExtra(EXTRA_TAB)
    }

    override fun onStart() {
        super.onStart()
        val app = application as PresenceApp
        app.ready.invokeOnCompletion {
            if (app.hasLan()) app.lan.auto()
            app.center.catchUp()
        }
    }

    companion object {
        const val EXTRA_TAB = "tab"
    }
}
