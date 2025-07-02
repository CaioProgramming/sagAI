package com.ilustris.sagai.ui.theme.filters

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import loadShaderFromAssetsOnce

private const val DEFAULT_TOLERANCE = 0.25f
private const val DEFAULT_SATURATION_THRESHOLD = 0.10f
private const val DEFAULT_LIGHTNESS_THRESHOLD = 0.20f
private const val DEFAULT_HIGHLIGHT_SATURATION_BOOST = 1.5f
private const val DEFAULT_HIGHLIGHT_LIGHTNESS_BOOST = 0.15f
private const val DEFAULT_DESATURATION_FACTOR_NON_TARGET = 0.05f

data class SelectiveColorParams(
    val targetColor: Color,
    val hueTolerance: Float = DEFAULT_TOLERANCE,
    val saturationThreshold: Float = DEFAULT_SATURATION_THRESHOLD,
    val lightnessThreshold: Float = DEFAULT_LIGHTNESS_THRESHOLD,
    val highlightSaturationBoost: Float = DEFAULT_HIGHLIGHT_SATURATION_BOOST,
    val highlightLightnessBoost: Float = DEFAULT_HIGHLIGHT_LIGHTNESS_BOOST,
    val desaturationFactorNonTarget: Float = DEFAULT_DESATURATION_FACTOR_NON_TARGET,
) {
    constructor(targetColor: Color) : this(
        targetColor = targetColor,
        hueTolerance = DEFAULT_TOLERANCE,
        saturationThreshold = DEFAULT_SATURATION_THRESHOLD,
        lightnessThreshold = DEFAULT_LIGHTNESS_THRESHOLD,
        highlightSaturationBoost = DEFAULT_HIGHLIGHT_SATURATION_BOOST,
        highlightLightnessBoost = DEFAULT_HIGHLIGHT_LIGHTNESS_BOOST,
        desaturationFactorNonTarget = DEFAULT_DESATURATION_FACTOR_NON_TARGET,
    )
}

@Composable
fun Modifier.selectiveColorHighlight(
    params: SelectiveColorParams,
    shaderAssetFileName: String = "selective_color_highlight.agsl",
): Modifier {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Log.w("SelectiveColor", "Shader effects not supported on this API level.")
        return this // Or a fallback using ColorMatrix if desired
    }

    val agslShaderSource = loadShaderFromAssetsOnce(shaderAssetFileName) // From your Filters.kt

    if (agslShaderSource == null) {
        // Log.w("SelectiveColor", "AGSL Shader source '$shaderAssetFileName' is null.")
        return this
    }

    val runtimeShader =
        remember(agslShaderSource) {
            RuntimeShader(agslShaderSource)
        }

    var composableSize by remember { mutableStateOf(IntSize.Zero) }

    return this
        .onSizeChanged { newSize ->
            composableSize = newSize
        }.graphicsLayer {
            if (composableSize.width > 0 && composableSize.height > 0) {
                runtimeShader.setFloatUniform(
                    "iResolution",
                    composableSize.width.toFloat(),
                    composableSize.height.toFloat(),
                )
                // if (agslShaderSource.contains("iTime")) {
                //     runtimeShader.setFloatUniform("iTime", timeState)
                // }

                // Convert Compose Color to normalized RGB for the shader
                val targetColorRgb =
                    floatArrayOf(
                        params.targetColor.red,
                        params.targetColor.green,
                        params.targetColor.blue,
                    )
                runtimeShader.setFloatUniform("u_targetHighlightColorRGB", targetColorRgb)

                runtimeShader.setFloatUniform("u_hueTolerance", params.hueTolerance)
                runtimeShader.setFloatUniform("u_saturationThreshold", params.saturationThreshold)
                runtimeShader.setFloatUniform("u_lightnessThreshold", params.lightnessThreshold)
                runtimeShader.setFloatUniform(
                    "u_highlightSaturationBoost",
                    params.highlightSaturationBoost,
                )
                runtimeShader.setFloatUniform(
                    "u_highlightLightnessBoost",
                    params.highlightLightnessBoost,
                )
                runtimeShader.setFloatUniform(
                    "u_desaturationFactorNonTarget",
                    params.desaturationFactorNonTarget,
                )

                renderEffect =
                    RenderEffect
                        .createRuntimeShaderEffect(runtimeShader, "composable_shader")
                        .asComposeRenderEffect()
            } else {
                renderEffect = null
            }
        }
}
