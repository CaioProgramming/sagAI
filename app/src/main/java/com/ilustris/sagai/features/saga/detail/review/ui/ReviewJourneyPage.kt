package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class ReviewJourneyPage(
    override val content: SagaContent,
    private val journeyReview: ReviewText,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.JOURNEY

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val chapters = content.flatChapters().map { it.data }
        var showImages by remember {
            mutableStateOf(!canAnimate)
        }

        var showShareButton by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(showImages) {
            if (showImages) {
                delay(2.seconds)
                showShareButton = true
            }
        }

        val genre = content.data.genre

        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .animateContentSize(tween(2000))
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                journeyReview.title?.let {
                    SimpleTypewriterText(
                        it,
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            ),
                        duration = 2.seconds,
                        easing = FastOutSlowInEasing,
                        onAnimationFinished = {
                            showImages = true
                        },
                    )
                }

                AnimatedVisibility(
                    showImages,
                    enter = fadeIn(tween(800)),
                ) {
                    JourneyCollage(
                        saga = content,
                        chapters = chapters,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                    )
                }

                journeyReview.subtitle?.let {
                    SimpleTypewriterText(
                        it,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                                textAlign = TextAlign.Center,
                            ),
                        duration = 5.seconds,
                    )
                }

                AnimatedVisibility(showShareButton, modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            onAction(ReviewAction.Share(ShareType.HISTORY))
                        },
                        colors =
                            ButtonDefaults.elevatedButtonColors().copy(
                                containerColor = MaterialTheme.colorScheme.onBackground,
                                contentColor = genre.color,
                            ),
                    ) {
                        Text(stringResource(R.string.share))
                    }
                }
            }
        }
    }
}
