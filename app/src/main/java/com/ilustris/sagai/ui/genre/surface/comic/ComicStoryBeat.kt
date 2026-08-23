package com.ilustris.sagai.ui.genre.surface.comic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.ui.genre.comic.COMIC_INK
import com.ilustris.sagai.ui.genre.comic.COMIC_PAPER
import com.ilustris.sagai.ui.genre.comic.ComicCaptionBox
import com.ilustris.sagai.ui.genre.comic.ComicFadeIn
import com.ilustris.sagai.ui.genre.comic.ComicPanel
import com.ilustris.sagai.ui.genre.comic.ComicShoutBlock
import com.ilustris.sagai.ui.genre.comic.ComicSpeechBalloon
import com.ilustris.sagai.ui.genre.comic.ComicTag
import com.ilustris.sagai.ui.genre.comic.SlantShape
import com.ilustris.sagai.ui.genre.comic.comicNarrationBalloons
import com.ilustris.sagai.ui.genre.surface.StoryActionEmphasis
import com.ilustris.sagai.ui.genre.surface.StoryBeat
import com.ilustris.sagai.ui.genre.surface.StoryBeatAction
import com.ilustris.sagai.ui.genre.surface.StoryProgress

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
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val hasAttachments = beat.figures.isNotEmpty() || beat.entries.isNotEmpty() || beat.cast.isNotEmpty()

    var revealed by remember(beat.key) { mutableStateOf(!canAnimate) }
    LaunchedEffect(beat.key) { revealed = true }

    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(contentPadding),
    ) {
        if (hasAttachments) {
            ComicPageColumn(beat, Modifier.fillMaxSize())
        } else {
            ComicLooseBeat(beat, Modifier.fillMaxSize())
        }

        beat.progress?.takeIf { it.total > 1 }?.let {
            ComicIssueNumber(it, Modifier.align(Alignment.TopEnd).padding(16.dp))
        }

        AnimatedVisibility(
            visible = !beat.gateActionsOnReveal || revealed,
            modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                beat.actions.forEach { ComicActionBlock(it) }
            }
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
) {
    val balloons = remember(beat.key, beat.title, beat.body) { comicNarrationBalloons(beat.title, beat.body) }

    Box(modifier.padding(horizontal = 20.dp, vertical = 48.dp)) {
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
        beat.aside?.let { aside ->
            ComicFadeIn(delayMillis = 1400, modifier = Modifier.align(Alignment.BottomStart)) {
                ComicSpeechBalloon(text = aside.text, speaker = aside.label)
            }
        }
    }
}

/** Everything else: framed panels read down the page, each one arriving a beat after the last. */
@Composable
private fun ComicPageColumn(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
) {
    var delay = 0
    fun nextDelay(): Int = (delay + 260).also { delay = it }

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .padding(bottom = 72.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        beat.eyebrow?.let {
            ComicFadeIn(delayMillis = nextDelay()) { ComicTag(text = it) }
        }

        beat.title?.let {
            ComicFadeIn(delayMillis = nextDelay()) { ComicShoutBlock(text = it) }
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

/** An action as the loudest block on the page — a comic has no buttons, only lettering. */
@Composable
private fun ComicActionBlock(action: StoryBeatAction) {
    val isPrimary = action.emphasis == StoryActionEmphasis.PRIMARY
    Box(Modifier.then(if (action.busy) Modifier else Modifier.clickable(onClick = action.onClick))) {
        if (isPrimary) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (action.busy) {
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = COMIC_PAPER)
                    Spacer(Modifier.width(8.dp))
                }
                ComicShoutBlock(text = action.label)
            }
        } else {
            ComicCaptionBox(text = action.label, italic = false)
        }
    }
}
