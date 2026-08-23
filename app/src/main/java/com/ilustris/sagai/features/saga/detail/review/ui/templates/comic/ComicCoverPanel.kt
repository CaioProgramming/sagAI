package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.coverImageSource
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.genre.comic.ComicBalloonSpec
import com.ilustris.sagai.ui.genre.comic.ComicCaptionBox
import com.ilustris.sagai.ui.genre.comic.ComicTag

/**
 * The issue's cover: a full-bleed portrait splash the board opens on before the camera starts
 * working through the frames. [DepthLayout] puts the logotype *behind* the segmented character —
 * the same depth trick the Punk Rock poster uses, and what makes it read as a cover rather than as
 * art with a title laid over it.
 *
 * Type is deliberately set larger than a normal screen would use: this panel is wider than the
 * viewport, so the camera frames it at well under 1:1 and anything sized for a full screen would
 * arrive shrunken.
 *
 * Gets a longer dwell than a normal panel — it's the establishing beat of the whole review.
 */
class ComicCoverPanel(
    override val content: SagaContent,
) : ReviewPage,
    ComicPanelPage {
    override val pageType: ReviewPageType = ReviewPageType.INTRO

    override val estimatedRevealDurationMs: Long = 6500L

    override val panelSpan = PanelSpan.SPLASH

    private val hook get() =
        content.data.review
            ?.introduction
            ?.hook

    override val balloons: List<ComicBalloonSpec>
        get() =
            buildList {
                // The cover line, stamped in the corner the way an issue carries its strapline.
                hook?.title?.let { title ->
                    add(
                        ComicBalloonSpec(
                            alignment = Alignment.TopStart,
                            widthFraction = 0.7f,
                            offset = DpOffset(18.dp, 18.dp),
                        ) { ComicTag(text = title) },
                    )
                }

                // The blurb sits *on* the frame rather than inside it, so the art runs full bleed
                // behind it and the box breaks the border on its way out.
                hook?.subtitle?.let { subtitle ->
                    add(
                        ComicBalloonSpec(
                            alignment = Alignment.BottomCenter,
                            widthFraction = 0.84f,
                            offset = DpOffset(0.dp, 38.dp),
                        ) { ComicCaptionBox(text = subtitle) },
                    )
                }
            }

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val cover = content.coverImageSource()

        Box(modifier.fillMaxSize()) {
            if (cover != null) {
                DepthLayout(
                    imagePath = cover.url,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 48.dp),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        // Deliberately not fillMaxWidth: the word-art draws its text at its own
                        // intrinsic width and only centres *within* that width, so stretching it
                        // to the panel pinned the logotype to the left edge. Letting it size to
                        // its content and centring the whole block is what actually centres it.
                        genre.stylisedText(
                            text = content.data.title,
                            fontSize = MaterialTheme.typography.displaySmall.fontSize,
                        )
                    }
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    }
}
