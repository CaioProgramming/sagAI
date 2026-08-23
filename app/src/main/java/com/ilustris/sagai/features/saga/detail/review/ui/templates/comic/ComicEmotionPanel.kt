package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.comic.ComicBalloonSpec
import com.ilustris.sagai.ui.genre.comic.ComicCaptionBox
import com.ilustris.sagai.ui.genre.comic.ComicFadeIn
import com.ilustris.sagai.ui.genre.comic.ComicShoutBlock
import com.ilustris.sagai.ui.genre.comic.ComicTag
import kotlinx.coroutines.delay

/** The text lands first and the portrait develops in behind it, like a photo coming up. */
private const val PORTRAIT_DELAY_MS = 1400
private const val PORTRAIT_FADE_MS = 1600

/**
 * The emotional read, given the same scale as the cover and set beside it in the opening band.
 *
 * Built the way the cover is: one big piece of art carrying the frame, with the writing laid over
 * it as small narration boxes rather than as a paragraph. The tone itself gets the one loud block
 * on the panel — it is the verdict the whole stage exists to deliver, and a caption box would bury
 * it among the beats that merely explain it.
 */
class ComicEmotionPanel(
    override val content: SagaContent,
    private val stage: ReviewStage,
) : ReviewPage,
    ComicPanelPage {
    override val pageType: ReviewPageType = ReviewPageType.EXPRESSIVENESS

    override val panelSpan = PanelSpan.SPLASH

    override val estimatedRevealDurationMs: Long = 6500L

    private val tone
        get() =
            content
                .flatEvents()
                .flatMap { it.emotionalRanking() }
                .firstOrNull()
                ?.first

    // Everything is pinned to an edge on purpose. Balloons floating over the middle of the frame
    // covered the one thing the panel is for — the portrait — so the writing is kept banked against
    // the top and bottom borders and the face is left in the clear between them.
    override val balloons: List<ComicBalloonSpec>
        get() =
            buildList {
                stage.content?.title?.let { title ->
                    add(
                        ComicBalloonSpec(
                            alignment = Alignment.TopStart,
                            widthFraction = 0.82f,
                            offset = DpOffset(10.dp, 10.dp),
                        ) { ComicFadeIn { ComicTag(text = title) } },
                    )
                }

                tone?.let { emotionalTone ->
                    add(
                        ComicBalloonSpec(
                            alignment = Alignment.TopStart,
                            widthFraction = 0.82f,
                            offset = DpOffset(10.dp, 44.dp),
                        ) {
                            ComicFadeIn(delayMillis = 350) {
                                ComicShoutBlock(text = emotionalTone.getTitle())
                            }
                        },
                    )
                }

                // The beats travel together as one bottom-banked stack rather than as separately
                // anchored balloons: anchored apart they had no way of knowing each other's height
                // and overlapped whenever the prose ran long.
                val beats = splitIntoBeats(stage.content?.subtitle.orEmpty(), maxBeats = 2)
                if (beats.isNotEmpty()) {
                    add(
                        ComicBalloonSpec(
                            alignment = Alignment.BottomCenter,
                            widthFraction = 0.94f,
                            offset = DpOffset(0.dp, (-8).dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                beats.forEachIndexed { index, beat ->
                                    ComicFadeIn(
                                        delayMillis = 650 + index * 350,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth(0.94f)
                                                .align(
                                                    if (index % 2 == 0) {
                                                        Alignment.Start
                                                    } else {
                                                        Alignment.End
                                                    },
                                                ),
                                    ) {
                                        ComicCaptionBox(text = beat, align = TextAlign.Start)
                                    }
                                }
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
        val portrait =
            remember(content) { content.mainCharacter?.data?.image?.takeIf { it.isNotBlank() } }

        var revealed by remember { mutableStateOf(!canAnimate) }
        LaunchedEffect(canAnimate) {
            if (canAnimate) {
                delay(PORTRAIT_DELAY_MS.toLong())
                revealed = true
            }
        }
        val portraitAlpha by animateFloatAsState(
            targetValue = if (revealed) 1f else 0f,
            animationSpec = tween(PORTRAIT_FADE_MS),
            label = "comicEmotionPortrait",
        )

        Box(
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        ) {
            portrait?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(portraitAlpha),
                )
            }
        }
    }
}
