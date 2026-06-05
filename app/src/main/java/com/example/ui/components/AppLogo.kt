package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = size.toPx() * 0.05f
            
            // Outer Ring
            drawCircle(
                color = color,
                style = Stroke(width = strokeWidth)
            )

            // Stylized Road / Highway (V shape or straight with dash)
            val roadWidth = size.toPx() * 0.2f
            val centerY = size.toPx() / 2f
            val centerX = size.toPx() / 2f
            
            // Left Boundary
            drawLine(
                color = color,
                start = center.copy(x = centerX - roadWidth, y = size.toPx() * 0.2f),
                end = center.copy(x = centerX - roadWidth, y = size.toPx() * 0.8f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            // Right Boundary
            drawLine(
                color = color,
                start = center.copy(x = centerX + roadWidth, y = size.toPx() * 0.2f),
                end = center.copy(x = centerX + roadWidth, y = size.toPx() * 0.8f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            // Dashed center line
            val dashHeight = size.toPx() * 0.15f
            drawLine(
                color = color.copy(alpha = 0.5f),
                start = center.copy(x = centerX, y = size.toPx() * 0.3f),
                end = center.copy(x = centerX, y = size.toPx() * 0.3f + dashHeight),
                strokeWidth = strokeWidth / 2,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color.copy(alpha = 0.5f),
                start = center.copy(x = centerX, y = size.toPx() * 0.55f),
                end = center.copy(x = centerX, y = size.toPx() * 0.55f + dashHeight),
                strokeWidth = strokeWidth / 2,
                cap = StrokeCap.Round
            )

            // Biometric Arcs (Fingerprint like)
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = center.copy(x = centerX - size.toPx() * 0.35f, y = size.toPx() * 0.15f),
                size = androidx.compose.ui.geometry.Size(size.toPx() * 0.7f, size.toPx() * 0.7f),
                style = Stroke(width = strokeWidth / 2)
            )
            
            drawArc(
                color = color.copy(alpha = 0.7f),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = center.copy(x = centerX - size.toPx() * 0.25f, y = size.toPx() * 0.25f),
                size = androidx.compose.ui.geometry.Size(size.toPx() * 0.5f, size.toPx() * 0.5f),
                style = Stroke(width = strokeWidth / 2)
            )
        }
    }
}
