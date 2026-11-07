package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.DeckState
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBRed
import kotlin.math.abs

@Composable
fun DualWaveformDisplay(
    deckA: DeckState,
    deckB: DeckState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFF090B0F))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val halfHeight = height / 2f
            val centerX = width / 2f

            // Background subtle grid
            val gridStep = 24.dp.toPx()
            var x = 0f
            while (x < width) {
                drawLine(
                    color = Color(0xFF161922),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridStep
            }

            // Draw Deck A waveform in top half (Cyan)
            drawTrackWaveformStrip(
                deck = deckA,
                baseColor = DeckACyan,
                centerY = halfHeight * 0.5f,
                maxHalfAmplitude = halfHeight * 0.42f,
                centerX = centerX,
                totalWidth = width
            )

            // Center dividing hairline
            drawLine(
                color = Color(0xFF2C3242),
                start = Offset(0f, halfHeight),
                end = Offset(width, halfHeight),
                strokeWidth = 1.5f
            )

            // Draw Deck B waveform in bottom half (Red)
            drawTrackWaveformStrip(
                deck = deckB,
                baseColor = DeckBRed,
                centerY = halfHeight + halfHeight * 0.5f,
                maxHalfAmplitude = halfHeight * 0.42f,
                centerX = centerX,
                totalWidth = width
            )

            // Center playhead needle (Virtual DJ vertical line with marker ticks)
            drawLine(
                color = Color.White,
                start = Offset(centerX, 0f),
                end = Offset(centerX, height),
                strokeWidth = 2.dp.toPx()
            )
            // Playhead triangle top
            val path = Path().apply {
                moveTo(centerX - 5.dp.toPx(), 0f)
                lineTo(centerX + 5.dp.toPx(), 0f)
                lineTo(centerX, 6.dp.toPx())
                close()
            }
            drawPath(path, color = Color.White)
        }
    }
}

private fun DrawScope.drawTrackWaveformStrip(
    deck: DeckState,
    baseColor: Color,
    centerY: Float,
    maxHalfAmplitude: Float,
    centerX: Float,
    totalWidth: Float
) {
    val points = deck.track?.waveformPoints ?: return
    if (points.isEmpty()) return

    val duration = deck.durationMs.coerceAtLeast(180_000L).toFloat()
    val currentNorm = (deck.positionMs.toFloat() / duration).coerceIn(0f, 1f)

    // Window of waveform displayed around playhead: e.g. 15 seconds
    val windowSec = 16f
    val windowNorm = (windowSec * 1000f) / duration
    val pointsPerNorm = points.size.toFloat()

    val barWidth = 2.5f
    val spacing = 3.5f
    val barsCount = (totalWidth / spacing).toInt()

    for (b in 0 until barsCount) {
        val barX = b * spacing
        val offsetFromCenter = (barX - centerX) / (totalWidth / 2f)
        val sampleNorm = currentNorm + (offsetFromCenter * (windowNorm / 2f))

        if (sampleNorm in 0f..1f) {
            val idx = (sampleNorm * (points.size - 1)).toInt().coerceIn(0, points.size - 1)
            val amp = points[idx] * maxHalfAmplitude

            val isNearPlayhead = abs(barX - centerX) < 4f
            val barColor = if (isNearPlayhead) Color.White else baseColor.copy(alpha = 0.85f)

            drawLine(
                color = barColor,
                start = Offset(barX, centerY - amp),
                end = Offset(barX, centerY + amp),
                strokeWidth = barWidth
            )
        }
    }
}

@Composable
fun SingleDeckWaveform(
    deck: DeckState,
    waveformColor: Color,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(Color(0xFF0F1218))
            .pointerInput(deck.durationMs) {
                detectTapGestures { tapOffset ->
                    val fraction = (tapOffset.x / size.width).coerceIn(0f, 1f)
                    val targetMs = (fraction * deck.durationMs).toLong()
                    onSeek(targetMs)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            // Full track overview waveform
            val points = deck.track?.waveformPoints ?: emptyList()
            if (points.isNotEmpty()) {
                val stepX = width / points.size
                for (i in points.indices) {
                    val amp = points[i] * (centerY * 0.85f)
                    val x = i * stepX
                    drawLine(
                        color = waveformColor.copy(alpha = 0.7f),
                        start = Offset(x, centerY - amp),
                        end = Offset(x, centerY + amp),
                        strokeWidth = stepX.coerceAtLeast(1f)
                    )
                }
            }

            // Loop window overlay if active
            if (deck.isLoopActive && deck.loopEndMs > deck.loopStartMs && deck.durationMs > 0) {
                val startX = (deck.loopStartMs.toFloat() / deck.durationMs) * width
                val endX = (deck.loopEndMs.toFloat() / deck.durationMs) * width
                drawRect(
                    color = Color(0x4400E5FF),
                    topLeft = Offset(startX, 0f),
                    size = Size(endX - startX, height)
                )
            }

            // Playhead indicator
            val currentX = if (deck.durationMs > 0) {
                (deck.positionMs.toFloat() / deck.durationMs) * width
            } else 0f

            drawLine(
                color = Color.White,
                start = Offset(currentX, 0f),
                end = Offset(currentX, height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
