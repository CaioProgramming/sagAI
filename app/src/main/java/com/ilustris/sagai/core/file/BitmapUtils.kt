package com.ilustris.sagai.core.file

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.min // Added for scaling

fun cropBitmapToCircle(bitmap: Bitmap?): Bitmap? {
    if (bitmap == null) return null
    // Some sources (e.g. image loaders) may provide hardware bitmaps.
    // Canvas drawing in SW mode can't draw HARDWARE configs.
    val safeBitmap =
        if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

    val width = safeBitmap.width
    val height = safeBitmap.height
    val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(outputBitmap)
    val paint = Paint()
    val rect = Rect(0, 0, width, height)
    val rectF = RectF(rect)
    val radius = (width.coerceAtMost(height) / 2.0f)

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = Color.BLACK // Dummy color for mask shape
    canvas.drawCircle(width / 2f, height / 2f, radius, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(safeBitmap, rect, rect, paint)

    return outputBitmap
}

/**
 * Trims [insetFraction] of the width and of the height off every edge, so the aspect ratio is
 * preserved. Image models sometimes draw a frame and a paper margin around the artwork no matter
 * what the prompt says; a small crop is a more reliable fix than another prompt rule.
 */
fun insetCropBitmap(
    bitmap: Bitmap,
    insetFraction: Float,
): Bitmap {
    if (insetFraction <= 0f || insetFraction >= 0.5f) return bitmap
    val insetX = (bitmap.width * insetFraction).toInt()
    val insetY = (bitmap.height * insetFraction).toInt()
    val croppedWidth = bitmap.width - insetX * 2
    val croppedHeight = bitmap.height - insetY * 2
    if (croppedWidth <= 0 || croppedHeight <= 0) return bitmap
    return Bitmap.createBitmap(bitmap, insetX, insetY, croppedWidth, croppedHeight)
}

// --- NEW FUNCTION START ---
fun scaleBitmapForNotification(
    bitmap: Bitmap?,
    targetSizePx: Int,
): Bitmap? {
    if (bitmap == null) return null
    if (targetSizePx <= 0) return bitmap // No scaling if target size is invalid

    val currentWidth = bitmap.width
    val currentHeight = bitmap.height

    if (currentWidth <= targetSizePx && currentHeight <= targetSizePx) {
        return bitmap // No scaling needed if already smaller or equal
    }

    val scaleFactor = min(
        targetSizePx.toFloat() / currentWidth,
        targetSizePx.toFloat() / currentHeight,
    )

    val newWidth = (currentWidth * scaleFactor).toInt()
    val newHeight = (currentHeight * scaleFactor).toInt()

    return if (newWidth > 0 && newHeight > 0) {
        Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    } else {
        bitmap // Fallback to original if scaling results in 0 dimension
    }
}
// --- NEW FUNCTION END ---
