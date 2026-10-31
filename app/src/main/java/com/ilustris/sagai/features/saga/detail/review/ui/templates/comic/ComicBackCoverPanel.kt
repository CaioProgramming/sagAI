package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.comic.COMIC_INK
import com.ilustris.sagai.ui.genre.comic.COMIC_PAPER
import com.ilustris.sagai.ui.genre.comic.ComicFadeIn
import com.ilustris.sagai.ui.genre.comic.ComicShoutBlock

/**
 * The issue's back cover: the saga's icon, "the end", and the two ways out — read it again, or
 * close it. It exists only once the review is complete, so it doubles as the marker that nothing
 * more is coming (until then the pager ends on the loading page).
 */
class ComicBackCoverPanel(
    override val content: SagaContent,
) : ReviewPage,
    ComicPanelPage {
    override val pageType: ReviewPageType = ReviewPageType.SUMMARY

    override val panelSpan = PanelSpan.SPLASH

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        Column(
            modifier.fillMaxSize().background(COMIC_PAPER).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ComicFadeIn {
                content.data.icon.takeIf { it.isNotBlank() }?.let { icon ->
                    AsyncImage(
                        model = icon,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(160.dp).border(3.dp, COMIC_INK),
                    )
                }
            }
            ComicFadeIn(delayMillis = 300) {
                ComicShoutBlock(text = stringResource(R.string.review_comic_the_end))
            }
            ComicFadeIn(delayMillis = 600) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BackCoverButton(stringResource(R.string.review_restart_button)) {
                        onAction(ReviewAction.Restart)
                    }
                    BackCoverButton(stringResource(R.string.review_comic_close)) {
                        onAction(ReviewAction.Finish)
                    }
                }
            }
        }
    }
}

@Composable
private fun BackCoverButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .background(COMIC_PAPER)
            .border(3.dp, COMIC_INK)
            .padding(horizontal = 26.dp, vertical = 14.dp),
    ) {
        Text(
            text = text.uppercase(),
            color = COMIC_INK,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
