package com.ilustris.sagai.ui.theme.filters

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.core.ai.model.ShaderParamsConfig
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.brightness
import com.ilustris.sagai.ui.theme.colorTemperature
import com.ilustris.sagai.ui.theme.contrast
import com.ilustris.sagai.ui.theme.saturation
import com.ilustris.sagai.ui.theme.vignette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStreamReader

private val DEFAULT_RIM_ENERGY_COLOR = Triple(0.55f, 0.92f, 1.0f)
private val DEFAULT_RIM_ENERGY_OUTER_COLOR = Triple(0.15f, 0.45f, 1.0f)

private fun rimEnergyColorFromList(colors: List<Float>): Triple<Float, Float, Float> =
    if (colors.size == 3) {
        Triple(colors[0], colors[1], colors[2])
    } else {
        DEFAULT_RIM_ENERGY_COLOR
    }

private fun rimEnergyOuterColorFromList(colors: List<Float>): Triple<Float, Float, Float> =
    if (colors.size == 3) {
        Triple(colors[0], colors[1], colors[2])
    } else {
        DEFAULT_RIM_ENERGY_OUTER_COLOR
    }

internal fun RuntimeShader.setGenreFilterUniforms(
    composableSize: IntSize,
    scaleFactor: Float,
    uniformValues: ShaderParams,
    iTime: Float,
    selectiveParams: SelectiveColorParams?,
) {
    setFloatUniform(
        "iResolution",
        composableSize.width.toFloat(),
        composableSize.height.toFloat(),
    )
    setFloatUniform(
        "u_aspectRatio",
        composableSize.width.toFloat() / composableSize.height.toFloat(),
    )
    setFloatUniform("iTime", iTime)
    setFloatUniform("u_grainIntensity", uniformValues.grainIntensity * scaleFactor)
    setFloatUniform("u_bloomThreshold", uniformValues.bloomThreshold)
    setFloatUniform("u_bloomIntensity", uniformValues.bloomIntensity)
    setFloatUniform("u_bloomRadius", uniformValues.bloomRadius * scaleFactor)
    setFloatUniform("u_softFocusRadius", uniformValues.softFocusRadius * scaleFactor)
    setFloatUniform("u_saturation", uniformValues.saturation)
    setFloatUniform("u_contrast", uniformValues.contrast)
    setFloatUniform("u_brightness", uniformValues.brightness)
    setFloatUniform(
        "u_highlightTint",
        uniformValues.highlightTint.first,
        uniformValues.highlightTint.second,
        uniformValues.highlightTint.third,
    )
    setFloatUniform(
        "u_shadowTint",
        uniformValues.shadowTint.first,
        uniformValues.shadowTint.second,
        uniformValues.shadowTint.third,
    )
    setFloatUniform("u_tintStrength", uniformValues.tintStrength)
    setFloatUniform("u_vignetteStrength", uniformValues.vignetteStrength)
    setFloatUniform("u_vignetteSoftness", uniformValues.vignetteSoftness)
    setFloatUniform("u_pixelationBlockSize", uniformValues.pixelationBlockSize * scaleFactor)
    setFloatUniform("u_colorTemperature", uniformValues.colorTemperature)
    setFloatUniform("u_blackPoint", uniformValues.blackPoint)
    setFloatUniform("u_whitePoint", uniformValues.whitePoint)
    setFloatUniform("u_chromaticAberration", uniformValues.chromaticAberration * scaleFactor)
    setFloatUniform("u_scanlineIntensity", uniformValues.scanlineIntensity)
    setFloatUniform("u_scanlineDensity", uniformValues.scanlineDensity)
    setFloatUniform("u_posterizeLevels", uniformValues.posterizeLevels)
    setFloatUniform("u_halftoneScale", uniformValues.halftoneScale * scaleFactor)
    setFloatUniform("u_sharpenAmount", uniformValues.sharpenAmount)
    setFloatUniform("u_rimEnergyIntensity", uniformValues.rimEnergyIntensity)
    setFloatUniform(
        "u_rimEnergyColor",
        uniformValues.rimEnergyColor.first,
        uniformValues.rimEnergyColor.second,
        uniformValues.rimEnergyColor.third,
    )
    setFloatUniform(
        "u_rimEnergyOuterColor",
        uniformValues.rimEnergyOuterColor.first,
        uniformValues.rimEnergyOuterColor.second,
        uniformValues.rimEnergyOuterColor.third,
    )
    setFloatUniform("u_rimEnergyWidth", uniformValues.rimEnergyWidth * scaleFactor)
    setFloatUniform("u_wispIntensity", uniformValues.wispIntensity)
    setFloatUniform("u_wispSpeed", uniformValues.wispSpeed)
    setFloatUniform("u_progressiveBlurRadius", uniformValues.progressiveBlurRadius * scaleFactor)
    setFloatUniform(
        "u_progressiveBlurRange",
        uniformValues.progressiveBlurRange.first,
        uniformValues.progressiveBlurRange.second,
    )

    if (selectiveParams != null) {
        setFloatUniform("u_selectiveHighlightEnabled", 1f)
        setFloatUniform(
            "u_targetHighlightColorRGB",
            selectiveParams.targetColor.red,
            selectiveParams.targetColor.green,
            selectiveParams.targetColor.blue,
        )
        setFloatUniform("u_hueTolerance", selectiveParams.hueTolerance)
        setFloatUniform("u_saturationThreshold", selectiveParams.saturationThreshold)
        setFloatUniform("u_lightnessThreshold", selectiveParams.lightnessThreshold)
        setFloatUniform("u_highlightSaturationBoost", selectiveParams.highlightSaturationBoost)
        setFloatUniform("u_highlightLightnessBoost", selectiveParams.highlightLightnessBoost)
        setFloatUniform("u_desaturationFactorNonTarget", selectiveParams.desaturationFactorNonTarget)
    } else {
        setFloatUniform("u_selectiveHighlightEnabled", 0f)
    }
}

@Composable
fun loadShaderFromAssetsOnce(assetFileName: String): String? {
    val context = LocalContext.current
    return produceState<String?>(initialValue = null, key1 = assetFileName) {
        Timber.d("Attempting to load shader: $assetFileName")
        value =
            try {
                withContext(Dispatchers.IO) {
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
    if (visualConfig == null) return null
    val remote = visualConfig.shaderParams ?: ShaderParamsConfig()
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
        blackPoint = remote.blackPoint,
        whitePoint = remote.whitePoint.coerceIn(0.01f, 1f),
        chromaticAberration = remote.chromaticAberration,
        scanlineIntensity = remote.scanlineIntensity,
        scanlineDensity = remote.scanlineDensity,
        posterizeLevels = remote.posterizeLevels,
        halftoneScale = remote.halftoneScale,
        sharpenAmount = remote.sharpenAmount,
        rimEnergyIntensity = remote.rimEnergyIntensity,
        rimEnergyColor = rimEnergyColorFromList(remote.rimEnergyColor),
        rimEnergyOuterColor = rimEnergyOuterColorFromList(remote.rimEnergyOuterColor),
        rimEnergyWidth = remote.rimEnergyWidth,
        wispIntensity = remote.wispIntensity,
        wispSpeed = remote.wispSpeed,
    )
}

@Composable
fun Modifier.effectForGenre(
    genre: Genre?,
    visualConfig: GenreVisualConfig? = LocalGenreVisualConfig.current,
    focusRadius: Float? = null,
    customGrain: Float? = null,
    pixelSize: Float? = null,
    progressiveBlurRadius: Float? = null,
    progressiveBlurRange: Pair<Float, Float>? = null,
    useFallBack: Boolean = false,
    enableSelectiveHighlight: Boolean = false,
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
        )?.copy(
            progressiveBlurRadius = progressiveBlurRadius ?: 0f,
            progressiveBlurRange = progressiveBlurRange ?: (0f to 1f),
        )

    if (uniformValues == null) return this

    val selectiveParams =
        if (enableSelectiveHighlight) {
            genre.selectiveHighlight(visualConfig)
        } else {
            null
        }

    var iTime by remember { mutableFloatStateOf(0f) }

    if (rememberLifecycleAnimationsActive()) {
        LaunchedEffect(Unit) {
            var lastNanos = 0L
            while (true) {
                withFrameNanos { nanos ->
                    if (lastNanos != 0L) {
                        iTime += (nanos - lastNanos) / 1_000_000_000f
                    }
                    lastNanos = nanos
                }
            }
        }
    }

    return this
        .onSizeChanged { newSize ->
            composableSize = newSize
        }
        .graphicsLayer {
            if (composableSize.width > 0 && composableSize.height > 0) {
                val maxDim = maxOf(composableSize.width, composableSize.height).toFloat()
                val scaleFactor = (maxDim / 1080f).coerceIn(0.05f, 1f)

                runtimeShader.setGenreFilterUniforms(
                    composableSize = composableSize,
                    scaleFactor = scaleFactor,
                    uniformValues = uniformValues,
                    iTime = iTime,
                    selectiveParams = selectiveParams,
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
    val blackPoint: Float = 0.0f,
    val whitePoint: Float = 1.0f,
    val chromaticAberration: Float = 0.0f,
    val scanlineIntensity: Float = 0.0f,
    val scanlineDensity: Float = 2.0f,
    val posterizeLevels: Float = 0.0f,
    val halftoneScale: Float = 0.0f,
    val sharpenAmount: Float = 0.0f,
    val rimEnergyIntensity: Float = 0.0f,
    val rimEnergyColor: Triple<Float, Float, Float> = DEFAULT_RIM_ENERGY_COLOR,
    val rimEnergyOuterColor: Triple<Float, Float, Float> = DEFAULT_RIM_ENERGY_OUTER_COLOR,
    val rimEnergyWidth: Float = 8f,
    val wispIntensity: Float = 0f,
    val wispSpeed: Float = 3f,
    val progressiveBlurRadius: Float = 0f,
    val progressiveBlurRange: Pair<Float, Float> = 0f to 1f,
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
        modifier = modifier.saturation(saturation * fallbackReduction)
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
        val blurDp = (softFocusRadius * fallbackReduction).coerceAtMost(4f)
        modifier = modifier.blur(blurDp.dp)
    }
    return modifier
}
