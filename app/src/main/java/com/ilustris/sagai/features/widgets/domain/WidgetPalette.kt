package com.ilustris.sagai.features.widgets.domain

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Colors of one widget, as ARGB ints so the whole thing stays testable without Android or Compose.
 *
 * Built from a genre's `colorPalette` (Remote Config) and then checked for contrast: text colors
 * are guaranteed to read on [base], and [onAccent] on [accent].
 */
data class WidgetPalette(
    val base: Int,
    val baseAlt: Int,
    val text: Int,
    val textMuted: Int,
    val accent: Int,
    val onAccent: Int,
    val glow: Int,
    val bubble: Int,
    val isLight: Boolean,
)

object WidgetPaletteFactory {
    private const val BLACK = 0xFF000000.toInt()
    private const val WHITE = 0xFFFFFFFF.toInt()
    private const val INK = 0xFF17120F.toInt()
    private const val PAPER = 0xFFF1EDE3.toInt()
    private const val PAPER_ALT = 0xFFE4DDCC.toInt()
    private const val INK_MUTED = 0xFF5B524A.toInt()
    private const val MIN_TEXT_CONTRAST = 4.5
    private const val BASE_DEEPEN = 0.4f
    private const val MAX_BASE_LUMINANCE = 0.06

    /**
     * @param colors the genre's palette, in any order (empty is fine)
     * @param primary the genre's main color, used when the palette is empty or has no bright color
     * @param light true when the artwork is ink on paper: text goes dark on a paper-colored base
     */
    fun create(
        colors: List<Int>,
        primary: Int,
        light: Boolean,
    ): WidgetPalette {
        val opaque = colors.map { it or BLACK }.ifEmpty { listOf(primary or BLACK) }
        return if (light) lightPalette(primary or BLACK, opaque) else darkPalette(primary or BLACK, opaque)
    }

    private fun darkPalette(
        primary: Int,
        colors: List<Int>,
    ): WidgetPalette {
        // The widget wears the genre's own primary colour (blue for Heroes, red for Shinobi), deepened only
        // until white text reads on it with room to spare. The palette's darkest entry is a near-black
        // backdrop tint and made every widget look black.
        var base = blend(primary, BLACK, BASE_DEEPEN)
        var guard = 0
        while (luminance(base) > MAX_BASE_LUMINANCE && guard < 8) {
            base = blend(base, BLACK, 0.2f)
            guard++
        }
        val baseAlt = blend(base, BLACK, 0.4f)
        val accent =
            colors
                .filter { luminance(it) >= 0.25 }
                .maxWithOrNull(compareBy<Int>({ saturation(it) }, { luminance(it) }))
                ?: blend(primary, WHITE, 0.4f)
        val glow =
            colors
                .filter { luminance(it) in 0.05..0.6 }
                .maxByOrNull { saturation(it) }
                ?: accent
        var muted = blend(WHITE, base, 0.65f)
        if (contrast(muted, base) < MIN_TEXT_CONTRAST) muted = WHITE
        return WidgetPalette(
            base = base,
            baseAlt = baseAlt,
            text = WHITE,
            textMuted = muted,
            accent = accent,
            onAccent = readableOn(accent),
            glow = glow,
            bubble = 0x1FFFFFFF,
            isLight = false,
        )
    }

    private fun lightPalette(
        primary: Int,
        colors: List<Int>,
    ): WidgetPalette {
        val accent = ensureContrast(primary, PAPER, MIN_TEXT_CONTRAST)
        val glow = colors.maxByOrNull { saturation(it) } ?: accent
        return WidgetPalette(
            base = PAPER,
            baseAlt = PAPER_ALT,
            text = INK,
            textMuted = INK_MUTED,
            accent = accent,
            onAccent = readableOn(accent),
            glow = glow,
            bubble = 0x14170F12,
            isLight = true,
        )
    }

    /** Darkens [color] until it reads on [background], giving up after a few steps. */
    fun ensureContrast(
        color: Int,
        background: Int,
        minRatio: Double,
    ): Int {
        var current = color
        var steps = 0
        while (contrast(current, background) < minRatio && steps < 8) {
            current = blend(current, BLACK, 0.15f)
            steps++
        }
        return current
    }

    /** Black or white, whichever reads better on [background]. */
    fun readableOn(background: Int): Int = if (contrast(INK, background) >= contrast(WHITE, background)) INK else WHITE

    /** WCAG relative luminance, 0 (black) to 1 (white). */
    fun luminance(argb: Int): Double {
        fun channel(shift: Int): Double {
            val v = ((argb shr shift) and 0xFF) / 255.0
            return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }

    /** WCAG contrast ratio, 1 to 21. */
    fun contrast(
        first: Int,
        second: Int,
    ): Double {
        val a = luminance(first)
        val b = luminance(second)
        return (max(a, b) + 0.05) / (min(a, b) + 0.05)
    }

    /** HSV saturation, 0 to 1. */
    fun saturation(argb: Int): Double {
        val r = ((argb shr 16) and 0xFF) / 255.0
        val g = ((argb shr 8) and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0
        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        return if (maxC == 0.0) 0.0 else (maxC - minC) / maxC
    }

    /** [amount] of [to] mixed into [from], opaque result. */
    fun blend(
        from: Int,
        to: Int,
        amount: Float,
    ): Int {
        fun channel(shift: Int): Int {
            val a = (from shr shift) and 0xFF
            val b = (to shr shift) and 0xFF
            return (a + (b - a) * amount).toInt().coerceIn(0, 255)
        }
        return BLACK or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }
}
