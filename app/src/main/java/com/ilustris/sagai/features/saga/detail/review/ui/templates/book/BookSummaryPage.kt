package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/** The book's "Table of Contents" — tap a chapter to turn back to it. */
class BookSummaryPage(
    override val content: SagaContent,
    override val pageType: ReviewPageType = ReviewPageType.SUMMARY,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = content.data.genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary
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
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.review_summary_title),
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = accent,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )
            HorizontalDivider(color = accent.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth(0.5f))

            chapters.forEach { (titleRes, pageType) ->
                Text(
                    text = stringResource(titleRes),
                    color = ink,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier =
                        Modifier
                            .clickable { onAction(ReviewAction.Navigate(pageType)) }
                            .padding(vertical = 4.dp),
                )
            }

            Text(
                text = stringResource(R.string.review_restart_button),
                fontStyle = FontStyle.Italic,
                color = accent,
                style = MaterialTheme.typography.bodyLarge,
                modifier =
                    Modifier
                        .clickable { onAction(ReviewAction.Restart) }
                        .padding(top = 12.dp),
            )

            if (BuildConfig.DEBUG) {
                Text(
                    text = stringResource(R.string.review_regenerate_button),
                    fontStyle = FontStyle.Italic,
                    color = ink.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { onAction(ReviewAction.Regenerate) },
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        BookBackground(modifier)
    }
}
