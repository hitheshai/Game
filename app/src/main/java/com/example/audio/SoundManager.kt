package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundManager(private val context: Context) {
    var isSoundEnabled: Boolean = true
    var soundVolume: Float = 0.8f

    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)
    private var ambientJob: Job? = null
    private var currentAmbientType: String? = null

    // Pentatonic scale frequencies in Hz (C5, D5, E5, G5, A5, C6, D6, E6)
    val pentatonicFrequencies = floatArrayOf(
        523.25f, // C5
        587.33f, // D5
        659.25f, // E5
        783.99f, // G5
        880.00f, // A5
        1046.50f, // C6
        1174.66f, // D6
        1318.51f  // E6
    )

    fun playChime(frequencyIndex: Int = 0) {
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        val freq = pentatonicFrequencies.getOrElse(frequencyIndex % pentatonicFrequencies.size) { 523.25f }
        scope.launch(Dispatchers.Default) {
            synthesizeAndPlayTone(
                freq = freq,
                durationMs = 450,
                attackMs = 15,
                decayExponent = 5.0f,
                harmonics = true
            )
        }
    }

    fun playBubblePop() {
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        scope.launch(Dispatchers.Default) {
            // Swept sine wave for realistic water/bubble pop sound
            val numSamples = (sampleRate * 0.12).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / sampleRate
                // Frequency sweeps rapidly from 350Hz up to 900Hz then decays
                val instantaneousFreq = 350f + (550f * (i.toFloat() / numSamples))
                val env = exp(-t * 30.0).toFloat()
                val sample = sin(2.0 * PI * instantaneousFreq * t).toFloat() * env * soundVolume
                buffer[i] = (sample * Short.MAX_VALUE * 0.7f).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playSingingBowl() {
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        scope.launch(Dispatchers.Default) {
            // Deep 220Hz / 440Hz warm resonant singing bowl with long gentle decay
            val numSamples = (sampleRate * 1.5).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / sampleRate
                val env = exp(-t * 2.2).toFloat()
                val f1 = sin(2.0 * PI * 216.0 * t).toFloat()
                val f2 = 0.5f * sin(2.0 * PI * 432.0 * t).toFloat()
                val f3 = 0.25f * sin(2.0 * PI * 648.0 * t).toFloat()
                val sample = (f1 + f2 + f3) * 0.45f * env * soundVolume
                buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playSuccessChord() {
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        scope.launch(Dispatchers.Default) {
            // Pleasant 3-note arpeggio C5 -> E5 -> G5
            val chordNotes = listOf(523.25f, 659.25f, 783.99f, 1046.50f)
            for (freq in chordNotes) {
                synthesizeAndPlayTone(freq, durationMs = 300, attackMs = 10, decayExponent = 4.0f)
                delay(75)
            }
        }
    }

    fun playSoftClick() {
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        scope.launch(Dispatchers.Default) {
            synthesizeAndPlayTone(freq = 800f, durationMs = 40, attackMs = 2, decayExponent = 40.0f)
        }
    }

    fun toggleAmbient(type: String) { // "rain", "ocean", "zen_bell"
        if (currentAmbientType == type && ambientJob != null) {
            stopAmbient()
            return
        }
        stopAmbient()
        currentAmbientType = type
        if (!isSoundEnabled) return

        ambientJob = scope.launch(Dispatchers.Default) {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufSize, 4096)
            var audioTrack: AudioTrack? = null
            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack.play()
                val chunk = ShortArray(bufferSize)
                var lfoPhase = 0.0
                var filteredNoise = 0.0f

                while (isActive && isSoundEnabled && currentAmbientType == type) {
                    val vol = soundVolume * 0.45f
                    for (i in 0 until bufferSize) {
                        lfoPhase += 0.00015
                        val swell = (0.5 + 0.5 * sin(lfoPhase)).toFloat()
                        // Pink/brown filtered noise
                        val rawNoise = (Random.nextFloat() * 2f - 1f)
                        filteredNoise = filteredNoise * 0.88f + rawNoise * 0.12f

                        val sample = when (type) {
                            "ocean" -> filteredNoise * swell * vol
                            "rain" -> (filteredNoise * 0.8f + (Random.nextFloat() * 2f - 1f) * 0.1f) * 0.5f * vol
                            else -> filteredNoise * 0.4f * vol
                        }
                        chunk[i] = (sample * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                            .toShort()
                    }
                    audioTrack.write(chunk, 0, bufferSize)
                }
            } catch (e: Exception) {
                // Ignore audio interruption
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Exception) {}
            }
        }
    }

    fun stopAmbient() {
        ambientJob?.cancel()
        ambientJob = null
        currentAmbientType = null
    }

    fun getActiveAmbient(): String? = currentAmbientType

    private fun synthesizeAndPlayTone(
        freq: Float,
        durationMs: Int,
        attackMs: Int = 10,
        decayExponent: Float = 6.0f,
        harmonics: Boolean = false
    ) {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val attackSamples = (sampleRate * (attackMs / 1000.0)).toInt()

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = if (i < attackSamples) {
                i.toFloat() / attackSamples
            } else {
                exp(-((i - attackSamples).toFloat() / sampleRate) * decayExponent).toFloat()
            }
            var s = sin(2.0 * PI * freq * t).toFloat()
            if (harmonics) {
                s += 0.35f * sin(2.0 * PI * (freq * 2) * t).toFloat()
                s += 0.15f * sin(2.0 * PI * (freq * 3) * t).toFloat()
            }
            val sample = s * 0.55f * env * soundVolume
            buffer[i] = (sample * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        playPcmBuffer(buffer)
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            scope.launch {
                delay((buffer.size * 1000L / sampleRate) + 200)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
