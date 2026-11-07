package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeckId
import com.example.model.DeckState
import com.example.model.MixerState
import com.example.model.MixerTab
import com.example.ui.theme.ButtonNeutral
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.ConsoleSurface
import com.example.ui.theme.ConsoleSurfaceElevated
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBRed
import com.example.ui.theme.FaderCap
import com.example.ui.theme.FaderTrack
import com.example.ui.theme.LedGreen
import com.example.ui.theme.LedRed
import com.example.ui.theme.LedYellow
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun MixerSection(
    deckA: DeckState,
    deckB: DeckState,
    mixer: MixerState,
    onCrossfaderChange: (Float) -> Unit,
    onDeckVolumeChange: (DeckId, Float) -> Unit,
    onDeckGainChange: (DeckId, Float) -> Unit,
    onDeckEqChange: (DeckId, Float?, Float?, Float?) -> Unit,
    onFilterChange: (DeckId, Float) -> Unit,
    onHeadphoneCueToggle: (DeckId) -> Unit,
    onTabSelect: (MixerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(ConsoleSurface)
            .border(1.dp, ConsoleBorder)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mixer Top Tabs (MIXER, VIDEO, SCRATCH, MASTER)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MixerTab.entries.forEach { tab ->
                val isSelected = mixer.activeTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) ConsoleSurfaceElevated else Color.Transparent)
                        .border(1.dp, if (isSelected) DeckACyan else Color.Transparent, RoundedCornerShape(3.dp))
                        .clickable { onTabSelect(tab) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("mixer_tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.name,
                        fontSize = 8.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextWhite else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Center Controls: Deck A Knobs, VU Meters & Faders, Deck B Knobs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Deck A EQ & Gain column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.weight(1f)
            ) {
                KnobControl(
                    value = deckA.gainDb / 12f,
                    onValueChange = { onDeckGainChange(DeckId.DECK_A, it * 12f) },
                    label = "GAIN",
                    accentColor = DeckACyan,
                    size = 32.dp
                )
                KnobControl(
                    value = deckA.eqHighDb / 12f,
                    onValueChange = { onDeckEqChange(DeckId.DECK_A, it * 12f, null, null) },
                    label = "HIGH",
                    accentColor = DeckACyan,
                    size = 32.dp
                )
                KnobControl(
                    value = deckA.eqMidDb / 12f,
                    onValueChange = { onDeckEqChange(DeckId.DECK_A, null, it * 12f, null) },
                    label = "MID",
                    accentColor = DeckACyan,
                    size = 32.dp
                )
                KnobControl(
                    value = deckA.eqLowDb / 12f,
                    onValueChange = { onDeckEqChange(DeckId.DECK_A, null, null, it * 12f) },
                    label = "LOW",
                    accentColor = DeckACyan,
                    size = 32.dp
                )
                KnobControl(
                    value = deckA.filterCutoff,
                    onValueChange = { onFilterChange(DeckId.DECK_A, it) },
                    label = "FILTER",
                    accentColor = DeckACyan,
                    size = 32.dp
                )
                // Headphone Cue A button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (mixer.headphoneCueA) DeckACyan else ButtonNeutral)
                        .clickable { onHeadphoneCueToggle(DeckId.DECK_A) }
                        .testTag("cue_headphones_a"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Cue A",
                        tint = if (mixer.headphoneCueA) Color.Black else TextGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Center Channel Faders and Stereo LED VU Meter
            Row(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Deck A Volume Fader
                VerticalFader(
                    value = deckA.volume,
                    onValueChange = { onDeckVolumeChange(DeckId.DECK_A, it) },
                    activeColor = DeckACyan,
                    tag = "fader_deck_a",
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Dual VU Meters
                Row(
                    modifier = Modifier
                        .height(140.dp)
                        .background(Color(0xFF0A0C10), RoundedCornerShape(2.dp))
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    VuMeterStrip(level = mixer.vuLevelLeft, color = DeckACyan)
                    VuMeterStrip(level = mixer.vuLevelRight, color = DeckBRed)
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Deck B Volume Fader
                VerticalFader(
                    value = deckB.volume,
                    onValueChange = { onDeckVolumeChange(DeckId.DECK_B, it) },
                    activeColor = DeckBRed,
                    tag = "fader_deck_b",
                    modifier = Modifier.weight(1f)
                )
            }

            // Deck B EQ & Gain column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.weight(1f)
            ) {
                KnobControl(
                    value = deckB.gainDb / 12f,
                    onValueChange = { onDeckGainChange(DeckId.DECK_B, it * 12f) },
                    label = "GAIN",
                    accentColor = DeckBRed,
                    size = 32.dp
                )
                KnobControl(
                    value = deckB.eqHighDb / 12f,
                    onValueChange = { onDeckEqChange(DeckId.DECK_B, it * 12f, null, null) },
                    label = "HIGH",
                    accentColor = DeckBRed,
                    size = 32.dp
                )
                KnobControl(
                    value = deckB.eqMidDb / 12f,
                    onValueChange = { onDeckEqChange(DeckId.DECK_B, null, it * 12f, null) },
                    label = "MID",
                    accentColor = DeckBRed,
                    size = 32.dp
                )
                KnobControl(
                    value = deckB.eqLowDb / 12f,
                    onValueChange = { onDeckEqChange(DeckId.DECK_B, null, null, it * 12f) },
                    label = "LOW",
                    accentColor = DeckBRed,
                    size = 32.dp
                )
                KnobControl(
                    value = deckB.filterCutoff,
                    onValueChange = { onFilterChange(DeckId.DECK_B, it) },
                    label = "FILTER",
                    accentColor = DeckBRed,
                    size = 32.dp
                )
                // Headphone Cue B button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (mixer.headphoneCueB) DeckBRed else ButtonNeutral)
                        .clickable { onHeadphoneCueToggle(DeckId.DECK_B) }
                        .testTag("cue_headphones_b"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Cue B",
                        tint = if (mixer.headphoneCueB) Color.White else TextGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Horizontal Crossfader at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "DECK A", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DeckACyan)
                Text(text = "CROSSFADER", fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = TextGray)
                Text(text = "DECK B", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DeckBRed)
            }

            Slider(
                value = mixer.crossfader,
                onValueChange = onCrossfaderChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = FaderCap,
                    activeTrackColor = DeckBRed,
                    inactiveTrackColor = DeckACyan
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .testTag("crossfader_slider")
            )
        }
    }
}

@Composable
private fun VerticalFader(
    value: Float,
    onValueChange: (Float) -> Unit,
    activeColor: Color,
    tag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(28.dp)
            .height(130.dp)
            .testTag(tag)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val fraction = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    onValueChange(fraction)
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Track groove
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(FaderTrack, RoundedCornerShape(2.dp))
        )

        // Fader thumb
        val thumbOffset = ((1f - value) * 105).dp
        Box(
            modifier = Modifier
                .padding(bottom = (value * 105).dp)
                .size(width = 24.dp, height = 18.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF475569))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .background(activeColor)
            )
        }
    }
}

@Composable
private fun VuMeterStrip(
    level: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val totalLeds = 14
    val activeCount = (level.coerceIn(0f, 1f) * totalLeds).toInt()

    Column(
        modifier = modifier
            .width(5.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (i in totalLeds downTo 1) {
            val isLit = i <= activeCount
            val ledColor = when {
                i > 12 -> LedRed
                i > 9 -> LedYellow
                else -> LedGreen
            }

            Box(
                modifier = Modifier
                    .size(width = 5.dp, height = 6.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isLit) ledColor else Color(0xFF151820))
            )
        }
    }
}
