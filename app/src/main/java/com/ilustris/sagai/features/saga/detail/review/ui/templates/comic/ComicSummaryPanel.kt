package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.comic.COMIC_INK
import com.ilustris.sagai.ui.genre.comic.COMIC_PAPER

/**
 * The last beat: a way back to the start, and nothing else.
 *
 * The default review ends on a grid of cards linking back to each stage, which earns its place
 * when the stages were separate screens you'd otherwise have to swipe through again. Here they are
 * all still on the page — the reader can see them and tap any one of them — so an index of them is
 * a menu for navigation that already exists.
 */
class ComicSummaryPanel(
    override val content: SagaContent,
) : ReviewPage,
    ComicPanelPage {
    override val pageType: ReviewPageType = ReviewPageType.SUMMARY

    override val panelSpan = PanelSpan.BAND

    override val hasFrame = false

    override val estimatedRevealDurationMs: Long = 4000L

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .clickable { onAction(ReviewAction.Restart) }
                    .background(COMIC_PAPER)
                    .border(3.dp, COMIC_INK)
                    .padding(horizontal = 26.dp, vertical = 14.dp),
            ) {
                Text(
                    text = stringResource(R.string.review_restart_button).uppercase(),
                    color = COMIC_INK,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
