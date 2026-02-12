package com.ilustris.sagai.core.ai.model

import androidx.compose.runtime.staticCompositionLocalOf
import com.google.gson.annotations.SerializedName

/**
 * CompositionLocal to provide [GenreVisualConfig] to the UI.
 */
val LocalGenreVisualConfig = staticCompositionLocalOf<GenreVisualConfig?> { null }

/**
 * Remote Config model for all remoteable visual properties of a genre.
 * Fetched using the key `<genre_name>_visual_config`.
 *
 * All fields have sensible defaults so that partial JSON payloads work fine.
 * Extensions in Genre.kt, Shapes.kt, and Filters.kt use these values as overrides
 * with compiled fallbacks when the remote value is absent (empty/default).
 */
data class GenreVisualConfig(
    // ── Colors ──────────────────────────────────────────────────────
    @SerializedName("primaryColor")
    val primaryColor: String = "",
    @SerializedName("iconColor")
    val iconColor: String = "",
    @SerializedName("colorPalette")
    val colorPalette: List<String> = emptyList(),
    // ── Shape ────────────────────────────────────────────────────────
    @SerializedName("cornerSizeDp")
    val cornerSizeDp: Float = 0f,
    // ── Haptics ─────────────────────────────────────────────────────
    @SerializedName("vibrationPattern")
    val vibrationPattern: List<Long> = emptyList(),
    // ── Selective Highlight Filter ──────────────────────────────────
    @SerializedName("selectiveHighlight")
    val selectiveHighlight: SelectiveHighlightConfig? = null,
    // ── Shader Params (post-processing) ─────────────────────────────
    @SerializedName("shaderParams")
    val shaderParams: ShaderParamsConfig? = null,
    // ── Color Tones ─────────────────────────────────────────────────
    @SerializedName("colorTones")
    val colorTones: ColorTonesConfig? = null,
    // ── Remote Images (Phase 2) ─────────────────────────────────────
    @SerializedName("backgroundUrl")
    val backgroundUrl: String = "",
    @SerializedName("headerImageUrl")
    val headerImageUrl: String = "",
)

/**
 * Remote representation of [SelectiveColorParams].
 * The `targetColor` is not included here because it always derives from the genre's primary color.
 */
data class SelectiveHighlightConfig(
    @SerializedName("hueTolerance")
    val hueTolerance: Float = 0.2f,
    @SerializedName("saturationThreshold")
    val saturationThreshold: Float = 0.02f,
    @SerializedName("lightnessThreshold")
    val lightnessThreshold: Float = 0.05f,
    @SerializedName("highlightSaturationBoost")
    val highlightSaturationBoost: Float = 1.0f,
    @SerializedName("highlightLightnessBoost")
    val highlightLightnessBoost: Float = 0.0f,
    @SerializedName("desaturationFactorNonTarget")
    val desaturationFactorNonTarget: Float = 0.0f,
)

/**
 * Remote representation of [ShaderParams].
 * Uses sensible defaults for neutral state if fields are omitted in JSON.
 */
data class ShaderParamsConfig(
    @SerializedName("grainIntensity")
    val grainIntensity: Float = 0f,
    @SerializedName("bloomThreshold")
    val bloomThreshold: Float = 0.8f,
    @SerializedName("bloomIntensity")
    val bloomIntensity: Float = 0f,
    @SerializedName("bloomRadius")
    val bloomRadius: Float = 3f,
    @SerializedName("softFocusRadius")
    val softFocusRadius: Float = 0f,
    @SerializedName("saturation")
    val saturation: Float = 1f,
    @SerializedName("contrast")
    val contrast: Float = 1f,
    @SerializedName("brightness")
    val brightness: Float = 0f,
    @SerializedName("highlightTint")
    val highlightTint: List<Float> = emptyList(),
    @SerializedName("shadowTint")
    val shadowTint: List<Float> = emptyList(),
    @SerializedName("tintStrength")
    val tintStrength: Float = 0f,
    @SerializedName("vignetteStrength")
    val vignetteStrength: Float = 0f,
    @SerializedName("vignetteSoftness")
    val vignetteSoftness: Float = 0.5f,
    @SerializedName("pixelationBlockSize")
    val pixelationBlockSize: Float = 0f,
    @SerializedName("colorTemperature")
    val colorTemperature: Float = 0f,
)

/**
 * Remote representation of [ColorTonePalette].
 */
data class ColorTonesConfig(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("highlightTint")
    val highlightTint: List<Float> = emptyList(),
    @SerializedName("shadowTint")
    val shadowTint: List<Float> = emptyList(),
    @SerializedName("defaultTintStrength")
    val defaultTintStrength: Float = 0.3f,
)
