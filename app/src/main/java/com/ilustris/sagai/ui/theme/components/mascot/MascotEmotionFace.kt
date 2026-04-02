package com.ilustris.sagai.ui.theme.components.mascot

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.ui.theme.components.VibeShapeDrawing
import com.ilustris.sagai.ui.theme.levitate
import kotlin.time.Duration.Companion.seconds

@Composable
fun MascotEmotionFace(
    imageUrl: String?,
    emotionalTone: EmotionalTone,
    modifier: Modifier,
) {
    AnimatedContent(imageUrl, label = "mascotIconAnimation") { url ->
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = emotionalTone.name,
                modifier =
                    modifier
                        .levitate(duration = 5.seconds, yOffset = 10f)
                        .animateEnterExit(
                            enter = scaleIn(tween(600, easing = EaseInBounce)),
                        ),
                onError = {
                    Log.e("MascotSticker", "Error loading image $url")
                    it.result.throwable.printStackTrace()
                },
            )
        } else {
            VibeShapeDrawing(
                emotionalTone = emotionalTone,
                modifier = modifier,
            )
        }
    }
}
