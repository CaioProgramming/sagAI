package com.ilustris.sagai.features.share.ui.card

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.components.mascot.rememberTiltLook
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * Where a collector card is tilted, as an [Offset] with each axis in -1..1.
 *
 * [read] is meant to be called inside draw or layer lambdas so tilt updates repaint without
 * recomposing; [gestures] has to sit on the card itself.
 */
@Stable
class CardTiltState internal constructor(
    val read: () -> Offset,
    val gestures: Modifier,
)

/**
 * One tilt, fed by a finger on the card, the phone's own tilt, and a slow sway that keeps the card
 * alive. All three go through the same per-frame smoothing toward a single target, so changing
 * source (a finger landing or lifting) never makes the card jump: the target moves and the card
 * follows, quickly while held and gently after release.
 *
 * @param frozen pins the card flat and the finish at rest. Used while the card is captured as an
 * image, so the shared picture never shows a mid-motion frame.
 */
@Composable
fun rememberCardTilt(frozen: Boolean): CardTiltState {
    val sensor = rememberTiltLook(enabled = !frozen)
    val animationsActive = rememberLifecycleAnimationsActive()
    val frozenState = rememberUpdatedState(frozen)
    val activeState = rememberUpdatedState(animationsActive)
    val drag = remember { mutableStateOf<Offset?>(null) }
    val current = remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(frozen) {
        if (frozen) {
            current.value = Offset.Zero
            return@LaunchedEffect
        }
        var last = withFrameNanos { it }
        var clock = 0f
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, MAX_FRAME_S)
            last = now

            val active = activeState.value
            if (active) clock += dt
            val sway =
                if (active) {
                    Offset(sin(clock * SWAY_SPEED) * SWAY_X, cos(clock * SWAY_SPEED * 0.7f) * SWAY_Y)
                } else {
                    Offset.Zero
                }
            val held = drag.value
            val target = held ?: ((sensor.value ?: Offset.Zero) + sway)
            val tau = if (held != null) HELD_TAU_S else RELEASE_TAU_S
            current.value += (target - current.value) * (1f - exp(-dt / tau))
        }
    }

    val read: () -> Offset =
        remember(current, frozenState) {
            { if (frozenState.value) Offset.Zero else current.value }
        }

    val gestures =
        remember(drag) {
            Modifier.pointerInput(Unit) {
                // No touch slop: the card tilts toward the finger the moment it lands, and keeps
                // following while it is held, still or moving.
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    drag.value = down.position.toTilt(size)
                    do {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (change.pressed) {
                            change.consume()
                            drag.value = change.position.toTilt(size)
                        }
                    } while (change.pressed)
                    drag.value = null
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

/** Time constant, in seconds, of how fast the card catches up with a finger. */
private const val HELD_TAU_S = 0.05f

/** Time constant, in seconds, of how gently it drifts back once released. */
private const val RELEASE_TAU_S = 0.3f

/** A long frame (the app was paused) must not make the card leap. */
private const val MAX_FRAME_S = 0.05f

private const val SWAY_SPEED = (2 * PI / 9).toFloat()
private const val SWAY_X = 0.55f
private const val SWAY_Y = 0.3f
