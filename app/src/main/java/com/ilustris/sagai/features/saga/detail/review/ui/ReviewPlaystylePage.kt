package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.playthrough.AnimatedPlaytimeCounter
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText

class ReviewPlaystylePage(
    override val content: SagaContent,
    val playstyle: ReviewText,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.PLAYSTYLE

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        var showText by remember { mutableStateOf(false) }
        content.data.genre
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .animateContentSize(tween(1500, easing = FastOutSlowInEasing)),
            ) {
                AnimatedPlaytimeCounter(
                    playtimeMs = content.data.playTimeMs,
                    label = playstyle.title ?: stringResource(R.string.playtime_title),
                    textStyle =
                        MaterialTheme.typography.displayMedium.copy(
                            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                            fontWeight = FontWeight.Bold,
                            shadow =
                                Shadow(
                                    MaterialTheme.colorScheme.primary,
                                    blurRadius = 1f,
                                    offset = Offset(5f, 0f),
                                ),
                        ),
                    labelStyle =
                        MaterialTheme.typography.labelMedium.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        ),
                    onAnimationFinished = {
                        showText = true
                    },
                )

                if (showText) {
                    playstyle.subtitle?.let {
                        Text(
                            it,
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                ),
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}
