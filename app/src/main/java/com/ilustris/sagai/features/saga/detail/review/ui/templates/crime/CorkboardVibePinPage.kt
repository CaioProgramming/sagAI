package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.crime.CorkPin
import com.ilustris.sagai.ui.genre.crime.PinBackNote
import com.ilustris.sagai.ui.genre.crime.PinCaption
import com.ilustris.sagai.ui.genre.crime.PinProse
import com.ilustris.sagai.ui.genre.crime.PinSignature
import com.ilustris.sagai.ui.genre.crime.PinTitle
import com.ilustris.sagai.ui.genre.crime.CorkboardBackground
import com.ilustris.sagai.ui.theme.components.VibeShapeDrawing

/** The vibe caption is short by nature; anything longer than this is the stage over-writing. */
private const val CAPTION_MAX_LINES = 5

/**
 * The table's "vibe" card — [VibeShapeDrawing] sketched on paper, with the Expressiveness stage's
 * own caption underneath.
 */
class CorkboardVibePinPage(
    override val content: SagaContent,
    private val tone: EmotionalTone,
    private val caption: String?,
) : ReviewPage, CorkboardPinPage {
    override val pageType: ReviewPageType = ReviewPageType.EXPRESSIVENESS
    override val pinSize: CorkPinSize = CorkPinSize.NOTE

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        CorkPin(
            modifier = modifier,
            seed = tone.ordinal,
            pinColor = tone.color,
        ) { ink ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                VibeShapeDrawing(
                    emotionalTone = tone,
                    strokeWidth = 2.dp,
                    color = tone.color,
                    isAnimated = canAnimate,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                )
                PinTitle(
                    text = tone.getTitle(),
                    ink = ink,
                    isAnimated = canAnimate,
                    modifier = Modifier.padding(top = 6.dp),
                )
                caption?.let {
                    PinProse(
                        text = it,
                        ink = ink,
                        centered = true,
                        maxLines = CAPTION_MAX_LINES,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CorkboardBackground(modifier)
    }
}
