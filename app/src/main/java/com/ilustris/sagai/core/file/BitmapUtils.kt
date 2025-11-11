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
    val width = bitmap.width
    val height = bitmap.height
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
    canvas.drawBitmap(bitmap, rect, rect, paint)

    return outputBitmap
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
