package com.ilustris.sagai.ui.theme.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import com.ilustris.sagai.ui.theme.SAGA_THEME_TRANSITION_MS
import com.ilustris.sagai.ui.theme.ThemeIcon
import com.ilustris.sagai.ui.theme.iconDropShadow
import com.ilustris.sagai.ui.theme.rememberVectorShape
import com.ilustris.sagai.ui.theme.sagaBrush

@Composable
fun MorphingThemeIcon(
    modifier: Modifier = Modifier,
    genre: Genre? = LocalSagaGenre.current,
    brush: Brush = sagaBrush(),
    glowIntensity: Float = 0.5f,
    tint: Color = Color.Unspecified,
    contentDescription: String? = null,
    iconModifier: Modifier = Modifier,
) {
    val animationsActive = rememberLifecycleAnimationsActive()
    val transition = updateTransition(genre, label = "morphingThemeIcon")
    val clampedIntensity = glowIntensity.coerceIn(0f, 1f)
    val themeTween = tween<Float>(SAGA_THEME_TRANSITION_MS, easing = FastOutSlowInEasing)

    val glowProgress by transition.animateFloat(
        transitionSpec = {
            when {
                !animationsActive -> {
                    tween(0)
                }

                initialState != targetState -> {
                    keyframes {
                        durationMillis = SAGA_THEME_TRANSITION_MS
                        0f at 0 using FastOutSlowInEasing
                        clampedIntensity at (SAGA_THEME_TRANSITION_MS / 2)
                        clampedIntensity * 0.92f at SAGA_THEME_TRANSITION_MS
                    }
                }

                else -> {
                    themeTween
                }
            }
        },
        label = "glowProgress",
    ) { targetGenre ->
        when {
            !animationsActive -> {
                if (targetGenre != null) {
                    clampedIntensity
                } else {
                    clampedIntensity * 0.35f
                }
            }

            targetGenre == null -> {
                clampedIntensity * 0.35f
            }

            else -> {
                clampedIntensity
            }
        }
    }

    Box(
        modifier = modifier.graphicsLayer { clip = false },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = genre,
            transitionSpec = {
                (
                    fadeIn(themeTween) +
                        scaleIn(
                            initialScale = 0.88f,
                            animationSpec = themeTween,
                        )
                ) togetherWith
                    (
                        fadeOut(themeTween) +
                            scaleOut(
                                targetScale = 0.88f,
                                animationSpec = themeTween,
                            )
                    )
            },
            label = "themeIconCrossfade",
        ) { activeGenre ->
            val iconRes = activeGenre?.icon ?: R.drawable.ic_spark
            val iconShape = rememberVectorShape(iconRes)
            val imageVector = ImageVector.vectorResource(iconRes)

            ThemeIcon(
                imageVector = imageVector,
                brush = brush,
                tint = tint,
                contentDescription = contentDescription,
                glowIntensity = 0f,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .iconDropShadow(
                            shape = iconShape,
                            brush = brush,
                            progress = glowProgress,
                        ).then(iconModifier),
            )
        }
    }
}
