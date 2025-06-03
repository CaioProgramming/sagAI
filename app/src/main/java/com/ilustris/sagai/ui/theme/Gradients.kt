package com.ilustris.sagai.ui.theme

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun gradientAnimation(
    colors: List<Color>,
    duration: Duration = 3.seconds,
    targetValue: Float = 100f,
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
    return Brush.linearGradient(
        colors = colors,
        start = Offset.Zero,
        end = Offset(offsetAnimation.value * 2, offsetAnimation.value * 3),
        tileMode = TileMode.Clamp,
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

@Composable
fun fadedGradientTopAndBottom(
    tintColor: Color = MaterialTheme.colorScheme.background,
): Brush =
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

val holographicGradient =
    listOf(
        Color(0xfffcc5e4),
        Color(0xfffda34b),
        Color(0xffff7882),
        Color(0xffc8699e),
        Color(0xff7046aa),
        Color(0xff020f75),
    )

fun genresGradient(): List<Color> {
    val colors =
        Genre.entries.map {
            it.gradient()
        }

    return colors.flatten()
}

fun Genre.gradient(): List<Color> =
    when (this) {
        Genre.FANTASY ->
            listOf(
                Color(0xffff0844),
                Color(0xffffb199),
                Color(0xff7a011e),
            )

        Genre.SCI_FI ->
            listOf(
                Color(0xff5271c4),
                Color(0xffb19fff),
                Color(0xffeca1fe),
            )
    }
