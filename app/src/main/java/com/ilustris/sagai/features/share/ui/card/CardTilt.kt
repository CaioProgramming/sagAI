package com.ilustris.sagai.features.share.ui.card

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.components.mascot.rememberTiltLook
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Where a collector card is tilted, as an [Offset] with each axis in -1..1.
 *
 * Three sources: a finger on the card, the phone's own tilt, and a slow sway that keeps the card
 * alive whenever no finger is on it (a phone held still would otherwise leave it dead flat). [read] is meant to be called inside draw or layer lambdas so tilt updates repaint
 * without recomposing; [gestures] has to sit on the card itself.
 */
@Stable
class CardTiltState internal constructor(
    val read: () -> Offset,
    val gestures: Modifier,
)

/**
 * @param frozen pins the card flat and the foil at rest. Used while the card is captured as an
 * image, so the shared picture never shows a mid-motion frame.
 */
@Composable
fun rememberCardTilt(frozen: Boolean): CardTiltState {
    val sensor = rememberTiltLook(enabled = !frozen)
    val animationsActive = rememberLifecycleAnimationsActive()
    val frozenState = rememberUpdatedState(frozen)
    val activeState = rememberUpdatedState(animationsActive)
    val drag = remember { mutableStateOf<Offset?>(null) }
    val animated = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val sway =
        rememberInfiniteTransition(label = "card-sway").animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(SWAY_PERIOD_MS, easing = LinearEasing)),
            label = "card-sway-phase",
        )

    LaunchedEffect(frozen) {
        if (frozen) {
            animated.snapTo(Offset.Zero)
            return@LaunchedEffect
        }
        snapshotFlow { drag.value ?: sensor.value ?: Offset.Zero }
            .collectLatest {
                animated.animateTo(it, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow))
            }
    }

    val read: () -> Offset =
        remember(animated, sway, sensor, drag) {
            read@{
                if (frozenState.value) return@read Offset.Zero
                val swaying = activeState.value && drag.value == null
                if (swaying) {
                    animated.value + Offset(sin(sway.value) * SWAY_X, cos(sway.value * 0.7f) * SWAY_Y)
                } else {
                    animated.value
                }
            }
        }

    val gestures =
        remember(drag) {
            Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { drag.value = it.toTilt(size) },
                    onDragEnd = { drag.value = null },
                    onDragCancel = { drag.value = null },
                ) { change, _ ->
                    change.consume()
                    drag.value = change.position.toTilt(size)
                }
            }
        }

    return remember(read, gestures) { CardTiltState(read, gestures) }
}

private fun Offset.toTilt(size: IntSize): Offset =
    Offset(
        x = (((x / size.width) - 0.5f) * 2f).coerceIn(-1f, 1f),
        y = (((y / size.height) - 0.5f) * 2f).coerceIn(-1f, 1f),
    )

private const val SWAY_PERIOD_MS = 9_000
private const val SWAY_X = 0.55f
private const val SWAY_Y = 0.3f
