package com.ilustris.sagai.features.share.ui.card

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import com.ilustris.sagai.ui.theme.filters.loadShaderFromAssetsOnce
import timber.log.Timber

/** The fringe is always a little visible, so the card at rest still reads as foil. */
private const val RESTING_FRINGE = 0.2f

/**
 * Splits the colour channels of whatever it is applied to, sideways, by an amount that follows
 * [tilt]: a prismatic halo on edges that swings with the card.
 *
 * [strengthDp] is the offset at full tilt. Needs API 33; below that it does nothing, and the card
 * just has no fringe. See `holo_fringe.agsl`.
 */
@Composable
fun Modifier.tiltFringe(
    tilt: () -> Offset,
    strengthDp: Float,
): Modifier {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return this
    val source = loadShaderFromAssetsOnce("holo_fringe.agsl") ?: return this
    return tiltFringeWithShader(source, tilt, strengthDp)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun Modifier.tiltFringeWithShader(
    source: String,
    tilt: () -> Offset,
    strengthDp: Float,
): Modifier {
    val density = LocalDensity.current.density
    val shader =
        remember(source) {
            runCatching { RuntimeShader(source).also { it.setFloatUniform("u_shift", 0f, 0f) } }
                .onFailure { Timber.e(it, "holo_fringe.agsl unusable — no fringe") }
                .getOrNull()
        } ?: return this

    return graphicsLayer {
        val t = tilt()
        val strength = strengthDp * density
        shader.setFloatUniform("u_shift", (t.x + RESTING_FRINGE) * strength, t.y * strength * 0.6f)
        renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "image").asComposeRenderEffect()
    }
}
