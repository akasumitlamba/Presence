package app.presence.desktop

import app.presence.core.*
import java.util.concurrent.*
import java.time.ZoneId
import java.awt.*
import javax.sound.sampled.*
import java.io.ByteArrayInputStream
import java.nio.file.Files

class DesktopAudio(private val store: Store) : AutoCloseable {
    private var loop: Clip? = null
    private var alarm: Clip? = null
    private var looping = "off"
    private val executor = Executors.newSingleThreadExecutor { r -> Thread(r,"presence-audio").apply { isDaemon = true } }
    private fun clip(samples: ShortArray): Clip {
        val bytes = ByteArray(samples.size*2)
        samples.forEachIndexed { i,v -> bytes[i*2] = (v.toInt() and 255).toByte(); bytes[i*2+1] = (v.toInt() shr 8).toByte() }
        val format = AudioFormat(Tones.RATE.toFloat(),16,1,true,false)
        return AudioSystem.getClip().apply { open(format,bytes,0,bytes.size) }
    }
    fun update(snap: Snap) { executor.execute {
        val g = store.globals()
        val kind = if (snap.status == "running" && snap.kind in setOf("focus","track")) g.background else "off"
        try {
            if (kind != looping) {
                loop?.close(); loop = null; looping = "off"
                val samples = when(kind) { "rain" -> Tones.rain(); "brook" -> Tones.brook(); "hearth" -> Tones.hearth(); else -> null }
                if (samples != null) { loop = clip(samples); looping = kind; loop?.loop(Clip.LOOP_CONTINUOUSLY) }
            }
            loop?.let { c -> if (c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                val gain = c.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
                gain.value = (if(g.volume <= 0) gain.minimum else (20 * kotlin.math.log10(g.volume / 100.0)).toFloat()).coerceIn(gain.minimum,gain.maximum)
            } }
        } catch (e: Exception) { looping = "off"; System.err.println("Audio unavailable: ${e.message}") }
    } }
    fun chime() { executor.execute {
        try {
            alarm?.close()
            alarm = when(store.globals().alarm) {
                "custom" -> decodedAudio(store.directory.resolve("alarm-custom").toFile()).use { stream -> AudioSystem.getClip().apply { open(stream) } }
                "bell" -> clip(Tones.bell()); "pulse" -> clip(Tones.pulse()); else -> clip(Tones.chime())
            }
            alarm?.start()
        } catch (e: Exception) { System.err.println("Alarm unavailable: ${e.message}"); Toolkit.getDefaultToolkit().beep() }
    } }
    override fun close() { executor.shutdownNow(); loop?.close(); alarm?.close() }
}
class DesktopNotifier {
    @Volatile var tray: TrayIcon? = null
    fun requestPermission() { /* Desktop notifications have no Android runtime permission. */ }
    fun show(title: String,text: String) {
        val icon = tray
        if (icon != null) EventQueue.invokeLater { icon.displayMessage(title,text,TrayIcon.MessageType.INFO) }
        else EventQueue.invokeLater { javax.swing.JOptionPane.showMessageDialog(null,text,title,javax.swing.JOptionPane.INFORMATION_MESSAGE) }
    }
}
class DesktopReminders(private val repo: Store,private val notifier: DesktopNotifier) : AutoCloseable {
    private val worker = Executors.newSingleThreadScheduledExecutor { r -> Thread(r,"presence-reminders").apply { isDaemon = true } }
    private val futures = mutableListOf<ScheduledFuture<*>>()
    @Synchronized fun reschedule() {
        futures.forEach { it.cancel(false) }; futures.clear()
        val g = repo.globals()
        fun schedule(kind: String,hour: Int,minute: Int) {
            val now = System.currentTimeMillis(); val zone = ZoneId.systemDefault()
            val whenMs = nextClockMillis(hour,minute,now,zone)
            futures += worker.schedule({
                val t = T(resolveLang(repo.flag("language","system")))
                if(kind == "streak") notifier.show(t.streak,t.streakLine(streakDays(repo.snapshot(),System.currentTimeMillis(),zone,repo.globals().dayStart),loggedToday(repo.snapshot(),System.currentTimeMillis(),zone,repo.globals().dayStart)))
                else notifier.show(t.dailyReminder,t.timeToFocus)
                reschedule()
            },(whenMs-now).coerceAtLeast(1),TimeUnit.MILLISECONDS)
        }
        if(g.dailyOn) schedule("daily",g.dailyHour,g.dailyMinute)
        if(g.streakOn) schedule("streak",g.streakHour,g.streakMinute)
    }
    override fun close() { worker.shutdownNow() }
}
class PresenceApp(val repo: Store) : AutoCloseable {
    val notifier = DesktopNotifier()
    val audio = DesktopAudio(repo)
    val reminders = DesktopReminders(repo,notifier)
    val lan = DesktopLanAdapter(runCatching { DesktopLan(repo) }.getOrNull(),repo)
    @Volatile var openWidget: ((String) -> Unit)? = null
    @Volatile var back: (() -> Unit)? = null
    val center: TimerCenter
    private val timerWorker = Executors.newSingleThreadScheduledExecutor { r -> Thread(r,"presence-timer").apply { isDaemon = true } }
    init {
        reanchorTimer(repo)
        center = TimerCenter(repo) { step ->
            audio.update(step.snap)
            if(step.chime) {
                audio.chime()
                val t = T(resolveLang(repo.flag("language","system")))
                notifier.show(t.finished,step.snap.projectName.ifBlank { "Presence" })
            }
        }
        center.catchUp()
        reminders.reschedule()
        timerWorker.scheduleWithFixedDelay({ try { center.due(null) } catch(e: Exception) { System.err.println("Timer save failed: ${e.message}") } },200,200,TimeUnit.MILLISECONDS)
    }
    fun hasLan() = lan.available
    override fun close() { timerWorker.shutdownNow(); reminders.close(); audio.close(); lan.close() }
}

fun decodedAudio(file: java.io.File): AudioInputStream {
    var source = AudioSystem.getAudioInputStream(file)
    try {
        val format = source.format
        if(format.encoding.toString().contains("VORBIS",ignoreCase = true)) {
            source.close()
            // Let the Vorbis SPI drain a short file's final buffered page before reaching EOF.
            val encoded = java.io.SequenceInputStream(Files.newInputStream(file.toPath()),ByteArrayInputStream(ByteArray(8192)))
            source = AudioInputStream(encoded,format,AudioSystem.NOT_SPECIFIED.toLong())
        }
        if(format.encoding == AudioFormat.Encoding.PCM_SIGNED && format.sampleSizeInBits == 16) return source
        val pcm = AudioFormat(AudioFormat.Encoding.PCM_SIGNED,format.sampleRate,16,format.channels,format.channels*2,format.sampleRate,false)
        val bigEndian = AudioFormat(AudioFormat.Encoding.PCM_SIGNED,format.sampleRate,16,format.channels,format.channels*2,format.sampleRate,true)
        val codecFormat = when {
            AudioSystem.isConversionSupported(pcm,format) -> pcm
            AudioSystem.isConversionSupported(bigEndian,format) -> bigEndian
            else -> throw UnsupportedAudioFileException("No PCM decoder for $format")
        }
        val decoded = AudioSystem.getAudioInputStream(codecFormat,source)
        return if(codecFormat.matches(pcm)) decoded else AudioSystem.getAudioInputStream(pcm,decoded)
    } catch(e: Exception) { source.close(); throw e }
}
fun validateCustomAudio(file: java.io.File) {
    decodedAudio(file).use { stream ->
        val buffer = ByteArray(4096)
        val deadline = System.nanoTime() + 5_000_000_000L
        var read = stream.read(buffer)
        // Some asynchronous Java Sound providers return zero before their first decoded frame.
        while(read == 0 && System.nanoTime() < deadline) { Thread.yield(); read = stream.read(buffer) }
        require(read > 0) { "The audio file is empty or unsupported" }
    }
}

fun replaceCustomAudio(pending: java.io.File, target: java.io.File): Boolean {
    return try {
        try { Files.move(pending.toPath(),target.toPath(),java.nio.file.StandardCopyOption.ATOMIC_MOVE,java.nio.file.StandardCopyOption.REPLACE_EXISTING) }
        catch(_: java.nio.file.AtomicMoveNotSupportedException) { Files.move(pending.toPath(),target.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING) }
        true
    } catch(_: Exception) { false }
}
