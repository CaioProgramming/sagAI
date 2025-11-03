package com.ilustris.sagai.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient

@Composable
fun StarryLoader(
    isLoading: Boolean,
    loadingMessage: String?,
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    brush: Brush = Brush.verticalGradient(holographicGradient),
) {
    if (isLoading) {
        Dialog(
            onDismissRequest = { },
            properties =
                DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
        ) {
            Box(Modifier.fillMaxSize()) {
                val starsAlpha by animateFloatAsState(
                    targetValue = if (loadingMessage == null) 1f else .7f,
                    animationSpec = tween(500),
                )
                StarryTextPlaceholder(
                    modifier =
                        Modifier
                            .alpha(starsAlpha)
                            .fillMaxSize()
                            .gradientFill(brush),
                )

                AnimatedContent(loadingMessage, transitionSpec = {
                    fadeIn(tween(500)) togetherWith slideOutVertically { it }
                }) {
                    it?.let { message ->
                        Text(
                            message,
                            style = textStyle,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
        }
    }
}
