package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.ui.theme.darker
import kotlin.random.Random

/**
 * True while a page is rendered into a share card instead of the live experience.
 * Pages read it to settle straight into their final state and to drop interactive
 * chrome (share buttons) that shouldn't land in the exported image.
 */
val LocalReviewCapture = staticCompositionLocalOf { false }

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
            lineCount = Random.nextInt(4, 10),
            strokeWidth = 4.dp,
            enabled = !LocalReviewCapture.current,
            modifier = modifier,
        )
}
