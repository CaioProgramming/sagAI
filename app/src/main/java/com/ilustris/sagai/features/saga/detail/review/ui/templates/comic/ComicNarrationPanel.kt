package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.comic.ComicBalloonSpec
import com.ilustris.sagai.ui.genre.comic.ComicCaptionBox
import com.ilustris.sagai.ui.genre.comic.ComicFadeIn
import com.ilustris.sagai.ui.genre.comic.ComicTag

/**
 * A frame that is pure narration, told as caption boxes scattered *loose* over the frame rather
 * than stacked inside it.
 *
 * The boxes are balloons, so they sit above the page and spill past the borders — alternating side
 * to side and overhanging the frame is what gives a text-only beat any movement at all. Boxed
 * neatly inside the panel the same words read as a slide; hung off it they read as a comic.
 *
 * The frame beneath is left as bare ground on purpose: it is the plate the boxes are pinned to,
 * not a container they have to fit within.
 */
class ComicNarrationPanel(
    override val content: SagaContent,
    private val text: ReviewText,
    override val pageType: ReviewPageType,
) : ReviewPage,
    ComicPanelPage {
    // No border, no ground. The boxes are the whole panel, and framing them inside an empty
    // rectangle made a beat that should feel loose on the page read as a boxed-in slide instead.
    override val hasFrame = false

    override val panelSpan = PanelSpan.BAND

    override val balloons: List<ComicBalloonSpec>
        get() =
            buildList {
                text.title?.let { title ->
                    add(
                        ComicBalloonSpec(
                            alignment = Alignment.TopStart,
                            widthFraction = 0.86f,
                            offset = DpOffset((-10).dp, 10.dp),
                        ) { ComicFadeIn { ComicTag(text = title) } },
                    )
                }

                val beats = splitIntoBeats(text.subtitle.orEmpty(), maxBeats = 3)
                beats.forEachIndexed { index, beat ->
                    // Spread down the frame by bias rather than by fixed offsets: the boxes have
                    // no way to measure each other, so dividing the height between them up front
                    // is what keeps a long beat from landing on top of the next one.
                    val verticalBias = -0.35f + 1.25f * ((index + 0.5f) / beats.size)
                    val leaning = if (index % 2 == 0) 1 else -1
                    add(
                        ComicBalloonSpec(
                            alignment = BiasAlignment(0.28f * leaning, verticalBias),
                            widthFraction = 0.82f,
                            offset = DpOffset((14 * leaning).dp, 0.dp),
                        ) {
                            ComicFadeIn(delayMillis = 250 + index * 400) {
                                ComicCaptionBox(text = beat, align = TextAlign.Start)
                            }
                        },
                    )
                }
            }

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        Box(modifier.fillMaxSize())
    }
}

/**
 * Breaks prose on sentence boundaries and regroups it into at most [maxBeats] roughly even chunks,
 * so a long paragraph becomes a few caption boxes instead of one per sentence (which would flood
 * the frame) or one box holding everything (which is the slab we're avoiding).
 */
internal fun splitIntoBeats(
    text: String,
    maxBeats: Int,
): List<String> {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return emptyList()

    val sentences =
        Regex("(?<=[.!?…])\\s+")
            .split(trimmed)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    if (sentences.size <= 1) return listOf(trimmed)

    val beats = minOf(maxBeats, sentences.size)
    val perBeat = (sentences.size + beats - 1) / beats
    return sentences.chunked(perBeat).map { it.joinToString(" ") }
}
