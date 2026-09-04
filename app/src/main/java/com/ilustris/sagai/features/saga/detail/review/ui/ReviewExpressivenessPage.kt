package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.theme.components.mascot.BlobMascot
import com.ilustris.sagai.ui.theme.components.mascot.rememberMascotExpression
import com.ilustris.sagai.ui.theme.components.mascot.rememberTiltLook
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shimmerize
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReviewExpressivenessPage(
    private val stage: ReviewStage,
    override val content: SagaContent,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.EXPRESSIVENESS

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        content.data.genre
        var showText by remember {
            mutableStateOf(false)
        }
        var showButton by remember {
            mutableStateOf(false)
        }

        // The blob loops instead of finishing a stroke, so the caption is timed off the page
        // appearing rather than off VibeShapeDrawing's onFinishDraw.
        LaunchedEffect(Unit) {
            delay(BLOB_SETTLE_DELAY)
            showText = true
        }

        LaunchedEffect(showText) {
            if (showText) {
                delay(2.seconds)
                showButton = true
            }
        }

        val emotionalTone =
            remember {
                content
                    .flatEvents()
                    .map { it.emotionalRanking() }
                    .first()
                    .first()
            }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .animateContentSize(
                        tween(1200, easing = LinearOutSlowInEasing),
                    ).fillMaxWidth(),
        ) {
            emotionalTone.first?.let {
                val tilt = rememberTiltLook(enabled = canAnimate)
                BlobMascot(
                    expression = rememberMascotExpression(it),
                    color = MaterialTheme.colorScheme.primary,
                    eyeColor = MaterialTheme.colorScheme.background,
                    look = { tilt.value },
                    modifier =
                        Modifier
                            .size(BLOB_SIZE)
                            .reactiveShimmer(
                                true,
                                shimmerColors = it.color.shimmerize(),
                            ),
                )
            }

            AnimatedVisibility(showText, modifier = Modifier.padding(16.dp)) {
                stage.content?.let {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        it.title?.let { title ->
                            Text(
                                title,
                                style =
                                    MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                    ),
                            )
                        }

                        emotionalTone.first?.let {
                            AutoResizeText(
                                it.getTitle(),
                                style =
                                    MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        shadow =
                                            Shadow(
                                                it.color,
                                                offset = Offset(2f, 2f),
                                                blurRadius = 10f,
                                            ),
                                    ),
                                modifier =
                                    Modifier.levitate(),
                            )
                        }

                        Text(
                            it.subtitle ?: emptyString(),
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.Center,
                                ),
                        )
                    }
                }
            }

            AnimatedVisibility(showButton, modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        onAction(ReviewAction.Share(ShareType.EMOTIONS))
                    },
                    colors =
                        ButtonDefaults.elevatedButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text(stringResource(R.string.share))
                }
            }
        }
    }
}

private val BLOB_SIZE = 200.dp
private val BLOB_SETTLE_DELAY = 1.5.seconds
