package com.ilustris.sagai.ui.genre.recap.terminal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.genre.recap.RecapCard
import com.ilustris.sagai.ui.genre.terminal.TerminalBackground
import com.ilustris.sagai.ui.genre.terminal.TerminalCommandLine
import com.ilustris.sagai.ui.genre.terminal.TerminalProgress
import com.ilustris.sagai.ui.genre.terminal.plateFrame
import com.ilustris.sagai.ui.genre.terminal.terminalHost

/**
 * The recap as a command that already ran: `saga --recap` at the top, every stat listed as its own
 * output line, and the call to action left sitting at the prompt with the caret still blinking on
 * it — the next thing you'd type.
 *
 * Shows the whole stat list at once rather than rotating through it. A terminal prints its output
 * and leaves it on screen; a line that swapped itself out every two seconds would read as a
 * ticker, which is a different machine entirely.
 *
 * Deliberately not typed in with [com.ilustris.sagai.ui.genre.terminal.TerminalTypewriter]: this
 * card lives inside a scrolling list, and re-running its print-out every time it scrolled back into
 * view would turn a quiet summary into a distraction. The blinking caret carries the aliveness on
 * its own.
 */
@Composable
fun TerminalRecapCard(
    card: RecapCard,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    val normal = MaterialTheme.colorScheme.onBackground
    val mono = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
    val host = terminalHost(card.title)

    Box(modifier.plateFrame(accent)) {
        TerminalBackground(Modifier.matchParentSize())

        Column(
            Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TerminalCommandLine(
                host = host,
                command = "saga --recap",
                accent = accent,
                style = mono,
                showCaret = false,
                modifier = Modifier.fillMaxWidth(),
            )

            if (card.isReady) {
                card.stats.forEach { stat ->
                    Text(
                        text = "  [+] ${stat.value} ${stat.label}",
                        style = mono.copy(color = normal.copy(alpha = .8f)),
                    )
                }
            } else {
                card.progress?.let { progress ->
                    Text(
                        text = "  ${progress.message}",
                        style = mono.copy(color = normal.copy(alpha = .7f)),
                    )
                    TerminalProgress(
                        current = progress.completed,
                        total = progress.total,
                        color = accent,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    )
                }
            }

            if (card.isReady) {
                TerminalCommandLine(
                    host = "",
                    command = card.callToAction.lowercase().replace(' ', '_'),
                    accent = accent,
                    style = mono,
                    showCaret = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                )
            }
        }
    }
}
