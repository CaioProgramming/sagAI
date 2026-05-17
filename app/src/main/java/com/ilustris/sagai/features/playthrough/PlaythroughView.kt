package com.ilustris.sagai.features.playthrough

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PlaythroughView(
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: PlaythroughViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPlaythroughData()
    }

    Scaffold(
        topBar = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(8.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(R.drawable.ic_back_left),
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            AnimatedContent(
                targetState = state,
                label = "PlaythroughContent",
            ) { currentState ->
                when (currentState) {
                    is PlaythroughUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            StarryLoader(
                                true,
                                "Analisando sua jornada...",
                                textStyle =
                                    MaterialTheme.typography.labelMedium.copy(
                                        textAlign = TextAlign.Center,
                                    ),
                            )
                        }
                    }

                    is PlaythroughUiState.Empty -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                currentState.message,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(32.dp),
                            )
                        }
                    }

                    is PlaythroughUiState.Success -> {
                        val genres = currentState.completedSagas.map { it.data.genre }
                        var currentGenreIndex by remember { mutableIntStateOf(0) }
                        val currentGenre =
                            genres.getOrElse(currentGenreIndex) { Genre.entries.first() }

                        LaunchedEffect(Unit) {
                            if (genres.isNotEmpty()) {
                                while (true) {
                                    delay(5.seconds)
                                    currentGenreIndex = (currentGenreIndex + 1) % genres.size
                                }
                            }
                        }

                        val color by animateColorAsState(
                            targetValue = currentGenre.color,
                            animationSpec = tween(durationMillis = 1000),
                            label = "GenreColor",
                        )

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(24.dp)
                                    .animateContentSize(tween(500, easing = EaseIn)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_spark),
                                tint = color,
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .size(80.dp)
                                        .reactiveShimmer(
                                            true,
                                            repeatMode = RepeatMode.Restart,
                                        ),
                            )

                            AnimatedContent(
                                targetState = currentGenre,
                                transitionSpec = {
                                    fadeIn(tween(200, easing = FastOutSlowInEasing)) togetherWith
                                        fadeOut(tween(800, easing = EaseIn))
                                },
                                label = "GenreTitle",
                            ) {
                                val genreColor by animateColorAsState(
                                    targetValue = it.color,
                                    animationSpec = tween(durationMillis = 1000),
                                    label = "TextGenreColor",
                                )
                                Text(
                                    text = currentState.data.title,
                                    style =
                                        MaterialTheme.typography.headlineMedium.copy(
                                            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                            shadow =
                                                Shadow(
                                                    genreColor,
                                                    blurRadius = 20f,
                                                ),
                                        ),
                                    textAlign = TextAlign.Center,
                                    modifier =
                                        Modifier
                                            .padding(horizontal = 16.dp)
                                            .reactiveShimmer(
                                                true,
                                                targetValue = 400f,
                                                repeatMode = RepeatMode.Restart,
                                            ),
                                )
                            }

                            AnimatedPlaytimeCounter(
                                playtimeMs = currentState.completedSagas.sumOf { it.data.playTimeMs },
                                label = stringResource(R.string.total_playtime_label),
                                textStyle =
                                    MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                    ),
                            )

                            Text(
                                text = currentState.data.review,
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2,
                                    ),
                                textAlign = TextAlign.Justify,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                            )

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Refletindo sobre sua alma através das histórias.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
