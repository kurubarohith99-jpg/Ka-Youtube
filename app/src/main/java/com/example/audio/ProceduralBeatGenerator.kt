package com.example.audio

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object ProceduralBeatGenerator {

    private const val SAMPLE_RATE = 44100
    private const val NUM_CHANNELS = 2
    private const val BITS_PER_SAMPLE = 16

    /**
     * Generates a rhythmic dance track stem with kick, clap, hi-hats, bassline, and synth chord.
     * Writes to a standard WAV file in the app cache and returns the file path.
     */
    fun generateTrackWav(
        context: Context,
        fileName: String,
        bpm: Float,
        keyFreq: Float = 130.81f, // C3
        styleVariant: Int = 0
    ): File {
        val cacheFile = File(context.cacheDir, "$fileName.wav")
        if (cacheFile.exists() && cacheFile.length() > 44) {
            return cacheFile
        }

        val secondsPerBeat = 60.0 / bpm
        val bars = 8 // 8 bars = 32 beats
        val totalBeats = bars * 4
        val totalDurationSec = totalBeats * secondsPerBeat
        val totalSamples = (totalDurationSec * SAMPLE_RATE).toInt()

        val pcmBuffer = ShortArray(totalSamples * NUM_CHANNELS)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val beatPos = (t / secondsPerBeat) % totalBeats
            val beatInBar = beatPos % 4.0
            val subBeat = beatPos % 1.0 // 0..1 within a beat

            var sampleL = 0.0
            var sampleR = 0.0

            // 1. Four-on-the-floor Kick Drum (on every beat 0, 1, 2, 3)
            val kickTime = subBeat * secondsPerBeat
            if (kickTime < 0.28) {
                // Pitch envelope drops from 150Hz to 48Hz
                val kickFreq = 48.0 + 105.0 * exp(-kickTime * 28.0)
                val kickEnv = exp(-kickTime * 12.0)
                val kickWave = sin(2.0 * PI * kickFreq * kickTime) * kickEnv * 0.75
                sampleL += kickWave
                sampleR += kickWave
            }

            // 2. Snare / Clap on beats 1 and 3 (in 0-indexed: beat 1 and 3)
            val isClapBeat = beatInBar >= 1.0 && beatInBar < 2.0 || beatInBar >= 3.0
            val clapTime = subBeat * secondsPerBeat
            if (isClapBeat && clapTime < 0.22) {
                val noise = (Math.random() * 2.0 - 1.0)
                val tone = sin(2.0 * PI * 220.0 * clapTime) * 0.3
                val clapEnv = exp(-clapTime * 18.0)
                val clapWave = (noise * 0.7 + tone) * clapEnv * 0.45
                sampleL += clapWave * 0.95
                sampleR += clapWave * 1.05
            }

            // 3. Off-beat Hi-Hat (on every 0.5 beat)
            val hiHatPos = (beatPos * 2.0) % 1.0
            val hiHatTime = hiHatPos * (secondsPerBeat / 2.0)
            if (hiHatPos < 0.5 && hiHatTime < 0.09) {
                val hatNoise = (Math.random() * 2.0 - 1.0)
                val hatEnv = exp(-hiHatTime * 45.0)
                val hatWave = hatNoise * hatEnv * 0.22
                sampleL += hatWave * 1.1
                sampleR += hatWave * 0.9
            }

            // 4. Bassline arpeggio (sidechained to kick)
            val sixteenth = (beatPos * 4.0).toInt() % 16
            val bassFreqMult = when (styleVariant % 3) {
                0 -> when (sixteenth % 4) {
                    0 -> 1.0
                    1 -> 1.25 // Major 3rd
                    2 -> 1.5 // 5th
                    else -> 1.0
                }
                1 -> when (sixteenth % 4) {
                    0 -> 1.0
                    1 -> 1.189 // Minor 3rd
                    2 -> 1.498 // 5th
                    else -> 1.334 // 4th
                }
                else -> when (sixteenth % 8) {
                    0, 2, 4 -> 1.0
                    6 -> 1.5
                    else -> 1.12
                }
            }
            val bassFreq = keyFreq * bassFreqMult
            val bassT = (beatPos * 4.0 % 1.0) * (secondsPerBeat / 4.0)
            val bassEnv = exp(-bassT * 8.0)
            // Sidechain pumping: dips when kick hits
            val sidechain = (subBeat * 2.0).coerceIn(0.15, 1.0)
            val bassWave = (sin(2.0 * PI * bassFreq * t) + 0.3 * sin(2.0 * PI * bassFreq * 2.0 * t)) * bassEnv * sidechain * 0.45
            sampleL += bassWave
            sampleR += bassWave

            // 5. Synth Stabs / Arpeggio
            val synthT = t % (secondsPerBeat * 2.0)
            val synthFreq = keyFreq * 4.0 * if (beatInBar < 2.0) 1.0 else 1.25
            val synthEnv = (1.0 - (subBeat * 0.8)).coerceIn(0.0, 1.0) * 0.15
            val synthWave = (sin(2.0 * PI * synthFreq * t) + 0.5 * sin(2.0 * PI * (synthFreq * 1.5) * t)) * synthEnv
            sampleL += synthWave * 0.85
            sampleR += synthWave * 1.15

            // Clip & write 16-bit PCM
            val clampedL = (sampleL * 0.85).coerceIn(-1.0, 1.0)
            val clampedR = (sampleR * 0.85).coerceIn(-1.0, 1.0)

            pcmBuffer[i * 2] = (clampedL * 32767.0).toInt().toShort()
            pcmBuffer[i * 2 + 1] = (clampedR * 32767.0).toInt().toShort()
        }

        writeWavFile(cacheFile, pcmBuffer, SAMPLE_RATE, NUM_CHANNELS, BITS_PER_SAMPLE)
        return cacheFile
    }

    private fun writeWavFile(
        file: File,
        pcmData: ShortArray,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) {
        val byteData = ByteArray(pcmData.size * 2)
        ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(pcmData)

        val totalDataLen = byteData.size + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8

        FileOutputStream(file).use { out ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray())
            header.putInt(totalDataLen)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16) // SubChunk1Size (16 for PCM)
            header.putShort(1.toShort()) // AudioFormat (1 for PCM)
            header.putShort(channels.toShort())
            header.putInt(sampleRate)
            header.putInt(byteRate)
            header.putShort((channels * bitsPerSample / 8).toShort()) // BlockAlign
            header.putShort(bitsPerSample.toShort())
            header.put("data".toByteArray())
            header.putInt(byteData.size)

            out.write(header.array())
            out.write(byteData)
        }
    }

    /**
     * Generates a precomputed waveform representation (normalized amplitudes 0..1)
     * for realistic display on Virtual DJ waveforms.
     */
    fun generateWaveformPoints(pointsCount: Int = 200, bpm: Float = 128f): List<Float> {
        val points = mutableListOf<Float>()
        for (i in 0 until pointsCount) {
            val beatPhase = (i.toFloat() / (pointsCount / 32f)) % 4f
            val isKick = (beatPhase % 1f) < 0.25f
            val isSnare = (beatPhase >= 1f && beatPhase < 1.3f) || (beatPhase >= 3f && beatPhase < 3.3f)
            val base = if (isKick) 0.85f else if (isSnare) 0.75f else 0.35f
            val variation = (sin(i * 0.35f) * 0.15f) + ((Math.random().toFloat() - 0.5f) * 0.1f)
            points.add((base + variation).coerceIn(0.12f, 0.98f))
        }
        return points
    }
}
