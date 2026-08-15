package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.playthrough.AnimatedPlaytimeCounter
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** "The Playstyle" page — the same playtime stat Default shows, inked in the book's own type. */
class BookPlaystylePage(
    override val content: SagaContent,
    private val playstyle: ReviewText,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.PLAYSTYLE

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
        val ink = LocalContentColor.current
        var showText by remember { mutableStateOf(false) }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(28.dp)
                    .animateContentSize(tween(1500, easing = FastOutSlowInEasing)),
        ) {
            AnimatedPlaytimeCounter(
                playtimeMs = content.data.playTimeMs,
                label = playstyle.title ?: stringResource(R.string.playtime_title),
                animationDuration = 2.seconds,
                textStyle =
                    MaterialTheme.typography.headlineLarge.copy(
                        color = ink,
                    ),
                labelStyle =
                    MaterialTheme.typography.labelMedium.copy(
                        fontStyle = FontStyle.Italic,
                        color = accent,
                    ),
                onAnimationFinished = {
                    showText = true
                },
            )

            AnimatedVisibility(showText, modifier = Modifier.padding(top = 16.dp)) {
                playstyle.subtitle?.let {
                    SimpleTypewriterText(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge.copy(color = ink.copy(alpha = 0.85f)),
                        textAlign = TextAlign.Center,
                        duration = (it.length * 16).coerceIn(800, 4000).milliseconds,
                        isAnimated = canAnimate,
                    )
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        BookBackground(modifier)
    }
}
