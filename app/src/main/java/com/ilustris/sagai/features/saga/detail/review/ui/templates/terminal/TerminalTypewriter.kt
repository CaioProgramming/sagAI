package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import kotlin.time.Duration.Companion.milliseconds

/** A shell prints at a steady rate, so a long line takes longer than a short one. */
private const val DEFAULT_CHARS_PER_SECOND = 45f

/** Even a two-character line should register as having been typed. */
private const val MIN_LINE_MS = 140L

/** One line of terminal output, with the styling it should be printed in. */
data class TerminalLine(
    val text: String,
    val style: TextStyle,
    val alpha: Float = 1f,
)

/**
 * Prints a run of lines one after another, then hands control back through [onFinished].
 *
 * The sequencing is the whole point. Every page here was doing it by hand — a `titleTyped` flag
 * gating the next block — or not doing it at all, which left three lines racing each other onto
 * the screen at once. Neither reads as a machine printing; a terminal emits one line, finishes it,
 * and only then starts the next.
 *
 * Timing is expressed in characters per second rather than as a duration per line, which is the
 * other half of that. [SimpleTypewriterText] takes a duration, so a long line and a short one
 * would each take the same time — meaning the long one races and the short one crawls. Deriving
 * the duration from the length keeps the rate constant, which is what makes it read as output
 * rather than as animation.
 *
 * [onFinished] is what lets a page hold its real content back until the text has had its say —
 * a counter, a shape, a plate of images appearing once the print-out settles.
 */
@Composable
fun TerminalTypewriter(
    lines: List<TerminalLine>,
    modifier: Modifier = Modifier,
    canAnimate: Boolean = true,
    charsPerSecond: Float = DEFAULT_CHARS_PER_SECOND,
    caretColor: Color? = null,
    spacing: Dp = 10.dp,
    onFinished: () -> Unit = {},
) {
    // Keyed on the content so a page whose text streams in restarts its print-out rather than
    // resuming into lines that no longer exist.
    var typedCount by remember(lines, canAnimate) {
        mutableIntStateOf(if (canAnimate) 0 else lines.size)
    }

    LaunchedEffect(typedCount, lines.size) {
        if (typedCount >= lines.size) onFinished()
    }

    // One rhythm for the whole run, owned above the lines so moving the cursor down doesn't
    // restart it — and so it survives the printer finishing, where there is no active line left
    // for it to belong to.
    val caretVisible = rememberCaretBlink()
    val caretLine = typedCount.coerceAtMost(lines.lastIndex)

    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        lines.forEachIndexed { index, line ->
            // Lines the printer has not reached yet simply do not exist on screen.
            if (index > typedCount) return@forEachIndexed

            key(index) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Always the same composable, never a swap between an animating and a static
                    // one: flipping `isAnimated` is what settles a finished line, and keeping the
                    // node identical across that flip avoids rebuilding its state underneath it.
                    SimpleTypewriterText(
                        text = line.text,
                        style = line.style,
                        modifier = Modifier.alpha(line.alpha),
                        isAnimated = canAnimate && index == typedCount,
                        duration = line.text.typingDuration(charsPerSecond),
                        easing = LinearEasing,
                        onAnimationFinished = {
                            // Guarded: a settled line's animation can still report finishing, and
                            // without this the printer would skip ahead a line for free.
                            if (index == typedCount) typedCount++
                        },
                    )

                    if (caretColor != null && index == caretLine) {
                        TerminalCaret(color = caretColor, visible = caretVisible)
                    }
                }
            }
        }
    }
}

private fun String.typingDuration(charsPerSecond: Float) =
    ((length / charsPerSecond) * 1000f)
        .toLong()
        .coerceAtLeast(MIN_LINE_MS)
        .milliseconds
