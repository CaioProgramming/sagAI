package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.theme.components.VibeShapeDrawing

/**
 * The "emotional vibe" attachment — sent right after the Expressiveness content bubble, same side.
 * Reuses [VibeShapeDrawing], the same shape [com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookExpressivenessPage]
 * inks, drawn small enough to read as a photo-sized card rather than a full page.
 */
class CrimeVibeStatPage(
    override val content: SagaContent,
    private val tone: EmotionalTone,
    private val isMe: Boolean,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.EXPRESSIVENESS

    /** No typing to wait for, just the pop-in plus a beat for the shape to trace itself. */
    override val estimatedRevealDurationMs: Long = 1200L

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre

        CrimeBubbleFrame(
            isMe = isMe,
            genre = genre,
            useSpeechShape = false,
            canAnimate = canAnimate,
            modifier = modifier,
        ) { contentColor ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.width(160.dp).padding(vertical = 4.dp),
            ) {
                VibeShapeDrawing(
                    emotionalTone = tone,
                    strokeWidth = 2.dp,
                    color = contentColor,
                    isAnimated = canAnimate,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                )

                Text(
                    text = tone.getTitle(),
                    fontWeight = FontWeight.Bold,
                    color = tone.color,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CrimeBackground(modifier)
    }
}
