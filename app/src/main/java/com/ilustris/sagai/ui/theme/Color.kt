package com.ilustris.sagai.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.Genre.*
import com.ilustris.sagai.features.saga.chat.domain.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.isCharacter

val Purple80 = Color(0xff11283b)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

fun Color.adjust(factor: Float): Color {
    val red = (this.red * 255 * factor).toInt().coerceIn(0, 255)
    val green = (this.green * 255 * factor).toInt().coerceIn(0, 255)
    val blue = (this.blue * 255 * factor).toInt().coerceIn(0, 255)
    return Color(red, green, blue, (this.alpha * 255).toInt())
}

fun Color.lighter(factor: Float = 0.1f): Color = this.adjust(1f + factor)

fun Color.saturate(saturationFactor: Float): Color {
    val matrix = ColorMatrix()
    matrix.setToSaturation(saturationFactor)
    val r = this.red * matrix[0, 0] + this.green * matrix[0, 1] + this.blue * matrix[0, 2] + this.alpha * matrix[0, 3] + matrix[0, 4]
    val g = this.red * matrix[1, 0] + this.green * matrix[1, 1] + this.blue * matrix[1, 2] + this.alpha * matrix[1, 3] + matrix[1, 4]
    val b = this.red * matrix[2, 0] + this.green * matrix[2, 1] + this.blue * matrix[2, 2] + this.alpha * matrix[2, 3] + matrix[2, 4]
    val a = this.red * matrix[3, 0] + this.green * matrix[3, 1] + this.blue * matrix[3, 2] + this.alpha * matrix[3, 3] + matrix[3, 4]
    return Color(red = r, green = g, blue = b, alpha = a)
}

fun Color.darker(factor: Float = 0.1f): Color = this.adjust(1f - factor)

fun String.hexToColor(): Color? {
    val hex = this.removePrefix("#")
    if (!hex.matches(Regex("^[0-9A-Fa-f]{6}$|^[0-9A-Fa-f]{8}$"))) {
        return null
    }
    val color = android.graphics.Color.parseColor("#$hex")
    return Color(color)
}

fun String.isValidHexColor(): Boolean {
    val hex = this.removePrefix("#")
    return hex.matches(Regex("^[0-9A-Fa-f]{6}$|^[0-9A-Fa-f]{8}$"))
}

fun Modifier.grayScale(saturationFactor: Float = 1f): Modifier {
    val saturationMatrix = ColorMatrix().apply { setToSaturation(saturationFactor) }
    val saturationFilter = ColorFilter.colorMatrix(saturationMatrix)
    val paint = Paint().apply { colorFilter = saturationFilter }

    return drawWithCache {
        val canvasBounds = Rect(Offset.Zero, size)
        onDrawWithContent {
            drawIntoCanvas {
                it.saveLayer(canvasBounds, paint)
                drawContent()
                it.restore()
            }
        }
    }
}

fun Modifier.invertedColors(): Modifier {
    val invertMatrix =
        ColorMatrix(
            floatArrayOf(
                -1f,
                0f,
                0f,
                0f,
                255f,
                0f,
                -1f,
                0f,
                0f,
                255f,
                0f,
                0f,
                -1f,
                0f,
                255f,
                0f,
                0f,
                0f,
                1f,
                0f,
            ),
        )
    val invertFilter = ColorFilter.colorMatrix(invertMatrix)
    val paint = Paint().apply { colorFilter = invertFilter }
    return drawWithCache {
        val canvasBounds = Rect(Offset.Zero, size)
        onDrawWithContent {
            drawIntoCanvas {
                it.saveLayer(canvasBounds, paint)
                drawContent()
                it.restore()
            }
        }
    }
}

fun Modifier.brightness(brightnessFactor: Float): Modifier {
    val brightnessMatrix =
        ColorMatrix().apply {
            val scale = brightnessFactor + 1f
            this[0, 0] = scale
            this[1, 1] = scale
            this[2, 2] = scale
        }
    val brightnessFilter = ColorFilter.colorMatrix(brightnessMatrix)
    val paint = Paint().apply { colorFilter = brightnessFilter }

    return drawWithCache {
        val canvasBounds = Rect(Offset.Zero, size)
        onDrawWithContent {
            drawIntoCanvas {
                it.saveLayer(canvasBounds, paint)
                drawContent()
                it.restore()
            }
        }
    }
}

fun Genre.bubbleTextColors(sender: SenderType) =
    when (this) {
        FANTASY -> if (sender.isCharacter()) Color.Black else Color.White
        else -> Color.White
    }

fun Modifier.contrast(contrastFactor: Float): Modifier {
    val contrastMatrix =
        ColorMatrix().apply {
            val scale = contrastFactor + 1f
            val translate = (-.5f * scale + .5f) * 255f
            this[0, 0] = scale
            this[0, 4] = translate
            this[1, 1] = scale
            this[1, 4] = translate
            this[2, 2] = scale
            this[2, 4] = translate
        }
    val contrastFilter = ColorFilter.colorMatrix(contrastMatrix)
    val paint = Paint().apply { colorFilter = contrastFilter }
    return drawWithCache {
        val canvasBounds = Rect(Offset.Zero, size)
        onDrawWithContent {
            drawIntoCanvas {
                it.saveLayer(canvasBounds, paint)
                drawContent()
                it.restore()
            }
        }
    }
}
