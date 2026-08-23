package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.coverImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.notableChapterImageSources
import com.ilustris.sagai.ui.genre.comic.ComicBalloonSpec
import com.ilustris.sagai.ui.genre.comic.ComicCaptionBox
import com.ilustris.sagai.ui.genre.comic.ComicFadeIn
import com.ilustris.sagai.ui.genre.comic.SlantShape

/**
 * One image, one frame.
 *
 * Chapter art used to be tiled inside a single panel, which read as one wide photograph chopped up
 * rather than as a page of moments. Giving every image its own border is the whole difference: the
 * grid becomes a sequence of frames, which is what a comic page of establishing shots actually is.
 */
class ComicPlatePanel(
    override val content: SagaContent,
    private val image: ReviewImageSource,
    override val groupKey: String,
    private val index: Int,
    private val caption: String? = null,
) : ReviewPage,
    ComicPanelPage {
    override val pageType: ReviewPageType = ReviewPageType.JOURNEY

    // Mosaic rather than grid: these want uneven rows. Equal cells turned the run into a contact
    // sheet, which is the opposite of the page of moments it should read as.
    override val panelSpan = PanelSpan.MOSAIC

    override val estimatedRevealDurationMs: Long = 3200L

    // Cut on a slant, alternating which corner gives. Straight frames in a row square up into a
    // table; a leaning edge is what makes the run feel cut apart.
    override val panelShape =
        when (index % 3) {
            0 -> SlantShape(topRightLean = 0.06f)
            1 -> SlantShape(bottomLeftLean = 0.06f)
            else -> SlantShape(topLeftLean = 0.05f, bottomRightLean = 0.05f)
        }

    // The caption alternates corner with the frame's lean, so the writing travels across the run
    // rather than sitting in the same spot on every picture.
    override val balloons: List<ComicBalloonSpec>
        get() =
            caption?.let {
                val fromStart = index % 2 == 0
                listOf(
                    ComicBalloonSpec(
                        alignment = if (fromStart) Alignment.BottomStart else Alignment.TopEnd,
                        widthFraction = 0.9f,
                        offset =
                            if (fromStart) DpOffset(6.dp, (-6).dp) else DpOffset((-6).dp, 6.dp),
                    ) { ComicFadeIn { ComicCaptionBox(text = it) } },
                )
            } ?: emptyList()

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
            AsyncImage(
                model = image.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Every picture the saga owns, in one ordered pool: its own cover first, then the chapter covers.
 *
 * The template shows art in two places and the pool is sliced between them rather than each site
 * asking for "the first N chapters" — that was handing both the same images, so the page repeated
 * itself. Slicing one list guarantees a picture appears once.
 */
internal fun SagaContent.comicImagePool(chapterLimit: Int = 10): List<ReviewImageSource> =
    buildList {
        coverImageSource()?.let { add(it) }
        addAll(notableChapterImageSources(limit = chapterLimit))
    }.distinctBy { it.url }
