package app.presence.core

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object Tones {
    const val RATE = 22_050

    fun chime(): ShortArray = melody(doubleArrayOf(523.25, 659.25, 783.99), 170)
    fun bell(): ShortArray = tone(392.0, 900, decay = 4.2)
    fun pulse(): ShortArray {
        val beep = tone(660.0, 90, decay = 6.0)
        val gap = ShortArray(RATE / 20)
        return beep + gap + beep + gap + beep
    }

    fun rain(): ShortArray = coloredNoise(RATE, 11, 0.55f)
    fun brook(): ShortArray = coloredNoise(RATE, 29, 0.22f)
    fun hearth(): ShortArray = coloredNoise(RATE, 47, 0.08f)

    private fun melody(freqs: DoubleArray, msEach: Int): ShortArray {
        val parts = freqs.map { tone(it, msEach, decay = 3.4) }
        return parts.reduce { a, b -> a + b }
    }

    private fun tone(freq: Double, ms: Int, decay: Double): ShortArray {
        val n = RATE * ms / 1000
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / RATE
            val env = exp(-decay * t)
            val sample = sin(2.0 * PI * freq * t) * env * 0.45
            out[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        fadeEdges(out, RATE / 100)
        return out
    }

    private fun coloredNoise(frames: Int, seed: Int, cutoff: Float): ShortArray {
        var state = seed.toLong() and 0xffffffffL
        var hold = 0.0
        val out = ShortArray(frames)
        for (i in out.indices) {
            state = (1664525L * state + 1013904223L) and 0xffffffffL
            val white = ((state ushr 16) / 32767.5) - 1.0
            hold += (white - hold) * cutoff
            val sample = (hold * 0.35).coerceIn(-1.0, 1.0)
            out[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        fadeEdges(out, RATE / 80)
        return out
    }

    private fun fadeEdges(pcm: ShortArray, edge: Int) {
        val n = minOf(edge, pcm.size / 2)
        for (i in 0 until n) {
            val g = i.toFloat() / n
            pcm[i] = (pcm[i] * g).toInt().toShort()
            val j = pcm.lastIndex - i
            pcm[j] = (pcm[j] * g).toInt().toShort()
        }
    }
}
