package com.ilustris.sagai.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun WaveAnimation(modifier: Modifier = Modifier, waveColor: Color, duration: Duration = 1.seconds) {
    val infiniteTransition = rememberInfiniteTransition()
    val waveOffset = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration.toInt(DurationUnit.MILLISECONDS), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier) {
        drawWave(waveOffset.value, waveColor)
    }
}

private fun DrawScope.drawWave(offset: Float, color: Color = Color.Red) {
    val path = Path()
    val waveHeight = 50.dp.toPx()
    val waveLength = size.width / 2

    path.moveTo(0f, size.height / 2)
    for (x in 0..size.width.toInt() step 10) {
        val y = (waveHeight * kotlin.math.sin((x + offset) * Math.PI / waveLength)).toFloat()
        path.lineTo(x.toFloat(), size.height / 2 + y)
    }
    path.lineTo(size.width, size.height)
    path.lineTo(0f, size.height)
    path.close()

    drawPath(path, color = color)
}

@Preview
@Composable
fun WavePreview() {
    WaveAnimation(modifier = Modifier.size(300.dp), MaterialTheme.colorScheme.primary)
}