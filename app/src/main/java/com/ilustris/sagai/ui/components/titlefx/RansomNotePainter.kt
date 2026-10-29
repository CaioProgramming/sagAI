package com.ilustris.sagai.ui.components.titlefx

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Punk Rock title: every letter is a cut-out tile with its own font, case, colour and tilt, like a
 * ransom note. Port of `paintRansom` / `ransomStep` in the HTML lab (`docs/prototypes/genre-titles`).
 *
 * It paints two layers the shader reads: the mask (white tiles, for the drop shadow and the glow)
 * and the colour layer (tile, border and letter). Fonts, case and colour reshuffle every 5 s, the
 * tiles jitter at 2 fps, the entry reveals one letter every 0.24 s, and tiles near the finger drift
 * away and tilt.
 */
internal class RansomNotePainter(
    private val typefaces: List<Typeface>,
) {
    private class Tile(
        val index: Int,
        val glyph: String,
        val font: Int,
        val size: Float,
        val tint: Int,
        val width: Float,
    )

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    // per-letter scatter pushed by the finger (x, y in px, rotation in radians) and last centres
    private var scatter = FloatArray(0)
    private var centres = FloatArray(0)
    private var tileHeight = 1f
    private var lastKey = ""
    private var lastPaintMs = 0L

    /** Moves the tiles away from the finger; returns true while any tile is still displaced. */
    fun step(
        dt: Float,
        fingerX: Float,
        fingerY: Float,
        active: Float,
    ): Boolean {
        val r = tileHeight * 0.75f
        val push = tileHeight * 0.17f
        val a = min(1f, dt * 8f)
        var moved = false
        for (i in 0 until centres.size / 2) {
            val cx = centres[i * 2]
            if (cx.isNaN()) continue
            val dx = cx - fingerX
            val dy = centres[i * 2 + 1] - fingerY
            val d = hypot(dx, dy) + 1e-3f
            var f = max(0f, 1f - d / r)
            f = f * f * active
            val o = i * 3
            scatter[o] += (dx / d * f * push - scatter[o]) * a
            scatter[o + 1] += (dy / d * f * push - scatter[o + 1]) * a
            scatter[o + 2] += ((if (dx > 0f) 1f else -1f) * f * 0.5f - scatter[o + 2]) * a
            if (abs(scatter[o]) + abs(scatter[o + 1]) + abs(scatter[o + 2]) > 0.05f) moved = true
        }
        return moved
    }

    /** Whether the layers must be repainted: a new 5 s identity, a 2 fps jitter, a revealed letter, or a push. */
    fun needsPaint(
        t: Float,
        entry: Float,
        moved: Boolean,
        nowMs: Long,
    ): Boolean {
        val key = "${floor(t / 5f).toInt()}_${floor(t * 2f).toInt()}_${min(60, floor(entry / 0.24f).toInt())}"
        if (key != lastKey || (moved && nowMs - lastPaintMs > 33)) {
            lastKey = key
            lastPaintMs = nowMs
            return true
        }
        return false
    }

    /**
     * Paints the tiles centred on ([cx], [cy]), fitted to [maxWidth] and [lineHeight].
     * [entry] is the entry time in seconds (large = every letter visible).
     */
    fun paint(
        mask: Bitmap,
        color: Bitmap,
        text: String,
        cx: Float,
        cy: Float,
        maxWidth: Float,
        lineHeight: Float,
        t: Float,
        entry: Float,
        textColor: FloatArray,
        density: Float,
    ) {
        val maskCanvas = Canvas(mask)
        val colorCanvas = Canvas(color)
        maskCanvas.drawColor(Color.BLACK)
        colorCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val chars = text.ifEmpty { " " }.map { it.toString() }
        if (scatter.size != chars.size * 3) {
            scatter = FloatArray(chars.size * 3)
            centres = FloatArray(chars.size * 2) { Float.NaN }
        }
        val ident = floor(t / 5f)
        val jit = floor(t * 2f)
        val visible = entry / 0.24f
        val tints =
            intArrayOf(
                rgb(0.95f, 0.93f, 0.88f),
                rgb(0.98f, 1f, 0.1f),
                rgb(textColor[0], textColor[1], textColor[2]),
                rgb(1f, 0.49f, 0f),
                rgb(0.04f, 0.04f, 0.05f),
            )

        val base = 100f
        val tiles = ArrayList<Tile?>(chars.size)
        val widths = FloatArray(chars.size)
        var total = 0f
        chars.forEachIndexed { idx, ch ->
            if (ch.isBlank()) {
                tiles += null
                widths[idx] = base * 0.4f
            } else {
                fun r(k: Float) = h1(idx * 7.1f + k + ident * 13.3f)
                val font = floor(r(1f) * typefaces.size).toInt().coerceIn(0, typefaces.size - 1)
                val glyph = if (r(2f) > 0.5f) ch.uppercase() else ch.lowercase()
                val size = 0.9f + r(3f) * 0.25f
                val tint = floor(r(4f) * 5f).toInt().coerceIn(0, 4)
                textPaint.typeface = typefaces[font]
                textPaint.textSize = base * size
                val w = textPaint.measureText(glyph) + base * 0.28f
                tiles += Tile(idx, glyph, font, size, tint, w)
                widths[idx] = w
            }
            total += widths[idx]
        }
        val k = min(maxWidth / total, lineHeight / (base * 1.25f))
        tileHeight = base * 1.25f * k
        var x = cx - total * k / 2f
        borderPaint.strokeWidth = 2f * density
        borderPaint.color = rgb(0.04f, 0.04f, 0.04f)

        tiles.forEachIndexed { idx, tile ->
            val mid = x + widths[idx] * k / 2f
            x += widths[idx] * k
            if (tile == null) return@forEachIndexed
            fun jr(q: Float) = h1(tile.index * 3.3f + q + jit * 5.7f)
            val bx = mid + (jr(2f) - 0.5f) * base * 0.12f * k
            val by = cy + (jr(3f) - 0.5f) * base * 0.16f * k
            centres[idx * 2] = bx
            centres[idx * 2 + 1] = by
            if (idx >= visible) return@forEachIndexed
            val o = idx * 3
            val rot = Math.toDegrees(((jr(1f) - 0.5f) * 0.34f + scatter[o + 2]).toDouble()).toFloat()
            val tw = tile.width * k
            val th = base * 1.2f * tile.size * k

            for (pass in 0..1) {
                val c = if (pass == 0) maskCanvas else colorCanvas
                c.save()
                c.translate(bx + scatter[o], by + scatter[o + 1])
                c.rotate(rot)
                if (pass == 0) {
                    c.drawRect(-tw / 2f, -th / 2f, tw / 2f, th / 2f, maskPaint)
                } else {
                    fillPaint.color = tints[tile.tint]
                    c.drawRect(-tw / 2f, -th / 2f, tw / 2f, th / 2f, fillPaint)
                    c.drawRect(-tw / 2f, -th / 2f, tw / 2f, th / 2f, borderPaint)
                    textPaint.typeface = typefaces[tile.font]
                    textPaint.textSize = base * tile.size * k
                    textPaint.color = if (tile.tint == 4) rgb(0.98f, 1f, 0f) else rgb(0.04f, 0.04f, 0.04f)
                    // centre the glyph vertically, like textBaseline = 'middle' in the lab
                    val fm = textPaint.fontMetrics
                    c.drawText(tile.glyph, 0f, base * 0.04f * k - (fm.ascent + fm.descent) / 2f, textPaint)
                }
                c.restore()
            }
        }
    }

    private fun h1(n: Float): Float {
        val x = sin(n.toDouble() * 12.9898) * 43758.5453
        return (x - floor(x)).toFloat()
    }

    private fun rgb(
        r: Float,
        g: Float,
        b: Float,
    ) = Color.rgb((r * 255f).toInt().coerceIn(0, 255), (g * 255f).toInt().coerceIn(0, 255), (b * 255f).toInt().coerceIn(0, 255))
}
