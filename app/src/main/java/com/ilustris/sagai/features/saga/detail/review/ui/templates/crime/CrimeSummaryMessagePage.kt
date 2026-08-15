package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/**
 * The thread's closing "system message" — centered, not left/right, since it isn't from anyone in
 * the conversation. Chapter chips jump back to that stage's first message.
 */
class CrimeSummaryMessagePage(
    override val content: SagaContent,
    override val pageType: ReviewPageType = ReviewPageType.SUMMARY,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = MaterialTheme.colorScheme.primary
        val ink = LocalContentColor.current
        val review = content.data.review ?: return

        val chapters =
            remember(review) {
                listOfNotNull(
                    (R.string.review_stage_expressiveness_title to ReviewPageType.EXPRESSIVENESS)
                        .takeIf { review.expressiveness != null },
                    (R.string.review_stage_playstyle_title to ReviewPageType.PLAYSTYLE)
                        .takeIf { review.playstyle != null },
                    (R.string.review_stage_characters_title to ReviewPageType.CHARACTERS)
                        .takeIf { review.topCharacters != null },
                    (R.string.review_stage_journey_title to ReviewPageType.JOURNEY)
                        .takeIf { review.actsInsight != null },
                    (R.string.review_stage_conclusion_title to ReviewPageType.CONCLUSION)
                        .takeIf { review.conclusion != null },
                )
            }

        Column(
            modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.review_summary_title),
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    items(chapters) { (titleRes, target) ->
                        Text(
                            text = stringResource(titleRes),
                            color = ink,
                            style = MaterialTheme.typography.labelLarge,
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(50))
                                    .clickable { onAction(ReviewAction.Navigate(target)) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.review_restart_button),
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier =
                        Modifier
                            .clickable { onAction(ReviewAction.Restart) }
                            .padding(top = 4.dp),
                )

                if (BuildConfig.DEBUG) {
                    Text(
                        text = stringResource(R.string.review_regenerate_button),
                        color = ink.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clickable { onAction(ReviewAction.Regenerate) },
                    )
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CrimeBackground(modifier)
    }
}
