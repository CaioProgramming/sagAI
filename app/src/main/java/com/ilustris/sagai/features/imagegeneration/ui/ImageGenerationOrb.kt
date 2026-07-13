package com.ilustris.sagai.features.imagegeneration.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.themePainter

@Composable
fun ImageGenerationOrb(
    modifier: Modifier = Modifier,
    isPulsing: Boolean = true,
) {
    val colors = morphingGradient()
    val orbBrush =
        Brush.radialGradient(
            colors = listOf(colors.first(), colors.last().copy(alpha = 0.6f)),
            center = Offset(0.35f, 0.35f),
            radius = 1.2f,
        )

    val breathingScale =
        if (isPulsing && rememberLifecycleAnimationsActive()) {
            val infiniteTransition = rememberInfiniteTransition(label = "orbBreathing")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.92f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "orbScale",
            )
            scale
        } else {
            1f
        }

    Box(
        modifier =
            modifier
                .scale(breathingScale)
                .size(32.dp)
                .clip(CircleShape)
                .background(orbBrush)
                .then(if (isPulsing) Modifier.reactiveShimmer(true) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = themePainter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp),
        )
    }
}
