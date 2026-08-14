package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.share.domain.model.ShareType

/**
 * A "plate" page of several chapter stills — the book equivalent of Default's `JourneyCollage`,
 * framed in sepia instead of the app's neon/holographic chapter-card styling so it stays inside
 * the parchment idiom.
 */
class BookJourneyPlatePage(
    override val content: SagaContent,
    private val images: List<ReviewImageSource>,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.JOURNEY

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent =
            content.data.genre
                .compiledColorPalette()
                .firstOrNull() ?: MaterialTheme.colorScheme.primary
        val ink = LocalContentColor.current

        Column(
            modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.review_stage_journey_title),
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = accent,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )

            images.chunked(2).forEach { rowImages ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowImages.forEach { image ->
                        Column(Modifier.weight(1f)) {
                            AsyncImage(
                                model = image.url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.85f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .border(BorderStroke(2.dp, accent.copy(alpha = 0.6f)), RoundedCornerShape(2.dp)),
                            )
                            Text(
                                text = image.caption,
                                fontStyle = FontStyle.Italic,
                                color = ink.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    if (rowImages.size == 1) {
                        Column(Modifier.weight(1f)) {}
                    }
                }
            }

            BookShareLink(ShareType.HISTORY, accent, onAction, modifier = Modifier.padding(top = 8.dp))
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        BookBackground(modifier)
    }
}
