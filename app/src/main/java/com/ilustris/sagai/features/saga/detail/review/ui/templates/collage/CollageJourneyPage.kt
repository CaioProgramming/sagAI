package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/** Punk Rock's journey stage: every chapter cover pasted up as a torn scrap around the stage title. */
class CollageJourneyPage(
    override val content: SagaContent,
    private val journeyReview: ReviewText,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.JOURNEY

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val covers =
            remember {
                content
                    .flatChapters()
                    .map { it.data.coverImage }
                    .filter { it.isNotBlank() }
            }

        CollageScatterLayout(
            imageUrls = covers,
            title = journeyReview.title,
            note = journeyReview.subtitle,
            canAnimate = canAnimate,
            seedBase = 120,
            modifier = modifier,
        )
    }

    @Composable
    override fun Background(modifier: Modifier) {
        Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    }
}
