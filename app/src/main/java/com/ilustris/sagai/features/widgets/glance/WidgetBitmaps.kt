package com.ilustris.sagai.features.widgets.glance

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import com.ilustris.sagai.features.widgets.domain.WidgetPaletteFactory
import timber.log.Timber
import java.io.File
import kotlin.math.max
import kotlin.math.min

/**
 * Where the face usually sits in the pictures the app generates, as fractions of width and height.
 * There is no face detection in the project, so crops lean on this instead.
 */
object WidgetFocus {
    const val PORTRAIT_X = 0.5f
    const val PORTRAIT_Y = 0.36f
    const val ARTWORK_X = 0.5f
    const val ARTWORK_Y = 0.42f
}

/**
 * Bitmap work for the widgets. RemoteViews carry every bitmap in memory, so everything is decoded
 * small and cropped to the exact box it will fill.
 */
object WidgetBitmaps {
    private const val TAG = "WidgetBitmaps"
    private const val MAX_SIDE_PX = 640
    private const val LUMINANCE_GRID = 16
    private const val BLUR_DIVISOR = 14

    /** Decodes [path] down to at most [maxSidePx] on its longest side, or null when it can't be read. */
    fun decode(
        path: String?,
        maxSidePx: Int = MAX_SIDE_PX,
    ): Bitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists()) return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            var sample = 1
            while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSidePx) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Could not decode $path")
            null
        } catch (e: OutOfMemoryError) {
            Timber.tag(TAG).w(e, "Out of memory decoding $path")
            null
        }
    }

    /**
     * Crops [source] to [widthPx] x [heightPx], keeping the point ([focusX], [focusY]) in view and
     * placing it at [targetY] of the result's height (0 = top edge, 1 = bottom edge).
     */
    fun crop(
        source: Bitmap,
        widthPx: Int,
        heightPx: Int,
        focusX: Float,
        focusY: Float,
        targetY: Float = 0.5f,
    ): Bitmap {
        val outW = widthPx.coerceIn(1, MAX_SIDE_PX)
        val outH = heightPx.coerceIn(1, MAX_SIDE_PX)
        val aspect = outW.toFloat() / outH
        val srcW = source.width.toFloat()
        val srcH = source.height.toFloat()
        val windowW: Float
        val windowH: Float
        if (aspect > srcW / srcH) {
            windowW = srcW
            windowH = srcW / aspect
        } else {
            windowH = srcH
            windowW = srcH * aspect
        }
        val left = (focusX * srcW - windowW / 2f).coerceIn(0f, max(0f, srcW - windowW))
        val top = (focusY * srcH - targetY * windowH).coerceIn(0f, max(0f, srcH - windowH))
        val window =
            Bitmap.createBitmap(
                source,
                left.toInt(),
                top.toInt(),
                windowW.toInt().coerceIn(1, source.width - left.toInt()),
                windowH.toInt().coerceIn(1, source.height - top.toInt()),
            )
        return if (window.width == outW && window.height == outH) {
            window
        } else {
            Bitmap.createScaledBitmap(window, outW, outH, true)
        }
    }

    /** A round avatar of the face region of [source], with an optional ring. */
    fun circle(
        source: Bitmap,
        sizePx: Int,
        ringColor: Int,
        ringWidthPx: Float,
        focusX: Float = WidgetFocus.PORTRAIT_X,
        focusY: Float = WidgetFocus.PORTRAIT_Y,
    ): Bitmap {
        val size = sizePx.coerceIn(8, MAX_SIDE_PX)
        // A square window about 55% as wide as the picture: tight enough to read as a face.
        val side = (min(source.width, source.height) * 0.62f).toInt().coerceAtLeast(1)
        val square = crop(source, side, side, focusX, focusY, 0.5f)
        val scaled = Bitmap.createScaledBitmap(square, size, size, true)
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val inset = ringWidthPx / 2f
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - ringWidthPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaled, 0f, 0f, paint)
        if (ringWidthPx > 0f) {
            val ring = Paint(Paint.ANTI_ALIAS_FLAG)
            ring.style = Paint.Style.STROKE
            ring.strokeWidth = ringWidthPx
            ring.color = ringColor
            canvas.drawCircle(size / 2f, size / 2f, size / 2f - inset, ring)
        }
        return out
    }

    /** A soft blur made by shrinking and stretching back, cheap and good enough to hide a picture. */
    fun blur(source: Bitmap): Bitmap {
        val smallW = (source.width / BLUR_DIVISOR).coerceAtLeast(1)
        val smallH = (source.height / BLUR_DIVISOR).coerceAtLeast(1)
        val small = Bitmap.createScaledBitmap(source, smallW, smallH, true)
        return Bitmap.createScaledBitmap(small, source.width, source.height, true)
    }

    /** Mean WCAG luminance of [source] over a coarse grid, 0 to 1. Decides light or dark text. */
    fun averageLuminance(source: Bitmap): Double {
        val small = Bitmap.createScaledBitmap(source, LUMINANCE_GRID, LUMINANCE_GRID, true)
        var total = 0.0
        for (x in 0 until LUMINANCE_GRID) {
            for (y in 0 until LUMINANCE_GRID) {
                total += WidgetPaletteFactory.luminance(small.getPixel(x, y))
            }
        }
        return total / (LUMINANCE_GRID * LUMINANCE_GRID)
    }

    /**
     * One book of the stories shelf: a rounded gradient tile with a darker spine on the left, and
     * either the saga's own icon or the genre glyph in the middle.
     */
    fun bookTile(
        widthPx: Int,
        heightPx: Int,
        topColor: Int,
        bottomColor: Int,
        radiusPx: Float,
        icon: Bitmap?,
        glyph: Drawable?,
    ): Bitmap {
        val w = widthPx.coerceIn(8, MAX_SIDE_PX)
        val h = heightPx.coerceIn(8, MAX_SIDE_PX)
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        if (icon != null) {
            val fitted = crop(icon, w, h, WidgetFocus.PORTRAIT_X, WidgetFocus.PORTRAIT_Y, 0.4f)
            canvas.drawRoundRect(rect, radiusPx, radiusPx, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(fitted, 0f, 0f, paint)
            paint.xfermode = null
        } else {
            paint.shader = LinearGradient(0f, 0f, w.toFloat() * 0.4f, h.toFloat(), topColor, bottomColor, Shader.TileMode.CLAMP)
            canvas.drawRoundRect(rect, radiusPx, radiusPx, paint)
            paint.shader = null
            if (glyph != null) {
                val side = (min(w, h) * 0.58f).toInt()
                val left = (w - side) / 2
                val top = (h - side) / 2
                glyph.setBounds(left, top, left + side, top + side)
                glyph.draw(canvas)
            }
        }
        // Spine: a darker strip along the left edge, clipped to the rounded shape.
        val spine = Paint(Paint.ANTI_ALIAS_FLAG)
        spine.color = SPINE_COLOR
        canvas.save()
        canvas.clipRect(0f, 0f, max(2f, w * 0.09f), h.toFloat())
        canvas.drawRoundRect(rect, radiusPx, radiusPx, spine)
        canvas.restore()
        return out
    }

    private const val SPINE_COLOR = 0x47000000
}
