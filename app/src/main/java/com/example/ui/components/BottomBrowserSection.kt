package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeckId
import com.example.model.DjTrack
import com.example.model.SamplerPad
import com.example.ui.theme.ButtonNeutral
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.ConsoleSurface
import com.example.ui.theme.ConsoleSurfaceElevated
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBRed
import com.example.ui.theme.DisplayBlue
import com.example.ui.theme.DisplayGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.util.Locale

@Composable
fun BottomBrowserSection(
    tracks: List<DjTrack>,
    selectedCategory: String,
    searchQuery: String,
    selectedTrackForInspect: DjTrack?,
    samplerPads: List<SamplerPad>,
    onSelectCategory: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onLoadTrackToDeck: (DeckId, DjTrack) -> Unit,
    onInspectTrack: (DjTrack) -> Unit,
    onTriggerSampler: (SamplerPad) -> Unit,
    onOpenLocalFilePicker: () -> Unit,
    onOpenYouTubeDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(ConsoleSurface)
            .border(1.dp, ConsoleBorder)
    ) {
        // 1. Left Folder Tree
        Column(
            modifier = Modifier
                .width(135.dp)
                .fillMaxHeight()
                .background(Color(0xFF0F1117))
                .border(1.dp, ConsoleBorder)
                .padding(4.dp)
        ) {
            Text(
                text = "FOLDERS",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            val categories = listOf(
                "All Tracks" to Icons.Default.LibraryMusic,
                "Club / EDM" to Icons.Default.MusicNote,
                "Remixes" to Icons.Default.QueueMusic,
                "Cloud Streams" to Icons.Default.Language,
                "Recordings" to Icons.Default.Mic
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(categories) { (cat, icon) ->
                    val isSelected = selectedCategory == cat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) ConsoleSurfaceElevated else Color.Transparent)
                            .clickable { onSelectCategory(cat) }
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = cat,
                            tint = if (isSelected) DeckACyan else TextGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = cat,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TextWhite else TextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Quick Import Buttons at bottom of left pane
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(ConsoleSurfaceElevated)
                    .clickable { onOpenLocalFilePicker() }
                    .padding(vertical = 3.dp, horizontal = 4.dp)
                    .testTag("import_local_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Import",
                    tint = DeckACyan,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text("+ Local File", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }

        // 2. Center: 3D Cover Flow + Track Table
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(4.dp)
        ) {
            // Search and Status Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search songs, artists, remix...", fontSize = 9.sp, color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGray, modifier = Modifier.size(12.dp))
                    },
                    modifier = Modifier
                        .height(28.dp)
                        .weight(1f)
                        .testTag("library_search_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = TextWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeckACyan,
                        unfocusedBorderColor = ConsoleBorder,
                        focusedContainerColor = ConsoleSurfaceElevated,
                        unfocusedContainerColor = ConsoleSurfaceElevated
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${tracks.size} files",
                    fontSize = 9.sp,
                    color = TextGray,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Cover Flow Preview Carousel
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tracks) { track ->
                    val isSelected = selectedTrackForInspect?.id == track.id
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(track.coverColorHex))
                            .border(1.5.dp, if (isSelected) DeckACyan else Color.Transparent, RoundedCornerShape(3.dp))
                            .clickable { onInspectTrack(track) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = track.title.take(3).uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1218))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Title", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(2f))
                Text("Artist", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.5f))
                Text("BPM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(0.7f))
                Text("Key", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(0.6f))
                Text("Load", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.2f))
            }

            // Track List Table
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(tracks) { track ->
                    val isInspect = selectedTrackForInspect?.id == track.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isInspect) ConsoleSurfaceElevated else Color.Transparent)
                            .clickable { onInspectTrack(track) }
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = track.title,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = track.artist,
                            fontSize = 8.sp,
                            color = TextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1.5f)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", track.bpm),
                            fontSize = 8.sp,
                            color = DisplayBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.7f)
                        )
                        Text(
                            text = track.musicalKey,
                            fontSize = 8.sp,
                            color = TextMuted,
                            modifier = Modifier.weight(0.6f)
                        )

                        // Action Load Buttons [A] and [B]
                        Row(
                            modifier = Modifier.weight(1.2f),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(DeckACyan)
                                    .clickable { onLoadTrackToDeck(DeckId.DECK_A, track) }
                                    .testTag("load_deck_a_${track.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("A", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(DeckBRed)
                                    .clickable { onLoadTrackToDeck(DeckId.DECK_B, track) }
                                    .testTag("load_deck_b_${track.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("B", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // 3. Right: Sampler Pad Grid (Top) + Track Info Inspector (Bottom)
        Column(
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
                .background(Color(0xFF0F1117))
                .border(1.dp, ConsoleBorder)
                .padding(4.dp)
        ) {
            // Sampler Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAMPLER PADS",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Text(text = "8 FX", fontSize = 7.sp, color = DisplayGold)
            }

            // 4x2 Grid of Sampler Drops
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (row in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (col in 0..3) {
                            val padIdx = row * 4 + col
                            if (padIdx < samplerPads.size) {
                                val pad = samplerPads[padIdx]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(24.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (pad.isPlaying) DisplayGold else ButtonNeutral)
                                        .clickable { onTriggerSampler(pad) }
                                        .testTag("sampler_pad_${pad.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pad.name,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pad.isPlaying) Color.Black else TextWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Track Inspector Preview Box
            selectedTrackForInspect?.let { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(ConsoleSurfaceElevated, RoundedCornerShape(3.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(track.coverColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DJ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            fontSize = 8.sp,
                            color = TextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = DisplayGold,
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${track.bpm.toInt()} BPM • ${track.musicalKey}",
                                fontSize = 7.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
