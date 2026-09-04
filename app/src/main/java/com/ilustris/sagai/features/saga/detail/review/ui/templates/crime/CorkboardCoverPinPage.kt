package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
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
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.sagaBrush

/** A cover caption is the line or two that fits under the photo; the rest is on the back. */
private const val CAPTION_MAX_LINES = 3

/**
 * The table's opening photo: the saga's own icon, laid down like the case file's first exhibit.
 *
 * The saga title is the one place the handwritten face earns its impact, so it keeps it. The
 * introduction's prose does not fit under a photo, so the short [caption] rides on the front and
 * [fullIntroduction] goes on the back — see [CorkPin]'s flip.
 */
class CorkboardCoverPinPage(
    override val content: SagaContent,
    private val caption: String?,
    private val fullIntroduction: String? = null,
) : ReviewPage,
    CorkboardPinPage {
    override val pageType: ReviewPageType = ReviewPageType.INTRO
    override val pinSize: CorkPinSize = CorkPinSize.COVER

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val accent = genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary

        CorkPin(
            modifier = modifier,
            seed = content.data.id,
            pinColor = accent,
            back =
                fullIntroduction?.let { prose ->
                    { ink -> PinBackNote(text = prose, ink = ink, title = content.data.title) }
                },
        ) { ink ->
            Column {
                AsyncImage(
                    model = content.data.icon,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.85f)
                            .clip(RoundedCornerShape(2.dp))
                            .dreamyHaze(),
                )
                PinTitle(
                    text = content.data.title,
                    ink = ink,
                    isAnimated = canAnimate,
                    modifier = Modifier.padding(top = 10.dp).gradientFill(sagaBrush()),
                )
                caption?.let {
                    PinProse(
                        text = it,
                        ink = ink,
                        centered = true,
                        maxLines = CAPTION_MAX_LINES,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CorkboardBackground(modifier)
    }
}
