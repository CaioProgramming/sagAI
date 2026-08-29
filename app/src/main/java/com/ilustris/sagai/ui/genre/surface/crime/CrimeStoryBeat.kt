package com.ilustris.sagai.ui.genre.surface.crime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.ui.genre.crime.CorkPin
import com.ilustris.sagai.ui.genre.crime.PinCaption
import com.ilustris.sagai.ui.genre.crime.PinProse
import com.ilustris.sagai.ui.genre.crime.PinSignature
import com.ilustris.sagai.ui.genre.crime.PinTitle
import com.ilustris.sagai.ui.genre.crime.rememberCorkboardPalette
import com.ilustris.sagai.ui.genre.surface.StoryActionEmphasis
import com.ilustris.sagai.ui.genre.surface.StoryAside
import com.ilustris.sagai.ui.genre.surface.StoryBeat
import com.ilustris.sagai.ui.genre.surface.StoryBeatAction
import com.ilustris.sagai.ui.genre.surface.StoryBody
import com.ilustris.sagai.ui.genre.surface.StoryProgress
import com.ilustris.sagai.ui.genre.surface.storyRoot
import com.ilustris.sagai.ui.theme.components.HandwrittenText
import com.ilustris.sagai.ui.theme.filters.dreamyHaze
import com.ilustris.sagai.ui.theme.hexToColor
import kotlinx.coroutines.delay

/** How long between one card being pinned up and the next. */
private const val PIN_STAGGER_MS = 420L

private const val PIN_ENTER_MS = 480

/** A milestone body is read on one screen, so it gets room — but not an unbounded amount. */
private const val BODY_MAX_LINES = 14

/**
 * A beat as evidence going up on the case board — the same table
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.crime.CorkboardStrip] lays the
 * finished saga out on, at the scale of a single moment.
 *
 * This used to be a chat thread: bubbles, read receipts, quick replies. That was right while Crime's
 * whole identity was the messaging app, but the review has since become a board, and a milestone
 * that still simulated a conversation left the genre speaking two languages depending on which
 * screen you were on. The in-game chat stays a chat — that is the conversation itself. This screen
 * is what happens *between* scenes, which is exactly when someone steps back and pins up what they
 * have.
 *
 * Cards go up one at a time rather than all at once. A milestone is a pause in play, and watching
 * the board fill is what gives that pause its beat — the job the typing indicator used to do.
 */
@Composable
fun CrimeStoryBeat(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
    canAnimate: Boolean = true,
    embedded: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val palette = rememberCorkboardPalette()

    // Built as a list so the reveal can walk it. Each entry is one card on the board.
    val cards: List<@Composable () -> Unit> =
        buildList {
            val headline = beat.title?.takeIf { it.isNotBlank() }
            val body = beat.body?.takeIf { it.isNotBlank() }

            if (headline != null || body != null) {
                add {
                    CrimeBeatCard(
                        title = headline,
                        body = body,
                        speaker = beat.speaker,
                        canAnimate = canAnimate,
                    )
                }
            }

            beat.figures.filter { it.isNotBlank() }.forEach { url ->
                add { CrimeEvidencePhoto(url) }
            }

            beat.entries.forEach { wiki ->
                add { CrimeCaseFileCard(title = wiki.title, body = wiki.content, canAnimate = canAnimate) }
            }

            beat.cast.takeIf { it.isNotEmpty() }?.let { cast ->
                add { CrimeSuspectRow(cast = cast, label = beat.castLabel) }
            }

            beat.aside?.let { aside -> add { CrimeMarginNote(aside, canAnimate) } }
        }

    var pinned by remember(beat.key, canAnimate, cards.size) {
        mutableIntStateOf(if (canAnimate) 0 else cards.size)
    }

    LaunchedEffect(beat.key, canAnimate, cards.size) {
        if (!canAnimate) return@LaunchedEffect
        repeat(cards.size) { index ->
            pinned = index + 1
            delay(PIN_STAGGER_MS)
        }
    }

    Column(
        modifier
            .storyRoot(embedded)
            .padding(contentPadding)
            .padding(
                horizontal = if (embedded) 0.dp else 20.dp,
                vertical = if (embedded) 0.dp else 16.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StoryBody(embedded, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            beat.eyebrow?.let { CrimeFileLabel(it) }

            cards.forEachIndexed { index, card ->
                AnimatedVisibility(
                    visible = index < pinned,
                    enter =
                        fadeIn(tween(PIN_ENTER_MS)) +
                            // Settles in from slightly oversized, like a card pressed onto the
                            // board rather than faded onto it.
                            scaleIn(tween(PIN_ENTER_MS), initialScale = 1.06f),
                ) {
                    card()
                }
            }

            beat.progress?.takeIf { it.total > 1 }?.let { CrimePinTrail(it, palette.thread) }
        }

        CrimeCaseActions(beat.actions)
    }
}

/** The beat itself: title handwritten, prose in the normal face, signed if anyone said it. */
@Composable
private fun CrimeBeatCard(
    title: String?,
    body: String?,
    speaker: Character?,
    canAnimate: Boolean,
) {
    val accent = MaterialTheme.colorScheme.primary
    val signatureColor = speaker?.hexColor?.hexToColor() ?: accent

    CorkPin(
        modifier = Modifier.fillMaxWidth(),
        seed = (title ?: body).hashCode(),
        // A board has no left and right, so the tone can't place the card the way it placed a chat
        // bubble. The pushpin carries who is speaking instead — their own colour holding their card
        // up — rather than that attribution being dropped entirely.
        pinColor = signatureColor,
    ) { ink ->
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            title?.let { PinTitle(it, ink, centered = false, isAnimated = canAnimate) }
            body?.let { PinProse(text = it, ink = ink, maxLines = BODY_MAX_LINES) }
            speaker?.let {
                PinSignature(
                    name = it.name,
                    color = signatureColor,
                    isAnimated = canAnimate,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

/** A photo tacked to the board, wearing the same soft-focus haze the review's stills do. */
@Composable
private fun CrimeEvidencePhoto(url: String) {
    CorkPin(
        modifier = Modifier.fillMaxWidth(0.82f),
        seed = url.hashCode(),
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f)
                    .clip(RoundedCornerShape(2.dp))
                    .dreamyHaze(),
        )
    }
}

/** New lore as a clipping pinned to the board: subject handwritten, the entry itself typeset. */
@Composable
private fun CrimeCaseFileCard(
    title: String,
    body: String,
    canAnimate: Boolean,
) {
    CorkPin(
        modifier = Modifier.fillMaxWidth(0.92f),
        seed = title.hashCode(),
    ) { ink ->
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PinTitle(title, ink, centered = false, isAnimated = canAnimate)
            PinProse(text = body, ink = ink, maxLines = BODY_MAX_LINES)
        }
    }
}

/** The cast as suspect photos in a row, each its own small pinned card. */
@Composable
private fun CrimeSuspectRow(
    cast: List<Character>,
    label: String?,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        label?.let { CrimeFileLabel(it) }

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            cast.forEach { character ->
                CorkPin(
                    modifier = Modifier.width(112.dp),
                    seed = character.id,
                ) { ink ->
                    Column {
                        AsyncImage(
                            model = character.image,
                            contentDescription = character.name,
                            contentScale = ContentScale.Crop,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(2.dp)),
                        )
                        PinCaption(
                            text = character.name,
                            ink = ink,
                            emphasized = true,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        character.profile.occupation
                            .takeIf { it.isNotBlank() }
                            ?.let { PinCaption(text = it, ink = ink, maxLines = 1) }
                    }
                }
            }
        }
    }
}

/**
 * The aside as a note scribbled in the margin — handwriting, because unlike the beat's prose this
 * genuinely is someone's own hand commenting on what's pinned up, not part of the record.
 *
 * This carries the milestone's emotional review, which is a reflection of a sentence or three. It
 * is deliberately *not* [PinSignature]: that one caps at a single line because a signature is a
 * name, and running the aside through it silently truncated the reflection to its first line.
 */
@Composable
private fun CrimeMarginNote(
    aside: StoryAside,
    canAnimate: Boolean,
) {
    val palette = rememberCorkboardPalette()

    Column(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = aside.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = palette.thread.copy(alpha = 0.8f),
            textAlign = TextAlign.End,
        )
        HandwrittenText(
            text = aside.text,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            fontSize = 16.sp,
            centered = false,
            isAnimated = canAnimate,
        )
    }
}

/** A label written straight on the board, not on paper — a section marker for what follows. */
@Composable
private fun CrimeFileLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    )
}

/**
 * Step count as a row of pushpins — the ones already passed filled in, the rest empty. A board has
 * no read receipts; it has how many things are up on it.
 */
@Composable
private fun CrimePinTrail(
    progress: StoryProgress,
    threadColor: Color,
) {
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(progress.total) { index ->
            val done = index < progress.index
            Box(
                Modifier
                    .size(if (done) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(threadColor.copy(alpha = if (done) 0.9f else 0.3f)),
            )
        }
    }
}

/** Actions as tags clipped to the bottom of the board. */
@Composable
private fun CrimeCaseActions(actions: List<StoryBeatAction>) {
    if (actions.isEmpty()) return
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        actions.forEach { action ->
            val isPrimary = action.emphasis == StoryActionEmphasis.PRIMARY
            val accent = MaterialTheme.colorScheme.primary
            Surface(
                shape = CircleShape,
                color = if (isPrimary) accent else Color.Transparent,
                border = if (isPrimary) null else BorderStroke(1.dp, accent.copy(alpha = .6f)),
                modifier =
                    Modifier.then(
                        if (action.busy) Modifier else Modifier.clickable(onClick = action.onClick),
                    ),
            ) {
                Row(
                    Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (action.busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(13.dp),
                            strokeWidth = 1.5.dp,
                            color = if (isPrimary) MaterialTheme.colorScheme.onPrimary else accent,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = action.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isPrimary) MaterialTheme.colorScheme.onPrimary else accent,
                    )
                }
            }
        }
    }
}
