package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent

class ReviewIntroPage(
    private val content: SagaContent,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        Box(modifier = modifier.fillMaxSize()) {
            AsyncImage(
                model = content.data.icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .blur(16.dp)
                        .alpha(0.5f),
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Black,
                                ),
                            ),
                        ),
            )

            ReviewTextDisplay(
                title =
                    content.data.review
                        ?.introduction
                        ?.content
                        ?.title,
                subtitle =
                    content.data.review
                        ?.introduction
                        ?.content
                        ?.subtitle,
                genre = genre,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
