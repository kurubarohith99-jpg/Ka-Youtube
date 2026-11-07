package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.model.SampleType
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SamplerManager(private val context: Context) {

    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<SampleType, Int>()

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attrs)
            .build()

        loadSyntheticSamples()
    }

    private fun loadSyntheticSamples() {
        SampleType.entries.forEach { type ->
            try {
                val file = generateSampleWav(type)
                val id = soundPool.load(file.absolutePath, 1)
                soundIds[type] = id
            } catch (_: Exception) {
            }
        }
    }

    fun play(sampleType: SampleType, volume: Float = 1.0f) {
        val soundId = soundIds[sampleType] ?: return
        soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }

    private fun generateSampleWav(type: SampleType): File {
        val file = File(context.cacheDir, "sample_${type.name.lowercase()}.wav")
        if (file.exists() && file.length() > 44) return file

        val sampleRate = 44100
        val durationSec = when (type) {
            SampleType.AIRHORN -> 0.75
            SampleType.SIREN -> 1.0
            SampleType.LASER -> 0.35
            SampleType.SAXO -> 0.8
            SampleType.SHOTS -> 0.6
            SampleType.HANDS_UP -> 0.7
            SampleType.THIS_THIS -> 0.5
            SampleType.VINYL_DROP -> 0.9
        }
        val totalSamples = (durationSec * sampleRate).toInt()
        val pcm = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val sample = when (type) {
                SampleType.AIRHORN -> {
                    // Staccato trumpet blast: fundamental 440Hz + 880Hz + 1320Hz modulated by 12Hz vibrato
                    val env = (1.0 - t / durationSec).coerceIn(0.0, 1.0)
                    val vib = sin(2.0 * PI * 14.0 * t) * 15.0
                    val f = 466.16 + vib // Bb4
                    (sin(2.0 * PI * f * t) + 0.6 * sin(2.0 * PI * (f * 2.0) * t) + 0.4 * sin(2.0 * PI * (f * 3.0) * t)) * env * 0.7
                }
                SampleType.SIREN -> {
                    // Classic reggae DJ sweep siren
                    val f = 600.0 + 400.0 * sin(2.0 * PI * 2.5 * t)
                    val env = if (t < 0.1) t / 0.1 else (1.0 - (t - 0.1) / (durationSec - 0.1))
                    sin(2.0 * PI * f * t) * env * 0.6
                }
                SampleType.LASER -> {
                    // Fast downward pitch chirp
                    val f = 2400.0 * exp(-t * 12.0) + 120.0
                    val env = exp(-t * 8.0)
                    sin(2.0 * PI * f * t) * env * 0.7
                }
                SampleType.SAXO -> {
                    // Jazzy club sax horn rift
                    val noteFreq = if (t < 0.25) 523.25 else if (t < 0.5) 587.33 else 659.25
                    val env = (1.0 - (t % 0.25) / 0.25).coerceIn(0.0, 1.0)
                    (sin(2.0 * PI * noteFreq * t) + 0.5 * sin(2.0 * PI * (noteFreq * 2.0) * t)) * env * 0.6
                }
                SampleType.SHOTS -> {
                    // Heavy industrial shot / impact
                    val f = 80.0 + 300.0 * exp(-t * 30.0)
                    val noise = (Math.random() * 2.0 - 1.0) * exp(-t * 20.0)
                    (sin(2.0 * PI * f * t) * 0.6 + noise * 0.4) * exp(-t * 6.0)
                }
                SampleType.HANDS_UP -> {
                    // Synthesizer rave chord stab
                    val chord = sin(2.0 * PI * 330.0 * t) + sin(2.0 * PI * 415.3 * t) + sin(2.0 * PI * 493.88 * t)
                    chord * exp(-t * 5.0) * 0.35
                }
                SampleType.THIS_THIS -> {
                    // Vocal glitch chop
                    val chopFreq = 260.0 + (if ((t * 8.0).toInt() % 2 == 0) 100.0 else 0.0)
                    val env = exp(-((t % 0.125) * 16.0))
                    sin(2.0 * PI * chopFreq * t) * env * 0.5
                }
                SampleType.VINYL_DROP -> {
                    // Turntable brake / needle scratch down
                    val f = 400.0 * (1.0 - t / durationSec).coerceAtLeast(0.05)
                    val noise = (Math.random() * 2.0 - 1.0) * 0.2
                    (sin(2.0 * PI * f * t) + noise) * (1.0 - t / durationSec) * 0.6
                }
            }
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
        }

        val byteData = ByteArray(pcm.size * 2)
        ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(pcm)

        FileOutputStream(file).use { out ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray())
            header.putInt(byteData.size + 36)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16)
            header.putShort(1.toShort()) // PCM
            header.putShort(1.toShort()) // Mono
            header.putInt(sampleRate)
            header.putInt(sampleRate * 2)
            header.putShort(2.toShort())
            header.putShort(16.toShort())
            header.put("data".toByteArray())
            header.putInt(byteData.size)

            out.write(header.array())
            out.write(byteData)
        }

        return file
    }
}
