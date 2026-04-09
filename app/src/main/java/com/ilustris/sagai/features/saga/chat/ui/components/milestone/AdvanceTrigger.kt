package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.core.utils.vibrate
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.saga.chat.presentation.model.PendingAdvance
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdvanceTrigger(
    pendingAdvance: PendingAdvance,
    genre: Genre,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isHolding by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val visualConfig = LocalGenreVisualConfig.current
    val primaryColor = genre.resolveColor(visualConfig)
    genre.colorPalette(visualConfig)

    val progress by animateFloatAsState(
        targetValue = if (isHolding) 1f else 0f,
        animationSpec = if (isHolding) tween(1500, easing = LinearEasing) else tween(300),
        label = "progress",
    )

    val scale by animateFloatAsState(
        targetValue = if (isHolding) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale",
    )

    val shape = RoundedCornerShape(genre.cornerSize())

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val genreBrush = genre.gradient()
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = shape,
            tonalElevation = if (isHolding) 8.dp else 2.dp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .scale(scale)
                    .dropShadow(shape) {
                        radius = 25f * progress
                        color = primaryColor
                        spread = 10f * progress
                        brush = genreBrush
                    }.shadow(
                        elevation = 12.dp * progress,
                        shape = shape,
                        clip = false,
                        ambientColor = primaryColor.copy(alpha = progress),
                        spotColor = primaryColor.copy(alpha = progress),
                    ).pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHolding = true
                                view.context.vibrate(longArrayOf(0, 20))

                                val holdJob =
                                    scope.launch {
                                        delay(1000) // Vibrate after 1 second
                                        if (isHolding) {
                                            view.context.vibrate(longArrayOf(0, 60))
                                        }
                                        delay(500) // Complete at 1.5 seconds
                                        if (isHolding) {
                                            view.context.vibrate(longArrayOf(0, 400))
                                            onAdvance()
                                            isHolding = false
                                        }
                                    }
                                try {
                                    awaitRelease()
                                } finally {
                                    isHolding = false
                                    holdJob.cancel()
                                }
                            },
                        )
                    },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Progress Background
                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                color = primaryColor.copy(alpha = 0.8f),
                                shape = shape,
                            ),
                )

                // Label Text
                Text(
                    text =
                        if (isHolding) {
                            stringResource(pendingAdvance.holdingTextRes)
                        } else {
                            stringResource(pendingAdvance.titleRes)
                        }.uppercase(),
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            fontFamily = genre.bodyFont(),
                            fontWeight = FontWeight.Bold,
                            color =
                                if (progress > 0.6f) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        ),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}
