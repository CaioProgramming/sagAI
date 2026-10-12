package com.ilustris.sagai.features.share.ui.card

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import com.ilustris.sagai.ui.theme.filters.loadShaderFromAssetsOnce
import timber.log.Timber

private const val FOIL_STOPS = 5
private const val FOIL_INTENSITY = 0.9f

/** Colour dodge tints the art with the foil; it needs API 29, below that Screen is the closest. */
private val FoilBlend: BlendMode
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) BlendMode.ColorDodge else BlendMode.Screen

private val GlareBlend: BlendMode
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) BlendMode.Softlight else BlendMode.Screen

/**
 * The holographic foil laid over a collector card's art: a band of the genre's own palette that
 * slides across the card as it tilts, plus a soft glare that follows the same tilt.
 *
 * [tilt] is read inside the draw phase, so a tilt update repaints this layer without recomposing
 * anything around it. Draw it over an offscreen layer; it only brightens.
 *
 * Needs API 33 for the AGSL shader. Below that, and whenever the shader cannot be built or fed, a
 * plain palette gradient offset by the tilt stands in: the same colours and the same motion, without
 * the fine grain.
 */
@Composable
fun HoloFoil(
    colors: List<Color>,
    tilt: () -> Offset,
    modifier: Modifier = Modifier,
) {
    val stops = remember(colors) { colors.sampleEvenly(FOIL_STOPS) }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        FoilCanvas(modifier, stops, tilt, shader = null)
        return
    }

    val source = loadShaderFromAssetsOnce("holo_foil.agsl")
    if (source == null) {
        // Still loading off the main thread, or the asset is missing. The fallback is a full foil
        // on its own, so there is never a frame without one.
        FoilCanvas(modifier, stops, tilt, shader = null)
        return
    }

    FoilWithShader(modifier, stops, tilt, source)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun FoilWithShader(
    modifier: Modifier,
    stops: List<Color>,
    tilt: () -> Offset,
    source: String,
) {
    // AGSL compiles at runtime, so a bad shader, or a uniform fed the wrong way, throws instead of
    // failing the build. Both are proven here once against a dummy frame: whatever survives is safe
    // to drive every frame without a guard in the draw path.
    val shader =
        remember(source, stops) {
            runCatching {
                RuntimeShader(source).also { it.applyFoilUniforms(1f, 1f, Offset.Zero, stops) }
            }.onFailure { Timber.e(it, "holo_foil.agsl unusable — falling back") }
                .getOrNull()
        }
    FoilCanvas(modifier, stops, tilt, shader)
}

@Composable
private fun FoilCanvas(
    modifier: Modifier,
    stops: List<Color>,
    tilt: () -> Offset,
    shader: Any?,
) {
    val brush = remember(shader) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) (shader as? RuntimeShader)?.let(::ShaderBrush) else null }

    Box(
        modifier.drawBehind {
            if (size.minDimension <= 0f) return@drawBehind
            val t = tilt()

            if (brush != null && shader is RuntimeShader && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                shader.applyFoilUniforms(size.width, size.height, t, stops)
                drawRect(brush, blendMode = FoilBlend)
            } else {
                val sweep = size.width * (1.2f * t.x)
                drawRect(
                    brush =
                        Brush.linearGradient(
                            colors = stops.map { it.copy(alpha = 0.45f) },
                            start = Offset(sweep - size.width * 0.4f, 0f),
                            end = Offset(sweep + size.width * 1.4f, size.height),
                        ),
                    blendMode = FoilBlend,
                )
            }

            drawRect(
                brush =
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(size.width * (0.5f + t.x * 0.5f), size.height * (0.5f + t.y * 0.5f)),
                        radius = size.maxDimension * 0.6f,
                    ),
                blendMode = GlareBlend,
            )
        },
    )
}

/** Every uniform `holo_foil.agsl` takes, in one place so construction and drawing share it. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun RuntimeShader.applyFoilUniforms(
    width: Float,
    height: Float,
    tilt: Offset,
    stops: List<Color>,
) {
    setFloatUniform("iResolution", width, height)
    setFloatUniform("u_tilt", tilt.x, tilt.y)
    setFloatUniform("u_intensity", FOIL_INTENSITY)
    stops.forEachIndexed { index, color ->
        setFloatUniform("u_c$index", color.red, color.green, color.blue)
    }
}

/** [count] colours picked evenly across the list, keeping its order and both ends. */
private fun List<Color>.sampleEvenly(count: Int): List<Color> {
    if (isEmpty()) return List(count) { Color.White }
    return List(count) { i -> this[((i * (size - 1)) / (count - 1).coerceAtLeast(1)).coerceIn(0, size - 1)] }
}
