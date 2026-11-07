package com.example.model

enum class DjEffectType(val displayName: String) {
    NONE("None"),
    BEAT_GRID("Beat Grid"),
    ECHO("Echo"),
    FLANGER("Flanger"),
    REVERB("Reverb"),
    VINYL_BRAKE("Vinyl Brake"),
    BITCRUSHER("Bitcrusher"),
    FILTER_SWEEP("Filter Sweep")
}

data class DeckState(
    val deckId: DeckId,
    val track: DjTrack? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 180_000L,
    val pitchPercent: Float = 0f, // -16% to +16%
    val baseBpm: Float = 128.0f,
    val isSyncActive: Boolean = false,
    val isKeyLocked: Boolean = true,
    val isVinylMode: Boolean = true,
    val isSlipMode: Boolean = false,
    val volume: Float = 0.85f, // 0..1 vertical channel fader
    val gainDb: Float = 0f, // -12..+12 dB
    val eqHighDb: Float = 0f, // -12..+12 dB
    val eqMidDb: Float = 0f,
    val eqLowDb: Float = 0f,
    val isHighKill: Boolean = false,
    val isMidKill: Boolean = false,
    val isLowKill: Boolean = false,
    val filterCutoff: Float = 0f, // -1..+1, <0 LPF, >0 HPF, 0 Neutral
    val selectedLoopBeats: Float = 4f, // 1/16, 1/8, 1/4, 1/2, 1, 2, 4, 8, 16
    val isLoopActive: Boolean = false,
    val loopStartMs: Long = 0L,
    val loopEndMs: Long = 0L,
    val cuePositionMs: Long = 0L,
    val hotCues: Map<Int, Long> = emptyMap(),
    val activeEffect: DjEffectType = DjEffectType.BEAT_GRID,
    val effectDryWet: Float = 0.5f,
    val isEffectActive: Boolean = false,
    val jogAngleDeg: Float = 0f,
    val isScratching: Boolean = false
) {
    val currentBpm: Float
        get() = baseBpm * (1f + pitchPercent / 100f)

    val remainingMs: Long
        get() = (durationMs - positionMs).coerceAtLeast(0L)
}
