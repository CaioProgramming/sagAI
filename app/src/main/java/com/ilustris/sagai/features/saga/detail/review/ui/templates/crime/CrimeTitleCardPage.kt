package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.components.HandwrittenText
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.morphingGradient
import kotlin.time.Duration.Companion.milliseconds

/**
 * Opens the thread before any bubble appears — the saga's title, handwritten and centered, the
 * same charm [com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookCoverPage]
 * gives the continuous-scroll journal. Crime has no dedicated cover art in a chat thread, so this
 * stands in for it: a beat of stillness before the conversation starts typing in.
 */
class CrimeTitleCardPage(
    override val content: SagaContent,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.INTRO

    private val animationDurationMs = 2600

    /** Handwriting settle time, then a short beat before the first bubble pops in. */
    override val estimatedRevealDurationMs: Long = animationDurationMs + 400L

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent =
            content.data.genre
                .compiledColorPalette()
                .firstOrNull() ?: MaterialTheme.colorScheme.primary

        Box(
            modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            contentAlignment = Alignment.Center,
        ) {
            content.data.genre.stylisedText(
                text = content.data.title,
                modifier =
                    Modifier.fillMaxWidth().gradientFill(
                        Brush.verticalGradient(
                            morphingGradient(),
                        ),
                    ),
            )
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CrimeBackground(modifier.fillMaxSize())
    }
}
