package com.ilustris.sagai.ui.components.titlefx

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Where the letters really are inside the layout box, as fractions of the box. */
internal class TitleFxInk(
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float,
)

internal class TitleFxField(
    val bitmap: Bitmap,
    val ink: TitleFxInk,
)

/** White letters on black, at the exact pixel size of the layout box. */
internal fun buildTitleMask(
    width: Int,
    height: Int,
    layout: TextLayoutResult,
    topLeft: Offset,
    strokePx: Float,
    density: Density,
): Bitmap {
    val image = ImageBitmap(width, height)
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(image), Size(width.toFloat(), height.toFloat())) {
        drawRect(Color.Black)
        if (strokePx > 0f) {
            drawText(
                textLayoutResult = layout,
                color = Color.White,
                topLeft = topLeft,
                drawStyle = Stroke(width = strokePx, join = StrokeJoin.Round),
            )
        }
        drawText(textLayoutResult = layout, color = Color.White, topLeft = topLeft)
    }
    return image.asAndroidBitmap()
}

private fun blurLine(
    a: FloatArray,
    out: FloatArray,
    start: Int,
    step: Int,
    len: Int,
    r: Int,
) {
    val n = 2 * r + 1
    var sum = 0f
    for (i in -r..r) sum += a[start + min(len - 1, max(0, i)) * step]
    for (i in 0 until len) {
        out[start + i * step] = sum / n
        sum += a[start + min(len - 1, i + r + 1) * step] - a[start + max(0, i - r) * step]
    }
}

private fun blur2d(
    a: FloatArray,
    w: Int,
    h: Int,
    rx: Int,
    ry: Int,
): FloatArray {
    val tmp = FloatArray(a.size)
    val out = FloatArray(a.size)
    for (y in 0 until h) blurLine(a, tmp, y * w, 1, w, rx)
    for (x in 0 until w) blurLine(tmp, out, x, w, h, ry)
    return out
}

/**
 * Small CPU "field" the shaders sample instead of doing the blurs themselves (the first, fully
 * procedural fire shader was too heavy for mid-range phones):
 * R = text smeared upwards (decay proportional to the text height), G = wide glow, B = tight halo.
 * Ported from `buildField` in the HTML lab.
 */
internal fun buildTitleField(
    mask: Bitmap,
    smearLen: Float,
): TitleFxField {
    val sw = (mask.width / 2).coerceIn(64, 320)
    val sh = max(32, (sw.toFloat() * mask.height / mask.width).roundToInt())
    val small = Bitmap.createScaledBitmap(mask, sw, sh, true)
    val px = IntArray(sw * sh)
    small.getPixels(px, 0, sw, 0, 0, sw, sh)
    if (small !== mask) small.recycle()

    val n = sw * sh
    val m = FloatArray(n) { ((px[it] shr 16) and 0xFF) / 255f }

    var minY = sh
    var maxY = -1
    var minX = sw
    var maxX = -1
    for (y in 0 until sh) {
        for (x in 0 until sw) {
            if (m[y * sw + x] > 0.55f) {
                if (y < minY) minY = y
                if (y > maxY) maxY = y
                if (x < minX) minX = x
                if (x > maxX) maxX = x
            }
        }
    }
    if (maxY < minY) {
        minY = (sh * 0.3f).toInt()
        maxY = (sh * 0.7f).toInt()
        minX = (sw * 0.1f).toInt()
        maxX = (sw * 0.9f).toInt()
    }

    val thS = max(4f, (maxY - minY + 1).toFloat())
    val dec = 1f / (thS * smearLen)
    val sm = FloatArray(n)
    for (x in 0 until sw) {
        var prev = 0f
        for (y in sh - 1 downTo 0) {
            val v = max(m[y * sw + x], prev - dec)
            sm[y * sw + x] = v
            prev = v
        }
    }
    val body = blur2d(sm, sw, sh, max(1, (sw * 0.01f).roundToInt()), 1)
    val glow = blur2d(sm, sw, sh, max(2, (thS * 0.9f).roundToInt()), max(2, (thS * 0.6f).roundToInt()))
    val halo = blur2d(m, sw, sh, max(2, (thS * 0.12f).roundToInt()), max(2, (thS * 0.12f).roundToInt()))

    val out = IntArray(n)
    for (i in 0 until n) {
        val r = min(255, (body[i] * 255f).toInt())
        val g = min(255, (glow[i] * 2.6f * 255f).toInt())
        val b = min(255, (halo[i] * 2.4f * 255f).toInt())
        out[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }
    val bmp = Bitmap.createBitmap(sw, sh, Bitmap.Config.ARGB_8888)
    bmp.setPixels(out, 0, sw, 0, 0, sw, sh)
    return TitleFxField(
        bitmap = bmp,
        ink =
            TitleFxInk(
                minX = minX.toFloat() / sw,
                maxX = (maxX + 1).toFloat() / sw,
                minY = minY.toFloat() / sh,
                maxY = (maxY + 1).toFloat() / sh,
            ),
    )
}
