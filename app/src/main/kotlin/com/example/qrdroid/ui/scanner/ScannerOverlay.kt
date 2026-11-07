package com.example.qrdroid.ui.scanner

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier,
    laserColor: Color = Color(0xFF87CFF9),
    cornerColor: Color = Color.White,
    scrimColor: Color = Color(0x99000000)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_progress"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val boxWidth = (canvasWidth * 0.76f).coerceIn(240.dp.toPx(), 320.dp.toPx())
            val boxHeight = boxWidth
            val left = (canvasWidth - boxWidth) / 2f
            val top = (canvasHeight - boxHeight) / 2.5f
            val right = left + boxWidth
            val bottom = top + boxHeight
            val cornerRadius = 24.dp.toPx()
            val cornerLength = 38.dp.toPx()
            val strokeWidth = 5.dp.toPx()

            // 1. Draw scrim with hollow cutout using PathOperation.Difference
            val fullScreenPath = Path().apply {
                addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
            }
            val cutPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(left, top, right, bottom),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                )
            }
            val hollowScrimPath = Path.combine(
                operation = PathOperation.Difference,
                path1 = fullScreenPath,
                path2 = cutPath
            )
            drawPath(path = hollowScrimPath, color = scrimColor)

            val cornerStroke = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )

            // 2. Draw continuous rounded corners (Top-Left)
            val tlPath = Path().apply {
                moveTo(left, top + cornerLength)
                lineTo(left, top + cornerRadius)
                quadraticTo(left, top, left + cornerRadius, top)
                lineTo(left + cornerLength, top)
            }
            drawPath(path = tlPath, color = cornerColor, style = cornerStroke)

            // Top-Right
            val trPath = Path().apply {
                moveTo(right - cornerLength, top)
                lineTo(right - cornerRadius, top)
                quadraticTo(right, top, right, top + cornerRadius)
                lineTo(right, top + cornerLength)
            }
            drawPath(path = trPath, color = cornerColor, style = cornerStroke)

            // Bottom-Left
            val blPath = Path().apply {
                moveTo(left, bottom - cornerLength)
                lineTo(left, bottom - cornerRadius)
                quadraticTo(left, bottom, left + cornerRadius, bottom)
                lineTo(left + cornerLength, bottom)
            }
            drawPath(path = blPath, color = cornerColor, style = cornerStroke)

            // Bottom-Right
            val brPath = Path().apply {
                moveTo(right - cornerLength, bottom)
                lineTo(right - cornerRadius, bottom)
                quadraticTo(right, bottom, right, bottom - cornerRadius)
                lineTo(right, bottom - cornerLength)
            }
            drawPath(path = brPath, color = cornerColor, style = cornerStroke)

            // 3. Draw animated laser beam
            val laserY = top + (boxHeight * laserProgress)
            val laserMargin = 12.dp.toPx()
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        laserColor.copy(alpha = 0.7f),
                        laserColor,
                        laserColor.copy(alpha = 0.7f),
                        Color.Transparent
                    ),
                    startX = left + laserMargin,
                    endX = right - laserMargin
                ),
                topLeft = Offset(left + laserMargin, laserY - 1.5.dp.toPx()),
                size = Size(boxWidth - (laserMargin * 2), 3.dp.toPx())
            )
        }

        // Instructional text below reticle
        Text(
            text = "Aponte a câmera para um\nQR Code ou Código de Barras",
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 295.dp)
        )
    }
}
