package com.exa.android.reflekt.loopit.presentation.auth.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin

@Composable
fun AnimatedAuthBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val tertiaryColor = colorScheme.tertiary

    // Dark mode: deep dark surface; Light mode: off-white warmth
    val bgTop = if (isDark) Color(0xFF0D0D14) else Color(0xFFF8F6FF)
    val bgBottom = if (isDark) Color(0xFF111118) else Color(0xFFF0EDFA)

    // Subtle accent tints
    // Subtle accent tints - Increased for visibility
    val blobAlpha = if (isDark) 0.12f else 0.15f
    val dotAlpha = if (isDark) 0.15f else 0.2f

    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
        label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "w2"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Base gradient
            drawRect(brush = Brush.verticalGradient(listOf(bgTop, bgBottom)))

            // Subtle top blob
            drawWave(
                primaryColor.copy(alpha = blobAlpha), w, h * 0.30f,
                phase = wave1, amplitude = 35f, yOff = 0f
            )

            // Subtle bottom blob
            drawWave(
                tertiaryColor.copy(alpha = blobAlpha * 0.7f), w, h * 0.25f,
                phase = wave2, amplitude = 25f, yOff = h * 0.75f
            )

            // Small floating dots
            val dY1 = h * 0.12f + sin(Math.toRadians(wave1.toDouble())).toFloat() * 15f
            drawCircle(primaryColor.copy(alpha = dotAlpha), 45f, Offset(w * 0.82f, dY1))

            val dY2 = h * 0.88f + sin(Math.toRadians(wave2.toDouble())).toFloat() * 12f
            drawCircle(tertiaryColor.copy(alpha = dotAlpha * 0.7f), 30f, Offset(w * 0.12f, dY2))
        }

        content()
    }
}

private fun DrawScope.drawWave(
    color: Color, width: Float, height: Float,
    phase: Float, amplitude: Float, yOff: Float
) {
    val path = Path()
    val n = 100
    for (i in 0..n) {
        val x = width * i / n
        val r = Math.toRadians((i * 3.6 + phase).toDouble())
        val y = yOff + height * 0.5f + sin(r).toFloat() * amplitude +
                sin(r * 0.5).toFloat() * amplitude * 0.4f
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.lineTo(width, yOff + height)
    path.lineTo(0f, yOff + height)
    path.close()
    drawPath(path, color)
}
