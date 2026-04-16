package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.ilustris.sagai.features.saga.chat.presentation.model.IntroductionType
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.bodyFont
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun IntroductionOverlay(
    introduction: SagaMilestone.Introduction,
    saga: SagaContent,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    onComplete: () -> Unit,
) {
    val message = introduction.introduction
    val genre = saga.data.genre
    var showContent by remember { mutableStateOf(false) }
    var showTypewriter by remember { mutableStateOf(false) }
    var animationComplete by remember { mutableStateOf(false) }

    val title =
        if (introduction.type == IntroductionType.ACT) {
            stringResource(
                R.string.act_title_template,
                introduction.number,
            )
        } else {
            stringResource(R.string.chapter_title_template, introduction.number)
        }

    LaunchedEffect(message) {
        if (message != null) {
            showContent = true
            delay(1.seconds)
            showTypewriter = true
            delay(5.seconds)
            animationComplete = true
        }
    }

    LaunchedEffect(animationComplete) {
        if (animationComplete) {
            delay(2.seconds)
            onComplete()
        }
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .animateContentSize()
                    .verticalScroll(rememberScrollState()),
        ) {
            AnimatedVisibility(showContent) {
                Text(
                    text = "$title ${introduction.titleText}",
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = genre.bodyFont(),
                            textAlign = TextAlign.Center,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            with(sharedTransitionScope) {
                genre.stylisedText(
                    text = saga.data.title,
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .sharedElement(
                                rememberSharedContentState(
                                    key = "saga_${saga.data.id}_title",
                                ),
                                animatedVisibilityScope = animatedVisibilityScope,
                            ),
                )
            }

            message?.let {
                AnimatedVisibility(
                    visible = showTypewriter,
                    enter = fadeIn(tween(300)),
                ) {
                    Text(
                        it,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                            ),
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                    )
                }
            }
        }
    }
}
