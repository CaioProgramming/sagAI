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
    val cornerSizeDp: Float = -1f,
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
    val hueTolerance: Float = -1f,
    @SerializedName("saturationThreshold")
    val saturationThreshold: Float = -1f,
    @SerializedName("lightnessThreshold")
    val lightnessThreshold: Float = -1f,
    @SerializedName("highlightSaturationBoost")
    val highlightSaturationBoost: Float = -1f,
    @SerializedName("highlightLightnessBoost")
    val highlightLightnessBoost: Float = -1f,
    @SerializedName("desaturationFactorNonTarget")
    val desaturationFactorNonTarget: Float = -1f,
)

/**
 * Remote representation of [ShaderParams].
 * Uses -1f as sentinel to distinguish "not set" from "set to 0".
 */
data class ShaderParamsConfig(
    @SerializedName("grainIntensity")
    val grainIntensity: Float = -1f,
    @SerializedName("bloomThreshold")
    val bloomThreshold: Float = -1f,
    @SerializedName("bloomIntensity")
    val bloomIntensity: Float = -1f,
    @SerializedName("bloomRadius")
    val bloomRadius: Float = -1f,
    @SerializedName("softFocusRadius")
    val softFocusRadius: Float = -1f,
    @SerializedName("saturation")
    val saturation: Float = -1f,
    @SerializedName("contrast")
    val contrast: Float = -1f,
    @SerializedName("brightness")
    val brightness: Float = -1f,
    @SerializedName("highlightTint")
    val highlightTint: List<Float> = emptyList(),
    @SerializedName("shadowTint")
    val shadowTint: List<Float> = emptyList(),
    @SerializedName("tintStrength")
    val tintStrength: Float = -1f,
    @SerializedName("vignetteStrength")
    val vignetteStrength: Float = -1f,
    @SerializedName("vignetteSoftness")
    val vignetteSoftness: Float = -1f,
    @SerializedName("pixelationBlockSize")
    val pixelationBlockSize: Float = -1f,
    @SerializedName("colorTemperature")
    val colorTemperature: Float = -1f,
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
    val defaultTintStrength: Float = -1f,
)
