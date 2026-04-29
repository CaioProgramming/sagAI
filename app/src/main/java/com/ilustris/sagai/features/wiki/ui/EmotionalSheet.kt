package com.ilustris.sagai.features.wiki.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.emotional.ui.EmotionalProfileViewModel
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.saga.detail.presentation.EmotionalReviewViewModel
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.zoomAnimation

@Composable
fun EmotionalSheet(
    saga: SagaContent,
    onDismissRequest: () -> Unit = {},
    viewModel: EmotionalReviewViewModel = hiltViewModel(),
    profileViewModel: EmotionalProfileViewModel = hiltViewModel(),
) {
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val genre = saga.data.genre
    val emotionalIconUrl by profileViewModel.emotionalIconUrl.collectAsStateWithLifecycle()

    LaunchedEffect(saga) {
        viewModel.createEmotionalReview(saga)
        profileViewModel.loadEmotionalIcon(saga)
    }

    if (isGenerating) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            StarryLoader(
                true,
                loadingMessage ?: emptyString(),
                textStyle =
                    MaterialTheme.typography.labelMedium.copy(
                        Brush.verticalGradient(
                            genre.colorPalette(),
                        ),
                        shadow =
                            Shadow(
                                color = genre.resolveColor(),
                                blurRadius = 10f,
                            ),
                    ),
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            emotionalIconUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter =
                ColorFilter.tint(
                    genre.resolveColor(),
                    blendMode = BlendMode.Multiply,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.6f)
                    .zoomAnimation()
                    .clipToBounds()
                    .reactiveShimmer(true),
        )

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .background(fadeGradientBottom())
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(
                saga.data.emotionalProfile?.personaTitle ?: stringResource(R.string.inner_journey),
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                    ),
                modifier = Modifier.padding(16.dp),
            )

            Text(
                saga.data.emotionalProfile?.emotionalContent ?: saga.data.emotionalReview
                    ?: emptyString(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(16.dp),
            )

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
