package com.ilustris.sagai.ui.theme.filters

import androidx.compose.ui.graphics.Color

private const val DEFAULT_TOLERANCE = 0.20f
private const val DEFAULT_SATURATION_THRESHOLD = 0.02f
private const val DEFAULT_LIGHTNESS_THRESHOLD = 0.05f
private const val DEFAULT_HIGHLIGHT_SATURATION_BOOST = 1f
private const val DEFAULT_HIGHLIGHT_LIGHTNESS_BOOST = 0.05f
private const val DEFAULT_DESATURATION_FACTOR_NON_TARGET = 0f

/**
 * Parameters for the selective colour highlight — the "pop out" that keeps one hue saturated and
 * drains the rest. The target colour always derives from the genre's primary
 * (see [com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight]).
 *
 * The effect itself has no modifier of its own: it is a stage inside the genre filter shader,
 * switched on by `effectForGenre(enableSelectiveHighlight = true)`, so it costs nothing extra
 * beyond the pass that screen was already paying for.
 */
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
