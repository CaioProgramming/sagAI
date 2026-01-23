package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.home.data.model.SagaContent

class ReviewJourneyPage(
    override val content: SagaContent,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            DynamicLinework(content.data.genre.color, 20, enabled = canAnimate)
            ReviewTextDisplay(
                title =
                    content.data.review
                        ?.actsInsight
                        ?.content
                        ?.title,
                subtitle =
                    content.data.review
                        ?.actsInsight
                        ?.content
                        ?.subtitle,
                canAnimate = canAnimate,
                genre = content.data.genre,
            )
        }
    }
}
