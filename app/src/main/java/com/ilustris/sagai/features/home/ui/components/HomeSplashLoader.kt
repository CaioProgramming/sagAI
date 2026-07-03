package com.ilustris.sagai.features.home.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.morphingGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private val SPLASH_ICON_SWAP_MS = 1.seconds
private const val SPLASH_ICON_TRANSITION_MS = 500
private val splashIconSize = 150.dp
private val splashIconOffsetY = (-24).dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeSplashLoader(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
) {
    val animationsActive = rememberLifecycleAnimationsActive()
    val splashIcons =
        remember {
            listOf(R.drawable.ic_spark) + Genre.entries.map { it.icon }
        }
    var iconIndex by remember { mutableIntStateOf(0) }
    val morphBrush = Brush.verticalGradient(morphingGradient(duration = 1.seconds))

    LaunchedEffect(animationsActive) {
        if (!animationsActive) return@LaunchedEffect
        while (isActive) {
            delay(SPLASH_ICON_SWAP_MS)
            iconIndex = Random.nextInt(splashIcons.size)
        }
    }

    val currentIcon = splashIcons[iconIndex]

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        AnimatedContent(
            targetState = currentIcon,
            transitionSpec = {
                fadeIn(tween(SPLASH_ICON_TRANSITION_MS, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        animationSpec =
                            tween(
                                SPLASH_ICON_TRANSITION_MS,
                                easing = FastOutSlowInEasing,
                            ),
                    ) togetherWith
                    fadeOut(tween(SPLASH_ICON_TRANSITION_MS, easing = FastOutSlowInEasing)) +
                    scaleOut(
                        animationSpec =
                            tween(
                                SPLASH_ICON_TRANSITION_MS,
                                easing = FastOutSlowInEasing,
                            ),
                    )
            },
            label = "homeSplashIcon",
            modifier = Modifier.size(splashIconSize).offset(y = splashIconOffsetY),
        ) { iconRes ->
            val iconModifier =
                Modifier
                    .padding(4.dp)
                    .fillMaxSize()
                    .gradientFill(morphBrush)

            with(sharedTransitionScope) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier =
                        iconModifier.sharedElement(
                            rememberSharedContentState("spark_icon"),
                            animatedContentScope,
                        ),
                )
            }
        }
    }
}
