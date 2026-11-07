package com.example.model

enum class SampleType {
    SAXO,
    THIS_THIS,
    SHOTS,
    HANDS_UP,
    AIRHORN,
    SIREN,
    LASER,
    VINYL_DROP
}

data class SamplerPad(
    val id: Int,
    val name: String,
    val sampleType: SampleType,
    val isPlaying: Boolean = false
)

data class RecordedMix(
    val id: String,
    val title: String,
    val durationFormatted: String,
    val filePath: String,
    val timestamp: Long,
    val fileSizeFormatted: String
)
