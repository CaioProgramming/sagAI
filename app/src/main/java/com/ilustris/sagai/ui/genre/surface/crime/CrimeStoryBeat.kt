package com.ilustris.sagai.ui.genre.surface.crime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.ui.components.rememberMessageBlocks
import com.ilustris.sagai.ui.genre.crime.CRIME_AVATAR_SLOT
import com.ilustris.sagai.ui.genre.crime.CRIME_BUBBLE_HORIZONTAL_PADDING
import com.ilustris.sagai.ui.genre.crime.CRIME_BUBBLE_RESERVED_MARGIN
import com.ilustris.sagai.ui.genre.crime.CRIME_BUBBLE_ROW_PADDING
import com.ilustris.sagai.ui.genre.crime.CrimeBubbleFrame
import com.ilustris.sagai.ui.genre.crime.CrimeContactRow
import com.ilustris.sagai.ui.genre.surface.StoryActionEmphasis
import com.ilustris.sagai.ui.genre.surface.StoryBeat
import com.ilustris.sagai.ui.genre.surface.StoryBody
import com.ilustris.sagai.ui.genre.surface.storyRoot
import com.ilustris.sagai.ui.genre.surface.StoryBeatAction
import com.ilustris.sagai.ui.genre.surface.StoryBeatTone
import com.ilustris.sagai.ui.genre.surface.StoryProgress
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * A beat as messages landing in a thread. Every attachment becomes the kind of message a chat app
 * would actually send: a photo, a forwarded case file, a shared contact — never a card floating in
 * a chat-coloured room.
 *
 * The aside is deliberately *not* a bubble. It is a second voice commenting on the conversation
 * rather than a participant in it, so it renders as the centred grey line a messaging app uses for
 * its own notices.
 */
@Composable
fun CrimeStoryBeat(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
    canAnimate: Boolean = true,
    embedded: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val genre = LocalSagaGenre.current ?: Genre.CRIME
    val isMe = beat.tone == StoryBeatTone.PLAYER

    Column(
        modifier
            .storyRoot(embedded)
            .padding(contentPadding)
            .padding(
                horizontal = if (embedded) 0.dp else 12.dp,
                vertical = if (embedded) 0.dp else 16.dp,
            ),
    ) {
        StoryBody(embedded, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            beat.eyebrow?.let { CrimeThreadDivider(it) }

            beat.body?.takeIf { it.isNotBlank() }?.let { body ->
                CrimeMessageBubble(
                    body = body,
                    title = beat.title,
                    isMe = isMe,
                    genre = genre,
                    speaker = beat.speaker,
                    canAnimate = canAnimate,
                    revealKey = beat.key,
                )
            } ?: beat.title?.let { title ->
                CrimeMessageBubble(
                    body = title,
                    title = null,
                    isMe = isMe,
                    genre = genre,
                    speaker = beat.speaker,
                    canAnimate = canAnimate,
                    revealKey = beat.key,
                )
            }

            beat.figures.forEach { url -> CrimePhotoBubble(url, genre) }

            beat.entries.takeIf { it.isNotEmpty() }?.let { entries ->
                beat.entriesLabel?.let { CrimeThreadDivider(it) }
                entries.forEach { wiki ->
                    CrimeCaseFileBubble(title = wiki.title, body = wiki.content, genre = genre)
                }
            }

            beat.cast.takeIf { it.isNotEmpty() }?.let { cast ->
                beat.castLabel?.let { CrimeThreadDivider(it) }
                CrimeBubbleFrame(isMe = false, genre = genre, showAvatar = false, showTail = false) { contentColor ->
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        cast.forEach { character ->
                            CrimeContactRow(
                                character = character,
                                genre = genre,
                                subtitle = character.profile.occupation,
                                ink = contentColor,
                            )
                        }
                    }
                }
            }

            beat.aside?.let { CrimeSystemNote(it.label, it.text) }

            beat.progress?.takeIf { it.total > 1 }?.let { CrimeReadReceipt(it, isMe) }
        }

        CrimeQuickReplies(beat.actions)
    }
}

/**
 * One message, split into several consecutive bubbles when it runs long — the same splitter the
 * real in-game chat uses decides the cut points, and they reveal block by block so a paragraph
 * arrives as a burst of messages rather than one wall of text.
 */
@Composable
private fun CrimeMessageBubble(
    body: String,
    title: String?,
    isMe: Boolean,
    genre: Genre,
    speaker: Character?,
    canAnimate: Boolean,
    revealKey: Any,
) {
    val bodyStyle = MaterialTheme.typography.labelMedium

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val hasAvatarSlot = !isMe && speaker != null
        val bubbleMaxWidth =
            (
                maxWidth - CRIME_BUBBLE_ROW_PADDING - CRIME_BUBBLE_RESERVED_MARGIN -
                    CRIME_BUBBLE_HORIZONTAL_PADDING * 2 -
                    (if (hasAvatarSlot) CRIME_AVATAR_SLOT + 8.dp else 0.dp)
            ).coerceAtLeast(40.dp)
        val maxWidthPx = with(LocalDensity.current) { bubbleMaxWidth.roundToPx() }

        val blocks = rememberMessageBlocks(text = body, style = bodyStyle, maxWidthPx = maxWidthPx)
        val durations = rememberBlockDurations(blocks, body.length)

        // Keyed on revealKey, not on the beat: an attachment arriving late must not restart the
        // message the reader has already watched type in.
        var revealed by remember(revealKey, blocks.size, canAnimate) {
            mutableIntStateOf(if (canAnimate) 1 else blocks.size)
        }

        LaunchedEffect(revealKey, blocks.size, canAnimate) {
            if (canAnimate) {
                durations.indices.forEach { index ->
                    delay(durations[index])
                    if (index < durations.lastIndex) revealed = index + 2
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            blocks.take(revealed).forEachIndexed { index, blockText ->
                val isLastVisible = index == revealed - 1
                CrimeBubbleFrame(
                    isMe = isMe,
                    genre = genre,
                    sender = speaker,
                    showTail = isLastVisible,
                    showAvatar = isLastVisible,
                    canAnimate = canAnimate,
                ) { contentColor ->
                    if (index == 0) {
                        title?.let {
                            Text(
                                text = it,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                    SimpleTypewriterText(
                        text = blockText,
                        style = bodyStyle.copy(color = contentColor),
                        duration = durations[index].milliseconds,
                        isAnimated = canAnimate && isLastVisible,
                    )
                }
            }
        }
    }
}

/** Floor for one block's share of the typing budget — mirrors the real chat bubble's own. */
private const val MIN_BLOCK_DURATION_MS = 400L
private const val MS_PER_CHAR = 16
private const val MIN_TYPING_MS = 500
private const val MAX_TYPING_MS = 3000

@Composable
private fun rememberBlockDurations(
    blocks: List<String>,
    totalLength: Int,
): List<Long> {
    val budget = (totalLength * MS_PER_CHAR).coerceIn(MIN_TYPING_MS, MAX_TYPING_MS)
    return remember(blocks, budget) {
        val totalChars = blocks.sumOf { it.length }.coerceAtLeast(1)
        blocks.map { block ->
            (budget * (block.length.toDouble() / totalChars)).toLong().coerceAtLeast(MIN_BLOCK_DURATION_MS)
        }
    }
}

/** An image sent into the thread — square-cornered, no tail: a photo is not a sentence. */
@Composable
private fun CrimePhotoBubble(
    url: String,
    genre: Genre,
) {
    AnimatedVisibility(visible = url.isNotBlank(), enter = fadeIn(tween(400))) {
        CrimeBubbleFrame(
            isMe = false,
            genre = genre,
            useSpeechShape = false,
            showTail = false,
            showAvatar = false,
            contentPadding = PaddingValues(4.dp),
        ) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .width(230.dp)
                        .height(150.dp)
                        .clip(MaterialTheme.shapes.medium),
            )
        }
    }
}

/** New lore arriving as a forwarded case file: bold subject over the body, marked by a left rule. */
@Composable
private fun CrimeCaseFileBubble(
    title: String,
    body: String,
    genre: Genre,
) {
    CrimeBubbleFrame(isMe = false, genre = genre, showTail = false, showAvatar = false) { contentColor ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(38.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = .5f)),
            )
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = body,
                    color = contentColor.copy(alpha = .8f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

/** The centred label a thread uses to mark a break — a date, a new section of the conversation. */
@Composable
private fun CrimeThreadDivider(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .6f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
    )
}

/**
 * The aside as a system notice, not a bubble. Nobody in the conversation said this — it is the
 * app's own voice — and giving it a bubble would have put words in a character's mouth.
 */
@Composable
private fun CrimeSystemNote(
    label: String,
    text: String,
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f),
            textAlign = TextAlign.Center,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/** Step count as a read receipt under the last message, where a thread puts its status. */
@Composable
private fun CrimeReadReceipt(
    progress: StoryProgress,
    isMe: Boolean,
) {
    Text(
        text = "${progress.index}/${progress.total}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        textAlign = if (isMe) TextAlign.End else TextAlign.Start,
    )
}

/** Actions as the quick-reply pills a messaging app offers under the last message. */
@Composable
private fun CrimeQuickReplies(actions: List<StoryBeatAction>) {
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
