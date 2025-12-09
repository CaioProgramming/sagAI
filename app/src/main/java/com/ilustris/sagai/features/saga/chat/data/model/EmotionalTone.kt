
package com.ilustris.sagai.features.saga.chat.data.model

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.MorphPolygonShape
import android.graphics.Paint as AndroidPaint

enum class EmotionalTone(
    val color: Color,
) {
    NEUTRAL(Color(0xFFB0BEC5)),
    CALM(Color(0xFFA5D6A7)),
    CURIOUS(Color(0xFFFFF59D)),
    HOPEFUL(Color(0xFF81D4FA)),
    DETERMINED(Color(0xFFFFAB91)),
    EMPATHETIC(Color(0xFFCE93D8)),
    JOYFUL(Color(0xFFFFE082)),
    CONCERNED(Color(0xFFBCAAA4)),
    ANXIOUS(Color(0xFFFFCC80)),
    FRUSTRATED(Color(0xFFEF9A9A)),
    ANGRY(Color(0xFFD32F2F)),
    SAD(Color(0xFF90A4AE)),
    MELANCHOLIC(Color(0xFF7986CB)),
    CYNICAL(Color(0xFF757575)),
    ;

    fun starShape(numVertices: Int? = null): RoundedPolygon {
        val mappedVertices =
            when (this) {
                ANGRY -> 4
                FRUSTRATED -> 5
                SAD -> 6
                CYNICAL -> 7
                ANXIOUS -> 8
                CONCERNED -> 9
                MELANCHOLIC -> 10
                NEUTRAL -> 11
                DETERMINED -> 12
                CURIOUS -> 13
                HOPEFUL -> 14
                CALM -> 15
                EMPATHETIC -> 16
                JOYFUL -> 17
            }
        val n = maxOf(4, numVertices ?: mappedVertices)
        val roundingRadius =
            when (this) {
                ANGRY, FRUSTRATED, SAD, CYNICAL -> 0f
                ANXIOUS, CONCERNED, MELANCHOLIC -> 1f
                NEUTRAL, DETERMINED -> 2f
                CURIOUS -> 3f
                HOPEFUL -> 4f
                CALM, EMPATHETIC -> 5f
                JOYFUL -> 6f
            }
        return RoundedPolygon.star(numVerticesPerRadius = n, rounding = CornerRounding(roundingRadius))
    }

    @Composable
    fun getTitle(): String {
        val resId =
            when (this) {
                NEUTRAL -> R.string.tone_neutral
                CALM -> R.string.tone_calm
                CURIOUS -> R.string.tone_curious
                HOPEFUL -> R.string.tone_hopeful
                DETERMINED -> R.string.tone_determined
                EMPATHETIC -> R.string.tone_empathetic
                JOYFUL -> R.string.tone_joyful
                CONCERNED -> R.string.tone_concerned
                ANXIOUS -> R.string.tone_anxious
                FRUSTRATED -> R.string.tone_frustrated
                ANGRY -> R.string.tone_angry
                SAD -> R.string.tone_sad
                MELANCHOLIC -> R.string.tone_melancholic
                CYNICAL -> R.string.tone_cynical
            }
        return stringResource(id = resId)
    }

    companion object {
        fun getTone(tone: String?) =
            try {
                if (tone == null) NEUTRAL else valueOf(tone.uppercase())
            } catch (e: Exception) {
                NEUTRAL
            }
    }
}

@Composable
fun AnimatedEmotionalShape(
    modifier: Modifier = Modifier,
    emotionalTone: EmotionalTone,
    morphProgress: Float,
    rotationAngle: Float,
    backgroundBrush: Brush,
    outlineBrush: Brush,
    baseShape: RoundedPolygon = remember { RoundedPolygon.star(4, rounding = CornerRounding(5f)) },
    glowRadius: Dp = 10.dp,
    glowColor: Color? = null,
) {
    val targetShape = remember(emotionalTone) { emotionalTone.starShape() }
    val morph = remember(baseShape, targetShape) { Morph(baseShape, targetShape) }
    val animatedComposeShape =
        remember(morph, morphProgress) {
            MorphPolygonShape(morph, morphProgress)
        }

    val actualGlowColor = glowColor ?: emotionalTone.color.copy(alpha = 0.6f)

    val glowPaint =
        remember(actualGlowColor, glowRadius) {
            AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.FILL
                color = actualGlowColor.toArgb()
                setShadowLayer(
                    glowRadius.value,
                    0f,
                    0f,
                    actualGlowColor.toArgb(),
                )
            }
        }

    Canvas(
        modifier = modifier.rotate(rotationAngle),
    ) {
        val outline = animatedComposeShape.createOutline(this.size, layoutDirection, this)

        val androidPath = Path()
        when (outline) {
            is Outline.Generic -> androidPath.addPath(outline.path)
            is Outline.Rectangle -> androidPath.addRect(outline.rect)
            is Outline.Rounded -> androidPath.addRoundRect(outline.roundRect)
        }
        val nativePath = androidPath.asAndroidPath()

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawPath(nativePath, glowPaint)
        }

        when (outline) {
            is Outline.Generic -> {
                drawPath(path = outline.path, brush = backgroundBrush)
                drawPath(path = outline.path, brush = outlineBrush, style = Stroke(width = 2.dp.toPx()))
            }

            is Outline.Rectangle -> {
                val mainPath = Path().apply { addRect(outline.rect) }
                drawPath(path = mainPath, brush = backgroundBrush)
                drawPath(path = mainPath, brush = outlineBrush, style = Stroke(width = 2.dp.toPx()))
            }

            is Outline.Rounded -> {
                val mainPath = Path().apply { addRoundRect(outline.roundRect) }
                drawPath(path = mainPath, brush = backgroundBrush)
                drawPath(path = mainPath, brush = outlineBrush, style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}
