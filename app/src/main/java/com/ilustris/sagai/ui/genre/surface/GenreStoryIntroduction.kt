package com.ilustris.sagai.ui.genre.surface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.genre.GenreSurfaceStyle
import com.ilustris.sagai.ui.genre.PhysicalButton
import com.ilustris.sagai.ui.genre.comic.ComicCaptionBox
import com.ilustris.sagai.ui.genre.comic.ComicTag
import com.ilustris.sagai.ui.genre.surfaceStyle
import com.ilustris.sagai.ui.genre.terminal.TerminalCommandButton
import com.ilustris.sagai.ui.genre.terminal.TerminalLine
import com.ilustris.sagai.ui.genre.terminal.TerminalTypewriter
import com.ilustris.sagai.ui.genre.terminal.terminalHost
import com.ilustris.sagai.ui.genre.terminal.terminalPromptLine
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.themePainter
import com.ilustris.sagai.ui.theme.themeStylizedText
import com.ilustris.sagai.ui.theme.themeVfx

/**
 * The cold open of a chapter or act — the one beat that stays the same shape no matter which genre
 * is telling it.
 *
 * Every other beat picks its own layout entirely: a shell transcript, a chat thread, a torn page.
 * An introduction is a single held breath before the story resumes, and stretching that beat across
 * five unrelated layouts would make the one moment every saga shares feel the least consistent of
 * all of them. So it stays one centred composition — only the ground under it, the ambient weather
 * over it, and the lettering of its title change hands between genres.
 *
 * Comic and Terminal are the two exceptions to "the lettering changes hands and nothing else". A
 * comic page has no icon or word-art to spare, so it drops straight to the genre's own idiom — a
 * plain subtitle and the body set in a [ComicCaptionBox], the same narration box a comic panel uses
 * for scene-setting text. A terminal has no icon either — it has a prompt — so the whole beat prints
 * as one command instead of standing next to one.
 */
@Composable
fun GenreStoryIntroduction(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
    genre: Genre? = LocalSagaGenre.current,
    canAnimate: Boolean = true,
) {
    val style = genre?.surfaceStyle() ?: GenreSurfaceStyle.DEFAULT
    val isComic = style == GenreSurfaceStyle.COMIC
    val isTerminal = style == GenreSurfaceStyle.TERMINAL

    // Comic has no reveal animation to wait for, so its beat counts as already-revealed the
    // instant it lands. Terminal's own TerminalTypewriter drives revealed itself, through onFinished.
    var revealed by remember(beat.key) { mutableStateOf(!canAnimate || isComic) }
    LaunchedEffect(beat.key) { if (!canAnimate || isComic) revealed = true }

    Box(modifier.fillMaxSize()) {
        GenreStoryBackground(Modifier.fillMaxSize(), genre)

        Column(
            Modifier
                .storyRoot(embedded = false)
                .padding(horizontal = 32.dp, vertical = 24.dp),
        ) {
            Column(
                Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isComic) {
                    // Two hard-edged boxes, not one: the chapter number reads as its own stamped
                    // tag rather than a caption sharing a box with the scene-setting text below it.
                    beat.eyebrow?.let {
                        ComicTag(text = it)
                    }

                    beat.title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                    beat.body?.takeIf { it.isNotBlank() }?.let {
                        ComicCaptionBox(
                            text = it,
                            align = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                } else if (isTerminal) {
                    val accent = MaterialTheme.colorScheme.primary
                    val normal = MaterialTheme.colorScheme.onBackground
                    val mono = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                    val host = terminalHost(beat.source.orEmpty())

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
                                beat.title?.let { add(TerminalLine(text = it, style = mono.copy(color = normal))) }
                                beat.body?.takeIf { it.isNotBlank() }?.let {
                                    add(TerminalLine(text = it, style = mono.copy(color = normal.copy(alpha = .85f))))
                                }
                            },
                        canAnimate = canAnimate,
                        caretColor = accent,
                        onFinished = { revealed = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Icon(
                        painter = themePainter(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(50.dp).themeVfx(true),
                    )

                    beat.eyebrow?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 24.dp).alpha(.6f),
                        )
                    }

                    beat.title?.let {
                        themeStylizedText(text = it, modifier = Modifier.padding(top = 8.dp))
                    }

                    beat.body?.takeIf { it.isNotBlank() }?.let {
                        SimpleTypewriterText(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                            isAnimated = canAnimate,
                            modifier = Modifier.padding(top = 16.dp),
                            onAnimationFinished = { revealed = true },
                        )
                    } ?: LaunchedEffect(beat.key) { revealed = true }
                }
            }

            val usesPhysicalButton = genre == Genre.HEROES || genre == Genre.HORROR

            AnimatedVisibility(!beat.gateActionsOnReveal || revealed) {
                Column {
                    beat.actions.forEach { action ->
                        when {
                            // Both genres are tactile rather than printed — ink and panel borders,
                            // a scene taped off — so the button they press is a physical object
                            // too, not a flat Material surface.
                            usesPhysicalButton ->
                                PhysicalButton(
                                    text = action.label,
                                    onClick = action.onClick,
                                    busy = action.busy,
                                    accent = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth(),
                                )

                            isTerminal ->
                                TerminalCommandButton(
                                    label = action.label,
                                    onClick = action.onClick,
                                    accent = MaterialTheme.colorScheme.primary,
                                    busy = action.busy,
                                    modifier = Modifier.fillMaxWidth(),
                                )

                            else ->
                                Button(
                                    onClick = action.onClick,
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(action.label)
                                }
                        }
                    }
                }
            }
        }

        GenreStoryAmbientOverlay(Modifier.fillMaxSize(), genre)
    }
}
