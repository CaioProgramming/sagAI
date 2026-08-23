package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.playthrough.AnimatedPlaytimeCounter
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.comic.ComicBalloonSpec
import com.ilustris.sagai.ui.genre.comic.splitIntoBeats
import com.ilustris.sagai.ui.genre.comic.ComicCaptionBox
import com.ilustris.sagai.ui.genre.comic.ComicFadeIn

/**
 * How the playstyle prose is divided: the first beat stays on the counter, the rest travel across
 * the chapter plates that follow. [ComicReviewExperience] splits with the same count.
 */
internal const val PLAYSTYLE_BEATS = 3

/**
 * The playtime figure, counting up inside its own frame.
 *
 * This beat had gone missing entirely: the playstyle stage was being spent on a plate of chapter
 * art and the number itself — the one hard fact in the whole review — was never drawn. It gets a
 * frame of its own here, with the stage's prose kept to a caption so the counter stays the subject.
 */
class ComicPlaytimePanel(
    override val content: SagaContent,
    private val playstyle: ReviewText,
) : ReviewPage,
    ComicPanelPage {
    override val pageType: ReviewPageType = ReviewPageType.PLAYSTYLE

    // A short strip with no frame, like the narration beat before it: the counter is the whole
    // content, and boxing it left most of a panel empty around a number.
    override val panelSpan = PanelSpan.BAND

    override val hasFrame = false

    override val estimatedRevealDurationMs: Long = 6000L

    /**
     * The stage's opening line stays with the counter. Its whole subject is the playtime and the
     * hours the saga was played at — sending all of it off to ride the chapter plates left the
     * number standing alone with nothing saying what it meant.
     */
    override val balloons: List<ComicBalloonSpec>
        get() =
            playstyle.subtitle
                ?.let { splitIntoBeats(it, maxBeats = PLAYSTYLE_BEATS).firstOrNull() }
                ?.let { lead ->
                    listOf(
                        ComicBalloonSpec(
                            alignment = Alignment.BottomCenter,
                            widthFraction = 0.78f,
                            offset = DpOffset(0.dp, (-4).dp),
                        ) { ComicFadeIn(delayMillis = 1200) { ComicCaptionBox(text = lead) } },
                    )
                } ?: emptyList()

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        Box(
            modifier.fillMaxSize().padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedPlaytimeCounter(
                playtimeMs = content.data.playTimeMs,
                label = playstyle.title ?: stringResource(R.string.playtime_title),
                isAnimated = canAnimate,
                horizontalAlignment = Alignment.CenterHorizontally,
                textStyle =
                    MaterialTheme.typography.displayMedium.copy(
                        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                        fontWeight = FontWeight.Black,
                    ),
                labelStyle =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    ),
            )
        }
    }
}
