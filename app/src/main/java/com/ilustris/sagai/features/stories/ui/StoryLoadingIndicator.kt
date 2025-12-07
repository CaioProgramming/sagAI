package com.ilustris.sagai.features.stories.ui

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StoryLoadingIndicator(
    modifier: Modifier = Modifier,
    brush: Brush,
    strokeWidth: Dp = 2.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "story_loading_transition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseIn),
            repeatMode = RepeatMode.Restart,
        ),
        label = "story_loading_rotation",
    )


    Canvas(modifier = modifier
        .fillMaxSize()
        .padding(4.dp)) {
        val arcStroke = strokeWidth.toPx()
        // Draw blurred glow arc first
        drawArc(
            brush = brush,
            startAngle = rotation,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = arcStroke * 2.5f), // Thicker for glow
            alpha = 0.45f
        )
        // Draw solid arc on top
        drawArc(
            brush = brush,
            startAngle = rotation,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = arcStroke)
        )
    }
}