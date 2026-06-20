package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.vibrate
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.presentation.model.toUi
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.themeIcon
import com.ilustris.sagai.ui.theme.themeShimmer
import com.ilustris.sagai.ui.theme.themeVfx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun AdvanceTrigger(
    action: NarrativeAction,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
    isGenerating: Boolean,
) {
    val actionUi = action.toUi()
    var isHolding by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val colors = themeShimmer()

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

    val shape = MaterialTheme.shapes.medium

    AnimatedContent(isGenerating) {
        if (it) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    themeIcon(),
                    contentDescription = stringResource(R.string.milestone_loading_cd),
                    modifier =
                        Modifier
                            .gradientFill(Brush.verticalGradient(colors))
                            .size(50.dp)
                            .themeVfx(),
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
                val shadowBrush = Brush.linearGradient(morphingGradient())
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
                                brush = shadowBrush
                            }.border(
                                1.dp,
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = .3f),
                                shape,
                            ).clip(shape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isHolding = true
                                        view.context.vibrate(longArrayOf(0, 20))

                                        val holdJob =
                                            scope.launch {
                                                delay(1.seconds)
                                                if (isHolding) {
                                                    view.context.vibrate(longArrayOf(0, 60))
                                                }
                                                delay(500.milliseconds)
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

                        val textColor by animateColorAsState(
                            targetValue =
                                if (progress > 0.6f) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            animationSpec = tween(500),
                        )

                        Text(
                            text =
                                if (isHolding) {
                                    stringResource(actionUi.holdingTextRes)
                                } else {
                                    stringResource(actionUi.titleRes ?: R.string.continue_text)
                                }.uppercase(),
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    color = textColor,
                                ),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
        }
    }
}
