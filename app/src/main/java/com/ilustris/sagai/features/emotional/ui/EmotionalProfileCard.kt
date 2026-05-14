@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.emotional.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.wiki.ui.EmotionalSheet
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.zoomAnimation

@Composable
fun EmotionalProfileCard(
    saga: Saga,
    modifier: Modifier,
    viewModel: EmotionalProfileViewModel = hiltViewModel(),
) {
    val emotionalIconUrl by viewModel.emotionalIconUrl.collectAsState()

    LaunchedEffect(saga.id) {
        viewModel.loadEmotionalIcon(saga.id)
    }

    val genre = saga.genre
    var showEmotionalReview by remember { mutableStateOf(false) }
    Box(
        modifier
            .clip(
                sagaShape(),
            ).border(
                1.dp,
                MaterialTheme.colorScheme.onBackground.gradientFade(),
                sagaShape(),
            ).background(
                MaterialTheme.colorScheme.surfaceContainer,
                sagaShape(),
            ).fillMaxWidth()
            .height(200.dp)
            .clickable {
                showEmotionalReview = true
            },
    ) {
        AsyncImage(
            emotionalIconUrl,
            null,
            colorFilter =
                ColorFilter.tint(
                    genre.color,
                    blendMode = BlendMode.Multiply,
                ),
            modifier =
                Modifier
                    .fillMaxSize()
                    .zoomAnimation(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .background(
                        fadeGradientBottom(),
                    ).fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            Text(
                saga.emotionalProfile?.personaTitle
                    ?: stringResource(id = R.string.inner_journey),
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                modifier = Modifier.padding(vertical = 16.dp),
            )

            Text(
                saga.emotionalProfile?.actionText
                    ?: stringResource(id = R.string.inner_journey_description),
                style =
                    MaterialTheme.typography.bodySmall.copy(),
            )
        }
    }

    if (showEmotionalReview) {
        ModalBottomSheet(
            onDismissRequest = { showEmotionalReview = false },
            containerColor = MaterialTheme.colorScheme.background,
            shape = sagaShape(),
            dragHandle = {},
        ) {
            EmotionalSheet(saga, onDismiss = {
                showEmotionalReview = false
            })
        }
    }
}
