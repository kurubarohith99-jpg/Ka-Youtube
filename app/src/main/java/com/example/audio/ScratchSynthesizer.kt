package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class ScratchSynthesizer {

    private val sampleRate = 22050
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    private var scratchVelocity: Float = 0f

    @Volatile
    private var isScratching: Boolean = false

    init {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = (minBufSize * 2).coerceAtLeast(1024)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
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
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (_: Exception) {
            // AudioTrack init fallback
        }
    }

    fun startScratch(initialVelocity: Float = 1.0f) {
        scratchVelocity = initialVelocity
        isScratching = true

        if (playbackJob?.isActive == true) return

        playbackJob = scope.launch {
            val chunk = ShortArray(256)
            var phase = 0.0

            while (isActive && isScratching) {
                val speed = abs(scratchVelocity).coerceIn(0.1f, 4.0f)
                val baseFreq = 160.0 * speed

                for (i in chunk.indices) {
                    val frictionNoise = (Math.random() * 2.0 - 1.0) * 0.25
                    val harmonic1 = sin(phase) * 0.55
                    val harmonic2 = sin(phase * 2.3) * 0.25
                    val wave = (harmonic1 + harmonic2 + frictionNoise)

                    chunk[i] = (wave * 26000.0 * (speed / 2.0).coerceIn(0.3, 1.0)).toInt().toShort()

                    phase += (2.0 * PI * baseFreq) / sampleRate
                    if (phase > 2.0 * PI) phase -= 2.0 * PI
                }

                try {
                    audioTrack?.write(chunk, 0, chunk.size)
                } catch (_: Exception) {
                    break
                }
            }
        }
    }

    fun updateVelocity(velocity: Float) {
        scratchVelocity = velocity
    }

    fun stopScratch() {
        isScratching = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun release() {
        stopScratch()
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (_: Exception) {
        }
    }
}
