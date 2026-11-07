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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DefaultTrackCatalog
import com.example.model.DeckId
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.ConsoleSurface
import com.example.ui.theme.ConsoleSurfaceElevated
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBRed
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun YouTubeStreamDialog(
    onDismiss: () -> Unit,
    onLoadStream: (url: String, title: String, targetDeck: DeckId) -> Unit
) {
    var urlText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }
    var targetDeck by remember { mutableStateOf(DeckId.DECK_A) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, ConsoleBorder, RoundedCornerShape(8.dp)),
            color = ConsoleSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = DeckACyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "YouTube & Cloud Audio Stream Loader",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Load any YouTube audio URL or online live stream directly into Deck A or Deck B disk:",
                    fontSize = 11.sp,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Custom URL input
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    placeholder = { Text("https://www.youtube.com/... or stream URL", fontSize = 11.sp, color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stream_url_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeckACyan,
                        unfocusedBorderColor = ConsoleBorder,
                        focusedContainerColor = ConsoleSurfaceElevated,
                        unfocusedContainerColor = ConsoleSurfaceElevated,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    placeholder = { Text("Optional Song Title", fontSize = 11.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeckACyan,
                        unfocusedBorderColor = ConsoleBorder,
                        focusedContainerColor = ConsoleSurfaceElevated,
                        unfocusedContainerColor = ConsoleSurfaceElevated,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target Deck Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Target Deck:", fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (targetDeck == DeckId.DECK_A) DeckACyan else ConsoleSurfaceElevated)
                                .clickable { targetDeck = DeckId.DECK_A }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Deck A (Blue)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (targetDeck == DeckId.DECK_A) Color.Black else TextWhite
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (targetDeck == DeckId.DECK_B) DeckBRed else ConsoleSurfaceElevated)
                                .clickable { targetDeck = DeckId.DECK_B }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Deck B (Red)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (targetDeck == DeckId.DECK_B) Color.White else TextWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // One-tap Live Stream Presets
                Text(
                    text = "Featured Live Stream Presets:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(modifier = Modifier.height(80.dp)) {
                    items(DefaultTrackCatalog.onlineStreamPresets) { (presetName, presetUrl) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(ConsoleSurfaceElevated)
                                .clickable {
                                    onLoadStream(presetUrl, presetName, targetDeck)
                                    onDismiss()
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = presetName, fontSize = 10.sp, color = TextWhite, fontWeight = FontWeight.SemiBold)
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Load",
                                tint = if (targetDeck == DeckId.DECK_A) DeckACyan else DeckBRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Load Button
                Button(
                    onClick = {
                        if (urlText.isNotBlank()) {
                            onLoadStream(urlText, titleText.ifBlank { "Cloud Stream" }, targetDeck)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("load_stream_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = if (targetDeck == DeckId.DECK_A) DeckACyan else DeckBRed),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "LOAD TO ${targetDeck.name.replace("_", " ")}",
                        fontWeight = FontWeight.Black,
                        color = if (targetDeck == DeckId.DECK_A) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
