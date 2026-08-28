package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.PanelSpan
import com.ilustris.sagai.ui.genre.crime.CorkPin
import com.ilustris.sagai.ui.genre.crime.CorkboardBackground
import com.ilustris.sagai.ui.theme.components.HandwrittenText

/**
 * One chapter still per pin, tiled in a shared contact-sheet cluster ([PanelSpan.MOSAIC], grouped
 * under [GROUP_KEY]) — replaces the single collapsed photo-stack the old chat thread used to send.
 */
class CorkboardChapterPinPage(
    override val content: SagaContent,
    private val image: ReviewImageSource,
) : ReviewPage, CorkboardPinPage {
    override val pageType: ReviewPageType = ReviewPageType.JOURNEY
    override val panelSpan: PanelSpan = PanelSpan.MOSAIC
    override val groupKey: String = GROUP_KEY

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        CorkPin(
            modifier = modifier.padding(14.dp),
            seed = image.url.hashCode(),
        ) {
            Column {
                AsyncImage(
                    model = image.url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.9f)
                            .clip(RoundedCornerShape(2.dp)),
                )
                HandwrittenText(
                    text = image.caption,
                    fontSize = 13.sp,
                    centered = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CorkboardBackground(modifier)
    }

    private companion object {
        const val GROUP_KEY = "chapters"
    }
}
