package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.lighter
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun MilestoneOverlay(
    milestone: SagaMilestone,
    saga: SagaContent,
) {
    val genre = saga.data.genre

    var showContent by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            delay(2.seconds)
            showContent = true
        }
    }

    AnimatedContent(milestone, transitionSpec = {
        fadeIn() togetherWith fadeOut(tween(700)) + scaleOut()
    }) {
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            tween(500, easing = EaseInExpo),
                        ),
            ) {
                Image(
                    painterResource(R.drawable.ic_spark),
                    null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background),
                    modifier =
                        Modifier
                            .reactiveShimmer(
                                true,
                                genre.shimmerColors(),
                                repeatMode = RepeatMode.Restart,
                                targetValue = 100f,
                            ).size(32.dp),
                )

                if (showContent) {
                    Text(
                        stringResource(milestone.title),
                        style =
                            MaterialTheme.typography.titleSmall.copy(
                                fontFamily = genre.bodyFont(),
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                    )

                    if (milestone is SagaMilestone.NewCharacter) {
                        if (milestone.character.image.isNotEmpty()) {
                            CharacterAvatar(
                                milestone.character,
                                genre = genre,
                                modifier = Modifier.size(50.dp),
                            )
                        }
                    }

                    Text(
                        milestone.subtitle,
                        style =
                            MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = genre.headerFont(),
                                brush =
                                    Brush.verticalGradient(
                                        listOf(
                                            genre.color,
                                            genre.color.lighter(),
                                            MaterialTheme.colorScheme.onBackground,
                                        ),
                                    ),
                                shadow = Shadow(genre.color, blurRadius = 15f),
                                textAlign = TextAlign.Center,
                            ),
                        modifier = Modifier.reactiveShimmer(true, genre.shimmerColors()),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun NewChapterPreview() {
    val genre = Genre.CYBERPUNK
    val sagaContent = SagaContent(Saga(genre = genre))
    SharedTransitionScope {
        AnimatedContent(sagaContent) {
            MilestoneOverlay(
                milestone =
                    SagaMilestone.ChapterFinished(
                        Chapter(
                            title = "The Dragon's Awakening",
                            actId = 0,
                        ),
                    ),
                saga = sagaContent,
            )
        }
    }
}
