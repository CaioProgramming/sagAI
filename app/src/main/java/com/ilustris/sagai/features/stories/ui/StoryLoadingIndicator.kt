package com.ilustris.sagai.features.stories.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StoryLoadingIndicator(
    modifier: Modifier = Modifier,
    brush: Brush,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "story_loading_transition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "story_loading_rotation",
    )

    val sparkPainter = painterResource(id = R.drawable.ic_spark)

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 3.dp.toPx()
        drawArc(
            brush = brush,
            startAngle = rotation,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        val radius = (size.minDimension - strokeWidth) / 2
        val angle = Math.toRadians((rotation + 180).toDouble())
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()

        with(sparkPainter) {
            val sparkSize = 12.dp.toPx()
            val newSize = sparkPainter.intrinsicSize * (sparkSize / sparkPainter.intrinsicSize.width)
            translate(left = x - newSize.width / 2, top = y - newSize.height / 2) {
                draw(size = newSize)
            }
        }
    }
}