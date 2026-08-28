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
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.PanelSpan
import com.ilustris.sagai.ui.genre.crime.CorkPin
import com.ilustris.sagai.ui.genre.crime.CorkboardBackground
import com.ilustris.sagai.ui.theme.components.HandwrittenText

/**
 * The board's opening pin: the saga's own icon tacked up like a case-file photo, the
 * introduction stage's hook+content merged into one caption underneath — replaces
 * [CrimeTitleCardPage]'s standalone title card now that there's no chat thread to precede.
 */
class CorkboardCoverPinPage(
    override val content: SagaContent,
    private val caption: String?,
) : ReviewPage, CorkboardPinPage {
    override val pageType: ReviewPageType = ReviewPageType.INTRO
    override val panelSpan: PanelSpan = PanelSpan.SPLASH

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val accent = genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary

        CorkPin(
            modifier = modifier.padding(20.dp),
            seed = content.data.id,
            pinColor = accent,
        ) {
            Column {
                AsyncImage(
                    model = content.data.icon,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.85f)
                            .clip(RoundedCornerShape(2.dp)),
                )
                HandwrittenText(
                    text = content.data.title,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    fontSize = 20.sp,
                    centered = true,
                )
                caption?.let {
                    HandwrittenText(
                        text = it,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        fontSize = 13.sp,
                        isBold = false,
                        centered = true,
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
