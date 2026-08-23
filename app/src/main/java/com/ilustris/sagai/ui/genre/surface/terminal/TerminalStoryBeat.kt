package com.ilustris.sagai.ui.genre.surface.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.ui.genre.surface.StoryActionEmphasis
import com.ilustris.sagai.ui.genre.surface.StoryBeat
import com.ilustris.sagai.ui.genre.surface.StoryBody
import com.ilustris.sagai.ui.genre.surface.storyRoot
import com.ilustris.sagai.ui.genre.surface.StoryBeatAction
import com.ilustris.sagai.ui.genre.surface.StoryProgress
import com.ilustris.sagai.ui.genre.terminal.TerminalCommandLine
import com.ilustris.sagai.ui.genre.terminal.TerminalLine
import com.ilustris.sagai.ui.genre.terminal.TerminalPortraitPlate
import com.ilustris.sagai.ui.genre.terminal.TerminalProgress
import com.ilustris.sagai.ui.genre.terminal.TerminalTypewriter
import com.ilustris.sagai.ui.genre.terminal.terminalHost
import com.ilustris.sagai.ui.genre.terminal.terminalPromptLine
import com.ilustris.sagai.ui.genre.terminal.terminalSelection

/**
 * A beat as a terminal session: one continuous transcript where every attachment is the output of a
 * command, rather than a card laid on top of a console-coloured background.
 *
 * The sections print in order — each waits for the one above it to finish typing, the way a real
 * shell does — so a cover image or a cast list arrives as the result of a command the reader just
 * watched run. That sequencing is the whole point: laying the same content out all at once would be
 * a styled form, not a terminal.
 *
 * Command verbs are English because shell commands are. The review's terminal template already
 * settled this (`boot`, `log`, `query`); translating `--close` would read as neither language.
 */
@Composable
fun TerminalStoryBeat(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
    canAnimate: Boolean = true,
    embedded: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val accent = MaterialTheme.colorScheme.primary
    val normal = MaterialTheme.colorScheme.onBackground
    val host = terminalHost(beat.source.orEmpty())
    val mono = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)

    // Sections reveal one at a time, each unlocked by the one before it finishing its print-out.
    // Keyed on beat.key rather than the beat: a cover image landing late must not reset the
    // transcript to the top and re-type everything the reader has already read.
    var printed by remember(beat.key, canAnimate) { mutableIntStateOf(if (canAnimate) 0 else Int.MAX_VALUE) }
    var section = 0
    fun next() = section++

    Column(
        modifier
            .storyRoot(embedded)
            .padding(contentPadding)
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        StoryBody(embedded, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // The opening command and its output — the beat itself.
            val head = next()
            TerminalTypewriter(
                lines =
                    buildList {
                        add(
                            terminalPromptLine(
                                host = host,
                                command = beat.verb ?: beat.title.orEmpty(),
                                accent = accent,
                            ),
                        )
                        beat.eyebrow?.let {
                            add(TerminalLine(text = "> $it", style = mono.copy(color = accent), alpha = .7f))
                        }
                        // The title doubles as the command when there is no verb; printing it twice
                        // would read as the shell echoing itself.
                        if (beat.verb != null) {
                            beat.title?.let { add(TerminalLine(text = it, style = mono.copy(color = normal))) }
                        }
                        beat.body?.takeIf { it.isNotBlank() }?.let {
                            add(TerminalLine(text = it, style = mono.copy(color = normal)))
                        }
                    },
                canAnimate = canAnimate,
                caretColor = accent,
                onFinished = { if (printed == head) printed = head + 1 },
            )

            beat.figures.forEach { url ->
                val s = next()
                TerminalOutput(visible = printed >= s, host = host, command = "open cover.png", accent = accent, canAnimate = canAnimate, onFinished = { if (printed == s) printed = s + 1 }) {
                    TerminalPortraitPlate(
                        imageUrl = url,
                        accentColor = accent,
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                    )
                }
            }

            if (beat.entries.isNotEmpty()) {
                val s = next()
                TerminalOutput(visible = printed >= s, host = host, command = "wiki --list --new", accent = accent, canAnimate = canAnimate, onFinished = { if (printed == s) printed = s + 1 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        beat.entries.forEach { wiki ->
                            Text(
                                text = "  [+] ${wiki.title}",
                                style = mono.copy(color = accent, fontSize = MaterialTheme.typography.bodyMedium.fontSize),
                            )
                            Text(
                                text = "      ${wiki.content}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = normal.copy(alpha = .7f)),
                            )
                        }
                    }
                }
            }

            if (beat.cast.isNotEmpty()) {
                val s = next()
                TerminalOutput(visible = printed >= s, host = host, command = "cast --diff", accent = accent, canAnimate = canAnimate, onFinished = { if (printed == s) printed = s + 1 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        beat.cast.forEach { CastLine(it, accent, normal) }
                    }
                }
            }

            beat.aside?.let { aside ->
                val s = next()
                TerminalOutput(visible = printed >= s, host = host, command = "cat emotional.log", accent = accent, canAnimate = canAnimate, onFinished = { if (printed == s) printed = s + 1 }) {
                    Text(
                        text = "  ${aside.text}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = normal.copy(alpha = .55f)),
                    )
                }
            }
        }

        // Inline in the transcript rather than pinned over the screen: a progress bar the terminal
        // itself printed belongs in the log, and floating it above meant the layout underneath had
        // to be told to leave a hole for it.
        beat.progress?.takeIf { it.total > 1 }?.let {
            TerminalStepBar(it, accent, Modifier.padding(vertical = 8.dp))
        }

        AnimatedVisibility(!beat.gateActionsOnReveal || printed >= section) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                beat.actions.forEach { TerminalAction(it, host, accent) }
            }
        }
    }
}

/** A command line plus whatever it printed, revealed only once the transcript reaches it. */
@Composable
private fun TerminalOutput(
    visible: Boolean,
    host: String,
    command: String,
    accent: Color,
    canAnimate: Boolean,
    onFinished: () -> Unit,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(250))) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TerminalTypewriter(
                lines = listOf(terminalPromptLine(host = host, command = command, accent = accent)),
                canAnimate = canAnimate,
                caretColor = accent,
                onFinished = onFinished,
            )
            content()
        }
    }
}

@Composable
private fun CastLine(
    character: Character,
    accent: Color,
    normal: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TerminalPortraitPlate(
            imageUrl = character.image.orEmpty(),
            accentColor = accent,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = "[±] ${"${character.name} ${character.lastName.orEmpty()}".trim()}",
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = normal),
        )
    }
}

@Composable
private fun TerminalStepBar(
    progress: StoryProgress,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    TerminalProgress(
        current = progress.index,
        total = progress.total,
        color = accent,
        modifier = modifier.fillMaxWidth(),
    )
}

/** An action as the next command waiting to be run — caret on the one the reader is meant to take. */
@Composable
private fun TerminalAction(
    action: StoryBeatAction,
    host: String,
    accent: Color,
) {
    val isPrimary = action.emphasis == StoryActionEmphasis.PRIMARY
    TerminalCommandLine(
        host = if (isPrimary) "" else host,
        command = action.label.lowercase().replace(' ', '_'),
        accent = accent,
        showCaret = isPrimary && !action.busy,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (action.busy) {
                        Modifier
                    } else {
                        // terminalSelection inverts the character cells under the press instead of
                        // washing a ripple over them — a console highlights, it doesn't glow.
                        Modifier.clickable(
                            interactionSource = null,
                            indication = terminalSelection(accent),
                            onClick = action.onClick,
                        )
                    },
                ),
    )
}
