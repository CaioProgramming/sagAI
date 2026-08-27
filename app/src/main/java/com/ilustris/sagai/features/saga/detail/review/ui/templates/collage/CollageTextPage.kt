package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.surface.GenreStorySurface
import com.ilustris.sagai.ui.genre.surface.StoryBeat

/**
 * Replacement for the default review's plain hook/text pages: each line of copy gets its own
 * edge-to-edge torn strip, the title ripping across first and the body following a beat later, so
 * the page reads as something physically torn open rather than text fading in over a background.
 *
 * An adapter now, over the collage layout in
 * [com.ilustris.sagai.ui.genre.surface.collage.CollageStoryBeat] that the Milestone screen also
 * uses — same seeds, same tear delays, same overscan.
 */
class CollageTextPage(
    override val content: SagaContent,
    override val pageType: ReviewPageType,
    private val text: ReviewText,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        GenreStorySurface(
            beat = StoryBeat(key = pageType, title = text.title, body = text.subtitle),
            modifier = modifier,
            genre = content.data.genre,
            canAnimate = canAnimate,
            embedded = true,
        )
    }

    @Composable
    override fun Background(modifier: Modifier) {
        Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    }
}
