package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.book.BookBackground
import com.ilustris.sagai.ui.genre.surface.GenreStorySurface
import com.ilustris.sagai.ui.genre.surface.StoryBeat
import com.ilustris.sagai.ui.genre.surface.StoryBeatTone

/**
 * A single storybook page: [isEpigraph] renders it as a centered italic quote (used for a stage's
 * "hook" text), otherwise as a titled body paragraph (used for the stage's main content).
 *
 * An adapter now — the storybook layout itself lives in
 * [com.ilustris.sagai.ui.genre.surface.book.BookStoryBeat], shared with the Milestone screen, so a
 * saga's prose is set the same way wherever the player meets it.
 */
class BookTextPage(
    override val content: SagaContent,
    private val text: ReviewText,
    override val pageType: ReviewPageType,
    private val isEpigraph: Boolean = false,
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
                    title = text.title,
                    body = text.subtitle,
                    tone = if (isEpigraph) StoryBeatTone.EPIGRAPH else StoryBeatTone.NARRATION,
                ),
            modifier = modifier,
            genre = content.data.genre,
            canAnimate = canAnimate,
            embedded = true,
        )
    }

    @Composable
    override fun Background(modifier: Modifier) {
        BookBackground(modifier)
    }
}
