package com.ilustris.sagai.features.playthrough

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaythroughSheet(
    onDismiss: () -> Unit,
    viewModel: PlaythroughViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPlaythroughData()
    }

    AnimatedContent(state) { currentState ->
        when (currentState) {
            is PlaythroughUiState.Loading -> {
                StarryLoader(
                    true,
                    "Analisando sua jornada...",
                    textStyle =
                        MaterialTheme.typography.labelMedium.copy(
                            textAlign = TextAlign.Center,
                        ),
                )
            }

            is PlaythroughUiState.Empty -> {
                ModalBottomSheet(
                    onDismissRequest = onDismiss,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    Text(
                        currentState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                    )
                }
            }

            is PlaythroughUiState.Success -> {
                val genres = currentState.completedSagas.map { it.data.genre }
                var currentGenreIndex by remember { mutableIntStateOf(0) }
                val currentGenre = genres.getOrElse(currentGenreIndex) { Genre.entries.first() }

                LaunchedEffect(Unit) {
                    if (genres.isNotEmpty()) {
                        while (true) {
                            delay(5.seconds)
                            currentGenreIndex = (currentGenreIndex + 1) % genres.size
                        }
                    }
                }

                val palette = currentGenre.colorPalette()
                val animatedPalette =
                    palette.map { color ->
                        animateColorAsState(
                            targetValue = color,
                            animationSpec = tween(durationMillis = 1000),
                        ).value
                    }

                ModalBottomSheet(
                    onDismissRequest = onDismiss,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .padding(bottom = 32.dp)
                                .verticalScroll(rememberScrollState())
                                .animateContentSize(tween(500, easing = EaseIn)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_spark),
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .size(64.dp)
                                    .reactiveShimmer(
                                        true,
                                        currentGenre.shimmerColors(),
                                        duration = 10.seconds,
                                        repeatMode = RepeatMode.Restart,
                                    ).gradientFill(Brush.linearGradient(animatedPalette)),
                        )

                        AnimatedContent(currentGenre, transitionSpec = {
                            fadeIn(tween(200, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(
                                    tween(800, easing = EaseIn),
                                )
                        }) {
                            Text(
                                text = currentState.data.title,
                                style =
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = it.headerFont(),
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                    ),
                                textAlign = TextAlign.Center,
                                modifier =
                                    Modifier
                                        .padding(horizontal = 16.dp)
                                        .reactiveShimmer(
                                            true,
                                            it.shimmerColors(),
                                            duration = 10.seconds,
                                            targetValue = 400f,
                                            repeatMode = RepeatMode.Restart,
                                        ),
                            )
                        }

                        AnimatedPlaytimeCounter(
                            playtimeMs = currentState.completedSagas.sumOf { it.data.playTimeMs },
                            label = stringResource(R.string.total_playtime_label),
                            textStyle =
                                MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                        )

                        Text(
                            text = currentState.data.review,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}
