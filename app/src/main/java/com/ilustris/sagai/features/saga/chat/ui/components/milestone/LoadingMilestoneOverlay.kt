package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.rememberVectorShape
import com.ilustris.sagai.ui.theme.shimmerize
import com.ilustris.sagai.ui.theme.themeIconVector
import com.ilustris.sagai.ui.theme.themePainter
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
                    .padding(32.dp)
                    .reactiveShimmer(
                        true,
                        duration = 3.seconds,
                        repeatMode = RepeatMode.Restart,
                        shimmerColors = Color.White.shimmerize(),
                    ),
        ) {
            val morphingBrush = Brush.horizontalGradient(morphingGradient())
            Icon(
                themePainter(),
                null,
                tint = MaterialTheme.colorScheme.background,
                modifier =
                    sparkModifier
                        .dropShadow(rememberVectorShape(themeIconVector())) {
                            brush = morphingBrush
                            radius = 10f
                            spread = 0.5f
                        }.genreVfx(genre)
                        .size(
                            50.dp,
                        ),
            )

            AnimatedContent(contentReasoning, transitionSpec = {
                fadeIn(tween(1000, easing = EaseInOutQuad)) togetherWith
                    fadeOut(
                        animationSpec = tween(500, easing = EaseInOutQuad),
                    )
            }) { text ->
                text?.let {
                    Text(
                        it,
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                textAlign = TextAlign.Center,
                                shadow =
                                    Shadow(
                                        MaterialTheme.colorScheme.primary,
                                        blurRadius = 10f,
                                    ),
                                brush = Brush.horizontalGradient(morphingGradient()),
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
