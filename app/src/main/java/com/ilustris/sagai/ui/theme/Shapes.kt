package com.ilustris.sagai.ui.theme

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class CustomRotatingMorphShape(
    private val morph: Morph,
    private val percentage: Float,
    private val rotation: Float,
) : Shape {
    private val matrix = Matrix()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        // Below assumes that you haven't changed the default radius of 1f, nor the centerX and centerY of 0f
        // By default this stretches the path to the size of the container, if you don't want stretching, use the same size.width for both x and y.
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)

        val path = morph.toPath(progress = percentage).asComposePath()
        path.transform(matrix)

        return Outline.Generic(path)
    }
}

class MorphPolygonShape(
    private val morph: Morph,
    private val percentage: Float,
) : Shape {
    private val matrix = Matrix()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        // Below assumes that you haven't changed the default radius of 1f, nor the centerX and centerY of 0f
        // By default this stretches the path to the size of the container, if you don't want stretching, use the same size.width for both x and y.
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)

        val path = morph.toPath(progress = percentage).asComposePath()
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

fun RoundedPolygon.getBounds() = calculateBounds().let { Rect(it[0], it[1], it[2], it[3]) }

class RoundedPolygonShape(
    private val polygon: RoundedPolygon,
    private var matrix: Matrix = Matrix(),
) : Shape {
    private var path = Path()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        path.rewind()
        path = polygon.toPath().asComposePath()
        matrix.reset()
        val bounds = polygon.getBounds()
        val maxDimension = max(bounds.width, bounds.height)
        matrix.scale(size.width / maxDimension, size.height / maxDimension)
        matrix.translate(-bounds.left, -bounds.top)

        path.transform(matrix)
        return Outline.Generic(path)
    }
}

@Composable
fun DrawShape(
    modifier: Modifier,
    strokeSize: Dp, // Your custom morph object, which can produce a Path
    morph: Morph,
    brush: Brush,
    duration: Duration = 5.seconds,
) {
    val pathMeasurer =
        remember {
            PathMeasure() // Used to measure and get segments of a path
        }
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val progress = // This progress value will go from 0f to 1f and back
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    tween(
                        duration.toInt(DurationUnit.MILLISECONDS),
                        easing = EaseIn,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "progress",
        )

    var morphPath =
        remember {
            Path() // The Path to be drawn (from your Morph object)
        }
    val destinationPath =
        remember {
            Path() // The segment of morphPath that will actually be rendered
        }
    var androidPath = // Helper for converting Morph to Compose Path
        remember {
            android.graphics.Path()
        }
    val matrix =
        remember {
            Matrix()
        }

    Box(
        modifier =
            modifier
                .drawWithCache {
                    // Good for caching expensive drawing operations
                    // 1. Get the full path from your Morph object based on current progress
                    // (Your Morph object seems to handle its own internal animation/state for morphing)
                    // For a simple stroke animation of a static path, this would just be setting a fixed path.
                    androidPath = morph.toPath(progress.value, androidPath) // Assuming morph.toPath can take progress for morphing
                    morphPath = androidPath.asComposePath()

                    // Optional: Scale the path to fit the Box (as in your code)
                    matrix.reset()
                    matrix.scale(size.minDimension / 2f, size.minDimension / 2f)
                    morphPath.transform(matrix)

                    // 2. Prepare PathMeasure
                    pathMeasurer.setPath(morphPath, false) // Set the full path to measure
                    val totalLength = pathMeasurer.length // Get total length of the path

                    // 3. Get the segment to draw
                    destinationPath.reset() // Clear previous segment
                    pathMeasurer.getSegment(
                        startDistance = 0f,
                        stopDistance = totalLength * progress.value, // Animate up to this point
                        destination = destinationPath,
                        startWithMoveTo = true, // Important to start drawing correctly
                    )

                    onDrawBehind {
                        // Or onDrawWithContent
                        translate(size.width / 2f, size.height / 2f) {
                            // Center the drawing
                            // 4. Draw ONLY the destinationPath (the animated segment)
                            drawPath(
                                path = destinationPath, // Draw the progressively revealed segment
                                brush = brush,
                                style = Stroke(width = strokeSize.toPx(), cap = StrokeCap.Round),
                            )

                            // If you wanted to show the full path underneath faintly, you could draw morphPath here too
                            // with a different style/alpha.
                        }
                    }
                },
    )
}

fun Modifier.dashedBorder(
    strokeWidth: Dp,
    color: Color,
    cornerRadiusDp: Dp,
) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }
        val cornerRadiusPx = density.run { cornerRadiusDp.toPx() }

        this.then(
            Modifier.drawWithCache {
                onDrawBehind {
                    val stroke =
                        Stroke(
                            width = strokeWidthPx,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                        )

                    drawRoundRect(
                        color = color,
                        style = stroke,
                        cornerRadius = CornerRadius(cornerRadiusPx),
                    )
                }
            },
        )
    },
)

fun Genre?.cornerSize() =
    when (this) {
        Genre.FANTASY -> 20.dp
        Genre.SCI_FI -> 10.dp
        Genre.HORROR -> 7.dp
        Genre.HEROES -> 4.dp
        else -> 0.dp
    }

fun Genre?.shape() = RoundedCornerShape(this.cornerSize())

fun Morph.toComposePath(
    progress: Float,
    scale: Float = 1f,
    path: Path = Path(),
): Path {
    var first = true
    path.rewind()
    forEachCubic(progress) { bezier ->
        if (first) {
            path.moveTo(bezier.anchor0X * scale, bezier.anchor0Y * scale)
            first = false
        }
        path.cubicTo(
            bezier.control0X * scale,
            bezier.control0Y * scale,
            bezier.control1X * scale,
            bezier.control1Y * scale,
            bezier.anchor1X * scale,
            bezier.anchor1Y * scale,
        )
    }
    path.close()
    return path
}
