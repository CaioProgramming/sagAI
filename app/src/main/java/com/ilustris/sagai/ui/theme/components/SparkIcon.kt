package com.ilustris.sagai.ui.theme.components

import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.RepeatMode.Reverse
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.RoundedPolygonShape
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun SparkIcon(
    modifier: Modifier,
    description: String = emptyString(),
    brush: Brush,
    tint: Color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .4f),
    blurRadius: Dp = 10.dp,
    targetRadius: Float = 1f / 3f,
    duration: Duration = 5.seconds,
    rotationTarget: Float = 30f,
) {
    val starInnerRadius =
        if (rememberLifecycleAnimationsActive()) {
            rememberSparkIconInnerRadius(targetRadius, duration)
        } else {
            targetRadius
        }

    SagaLoader(
        modifier = modifier,
        brush = brush,
        animationDuration = duration,
        scaleDistortion = .8f to .8f,
        blurRadius = blurRadius,
        tint = tint,
        rotationTarget = rotationTarget,
        shape =
            RoundedPolygonShape(
                RoundedPolygon.star(
                    numVerticesPerRadius = 4,
                    innerRadius = starInnerRadius,
                    radius = 1f,
                    rounding = CornerRounding(0f),
                ),
            ),
    )
}

@Composable
private fun rememberSparkIconInnerRadius(
    targetRadius: Float,
    duration: Duration,
): Float {
    val infiniteTransition = rememberInfiniteTransition()
    val starInnerRadius by infiniteTransition.animateFloat(
        initialValue = 1f / 2f,
        targetValue = targetRadius,
        animationSpec =
            infiniteRepeatable(
                tween(duration.toInt(DurationUnit.MILLISECONDS), easing = EaseInElastic),
                Reverse,
            ),
    )
    return starInnerRadius
}

@Preview
@Composable
fun SparkIconPreview() {
    SagAIScaffold {
        SparkIcon(
            modifier = Modifier.size(200.dp),
            description = "Spark Icon",
            blurRadius = 10.dp,
            targetRadius = 1f / 5f,
            brush = gradientAnimation(holographicGradient, targetValue = 500f),
        )
    }
}
