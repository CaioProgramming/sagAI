package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.playthrough.AnimatedPlaytimeCounter
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.crime.CrimeBackground
import com.ilustris.sagai.ui.genre.crime.CrimeBubbleFrame
import kotlin.time.Duration.Companion.seconds

/**
 * The playtime attachment — sent right after the Playstyle content bubble, same side. Reuses
 * [AnimatedPlaytimeCounter], the same stat [com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookPlaystylePage]
 * shows, sized down to a card rather than a full page.
 */
class CrimePlaystyleStatPage(
    override val content: SagaContent,
    private val isMe: Boolean,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.PLAYSTYLE

    /** No typing to wait for, just the pop-in plus the counter's own count-up. */
    override val estimatedRevealDurationMs: Long = 1400L

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val accent = genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary

        CrimeBubbleFrame(
            isMe = isMe,
            genre = genre,
            useSpeechShape = false,
            canAnimate = canAnimate,
            modifier = modifier,
        ) { contentColor ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(160.dp).padding(vertical = 4.dp),
            ) {
                AnimatedPlaytimeCounter(
                    playtimeMs = content.data.playTimeMs,
                    label = stringResource(R.string.playtime_title),
                    animationDuration = 1.2.seconds,
                    isAnimated = canAnimate,
                    textStyle = MaterialTheme.typography.titleLarge.copy(color = contentColor),
                    labelStyle =
                        MaterialTheme.typography.labelSmall.copy(
                            fontStyle = FontStyle.Italic,
                            color = accent,
                        ),
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CrimeBackground(modifier)
    }
}
