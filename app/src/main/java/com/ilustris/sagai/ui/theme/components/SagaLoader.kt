package com.ilustris.sagai.ui.theme.components

import ai.atick.material.MaterialColor
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.DrawShape
import com.ilustris.sagai.ui.theme.RoundedPolygonShape
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun SagaLoader(
    modifier: Modifier = Modifier,
    brush: Brush = gradientAnimation(holographicGradient, targetValue = 500f),
    tint: Color = MaterialTheme.colorScheme.background.copy(alpha = .5f),
    blurRadius: Dp = 20.dp,
    animationDuration: Duration = 5.seconds,
    shape: Shape = CircleShape,
    rotationTarget: Float = 180f,
    scaleDistortion: Pair<Float, Float> = Pair(0.9f, 1.2f),
) {
    val infiniteTransition = rememberInfiniteTransition()

    infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec =
            infiniteRepeatable(
                tween(
                    durationMillis = animationDuration.toInt(DurationUnit.MILLISECONDS),
                    easing = EaseIn,
                ),
                repeatMode = Reverse,
            ),
    )

    val starPolygon =
        remember {
            RoundedPolygon.star(
                numVerticesPerRadius = 4,
                innerRadius = 1f / 5f,
                radius = 1.5f,
                rounding = CornerRounding(.0f),
            )
        }
    val circlePolygon =
        remember {
            RoundedPolygon.circle(
                numVertices = 12,
            )
        }

    DistortingBubble(
        modifier,
        brush,
        tint,
        blurRadius,
        animationDuration,
        shape,
        rotationTarget = rotationTarget,
        verticalDistortion = scaleDistortion.first,
        horizontalDistortion = scaleDistortion.second,
    )
}

fun Modifier.glow(
    color: Color,
    radius: Float = Float.POSITIVE_INFINITY,
) = this.background(
    Brush.radialGradient(
        listOf(
            color,
            Color.Transparent,
        ),
        radius = radius,
    ),
    CircleShape,
)

@Composable
fun DistortingBubble(
    modifier: Modifier = Modifier,
    brush: Brush,
    tint: Color,
    blurRadius: Dp,
    animationDduration: Duration = 5.seconds,
    shape: Shape,
    horizontalDistortion: Float = 0.9f,
    verticalDistortion: Float = 1.2f,
    rotationTarget: Float = 180f,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val duration = animationDduration.toInt(DurationUnit.MILLISECONDS)
    val horizontalDistortion =
        infiniteTransition.animateFloat(
            initialValue = .9f,
            targetValue = horizontalDistortion,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = EaseInOut),
                    repeatMode = Reverse,
                ),
        )

    val verticalDistortion =
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = verticalDistortion,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = EaseInOut),
                    repeatMode = Reverse,
                ),
        )

    val rotateAnimation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = rotationTarget,
            animationSpec =
                infiniteRepeatable(
                    tween(duration, easing = EaseInElastic),
                    repeatMode = Reverse,
                ),
        )

    Box(
        modifier
            .padding(16.dp)
            .rotate(rotateAnimation.value)
            .scale(horizontalDistortion.value, verticalDistortion.value),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .background(brush, shape),
        )
        Box(
            Modifier
                .fillMaxSize()
                .blur(2.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .background(
                    tint,
                    shape,
                ),
        )
    }
}

@Composable
fun SparkLoader(
    brush: Brush,
    strokeSize: Dp = 5.dp,
    duration: Duration = 5.seconds,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()

    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1.5f,
        animationSpec =
            infiniteRepeatable(
                tween(
                    durationMillis = duration.toInt(DurationUnit.MILLISECONDS),
                    easing = EaseIn,
                    delayMillis = duration.toInt(DurationUnit.MILLISECONDS) / 2,
                ),
                repeatMode = Reverse,
            ),
    )

    val rotateAnimation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    tween(duration.toInt(DurationUnit.MILLISECONDS), easing = EaseIn),
                    repeatMode = Reverse,
                ),
        )

    val shapeA =
        remember {
            RoundedPolygon.star(
                4,
                rounding = CornerRounding(5f),
            )
        }
    val shapeB =
        remember {
            RoundedPolygon.star(
                4,
                rounding = CornerRounding(0f),
            )
        }
    val morph =
        remember {
            Morph(shapeA, shapeB)
        }

    val morphProgress =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    tween(3.seconds.toInt(DurationUnit.MILLISECONDS), easing = EaseIn),
                    repeatMode = Reverse,
                ),
            label = "morph",
        )

    Box(modifier = modifier.padding(4.dp)) {
        val drawDuration = duration - 1.seconds


        DrawShape(
            modifier = Modifier.fillMaxSize(),
            strokeSize = strokeSize,
            morph = morph,
            brush = brush,
            duration = drawDuration,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SagaLoaderPreview() {
    SagAIScaffold {
        Column {
            SagaLoader(
                modifier = Modifier.size(200.dp),
                brush = gradientAnimation(holographicGradient, targetValue = 500f),
                animationDuration = 5.seconds,
                rotationTarget = 30f,
            )

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
                modifier = Modifier.size(200.dp),
                brush = Genre.FANTASY.gradient(),
                animationDuration = 10.seconds,
                scaleDistortion = .8f to .8f,
                tint = MaterialColor.RedA200.copy(alpha = .4f),
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

            SparkLoader(
                gradientAnimation(holographicGradient, targetValue = 1000f),
                modifier = Modifier.size(200.dp),
            )
        }
    }
}
