package com.ilustris.sagai.ui.theme.utils

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Generates a random unique color for characters with restrictions:
 * - Avoids pure black and white
 * - Avoids too dark colors (close to black)
 * - Avoids too light colors (close to white)
 * - Ensures good contrast and visibility
 */
fun getRandomColorHex(): String {
    var randomColor: Color
    var attempts = 0
    val maxAttempts = 50

    do {
        // Generate random RGB values with constraints
        // Avoid extremes: min 0.15 (avoid too dark) and max 0.85 (avoid too light)
        val red = Random.nextFloat() * 0.7f + 0.15f   // Range: 0.15 to 0.85
        val green = Random.nextFloat() * 0.7f + 0.15f // Range: 0.15 to 0.85
        val blue = Random.nextFloat() * 0.7f + 0.15f  // Range: 0.15 to 0.85

        randomColor = Color(red, green, blue, 1.0f)
        attempts++
    } while (randomColor.isInvalidColor() && attempts < maxAttempts)

    // Fallback to a predefined set of good colors if we couldn't generate one
    if (randomColor.isInvalidColor()) {
        val fallbackColors = listOf(
            Color(0xFF8B2635), // Deep Ruby Red
            Color(0xFF2E294E), // Dark Purple
            Color(0xFF1C2541), // Dark Navy
            Color(0xFF003F88), // Classic Blue
            Color(0xFFE91E63), // Hot Pink
            Color(0xFF5C2751), // Deep Plum
            Color(0xFF0081A7), // Space Teal
            Color(0xFF8B4513), // Saddle Brown
        )
        randomColor = fallbackColors.random()
    }

    return randomColor.toHexString()
}

/**
 * Checks if a color is invalid for character use
 */
private fun Color.isInvalidColor(): Boolean {
    val red = red
    val green = green
    val blue = blue

    // Check for pure black or white
    val isPureBlack = red == 0f && green == 0f && blue == 0f
    val isPureWhite = red == 1f && green == 1f && blue == 1f

    // Check if color is too dark (all components below threshold)
    val tooDark = red < 0.1f && green < 0.1f && blue < 0.1f

    // Check if color is too light (all components above threshold)
    val tooLight = red > 0.9f && green > 0.9f && blue > 0.9f

    // Check if color lacks contrast (all components too similar - gray-ish)
    val maxComponent = maxOf(red, green, blue)
    val minComponent = minOf(red, green, blue)
    val lacksContrast = (maxComponent - minComponent) < 0.1f

    return isPureBlack || isPureWhite || tooDark || tooLight || lacksContrast
}

fun Color.toHexString(): String {
    val alpha = (alpha * 255).toInt()
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()

    // Format to #AARRGGBB
    return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
}
