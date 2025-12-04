package com.ilustris.sagai.ui.theme.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import kotlin.random.Random

fun Genre.getRandomColorHex(): String {
    val palette = this.colorPalette().plus(this.color).distinct()

    if (palette.isEmpty()) {
        return "#FF000000" // Fallback to opaque black
    }
    if (palette.size == 1) {
        return palette.first().toHexString()
    }

    var randomColor: Color
    var tries = 0
    do {
        // Pick two distinct random colors from the palette
        val color1 = palette.random()
        val color2 = palette.filter { it != color1 }.random()

        // Generate a random fraction for interpolation
        val fraction = Random.nextFloat()

        // Interpolate between the two colors
        randomColor = lerp(color1, color2, fraction)
        tries++
    } while (randomColor.isPureWhiteOrBlack() && tries < 10) // Avoid getting stuck in a loop

    // If after many tries it's still pure, find the first non-pure color in the palette
    if (randomColor.isPureWhiteOrBlack()) {
        val fallbackColor = palette.firstOrNull { !it.isPureWhiteOrBlack() }
        if (fallbackColor != null) {
            return fallbackColor.toHexString()
        }
    }

    return randomColor.toHexString()
}

fun Color.isPureWhiteOrBlack(): Boolean {
    // Colors in Compose are floats from 0.0 to 1.0
    val red = red
    val green = green
    val blue = blue

    val isBlack = red == 0f && green == 0f && blue == 0f
    val isWhite = red == 1f && green == 1f && blue == 1f
    return isBlack || isWhite
}

fun Color.toHexString(): String {
    val alpha = (alpha * 255).toInt()
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()

    // Format to #AARRGGBB
    return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
}
