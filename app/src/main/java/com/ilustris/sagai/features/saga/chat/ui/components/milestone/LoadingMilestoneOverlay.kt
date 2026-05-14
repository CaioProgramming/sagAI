package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlin.time.Duration.Companion.seconds

@Composable
fun LoadingMilestoneOverlay(
    saga: Saga,
    sparkModifier: Modifier,
    titleModifier: Modifier,
    contentReasoning: String? = null,
    modifier: Modifier = Modifier,
) {
    val genre = remember { saga.genre }
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
        ) {
            Image(
                painterResource(genre.icon),
                null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier =
                    sparkModifier
                        .genreVfx(genre)
                        .padding(8.dp)
                        .gradientFill(Brush.verticalGradient(genre.colorPalette()))
                        .size(
                            50.dp,
                        ).reactiveShimmer(
                            true,
                            duration = 4.seconds,
                            targetValue = 200f,
                            repeatMode = RepeatMode.Restart,
                        ),
            )

            AnimatedContent(contentReasoning) { text ->
                text?.let {
                    Text(
                        it,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                textAlign = TextAlign.Center,
                                shadow =
                                    Shadow(
                                        MaterialTheme.colorScheme.primary,
                                        blurRadius = 5f,
                                    ),
                            ),
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                }
            }
        }
    }
}
