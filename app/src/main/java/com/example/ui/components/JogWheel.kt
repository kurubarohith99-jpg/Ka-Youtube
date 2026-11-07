package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.DeckId
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JogWheel(
    deckId: DeckId,
    angleDeg: Float,
    bpm: Float,
    isPlaying: Boolean,
    accentColor: Color,
    ledRingColors: List<Color>,
    onTouchStart: () -> Unit,
    onScratchMove: (angleDeltaDeg: Float, velocity: Float) -> Unit,
    onTouchEnd: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    var lastTouchAngle by remember { mutableFloatStateOf(0f) }
    var lastTouchTime by remember { mutableLongStateOf(0L) }

    Box(
        modifier = modifier
            .size(size)
            .aspectRatio(1f)
            .testTag("jog_wheel_${deckId.name.lowercase()}")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val centerX = this.size.width / 2f
                        val centerY = this.size.height / 2f
                        lastTouchAngle = Math.toDegrees(
                            atan2((offset.y - centerY).toDouble(), (offset.x - centerX).toDouble())
                        ).toFloat()
                        lastTouchTime = System.currentTimeMillis()
                        onTouchStart()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val centerX = this.size.width / 2f
                        val centerY = this.size.height / 2f
                        val currentAngle = Math.toDegrees(
                            atan2((change.position.y - centerY).toDouble(), (change.position.x - centerX).toDouble())
                        ).toFloat()

                        var delta = currentAngle - lastTouchAngle
                        // Wrap delta over -180..180 boundary
                        if (delta > 180f) delta -= 360f
                        if (delta < -180f) delta += 360f

                        val currentTime = System.currentTimeMillis()
                        val dt = (currentTime - lastTouchTime).coerceAtLeast(1L).toFloat()
                        val velocity = (delta / dt) * 10f

                        lastTouchAngle = currentAngle
                        lastTouchTime = currentTime

                        onScratchMove(delta, velocity)
                    },
                    onDragEnd = {
                        onTouchEnd()
                    },
                    onDragCancel = {
                        onTouchEnd()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val width = this.size.width
            val height = this.size.height
            val center = Offset(width / 2f, height / 2f)
            val outerRadius = width / 2f - 2.dp.toPx()

            // 1. Outer Dark Chassis Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF222733), Color(0xFF14171F), Color(0xFF0D0F14)),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )

            // 2. Multi-color LED Chasing Edge Lights
            // 36 LED notches around the perimeter
            val ledCount = 36
            val ledRadius = outerRadius - 4.dp.toPx()
            for (i in 0 until ledCount) {
                val ledAngle = (i * (360f / ledCount) + (angleDeg * 0.5f)) % 360f
                val ledRad = (ledAngle * PI / 180f).toFloat()
                val ledPos = Offset(
                    center.x + ledRadius * cos(ledRad),
                    center.y + ledRadius * sin(ledRad)
                )

                // Color cycling or neon accent
                val colorIdx = (i + (angleDeg / 10f).toInt()) % ledRingColors.size
                val baseColor = ledRingColors[kotlin.math.abs(colorIdx) % ledRingColors.size]
                val isActiveNearNeedle = kotlin.math.abs(ledAngle - (angleDeg % 360f)) < 30f
                val ledColor = if (isActiveNearNeedle) Color.White else baseColor.copy(alpha = if (isPlaying) 0.95f else 0.45f)

                drawCircle(
                    color = ledColor,
                    radius = 2.dp.toPx(),
                    center = ledPos
                )
            }

            // 3. Black Vinyl Platter with concentric micro-grooves
            val vinylRadius = outerRadius - 9.dp.toPx()
            drawCircle(
                color = Color(0xFF101216),
                radius = vinylRadius,
                center = center
            )

            // Subtle vinyl sheen reflections
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF1E222A),
                        Color(0xFF0C0E12),
                        Color(0xFF282F3B),
                        Color(0xFF0C0E12),
                        Color(0xFF1E222A)
                    ),
                    center = center
                ),
                radius = vinylRadius,
                center = center
            )

            // Concentric vinyl grooves
            for (r in 1..4) {
                val grooveR = vinylRadius * (0.35f + r * 0.12f)
                drawCircle(
                    color = Color(0x333A4454),
                    radius = grooveR,
                    center = center,
                    style = Stroke(width = 1f)
                )
            }

            // 4. Center Label & Platter Cap
            val centerLabelRadius = vinylRadius * 0.36f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF242936), Color(0xFF12141A)),
                    center = center,
                    radius = centerLabelRadius
                ),
                radius = centerLabelRadius,
                center = center
            )

            // Center neon ring around label
            drawCircle(
                color = accentColor,
                radius = centerLabelRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Center spindle
            val spindleR = centerLabelRadius * 0.28f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFB0B7C3), Color(0xFF475569), Color(0xFF1E232E)),
                    center = center,
                    radius = spindleR
                ),
                radius = spindleR,
                center = center
            )

            // 5. Position Needle Indicator (Neon arc / line tracing current angle)
            val currentRad = (angleDeg * PI / 180f).toFloat()
            val needleStart = Offset(
                center.x + (centerLabelRadius * 0.95f) * cos(currentRad),
                center.y + (centerLabelRadius * 0.95f) * sin(currentRad)
            )
            val needleEnd = Offset(
                center.x + (vinylRadius * 0.95f) * cos(currentRad),
                center.y + (vinylRadius * 0.95f) * sin(currentRad)
            )

            drawLine(
                color = accentColor,
                start = needleStart,
                end = needleEnd,
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Glow needle tip
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = needleEnd
            )

            // Glowing position sector arc behind needle
            drawArc(
                color = accentColor.copy(alpha = 0.35f),
                startAngle = angleDeg - 40f,
                sweepAngle = 40f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                size = Size(vinylRadius * 1.9f, vinylRadius * 1.9f),
                topLeft = Offset(center.x - vinylRadius * 0.95f, center.y - vinylRadius * 0.95f)
            )
        }
    }
}
