package io.shareit.transfer.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class HappyBirthdayPlayer {

    private var track: AudioTrack? = null
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun start(loop: Boolean = true) {
        if (job?.isActive == true) return
        job = scope.launch {
            while (true) {
                playMelody()
                if (!loop) break
                Thread.sleep(900)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        try {
            track?.stop()
            track?.release()
        } catch (_: Exception) {
        }
        track = null
    }

    private fun playMelody() {
        val sampleRate = 44100
        val notes = HAPPY_BIRTHDAY
        val totalSeconds = notes.sumOf { it.beats.toDouble() } * SECONDS_PER_BEAT
        val totalSamples = (totalSeconds * sampleRate).toInt()

        val buffer = ShortArray(totalSamples)
        var pos = 0
        for (note in notes) {
            val durSec = note.beats * SECONDS_PER_BEAT
            val samples = (durSec * sampleRate).toInt()
            if (note.frequency > 0f) {
                renderNote(buffer, pos, samples, note.frequency.toDouble(), sampleRate)
            }
            pos += samples
        }

        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        val newTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(minBuffer.coerceAtLeast(buffer.size * 2))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        newTrack.write(buffer, 0, buffer.size)
        track = newTrack
        try {
            newTrack.setVolume(AudioTrack.getMaxVolume() * 0.85f)
            newTrack.play()
            val playMs = (totalSeconds * 1000).toLong() + 200L
            Thread.sleep(playMs)
            newTrack.stop()
        } catch (_: Exception) {
        } finally {
            try {
                newTrack.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun renderNote(
        buffer: ShortArray,
        offset: Int,
        samples: Int,
        frequency: Double,
        sampleRate: Int,
    ) {
        val attack = (samples * 0.04).toInt().coerceAtLeast(1)
        val release = (samples * 0.18).toInt().coerceAtLeast(1)
        val sustainEnd = samples - release
        val twoPi = 2.0 * PI
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val angle = twoPi * frequency * t
            val raw = (
                sin(angle) * 0.55 +
                    sin(angle * 2.0) * 0.18 +
                    sin(angle * 3.0) * 0.10 +
                    sin(angle * 4.0) * 0.05
                )
            val env = when {
                i < attack -> i.toDouble() / attack
                i > sustainEnd -> {
                    val r = (i - sustainEnd).toDouble() / release
                    exp(-3.0 * r)
                }
                else -> 1.0
            }
            val sample = (raw * env * Short.MAX_VALUE * 0.55).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            val idx = offset + i
            if (idx < buffer.size) {
                buffer[idx] = sample.toShort()
            }
        }
    }

    companion object {
        private const val BPM = 120.0
        private val SECONDS_PER_BEAT = 60.0 / BPM

        private data class Note(val frequency: Float, val beats: Float)

        private val C5 = 523.25f
        private val D5 = 587.33f
        private val E5 = 659.25f
        private val F5 = 698.46f
        private val G5 = 783.99f
        private val A5 = 880.00f
        private val Bb5 = 932.33f
        private val C6 = 1046.50f
        private val F4 = 349.23f
        private val REST = 0f

        private val HAPPY_BIRTHDAY = listOf(
            Note(F4, 0.75f), Note(F4, 0.25f),
            Note(G5, 1f), Note(F4, 1f), Note(Bb5, 1f), Note(A5, 2f),

            Note(F4, 0.75f), Note(F4, 0.25f),
            Note(G5, 1f), Note(F4, 1f), Note(C6, 1f), Note(Bb5, 2f),

            Note(F4, 0.75f), Note(F4, 0.25f),
            Note(F5, 1f), Note(D5, 1f), Note(Bb5, 1f), Note(A5, 1f), Note(G5, 1f),

            Note(E5, 0.75f), Note(E5, 0.25f),
            Note(D5, 1f), Note(Bb5, 1f), Note(C6, 1f), Note(Bb5, 2f),
            Note(REST, 0.5f),
        )
    }
}
