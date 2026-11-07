package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeckState
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.ConsoleSurface
import com.example.ui.theme.ConsoleSurfaceElevated
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBRed
import com.example.ui.theme.DisplayBlue
import com.example.ui.theme.LedRed
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.util.Locale

@Composable
fun VirtualDjHeader(
    deckA: DeckState,
    deckB: DeckState,
    isRecording: Boolean,
    recordDurationSec: Int,
    onOpenRecording: () -> Unit,
    onOpenYouTubeDialog: () -> Unit,
    onOpenSkinDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ConsoleSurface)
            .border(1.dp, ConsoleBorder)
    ) {
        // Dual Waveforms in center top
        DualWaveformDisplay(deckA = deckA, deckB = deckB)

        // Metadata Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Deck A Info (Left)
            DeckHeaderBadge(
                deck = deckA,
                badgeText = "A",
                badgeColor = DeckACyan,
                modifier = Modifier.weight(1f)
            )

            // Center Quick Action Toolbar
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .background(ConsoleSurfaceElevated, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Recording Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isRecording) LedRed.copy(alpha = 0.25f) else Color.Transparent)
                        .clickable { onOpenRecording() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("rec_header_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Record Mix",
                        tint = if (isRecording) LedRed else TextGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isRecording) {
                            val min = recordDurationSec / 60
                            val sec = recordDurationSec % 60
                            String.format(Locale.US, "REC %02d:%02d", min, sec)
                        } else "REC",
                        color = if (isRecording) LedRed else TextWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // YouTube / Cloud Stream Import Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .clickable { onOpenYouTubeDialog() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("youtube_stream_header_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Cloud / YouTube Stream",
                        tint = DisplayBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "STREAM",
                        color = TextWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // DJ Skin Customizer Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .clickable { onOpenSkinDialog() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("skin_header_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "DJ Skins",
                        tint = DeckACyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "SKIN",
                        color = TextWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Deck B Info (Right)
            DeckHeaderBadge(
                deck = deckB,
                badgeText = "B",
                badgeColor = DeckBRed,
                isRightAligned = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DeckHeaderBadge(
    deck: DeckState,
    badgeText: String,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    isRightAligned: Boolean = false
) {
    val totalSec = deck.remainingMs / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    val tenths = (deck.remainingMs % 1000) / 100
    val remainStr = String.format(Locale.US, "%d:%02d.%d", min, sec, tenths)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isRightAligned) Arrangement.End else Arrangement.Start
    ) {
        if (!isRightAligned) {
            // Deck Badge (Left)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(badgeColor, RoundedCornerShape(3.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(horizontalAlignment = if (isRightAligned) Alignment.End else Alignment.Start) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "REMAIN ",
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = remainStr,
                    color = badgeColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = deck.track?.title ?: "No Track Loaded",
                color = TextWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = deck.track?.artist ?: "Virtual DJ",
                    color = TextGray,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = String.format(Locale.US, "%.2f BPM", deck.currentBpm),
                    color = badgeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = deck.track?.musicalKey ?: "08A",
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (isRightAligned) {
            Spacer(modifier = Modifier.width(6.dp))
            // Deck Badge (Right)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(badgeColor, RoundedCornerShape(3.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}
