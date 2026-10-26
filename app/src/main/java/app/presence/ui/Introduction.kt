package app.presence.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.presence.R

@Composable
internal fun StartupContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Image(painterResource(R.drawable.presence_mark), null, Modifier.size(96.dp), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(androidx.compose.material3.MaterialTheme.colorScheme.primary))
        Text("Presence", style = MaterialTheme.typography.headlineLarge)
        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
    }
}

@Composable
internal fun Introduction(t: T, onDone: () -> Unit) {
    PresenceDialog(
        onDismissRequest = onDone,
        title = { Text(t.welcome) },
        text = {
            Text(t.projects, style = MaterialTheme.typography.titleMedium)
            Text(t.guideProject)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(t.timer, style = MaterialTheme.typography.titleMedium)
            Text(t.guideTimer)
            Text(t.guidePlan)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(t.guideOffline)
        },
        confirmButton = { TextButton(onClick = onDone) { Text(t.getStarted) } },
        dismissButton = {},
    )
}
