package com.ilustris.sagai.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.ui.theme.reactiveShimmer

@Composable
fun BookGenerationReasoning(
    reasoning: String?,
    genre: Genre,
    visualConfig: GenreVisualConfig,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = reasoning,
        label = "BookGenerationReasoning",
        transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(200))
        },
        modifier = modifier.fillMaxWidth(),
    ) { text ->
        if (text != null) {
            Text(
                text = text,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .reactiveShimmer(true, genre.shimmerColors(visualConfig)),
            )
        }
    }
}
