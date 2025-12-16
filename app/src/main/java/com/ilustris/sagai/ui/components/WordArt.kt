package com.ilustris.sagai.ui.components

import ai.atick.material.MaterialColor
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.lighter

@Composable
fun WordArtText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = MaterialTheme.typography.displayMedium.fontSize,
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
    glowColor: Color? = null,
    glowRadiusFactor: Float = 0.18f,
    glowAlpha: Float = 0.55f,
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

    val outlineTextStyle =
        remember(baseTextStyle, outlineColor, outlineWidthPx) {
            baseTextStyle.copy(
                brush = SolidColor(outlineColor),
                drawStyle = Stroke(width = outlineWidthPx, join = StrokeJoin.Round),
            )
        }

    val outlineTextLayoutResult =
        remember(text, outlineTextStyle) {
            textMeasurer.measure(AnnotatedString(text), style = outlineTextStyle)
        }

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    this.rotationX = rotationX
                }.drawBehind {
                    // Optional outer glow around the text outline to emulate neon/cyberpunk

                    // 1. Extrusion Layers
                    for (i in numberOfExtrusionLayers downTo 1) {
                        val shadow =
                            if (i == numberOfExtrusionLayers) {
                                if (glowColor != null) {
                                    Shadow(
                                        glowColor,
                                        offset = Offset(0f, 0f),
                                        blurRadius = glowRadiusFactor,
                                    )
                                } else {
                                    null
                                }
                            } else {
                                null
                            }
                        drawText(
                            textLayoutResult = textLayoutResult,
                            color = extrusionColor,
                            shadow = shadow,
                            topLeft =
                                Offset(
                                    x = i * extrusionOffsetPx * 0.5f,
                                    y = i * extrusionOffsetPx * 0.866f,
                                ),
                        )
                    }
                    drawText(
                        textLayoutResult = outlineTextLayoutResult,
                    )
                },
    ) {
        Text(
            text = text,
            style = baseTextStyle.copy(brush = mainTextBrush, textAlign = TextAlign.Center),
        )
    }
}

@Composable
fun Genre.stylisedText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = MaterialTheme.typography.displaySmall.fontSize,
) {
    when (this) {
        Genre.FANTASY -> {
            val palette = this.colorPalette()
            WordArtText(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontFamily = this.headerFont(),
                topColor = palette.first(),
                bottomColor = palette.last(),
                numberOfExtrusionLayers = 1,
                outlineWidthFactor = .1f,
                outlineColor = color.darker(.5f),
                extrusionColor = palette.last().darker(.3f),
                glowAlpha = .6f,
                glowColor = color,
                glowRadiusFactor = 15f,
            )
        }

        Genre.CYBERPUNK -> {
            val palette = this.colorPalette()
            WordArtText(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontFamily = this.headerFont(),
                topColor = color,
                bottomColor = palette[2],
                numberOfExtrusionLayers = 2,
                outlineColor = palette.first(),
                extrusionColor = palette.last(),
                glowRadiusFactor = 15f,
                glowColor = iconColor,
                glowAlpha = 1f,
            )
        }

        Genre.HORROR -> {
            val palette = this.colorPalette()
            WordArtText(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontFamily = this.headerFont(),
                topColor = color,
                bottomColor = palette.last(),
                numberOfExtrusionLayers = 2,
                outlineColor = iconColor,
                outlineWidthFactor = 0.03f,
                glowColor = color,
                glowAlpha = .5f,
                glowRadiusFactor = 10f,
            )
        }

        Genre.COWBOY -> {
            val palette = this.colorPalette()

            WordArtText(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontFamily = this.headerFont(),
                topColor = palette.first(),
                bottomColor = palette[1],
                numberOfExtrusionLayers = 5,
                outlineColor = iconColor.copy(alpha = .2f),
                extrusionColor = palette[3],
                outlineWidthFactor = .03f,
                glowRadiusFactor = 2f,
                glowAlpha = .5f,
                glowColor = iconColor,
                rotationX = 25f,
                extrusionDepthFactor = .03f,
            )
        }

        Genre.CRIME -> {
            val genre = this
            Box(modifier = modifier) {
                WordArtText(
                    text = text,
                    fontSize = fontSize,
                    fontFamily = genre.headerFont(),
                    topColor = genre.color,
                    bottomColor = genre.color.darker(.3f),
                    extrusionColor = MaterialColor.DeepPurple800,
                    extrusionDepthFactor = .03f,
                    numberOfExtrusionLayers = 10,
                    outlineColor = genre.colorPalette().last(),
                    outlineWidthFactor = .05f,
                    rotationX = 15f,
                    glowColor = genre.color,
                    glowRadiusFactor = 10f,
                    glowAlpha = 1f,
                )

                StarryTextPlaceholder(
                    modifier =
                        Modifier
                            .matchParentSize(),
                    starColor = Color.White,
                    starCount = 50,
                )
            }
        }

        Genre.HEROES -> {
            val palette = this.colorPalette()
            WordArtText(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontFamily = headerFont(),
                topColor = color.lighter(.5f),
                bottomColor = palette.first().darker(.5f),
                extrusionColor = palette[1],
                extrusionDepthFactor = 0.02f,
                numberOfExtrusionLayers = 15,
                outlineColor = iconColor,
                outlineWidthFactor = .07f,
                rotationX = 15f,
                glowColor = color,
                glowAlpha = .5f,
                glowRadiusFactor = 10f,
            )
        }

        Genre.SPACE_OPERA -> {
            val palette = this.colorPalette()
            WordArtText(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontFamily = this.headerFont(),
                topColor = color,
                bottomColor = palette.last(),
                extrusionColor = palette.first().darker(.5f),
                extrusionDepthFactor = 0.04f,
                numberOfExtrusionLayers = 8,
                outlineColor = iconColor,
                outlineWidthFactor = 0.08f,
                rotationX = 20f,
                glowColor = color,
                glowRadiusFactor = 8f,
                glowAlpha = .7f,
            )
        }

        Genre.SHINOBI -> {
            val palette = this.colorPalette()
            WordArtText(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontFamily = this.headerFont(),
                topColor = palette[1],
                bottomColor = color,
                extrusionColor = color.darker(),
                extrusionDepthFactor = .02f,
                numberOfExtrusionLayers = 5,
                outlineColor = iconColor,
                outlineWidthFactor = .02f,
                rotationX = 35f,
                glowColor = iconColor,
                glowRadiusFactor = 5f,
                glowAlpha = 1f,
            )
        }

        Genre.PUNK_ROCK -> {
            val palette = this.colorPalette()
            WordArtText(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontFamily = this.headerFont(),
                topColor = color,
                bottomColor = palette.first(),
                extrusionColor = palette[2],
                glowColor = color,
                glowRadiusFactor = .3f,
                outlineColor = MaterialTheme.colorScheme.background,
            )
        }

        else -> {
            // Default fallback using palette
            val palette = this.colorPalette()
            WordArtText(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontFamily = this.headerFont(),
                topColor = palette.first(),
                bottomColor = palette.last(),
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
fun WordArtTextPreview() {
    SagAIScaffold {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(Genre.entries) {
                it.stylisedText(
                    stringResource(it.title),
                )
            }
        }
    }
}
