package com.ilustris.sagai.ui.theme.filters

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.brightness
import com.ilustris.sagai.ui.theme.colorTemperature
import com.ilustris.sagai.ui.theme.contrast
import com.ilustris.sagai.ui.theme.grayScale
import com.ilustris.sagai.ui.theme.vignette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStreamReader

@Composable
fun loadShaderFromAssetsOnce(assetFileName: String): String? {
    val context = LocalContext.current
    // produceState ensures this runs once and caches the result across recompositions
    // unless assetFileName changes.
    return produceState<String?>(initialValue = null, key1 = assetFileName) {
        Timber.d("Attempting to load shader: $assetFileName")
        value =
            try {
                withContext(Dispatchers.IO) {
                    // Perform file I/O on a background thread
                    context.assets.open(assetFileName).use { inputStream ->
                        InputStreamReader(inputStream).use { reader ->
                            reader.readText()
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading shader '$assetFileName': ${e.message}")
                null
            }
    }.value
}

@Composable
fun Genre.colorTones(visualConfig: GenreVisualConfig? = LocalGenreVisualConfig.current): ColorTonePalette? {
    val remote = visualConfig?.colorTones ?: return null
    // We only return null if the crucial lists are not 3-elements long (RGB)
    if (remote.highlightTint.size != 3 || remote.shadowTint.size != 3) return null
    return ColorTonePalette(
        name = remote.name.ifBlank { "Remote" },
        highlightTint =
            Triple(
                remote.highlightTint[0],
                remote.highlightTint[1],
                remote.highlightTint[2],
            ),
        shadowTint = Triple(remote.shadowTint[0], remote.shadowTint[1], remote.shadowTint[2]),
        defaultTintStrength = remote.defaultTintStrength,
    )
}

@Composable
fun Genre.shaderParams(
    customGrain: Float? = null,
    focusRadius: Float? = null,
    pixelSize: Float? = null,
    visualConfig: GenreVisualConfig? = LocalGenreVisualConfig.current,
): ShaderParams? {
    val remote = visualConfig?.shaderParams ?: return null
    val tones = colorTones(visualConfig)
    return ShaderParams(
        grainIntensity = customGrain ?: remote.grainIntensity,
        bloomThreshold = remote.bloomThreshold,
        bloomIntensity = remote.bloomIntensity,
        bloomRadius = remote.bloomRadius,
        softFocusRadius = focusRadius ?: remote.softFocusRadius,
        saturation = remote.saturation,
        contrast = remote.contrast,
        brightness = remote.brightness,
        highlightTint =
            if (remote.highlightTint.size == 3) {
                Triple(
                    remote.highlightTint[0],
                    remote.highlightTint[1],
                    remote.highlightTint[2],
                )
            } else {
                tones?.highlightTint ?: Triple(1f, 1f, 1f)
            },
        shadowTint =
            if (remote.shadowTint.size == 3) {
                Triple(
                    remote.shadowTint[0],
                    remote.shadowTint[1],
                    remote.shadowTint[2],
                )
            } else {
                tones?.shadowTint ?: Triple(0f, 0f, 0f)
            },
        tintStrength = remote.tintStrength.takeIf { it != 0f } ?: tones?.defaultTintStrength ?: 0f,
        vignetteStrength = remote.vignetteStrength,
        vignetteSoftness = remote.vignetteSoftness,
        pixelationBlockSize = pixelSize ?: remote.pixelationBlockSize,
        colorTemperature = remote.colorTemperature,
    )
}

@Composable
fun Modifier.effectForGenre(
    genre: Genre?,
    visualConfig: GenreVisualConfig? = LocalGenreVisualConfig.current,
    focusRadius: Float? = null,
    customGrain: Float? = null,
    pixelSize: Float? = null,
    useFallBack: Boolean = false,
): Modifier {
    if (genre == null) return this
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || useFallBack) {
        return this.fallbackEffect(genre, visualConfig)
    }

    val agslShaderSource = loadShaderFromAssetsOnce("genre_filter_shader.agsl")
    if (agslShaderSource == null) {
        Timber.w("AGSL Shader source is null. Applying no effect. Applying fallback")
        return this.fallbackEffect(genre)
    }

    // Remember the shader instance itself, it's cheap to create if source is available
    val runtimeShader =
        remember(agslShaderSource) {
            RuntimeShader(agslShaderSource)
        }

    var composableSize by remember { mutableStateOf(IntSize.Zero) }

    val uniformValues =
        genre.shaderParams(
            customGrain = customGrain,
            focusRadius = focusRadius,
            pixelSize = pixelSize,
            visualConfig = visualConfig,
        )

    if (uniformValues == null) return this

    return this
        .onSizeChanged { newSize ->
            composableSize = newSize
        }.graphicsLayer {
            if (composableSize.width > 0 && composableSize.height > 0) {
                // Adaptive scale factor based on an assumed 1080px reference.
                // Weakens grain and pixel-based blurs on small elements (like avatars) to prevent destroying legibility.
                val maxDim = maxOf(composableSize.width, composableSize.height).toFloat()
                val scaleFactor = (maxDim / 1080f).coerceIn(0.05f, 1f)

                runtimeShader.setFloatUniform(
                    "iResolution",
                    composableSize.width.toFloat(),
                    composableSize.height.toFloat(),
                )
                runtimeShader.setFloatUniform(
                    "u_aspectRatio",
                    composableSize.width.toFloat() / composableSize.height.toFloat(),
                )
                runtimeShader.setFloatUniform(
                    "u_grainIntensity",
                    uniformValues.grainIntensity * scaleFactor,
                )
                runtimeShader.setFloatUniform("u_bloomThreshold", uniformValues.bloomThreshold)
                runtimeShader.setFloatUniform("u_bloomIntensity", uniformValues.bloomIntensity)
                runtimeShader.setFloatUniform(
                    "u_bloomRadius",
                    uniformValues.bloomRadius * scaleFactor,
                )
                runtimeShader.setFloatUniform(
                    "u_softFocusRadius",
                    uniformValues.softFocusRadius * scaleFactor,
                )
                runtimeShader.setFloatUniform("u_saturation", uniformValues.saturation)
                runtimeShader.setFloatUniform("u_contrast", uniformValues.contrast)
                runtimeShader.setFloatUniform("u_brightness", uniformValues.brightness)
                runtimeShader.setFloatUniform(
                    "u_highlightTint",
                    uniformValues.highlightTint.first,
                    uniformValues.highlightTint.second,
                    uniformValues.highlightTint.third,
                )
                runtimeShader.setFloatUniform(
                    "u_shadowTint",
                    uniformValues.shadowTint.first,
                    uniformValues.shadowTint.second,
                    uniformValues.shadowTint.third,
                )
                runtimeShader.setFloatUniform("u_tintStrength", uniformValues.tintStrength)
                runtimeShader.setFloatUniform("u_vignetteStrength", uniformValues.vignetteStrength)
                runtimeShader.setFloatUniform("u_vignetteSoftness", uniformValues.vignetteSoftness)
                runtimeShader.setFloatUniform(
                    "u_pixelationBlockSize",
                    uniformValues.pixelationBlockSize * scaleFactor,
                )
                runtimeShader.setFloatUniform(
                    "u_colorTemperature",
                    uniformValues.colorTemperature,
                ) // Set the new uniform

                renderEffect =
                    RenderEffect
                        .createRuntimeShaderEffect(runtimeShader, "composable_shader")
                        .asComposeRenderEffect()
            } else {
                renderEffect = null
            }
        }
}

data class ShaderParams(
    val grainIntensity: Float = 0.0f,
    val bloomThreshold: Float = 0.8f,
    val bloomIntensity: Float = 0.0f,
    val bloomRadius: Float = 3.0f,
    val softFocusRadius: Float = 0.0f,
    val saturation: Float = 1.0f,
    val contrast: Float = 1.0f,
    val brightness: Float = 0.0f,
    val highlightTint: Triple<Float, Float, Float> = Triple(1f, 1f, 1f),
    val shadowTint: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val tintStrength: Float = 0.0f,
    val vignetteStrength: Float = 0.0f,
    val vignetteSoftness: Float = 0.5f,
    val pixelationBlockSize: Float = 0.0f,
    val colorTemperature: Float = 0.0f,
)

@Composable
fun Modifier.fallbackEffect(
    genre: Genre,
    visualConfig: GenreVisualConfig? = LocalGenreVisualConfig.current,
): Modifier {
    val shaderParams = genre.shaderParams(visualConfig = visualConfig) ?: return this
    val saturation = shaderParams.saturation
    val brightnessValue = shaderParams.brightness
    val contrastValue = shaderParams.contrast
    val colorTemperature = shaderParams.colorTemperature
    val vignetteStrength = shaderParams.vignetteStrength
    val vignetteSoftness = shaderParams.vignetteSoftness
    val softFocusRadius = shaderParams.softFocusRadius
    val fallbackReduction = .5f
    var modifier: Modifier = this

    if (saturation != 1.0f) {
        modifier = modifier.grayScale(saturation * fallbackReduction)
    }
    if (brightnessValue != 0f) {
        modifier = modifier.brightness(brightnessValue * fallbackReduction)
    }
    if (contrastValue != 1.0f) {
        modifier = modifier.contrast(contrastValue * fallbackReduction)
    }
    if (colorTemperature != 0f) {
        modifier = modifier.colorTemperature(colorTemperature * fallbackReduction)
    }
    if (vignetteStrength > 0f) {
        modifier =
            modifier.vignette(
                vignetteStrength * fallbackReduction,
                vignetteSoftness * fallbackReduction,
            )
    }
    if (softFocusRadius > 0f) {
        modifier = modifier.blur((softFocusRadius * fallbackReduction).dp)
    }
    return modifier
}
