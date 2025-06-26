package com.ilustris.sagai.ui.theme

import ai.atick.material.MaterialColor
import androidx.compose.runtime.Composable
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
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.isCharacter

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
        SCI_FI -> Color.White
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

fun Modifier.noiseGrain(
    intensity: Float = 0.1f,
    grainColor: Color = Color.Black,
): Modifier =
    drawWithCache {
        onDrawWithContent {
            drawContent()
            val noisePaint =
                Paint().apply {
                    color = grainColor
                    alpha = intensity
                }
            drawIntoCanvas { canvas ->
                val width = size.width.toInt()
                val height = size.height.toInt()
                for (i in 0 until (width * height * intensity).toInt()) {
                    val x = (Math.random() * width).toFloat()
                    val y = (Math.random() * height).toFloat()
                    canvas.drawRect(x, y, x + 1, y + 1, noisePaint)
                }
            }
        }
    }


@Composable
fun Genre.backgroundTintFade() : Pair<Color, Color> = when(this) {
    Genre.FANTASY -> Color.Black to Color.Black
    Genre.SCI_FI -> MaterialColor.DeepPurple400 to Color.Black
}