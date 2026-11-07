package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.comic.COMIC_INK
import com.ilustris.sagai.ui.genre.comic.COMIC_PAPER
import com.ilustris.sagai.ui.genre.comic.ComicFadeIn
import com.ilustris.sagai.ui.genre.comic.ComicShoutBlock

/** One stage the reader can jump back to from the back cover. */
private data class StageCard(
    val type: ReviewPageType,
    val title: Int,
    val subtitle: String?,
)

/**
 * The issue's back cover: "the end", an index of the stages — each a card that turns straight back
 * to its page — and the two ways out, read it again or close it. It exists only once the review is
 * complete, so it doubles as the marker that nothing more is coming (until then the pager ends on
 * the loading page).
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
        val review = content.data.review
        val cards =
            remember(review) {
                listOfNotNull(
                    review?.expressiveness?.let {
                        StageCard(ReviewPageType.EXPRESSIVENESS, R.string.review_stage_expressiveness_title, it.content?.title)
                    },
                    review?.playstyle?.let {
                        StageCard(ReviewPageType.PLAYSTYLE, R.string.review_stage_playstyle_title, it.content?.title)
                    },
                    review?.topCharacters?.let {
                        StageCard(ReviewPageType.CHARACTERS, R.string.review_stage_characters_title, it.content?.title)
                    },
                    review?.actsInsight?.let {
                        StageCard(ReviewPageType.JOURNEY, R.string.review_stage_journey_title, it.content?.title)
                    },
                    review?.conclusion?.let {
                        StageCard(ReviewPageType.CONCLUSION, R.string.review_stage_conclusion_title, it.content?.title)
                    },
                    review?.farewells?.takeIf { it.isNotEmpty() }?.let {
                        StageCard(ReviewPageType.FAREWELLS, R.string.review_farewells_title, null)
                    },
                )
            }

        Column(
            modifier.fillMaxSize().background(COMIC_PAPER).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ComicFadeIn {
                ComicShoutBlock(text = stringResource(R.string.review_comic_the_end))
            }
            ComicFadeIn(delayMillis = 200) {
                Text(
                    text = stringResource(R.string.review_summary_title).uppercase(),
                    color = COMIC_INK,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            // Two to a row; an odd last card is left to take its half rather than stretch.
            cards.chunked(2).forEachIndexed { row, pair ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    pair.forEachIndexed { column, card ->
                        ComicFadeIn(
                            delayMillis = 350 + (row * 2 + column) * 120,
                            modifier = Modifier.weight(1f),
                        ) {
                            StageIndexCard(card) { onAction(ReviewAction.Navigate(card.type)) }
                        }
                    }
                    if (pair.size == 1) Box(Modifier.weight(1f))
                }
            }

            ComicFadeIn(delayMillis = 350 + cards.size * 120) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
private fun StageIndexCard(
    card: StageCard,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .background(COMIC_PAPER)
            .border(2.dp, COMIC_INK)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(card.title).uppercase(),
            color = COMIC_INK,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
        )
        card.subtitle?.let {
            Text(
                text = it,
                color = COMIC_INK,
                fontStyle = FontStyle.Italic,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
            )
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(
            text = text.uppercase(),
            color = COMIC_INK,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}
