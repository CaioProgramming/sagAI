package com.ilustris.sagai.features.brain.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val STAR_PRESENCE_MS = 980
private const val STAR_PRESENCE_STAGGER_MS = 48

/**
 * Smooth fade-in for stars entering the visible scene (drill-down, new satellites, etc.).
 * Stars already on screen stay at full alpha.
 */
@Composable
fun rememberBrainStarPresenceAlphas(visibleNodeIds: Set<String>): Map<String, Float> {
    val animators =
        remember {
            mutableStateMapOf<String, Animatable<Float, AnimationVector1D>>()
        }

    LaunchedEffect(visibleNodeIds) {
        animators.keys
            .toList()
            .filter { it !in visibleNodeIds }
            .forEach { animators.remove(it) }

        val newcomers =
            visibleNodeIds
                .filter { id -> id !in animators }
                .sortedBy { it.hashCode() }

        newcomers.forEachIndexed { index, id ->
            val anim = Animatable(0f)
            animators[id] = anim
            launch {
                val delayMs = index * STAR_PRESENCE_STAGGER_MS
                if (delayMs > 0) delay(delayMs.toLong())
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec =
                        tween(
                            durationMillis = STAR_PRESENCE_MS,
                            easing = FastOutSlowInEasing,
                        ),
                )
            }
        }
    }

    return visibleNodeIds.associateWith { id -> animators[id]?.value ?: 1f }
}
