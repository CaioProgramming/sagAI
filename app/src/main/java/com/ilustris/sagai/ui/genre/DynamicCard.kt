package com.ilustris.sagai.ui.genre

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Genre-neutral card chrome: sweeping decorative strokes behind a title/subtitle pair that
 * cross-fades whenever either changes.
 *
 * Lives here rather than in the review feature it was written for because the Milestone screen's
 * recap card needs the same chrome as its unstyled fallback — see
 * [com.ilustris.sagai.ui.genre.recap.GenreRecapCard]. Nothing in it knows what a review is.
 */
@Composable
fun DynamicCard(
    title: String,
    subtitle: String,
    titleStyle: TextStyle,
    subtitleStyle: TextStyle,
    lineColor: Color,
    modifier: Modifier,
) {
    val lineCount = Random.nextInt(1, 5)
    Box(modifier, contentAlignment = Alignment.Center) {
        DynamicLinework(lineColor, lineCount, modifier = Modifier.fillMaxSize(), strokeWidth = 2.dp)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(8.dp),
        ) {
            AnimatedContent(title, transitionSpec = {
                fadeIn(animationSpec = tween(500)) +
                    slideInVertically { it } togetherWith
                    fadeOut(animationSpec = tween(500)) +
                    slideOutVertically { -it }
            }) {
                Text(
                    text = it,
                    style = titleStyle,
                )
            }

            AnimatedContent(subtitle, transitionSpec = {
                fadeIn(animationSpec = tween(500)) +
                    slideInVertically { it } togetherWith
                    fadeOut(animationSpec = tween(500)) +
                    slideOutVertically { -it }
            }) {
                Text(
                    text = it,
                    style = subtitleStyle,
                )
            }
        }
    }
}

@Composable
fun DynamicLinework(
    color: Color,
    lineCount: Int,
    strokeWidth: Dp = 1.dp,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    // Generate minimalist, sweeping paths
    val paths =
        remember(size, lineCount) {
            if (size == androidx.compose.ui.unit.IntSize.Zero) return@remember emptyList<Path>()
            val width = size.width.toFloat()
            val height = size.height.toFloat()
            val random = Random(lineCount.toLong())

            List(lineCount) {
                val path = Path()
                // Randomly start from any of the 4 sides, well outside the view
                val startSide = random.nextInt(4)
                val endSide = (startSide + random.nextInt(1, 3)) % 4

                fun getPoint(side: Int): Offset {
                    val padding = 200f
                    return when (side) {
                        0 -> Offset(random.nextFloat() * width, -padding)

                        // Top
                        1 -> Offset(width + padding, random.nextFloat() * height)

                        // Right
                        2 -> Offset(random.nextFloat() * width, height + padding)

                        // Bottom
                        else -> Offset(-padding, random.nextFloat() * height) // Left
                    }
                }

                val start = getPoint(startSide)
                val end = getPoint(endSide)

                // Control points are deeply randomized to create wide, sweeping curves
                val cp1 = Offset(random.nextFloat() * width, random.nextFloat() * height)
                val cp2 = Offset(random.nextFloat() * width, random.nextFloat() * height)

                path.moveTo(start.x, start.y)
                path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y)
                path
            }
        }

    val animProgresses =
        remember(paths) {
            paths.map { Animatable(if (enabled) 0f else 1f) }
        }

    LaunchedEffect(paths, enabled) {
        if (enabled) {
            animProgresses.forEachIndexed { index, anim ->
                launch {
                    delay(index * 300L + Random.nextLong(0, 500))
                    anim.animateTo(
                        1f,
                        animationSpec =
                            tween(
                                durationMillis = 3000 + Random.nextInt(0, 2000),
                                easing = EaseOutCubic,
                            ),
                    )
                }
            }
        }
    }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .onSizeChanged { size = it },
    ) {
        paths.forEachIndexed { index, path ->
            val progress = animProgresses[index].value
            if (progress > 0f) {
                val pathMeasure = PathMeasure()
                pathMeasure.setPath(path, false)
                val segmentPath = Path()
                pathMeasure.getSegment(0f, pathMeasure.length * progress, segmentPath)

                drawPath(
                    path = segmentPath,
                    color = color,
                    style =
                        Stroke(
                            width = strokeWidth.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                )
            }
        }
    }
}
