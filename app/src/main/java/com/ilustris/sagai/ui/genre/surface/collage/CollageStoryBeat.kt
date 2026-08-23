package com.ilustris.sagai.ui.genre.surface.collage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.ui.genre.collage.AssemblingPiece
import com.ilustris.sagai.ui.genre.surface.StoryActionEmphasis
import com.ilustris.sagai.ui.genre.surface.StoryBeat
import com.ilustris.sagai.ui.genre.surface.StoryBeatAction
import com.ilustris.sagai.ui.genre.surface.StoryProgress
import com.ilustris.sagai.ui.genre.collage.CharacterSticker
import com.ilustris.sagai.ui.genre.collage.PAPER_INK
import com.ilustris.sagai.ui.genre.collage.PunkScribbleOverlay
import com.ilustris.sagai.ui.genre.collage.TornPaperScrap
import com.ilustris.sagai.ui.genre.collage.TornPaperStrip
import com.ilustris.sagai.ui.genre.collage.TornPhotoScrap
import com.ilustris.sagai.ui.genre.collage.readableTextColor
import com.ilustris.sagai.ui.genre.collage.rememberTearReveal
import com.ilustris.sagai.ui.theme.LocalSagaGenre

/** The title rips across first; the body follows once the reader has had a beat with it. */
private const val TITLE_TEAR_DELAY_MS = 250L
private const val BODY_TEAR_DELAY_MS = 1500L

/**
 * A beat as something torn out and glued down. Each piece of copy gets its own edge-to-edge strip,
 * ripping across the screen in turn, and every attachment is a scrap pasted onto the same page —
 * photos taped in, lore on sticky notes, the cast as stickers.
 *
 * Deliberately separate strips rather than one growing card: a single container that merely got
 * taller read as a resizing box, while independent rips each with their own tear geometry are what
 * sells "breaking the layout".
 */
@Composable
fun CollageStoryBeat(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
    canAnimate: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val genre = LocalSagaGenre.current ?: Genre.PUNK_ROCK
    val accent = genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary
    val titleReveal = rememberTearReveal(canAnimate, TITLE_TEAR_DELAY_MS)
    val bodyReveal = rememberTearReveal(canAnimate, BODY_TEAR_DELAY_MS)

    var revealed by remember(beat.key) { mutableStateOf(!canAnimate) }
    LaunchedEffect(beat.key) { revealed = true }

    Box(modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(contentPadding)
                .padding(vertical = 20.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                beat.eyebrow?.let {
                    AssemblingPiece(rotation = 2.4f, delayMs = 80L, canAnimate = canAnimate, seed = 59, scaleFrom = 1f) {
                        TornPaperScrap(seed = 59, paperColor = accent) {
                            Text(
                                text = it.uppercase(),
                                color = accent.readableTextColor(),
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }

                Column(
                    Modifier
                        // Slightly wider than the screen so the strips' small rotation never
                        // exposes a gap at either edge — they have to run clean off the page.
                        .fillMaxWidth(1.14f)
                        // The body strip joins the layout only when its turn comes; without this
                        // the column's height jumps in one frame instead of opening up.
                        .animateContentSize(tween(700, easing = FastOutSlowInEasing)),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    beat.title?.let {
                        AssemblingPiece(
                            rotation = -1.6f,
                            delayMs = TITLE_TEAR_DELAY_MS,
                            canAnimate = canAnimate,
                            seed = 61,
                            entranceOffset = Offset(0f, 26f),
                            scaleFrom = 1f,
                        ) {
                            TornPaperStrip(
                                seed = 61,
                                modifier = Modifier.fillMaxWidth(),
                                revealProgress = titleReveal,
                                contentPadding = PaddingValues(horizontal = 30.dp, vertical = 38.dp),
                            ) {
                                Text(
                                    text = it,
                                    color = PAPER_INK,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }

                    beat.body?.takeIf { it.isNotBlank() }?.let {
                        AssemblingPiece(
                            rotation = 1.3f,
                            delayMs = BODY_TEAR_DELAY_MS,
                            canAnimate = canAnimate,
                            seed = 62,
                            entranceOffset = Offset(0f, 26f),
                            scaleFrom = 1f,
                        ) {
                            TornPaperStrip(
                                seed = 62,
                                modifier = Modifier.fillMaxWidth(),
                                revealProgress = bodyReveal,
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 36.dp),
                            ) {
                                Text(
                                    text = it,
                                    color = PAPER_INK,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }

                beat.figures.forEachIndexed { index, url ->
                    AssemblingPiece(
                        rotation = if (index % 2 == 0) -3.5f else 2.8f,
                        delayMs = 1900L + index * 220L,
                        canAnimate = canAnimate,
                        seed = 70 + index,
                        scaleFrom = 1f,
                    ) {
                        TornPhotoScrap(
                            imageUrl = url,
                            seed = 70 + index,
                            modifier = Modifier.width(240.dp).height(170.dp),
                        )
                    }
                }

                if (beat.entries.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        beat.entries.forEachIndexed { index, wiki ->
                            AssemblingPiece(
                                rotation = if (index % 2 == 0) 3f else -2.5f,
                                delayMs = 2100L + index * 200L,
                                canAnimate = canAnimate,
                                seed = 80 + index,
                                scaleFrom = 1f,
                            ) {
                                TornPaperScrap(seed = 80 + index, modifier = Modifier.width(190.dp)) {
                                    Column {
                                        Text(
                                            text = wiki.title.uppercase(),
                                            color = PAPER_INK,
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                        Text(
                                            text = wiki.content,
                                            color = PAPER_INK.copy(alpha = .8f),
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(top = 4.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (beat.cast.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        beat.cast.forEachIndexed { index, character ->
                            AssemblingPiece(
                                rotation = if (index % 2 == 0) -4f else 3.2f,
                                delayMs = 2300L + index * 180L,
                                canAnimate = canAnimate,
                                seed = 90 + index,
                                scaleFrom = 1f,
                            ) {
                                CharacterSticker(
                                    character = character,
                                    accentColor = accent,
                                    caption = "${character.name} ${character.lastName.orEmpty()}".trim(),
                                    featured = index == 0,
                                )
                            }
                        }
                    }
                }

                beat.aside?.let { aside ->
                    AssemblingPiece(rotation = 4f, delayMs = 2600L, canAnimate = canAnimate, seed = 99, scaleFrom = 1f) {
                        TornPaperScrap(seed = 99, paperColor = accent, modifier = Modifier.width(250.dp)) {
                            Column {
                                Text(
                                    text = aside.label.uppercase(),
                                    color = accent.readableTextColor().copy(alpha = .7f),
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                Text(
                                    text = aside.text,
                                    color = accent.readableTextColor(),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }

            beat.progress?.takeIf { it.total > 1 }?.let { CollageTabs(it, accent) }

            AnimatedVisibility(!beat.gateActionsOnReveal || revealed) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    beat.actions.forEach { action ->
                        CollageTab(action, accent, canAnimate, Modifier.weight(1f))
                    }
                }
            }
        }

        // Doodled on the finished poster, not tucked behind it.
        PunkScribbleOverlay(Modifier.fillMaxSize())
    }
}

/** Step count as inked tabs torn along the top of the stack. */
@Composable
private fun CollageTabs(
    progress: StoryProgress,
    accent: Color,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        repeat(progress.total) { i ->
            TornPaperScrap(
                seed = 40 + i,
                paperColor = if (i < progress.index) accent else PAPER_INK.copy(alpha = .25f),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                modifier = Modifier.width(26.dp),
            ) {
                Spacer(Modifier.size(1.dp))
            }
        }
    }
}

/** An action as a torn tab you slap down — no Material button anywhere in this language. */
@Composable
private fun CollageTab(
    action: StoryBeatAction,
    accent: Color,
    canAnimate: Boolean,
    modifier: Modifier = Modifier,
) {
    val isPrimary = action.emphasis == StoryActionEmphasis.PRIMARY
    val paper = if (isPrimary) accent else PAPER_INK.copy(alpha = .12f)
    val ink = if (isPrimary) accent.readableTextColor() else PAPER_INK

    AssemblingPiece(
        modifier = modifier,
        rotation = if (isPrimary) -1.2f else 1.6f,
        delayMs = 200L,
        canAnimate = canAnimate,
        seed = action.id.hashCode(),
        scaleFrom = 1f,
    ) {
        Box(
            Modifier.then(if (action.busy) Modifier else Modifier.clickable(onClick = action.onClick)),
        ) {
            TornPaperScrap(
                seed = action.id.hashCode(),
                paperColor = paper,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (action.busy) {
                        CircularProgressIndicator(Modifier.size(13.dp), strokeWidth = 2.dp, color = ink)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = action.label.uppercase(),
                        color = ink,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
