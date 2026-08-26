package com.ilustris.sagai.ui.genre.terminal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * An action as the next command waiting to be run — caret on the one the reader is meant to take.
 * A primary action drops the host, reading as the shell's own prompt; a secondary one keeps it, so
 * it reads as one more line typed at the same prompt rather than the thing the terminal is asking
 * for.
 *
 * Shared by every Terminal surface — the story review's transcript, the Milestone screen's own beat,
 * its introduction and its error notice — so "what a button looks like" stays one answer instead of
 * four call sites each inventing their own [TerminalCommandLine] + [terminalSelection] wiring.
 */
@Composable
fun TerminalCommandButton(
    label: String,
    onClick: () -> Unit,
    accent: Color,
    modifier: Modifier = Modifier,
    host: String = "",
    primary: Boolean = true,
    busy: Boolean = false,
) {
    TerminalCommandLine(
        host = if (primary) "" else host,
        command = label.lowercase().replace(' ', '_'),
        accent = accent,
        showCaret = primary && !busy,
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (busy) {
                        Modifier
                    } else {
                        // terminalSelection inverts the character cells under the press instead of
                        // washing a ripple over them — a console highlights, it doesn't glow.
                        Modifier.clickable(
                            interactionSource = null,
                            indication = terminalSelection(accent),
                            onClick = onClick,
                        )
                    },
                ),
    )
}
