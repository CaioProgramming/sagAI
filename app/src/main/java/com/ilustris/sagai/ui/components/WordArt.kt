package com.ilustris.sagai.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.animations.chromaticAberration
import com.ilustris.sagai.ui.animations.cowboyBurn
import com.ilustris.sagai.ui.animations.divineAura
import com.ilustris.sagai.ui.animations.dreamySparkle
import com.ilustris.sagai.ui.animations.glitch
import com.ilustris.sagai.ui.animations.heroChrome
import com.ilustris.sagai.ui.animations.katanaSlice
import com.ilustris.sagai.ui.animations.psychosis
import com.ilustris.sagai.ui.animations.spaceVoyage
import com.ilustris.sagai.ui.animations.vhs
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.levitate
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RansomNoteText(
    text: String,
    genre: Genre,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 48.sp,
    fontFamily: FontFamily? = null,
    primaryColor: Color = Color.White,
    secondaryColor: Color = Color.Black,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "punkPulse")
    val ticker by
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1000000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "punkTicker",
        )

    // Derived states ensure we ONLY recompose when the integer actually changes
    val colorFrame by remember { derivedStateOf { (ticker / 5f).toInt() } }
    val jitterFrame by remember { derivedStateOf { (ticker / 0.5f).toInt() } }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center,
        maxItemsInEachRow = 12,
    ) {
        text.forEachIndexed { index, char ->
            if (char.isWhitespace()) {
                Spacer(modifier = Modifier.width(fontSize.value.dp / 3))
            } else {
                RansomLetter(
                    char = char,
                    index = index,
                    colorFrame = colorFrame,
                    jitterFrame = jitterFrame,
                    genre = genre,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                )
            }
        }
    }
}

@Composable
private fun RansomLetter(
    char: Char,
    index: Int,
    colorFrame: Int,
    jitterFrame: Int,
    genre: Genre,
    fontSize: TextUnit,
    fontFamily: FontFamily?,
    primaryColor: Color,
    secondaryColor: Color,
) {
    // VISUAL IDENTITY: Only changes every 10 seconds (Slow Cycle)
    val visualIdentity =
        remember(index, colorFrame) {
            val r = kotlin.random.Random(colorFrame + index * 123)
            val isReversed = r.nextBoolean()
            val isUpper = r.nextBoolean()
            val sizeMult = 0.9f + r.nextFloat() * 0.2f
            val scaleBase = 0.85f + r.nextFloat() * 0.25f

            object {
                val bg = if (isReversed) secondaryColor else primaryColor
                val text = if (isReversed) primaryColor else secondaryColor
                val upper = isUpper
                val size = sizeMult
                val scale = scaleBase
            }
        }

    // JITTER: Fast cycle (2 FPS stop-motion)
    // We use graphicsLayer lambda to avoid recomposing the Text/Surface content for simple jitters
    Surface(
        modifier =
            Modifier
                .padding(horizontal = 1.dp, vertical = 2.dp)
                .graphicsLayer {
                    val jRandom = kotlin.random.Random(jitterFrame + index * 456L)
                    rotationZ = jRandom.nextFloat() * 18f - 9f
                    translationX = (jRandom.nextFloat() - 0.5f) * 14f
                    translationY = (jRandom.nextFloat() - 0.5f) * 8f
                    scaleX = visualIdentity.scale
                    scaleY = visualIdentity.scale
                },
        color = visualIdentity.bg,
        shape = genre.bubble(isNarrator = true),
    ) {
        Text(
            text = if (visualIdentity.upper) char.uppercase() else char.lowercase(),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            style =
                TextStyle(
                    color = visualIdentity.text,
                    fontSize = fontSize * visualIdentity.size,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Black,
                ),
        )
    }
}

@Composable
fun Genre.stylisedText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = MaterialTheme.typography.headlineLarge.fontSize,
) {
    val resolvedColor = resolveColor()
    val resolvedIconColor = resolveIconColor()
    colorPalette()
    when (this) {
        Genre.FANTASY -> {
            AutoResizeText(
                text = text,
                modifier =
                    modifier
                        .levitate()
                        .divineAura(
                            auraColor = Color(0xFFFFECB3),
                        ).chromaticAberration(intensity = 1.5f, blurRadius = 10f),
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        textAlign = TextAlign.Center,
                        fontSize = fontSize,
                        brush =
                            Brush.verticalGradient(
                                colorPalette(),
                            ),
                        fontFamily = headerFont(),
                        fontWeight = FontWeight.Normal, // Divine elegance
                        shadow =
                            Shadow(
                                resolvedColor.copy(alpha = 0.5f), // Golden shadow
                                blurRadius = 15f,
                                offset = Offset(0f, 2f),
                            ),
                    ),
            )
        }

        Genre.CYBERPUNK -> {
            val palette = this.colorPalette()
            AutoResizeText(
                text = text,
                modifier = modifier.glitch(),
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        textAlign = TextAlign.Center,
                        fontFamily = headerFont(),
                        brush = Brush.verticalGradient(palette),
                        shadow =
                            Shadow(
                                color = resolvedIconColor,
                                blurRadius = 20f,
                            ),
                    ),
            )
        }

        Genre.HORROR -> {
            AutoResizeText(
                text = text,
                modifier =
                    modifier
                        .padding(2.dp)
                        .psychosis(),
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontFamily = headerFont(),
                        brush = Brush.verticalGradient(colorPalette()),
                        shadow =
                            Shadow(
                                resolvedIconColor,
                                blurRadius = 10f,
                                offset = Offset(x = 0f, y = 2f),
                            ),
                    ),
            )
        }

        Genre.COWBOY -> {
            val palette = this.colorPalette()
            AutoResizeText(
                text = text,
                modifier =
                    modifier
                        .cowboyBurn(true)
                        .padding(2.dp),
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = this.headerFont(),
                        brush = Brush.verticalGradient(listOf(resolvedColor).plus(palette)),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize,
                        shadow =
                            Shadow(
                                resolvedIconColor.copy(alpha = 0.6f),
                                blurRadius = 15f,
                            ),
                    ),
            )
        }

        Genre.CRIME -> {
            val genre = this
            Text(
                text = text,
                modifier =
                    modifier
                        .vhs()
                        .dreamySparkle(color = Color.White),
                fontSize = fontSize,
                fontFamily = genre.headerFont(),
                color = Color.White,
                style =
                    TextStyle(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        shadow =
                            Shadow(
                                color = Color(0xFFFF4081).copy(alpha = 0.8f),
                                offset = Offset(0f, 0f),
                                blurRadius = 30f,
                            ),
                    ),
            )
        }

        Genre.HEROES -> {
            val palette = this.colorPalette()
            WordArtText(
                text = text,
                modifier = modifier.heroChrome(color = Color.White),
                fontSize = (fontSize.value * .75f).sp,
                fontFamily = headerFont(),
                topColor = resolvedColor.lighter(.7f), // High-power chrome edge
                bottomColor = palette.first().darker(.3f),
                extrusionColor = palette[1].darker(.5f),
                extrusionDepthFactor = 0.04f,
                numberOfExtrusionLayers = 20,
                outlineColor = Color.Black,
                outlineWidthFactor = .08f,
                rotationX = 35f, // X-Men Intro Tilted Perspective
                glowColor = resolvedColor.copy(alpha = 0.4f),
                glowAlpha = .3f,
                glowRadiusFactor = 15f,
            )
        }

        Genre.SPACE_OPERA -> {
            Text(
                text = text,
                modifier =
                    modifier
                        .spaceVoyage(true)
                        .padding(8.dp),
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = this.headerFont(),
                        brush = Brush.verticalGradient(listOf(resolvedColor).plus(resolvedColor.darkerPalette())),
                        fontWeight = FontWeight.Normal,
                        fontSize = fontSize,
                        shadow =
                            Shadow(
                                resolvedColor.lighter(.2f),
                                blurRadius = 25f,
                            ),
                    ),
            )
        }

        Genre.SHINOBI -> {
            WordArtText(
                text = text,
                modifier =
                    modifier
                        .padding(12.dp)
                        .katanaSlice(true, resolvedIconColor),
                fontSize = fontSize,
                fontFamily = this.headerFont(),
                topColor = resolvedColor, // Main theme color
                bottomColor = Color.White, // White hot core for the text
                numberOfExtrusionLayers = 2,
                outlineColor = resolvedIconColor,
                outlineWidthFactor = 0.04f,
                glowColor = resolvedColor,
                glowAlpha = 0.4f,
                glowRadiusFactor = 6f,
            )
        }

        Genre.PUNK_ROCK -> {
            RansomNoteText(
                text = text,
                genre = this,
                modifier = modifier.padding(8.dp),
                fontSize = (fontSize.value * 0.8f).sp,
                fontFamily = this.headerFont(),
                primaryColor = resolvedColor,
                secondaryColor = resolvedIconColor,
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
