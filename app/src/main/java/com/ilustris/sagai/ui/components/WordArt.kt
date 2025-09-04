package com.ilustris.sagai.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.newsaga.data.model.Genre // Assuming this is the correct path
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.headerFont

@Composable
fun WordArtText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 64.sp,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight = FontWeight.Bold,
    topColor: Color = Color(0xFFFDB813),
    bottomColor: Color = Color(0xFFE35C00),
    extrusionColor: Color = Color(0xFF8C3400),
    extrusionDepthFactor: Float = 0.08f,
    numberOfExtrusionLayers: Int = 5,
    outlineColor: Color = Color(0xFF652800),
    outlineWidthFactor: Float = 0.12f,
    rotationX: Float = 15f,
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val extrusionOffsetPx = with(density) { (fontSize * extrusionDepthFactor).toPx() }
    val outlineWidthPx = with(density) { (fontSize * outlineWidthFactor).toPx() }

    val baseTextStyle =
        remember(fontSize, fontFamily, fontWeight) {
            TextStyle(
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = fontWeight,
            )
        }

    val mainTextBrush =
        remember(topColor, bottomColor) {
            Brush.verticalGradient(colors = listOf(topColor, bottomColor))
        }

    val textLayoutResult =
        remember(text, baseTextStyle) {
            textMeasurer.measure(text = AnnotatedString(text), style = baseTextStyle)
        }

    val outlineTextStyle = remember(baseTextStyle, outlineColor, outlineWidthPx) {
        baseTextStyle.copy(
            brush = SolidColor(outlineColor),
            drawStyle = Stroke(width = outlineWidthPx, join = StrokeJoin.Round)
        )
    }

    val outlineTextLayoutResult = remember(text, outlineTextStyle) {
        textMeasurer.measure(AnnotatedString(text), style = outlineTextStyle)
    }


    Box(
        modifier =
        modifier
            .graphicsLayer {
                this.rotationX = rotationX
            }
            .drawBehind {
                // 1. Extrusion Layers
                for (i in numberOfExtrusionLayers downTo 1) {
                    drawText(
                        textLayoutResult = textLayoutResult,
                        color = extrusionColor,
                        topLeft =
                        Offset(
                            x = i * extrusionOffsetPx * 0.5f,
                            y = i * extrusionOffsetPx * 0.866f,
                        ),
                    )
                }
                // 2. Outline Layer
                drawText(
                    textLayoutResult = outlineTextLayoutResult,
                    // Color is now part of outlineTextLayoutResult's style
                )
            },
    ) {
        // 3. Main Gradient Text
        Text(
            text = text,
            style = baseTextStyle.copy(brush = mainTextBrush),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WordArtTextPreview() {
    SagAIScaffold {
        // Replace with your actual app theme if different
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Genre.entries.forEach {
                WordArtText(
                    text = it.title,
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    fontFamily = it.headerFont(),
                    topColor = it.colorPalette().first(),
                    bottomColor = it.colorPalette().last(),
                    extrusionColor = it.color.darker(.3f),
                    outlineColor = it.color.darker(.5f),
                )

            }
        }
    }
}
