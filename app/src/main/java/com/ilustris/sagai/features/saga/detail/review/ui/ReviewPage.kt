package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.ui.theme.darker
import kotlin.random.Random

interface ReviewPage {
    val content: SagaContent
    val pageType: ReviewPageType

    @Composable
    fun Show(
        modifier: Modifier,
        canAnimate: Boolean = true,
        onAction: (ReviewAction) -> Unit = {},
    )

    @Composable
    fun Background(modifier: Modifier) =
        DynamicLinework(
            color =
                MaterialTheme.colorScheme.primary
                    .darker(.4f),
            Random.nextInt(4, 10),
            4.dp,
            true,
            modifier,
        )
}
