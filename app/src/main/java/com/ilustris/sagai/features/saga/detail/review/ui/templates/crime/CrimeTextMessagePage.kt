package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.crime.CrimeBackground
import com.ilustris.sagai.ui.genre.surface.GenreStorySurface
import com.ilustris.sagai.ui.genre.surface.StoryBeat
import com.ilustris.sagai.ui.genre.surface.StoryBeatTone
import com.ilustris.sagai.ui.genre.surface.estimatedRevealDurationMs

/** Pop-in, before any typing starts. */
private const val BUBBLE_POP_IN_MS = 320L

/**
 * One text bubble in Crime's simulated conversation. [isMe] is the player's own main character
 * (right side, like your own iMessage bubbles); everyone else lands on the left. No avatar for
 * either side by default — this reads as a 1:1 thread, not a group chat — [sender] only matters for
 * the Farewells stage, where each message really is attributed to a specific character and an
 * avatar earns its place. [title], when present, renders as a small bold line above [body] rather
 * than being crammed into the same sentence.
 *
 * An adapter now, over the thread layout in
 * [com.ilustris.sagai.ui.genre.surface.crime.CrimeStoryBeat] that the Milestone screen also uses —
 * including the block splitter that turns a long [body] into several consecutive bubbles.
 */
class CrimeTextMessagePage(
    override val content: SagaContent,
    override val pageType: ReviewPageType,
    private val body: String,
    private val isMe: Boolean,
    private val sender: Character? = null,
    private val title: String? = null,
) : ReviewPage {
    private val beat =
        StoryBeat(
            // Not pageType alone: a Crime stage contributes several bubbles with the same type, and
            // they must not share reveal state.
            key = pageType to body,
            title = title,
            body = body,
            tone = if (isMe) StoryBeatTone.PLAYER else StoryBeatTone.NARRATION,
            speaker = sender,
        )

    /**
     * Pop-in plus the typing budget. An approximation rather than a re-measure — blocks split that
     * budget proportionally, so their sum stays close to this. ChatScroll paces the whole thread off
     * this number, so the arithmetic is deliberately unchanged from before the surface was shared.
     */
    override val estimatedRevealDurationMs: Long = BUBBLE_POP_IN_MS + beat.estimatedRevealDurationMs()

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        GenreStorySurface(
            beat = beat,
            modifier = modifier,
            genre = content.data.genre,
            canAnimate = canAnimate,
            embedded = true,
        )
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CrimeBackground(modifier)
    }
}
