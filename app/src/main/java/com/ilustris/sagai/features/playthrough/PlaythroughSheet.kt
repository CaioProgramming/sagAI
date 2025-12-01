package com.ilustris.sagai.features.playthrough

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
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

    AnimatedContent(state, transitionSpec = {
        fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
    }) { currentState ->
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
                val sagasGenres = currentState.data.genres
                var currentAnimatedGenre by remember { mutableStateOf(sagasGenres.randomOrNull() ?: Genre.FANTASY) }

                LaunchedEffect(sagasGenres) {
                    if (sagasGenres.isNotEmpty()) {
                        val weightedGenres = sagasGenres.flatMap { genre -> List(sagasGenres.count { it == genre }) { genre } }
                        while (true) {
                            delay(2.seconds)
                            currentAnimatedGenre = weightedGenres.random()
                        }
                    }
                }

                ModalBottomSheet(
                    onDismissRequest = onDismiss,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .padding(bottom = 32.dp)
                                .reactiveShimmer(true),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {

                        Icon(
                            painterResource(R.drawable.ic_spark),
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .size(64.dp)
                                    .gradientFill(Brush.linearGradient(holographicGradient)),
                        )

                        AnimatedContent(currentAnimatedGenre, transitionSpec = {
                            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(
                                animationSpec = tween(
                                    500
                                )
                            )
                        }) {
                            Text(
                                text = currentState.data.playtimeReview.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontStyle = FontStyle.Italic,
                                    brush = it.gradient(),
                                    shadow = Shadow(Color.White, blurRadius = 5f),
                                    fontWeight = FontWeight.Light,
                                    fontFamily = it.bodyFont()
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(16.dp)
                            )
                        }



                        AnimatedPlaytimeCounter(
                            playtimeMs = currentState.data.totalPlaytimeMs,
                            label = stringResource(R.string.total_playtime_label),
                            textStyle = MaterialTheme.typography.headlineLarge,
                            animationDuration = 3.seconds
                        )

                        Text(
                            text = currentState.data.playtimeReview.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}
