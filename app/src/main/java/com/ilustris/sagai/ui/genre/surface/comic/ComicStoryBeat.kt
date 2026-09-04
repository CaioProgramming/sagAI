package com.ilustris.sagai.ui.genre.surface.comic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.ui.genre.PhysicalButton
import com.ilustris.sagai.ui.genre.comic.COMIC_INK
import com.ilustris.sagai.ui.genre.comic.COMIC_PAPER
import com.ilustris.sagai.ui.genre.comic.ComicCaptionBox
import com.ilustris.sagai.ui.genre.comic.ComicFadeIn
import com.ilustris.sagai.ui.genre.comic.ComicPanel
import com.ilustris.sagai.ui.genre.comic.ComicSpeechBalloon
import com.ilustris.sagai.ui.genre.comic.ComicTag
import com.ilustris.sagai.ui.genre.comic.SlantShape
import com.ilustris.sagai.ui.genre.comic.comicNarrationBalloons
import com.ilustris.sagai.ui.genre.surface.StoryActionEmphasis
import com.ilustris.sagai.ui.genre.surface.StoryBeat
import com.ilustris.sagai.ui.genre.surface.StoryBeatAction
import com.ilustris.sagai.ui.genre.surface.StoryProgress
import com.ilustris.sagai.ui.genre.surface.storyRoot

/**
 * The most body text the loose, unscrolled layout can hold before its lowest balloon starts running
 * off the screen. Past this a beat goes to the column instead.
 */
private const val LOOSE_BODY_MAX_CHARS = 280

/** Vertical room the floating action row needs, kept clear of the loose layout's balloons. */
private val ACTION_ROW_RESERVE = 96.dp

/**
 * A beat as one comic page.
 *
 * Two layouts, chosen by what the beat actually carries — not a hedge, an honest split. A beat that
 * is only words is laid out *loose*: caption boxes scattered by bias alignment, overhanging where a
 * frame border would be, exactly as the story review's board hangs them. That reads as a comic. But
 * bias alignment inside a fixed-height box cannot scroll, so the moment a beat brings a cover, two
 * pieces of lore and three characters, the same treatment would stack them on top of each other and
 * run them off the screen. Those beats get a scrolling column of framed panels instead, revealed in
 * staggered order — a page you read down, which is also what a comic does.
 */
@Composable
fun ComicStoryBeat(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
    canAnimate: Boolean = true,
    embedded: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val hasAttachments = beat.figures.isNotEmpty() || beat.entries.isNotEmpty() || beat.cast.isNotEmpty()

    // The loose layout has no scroll and places its balloons at absolute vertical biases, so it can
    // only ever hold what fits on one screen. Deciding that on attachments alone was wrong twice
    // over: a long body pushed its lowest balloon (bias ~0.78) off the bottom with nothing to
    // scroll to, and an aside — which is where the milestone's emotional review lands — was drawn
    // at BottomStart, directly underneath the action row sitting at BottomCenter. Anything with an
    // aside or a body past this length belongs in the column, which stacks and scrolls.
    val fitsLoose =
        !hasAttachments &&
            beat.aside == null &&
            beat.body.orEmpty().length <= LOOSE_BODY_MAX_CHARS

    var revealed by remember(beat.key) { mutableStateOf(!canAnimate) }
    LaunchedEffect(beat.key) { revealed = true }

    Box(
        modifier
            .background(MaterialTheme.colorScheme.background)
            .storyRoot(embedded)
            .padding(contentPadding),
    ) {
        if (!fitsLoose) {
            // Actions ride inside this column's own scroll, not pinned over it — a beat with a
            // cover, two wikis and a cast is tall enough that a floating action row either
            // overlapped the last line of text or had nowhere honest to sit.
            ComicPageColumn(beat, Modifier.fillMaxWidth(), embedded, revealed)
        } else {
            ComicLooseBeat(
                beat = beat,
                modifier = if (embedded) Modifier.fillMaxWidth().height(260.dp) else Modifier.fillMaxSize(),
                // The action row floats over this layout, so the balloons have to be told to stay
                // out from under it.
                bottomReserve = if (beat.actions.isEmpty()) 0.dp else ACTION_ROW_RESERVE,
            )

            AnimatedVisibility(
                visible = !beat.gateActionsOnReveal || revealed,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    beat.actions.forEach { ComicActionButton(it) }
                }
            }
        }

        beat.progress?.takeIf { it.total > 1 }?.let {
            ComicIssueNumber(it, Modifier.align(Alignment.TopEnd).padding(16.dp))
        }
    }
}

/**
 * Words only: the balloons hang off nothing, which is the point. Same specs the review's board
 * consumes, placed here with plain alignment instead of a measure policy.
 */
@Composable
private fun ComicLooseBeat(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
    bottomReserve: Dp = 0.dp,
) {
    val balloons = remember(beat.key, beat.title, beat.body) { comicNarrationBalloons(beat.title, beat.body) }

    Box(
        modifier.padding(
            start = 20.dp,
            end = 20.dp,
            top = 48.dp,
            bottom = 48.dp + bottomReserve,
        ),
    ) {
        beat.eyebrow?.let {
            ComicFadeIn(modifier = Modifier.align(Alignment.TopCenter)) { ComicTag(text = it) }
        }
        balloons.forEach { spec ->
            Box(
                Modifier
                    .align(spec.alignment)
                    .fillMaxWidth(spec.widthFraction)
                    .offset(spec.offset.x, spec.offset.y),
            ) {
                spec.content()
            }
        }
    }
}

/** Everything else: framed panels read down the page, each one arriving a beat after the last. */
@Composable
private fun ComicPageColumn(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
    embedded: Boolean = false,
    revealed: Boolean = true,
) {
    var delay = 0

    fun nextDelay(): Int = (delay + 260).also { delay = it }

    Column(
        modifier
            .then(if (embedded) Modifier else Modifier.verticalScroll(rememberScrollState()))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        beat.eyebrow?.let {
            ComicFadeIn(delayMillis = nextDelay()) { ComicTag(text = it) }
        }

        // Plain lettering, not another box: a page that is mostly panels and boxes already reads
        // as a comic — a title stamped in its own block on top of that just competed with them.
        beat.title?.let {
            ComicFadeIn(delayMillis = nextDelay()) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        beat.body?.takeIf { it.isNotBlank() }?.let {
            ComicFadeIn(delayMillis = nextDelay()) {
                ComicCaptionBox(text = it, align = TextAlign.Start, modifier = Modifier.fillMaxWidth())
            }
        }

        beat.figures.forEachIndexed { index, url ->
            ComicFadeIn(delayMillis = nextDelay()) {
                ComicPanel(
                    modifier = Modifier.fillMaxWidth().height(190.dp),
                    borderColor = COMIC_INK,
                    background = COMIC_PAPER,
                    shape = SlantShape(topRightLean = if (index % 2 == 0) 0.03f else 0f, bottomLeftLean = 0.02f),
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        beat.entries.forEach { wiki ->
            ComicFadeIn(delayMillis = nextDelay()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ComicTag(text = wiki.title)
                    ComicCaptionBox(
                        text = wiki.content,
                        italic = false,
                        align = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        if (beat.cast.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                beat.cast.take(3).forEach { character ->
                    ComicFadeIn(delayMillis = nextDelay(), modifier = Modifier.weight(1f)) {
                        Box {
                            ComicPanel(
                                modifier = Modifier.fillMaxWidth().height(140.dp),
                                borderColor = COMIC_INK,
                                background = COMIC_PAPER,
                            ) {
                                AsyncImage(
                                    model = character.image,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            // Hung off the corner rather than tucked inside — a name plate that
                            // fits neatly within the frame stops reading as a comic.
                            ComicTag(
                                text = character.name,
                                modifier = Modifier.align(Alignment.BottomStart).offset((-6).dp, 8.dp),
                            )
                        }
                    }
                }
            }
        }

        beat.aside?.let { aside ->
            ComicFadeIn(delayMillis = nextDelay()) {
                ComicSpeechBalloon(text = aside.text, speaker = aside.label)
            }
        }

        if (beat.actions.isNotEmpty()) {
            AnimatedVisibility(!beat.gateActionsOnReveal || revealed) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    beat.actions.forEach { ComicActionButton(it) }
                }
            }
        }
    }
}

/** Step count stamped in the corner, the way an issue carries its number. */
@Composable
private fun ComicIssueNumber(
    progress: StoryProgress,
    modifier: Modifier = Modifier,
) {
    ComicTag(text = "${progress.index} / ${progress.total}", modifier = modifier)
}

/**
 * An action as a physical button, same family as [PhysicalButton] everywhere else in Comic —
 * primary in the genre's own accent, secondary in paper, so "Continuar" and "Ver detalhes" read as
 * two weights of the same object instead of two different visual languages.
 */
@Composable
private fun ComicActionButton(action: StoryBeatAction) {
    val isPrimary = action.emphasis == StoryActionEmphasis.PRIMARY
    PhysicalButton(
        text = action.label,
        onClick = action.onClick,
        busy = action.busy,
        accent = if (isPrimary) MaterialTheme.colorScheme.primary else COMIC_PAPER,
        modifier = Modifier.fillMaxWidth(),
    )
}
