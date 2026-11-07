package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KnobIndicator
import com.example.ui.theme.KnobMetal
import com.example.ui.theme.TextGray
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Professional DJ console rotary knob.
 * Value range: -1f .. +1f (or custom range).
 * 0f represents 12 o'clock center detent.
 */
@Composable
fun KnobControl(
    value: Float, // -1f .. +1f (e.g. EQ or Filter)
    onValueChange: (Float) -> Unit,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    rangeMin: Float = -1f,
    rangeMax: Float = 1f
) {
    var dragAccumulator by remember { mutableFloatStateOf(value) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .testTag("knob_$label")
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { dragAccumulator = value },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // Drag up increases, drag down decreases
                            val delta = -dragAmount.y * 0.015f
                            val normalized = (dragAccumulator + delta).coerceIn(rangeMin, rangeMax)
                            dragAccumulator = normalized
                            onValueChange(normalized)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val canvasSize = this.size.minDimension
                val center = Offset(canvasSize / 2f, canvasSize / 2f)
                val radius = canvasSize / 2f - 4.dp.toPx()
                val strokeWidth = 3.dp.toPx()

                // Background track arc from 135 deg to 405 deg (270 deg total)
                val startAngle = 135f
                val sweepTotal = 270f

                drawArc(
                    color = Color(0xFF1E232E),
                    startAngle = startAngle,
                    sweepAngle = sweepTotal,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(center.x - radius, center.y - radius)
                )

                // Normalized fraction 0..1
                val fraction = ((value - rangeMin) / (rangeMax - rangeMin)).coerceIn(0f, 1f)
                val activeSweep = sweepTotal * fraction

                // Active colored LED track
                drawArc(
                    color = accentColor,
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(center.x - radius, center.y - radius)
                )

                // Knob metal cap with metallic gradient
                val knobRadius = radius - 4.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF3E4656), KnobMetal, Color(0xFF181B22)),
                        center = center,
                        radius = knobRadius
                    ),
                    radius = knobRadius,
                    center = center
                )
                // Knob rim ring
                drawCircle(
                    color = Color(0xFF4A5568),
                    radius = knobRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Current angle needle line
                val currentAngle = startAngle + activeSweep
                val rad = (currentAngle * PI / 180f).toFloat()
                val needleStart = Offset(
                    center.x + (knobRadius * 0.35f) * cos(rad),
                    center.y + (knobRadius * 0.35f) * sin(rad)
                )
                val needleEnd = Offset(
                    center.x + (knobRadius * 0.88f) * cos(rad),
                    center.y + (knobRadius * 0.88f) * sin(rad)
                )

                drawLine(
                    color = KnobIndicator,
                    start = needleStart,
                    end = needleEnd,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray,
            letterSpacing = 0.5.sp
        )
    }
}
