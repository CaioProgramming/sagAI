package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.ilustris.sagai.ui.genre.crime.CorkPin
import com.ilustris.sagai.ui.genre.crime.PinBackNote
import com.ilustris.sagai.ui.genre.crime.PinCaption
import com.ilustris.sagai.ui.genre.crime.PinProse
import com.ilustris.sagai.ui.genre.crime.PinSignature
import com.ilustris.sagai.ui.genre.crime.PinTitle
import com.ilustris.sagai.ui.genre.crime.CorkboardBackground

/**
 * The table's last card — a "case closed" note rather than the old thread's system message with
 * chapter-jump chips. There is no linear sequence to jump back into, so [ReviewAction.Restart] —
 * which [CorkboardStrip] intercepts to run the table back to the start — covers that job instead.
 */
class CorkboardSummaryPinPage(
    override val content: SagaContent,
    override val pageType: ReviewPageType = ReviewPageType.SUMMARY,
) : ReviewPage, CorkboardPinPage {
    override val pinSize: CorkPinSize = CorkPinSize.NOTE

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = content.data.genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary

        CorkPin(
            modifier = modifier,
            seed = content.data.id + SUMMARY_SEED_OFFSET,
            pinColor = accent,
        ) { ink ->
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PinTitle(
                    text = stringResource(R.string.review_summary_title),
                    ink = ink,
                    isAnimated = canAnimate,
                )

                Text(
                    text = stringResource(R.string.review_restart_button),
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().clickable { onAction(ReviewAction.Restart) },
                )

                if (BuildConfig.DEBUG) {
                    Text(
                        text = stringResource(R.string.review_regenerate_button),
                        color = ink.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.fillMaxWidth().clickable { onAction(ReviewAction.Regenerate) },
                    )
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CorkboardBackground(modifier)
    }

    private companion object {
        const val SUMMARY_SEED_OFFSET = 41
    }
}
