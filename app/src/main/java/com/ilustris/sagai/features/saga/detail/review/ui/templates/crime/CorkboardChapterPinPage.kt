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
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.crime.CorkPin
import com.ilustris.sagai.ui.genre.crime.PinBackNote
import com.ilustris.sagai.ui.genre.crime.PinCaption
import com.ilustris.sagai.ui.genre.crime.PinProse
import com.ilustris.sagai.ui.genre.crime.PinSignature
import com.ilustris.sagai.ui.genre.crime.PinTitle
import com.ilustris.sagai.ui.genre.crime.CorkboardBackground
import com.ilustris.sagai.ui.theme.filters.dreamyHaze

/**
 * One chapter still per photo, dealt out along the table in reading order.
 *
 * [journeyBeat] is this photo's share of the journey stage's write-up, written on the back the way
 * someone captions a photo they're mailing on. The comic page does the same thing with its plates
 * (see [com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicPlatePanel]) — and
 * it exists for the same reason: without it, the journey prose the review generates has nowhere to
 * go and is lost.
 */
class CorkboardChapterPinPage(
    override val content: SagaContent,
    private val image: ReviewImageSource,
    private val journeyBeat: String? = null,
) : ReviewPage, CorkboardPinPage {
    override val pageType: ReviewPageType = ReviewPageType.JOURNEY
    override val pinSize: CorkPinSize = CorkPinSize.PHOTO

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        CorkPin(
            modifier = modifier,
            seed = image.url.hashCode(),
            back =
                journeyBeat?.let { beat ->
                    { ink -> PinBackNote(text = beat, ink = ink, title = image.caption) }
                },
        ) { ink ->
            Column {
                AsyncImage(
                    model = image.url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.9f)
                            .clip(RoundedCornerShape(2.dp))
                            .dreamyHaze(),
                )
                PinCaption(
                    text = image.caption,
                    ink = ink,
                    emphasized = true,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CorkboardBackground(modifier)
    }
}
