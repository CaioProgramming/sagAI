package com.ilustris.sagai.ui.theme.filters

data class ColorTonePalette(
    val name: String,
    val highlightTint: Triple<Float, Float, Float>,
    val shadowTint: Triple<Float, Float, Float>,
    val defaultTintStrength: Float = 0.3f,
)
