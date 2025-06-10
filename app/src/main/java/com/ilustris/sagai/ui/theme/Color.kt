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
import com.ilustris.sagai.features.chat.data.model.SenderType
import com.ilustris.sagai.features.chat.data.model.isCharacter
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.Genre.*

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

fun Color.darker(factor: Float = 0.1f): Color = this.adjust(1f - factor)

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

fun Genre.bubbleTextColors(sender: SenderType) =
    when (this) {
        FANTASY -> if (sender.isCharacter()) Color.Black else Color.White
        SCI_FI -> Color.White
    }
