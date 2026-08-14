package com.ilustris.sagai.ui.theme.components

import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

private data class RevealContour(
    val path: android.graphics.Path,
    val length: Float,
)

/**
 * Text that appears to be traced by a pen, stroke by stroke, instead of fading or typing in.
 * Extracts each glyph's outline via [android.graphics.Paint.getTextPath], splits it into its
 * individual contours (a letter with a hole, like "e" or "a", is more than one contour), and
 * reveals a growing sub-segment of each contour in reading order every frame — the same
 * "stroke-dashoffset" trick used for SVG line-drawing animations.
 *
 * Deliberately stays in `android.graphics.Path`/`PathMeasure` for all the contour math — that's
 * the classic, long-stable API with real multi-contour support via `nextContour()`. The Compose
 * `androidx.compose.ui.graphics.PathMeasure` wrapper has no such method, so contours are only
 * converted to a Compose `Path` at the very last step, right before `drawPath`.
 *
 * Self-contained (own word-wrap + glyph measurement via a plain [android.graphics.Paint]) rather
 * than reusing Compose's own text layout, since there's no public API to get a glyph outline back
 * out of a [androidx.compose.ui.text.TextLayoutResult].
 */
@Composable
fun HandwrittenText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontSize: TextUnit = 18.sp,
    isItalic: Boolean = true,
    strokeWidth: Dp = 1.4.dp,
    lineSpacing: Float = 1.25f,
    duration: Duration = 2500.milliseconds,
    isAnimated: Boolean = true,
    onAnimationFinished: () -> Unit = {},
) {
    val density = LocalDensity.current
    val fontSizePx = with(density) { fontSize.toPx() }
    val strokeWidthPx = with(density) { strokeWidth.toPx() }

    val progress = remember { Animatable(if (isAnimated) 0f else 1f) }

    LaunchedEffect(text, isAnimated) {
        if (isAnimated) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(duration.toInt(DurationUnit.MILLISECONDS), easing = LinearEasing))
        } else {
            progress.snapTo(1f)
        }
    }

    LaunchedEffect(progress.value) {
        if (progress.value >= 1f) onAnimationFinished()
    }

    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = with(density) { maxWidth.toPx() }

        val paint =
            remember(fontSizePx, isItalic) {
                android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = Typeface.create(Typeface.SERIF, if (isItalic) Typeface.ITALIC else Typeface.NORMAL)
                    textSize = fontSizePx
                }
            }

        val (contours, totalLength, blockHeightPx) =
            remember(text, maxWidthPx, fontSizePx, isItalic, lineSpacing) {
                buildHandwrittenContours(text, paint, maxWidthPx, lineSpacing)
            }

        val heightDp = with(density) { blockHeightPx.toDp() }

        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(heightDp),
        ) {
            drawHandwrittenReveal(contours, totalLength * progress.value, color, strokeWidthPx)
        }
    }
}

/** Greedy word-wrap using the same [paint] that will later measure glyph outlines. */
private fun wrapText(
    text: String,
    paint: android.graphics.Paint,
    maxWidthPx: Float,
): List<String> {
    if (maxWidthPx <= 0f) return listOf(text)

    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = StringBuilder()

    for (word in words) {
        val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
        if (currentLine.isEmpty() || paint.measureText(candidate) <= maxWidthPx) {
            currentLine = StringBuilder(candidate)
        } else {
            lines += currentLine.toString()
            currentLine = StringBuilder(word)
        }
    }
    if (currentLine.isNotEmpty()) lines += currentLine.toString()
    return lines
}

/**
 * Wraps [text] to [maxWidthPx], extracts every line's glyph outline, and splits each outline into
 * its individual contours in reading order. Returns the flattened contour list, their combined
 * length (used to scale the reveal progress), and the wrapped block's total height in pixels.
 */
private fun buildHandwrittenContours(
    text: String,
    paint: android.graphics.Paint,
    maxWidthPx: Float,
    lineSpacing: Float,
): Triple<List<RevealContour>, Float, Float> {
    val lines = wrapText(text, paint, maxWidthPx)
    if (lines.isEmpty()) return Triple(emptyList(), 0f, 0f)

    val metrics = paint.fontMetrics
    val lineHeight = (metrics.descent - metrics.ascent) * lineSpacing
    val firstBaseline = -metrics.ascent

    val contours = mutableListOf<RevealContour>()
    var total = 0f

    lines.forEachIndexed { index, line ->
        if (line.isBlank()) return@forEachIndexed

        val outline = android.graphics.Path()
        val baselineY = firstBaseline + index * lineHeight
        paint.getTextPath(line, 0, line.length, 0f, baselineY, outline)

        val contourMeasure = android.graphics.PathMeasure(outline, false)
        do {
            val contourLength = contourMeasure.length
            if (contourLength > 0f) {
                val contourPath = android.graphics.Path()
                contourMeasure.getSegment(0f, contourLength, contourPath, true)
                contours += RevealContour(contourPath, contourLength)
                total += contourLength
            }
        } while (contourMeasure.nextContour())
    }

    val blockHeight = firstBaseline + (lines.size - 1) * lineHeight + metrics.descent
    return Triple(contours, total, blockHeight)
}

/** Draws every contour up to its share of [budget] (in path-length units), in reading order. */
private fun DrawScope.drawHandwrittenReveal(
    contours: List<RevealContour>,
    budget: Float,
    color: Color,
    strokeWidthPx: Float,
) {
    if (budget <= 0f) return

    var remaining = budget
    val segment = android.graphics.Path()

    for (contour in contours) {
        if (remaining <= 0f) break
        val take = minOf(remaining, contour.length)

        segment.reset()
        android.graphics.PathMeasure(contour.path, false).getSegment(0f, take, segment, true)

        drawPath(
            path = segment.asComposePath(),
            color = color,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        remaining -= take
    }
}
