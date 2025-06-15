package com.ilustris.sagai.ui.theme

import ai.atick.material.MaterialColor
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

enum class GradientType {
    LINEAR,
    VERTICAL,
    RADIAL,
    SWEEP,
    ;

    fun toBrush(
        colors: List<Color>,
        offsetAnimationValue: Float,
    ): Brush =
        when (this) {
            LINEAR ->
                Brush.linearGradient(
                    colors = colors,
                    start = Offset.Zero,
                    end = Offset(offsetAnimationValue * 2, offsetAnimationValue * 3),
                    tileMode = TileMode.Clamp,
                )
            VERTICAL ->
                Brush.verticalGradient(
                    colors = colors,
                    startY = 0f,
                    endY = offsetAnimationValue * 3,
                    tileMode = TileMode.Clamp,
                )
            RADIAL ->
                Brush.radialGradient(
                    colors = colors,
                    center = Offset(offsetAnimationValue, offsetAnimationValue),
                    radius = offsetAnimationValue * 2,
                    tileMode = TileMode.Clamp,
                )
            SWEEP ->
                Brush.sweepGradient(
                    colors = colors,
                    center = Offset(offsetAnimationValue, offsetAnimationValue),
                )
        }
}

@Composable
fun gradientAnimation(
    colors: List<Color> = themeBrushColors(),
    duration: Duration = 3.seconds,
    targetValue: Float = 100f,
    gradientType: GradientType = GradientType.LINEAR,
): Brush {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetAnimation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec =
                infiniteRepeatable(
                    tween(
                        duration.toInt(DurationUnit.MILLISECONDS),
                        easing = EaseIn,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "Gradient Offset Animation",
        )
    return gradientType.toBrush(colors = colors, offsetAnimationValue = offsetAnimation.value)
}

@Composable
fun fadeGradientBottom(tintColor: Color = MaterialTheme.colorScheme.background) =
    Brush.verticalGradient(
        0f to Color.Transparent,
        0.5f to tintColor.copy(alpha = .5f),
        1f to tintColor,
    )

@Composable
fun fadeGradientTop(tintColor: Color = MaterialTheme.colorScheme.background) =
    Brush.verticalGradient(
        0f to tintColor,
        0.5f to tintColor.copy(alpha = .5f),
        1f to Color.Transparent,
    )

@Composable
fun fadedGradientTopAndBottom(tintColor: Color = MaterialTheme.colorScheme.background): Brush =
    Brush.verticalGradient(
        0f to tintColor,
        0.5f to Color.Transparent,
        1f to tintColor,
    )

@Composable
fun Modifier.gradientFill(brush: Brush) =
    this
        .graphicsLayer(alpha = 0.99f)
        .drawWithCache {
            onDrawWithContent {
                drawContent()
                drawRect(
                    brush,
                    blendMode = BlendMode.SrcAtop,
                )
            }
        }

fun Color.gradientFade() =
    Brush.verticalGradient(
        listOf(
            this,
            this.copy(alpha = 0.5f),
            this.copy(alpha = 0.2f),
            Color.Transparent,
        ),
    )

fun Color.darkerPalette(
    count: Int = 4,
    factor: Float = 0.1f,
): List<Color> =
    List(count) {
        val indexColorFactor = it * factor
        this.darker(indexColorFactor)
    }

val holographicGradient =
    listOf(
        Color(0xfffcc5e4),
        Color(0xfffda34b),
        Color(0xffff7882),
        Color(0xffc8699e),
        Color(0xff7046aa),
        Color(0xff3e52ee),
        Color(0xffd3a7ff),
    )

@Composable
fun genresGradient(): List<Color> {
    val colors =
        Genre.entries.map {
            it.gradient()
        }

    return colors.flatten()
}

@Composable
fun Genre.gradient(): List<Color> {
    val isDarkTheme = isSystemInDarkTheme()
    return List(4) {
        val indexColorFactor = it / 10f
        if (isDarkTheme.not()) {
            this.color.lighter(indexColorFactor)
        } else {
            this.color.darker(indexColorFactor)
        }
    }
}

@Composable
fun Genre.userBubbleGradient(): Brush {
    val colors = this.gradient()
    return Brush.linearGradient(
        colors = colors,
        start = Offset.Zero,
        end = Offset(100f, 100f),
        tileMode = TileMode.Clamp,
    )
}

@Composable
fun Genre.botBubbleGradient(): Brush {
    val color =
        when (this) {
            Genre.FANTASY -> MaterialColor.Orange100
            Genre.SCI_FI -> MaterialColor.BlueGray700
            else -> MaterialTheme.colorScheme.secondary
        }
    val colors =
        List(4) {
            color.darker(it / 10f)
        }
    return Brush.linearGradient(
        colors = colors,
        start = Offset(100f, 100f),
        end = Offset.Zero,
        tileMode = TileMode.Clamp,
    )
}

enum class FadeDirection {
    TOP_TO_BOTTOM,
    BOTTOM_TO_TOP,
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    // You could add more, like corners, etc.
}

fun Modifier.fadeMask(
    fadeDirection: FadeDirection,
    startFadeFraction: Float = 0.0f,
    endFadeFraction: Float = 0.3f
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithCache {
        val gradientBrush = when (fadeDirection) {
            FadeDirection.BOTTOM_TO_TOP -> Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black),
                startY = size.height * (1f - startFadeFraction),
                endY = size.height * (1f - endFadeFraction)
            )
            FadeDirection.TOP_TO_BOTTOM -> Brush.verticalGradient(
                colors = listOf(Color.Black, Color.Transparent),
                startY = size.height * startFadeFraction,
                endY = size.height * endFadeFraction
            )
            FadeDirection.LEFT_TO_RIGHT -> Brush.horizontalGradient(
                colors = listOf(Color.Black, Color.Transparent),
                startX = size.width * startFadeFraction,
                endX = size.width * endFadeFraction
            )
            FadeDirection.RIGHT_TO_LEFT -> Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Black),
                startX = size.width * (1f - startFadeFraction),
                endX = size.width * (1f - endFadeFraction)
            )
        }

        onDrawWithContent {
            // 1. Draw the original content of the Composable this modifier is applied to
            drawContent()

            // 2. Draw the gradient mask on top with DstIn blend mode
            drawRect(
                brush = gradientBrush,
                blendMode = BlendMode.DstIn
            )
        }
    }