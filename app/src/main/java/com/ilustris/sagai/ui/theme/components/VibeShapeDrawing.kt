package com.ilustris.sagai.ui.theme.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.toPath
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import kotlin.random.Random

/**
 * A reusable composable that draws an emotional shape with an organic, hand-drawn look.
 * It can optionally draw entrance and exit lines that sweep across the screen.
 */
@Composable
fun VibeShapeDrawing(
    emotionalTone: EmotionalTone,
    modifier: Modifier = Modifier,
    color: Color = emotionalTone.color,
    duration: Int = 5000,
    strokeWidth: Dp = 2.dp,
    showEntranceLine: Boolean = true,
    showExitLine: Boolean = true,
    onFinishDraw: () -> Unit = {},
) {
    val targetShape = remember(emotionalTone) { emotionalTone.starShape() }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(emotionalTone) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            1f,
            animationSpec = tween(duration, easing = EaseInBounce),
        )
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f

        // Scale shape relative to the smaller dimension
        val shapeScale = minOf(width, height) / 3f

        // 1. Prepare the core shape path
        val androidShapePath = targetShape.toPath()
        val matrix = android.graphics.Matrix()
        matrix.postScale(shapeScale, shapeScale)
        matrix.postTranslate(centerX, centerY)
        androidShapePath.transform(matrix)
        val composeShapePath = androidShapePath.asComposePath()

        val shapeMeasure = PathMeasure()
        shapeMeasure.setPath(composeShapePath, false)
        val shapeLength = shapeMeasure.length

        // 2. Find connection points
        var leftMostDist = 0f
        var minX = Float.MAX_VALUE
        var rightMostDist = 0f
        var maxX = Float.MIN_VALUE

        val samples = 30
        for (i in 0..samples) {
            val dist = (i.toFloat() / samples) * shapeLength
            val pos = shapeMeasure.getPosition(dist)
            if (pos.x < minX) {
                minX = pos.x
                leftMostDist = dist
            }
            if (pos.x > maxX) {
                maxX = pos.x
                rightMostDist = dist
            }
        }

        val leftPoint = shapeMeasure.getPosition(leftMostDist)
        val rightPoint = shapeMeasure.getPosition(rightMostDist)

        // 3. Build the SMOOTH continuous path
        val fullPath = Path()
        val random = Random(emotionalTone.hashCode())

        // Entrance line
        if (showEntranceLine) {
            val entranceStart = Offset(0f, centerY + (random.nextFloat() - 0.5f) * height * 0.4f)
            val entranceCp1 = Offset(width * 0.1f, centerY - (random.nextFloat() * height * 0.2f))
            val entranceCp2 = Offset(width * 0.3f, leftPoint.y + (random.nextFloat() - 0.5f) * 100f)
            fullPath.moveTo(entranceStart.x, entranceStart.y)
            fullPath.cubicTo(
                entranceCp1.x,
                entranceCp1.y,
                entranceCp2.x,
                entranceCp2.y,
                leftPoint.x,
                leftPoint.y,
            )
        } else {
            fullPath.moveTo(leftPoint.x, leftPoint.y)
        }

        // Sequential Shape Loop (Clean and Smooth)
        // We manually append points from the shapeMeasure to fullPath to keep it as a SINGLE contour
        val step = 2f
        var currentShapeDist = 0f
        while (currentShapeDist <= shapeLength) {
            val actualDist = (leftMostDist + currentShapeDist) % shapeLength
            val pos = shapeMeasure.getPosition(actualDist)
            fullPath.lineTo(pos.x, pos.y)
            currentShapeDist += step
        }

        // Ensure we are exactly at the right point before starting the exit
        // We travel along the shape once more to the rightPoint to ensure the path is continuous
        var transitionDist = 0f
        val distToRight =
            if (rightMostDist >= leftMostDist) rightMostDist - leftMostDist else (shapeLength - leftMostDist) + rightMostDist
        while (transitionDist <= distToRight) {
            val actualDist = (leftMostDist + transitionDist) % shapeLength
            val pos = shapeMeasure.getPosition(actualDist)
            fullPath.lineTo(pos.x, pos.y)
            transitionDist += step
        }

        // Exit line
        if (showExitLine) {
            val exitEnd = Offset(width, centerY + (random.nextFloat() - 0.5f) * height * 0.4f)
            val exitCp1 = Offset(width * 0.7f, rightPoint.y + (random.nextFloat() - 0.5f) * 100f)
            val exitCp2 = Offset(width * 0.9f, centerY + (random.nextFloat() * height * 0.2f))
            fullPath.cubicTo(exitCp1.x, exitCp1.y, exitCp2.x, exitCp2.y, exitEnd.x, exitEnd.y)
        }

        // 4. Draw the segment based on progress
        val fullMeasure = PathMeasure()
        fullMeasure.setPath(fullPath, false)
        val segmentPath = Path()
        fullMeasure.getSegment(0f, fullMeasure.length * animProgress.value, segmentPath)

        drawPath(
            path = segmentPath,
            color = color,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
        )
    }

    LaunchedEffect(animProgress.value) {
        if (animProgress.value == 1f) {
            onFinishDraw()
        }
    }
}
