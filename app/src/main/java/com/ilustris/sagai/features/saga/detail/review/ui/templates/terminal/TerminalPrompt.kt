package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.delay

/** A real terminal's cursor blinks around twice a second, on and off in equal halves. */
private const val CARET_BLINK_MS = 530L

/** The `user@host` portion of every terminal prompt — the saga's own title, slugified. */
fun SagaContent.terminalHost(): String {
    val slug =
        data.title
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
    return "admin@${slug.ifBlank { "sagai" }}"
}

/**
 * The blink phase, hoisted so it can outlive the thing being drawn.
 *
 * A cursor that moves between lines must keep one continuous rhythm. Owning the phase inside the
 * caret meant every move disposed it and built a fresh one, restarting the blink — short lines
 * were destroyed before completing a single cycle, so the cursor flashed once and vanished. Held
 * above whatever moves, the rhythm survives the move.
 */
@Composable
fun rememberCaretBlink(periodMs: Long = CARET_BLINK_MS): Boolean {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(periodMs) {
        while (true) {
            delay(periodMs)
            visible = !visible
        }
    }
    return visible
}

/**
 * A block cursor.
 *
 * Deliberately a hard on/off rather than a fade: a terminal cursor is a character cell being
 * inverted by the driver, not something being animated, and easing it turns the one element that
 * says "this is a live shell" into decoration.
 */
@Composable
fun TerminalCaret(
    color: Color,
    modifier: Modifier = Modifier,
    visible: Boolean = rememberCaretBlink(),
    width: androidx.compose.ui.unit.Dp = 10.dp,
    height: androidx.compose.ui.unit.Dp = 18.dp,
) {
    Box(
        modifier
            .width(width)
            .height(height)
            .alpha(if (visible) 1f else 0f)
            .background(color),
    )
}

/**
 * The prompt as a printable line, so the command types itself alongside its output instead of
 * being already-there text the output appears beneath.
 */
@Composable
fun terminalPromptLine(
    host: String,
    command: String,
    accent: Color,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) = TerminalLine(
    text = "$host:~$ $command",
    style =
        style
            .copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accent,
            ).neonGlow(accent),
)

/**
 * A command as the shell shows it: the prompt, what was typed, and the cursor still sitting there
 * waiting. Every terminal page opens on one of these, so the caret lives here rather than being
 * re-added per page.
 */
@Composable
fun TerminalCommandLine(
    host: String,
    command: String,
    accent: Color,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    showCaret: Boolean = true,
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$host:~$ $command",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = accent,
            style = style.neonGlow(accent),
        )

        if (showCaret) {
            TerminalCaret(
                color = accent,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}
