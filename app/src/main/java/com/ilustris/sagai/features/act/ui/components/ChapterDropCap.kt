package com.ilustris.sagai.features.act.ui.components

import android.graphics.Canvas
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
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
) {
    if (!showDropCap) {
        Text(text = text, style = bodyStyle, modifier = modifier)
        return
    }

    val split = remember(text) { ChapterDropCap.split(text) }
    if (split == null) {
        Text(text = text, style = bodyStyle, modifier = modifier)
        return
    }

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
        val belowText = split.remainder.substring(breakIndex).trimStart()

        Box(Modifier.fillMaxWidth()) {
            Text(
                text = split.letter.toString(),
                style = dropCapStyle,
                modifier = Modifier.align(Alignment.TopStart),
            )
            Column(Modifier.fillMaxWidth()) {
                if (besideText.isNotEmpty()) {
                    Text(
                        text = besideText,
                        style = bodyStyle,
                        modifier = Modifier.padding(start = indentStart),
                    )
                }
                if (belowText.isNotEmpty()) {
                    Text(text = belowText, style = bodyStyle)
                }
            }
        }
    }
}
