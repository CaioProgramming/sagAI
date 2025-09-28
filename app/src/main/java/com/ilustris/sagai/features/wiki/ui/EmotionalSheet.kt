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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.zoomAnimation
import effectForGenre

@Composable
fun EmotionalSheet(
    saga: SagaContent,
    cardImage: String,
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        val genre = saga.data.genre
        val size = if (cardImage.isNotEmpty()) 350.dp else 100.dp

        Box(
            Modifier
                .fillMaxWidth()
                .height(size)
                .clipToBounds(),
        ) {
            AsyncImage(
                cardImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(
                    genre.color,
                    blendMode = BlendMode.Multiply
                ),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .zoomAnimation()
                        .clipToBounds(),
            )

            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .background(
                        fadeGradientTop(),
                    ).height(size * .5f)
                    .fillMaxWidth(),
            )
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(.2f)
                    .background(
                        fadeGradientBottom(),
                    ),
            )
        }

        Text(
            "Jornada Interior",
            style =
                MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                ),
            modifier = Modifier.padding(16.dp),
        )

        Text(
            saga.data.emotionalReview ?: emptyString(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(16.dp),
        )

        Spacer(modifier = Modifier.height(50.dp))
    }
}
