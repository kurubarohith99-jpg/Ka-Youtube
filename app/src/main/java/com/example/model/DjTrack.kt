package com.example.model

data class DjTrack(
    val id: String,
    val title: String,
    val artist: String,
    val remix: String = "Original Mix",
    val durationMs: Long = 180_000L,
    val bpm: Float = 128.0f,
    val musicalKey: String = "08B",
    val genre: String = "Club / EDM",
    val albumArtUrl: String? = null,
    val coverColorHex: Long = 0xFF1E293B,
    val audioPath: String? = null,
    val isStreamOrYouTube: Boolean = false,
    val waveformPoints: List<Float> = emptyList(),
    val defaultCues: Map<Int, Long> = emptyMap()
)
