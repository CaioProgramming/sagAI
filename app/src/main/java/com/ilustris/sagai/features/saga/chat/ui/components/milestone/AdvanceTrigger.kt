package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.core.utils.vibrate
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.presentation.model.toUi
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdvanceTrigger(
    action: NarrativeAction,
    genre: Genre,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
    isGenerating: Boolean,
) {
    val actionUi = action.toUi()
    var isHolding by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val visualConfig = LocalGenreVisualConfig.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val colors = genre.colorPalette(visualConfig)

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

    AnimatedContent(isGenerating) {
        if (it) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    painterResource(genre.icon),
                    "carregando",
                    modifier =
                        Modifier
                            .gradientFill(Brush.verticalGradient(colors))
                            .size(50.dp)
                            .genreVfx(genre),
                )
            }
        } else {
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val genreBrush = Brush.linearGradient(colors)
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shape = shape,
                    tonalElevation = if (isHolding) 8.dp else 2.dp,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .scale(scale)
                            .dropShadow(shape) {
                                radius = 35f * progress
                                color = primaryColor
                                spread = 20f * progress
                                brush = genreBrush
                            }.border(1.dp, MaterialTheme.colorScheme.primary.gradientFade(), shape)
                            .clip(shape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isHolding = true
                                        view.context.vibrate(longArrayOf(0, 20))

                                        val holdJob =
                                            scope.launch {
                                                delay(1000)
                                                if (isHolding) {
                                                    view.context.vibrate(longArrayOf(0, 60))
                                                }
                                                delay(500)
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
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .background(
                                        brush = genreBrush,
                                    ),
                        )

                        Text(
                            text =
                                if (isHolding) {
                                    stringResource(actionUi.holdingTextRes)
                                } else {
                                    stringResource(actionUi.titleRes)
                                }.uppercase(),
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    color =
                                        if (progress > 0.6f) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                    shadow =
                                        Shadow(
                                            color = primaryColor,
                                            blurRadius = 15f * progress,
                                        ),
                                ),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
        }
    }
}
