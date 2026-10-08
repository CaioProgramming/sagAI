package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.sqrt
import androidx.compose.runtime.withFrameMillis
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.rememberReduceMotion
import kotlinx.coroutines.isActive

/**
 * The fade under the chat input (what separates the messages from the composer): the saga's color
 * rising from the bottom edge, breathing with its ambient music — a lighter wash of that color
 * swells over it with the track's loudness, then settles when the music stops.
 *
 * With no music it is exactly the plain primary-tinted fade. The pulse only ever lightens it, so
 * the input keeps its contrast. [level] (0..1) and the frame clock are read only in the draw phase, so the
 * pulse redraws every frame without recomposing the chat. Frames run only while [isPlaying], the
 * screen is visible and the system hasn't asked for reduced motion.
 */
@Composable
fun MusicReactiveFade(
    level: () -> Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    // A lighter take on the theme color: over the fade's own primary a same-color glow can't show.
    val glowColor = lerp(primary, Color.White, GLOW_LIGHTEN)
    val clock = remember { mutableLongStateOf(0L) }
    val smoothed = remember { floatArrayOf(0f) }
    val running = isPlaying && rememberLifecycleAnimationsActive() && !rememberReduceMotion()

    // Keeps ticking after the music stops until the glow has faded out, then stops redrawing.
    LaunchedEffect(running) {
        while (isActive && (running || smoothed[0] > SETTLED)) {
            withFrameMillis { clock.longValue = it }
        }
    }

    Box(
        modifier
            .background(fadeGradientBottom(primary))
            .drawBehind {
                clock.longValue
                // Lifts the quiet parts of the track so the pulse reads without loud peaks.
                val target = if (running) sqrt(level().coerceIn(0f, 1f)) else 0f
                // Rises faster than it falls: a beat lights up, then eases off.
                smoothed[0] += (target - smoothed[0]) * if (target > smoothed[0]) ATTACK else RELEASE
                val energy = smoothed[0]
                if (energy < SETTLED) return@drawBehind

                val reach = size.height * (MIN_REACH + (1f - MIN_REACH) * energy)
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, glowColor.copy(alpha = MAX_ALPHA * energy)),
                            startY = size.height - reach,
                            endY = size.height,
                        ),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - reach),
                    size = androidx.compose.ui.geometry.Size(size.width, reach),
                )
            },
    )
}

private const val ATTACK = 0.18f
private const val RELEASE = 0.05f

/** Below this the glow is invisible and the clock can stop. */
private const val SETTLED = 0.01f

/** Share of the fade's height the glow always covers once there is any energy. */
private const val MIN_REACH = 0.5f
private const val MAX_ALPHA = 0.55f
private const val GLOW_LIGHTEN = 0.3f
