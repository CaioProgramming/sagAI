package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.levitate

@Composable
fun NewSagaReasoning(
    message: String,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = message,
        transitionSpec = {
            fadeIn() + slideInVertically { it / 4 } togetherWith
                fadeOut() + slideOutVertically { -it / 4 }
        },
        modifier = modifier.padding(16.dp),
        label = "NewSagaReasoning",
    ) { text ->
        Text(
            text = text,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    shadow =
                        Shadow(
                            Color.White,
                            blurRadius = 10f,
                        ),
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .levitate(),
            textAlign = TextAlign.Center,
        )
    }
}
