package com.ilustris.sagai.features.playthrough

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer

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
                ModalBottomSheet(
                    onDismissRequest = onDismiss,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_spark),
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .size(64.dp)
                                    .reactiveShimmer(true)
                                    .gradientFill(Brush.linearGradient(holographicGradient)),
                        )

                        AnimatedPlaytimeCounter(
                            playtimeMs = currentState.data.totalPlaytimeMs,
                            label = stringResource(R.string.total_playtime_label),
                            textStyle = MaterialTheme.typography.headlineLarge,
                        )

                        Text(
                            text = currentState.data.playtimeReview,
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
