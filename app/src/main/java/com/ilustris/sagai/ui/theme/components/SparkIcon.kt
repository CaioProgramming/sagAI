package com.ilustris.sagai.ui.theme.components

import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.RepeatMode.Reverse
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import com.ilustris.sagai.ui.theme.RoundedPolygonShape
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import kotlin.collections.plusAssign
import kotlin.compareTo
import kotlin.time.Duration.Companion.seconds

@Composable
fun SparkIcon(
    modifier: Modifier,
    description: String,
    brush: Brush,
    blurRadius: Dp = 10.dp,
) {
    val infiniteTranition = rememberInfiniteTransition()
    val starInnerRadius =
        infiniteTranition.animateFloat(
            initialValue = 1f / 3f,
            targetValue = 1f / 2f,
            animationSpec =
                infiniteRepeatable(
                    tween(5000, easing = EaseInCubic),
                    Reverse,
                ),
        )

    SagaLoader(
        modifier = modifier,
        brush = brush,
        animationDuration = 10.seconds,
        scaleDistortion = .8f to .8f,
        blurRadius = 10.dp,
        tint = MaterialTheme.colorScheme.background,
        shape =
            RoundedPolygonShape(
                RoundedPolygon.star(
                    numVerticesPerRadius = 4,
                    innerRadius = starInnerRadius.value,
                    radius = 1f,
                    rounding = CornerRounding(.0f),
                ),
            ),
    )
}

@Preview
@Composable
fun SparkIconPreview() {
    SparkIcon(
        modifier = Modifier.size(200.dp),
        description = "Spark Icon",
        brush =
            gradientAnimation(
                genresGradient(),
            ),
    )
}
