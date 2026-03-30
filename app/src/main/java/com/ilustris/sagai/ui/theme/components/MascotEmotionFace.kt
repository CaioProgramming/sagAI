package com.ilustris.sagai.ui.theme.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.ui.theme.LocalMascotEmotionService
import com.ilustris.sagai.ui.theme.levitate

@Composable
fun MascotEmotionFace(
    emotionalTone: EmotionalTone,
    genre: Genre,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    onFinishDraw: () -> Unit = {},
) {
    val service = LocalMascotEmotionService.current
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(genre, emotionalTone) {
        imageUrl = service.getEmotionUrl(genre, emotionalTone)
        isLoading = false
    }

    LaunchedEffect(imageUrl, isLoading) {
        if (!isLoading) {
            onFinishDraw()
        }
    }

    AnimatedContent(imageUrl, label = "mascotIconAnimation") { url ->
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = emotionalTone.name,
                modifier =
                    modifier
                        .size(size)
                        .levitate()
                        .animateEnterExit(
                            enter = scaleIn(tween(600, easing = EaseInBounce)),
                        ),
            )
        } else if (!isLoading) {
            // Graceful fallback to existing shape
            VibeShapeDrawing(
                emotionalTone = emotionalTone,
                modifier = modifier.size(size),
            )
        }
    }
}
