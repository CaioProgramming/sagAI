package com.ilustris.sagai.ui.theme

import ai.atick.material.MaterialColor
import android.graphics.BlurMaskFilter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import kotlinx.coroutines.delay
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
            LINEAR -> {
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(offsetAnimationValue, offsetAnimationValue),
                    end = Offset(offsetAnimationValue + 500f, offsetAnimationValue + 500f),
                    tileMode = TileMode.Mirror,
                )
            }

            VERTICAL -> {
                Brush.verticalGradient(
                    colors = colors,
                    startY = offsetAnimationValue,
                    endY = offsetAnimationValue + 500f,
                    tileMode = TileMode.Mirror,
                )
            }

            RADIAL -> {
                Brush.radialGradient(
                    colors = colors,
                    center = Offset(offsetAnimationValue, offsetAnimationValue),
                    radius = offsetAnimationValue + 300f,
                    tileMode = TileMode.Mirror,
                )
            }

            SWEEP -> {
                Brush.sweepGradient(
                    colors = colors,
                    center = Offset(offsetAnimationValue, offsetAnimationValue),
                )
            }
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
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "Gradient Offset Animation",
        )
    return gradientType.toBrush(
        colors = colors,
        offsetAnimationValue = if (isAnimating) offsetAnimation.value else targetValue,
    )
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

/** Scrim on top of a portrait (transparent at the top edge → tint), mirrors [fadeGradientBottom]. */
@Composable
fun fadeGradientTopOverImage(tintColor: Color = MaterialTheme.colorScheme.background) =
    Brush.verticalGradient(
        0f to Color.Transparent,
        0.4f to tintColor.copy(alpha = 0.3f),
        0.7f to tintColor.copy(alpha = 0.65f),
        1f to tintColor.copy(alpha = 0.92f),
    )

fun Color.blendedWith(
    other: Color,
    fraction: Float,
): Color =
    androidx.compose.ui.graphics
        .lerp(this, other, fraction)

/** Header scrim mixing saga adaptive color with character accent for readable titles. */
fun characterDetailsHeaderScrim(
    adaptiveColor: Color,
    characterAccent: Color,
): Color = adaptiveColor.blendedWith(characterAccent, 0.22f)

fun characterDetailsTitleGradient(
    legibleOnScrim: Color,
    characterAccent: Color,
): Brush =
    Brush.verticalGradient(
        0f to legibleOnScrim,
        0.45f to legibleOnScrim.blendedWith(characterAccent, 0.4f),
        1f to legibleOnScrim,
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

fun Modifier.iconDropShadow(
    brush: Brush,
    progress: Float,
    blurRadius: Dp = 15.dp,
    spread: Dp = 8.dp,
): Modifier {
    val clampedProgress = progress.coerceIn(0f, 1f)
    if (clampedProgress <= 0f) return this

    return drawWithCache {
        val blurPx = (blurRadius.toPx() + spread.toPx()) * clampedProgress
        onDrawWithContent {
            drawIntoCanvas { canvas ->
                val bounds = Rect(Offset.Zero, size)
                val glowPaint =
                    Paint().apply {
                        asFrameworkPaint().maskFilter =
                            BlurMaskFilter(
                                blurPx.coerceAtLeast(0.1f),
                                BlurMaskFilter.Blur.NORMAL,
                            )
                    }
                canvas.saveLayer(bounds, glowPaint)
                drawContent()
                canvas.restore()
                drawRect(brush = brush, size = size, blendMode = BlendMode.SrcIn)
            }
            drawContent()
        }
    }
}

fun Color.fadeColors() =
    listOf(
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
        MaterialColor.Purple800,
        MaterialColor.Orange400,
        MaterialColor.PinkA200,
        MaterialColor.Pink900,
        MaterialColor.Purple800,
        MaterialColor.Blue900,
        MaterialColor.Purple200,
    )

val iridescentGradient =
    listOf(
        Color(0xFF90E0EF), // Electric Blue
        Color(0xFFB1A7F0), // Deep Lavender
        Color(0xFFF7D1CD), // Soft Rose
        Color(0xFFE2CFEA), // Pale Lilac
        Color(0xFFA0CED9), // Sky Blue
        Color(0xFFADF7B6), // Mint
    )

@Composable
fun themeShimmer() =
    buildList {
        add(Color.Transparent)
        addAll(themeBrushColors())
        add(Color.Transparent)
    }

@Composable
fun genresGradient(): List<Color> =
    Genre.entries
        .map {
            it.colorPalette()
        }.flatten()
        .plus(holographicGradient)

@Composable
fun Genre?.gradient(
    animated: Boolean = false,
    duration: Duration = 3.seconds,
    targetValue: Float = 500f,
    gradientType: GradientType = GradientType.LINEAR,
) = if (animated) {
    gradientAnimation(
        this?.colorPalette() ?: holographicGradient,
        duration,
        targetValue,
        gradientType,
    )
} else {
    gradientType.toBrush(
        colors = this?.colorPalette() ?: holographicGradient,
        offsetAnimationValue = targetValue,
    )
}

fun Color.solidGradient() = SolidColor(this)

fun Color.shimmerize() =
    listOf(
        Color.Transparent,
        this.copy(alpha = .1f),
        this.copy(alpha = .5f),
        this,
        this.copy(alpha = .3f),
        this.copy(alpha = .1f),
        Color.Transparent,
    )

enum class FadeDirection {
    TOP_TO_BOTTOM,
    BOTTOM_TO_TOP,
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    // You could add more, like corners, etc.
}

@Composable
fun Modifier.reactiveShimmer(
    isPlaying: Boolean,
    shimmerColors: List<Color> = themeShimmer(),
    duration: Duration = 5.seconds,
    targetValue: Float = 500f,
    repeatMode: RepeatMode = RepeatMode.Reverse,
): Modifier {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetAnimation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec =
                infiniteRepeatable(
                    tween(duration.toInt(DurationUnit.MILLISECONDS), easing = LinearEasing),
                    repeatMode = repeatMode,
                ),
        )

    val brush =
        remember(shimmerColors, isPlaying, offsetAnimation.value) {
            val colors = shimmerColors.plus(Color.Transparent)
            val finalColors =
                if (colors.size < 2) {
                    listOf(Color.Transparent, Color.Transparent)
                } else {
                    colors
                }
            Brush.linearGradient(
                finalColors,
                start =
                    if (isPlaying) {
                        Offset(
                            offsetAnimation.value,
                            offsetAnimation.value,
                        )
                    } else {
                        Offset.Zero
                    },
                end =
                    if (isPlaying) {
                        Offset(
                            x = offsetAnimation.value * 5,
                            y = offsetAnimation.value * 3,
                        )
                    } else {
                        Offset.Infinite
                    },
            )
        }
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
            if (colorPalette.isEmpty()) {
                listOf(
                    Color.Transparent,
                    Color.Transparent,
                )
            } else {
                colorPalette
            }
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

@Composable
fun progressiveBrush(
    tintColor: Color,
    progress: Float,
    animationDuration: Int = 1000,
): Brush {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
        label = "progressAnimation",
    )

    val stop = 1f - animatedProgress
    return Brush.verticalGradient(
        0f to Color.Transparent,
        stop to Color.Transparent,
        stop + 0.001f to tintColor,
        1f to tintColor,
    )
}

@Composable
fun morphingGradient(
    colors: List<Color> = themeBrushColors(),
    duration: Duration = 2.seconds,
): List<Color> {
    // Use a mutable state so updates trigger recomposition and the animated targets change.
    var brushColors by remember { mutableStateOf(colors) }

    // Only run the shuffle loop while the composable's lifecycle is at least STARTED
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    var isStarted by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }

    DisposableEffect(lifecycle) {
        val observer =
            LifecycleEventObserver { _, _ ->
                isStarted = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(isStarted) {
        if (!isStarted) return@LaunchedEffect
        while (true) {
            delay(duration)
            brushColors = brushColors.shuffled()
        }
    }

    val animatedColors =
        brushColors.map {
            animateColorAsState(
                it,
                tween(
                    durationMillis =
                        (duration.toInt(DurationUnit.MILLISECONDS) / 2).coerceAtLeast(
                            1,
                        ),
                    easing = EaseIn,
                ),
            )
        }

    return animatedColors.map { it.value }
}
