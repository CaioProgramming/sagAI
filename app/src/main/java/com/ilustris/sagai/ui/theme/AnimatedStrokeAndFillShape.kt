package com.ilustris.sagai.ui.theme

import android.graphics.RectF
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath // Assuming this is from your graphics-shapes library
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun AnimatedStrokeAndFillShape(
    modifier: Modifier,
    morph: Morph, // Your custom morph object, which can produce a Path
    strokeBrush: Brush,
    fillBrush: Brush,
    strokeWidth: Dp = 5.dp,
    duration: Duration = 5.seconds,
) {
    val pathMeasurer = remember { PathMeasure() }
    val infiniteTransition = rememberInfiniteTransition(label = "strokeAndFill")

    // Single progress for both stroke and fill
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, // Start from 0 to draw and fill
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration.toInt(DurationUnit.MILLISECONDS), easing = LinearEasing),
            repeatMode = RepeatMode.Restart // Or Reverse if you want it to undraw/unfill
        ),
        label = "drawProgress",
    )

    val density = LocalDensity.current
    val strokeWidthPx = remember(strokeWidth) { with(density) { strokeWidth.toPx() } }

    var fullPathFromMorph = remember { Path() } // The complete path for the current morph state
    var strokeSegmentPath = remember { Path() } // The segment for the animated stroke
    var androidPath = remember { android.graphics.Path() } // Helper for graphics-shapes
    val matrix = remember { Matrix() }


    Box(
        modifier = modifier.drawWithCache {
            // 1. Get the full path from your Morph object
            // For simplicity in this example, let's assume morph.toPath takes a fixed progress for its shape
            // If your morph's shape itself should animate with the 'progress', adjust accordingly.
            // Here, we'll use a fixed morph (e.g., the end state of a morph) or a morph that
            // uses its own independent progress for the shape itself.
            // Let's assume progress here is purely for the draw/fill animation, not shape morphing.
            androidPath = morph.toPath(1f, androidPath) // Get the fully morphed shape
            fullPathFromMorph = androidPath.asComposePath()

            // Optional: Scale the path to fit the Box
            matrix.reset()
            val bounds =
                fullPathFromMorph.getBounds() // You might need a getBounds() extension for compose Path
            val pathWidth = bounds.width
            val pathHeight = bounds.height
            if (pathWidth > 0 && pathHeight > 0) {
                val scaleX = size.width / pathWidth
                val scaleY = size.height / pathHeight
                val scale = minOf(scaleX, scaleY) // Maintain aspect ratio
                matrix.scale(scale, scale)
                matrix.translate(
                    (size.width - pathWidth * scale) / (2f * scale), // Center after scaling
                    (size.height - pathHeight * scale) / (2f * scale)
                )
                fullPathFromMorph.transform(matrix)
            }


            // 2. Prepare PathMeasure for the stroke
            pathMeasurer.setPath(fullPathFromMorph, false)
            val totalLength = pathMeasurer.length

            // 3. Get the segment for the stroke
            strokeSegmentPath.reset()
            pathMeasurer.getSegment(
                startDistance = 0f,
                stopDistance = totalLength * progress,
                destination = strokeSegmentPath,
                startWithMoveTo = true
            )


            onDrawBehind {
                // translate(size.width / 2f, size.height / 2f) // Centering might be handled by matrix now

                // --- Fill Animation ---
                // We will fill the fullPathFromMorph but clip it to grow with progress.
                // The "margin" is achieved because the stroke will be drawn on top and is thicker.

                // Create a clipping path that grows.
                // For a simple "growing circle" clip effect:
                // val fillClipRadius = (max(size.width, size.height) / 2f) * progress
                // val clipPathForFill = Path().apply { addOval(Rect(center - Offset(fillClipRadius, fillClipRadius), Size(fillClipRadius * 2, fillClipRadius * 2))) }

                // For a fill that follows the path shape more closely:
                // We can scale down the fullPath slightly for the fill, or draw the full path
                // and rely on the stroke to overlap. For a distinct margin, scaling down the fill path
                // or using a clip path that is an inset of the main path is better.
                // Here, let's try filling the full path but clip it as if it's "revealed"
                // along with the stroke.

                clipPath(
                    path = strokeSegmentPath, // Clip the fill to the currently drawn stroke segment
                    // This creates a direct fill-under-stroke effect.
                    // For a margin, the stroke would need to be thicker than
                    // the conceptual edge of this fill.
                    // OR, the fill path itself could be an inset.
                    clipOp = ClipOp.Intersect
                ) {
                    drawPath(
                        path = fullPathFromMorph, // Draw the whole shape filled
                        brush = fillBrush,
                        style = Fill // Fill style
                    )
                }


                // --- Stroke Animation ---
                // Draw the animated stroke segment on top
                drawPath(
                    path = strokeSegmentPath,
                    brush = strokeBrush,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round, // Or Butt/Square
                        join = StrokeJoin.Round // For smoother corners
                    )
                )
            }
        }
    )
}

// Helper (you might already have this or similar for Compose Path)
fun Path.getBounds(): Rect {
    if (isEmpty) return Rect.Zero
    val bounds = RectF()
    asAndroidPath().computeBounds(bounds, true)
    return Rect(bounds.left, bounds.top, bounds.right, bounds.bottom)
}