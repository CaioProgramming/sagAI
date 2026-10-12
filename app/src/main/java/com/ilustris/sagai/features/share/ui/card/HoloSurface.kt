package com.ilustris.sagai.features.share.ui.card

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import com.ilustris.sagai.ui.theme.filters.loadShaderFromAssetsOnce
import timber.log.Timber

private const val FOIL_STOPS = 5
private const val FOIL_INTENSITY = 0.9f

/** The fringe is always a little visible, so the card at rest still reads as foil. */
private const val RESTING_FRINGE = 0.2f

/**
 * The holographic finish of a collector card, in one pass: a chromatic fringe, a band of [colors]
 * dodged into the art, and a glare, all driven by [tilt]. See `holo_surface.agsl`.
 *
 * [fringeDp] is the channel offset at full tilt. [tilt] is read when the layer is drawn, so a tilt
 * update repaints without recomposing.
 *
 * Needs API 33. Below that, or while the shader is still loading, this does nothing and the caller
 * is expected to fall back to [HoloFoil].
 */
@Composable
fun Modifier.holoSurface(
    colors: List<Color>,
    tilt: () -> Offset,
    fringeDp: Float,
): Modifier {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return this
    val source = loadShaderFromAssetsOnce("holo_surface.agsl") ?: return this
    return holoSurfaceWithShader(source, colors, tilt, fringeDp)
}

/** Whether [holoSurface] can run on this device. */
val HoloSurfaceSupported: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun Modifier.holoSurfaceWithShader(
    source: String,
    colors: List<Color>,
    tilt: () -> Offset,
    fringeDp: Float,
): Modifier {
    val density = LocalDensity.current.density
    val stops = remember(colors) { colors.sampleEvenly(FOIL_STOPS) }

    // AGSL compiles at runtime, so a bad shader or a uniform fed the wrong way throws instead of
    // failing the build. Both are proven here once, against a dummy frame.
    val shader =
        remember(source, stops) {
            runCatching {
                RuntimeShader(source).also {
                    it.setFloatUniform("iResolution", 1f, 1f)
                    it.setFloatUniform("u_tilt", 0f, 0f)
                    it.setFloatUniform("u_shift", 0f, 0f)
                    it.setFloatUniform("u_intensity", FOIL_INTENSITY)
                    stops.forEachIndexed { i, c -> it.setFloatUniform("u_c$i", c.red, c.green, c.blue) }
                }
            }.onFailure { Timber.e(it, "holo_surface.agsl unusable — no holo finish") }
                .getOrNull()
        } ?: return this

    return graphicsLayer {
        val t = tilt()
        val strength = fringeDp * density
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("u_tilt", t.x, t.y)
        shader.setFloatUniform("u_shift", (t.x + RESTING_FRINGE) * strength, t.y * strength * 0.6f)
        renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "image").asComposeRenderEffect()
    }
}
