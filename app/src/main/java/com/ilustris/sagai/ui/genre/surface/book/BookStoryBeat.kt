package com.ilustris.sagai.ui.genre.surface.book

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.genre.PhysicalButton
import com.ilustris.sagai.ui.genre.surface.GenreStoryAmbientOverlay
import com.ilustris.sagai.ui.genre.surface.StoryActionEmphasis
import com.ilustris.sagai.ui.genre.surface.StoryBeat
import com.ilustris.sagai.ui.genre.surface.StoryBeatAction
import com.ilustris.sagai.ui.genre.surface.StoryBeatTone
import com.ilustris.sagai.ui.genre.surface.StoryBody
import com.ilustris.sagai.ui.genre.surface.StoryProgress
import com.ilustris.sagai.ui.genre.surface.storyRoot
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.components.HandwrittenText
import kotlin.time.Duration.Companion.milliseconds

/** Body typing speed, clamped so a one-line beat still takes a readable moment. */
private const val MS_PER_CHAR = 16
private const val MIN_BODY_MS = 800
private const val MAX_BODY_MS = 4000

/**
 * A beat as a page of a printed book: a titled paragraph under a rule, with its attachments set as
 * plates, an appendix and a cast list — the furniture a book already has, rather than cards
 * borrowed from a different medium.
 *
 * [StoryBeatTone.EPIGRAPH] switches to a centred italic quote, the treatment a stage's hook text
 * has always had here.
 */
@Composable
fun BookStoryBeat(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
    canAnimate: Boolean = true,
    embedded: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val genre = LocalSagaGenre.current
    val accent = genre?.compiledColorPalette()?.firstOrNull() ?: MaterialTheme.colorScheme.primary
    val ink = LocalContentColor.current

    Box(modifier) {
        if (beat.tone == StoryBeatTone.EPIGRAPH) {
            BookEpigraph(beat, Modifier, canAnimate, contentPadding)
        } else {
            BookPage(beat, genre, accent, ink, canAnimate, embedded, contentPadding)
        }

        // Genre-exclusive weather over the finished page — drawn last, the same "already taped
        // off"/"ink already bloomed" layering BookMilestoneSkin used to give these three genres.
        GenreStoryAmbientOverlay(Modifier.fillMaxSize(), genre)
    }
}

@Composable
private fun BookPage(
    beat: StoryBeat,
    genre: Genre?,
    accent: Color,
    ink: Color,
    canAnimate: Boolean,
    embedded: Boolean,
    contentPadding: PaddingValues,
) {
    Column(
        Modifier
            .storyRoot(embedded)
            .padding(contentPadding)
            .padding(horizontal = 32.dp, vertical = 24.dp),
    ) {
        var revealed by remember(beat.key) { mutableStateOf(!canAnimate) }
        LaunchedEffect(beat.key) { if (!canAnimate) revealed = true }

        StoryBody(embedded, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            beat.eyebrow?.let {
                Text(
                    text = it.uppercase(),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp,
                            color = ink.copy(alpha = .55f),
                        ),
                )
            }

            beat.title?.let {
                HandwrittenText(
                    text = it.uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    isBold = true,
                    isItalic = false,
                    isAnimated = canAnimate,
                )
                HorizontalDivider(color = accent.copy(alpha = 0.4f))
            }

            beat.body?.takeIf { it.isNotBlank() }?.let {
                SimpleTypewriterText(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge.copy(color = ink),
                    duration = (it.length * MS_PER_CHAR).coerceIn(MIN_BODY_MS, MAX_BODY_MS).milliseconds,
                    isAnimated = canAnimate,
                    onAnimationFinished = { revealed = true },
                )
            } ?: LaunchedEffect(beat.key) { revealed = true }

            beat.figures.forEachIndexed { index, url ->
                BookPlate(url = url, number = index + 1, accent = accent, ink = ink)
            }

            if (beat.entries.isNotEmpty()) {
                BookAppendix(beat.entries, beat.entriesLabel, accent, ink)
            }

            if (beat.cast.isNotEmpty()) {
                BookDramatisPersonae(beat.cast, beat.castLabel, accent, ink)
            }

            beat.aside?.let { BookMarginNote(it.label, it.text, ink) }
        }

        beat.progress?.takeIf { it.total > 1 }?.let { BookFolio(it, ink) }

        AnimatedVisibility(!beat.gateActionsOnReveal || revealed) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                beat.actions.forEach { action ->
                    if (genre == Genre.HORROR) {
                        // Horror is the one Book genre that's tactile rather than printed — the
                        // page is already taped off, so what you press is a physical object too,
                        // not one more italic link on the paper.
                        PhysicalButton(
                            text = action.label,
                            onClick = action.onClick,
                            busy = action.busy,
                            accent = accent,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        BookLinkAction(action, accent, ink)
                    }
                }
            }
        }
    }
}

/** A stage's hook: a centred italic quote with nothing else on the page. */
@Composable
private fun BookEpigraph(
    beat: StoryBeat,
    modifier: Modifier,
    canAnimate: Boolean,
    contentPadding: PaddingValues,
) {
    Box(
        modifier.fillMaxWidth().padding(contentPadding).padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            beat.title?.let {
                HandwrittenText(
                    text = "“$it”",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    centered = true,
                    isAnimated = canAnimate,
                )
            }
            beat.body?.let {
                SimpleTypewriterText(
                    text = it,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    isAnimated = canAnimate,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

/** An illustration set in a thin double rule with a numbered caption, the way a plate is bound in. */
@Composable
private fun BookPlate(
    url: String,
    number: Int,
    accent: Color,
    ink: Color,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = url.isNotBlank(),
        enter = fadeIn(tween(600)),
        modifier = modifier,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, accent.copy(alpha = .5f))
                    .padding(4.dp)
                    .border(2.dp, accent.copy(alpha = .3f))
                    .padding(6.dp),
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(170.dp),
                )
            }
            Text(
                text = "— $PLATE_LABEL $number —",
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontStyle = FontStyle.Italic,
                        color = ink.copy(alpha = .6f),
                    ),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

/**
 * Roman-numeral-free on purpose: the label is the one piece of English left in this surface, and a
 * numeral would only make it harder to read at a glance. It reads as a plate caption either way.
 */
private const val PLATE_LABEL = "Plate"

/** New lore as a glossary: term in italic serif, definition indented under it. */
@Composable
private fun BookAppendix(
    entries: List<Wiki>,
    label: String?,
    accent: Color,
    ink: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        label?.let {
            Text(
                text = it.uppercase(),
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        color = ink.copy(alpha = .5f),
                    ),
            )
            HorizontalDivider(color = accent.copy(alpha = .25f))
        }
        entries.forEach { wiki ->
            Column {
                Text(
                    text = wiki.title,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            color = ink,
                        ),
                )
                Text(
                    text = wiki.content,
                    style = MaterialTheme.typography.bodySmall.copy(color = ink.copy(alpha = .75f)),
                    modifier = Modifier.padding(start = 14.dp, top = 2.dp),
                )
            }
        }
    }
}

/** The cast list a play prints before act one — portrait in an ink ring, name in small caps. */
@Composable
private fun BookDramatisPersonae(
    cast: List<Character>,
    label: String?,
    accent: Color,
    ink: Color,
) {
    val genre = LocalSagaGenre.current ?: Genre.FANTASY
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        label?.let {
            Text(
                text = it.uppercase(),
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        color = ink.copy(alpha = .5f),
                    ),
            )
            HorizontalDivider(color = accent.copy(alpha = .25f))
        }
        cast.forEach { character ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CharacterAvatar(
                    character = character,
                    genre = genre,
                    borderColor = accent.copy(alpha = .7f),
                    modifier = Modifier.size(44.dp),
                )
                Text(
                    text = "${character.name} ${character.lastName.orEmpty()}".trim().uppercase(),
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 1.sp,
                            color = ink,
                        ),
                )
            }
        }
    }
}

/**
 * The aside, written in the margin rather than boxed in the text — it is a second hand commenting
 * on the page, and a bordered card would have given it the same weight as the story itself.
 */
@Composable
private fun BookMarginNote(
    label: String,
    text: String,
    ink: Color,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .rotate(-0.6f)
            .padding(start = 18.dp, top = 4.dp),
    ) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontStyle = FontStyle.Italic,
                    color = ink.copy(alpha = .45f),
                ),
        )
        Text(
            text = text,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    color = ink.copy(alpha = .6f),
                ),
        )
    }
}

/** Page count set in the footer, where a book puts it. */
@Composable
private fun BookFolio(
    progress: StoryProgress,
    ink: Color,
) {
    Text(
        text = "${progress.index} / ${progress.total}",
        style =
            MaterialTheme.typography.labelSmall.copy(
                fontStyle = FontStyle.Italic,
                color = ink.copy(alpha = .45f),
            ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    )
}

/** Actions set as printed links rather than filled buttons — a book has no chrome to press. */
@Composable
private fun BookLinkAction(
    action: StoryBeatAction,
    accent: Color,
    ink: Color,
) {
    val isPrimary = action.emphasis == StoryActionEmphasis.PRIMARY
    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .then(
                if (action.busy) {
                    Modifier
                } else {
                    // No ripple: a printed page has no chrome to light up, and a Material
                    // highlight under a serif link is exactly the mismatch this surface exists
                    // to avoid.
                    Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        onClick = action.onClick,
                    )
                },
            ).padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (action.busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 1.5.dp,
                color = accent,
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = if (isPrimary) "${action.label} →" else action.label,
            style =
                MaterialTheme.typography.titleSmall.copy(
                    fontStyle = FontStyle.Italic,
                    fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isPrimary) accent else ink.copy(alpha = .7f),
                ),
        )
    }
}
