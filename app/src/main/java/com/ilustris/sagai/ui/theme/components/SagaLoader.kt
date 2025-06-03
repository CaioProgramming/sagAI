package com.ilustris.sagai.ui.theme.components

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun SagaLoader(
    modifier: Modifier = Modifier,
    brush: Brush = gradientAnimation(holographicGradient, targetValue = 500f),
    animationDuration: Duration = 5.seconds,
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

    val rotateAnimation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    tween(5.seconds.toInt(DurationUnit.MILLISECONDS), easing = EaseIn),
                    repeatMode = Reverse,
                ),
        )

    val shapeA =
        remember {
            RoundedPolygon(
                100,
                rounding = CornerRounding(1f),
            )
        }
    val shapeB =
        remember {
            RoundedPolygon(
                5,
                rounding = CornerRounding(.5f),
            )
        }
    val morph =
        remember {
            Morph(shapeA, shapeB)
        }

    val morphProgress =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0.9f,
            animationSpec =
                infiniteRepeatable(
                    tween(animationDuration.toInt(DurationUnit.MILLISECONDS), easing = EaseIn),
                    repeatMode = Reverse,
                ),
            label = "morph",
        )

    DistortingBubble(
        modifier,
        brush,
        animationDuration,
        CircleShape,
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
    animationDduration: Duration = 5.seconds,
    shape: Shape,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val duration = animationDduration.toInt(DurationUnit.MILLISECONDS)
    // Animação para largura e altura do círculo
    val horizontalDistortion =
        infiniteTransition.animateFloat(
            initialValue = .9f,
            targetValue = 1.20f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = EaseInOut),
                    repeatMode = Reverse,
                ),
        )

    val verticalDistortion =
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.95f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = EaseInOut),
                    repeatMode = Reverse,
                ),
        )

    val rotateAnimation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 180f,
            animationSpec =
                infiniteRepeatable(
                    tween(duration, easing = EaseIn),
                    repeatMode = Reverse,
                ),
        )

    Box(modifier.padding(16.dp)) {
        Box(
            Modifier
                .fillMaxSize()
                .rotate(rotateAnimation.value)
                .scale(scaleX = horizontalDistortion.value, scaleY = verticalDistortion.value)
                .blur(10.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .border(5.dp, brush, shape),
        )
        Box(
            Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .rotate(rotateAnimation.value)
                .scale(scaleX = horizontalDistortion.value, scaleY = verticalDistortion.value)
                .background(
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .5f),
                    shape,
                ),
        )
    }
}

private fun DrawScope.drawDistortedCircle(
    horizontalScale: Float,
    verticalScale: Float,
) {
    val width = size.width * horizontalScale
    val height = size.height * verticalScale
    drawRoundRect(
        color = Color.Cyan,
        size = Size(width, height),
        cornerRadius = CornerRadius(width / 2, height / 2),
        topLeft =
            androidx.compose.ui.geometry.Offset(
                (size.width - width) / 2,
                (size.height - height) / 2,
            ),
    )
}

@Preview(showBackground = true)
@Composable
fun SagaLoaderPreview() {
    SagAIScaffold {
        SagaLoader(
            modifier = Modifier.size(200.dp),
            brush = gradientAnimation(holographicGradient, targetValue = 500f),
            animationDuration = 10.seconds,
        )
    }
}
