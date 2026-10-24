package app.presence.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import app.presence.core.Tones
import app.presence.data.AppStorage
import java.io.File

class Player(private val context: Context) {
    private var loop: AudioTrack? = null
    private var loopKind: String? = null
    private var alarm: AudioTrack? = null
    private var media: MediaPlayer? = null

    @Synchronized
    fun stopLoop() {
        loop?.let { track ->
            try {
                track.pause()
                track.flush()
                track.release()
            } catch (_: Exception) {
            }
        }
        loop = null
        loopKind = null
    }

    @Synchronized
    fun loop(kind: String, volume: Float) {
        if (kind == "off") {
            stopLoop()
            return
        }
        if (loopKind == kind && loop != null) {
            loop?.setVolume(volume.coerceIn(0f, 1f))
            return
        }
        stopLoop()
        val pcm = when (kind) {
            "rain" -> Tones.rain()
            "brook" -> Tones.brook()
            "hearth" -> Tones.hearth()
            else -> return
        }
        val track = staticTrack(pcm, AudioAttributes.USAGE_MEDIA, AudioAttributes.CONTENT_TYPE_MUSIC, looping = true)
        track.setVolume(volume.coerceIn(0f, 1f))
        track.play()
        loop = track
        loopKind = kind
    }

    @Synchronized
    fun alarm(kind: String) {
        alarm?.release()
        alarm = null
        media?.release()
        media = null
        val custom = File(AppStorage.privateDir(context), "alarm-custom")
        if (kind == "custom" && custom.exists()) {
            try {
                val player = MediaPlayer()
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                player.setDataSource(custom.absolutePath)
                player.setOnCompletionListener {
                    synchronized(this) {
                        if (media === it) { media = null; it.release() }
                    }
                }
                player.prepare()
                player.start()
                media = player
                return
            } catch (_: Exception) {
            }
        }
        val pcm = when (kind) {
            "bell" -> Tones.bell()
            "pulse" -> Tones.pulse()
            else -> Tones.chime()
        }
        val track = staticTrack(pcm, AudioAttributes.USAGE_ALARM, AudioAttributes.CONTENT_TYPE_SONIFICATION, looping = false)
        track.setVolume(1f)
        track.play()
        alarm = track
    }

    private fun staticTrack(pcm: ShortArray, usage: Int, content: Int, looping: Boolean): AudioTrack {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder().setUsage(usage).setContentType(content).build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(Tones.RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        track.write(pcm, 0, pcm.size)
        if (looping) track.setLoopPoints(0, pcm.size, -1)
        return track
    }
}
