package com.ilustris.sagai.ui.genre.terminal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.hexToColor
import kotlinx.coroutines.delay

/** A soft colored blur behind text, for the terminal's neon-CRT look. */
fun TextStyle.neonGlow(
    color: Color,
    blurRadius: Float = 14f,
): TextStyle = copy(shadow = Shadow(color, Offset.Zero, blurRadius))

/** This character's own theme color, falling back to the saga's accent when unset/invalid. */
fun Character.terminalColor(fallback: Color): Color = hexColor.hexToColor() ?: fallback

/** Frames per second for stepped terminal motion — low enough to read as a redraw, not a tween. */
private const val TERMINAL_FPS = 10

/**
 * An angle that advances in whole frames at [TERMINAL_FPS] instead of continuously.
 *
 * The stepping is the point. A terminal redraws its buffer at whatever rate it manages, so smooth
 * 60fps motion is exactly what would give away that this is a modern surface dressed as an old
 * one — quantising the angle is what makes it read as a machine drawing frames.
 */
@Composable
fun rememberSteppedSpin(
    degreesPerSecond: Float = 45f,
    fps: Int = TERMINAL_FPS,
): Float {
    // The lifecycle gate is a condition *inside* the effect, never a branch around the remember
    // slots. Returning early on it meant this function allocated a different number of slots
    // depending on a value that flips at runtime, and the composition rebuilt underneath the
    // caller — which showed up as the shape drawing once and then vanishing.
    val active = rememberLifecycleAnimationsActive()
    var frame by remember { mutableIntStateOf(0) }
    val frameMs = remember(fps) { (1000L / fps).coerceAtLeast(1L) }

    LaunchedEffect(frameMs, active) {
        if (!active) return@LaunchedEffect
        while (true) {
            delay(frameMs)
            frame++
        }
    }

    return (frame * (degreesPerSecond / fps)) % 360f
}
