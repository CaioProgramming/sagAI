package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.presentation.model.IntroductionType
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun IntroductionOverlay(
    introduction: SagaMilestone.Introduction,
    saga: SagaContent,
    message: String?,
    onComplete: () -> Unit,
) {
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
            delay(1.seconds)
            showContent = true
            delay(500)
            showTypewriter = true
        }
    }

    LaunchedEffect(animationComplete) {
        if (animationComplete) {
            delay(5.seconds)
            showContent = false
            delay(1.seconds)
            onComplete()
        }
    }

    AnimatedVisibility(
        visible = showContent,
        enter = fadeIn(tween(800, easing = EaseInOut)),
        exit = fadeOut(tween(1200)),
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .statusBarsPadding(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .padding(32.dp)
                    .animateContentSize()
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = genre.headerFont(),
                        textAlign = TextAlign.Center,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            )

            message?.let {
                AnimatedVisibility(
                    visible = showTypewriter,
                    enter = fadeIn(tween(300)),
                ) {
                    SimpleTypewriterText(
                        text = message,
                        duration = 5.seconds,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp,
                            ),
                        textAlign = TextAlign.Center,
                        onAnimationFinished = {
                            animationComplete = true
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}
