package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.ui.components.rememberMessageBlocks
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/** Floor for one block's share of the typing budget — mirrors [com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble]'s `MIN_BLOCK_DURATION`. */
private const val MIN_BLOCK_DURATION_MS = 400L

/**
 * One text bubble in Crime's simulated conversation. [isMe] is the player's own main character
 * (right side, like your own iMessage bubbles); everyone else lands on the left. No avatar for
 * either side by default — this reads as a 1:1 thread, not a group chat — [sender] only matters
 * for the Farewells stage, where each message really is attributed to a specific character and an
 * avatar earns its place. [title], when present, renders as a small bold line above [body] rather
 * than being crammed into the same sentence — a raw `"$title $body"` concatenation reads like a
 * run-on when the title has no trailing punctuation of its own.
 *
 * A long [body] renders as several consecutive [CrimeBubbleFrame] bubbles instead of one giant one
 * — [com.ilustris.sagai.features.saga.chat.ui.components.rememberMessageBlocks], the same splitter
 * the real in-game chat uses, decides the cut points. This is still one [ReviewPage] / one item in
 * the outer `LazyColumn` — the split is purely visual, revealed internally block-by-block, same as
 * [com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble] reveals one `Message`'s blocks.
 */
class CrimeTextMessagePage(
    override val content: SagaContent,
    override val pageType: ReviewPageType,
    private val body: String,
    private val isMe: Boolean,
    private val sender: Character? = null,
    private val title: String? = null,
) : ReviewPage {
    private val typingDurationMs = (body.length * 16).coerceIn(500, 3000)

    /**
     * Pop-in (320ms) + the total typing budget. Blocks split the same budget proportionally
     * (see [Show]), so their sum stays close to this — an approximation, not a re-measure, since
     * this property has no `@Composable` context to actually run the splitter in.
     */
    override val estimatedRevealDurationMs: Long = 320L + typingDurationMs

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val bodyStyle = MaterialTheme.typography.labelMedium

        BoxWithConstraints(modifier) {
            val hasAvatarSlot = !isMe && sender != null
            val density = LocalDensity.current
            val bubbleMaxWidth =
                (
                    maxWidth - CRIME_BUBBLE_ROW_PADDING - CRIME_BUBBLE_RESERVED_MARGIN -
                        CRIME_BUBBLE_HORIZONTAL_PADDING * 2 -
                        (if (hasAvatarSlot) CRIME_AVATAR_SLOT + 8.dp else 0.dp)
                ).coerceAtLeast(40.dp)
            val maxWidthPx = with(density) { bubbleMaxWidth.roundToPx() }

            val blocks = rememberMessageBlocks(text = body, style = bodyStyle, maxWidthPx = maxWidthPx)
            val blockDurations =
                remember(blocks) {
                    val totalChars = blocks.sumOf { it.length }.coerceAtLeast(1)
                    blocks.map { block ->
                        (typingDurationMs * (block.length.toDouble() / totalChars))
                            .toLong()
                            .coerceAtLeast(MIN_BLOCK_DURATION_MS)
                    }
                }

            var revealedBlocks by remember(blocks.size, canAnimate) {
                mutableIntStateOf(if (canAnimate) 1 else blocks.size)
            }

            LaunchedEffect(blocks.size, canAnimate) {
                if (canAnimate) {
                    blockDurations.indices.forEach { index ->
                        delay(blockDurations[index])
                        if (index < blockDurations.lastIndex) {
                            revealedBlocks = index + 2
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                blocks.take(revealedBlocks).forEachIndexed { index, blockText ->
                    val isLastVisible = index == revealedBlocks - 1
                    CrimeBubbleFrame(
                        isMe = isMe,
                        genre = genre,
                        sender = sender,
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
                            duration = blockDurations[index].milliseconds,
                            isAnimated = canAnimate && isLastVisible,
                        )
                    }
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CrimeBackground(modifier)
    }
}
