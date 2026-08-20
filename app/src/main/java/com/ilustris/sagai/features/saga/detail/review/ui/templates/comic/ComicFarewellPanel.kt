package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

internal const val FAREWELL_GROUP = "farewells"

/**
 * One character, one frame, one line — the send-off split across the page instead of collected on
 * a single card.
 *
 * Modelled on the talking-heads page a comic runs when several people answer the same moment: each
 * gets their own frame, the portrait fills it, and a small caption box sits at the top. The
 * speaker is deliberately unnamed — in a sequence of faces the portrait *is* the attribution, and
 * a name plate would only repeat what the reader is already looking at.
 *
 * Emitted one per farewell rather than as a grid inside one panel, so the row packing gives them
 * real frames on the page and the camera can rest on each face in turn.
 */
class ComicFarewellPanel(
    override val content: SagaContent,
    private val character: Character,
    private val message: String,
) : ReviewPage,
    ComicPanelPage {
    override val pageType: ReviewPageType = ReviewPageType.FAREWELLS

    // Gridded rather than packed: the send-offs are peers, and the row templates would hand
    // whichever one landed on a full-width template a prominence the others didn't get.
    override val panelSpan = PanelSpan.GRID

    override val groupKey = FAREWELL_GROUP

    override val estimatedRevealDurationMs: Long = 4200L

    override val balloons: List<ComicBalloonSpec>
        get() =
            listOf(
                ComicBalloonSpec(
                    alignment = Alignment.TopCenter,
                    widthFraction = 0.88f,
                    offset = DpOffset(0.dp, 12.dp),
                ) {
                    ComicFadeIn {
                        ComicCaptionBox(
                            text = message,
                            align = TextAlign.Start,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                },
            )

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        Box(
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        ) {
            character.image.takeIf { it.isNotBlank() }?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
