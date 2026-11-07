package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeckId
import com.example.model.DeckState
import com.example.model.DjEffectType
import com.example.ui.theme.ButtonActive
import com.example.ui.theme.ButtonNeutral
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.ConsoleSurface
import com.example.ui.theme.ConsoleSurfaceElevated
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBRed
import com.example.ui.theme.DisplayBlue
import com.example.ui.theme.FaderCap
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.util.Locale

@Composable
fun DeckControlPanel(
    deck: DeckState,
    deckId: DeckId,
    accentColor: Color,
    ledRingColors: List<Color>,
    onPlayPause: () -> Unit,
    onCue: () -> Unit,
    onSync: () -> Unit,
    onPitchChange: (Float) -> Unit,
    onNudgePitch: (Float) -> Unit,
    onToggleKeyLock: () -> Unit,
    onToggleVinylMode: () -> Unit,
    onToggleSlipMode: () -> Unit,
    onSetHotCue: (Int) -> Unit,
    onJumpHotCue: (Int) -> Unit,
    onSeek: (Long) -> Unit,
    onToggleLoop: () -> Unit,
    onSetLoopLength: (Float) -> Unit,
    onLoopIn: () -> Unit,
    onLoopOut: () -> Unit,
    onJogTouchStart: () -> Unit,
    onJogScratchMove: (Float, Float) -> Unit,
    onJogTouchEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFxDropdownExpanded by remember { mutableStateOf(false) }
    var selectedEffect by remember { mutableStateOf(DjEffectType.BEAT_GRID) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(ConsoleSurface)
            .border(1.dp, ConsoleBorder)
            .padding(4.dp)
    ) {
        // 1. Single Deck Waveform
        SingleDeckWaveform(
            deck = deck,
            waveformColor = accentColor,
            onSeek = onSeek,
            modifier = Modifier.clip(RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.height(3.dp))

        // 2. Middle Row: Side Controls (Hot Cues, FX, Loops) + Center Jog Wheel + Pitch Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side Rack: Hot Cues & Loops
            Column(
                modifier = Modifier
                    .width(86.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Hot Cues (1, 2, 3)
                Column {
                    Text(
                        text = "HOT CUE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (cueNum in 1..3) {
                            val isAssigned = deck.hotCues.containsKey(cueNum)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isAssigned) accentColor.copy(alpha = 0.85f) else ButtonNeutral)
                                    .pointerInput(cueNum) {
                                        detectTapGestures(
                                            onTap = { onJumpHotCue(cueNum) },
                                            onLongPress = { onSetHotCue(cueNum) }
                                        )
                                    }
                                    .testTag("cue_${deckId.name.lowercase()}_$cueNum"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$cueNum",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isAssigned) Color.Black else TextWhite
                                )
                            }
                        }
                    }
                }

                // FX Dropdown Button
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(2.dp))
                            .background(ConsoleSurfaceElevated)
                            .clickable { isFxDropdownExpanded = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedEffect.displayName,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            maxLines = 1
                        )
                        Text(text = "▼", fontSize = 7.sp, color = TextGray)
                    }

                    DropdownMenu(
                        expanded = isFxDropdownExpanded,
                        onDismissRequest = { isFxDropdownExpanded = false }
                    ) {
                        DjEffectType.entries.forEach { fx ->
                            DropdownMenuItem(
                                text = { Text(fx.displayName, fontSize = 11.sp) },
                                onClick = {
                                    selectedEffect = fx
                                    isFxDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Loop Section
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LOOP",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (deck.isLoopActive) accentColor else ButtonNeutral)
                                .clickable { onToggleLoop() }
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                .testTag("loop_active_btn_${deckId.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (deck.isLoopActive) "ACTIVE" else "AUTO",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (deck.isLoopActive) Color.Black else TextWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Loop Size selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf(1f, 2f, 4f, 8f).forEach { beats ->
                            val isCurrent = deck.selectedLoopBeats == beats
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isCurrent) accentColor.copy(alpha = 0.35f) else ConsoleSurfaceElevated)
                                    .border(1.dp, if (isCurrent) accentColor else Color.Transparent, RoundedCornerShape(2.dp))
                                    .clickable { onSetLoopLength(beats) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${beats.toInt()}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) TextWhite else TextGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // IN / OUT Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(ButtonNeutral)
                                .clickable { onLoopIn() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("IN", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(ButtonNeutral)
                                .clickable { onLoopOut() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("OUT", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                    }
                }

                // Vinyl / Slip Mode Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (deck.isVinylMode) accentColor.copy(alpha = 0.25f) else ButtonNeutral)
                            .border(1.dp, if (deck.isVinylMode) accentColor else Color.Transparent, RoundedCornerShape(2.dp))
                            .clickable { onToggleVinylMode() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VINYL",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (deck.isVinylMode) accentColor else TextMuted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (deck.isSlipMode) DisplayBlue.copy(alpha = 0.25f) else ButtonNeutral)
                            .border(1.dp, if (deck.isSlipMode) DisplayBlue else Color.Transparent, RoundedCornerShape(2.dp))
                            .clickable { onToggleSlipMode() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SLIP",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (deck.isSlipMode) DisplayBlue else TextMuted
                        )
                    }
                }
            }

            // Center: Circular DJ Jog Wheel
            JogWheel(
                deckId = deckId,
                angleDeg = deck.jogAngleDeg,
                bpm = deck.currentBpm,
                isPlaying = deck.isPlaying,
                accentColor = accentColor,
                ledRingColors = ledRingColors,
                onTouchStart = onJogTouchStart,
                onScratchMove = onJogScratchMove,
                onTouchEnd = onJogTouchEnd,
                size = 142.dp
            )

            // Right Side: Pitch Slider, Key Lock, Nudge
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Pitch Percentage Text
                val pitchStr = String.format(Locale.US, "%+.1f%%", deck.pitchPercent)
                Text(
                    text = pitchStr,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )

                // Key Lock Button
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (deck.isKeyLocked) accentColor else ButtonNeutral)
                        .clickable { onToggleKeyLock() }
                        .testTag("key_lock_${deckId.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Key Lock",
                        tint = if (deck.isKeyLocked) Color.Black else TextGray,
                        modifier = Modifier.size(11.dp)
                    )
                }

                // Vertical Pitch Slider
                Slider(
                    value = deck.pitchPercent,
                    onValueChange = onPitchChange,
                    valueRange = -16f..16f,
                    colors = SliderDefaults.colors(
                        thumbColor = FaderCap,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = Color(0xFF1E232E)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp)
                        .testTag("pitch_slider_${deckId.name.lowercase()}")
                )

                // Pitch Nudge Buttons (- and +)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ButtonNeutral)
                            .clickable { onNudgePitch(-0.2f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ButtonNeutral)
                            .clickable { onNudgePitch(0.2f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // 3. Bottom Transport Row: CUE, PAUSE, PLAY, SYNC
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CUE Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ConsoleSurfaceElevated)
                    .border(1.dp, ConsoleBorder, RoundedCornerShape(3.dp))
                    .clickable { onCue() }
                    .testTag("cue_btn_${deckId.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CUE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
            }

            // PAUSE Button
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (!deck.isPlaying) ButtonActive else ConsoleSurfaceElevated)
                    .border(1.dp, ConsoleBorder, RoundedCornerShape(3.dp))
                    .clickable { if (deck.isPlaying) onPlayPause() }
                    .testTag("pause_btn_${deckId.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = TextWhite,
                    modifier = Modifier.size(16.dp)
                )
            }

            // PLAY Button (Illuminated when playing)
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (deck.isPlaying) accentColor else ButtonNeutral)
                    .clickable { onPlayPause() }
                    .testTag("play_btn_${deckId.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = if (deck.isPlaying) Color.Black else TextWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "PLAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (deck.isPlaying) Color.Black else TextWhite
                    )
                }
            }

            // SYNC Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (deck.isSyncActive) accentColor.copy(alpha = 0.35f) else ConsoleSurfaceElevated)
                    .border(1.dp, if (deck.isSyncActive) accentColor else ConsoleBorder, RoundedCornerShape(3.dp))
                    .clickable { onSync() }
                    .testTag("sync_btn_${deckId.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SYNC",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (deck.isSyncActive) accentColor else TextWhite
                )
            }
        }
    }
}
