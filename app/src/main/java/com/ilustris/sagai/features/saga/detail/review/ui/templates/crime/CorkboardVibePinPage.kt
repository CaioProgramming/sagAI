package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.crime.CorkPin
import com.ilustris.sagai.ui.genre.crime.CorkboardBackground
import com.ilustris.sagai.ui.theme.components.HandwrittenText
import com.ilustris.sagai.ui.theme.components.VibeShapeDrawing

/**
 * The board's "vibe" pin — [VibeShapeDrawing] pinned like a sketch in an evidence folder, with the
 * Expressiveness stage's own caption underneath. Reuses the same shape
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookExpressivenessPage] inks.
 */
class CorkboardVibePinPage(
    override val content: SagaContent,
    private val tone: EmotionalTone,
    private val caption: String?,
) : ReviewPage, CorkboardPinPage {
    override val pageType: ReviewPageType = ReviewPageType.EXPRESSIVENESS

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        CorkPin(
            modifier = modifier.padding(16.dp),
            seed = tone.ordinal,
            pinColor = tone.color,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(170.dp),
            ) {
                VibeShapeDrawing(
                    emotionalTone = tone,
                    strokeWidth = 2.dp,
                    color = tone.color,
                    isAnimated = canAnimate,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                )
                HandwrittenText(
                    text = tone.getTitle(),
                    fontSize = 15.sp,
                    isBold = true,
                    centered = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
                caption?.let {
                    HandwrittenText(
                        text = it,
                        fontSize = 12.sp,
                        centered = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
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
