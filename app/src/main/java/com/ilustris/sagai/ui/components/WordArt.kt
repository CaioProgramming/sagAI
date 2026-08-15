package com.ilustris.sagai.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.TextLayoutResult
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
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.components.HandwrittenText
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.lighter
import com.ilustris.sagai.ui.theme.themeBrushColors

@Composable
fun WordArtText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = MaterialTheme.typography.displayMedium.fontSize,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight = FontWeight.Black,
    topColor: Color = Color(0xFFFDB813),
    bottomColor: Color = Color(0xFFE35C00),
    extrusionColor: Color = bottomColor.darker(0.3f),
    extrusionDepthFactor: Float = 0.12f,
    numberOfExtrusionLayers: Int = 6,
    outlineColor: Color = Color(0xFF652800),
    outlineWidthFactor: Float = 0.1f,
    rotationX: Float = 0f,
    glowColor: Color? = null,
    glowRadiusFactor: Float = 0.18f,
    isPlaying: Boolean = true,
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
                textAlign = TextAlign.Center,
            )
        }

    // Flat solid fill (no vertical gradient) - a soft gradient reads as a glow/shadow, not the
    // chunky flat-color comic-book face we're after. `bottomColor` is now only the extrusion
    // body's default shade, kept as a param for callers that want a different depth tone.
    val mainTextBrush = remember(topColor) { SolidColor(topColor) }

    // Captured from the actually-rendered Text below, so it wraps under the exact same
    // constraints - the extrusion/outline layers are re-measured against this real layout
    // instead of an unconstrained one, which was the root cause of line-wrap desync.
    var frontLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val frontConstraints = frontLayout?.layoutInput?.constraints

    val outlineTextStyle =
        remember(baseTextStyle, outlineColor, outlineWidthPx) {
            baseTextStyle.copy(
                brush = SolidColor(outlineColor),
                drawStyle = Stroke(width = outlineWidthPx, join = StrokeJoin.Round),
            )
        }

    val extrusionLayoutResult =
        remember(text, baseTextStyle, frontConstraints) {
            frontConstraints?.let { textMeasurer.measure(AnnotatedString(text), style = baseTextStyle, constraints = it) }
        }

    val outlineTextLayoutResult =
        remember(text, outlineTextStyle, frontConstraints) {
            frontConstraints?.let { textMeasurer.measure(AnnotatedString(text), style = outlineTextStyle, constraints = it) }
        }

    // Depth never fully collapses - the block stays a solid 3D shape at rest, and just
    // punches a bit deeper on the loop instead of flattening back to a 2D sticker.
    val depthProgress =
        if (isPlaying && rememberLifecycleAnimationsActive()) {
            val infiniteTransition = rememberInfiniteTransition(label = "wordArtPunch")
            val progress by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 0.7f,
                animationSpec =
                    infiniteRepeatable(
                        animation =
                            keyframes {
                                durationMillis = 1800
                                0.7f at 0
                                1f at 450 using FastOutSlowInEasing
                                1f at 1250
                                0.7f at 1750 using FastOutSlowInEasing
                            },
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "wordArtDepth",
            )
            progress
        } else {
            1f
        }

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    this.rotationX = rotationX
                }.drawBehind {
                    val depth = extrusionOffsetPx * depthProgress

                    if (extrusionLayoutResult != null && depth > 0.5f) {
                        for (i in numberOfExtrusionLayers downTo 1) {
                            val t = i / numberOfExtrusionLayers.toFloat()
                            val shadow =
                                if (i == numberOfExtrusionLayers && glowColor != null) {
                                    Shadow(
                                        glowColor,
                                        offset = Offset(0f, 0f),
                                        blurRadius = glowRadiusFactor,
                                    )
                                } else {
                                    null
                                }
                            drawText(
                                textLayoutResult = extrusionLayoutResult,
                                color = extrusionColor,
                                shadow = shadow,
                                topLeft =
                                    Offset(
                                        x = depth * t * 0.5f,
                                        y = depth * t * 0.866f,
                                    ),
                            )
                        }
                    }
                    outlineTextLayoutResult?.let { drawText(textLayoutResult = it) }
                },
    ) {
        Text(
            text = text,
            style = baseTextStyle.copy(brush = mainTextBrush),
            onTextLayout = { frontLayout = it },
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
    if (rememberLifecycleAnimationsActive()) {
        RansomNoteTextAnimated(
            text = text,
            genre = genre,
            modifier = modifier,
            fontSize = fontSize,
            fontFamily = fontFamily,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
        )
    } else {
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
                        colorFrame = 0,
                        jitterFrame = 0,
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
}

@Composable
private fun RansomNoteTextAnimated(
    text: String,
    genre: Genre,
    modifier: Modifier,
    fontSize: TextUnit,
    fontFamily: FontFamily?,
    primaryColor: Color,
    secondaryColor: Color,
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
    fontSize: TextUnit = MaterialTheme.typography.displaySmall.fontSize,
) {
    val resolvedColor = MaterialTheme.colorScheme.primary
    val resolvedIconColor = MaterialTheme.colorScheme.onPrimary
    val palette = themeBrushColors()
    val style =
        MaterialTheme.typography.displaySmall.copy(
            textAlign = TextAlign.Center,
            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
            fontWeight = FontWeight.Normal,
        )
    when (this) {
        Genre.FANTASY -> {
            HandwrittenText(
                text = text,
                modifier =
                    modifier
                        .genreVfx(this, resolvedColor, resolvedIconColor),
                color = resolvedColor,
                fontSize = fontSize,
                isBold = true,
                isItalic = false,
                centered = true,
                shadow =
                    Shadow(
                        palette.last().darker(),
                        blurRadius = 15f,
                        offset = Offset(0f, 2f),
                    ),
            )
        }

        Genre.CRIME -> {
            HandwrittenText(
                text = text,
                modifier =
                    modifier
                        .genreVfx(this, resolvedColor, resolvedIconColor),
                color = resolvedColor,
                fontSize = fontSize,
                isBold = true,
                isItalic = false,
                centered = true,
                shadow =
                    Shadow(
                        resolvedColor,
                        blurRadius = 10f,
                        offset = Offset(0f, 2f),
                    ),
            )
        }

        Genre.CYBERPUNK -> {
            AutoResizeText(
                text = text,
                modifier = modifier.genreVfx(this),
                style =
                    style.copy(
                        brush = Brush.verticalGradient(palette),
                        shadow =
                            Shadow(
                                color = resolvedColor,
                                blurRadius = 15f,
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
                        .genreVfx(this),
                style =
                    style.copy(
                        brush = Brush.verticalGradient(colorPalette()),
                        shadow =
                            Shadow(
                                resolvedColor,
                                blurRadius = 10f,
                                offset = Offset(x = 0f, y = 2f),
                            ),
                    ),
            )
        }

        Genre.COWBOY -> {
            AutoResizeText(
                text = text,
                modifier =
                    modifier
                        .genreVfx(this)
                        .padding(2.dp),
                style =
                    style.copy(
                        brush = Brush.verticalGradient(palette),
                        fontSize = fontSize,
                        shadow =
                            Shadow(
                                resolvedColor.copy(alpha = 0.6f),
                                blurRadius = 15f,
                            ),
                    ),
            )
        }

        Genre.HEROES -> {
            WordArtText(
                text = text,
                modifier = modifier.padding(4.dp).genreVfx(this),
                fontSize = fontSize,
                fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                topColor = resolvedColor.lighter(0.6f),
                bottomColor = resolvedColor,
                extrusionColor = resolvedColor.darker(0.6f),
                outlineColor = Color.White,
                glowColor = resolvedColor.lighter(0.8f),
                numberOfExtrusionLayers = 12,
            )
        }

        Genre.SPACE_OPERA -> {
            AutoResizeText(
                text = text,
                modifier =
                    modifier
                        .genreVfx(this)
                        .padding(8.dp),
                style =
                    style.copy(
                        brush =
                            Brush.verticalGradient(
                                palette,
                            ),
                        shadow =
                            Shadow(
                                resolvedColor.lighter(.2f),
                                blurRadius = 20f,
                            ),
                    ),
            )
        }

        Genre.SHINOBI -> {
            AutoResizeText(
                text = text,
                modifier =
                    modifier
                        .padding(12.dp)
                        .genreVfx(this, secondaryColor = resolvedIconColor),
                style =
                    style.copy(
                        brush = Brush.verticalGradient(palette),
                        shadow = Shadow(resolvedColor.darker(), blurRadius = 15f),
                    ),
            )
        }

        Genre.PUNK_ROCK -> {
            AutoResizeText(
                text = text,
                modifier =
                    modifier
                        .genreVfx(this)
                        .padding(8.dp),
                style =
                    style.copy(
                        brush = Brush.verticalGradient(palette),
                        shadow = Shadow(resolvedColor.darker(), blurRadius = 15f),
                    ),
            )
        }

        else -> {
            // Default fallback using palette
            WordArtText(
                text = text,
                modifier = modifier,
                fontSize = fontSize,
                fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
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
