package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/**
 * The verdict, in words alone.
 *
 * It briefly carried an assembled shot of the cast, which was a mistake: the farewells that come
 * straight after are nothing *but* portraits of those same characters, so the page showed each of
 * them twice within a screen of each other. Words here, faces there — the beats stop competing and
 * neither has to repeat the other.
 */
class ComicConclusionPanel(
    override val content: SagaContent,
    private val conclusion: ReviewText,
) : ReviewPage,
    ComicPanelPage {
    override val pageType: ReviewPageType = ReviewPageType.CONCLUSION

    override val panelSpan = PanelSpan.BAND

    override val hasFrame = false

    override val estimatedRevealDurationMs: Long = 6000L

    override val balloons: List<ComicBalloonSpec>
        get() =
            buildList {
                conclusion.title?.let { title ->
                    add(
                        ComicBalloonSpec(
                            alignment = Alignment.TopCenter,
                            widthFraction = 0.7f,
                            offset = DpOffset(0.dp, 10.dp),
                        ) { ComicFadeIn { ComicShoutBlock(text = title) } },
                    )
                }

                conclusion.subtitle?.let { subtitle ->
                    add(
                        ComicBalloonSpec(
                            alignment = Alignment.BottomCenter,
                            widthFraction = 0.82f,
                            offset = DpOffset(0.dp, (-10).dp),
                        ) {
                            ComicFadeIn(delayMillis = 500) { ComicCaptionBox(text = subtitle) }
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
