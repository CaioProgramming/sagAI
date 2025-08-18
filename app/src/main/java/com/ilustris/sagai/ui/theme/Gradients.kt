package com.ilustris.sagai.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
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
    targetValue: Float = 1000f,
    gradientType: GradientType = GradientType.LINEAR,
    isAnimating: Boolean = false,
): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "gradientTransition")
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
    return gradientType.toBrush(colors = colors, offsetAnimationValue = if (isAnimating) offsetAnimation.value else targetValue)
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
fun Modifier.gradientFill(
    brush: Brush,
    blendMode: BlendMode = BlendMode.SrcAtop,
) = this
    .graphicsLayer(alpha = 0.90f)
    .drawWithCache {
        onDrawWithContent {
            drawContent()
            drawRect(
                brush,
                blendMode = blendMode,
            )
        }
    }

fun Color.fadeColors() = listOf(
    this,
    this.copy(alpha = 0.5f),
    this.copy(alpha = 0.2f),
    Color.Transparent,
)

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
fun genresGradient(): List<Color> =
    Genre.entries
        .map {
            it.colorPalette()
        }.flatten()
        .plus(holographicGradient)

@Composable
fun Genre.gradient(
    animated: Boolean = false,
    duration: Duration = 3.seconds,
    targetValue: Float = 500f,
    gradientType: GradientType = GradientType.VERTICAL,
) = if (animated) {
    gradientAnimation(this.colorPalette(), duration, targetValue, gradientType)
} else {
    gradientType.toBrush(colors = this.colorPalette(), offsetAnimationValue = targetValue)
}

fun Color.solidGradient() = Brush.verticalGradient(List(2) { this })

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
    endFadeFraction: Float = 0.3f,
): Modifier =
    this
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithCache {
            val gradientBrush =
                when (fadeDirection) {
                    FadeDirection.BOTTOM_TO_TOP ->
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = size.height * (1f - startFadeFraction),
                            endY = size.height * (1f - endFadeFraction),
                        )

                    FadeDirection.TOP_TO_BOTTOM ->
                        Brush.verticalGradient(
                            colors = listOf(Color.Black, Color.Transparent),
                            startY = size.height * startFadeFraction,
                            endY = size.height * endFadeFraction,
                        )

                    FadeDirection.LEFT_TO_RIGHT ->
                        Brush.horizontalGradient(
                            colors = listOf(Color.Black, Color.Transparent),
                            startX = size.width * startFadeFraction,
                            endX = size.width * endFadeFraction,
                        )

                    FadeDirection.RIGHT_TO_LEFT ->
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startX = size.width * (1f - startFadeFraction),
                            endX = size.width * (1f - endFadeFraction),
                        )
                }

            onDrawWithContent {
                // 1. Draw the original content of the Composable this modifier is applied to
                drawContent()

                // 2. Draw the gradient mask on top with DstIn blend mode
                drawRect(
                    brush = gradientBrush,
                    blendMode = BlendMode.DstIn,
                )
            }
        }

@Composable
fun Modifier.reactiveShimmer(
    isPlaying: Boolean,
    shimmerColors: List<Color> =
        listOf(
            Color.White.copy(alpha = 0.0f),
            Color.White.copy(alpha = 0.5f),
            Color.White.copy(alpha = 0.2f),
            Color.White.copy(alpha = 0.1f),
            Color.White.copy(alpha = 0.0f),
        ),
    duration: Duration = 2.seconds,
    targetValue: Float = 500f
): Modifier {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetAnimation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec =
                infiniteRepeatable(
                    tween(duration.toInt(DurationUnit.MILLISECONDS), easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
        )

    val brush =
        Brush.linearGradient(
            shimmerColors.plus(Color.Transparent),
            start = if (isPlaying) Offset(offsetAnimation.value, offsetAnimation.value) else Offset.Zero,
            end = if (isPlaying) Offset(x = offsetAnimation.value * 5, y = offsetAnimation.value * 3) else Offset.Infinite,
        )
    return this
        .graphicsLayer(alpha = 0.99f)
        .drawWithCache {
            onDrawWithContent {
                drawContent()
                if (isPlaying) {
                    drawRect(brush, blendMode = BlendMode.SrcAtop)
                }
            }
        }
}

/**
 * Creates and remembers a Brush that animates its gradient colors
 * by shuffling the provided colorPalette whenever the PagerState's current page changes.
 *
 * @param pagerState The PagerState to observe for page changes.
 * @param colorPalette The list of colors to be used in the gradient. This list itself will be shuffled.
 * @param animationDurationMillis The duration for the color transition animation.
 * @param createGradient A lambda to create the Brush from the list of animated colors.
 *                       Defaults to a vertical gradient.
 * @return An animated Brush.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberAnimatedShuffledGradientBrush(
    pagerState: PagerState,
    colorPalette: List<Color>, // The direct palette to be shuffled
    animationDurationMillis: Int = 1000,
    createGradient: (List<Color>) -> Brush = { colors -> Brush.verticalGradient(colors) },
): Brush {
    // Ensure the palette is not empty to avoid issues
    val safeColorPalette =
        remember(colorPalette) {
            if (colorPalette.isEmpty()) listOf(Color.Transparent, Color.Transparent) else colorPalette
        }

    var currentGradientColors by remember {
        mutableStateOf(safeColorPalette)
    }

    LaunchedEffect(pagerState.currentPage, safeColorPalette) {
        // Shuffle the current palette to get a new order
        currentGradientColors = safeColorPalette.shuffled()
    }

    val animatedColors =
        currentGradientColors.mapIndexed { index, targetColor ->
            animateColorAsState(
                targetValue = targetColor,
                animationSpec = tween(durationMillis = animationDurationMillis),
                label = "gradientColorAnimation_$index",
            ).value
        }

    // Create the gradient using the provided lambda
    return remember(animatedColors, createGradient) {
        createGradient(animatedColors)
    }
}
