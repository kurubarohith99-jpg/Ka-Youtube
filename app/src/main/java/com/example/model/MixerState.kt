package com.example.model

enum class MixerTab {
    MIXER,
    VIDEO,
    SCRATCH,
    MASTER
}

data class MixerState(
    val crossfader: Float = 0.5f, // 0.0 (100% A) -> 0.5 (Equal) -> 1.0 (100% B)
    val masterVolume: Float = 0.9f,
    val headphoneCueA: Boolean = false,
    val headphoneCueB: Boolean = false,
    val vuLevelLeft: Float = 0.65f,
    val vuLevelRight: Float = 0.62f,
    val vuLevelA: Float = 0.7f,
    val vuLevelB: Float = 0.68f,
    val activeTab: MixerTab = MixerTab.MIXER,
    val isRecording: Boolean = false,
    val recordDurationSec: Int = 0
)

data class DjSkin(
    val id: String,
    val name: String,
    val description: String,
    val deckAColor: Long,
    val deckBColor: Long,
    val surfaceColor: Long,
    val jogLedColors: List<Long>
) {
    companion object {
        val ClassicDark = DjSkin(
            id = "classic_dark",
            name = "Virtual DJ Classic",
            description = "Iconic pro console with electric blue & ruby red",
            deckAColor = 0xFF00E5FF,
            deckBColor = 0xFFFF1744,
            surfaceColor = 0xFF14171F,
            jogLedColors = listOf(0xFF00E5FF, 0xFF38BDF8, 0xFF0284C7)
        )

        val CyberpunkNeon = DjSkin(
            id = "cyberpunk",
            name = "Cyberpunk Neon",
            description = "Vibrant electric cyan and vivid magenta glow",
            deckAColor = 0xFF00F0FF,
            deckBColor = 0xFFFF007F,
            surfaceColor = 0xFF0F0C20,
            jogLedColors = listOf(0xFF00F0FF, 0xFFD946EF, 0xFFFF007F)
        )

        val PioneerPlatinum = DjSkin(
            id = "pioneer",
            name = "Pioneer Platinum",
            description = "Club flagship titanium silver and amber gold",
            deckAColor = 0xFF38BDF8,
            deckBColor = 0xFFF59E0B,
            surfaceColor = 0xFF1A1C23,
            jogLedColors = listOf(0xFFF59E0B, 0xFFEAB308, 0xFF38BDF8)
        )

        val StealthBlackout = DjSkin(
            id = "stealth",
            name = "Stealth Blackout",
            description = "Minimalist dark carbon with emerald & crimson accents",
            deckAColor = 0xFF10B981,
            deckBColor = 0xFFEF4444,
            surfaceColor = 0xFF0B0D11,
            jogLedColors = listOf(0xFF10B981, 0xFF059669, 0xFFEF4444)
        )

        val allSkins = listOf(ClassicDark, CyberpunkNeon, PioneerPlatinum, StealthBlackout)
    }
}
