package com.ilustris.sagai.ui.theme.components

import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
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
 * One glyph's outline (used whole, once fully traced, to paint it as solid ink) plus its
 * individual contours in reading order (used to animate the partial pen-stroke while this is
 * the glyph currently being "written").
 */
private data class RevealGlyph(
    val outline: android.graphics.Path,
    val contours: List<RevealContour>,
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
    fontFamily: FontFamily = MaterialTheme.typography.headlineSmall.fontFamily ?: FontFamily.Default,
    fontSize: TextUnit = 18.sp,
    isItalic: Boolean = true,
    isBold: Boolean = false,
    centered: Boolean = false,
    strokeWidth: Dp = 1.4.dp,
    lineSpacing: Float = 1.25f,
    duration: Duration = 3800.milliseconds,
    isAnimated: Boolean = true,
    shadow: Shadow? = null,
    onAnimationFinished: () -> Unit = {},
) {
    val density = LocalDensity.current
    val fontSizePx = with(density) { fontSize.toPx() }
    val strokeWidthPx = with(density) { strokeWidth.toPx() }

    val progress = remember { Animatable(if (isAnimated) 0f else 1f) }
    val shadowAlpha = remember { Animatable(0f) }

    LaunchedEffect(text, isAnimated) {
        shadowAlpha.snapTo(0f)
        if (isAnimated) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(duration.toInt(DurationUnit.MILLISECONDS), easing = FastOutSlowInEasing))
        } else {
            progress.snapTo(1f)
        }
    }

    LaunchedEffect(progress.value, shadow) {
        if (progress.value >= 1f) {
            onAnimationFinished()
            if (shadow != null) {
                shadowAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            }
        }
    }

    // Resolves to the theme's actual header Typeface (genre remote-config font once loaded,
    // system fallback until then) instead of a hardcoded serif — reactive, so the glyph outlines
    // rebuild automatically if the font finishes downloading after first composition.
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val composeWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
    val composeStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
    val typefaceState =
        remember(fontFamily, composeWeight, composeStyle) {
            fontFamilyResolver.resolve(fontFamily, composeWeight, composeStyle, FontSynthesis.All)
        }
    val resolvedTypeface = (typefaceState.value as? Typeface) ?: Typeface.DEFAULT

    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = with(density) { maxWidth.toPx() }

        val paint =
            remember(fontSizePx, resolvedTypeface) {
                android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = resolvedTypeface
                    textSize = fontSizePx
                }
            }

        val (glyphs, totalLength, blockHeightPx) =
            remember(text, maxWidthPx, fontSizePx, resolvedTypeface, centered, lineSpacing) {
                buildHandwrittenGlyphs(text, paint, maxWidthPx, lineSpacing, centered)
            }

        val heightDp = with(density) { blockHeightPx.toDp() }

        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(heightDp),
        ) {
            drawHandwrittenReveal(
                glyphs,
                totalLength * progress.value,
                color,
                strokeWidthPx,
                shadow,
                shadowAlpha.value,
            )
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
 * Wraps [text] to [maxWidthPx] and extracts each character's own glyph outline (split into its
 * individual contours in reading order, since a letter with a hole like "e" or "a" is more than
 * one contour). Per-glyph — rather than per-line — grouping lets the reveal fill each letter
 * solid as soon as its own stroke finishes, instead of only ever showing an outline. Returns the
 * glyphs in reading order, their combined stroke length (used to scale the reveal progress), and
 * the wrapped block's total height in pixels.
 */
private fun buildHandwrittenGlyphs(
    text: String,
    paint: android.graphics.Paint,
    maxWidthPx: Float,
    lineSpacing: Float,
    centered: Boolean,
): Triple<List<RevealGlyph>, Float, Float> {
    val lines = wrapText(text, paint, maxWidthPx)
    if (lines.isEmpty()) return Triple(emptyList(), 0f, 0f)

    val metrics = paint.fontMetrics
    val lineHeight = (metrics.descent - metrics.ascent) * lineSpacing
    val firstBaseline = -metrics.ascent

    val glyphs = mutableListOf<RevealGlyph>()
    var total = 0f

    lines.forEachIndexed { index, line ->
        if (line.isBlank()) return@forEachIndexed

        val baselineY = firstBaseline + index * lineHeight
        var x = if (centered) (maxWidthPx - paint.measureText(line)) / 2f else 0f

        for (char in line) {
            val charStr = char.toString()
            val charWidth = paint.measureText(charStr)
            if (!char.isWhitespace()) {
                val outline = android.graphics.Path()
                paint.getTextPath(charStr, 0, 1, x, baselineY, outline)

                val contours = mutableListOf<RevealContour>()
                var glyphLength = 0f
                val contourMeasure = android.graphics.PathMeasure(outline, false)
                do {
                    val contourLength = contourMeasure.length
                    if (contourLength > 0f) {
                        val contourPath = android.graphics.Path()
                        contourMeasure.getSegment(0f, contourLength, contourPath, true)
                        contours += RevealContour(contourPath, contourLength)
                        glyphLength += contourLength
                    }
                } while (contourMeasure.nextContour())

                if (glyphLength > 0f) {
                    glyphs += RevealGlyph(outline, contours, glyphLength)
                    total += glyphLength
                }
            }
            x += charWidth
        }
    }

    val blockHeight = firstBaseline + (lines.size - 1) * lineHeight + metrics.descent
    return Triple(glyphs, total, blockHeight)
}

/**
 * Draws [glyphs] in reading order up to [budget] (in path-length units): glyphs whose full
 * stroke length is already behind the budget are painted as solid ink (filled outline); the one
 * glyph currently being written is traced stroke-by-stroke instead, so the pen is always visibly
 * "drawing" exactly one letter at a time. Glyphs not yet reached aren't drawn at all.
 *
 * When [shadow] is set, a drop shadow fades in ([shadowAlpha], animated by the caller) behind the
 * finished glyphs — only ever visible once [shadowAlpha] > 0, which the caller only ramps up
 * after the whole reveal completes, so the shadow reads as a settling flourish, not something
 * dragging along behind the pen mid-stroke.
 */
private fun DrawScope.drawHandwrittenReveal(
    glyphs: List<RevealGlyph>,
    budget: Float,
    color: Color,
    strokeWidthPx: Float,
    shadow: Shadow?,
    shadowAlpha: Float,
) {
    if (budget <= 0f) return

    if (shadow != null && shadowAlpha > 0f) {
        val nativeCanvas = drawContext.canvas.nativeCanvas
        val shadowPaint =
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.FILL
                setColor(android.graphics.Color.TRANSPARENT)
                setShadowLayer(
                    shadow.blurRadius,
                    shadow.offset.x,
                    shadow.offset.y,
                    shadow.color.copy(alpha = shadow.color.alpha * shadowAlpha).toArgb(),
                )
            }
        for (glyph in glyphs) {
            nativeCanvas.drawPath(glyph.outline, shadowPaint)
        }
    }

    var remaining = budget
    val segment = android.graphics.Path()

    for (glyph in glyphs) {
        if (remaining <= 0f) break

        if (remaining >= glyph.length) {
            drawPath(path = glyph.outline.asComposePath(), color = color, style = Fill)
            remaining -= glyph.length
            continue
        }

        var localRemaining = remaining
        for (contour in glyph.contours) {
            if (localRemaining <= 0f) break
            val take = minOf(localRemaining, contour.length)

            segment.reset()
            android.graphics.PathMeasure(contour.path, false).getSegment(0f, take, segment, true)

            drawPath(
                path = segment.asComposePath(),
                color = color,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            localRemaining -= take
        }
        remaining = 0f
    }
}
