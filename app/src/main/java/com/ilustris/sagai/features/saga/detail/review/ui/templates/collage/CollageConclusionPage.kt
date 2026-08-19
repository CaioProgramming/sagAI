package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/**
 * Punk Rock's closing stage: the whole cast pasted up as torn scraps behind the send-off headline.
 * The main character leads the pile — this is the saga's curtain call, so unlike
 * [CollageCharactersPage] they aren't filtered out.
 */
class CollageConclusionPage(
    override val content: SagaContent,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.CONCLUSION

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val portraits =
            remember {
                val main = content.mainCharacter?.data?.image?.takeIf { it.isNotBlank() }
                val rest =
                    content
                        .getCharacters(filterMainCharacter = true)
                        .map { it.image }
                        .filter { it.isNotBlank() }
                listOfNotNull(main) + rest
            }

        val conclusion =
            content.data.review
                ?.conclusion
                ?.content

        CollageScatterLayout(
            imageUrls = portraits,
            title = conclusion?.title,
            note = conclusion?.subtitle,
            canAnimate = canAnimate,
            seedBase = 150,
            modifier = modifier,
        )
    }

    @Composable
    override fun Background(modifier: Modifier) {
        Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    }
}
