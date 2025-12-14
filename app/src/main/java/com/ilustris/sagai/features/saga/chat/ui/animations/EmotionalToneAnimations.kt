package com.ilustris.sagai.features.saga.chat.ui.animations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Main extension function that applies entrance animation based on EmotionalTone.
 * These animations are designed to be subtle enhancements to the reading experience.
 */
@Composable
fun Modifier.emotionalEntrance(
    emotionalTone: EmotionalTone?,
    isAnimated: Boolean,
    onAnimationFinished: () -> Unit = {}
): Modifier {
    if (!isAnimated) return this

    val tone = emotionalTone ?: EmotionalTone.NEUTRAL

    // We use a simple state to trigger the animation once when the composable enters the composition
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        // Approximate duration of longest animation to signal finish
        delay(800)
        onAnimationFinished()
    }

    return when (tone) {
        EmotionalTone.JOYFUL, EmotionalTone.HOPEFUL, EmotionalTone.EMPATHETIC ->
            gentleBounce(isVisible)

        EmotionalTone.ANGRY, EmotionalTone.FRUSTRATED ->
            impactEffect(isVisible)

        EmotionalTone.SAD, EmotionalTone.MELANCHOLIC ->
            driftDown(isVisible)

        EmotionalTone.ANXIOUS, EmotionalTone.CONCERNED ->
            tremorEffect(isVisible)

        EmotionalTone.CURIOUS, EmotionalTone.DETERMINED ->
            popIn(isVisible)

        EmotionalTone.CYNICAL ->
            slideFade(isVisible)

        EmotionalTone.CALM, EmotionalTone.NEUTRAL ->
            smoothFade(isVisible)
    }
}

/**
 * JOYFUL, HOPEFUL, EMPATHETIC
 * Soft bounce with slight rotation wobble.
 */
@Composable
private fun Modifier.gentleBounce(isVisible: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "gentleBounceScale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else -5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "gentleBounceRotation"
    )

    return this
        .scale(scale)
        .rotate(rotation)
        .alpha(if (isVisible) 1f else 0f)
}

/**
 * ANGRY, FRUSTRATED
 * Sharp scale-in with screen shake (simulated by offset).
 */
@Composable
private fun Modifier.impactEffect(isVisible: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 1.5f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "impactScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "impactAlpha"
    )

    // Shake effect
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Wait for impact
            delay(150)
            // Shake
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 300
                    -10f at 50
                    10f at 100
                    -5f at 150
                    5f at 200
                    0f at 300
                }
            )
        }
    }

    return this
        .scale(scale)
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        .alpha(alpha)
}

/**
 * SAD, MELANCHOLIC
 * Slow descent with fade-in.
 */
@Composable
private fun Modifier.driftDown(isVisible: Boolean): Modifier {
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else -50f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "driftOffset"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "driftAlpha"
    )

    return this
        .offset { IntOffset(0, offsetY.roundToInt()) }
        .alpha(alpha)
}

/**
 * ANXIOUS, CONCERNED
 * Micro-vibrations with flickering alpha.
 */
@Composable
private fun Modifier.tremorEffect(isVisible: Boolean): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "tremorAlpha"
    )

    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 100
                        2f at 25
                        -2f at 75
                        0f at 100
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    // Stop tremor after 500ms
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(500)
            offsetX.snapTo(0f)
        }
    }

    return this
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        .alpha(alpha)
}

/**
 * CURIOUS, DETERMINED
 * Elastic overshoot with rotation.
 */
@Composable
private fun Modifier.popIn(isVisible: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "popInScale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else -15f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "popInRotation"
    )

    return this
        .scale(scale)
        .rotate(rotation)
        .alpha(if (isVisible) 1f else 0f)
}

/**
 * CYNICAL
 * Horizontal slide with sarcastic tilt.
 */
@Composable
private fun Modifier.slideFade(isVisible: Boolean): Modifier {
    val offsetX by animateFloatAsState(
        targetValue = if (isVisible) 0f else 100f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else 5f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "slideRotation"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "slideAlpha"
    )

    return this
        .offset { IntOffset(offsetX.roundToInt(), 0) }
        .rotate(rotation)
        .alpha(alpha)
}

/**
 * CALM, NEUTRAL
 * Simple fade with minimal scale.
 */
@Composable
private fun Modifier.smoothFade(isVisible: Boolean): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "smoothFadeAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "smoothFadeScale"
    )

    return this
        .scale(scale)
        .alpha(alpha)
}
