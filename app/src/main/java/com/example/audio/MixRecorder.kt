package com.example.audio

import android.content.Context
import com.example.model.RecordedMix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MixRecorder(private val context: Context) {

    private val _recordedMixes = MutableStateFlow<List<RecordedMix>>(emptyList())
    val recordedMixes: StateFlow<List<RecordedMix>> = _recordedMixes.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _durationSec = MutableStateFlow(0)
    val durationSec: StateFlow<Int> = _durationSec.asStateFlow()

    private var currentFile: File? = null
    private var fileOutputStream: FileOutputStream? = null
    private var timerJob: Job? = null
    private var sampleWriterJob: Job? = null
    private var bytesWritten = 0L

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        loadExistingMixes()
    }

    private fun loadExistingMixes() {
        scope.launch {
            val dir = getRecordingsDir()
            val files = dir.listFiles { f -> f.extension.equals("wav", ignoreCase = true) }
                ?.sortedByDescending { it.lastModified() } ?: emptyList()

            val list = files.map { f ->
                val sizeKb = f.length() / 1024
                val sizeStr = if (sizeKb > 1024) "${sizeKb / 1024} MB" else "$sizeKb KB"
                // Estimate duration assuming 44.1kHz 16bit stereo = 176400 bytes/sec
                val durSec = ((f.length() - 44).coerceAtLeast(0) / 176400).toInt()
                val min = durSec / 60
                val sec = durSec % 60
                val durStr = String.format(Locale.US, "%02d:%02d", min, sec)

                RecordedMix(
                    id = f.name,
                    title = f.nameWithoutExtension,
                    durationFormatted = durStr,
                    filePath = f.absolutePath,
                    timestamp = f.lastModified(),
                    fileSizeFormatted = sizeStr
                )
            }
            _recordedMixes.value = list
        }
    }

    fun startRecording(customTitle: String? = null) {
        if (_isRecording.value) return

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val name = customTitle?.takeIf { it.isNotBlank() } ?: "Mix_$timestamp"
        val file = File(getRecordingsDir(), "$name.wav")
        currentFile = file
        bytesWritten = 0L

        try {
            val fos = FileOutputStream(file)
            fileOutputStream = fos
            // Write 44-byte placeholder header
            val placeholderHeader = ByteArray(44)
            fos.write(placeholderHeader)

            _isRecording.value = true
            _durationSec.value = 0

            // Start timer
            timerJob = scope.launch {
                while (isActive && _isRecording.value) {
                    delay(1000)
                    _durationSec.value += 1
                }
            }

            // Continuous mock audio writer simulating captured mixed master output
            sampleWriterJob = scope.launch {
                val buffer = ByteArray(4096)
                while (isActive && _isRecording.value) {
                    try {
                        fileOutputStream?.write(buffer)
                        bytesWritten += buffer.size
                        delay(23) // Approx 44100 * 4 bytes/sec = 176.4 kb/sec -> 4096 bytes every 23ms
                    } catch (_: Exception) {
                        break
                    }
                }
            }
        } catch (_: Exception) {
            _isRecording.value = false
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        _isRecording.value = false
        timerJob?.cancel()
        sampleWriterJob?.cancel()

        try {
            fileOutputStream?.flush()
            fileOutputStream?.close()
            fileOutputStream = null

            // Finalize WAV header
            currentFile?.let { finalizeWavHeader(it, bytesWritten) }
        } catch (_: Exception) {
        }

        loadExistingMixes()
    }

    private fun finalizeWavHeader(file: File, pcmDataLength: Long) {
        try {
            RandomAccessFile(file, "rw").use { raf ->
                val sampleRate = 44100
                val channels = 2
                val bitsPerSample = 16
                val byteRate = sampleRate * channels * bitsPerSample / 8
                val blockAlign = (channels * bitsPerSample / 8).toShort()
                val totalDataLen = pcmDataLength + 36

                val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
                header.put("RIFF".toByteArray())
                header.putInt(totalDataLen.toInt())
                header.put("WAVE".toByteArray())
                header.put("fmt ".toByteArray())
                header.putInt(16)
                header.putShort(1.toShort()) // PCM
                header.putShort(channels.toShort())
                header.putInt(sampleRate)
                header.putInt(byteRate)
                header.putShort(blockAlign)
                header.putShort(bitsPerSample.toShort())
                header.put("data".toByteArray())
                header.putInt(pcmDataLength.toInt())

                raf.seek(0)
                raf.write(header.array())
            }
        } catch (_: Exception) {
        }
    }

    fun deleteMix(mix: RecordedMix) {
        try {
            File(mix.filePath).delete()
            loadExistingMixes()
        } catch (_: Exception) {
        }
    }

    private fun getRecordingsDir(): File {
        val dir = File(context.filesDir, "recordings")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
