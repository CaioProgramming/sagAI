package com.ilustris.sagai.features.act.ui.components

import android.graphics.Canvas
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import kotlin.math.min

/** Classic book drop cap: large first letter with body text wrapping beside it. */
object ChapterDropCap {
    const val LINE_COUNT = 3

    /** Space between the drop cap glyph and the body text. */
    val letterGap = 16.dp

    /**
     * Decorative header fonts often draw wider than [measureText] / layout width;
     * reserve extra horizontal room so body copy does not overlap the glyph.
     */
    const val LETTER_WIDTH_EXTRA_FRACTION = 0.2f

    fun reservedLetterWidth(
        measuredWidthPx: Float,
        gapPx: Float,
    ): Float = measuredWidthPx * (1f + LETTER_WIDTH_EXTRA_FRACTION) + gapPx

    data class Split(
        val letter: Char,
        val remainder: String,
    )

    fun split(text: String): Split? {
        val trimmed = text.trimStart()
        if (trimmed.isEmpty()) return null
        return Split(
            letter = trimmed.first().uppercaseChar(),
            remainder = trimmed.drop(1),
        )
    }

    /**
     * Splits [remainder] after [lineCount] lines laid out at [besideWidthPx] with [bodyPaint].
     */
    fun breakAfterLines(
        remainder: String,
        bodyPaint: TextPaint,
        besideWidthPx: Int,
        lineCount: Int,
        lineSpacingMultiplier: Float,
    ): Int {
        if (remainder.isEmpty() || besideWidthPx <= 0) return 0
        val layout =
            StaticLayout.Builder
                .obtain(remainder, 0, remainder.length, bodyPaint, besideWidthPx)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, lineSpacingMultiplier)
                .setMaxLines(lineCount)
                .build()
        if (layout.lineCount == 0) return 0
        val lastLine = min(lineCount, layout.lineCount) - 1
        return layout.getLineEnd(lastLine).coerceIn(0, remainder.length)
    }

    fun drawOnCanvas(
        canvas: Canvas,
        split: Split,
        startX: Float,
        startY: Float,
        contentWidth: Float,
        headerPaint: TextPaint,
        bodyPaint: TextPaint,
        gapPx: Float,
        lineSpacingMultiplier: Float,
        lineCount: Int = LINE_COUNT,
    ) {
        val bodyTextSize = bodyPaint.textSize
        val lineHeight = bodyTextSize * lineSpacingMultiplier
        val dropCapHeight = lineHeight * lineCount

        val dropCapPaint =
            TextPaint(headerPaint).apply {
                textSize = dropCapHeight * 0.92f
                isAntiAlias = true
            }

        val letter = split.letter.toString()
        val dropCapWidth =
            reservedLetterWidth(
                measuredWidthPx = dropCapPaint.measureText(letter),
                gapPx = gapPx,
            )
        val besideWidth = (contentWidth - dropCapWidth).toInt().coerceAtLeast(1)

        val breakIndex =
            breakAfterLines(
                remainder = split.remainder,
                bodyPaint = bodyPaint,
                besideWidthPx = besideWidth,
                lineCount = lineCount,
                lineSpacingMultiplier = lineSpacingMultiplier,
            )

        val besideText = split.remainder.substring(0, breakIndex)
        val belowText = split.remainder.substring(breakIndex).trimStart()

        val dropCapMetrics = dropCapPaint.fontMetrics
        val dropCapBaseline = startY - dropCapMetrics.ascent
        canvas.drawText(letter, startX, dropCapBaseline, dropCapPaint)

        var bodyY = startY
        if (besideText.isNotEmpty()) {
            val besideLayout =
                StaticLayout.Builder
                    .obtain(besideText, 0, besideText.length, bodyPaint, besideWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, lineSpacingMultiplier)
                    .build()
            canvas.save()
            canvas.translate(startX + dropCapWidth, bodyY)
            besideLayout.draw(canvas)
            canvas.restore()
            bodyY += besideLayout.height
        }

        if (belowText.isNotEmpty()) {
            val belowLayout =
                StaticLayout.Builder
                    .obtain(belowText, 0, belowText.length, bodyPaint, contentWidth.toInt())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, lineSpacingMultiplier)
                    .build()
            canvas.save()
            canvas.translate(startX, bodyY)
            belowLayout.draw(canvas)
            canvas.restore()
        }
    }
}

/** Styles the part of [text] (from [offset] in the full page) that overlaps [highlight]; color-only so layout never shifts. */
private fun highlighted(
    text: String,
    offset: Int,
    highlight: TextRange?,
    style: SpanStyle,
): AnnotatedString =
    buildAnnotatedString {
        append(text)
        if (highlight == null) return@buildAnnotatedString
        val start = (highlight.start - offset).coerceIn(0, text.length)
        val end = (highlight.end - offset).coerceIn(0, text.length)
        if (end > start) addStyle(style, start, end)
    }

/** Text that reports taps as character offsets in the full page ([offset] is where [text] starts). */
@Composable
private fun PageText(
    text: AnnotatedString,
    offset: Int,
    style: TextStyle,
    onTap: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    // The highlight recomposes this every playback tick; keep the gesture detector alive across it.
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOffset by rememberUpdatedState(offset)
    Text(
        text = text,
        style = style,
        onTextLayout = { layout = it },
        modifier =
            if (onTap == null) {
                modifier
            } else {
                modifier.pointerInput(Unit) {
                    detectTapGestures { position ->
                        layout?.let { currentOnTap?.invoke(currentOffset + it.getOffsetForPosition(position)) }
                    }
                }
            },
    )
}

@Composable
fun ChapterDropCapText(
    text: String,
    showDropCap: Boolean,
    modifier: Modifier = Modifier,
    bodyStyle: TextStyle =
        MaterialTheme.typography.bodyMedium.copy(
            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
            fontWeight = FontWeight.Normal,
        ),
    /** Range of [text] being narrated right now, e.g. the current word of the audiobook. */
    highlight: TextRange? = null,
    highlightStyle: SpanStyle =
        SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            background = MaterialTheme.colorScheme.primary.copy(alpha = .12f),
        ),
    /** Receives the tapped character offset in [text], e.g. to seek the audiobook there. */
    onTextTap: ((Int) -> Unit)? = null,
) {
    if (!showDropCap) {
        PageText(highlighted(text, 0, highlight, highlightStyle), 0, bodyStyle, onTextTap, modifier)
        return
    }

    val split = remember(text) { ChapterDropCap.split(text) }
    if (split == null) {
        PageText(highlighted(text, 0, highlight, highlightStyle), 0, bodyStyle, onTextTap, modifier)
        return
    }
    val remainderOffset = text.length - text.trimStart().length + 1

    val headerFont = MaterialTheme.typography.headlineMedium.fontFamily
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val bodyLineHeight =
        if (!bodyStyle.lineHeight.isUnspecified) {
            bodyStyle.lineHeight
        } else {
            bodyStyle.fontSize * 1.4f
        }
    val dropCapHeight = bodyLineHeight * ChapterDropCap.LINE_COUNT
    val dropCapStyle =
        bodyStyle.copy(
            fontFamily = headerFont,
            fontSize = dropCapHeight * 0.92f,
            lineHeight = dropCapHeight,
        )

    BoxWithConstraints(modifier.then(Modifier.fillMaxWidth())) {
        val containerWidth = maxWidth
        val gap = ChapterDropCap.letterGap

        val capResult =
            remember(split.letter, dropCapStyle, density) {
                textMeasurer.measure(
                    text = split.letter.toString(),
                    style = dropCapStyle,
                    constraints = Constraints(),
                )
            }

        val capWidthPx =
            ChapterDropCap.reservedLetterWidth(
                measuredWidthPx = capResult.size.width.toFloat(),
                gapPx = with(density) { gap.toPx() },
            )
        val indentStart =
            with(density) { capWidthPx.toDp() }
        val besideWidthPx =
            with(density) {
                (containerWidth - indentStart).roundToPx().coerceAtLeast(1)
            }

        val besideResult =
            remember(split.remainder, besideWidthPx, bodyStyle) {
                textMeasurer.measure(
                    text = split.remainder,
                    style = bodyStyle,
                    constraints = Constraints(maxWidth = besideWidthPx),
                )
            }

        val linesToConsume = min(ChapterDropCap.LINE_COUNT, besideResult.lineCount)
        val breakIndex =
            if (linesToConsume > 0) {
                besideResult.getLineEnd(linesToConsume - 1, visibleEnd = true)
            } else {
                0
            }

        val besideText = split.remainder.substring(0, breakIndex)
        val belowRaw = split.remainder.substring(breakIndex)
        val belowText = belowRaw.trimStart()
        val belowOffset = remainderOffset + breakIndex + (belowRaw.length - belowText.length)

        Box(Modifier.fillMaxWidth()) {
            Text(
                text = split.letter.toString(),
                style = dropCapStyle,
                modifier = Modifier.align(Alignment.TopStart),
            )
            Column(Modifier.fillMaxWidth()) {
                if (besideText.isNotEmpty()) {
                    PageText(
                        text = highlighted(besideText, remainderOffset, highlight, highlightStyle),
                        offset = remainderOffset,
                        style = bodyStyle,
                        onTap = onTextTap,
                        modifier = Modifier.padding(start = indentStart),
                    )
                }
                if (belowText.isNotEmpty()) {
                    PageText(highlighted(belowText, belowOffset, highlight, highlightStyle), belowOffset, bodyStyle, onTextTap)
                }
            }
        }
    }
}
