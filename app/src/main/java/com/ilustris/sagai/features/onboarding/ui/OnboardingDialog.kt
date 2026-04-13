@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.onboarding.data.OnboardingPage
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.premium.PremiumTitle
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.animations.chromaticAberration
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.zoomAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun OnboardingDialog(
    type: OnboardingType,
    genre: Genre? = null,
    saga: Saga? = null,
    force: Boolean = false,
    onDismiss: () -> Unit = {},
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val uiState by viewModel.onboardingState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkOnboarding(type, genre, saga, force)
    }

    if (uiState is OnboardingUiState.Content && uiState.type == type) {
        OnboardingContentSheet(
            state = uiState as OnboardingUiState.Content,
            onDismiss = {
                viewModel.markAsSeen(type)
                onDismiss()
                viewModel.clearState()
            },
        )
    } else if (uiState is OnboardingUiState.Error && uiState.type == type) {
        SideEffect {
            onDismiss()
        }
    }

    AnimatedVisibility(
        uiState is OnboardingUiState.Loading,
        enter =
            fadeIn(
                tween(
                    durationMillis = 800,
                    delayMillis = 500,
                ),
            ),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            Modifier
                .fillMaxSize(),
        ) {
            Box(
                Modifier
                    .reactiveShimmer(true)
                    .background(
                        fadeGradientBottom(
                            MaterialTheme.colorScheme.primary,
                        ),
                    )
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ic_spark),
                    null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .size(50.dp)
                            .levitate()
                            .reactiveShimmer(
                                true,
                                shimmerColors = holographicGradient,
                                targetValue = 100f,
                            ),
                )
            }
        }
    }
}

@Composable
private fun OnboardingContentSheet(
    state: OnboardingUiState.Content,
    genre: Genre? = null,
    onDismiss: () -> Unit,
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val pagerState = rememberPagerState { state.pages.size }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val shape =
        RoundedCornerShape(
            topStart = CornerSize(15.dp),
            topEnd = CornerSize(15.dp),
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
        )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = shape,
        dragHandle = null,
        containerColor = Color.Transparent,
    ) {
        Box(
            modifier =
                Modifier
                    .padding(top = 16.dp)
                    .background(MaterialTheme.colorScheme.background, shape)
                    .clip(shape)
                    .fillMaxWidth()
                    .fillMaxHeight(),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
            ) { pageIndex ->
                val uiPage = state.pages.getOrNull(pageIndex) ?: state.pages.lastOrNull()
                uiPage?.background?.invoke()
            }

            Column(
                modifier =
                    Modifier
                        .background(fadeGradientBottom())
                        .fillMaxSize(),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painterResource(R.drawable.round_close_24),
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        var currentIcon by remember {
                            mutableStateOf(genre?.icon ?: Genre.entries.random().icon)
                        }

                        LaunchedEffect(pagerState.currentPage) {
                            currentIcon = genre?.icon
                                ?: Genre.entries
                                    .filter {
                                        it.icon != currentIcon
                                    }.random()
                                    .icon
                        }

                        repeat(state.pages.size) { iteration ->
                            val isSelected = pagerState.currentPage == iteration
                            val size by animateDpAsState(
                                targetValue = if (isSelected) 24.dp else 6.dp,
                                animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                                label = "dot_size",
                            )
                            val color by animateColorAsState(
                                targetValue =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.onBackground
                                    } else {
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                    },
                                label = "dot_color",
                            )

                            AnimatedContent(isSelected, transitionSpec = {
                                fadeIn(
                                    tween(
                                        100,
                                        easing = EaseIn,
                                    ),
                                ) + scaleIn(tween(600)) togetherWith
                                    fadeOut() + scaleOut(tween(600))
                            }) {
                                if (it) {
                                    Icon(
                                        painter = painterResource(currentIcon),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier =
                                            Modifier
                                                .clip(CircleShape)
                                                .size(size),
                                    )
                                } else {
                                    Box(
                                        modifier =
                                            Modifier
                                                .padding(4.dp)
                                                .size(size, 6.dp)
                                                .clip(CircleShape)
                                                .background(color),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                val currentPage =
                    state.pages.getOrNull(pagerState.currentPage) ?: state.pages.lastOrNull()

                currentPage?.let { uiPage ->
                    val context = LocalContext.current

                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            AnimatedContent(uiPage, transitionSpec = {
                                slideInVertically { -it } +
                                    fadeIn(
                                        tween(
                                            700,
                                            easing = EaseIn,
                                        ),
                                    ) togetherWith
                                    fadeOut()
                            }) {
                                it.content()
                            }
                        }

                        uiPage.primaryButton?.let { button ->
                            Button(
                                onClick = {
                                    if (button.action is OnboardingAction.Next) {
                                        if (pagerState.currentPage < state.pages.size - 1) {
                                            scope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        } else {
                                            onDismiss()
                                        }
                                    } else {
                                        viewModel.handleAction(
                                            button.action,
                                            context as? MainActivity,
                                        )
                                    }
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                                shape = MaterialTheme.shapes.large,
                            ) {
                                AnimatedContent(button) {
                                    Text(
                                        text = it.text.uppercase(),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    )
                                }
                            }
                        }

                        uiPage.secondaryButton?.let { button ->
                            TextButton(
                                onClick = {
                                    when (button.action) {
                                        is OnboardingAction.Skip -> {
                                            scope.launch {
                                                pagerState.animateScrollToPage(state.pages.size - 1)
                                            }
                                        }

                                        is OnboardingAction.Dismiss -> {
                                            onDismiss()
                                        }

                                        else -> {
                                            viewModel.handleAction(
                                                button.action,
                                                context as? MainActivity,
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                AnimatedContent(button) {
                                    Text(
                                        text = it.text.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStandardContent(page: OnboardingPage) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = page.title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = page.description,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}

@Composable
fun OnboardingMascotContent(
    mascotUrl: String?,
    genre: Genre? = null,
    color: Color? = null,
) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        StarryTextPlaceholder(
            modifier =
                Modifier.reactiveShimmer(
                    true,
                    (color ?: holographicGradient.first()).darkerPalette(),
                ),
        )
        mascotUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .size(240.dp)
                        .levitate(true),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
fun CinematicBackground(config: GenreVisualConfig?) {
    AsyncImage(
        model = config?.imageUrl ?: emptyString(),
        contentDescription = null,
        modifier =
            Modifier
                .fillMaxSize()
                .zoomAnimation(),
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun SparkBackground(
    colors: List<Color> = emptyList(),
    customIcon: Int? = null,
) {
    Box(
        Modifier
            .fillMaxSize()
            .reactiveShimmer(true, colors, repeatMode = RepeatMode.Restart, targetValue = 700f),
    ) {
        StarryTextPlaceholder(
            modifier =
                Modifier
                    .fillMaxSize()
                    .gradientFill(Brush.verticalGradient(colors)),
        )
        Icon(
            painter = painterResource(customIcon ?: R.drawable.ic_spark),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .size(120.dp)
                    .align(Alignment.Center)
                    .levitate(true)
                    .chromaticAberration(true),
        )
    }
}

@Composable
fun StarfieldBackground() {
    StarryTextPlaceholder(
        modifier = Modifier.fillMaxSize(),
        starColor = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
fun PremiumBackground() {
    Box(
        modifier =
            Modifier
                .reactiveShimmer(true, holographicGradient.plus(Color.Transparent))
                .fillMaxSize(),
    ) {
        StarryTextPlaceholder(
            modifier = Modifier.fillMaxSize(),
            starColor = Color.White,
        )
        PremiumTitle(
            titleStyle = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .levitate(true)
                    .chromaticAberration(true, 5f, 15f),
        )
    }
}

@Composable
fun MorphingGenresBackground(
    visualConfigs: Map<Genre, GenreVisualConfig?> = emptyMap(),
    onSwitchGenre: (Genre) -> Unit = {},
) {
    val genres = remember { Genre.entries.shuffled() }
    var currentGenreIndex by remember { mutableIntStateOf(0) }
    var nextGenreIndex by remember { mutableIntStateOf(1) }

    val currentGenre = genres[currentGenreIndex]
    val nextGenre = genres[nextGenreIndex]

    val wipeProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3.seconds)
            val hasConfigs =
                visualConfigs.containsKey(currentGenre) &&
                    visualConfigs.containsKey(nextGenre)

            if (hasConfigs) {
                wipeProgress.animateTo(1f, tween(1500))

                // Swap
                currentGenreIndex = nextGenreIndex
                nextGenreIndex = (nextGenreIndex + 1) % genres.size

                onSwitchGenre(genres[currentGenreIndex])
                wipeProgress.snapTo(0f)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GenreImage(
            genre = currentGenre,
            config = visualConfigs[currentGenre],
            modifier =
                Modifier
                    .fillMaxSize()
                    .zoomAnimation(),
        )

        GenreImage(
            genre = nextGenre,
            config = visualConfigs[nextGenre],
            modifier =
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        if (wipeProgress.value > 0) {
                            drawContent()
                        }
                    }
                    .graphicsLayer {
                        clip = true
                        shape =
                            GenericShape { size, _ ->
                                addRect(
                                    Rect(
                                        0f,
                                        0f,
                                        size.width * wipeProgress.value,
                                        size.height,
                                    ),
                                )
                            }
                    }
                    .zoomAnimation(),
        )
    }
}

@Composable
private fun GenreImage(
    genre: Genre,
    config: GenreVisualConfig?,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = config?.imageUrl ?: genre.icon,
        contentDescription = null,
        modifier =
            modifier
                .effectForGenre(genre, config)
                .selectiveColorHighlight(genre.selectiveHighlight(config)),
        contentScale = ContentScale.Crop,
    )
}
