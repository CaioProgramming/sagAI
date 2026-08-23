package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.surface.GenreStorySurface
import com.ilustris.sagai.ui.genre.surface.StoryBeat
import com.ilustris.sagai.ui.genre.terminal.TerminalBackground

/**
 * A single terminal "command" — a prompt line followed by its typed output. Covers hook and
 * stage-content text alike, since both are just a title/subtitle pair.
 *
 * An adapter now, over the transcript layout in
 * [com.ilustris.sagai.ui.genre.surface.terminal.TerminalStoryBeat] that the Milestone screen also
 * uses.
 *
 * The stage's own title is passed as the beat's verb rather than [command]: it is already the label
 * for what this beat is, it is already translated, and inventing a second English word for the same
 * thing meant carrying a string that would need translating later. [command] is the fallback.
 */
class TerminalTextPage(
    override val content: SagaContent,
    private val text: ReviewText,
    override val pageType: ReviewPageType,
    private val command: String,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        GenreStorySurface(
            beat =
                StoryBeat(
                    key = pageType,
                    body = text.subtitle,
                    verb = text.title ?: command,
                    source = content.data.title,
                ),
            modifier = modifier,
            genre = content.data.genre,
            canAnimate = canAnimate,
            embedded = true,
        )
    }

    @Composable
    override fun Background(modifier: Modifier) {
        TerminalBackground(modifier)
    }
}
