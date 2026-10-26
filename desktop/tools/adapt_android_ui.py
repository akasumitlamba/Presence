"""Build desktop UI from the Android source; only platform integrations are replaced.
Fail loudly when an integration anchor changes rather than silently dropping UI.
"""
from pathlib import Path
import re
root = Path(__file__).resolve().parents[2]
out = root / 'desktop/build/generated/androidUi/app/presence/desktop'
out.mkdir(parents=True, exist_ok=True)
ui = root / 'app/src/main/java/app/presence/ui'
def section(s, start, end, replacement):
    a=s.index(start); b=s.index(end,a)
    return s[:a]+replacement+s[b:]
def base(s):
    s=s.replace('package app.presence.ui','package app.presence.desktop')
    return s
s=base((ui/'Screens.kt').read_text(encoding='utf-8'))
s='\n'.join(line for line in s.split('\n') if not line.startswith((
    'import android.', 'import androidx.activity.', 'import androidx.core.',
    'import androidx.compose.material3.windowsizeclass.', 'import androidx.compose.ui.platform.LocalConfiguration',
    'import androidx.compose.ui.platform.LocalContext', 'import app.presence.MainActivity',
    'import app.presence.PresenceApp', 'import app.presence.data.', 'import app.presence.timer.', 'import app.presence.widget.')))
s=section(s,'@Composable\nfun PresenceRoot()', '@Composable\nprivate fun Shell(', '''@Composable
fun PresenceRoot(app: PresenceApp, chrome: @Composable () -> Unit = {}, dragArea: @Composable (Modifier, @Composable () -> Unit) -> Unit, overlays: @Composable () -> Unit = {}) {
    val rows by app.repo.observe().collectAsState(initial = app.repo.snapshot())
    val flags = rows.firstOrNull { it.kind == KIND_LOCAL && it.id == "flags" }?.body.orEmpty()
    val t = remember(flags["language"]) { T(resolveLang(flags["language"] ?: "system")) }
    val dark = when(flags["theme"]) { "dark" -> true; "light" -> false; else -> androidx.compose.foundation.isSystemInDarkTheme() }
    val scale = when(flags["text"]) { "compact" -> 0.9f; "large" -> 1.16f; else -> 1f }
    PresenceTheme(dark, flags["palette"] ?: "grove", scale) {
        BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            CompositionLocalProvider(LocalContext provides DesktopContext(app), LocalUiText provides t,
                LocalHeaderDrag provides dragArea,
                LocalUiLayout provides UiLayout(maxWidth, maxHeight, false)) {
                Shell(app, rows, t, true, LocalUiLayout.current.twoPanes)
                Box(Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 8.dp)) { chrome() }
                DesktopMenuHost()
                DesktopDialogs.content?.invoke()
                overlays()
            }
        }
    }
}

''')
s=section(s,'    val introPrefs = remember', '    var tick by rememberSaveable', '''    var guide by rememberSaveable { mutableStateOf(app.repo.flag("introduced", "0") != "1") }
    if (guide) Introduction(t) { app.repo.setFlag("introduced", "1"); guide = false }
''')
s=section(s,'    val permission = rememberLauncher', '    val back: (() -> Unit)?', '')
# Opt-in screen selection for the existing isolated preview capture mode.
s=s.replace('mutableStateOf("timer")', 'mutableStateOf(System.getenv("PRESENCE_PREVIEW_PAGE")?.takeIf { System.getenv("PRESENCE_PREVIEW_DIR") != null && it in setOf("timer", "projects", "stats", "settings") } ?: "timer")')
s=s.replace('import app.presence.R\n', '').replace('Image(painterResource(R.drawable.presence_mark), "Presence",', 'PresenceBrand(')
# Desktop uses maximize/restore, while Android owns immersive fullscreen.
s=section(s,'                if (tab == "timer" && !pairing) IconButton(', '            }\n        }\n    }\n    // Keep the content/provider', '')
s=s.replace('.widthIn(max = 1200.dp)', '')
s=s.replace('minOf(540.dp, maxHeight - 56.dp, maxWidth * 0.45f)', 'minOf(640.dp, maxHeight - 72.dp, maxWidth * 0.45f)')
s=s.replace('.coerceIn(36f, 84f)', '.coerceIn(36f, 144f)').replace('.coerceIn(28f, 60f)', '.coerceIn(28f, 104f)')
s=s.replace('Modifier.width(320.dp)', 'Modifier.width((LocalUiLayout.current.width * 0.28f).coerceIn(320.dp, 440.dp))')
s=s.replace('height(160.dp)', 'height((LocalUiLayout.current.height * 0.24f).coerceIn(180.dp, 300.dp))')
s=s.replace('height(196.dp)', 'height((LocalUiLayout.current.height * 0.27f).coerceIn(216.dp, 320.dp))')
s=s.replace('size(176.dp)', 'size((LocalUiLayout.current.height * 0.24f).coerceIn(196.dp, 288.dp))')
s=s.replace('DropdownMenu(', 'DesktopDropdownMenu(')
s=s.replace('            "system" to t.paletteSystem,\n', '')
s=s.replace('var projectMenu by rememberSaveable { mutableStateOf(false) }', 'var projectMenu by rememberSaveable { mutableStateOf(System.getenv("PRESENCE_PREVIEW_DIR") != null && System.getenv("PRESENCE_PREVIEW_MENU") == "project") }')
s=s.replace('SystemClock.elapsedRealtime()', 'monotonicMillis()')
s=s.replace('app.presence.data.Globals', 'DesktopGlobals')
s=s.replace('            android.util.Log.e("Presence", "Timer action failed", error)', '            error.printStackTrace()')
s='\n'.join(line for line in s.split('\n') if 'NotificationManagerCompat.from(context)' not in line)
s=s.replace('context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), t.share))', 'shareText(text, t.share)')
s=s.replace('rememberLauncherForActivityResult','rememberFileLauncher').replace('ActivityResultContracts.','FileContracts.')
s=s.replace('AppStorage.privateDir(context)', 'app.repo.directory.toFile()')
s=s.replace('!pending.renameTo(java.io.File(directory, "alarm-custom"))', '!replaceCustomAudio(pending, java.io.File(directory, "alarm-custom"))')
s=s.replace('                if (pending.length() == 0L', '                validateCustomAudio(pending)\n                if (pending.length() == 0L')
s=section(s,'    val notifyPermission = rememberFileLauncher', '    val theme = flags', '    val askNotify: () -> Unit = { app.notifier.requestPermission() }\n')
s=s.replace('Reminders.reschedule(context)', 'app.reminders.reschedule()').replace('; HomeWidgets.update(context)','').replace('HomeWidgets.update(context); ','')
s=section(s,'    val storagePane: @Composable () -> Unit =', '    val aboutPane: @Composable () -> Unit =', '    val storagePane: @Composable () -> Unit = { DesktopStoragePane(app, t) }\n')
s=section(s,'    var denied by rememberSaveable', '    LaunchedEffect(offer)', '    val withLink: (() -> Unit) -> Unit = { block -> block() }\n')
s=section(s,'            if (denied) SectionCard', '            if (LocalUiLayout.current.twoPanes)', '')
s=s.replace('android.util.Log.e("Presence", "Action failed", error)', 'error.printStackTrace()')
s=s.replace('android.widget.Toast.makeText(context, failure, android.widget.Toast.LENGTH_LONG).show()', 'showError(failure)')
s=s.replace('DatePickerDialog(', 'DesktopDatePicker(').replace('TimePickerDialog(', 'DesktopTimePicker(')
assert 'android.' not in s, [line for line in s.splitlines() if 'android.' in line]
(out/'Screens.kt').write_text(s,encoding='utf-8')
s=base((ui/'Design.kt').read_text(encoding='utf-8')).replace('androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp', 'LocalUiLayout.current.height')
s=s.replace('val rail get() = width > height', 'val rail get() = width >= 640.dp')
s=s.replace('.padding(horizontal = LocalUiLayout.current.margin, vertical = if (compact) 0.dp else 8.dp)', '.padding(start = LocalUiLayout.current.margin, end = 152.dp, top = if (compact) 0.dp else 8.dp, bottom = if (compact) 0.dp else 8.dp)')
s=s.replace('        Text(title, style = MaterialTheme.typography.headlineMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)', '        LocalHeaderDrag.current(Modifier.weight(1f).height(if (compact) 40.dp else 56.dp)) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) { Text(title, style = MaterialTheme.typography.headlineMedium, maxLines = 2, overflow = TextOverflow.Ellipsis) } }')
(out/'Design.kt').write_text(s,encoding='utf-8')
(out/'L10n.kt').write_text(base((ui/'L10n.kt').read_text(encoding='utf-8')).replace('System colors follow your wallpaper on Android 12+. Older devices use Grove. Or choose a fixed palette.', 'Choose a color palette.'),encoding='utf-8')
s=base((ui/'Introduction.kt').read_text(encoding='utf-8')).replace('import app.presence.R\n','').replace('R.drawable.ic_launcher_fg', '"presence.png"')
(out/'Introduction.kt').write_text(s,encoding='utf-8')
s=base((ui/'Theme.kt').read_text(encoding='utf-8'))
s=s.replace('platformColorScheme(androidx.compose.ui.platform.LocalContext.current, dark, palette)', 'presenceColorScheme(dark, palette)')
for line in ['import android.app.Activity\n','import androidx.compose.runtime.SideEffect\n','import androidx.compose.ui.platform.LocalView\n','import androidx.core.view.WindowCompat\n','import app.presence.R\n']:
    s=s.replace(line,'')
s=s.replace('import androidx.compose.ui.text.font.Font\n','import androidx.compose.ui.text.platform.Font\n')
for weight in ['regular','medium','semibold']:
    s=s.replace('R.font.source_sans_3_'+weight,'"font/source_sans_3_'+weight+'.ttf"')
s=section(s,'    val view = LocalView.current', '    fun scaled', '')
(out/'Theme.kt').write_text(s,encoding='utf-8')
repo=(root/'app/src/main/java/app/presence/data/Repo.kt').read_text(encoding='utf-8')
globals=repo[repo.index('data class Globals('):repo.index('\ndata class Todo(')]
(out/'Globals.kt').write_text('package app.presence.desktop\n\n'+globals.replace('class Globals(', 'class DesktopGlobals('),encoding='utf-8')
print('Adapted Android UI: screens, design, translations, typography, palettes, introduction and settings model')
s=(root/'app/src/main/java/app/presence/timer/Center.kt').read_text(encoding='utf-8').replace('package app.presence.timer','package app.presence.desktop').replace('import app.presence.data.Repo\n','').replace('private val repo: Repo','private val repo: Store').replace('android.os.SystemClock.elapsedRealtime()', 'monotonicMillis()')
(out/'Center.kt').write_text(s,encoding='utf-8')
defaults=repo[repo.index('            val defaults = mapOf('):repo.index('            val missing = defaults')]
reset=repo[repo.index('            val prefs = listOf('):repo.index('            dao.upsert(',repo.index('    fun resetSettings()'))]
(out/'Defaults.kt').write_text('package app.presence.desktop\n\n'+defaults.replace('val defaults','val ANDROID_DEFAULT_PREFS')+'\n'+reset.replace('val prefs','val ANDROID_RESET_PREFS'),encoding='utf-8')
