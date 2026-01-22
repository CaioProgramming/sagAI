package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.home.data.model.SagaContent

class ReviewPlaystylePage(
    private val content: SagaContent,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        onAction: (ReviewAction) -> Unit,
    ) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            DynamicLinework(content.data.genre.color, 15)
            ReviewTextDisplay(
                title =
                    content.data.review
                        ?.playstyle
                        ?.content
                        ?.title,
                subtitle =
                    content.data.review
                        ?.playstyle
                        ?.content
                        ?.subtitle,
                genre = content.data.genre,
            )
        }
    }
}
