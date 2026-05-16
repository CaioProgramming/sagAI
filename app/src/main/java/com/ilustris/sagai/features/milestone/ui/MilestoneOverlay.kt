package com.ilustris.sagai.features.milestone.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterYearbookItem
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.findChapter
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.milestone.presentation.MilestoneViewModel
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.playthrough.CounterText
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.DefaultOverlay
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.IntroductionOverlay
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.LoadingMilestoneOverlay
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.components.mascot.MascotEmotionFace
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MilestoneOverlay(
    milestone: SagaMilestone,
    saga: SagaMetadata,
    isLoading: Boolean = false,
    reasoningChunk: String? = null,
    onDismiss: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val viewModel: MilestoneViewModel = hiltViewModel()
    val congratsMessage by viewModel.congratsMessage.collectAsState()
    val genre = saga.data.genre

    with(sharedTransitionScope) {
        AnimatedContent(milestone, transitionSpec = {
            fadeIn(tween(500)) togetherWith fadeOut(tween(200))
        }) {
            val sparkModifier =
                Modifier.sharedElement(
                    rememberSharedContentState(
                        key = "saga_${saga.data.id}_spark",
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            val titleModifier =
                Modifier.sharedElement(
                    rememberSharedContentState(
                        key = "saga_${saga.data.id}_title",
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            when (it) {
                is SagaMilestone.CurrentObjective -> {
                    Box {}
                }

                is SagaMilestone.Introduction -> {
                    IntroductionOverlay(
                        it,
                        saga.data,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    ) {
                        onDismiss()
                    }
                }

                is SagaMilestone.Loading -> {
                    LoadingMilestoneOverlay(
                        saga.data,
                        sparkModifier,
                        titleModifier,
                        contentReasoning = reasoningChunk,
                    )
                }

                else -> {
                    DefaultOverlay(
                        stringResource(it.title),
                        it.subtitle,
                        it.message ?: congratsMessage,
                        genre,
                        sparkModifier,
                        extraContent = { it.extraContent() },
                        onDismiss = onDismiss,
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clear()
        }
    }
}

@Composable
fun MilestoneBadge(
    count: Int,
    labelText: String,
    brush: Brush,
    glowBlurRadius: Float,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    val shape = genre.bubble(BubbleTailAlignment.BottomRight, 0.dp, 0.dp, true)

    var showContent by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(700)
        showContent = true
    }
    AnimatedVisibility(
        showContent,
        enter = scaleIn(tween(600, easing = EaseInBounce)),
        exit = scaleOut(),
    ) {
        val resolvedColor = MaterialTheme.colorScheme.primary
        ConstraintLayout(
            modifier =
                modifier
                    .padding(8.dp)
                    .clip(shape)
                    .dropShadow(shape, {
                        color = resolvedColor
                        radius = glowBlurRadius
                    })
                    .background(brush, shape),
        ) {
            val (label, counter) = createRefs()
            Text(
                labelText,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                    ),
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp, 4.dp)
                        .constrainAs(label) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
            )

            CounterText(
                count,
                textStyle =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                        brush = brush,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                modifier =
                    Modifier
                        .padding(2.dp)
                        .constrainAs(counter) {
                            top.linkTo(label.bottom)
                            start.linkTo(label.start)
                            end.linkTo(label.end)
                            width = Dimension.fillToConstraints
                        }.background(
                            MaterialTheme.colorScheme.background,
                            shape,
                        ).padding(4.dp)
                        .reactiveShimmer(
                            true,
                            repeatMode = RepeatMode.Restart,
                        ),
            )
        }
    }
}

@Composable
fun NewEventContent(
    saga: SagaContent,
    timelineId: Int,
    emotionMascot: String?,
) {
    val genre = saga.data.genre
    val brush =
        Brush.horizontalGradient(
            MaterialTheme.colorScheme.primary.darkerPalette(
                factor = .25f,
            ),
        )
    val event = saga.findTimeline(timelineId)
    event?.let {
        val stats = event.statsSummary()
        val topTone = event.data.emotionalTone
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            topTone?.let {
                MascotEmotionFace(
                    emotionMascot,
                    topTone,
                    modifier = Modifier.size(64.dp),
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .animateContentSize()
                        .fillMaxWidth(),
            ) {
                items(stats) {
                    MilestoneBadge(
                        it.second,
                        stringResource(it.first),
                        brush,
                        10f,
                        genre,
                        modifier = Modifier.fillParentMaxWidth(.5f),
                    )
                }
            }
        }
    }
}

@Composable
fun NewChapterContent(
    saga: SagaContent,
    chapterId: Int,
) {
    val chapter = saga.findChapter(chapterId)
    val genre = saga.data.genre
    chapter?.let {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val characters =
                it.fetchCharacters(saga).filterNotNull()

            LazyRow(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp,
                    ),
            ) {
                items(characters) { character ->
                    CharacterYearbookItem(
                        character.data,
                        genre,
                        imageModifier =
                            Modifier
                                .size(50.dp)
                                .reactiveShimmer(true),
                    )
                }
            }
        }
    }
}

@Composable
fun NewCharacterContent(
    saga: Saga,
    character: Character,
) {
    val genre = saga.genre
    character.let {
        if (it.image.isNotBlank()) {
            CharacterAvatar(
                it,
                genre = genre,
                modifier =
                    Modifier
                        .size(200.dp)
                        .levitate(),
            )
        }
    }
}
