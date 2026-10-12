package com.ilustris.sagai.features.share.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.theme.SagaTitle

/** Width and height of the card, so it reads as a collectible instead of a screenshot. */
const val COLLECTOR_CARD_ASPECT = 5f / 7f

/** Every size below is written for a card this wide and scaled to the real width. */
private const val REFERENCE_WIDTH_DP = 320f

private const val MAX_TILT_DEGREES = 16f

/**
 * Palette entries darker than this are the backdrop tints at the start of a genre's palette (the
 * Fantasy one opens with `#050204`). On a dark card they vanish, so they stay out of the frame and
 * the foil. The one deliberate difference from `genre.gradient()`.
 */
private const val MIN_PALETTE_LUMINANCE = 0.04f

/**
 * A genre-themed trading card: art as the background, a name and one line under it, a vertical strip
 * on the right, a short text over a fade at the bottom, a holographic foil, and a 3D tilt.
 *
 * Colours come from the genre's visual config: the genre colour (`primary`) tints the name and the
 * strip, and the palette, in stored order, builds the frame and the foil. Fonts and the corner radius
 * come from the enclosing [com.ilustris.sagai.ui.theme.SagAITheme]. Wrap the card in one for [genre].
 *
 * @param art fills the card; receives the tilt, so it can drift in parallax, and the size scale
 * (1 at the reference width) to scale its own offsets.
 * @param captureLayer records the card flat, outside the 3D rotation, for exporting it as an image.
 */
@Composable
fun CollectorCard(
    genre: Genre,
    title: String,
    subtitle: String?,
    number: String,
    body: String?,
    tilt: CardTiltState,
    captureLayer: GraphicsLayer,
    modifier: Modifier = Modifier,
    art: @Composable BoxScope.(tilt: () -> Offset, scale: Float) -> Unit,
) {
    val density = LocalDensity.current
    val read = tilt.read
    val shape = MaterialTheme.shapes.extraLarge
    val primary = MaterialTheme.colorScheme.primary
    val palette = genre.colorPalette()
    val frameColors =
        remember(palette) {
            val lit = palette.filter { it.luminance() >= MIN_PALETTE_LUMINANCE }.ifEmpty { palette }
            if (lit.size >= 2) lit else List(2) { lit.firstOrNull() ?: Color.White }
        }
    val nameTop = remember(primary) { primary.lightenTo(0.5f) }
    val nameBottom = remember(primary) { primary.lightenTo(0.3f) }
    val shade =
        remember {
            Brush.verticalGradient(
                0f to Color.Black.copy(alpha = 0.45f),
                0.2f to Color.Transparent,
                0.46f to Color.Transparent,
                0.68f to Color.Black.copy(alpha = 0.72f),
                1f to Color.Black.copy(alpha = 0.92f),
            )
        }

    BoxWithConstraints(
        modifier
            .aspectRatio(COLLECTOR_CARD_ASPECT)
            .then(tilt.gestures)
            .graphicsLayer {
                val t = read()
                rotationY = t.x * MAX_TILT_DEGREES
                rotationX = -t.y * MAX_TILT_DEGREES
                cameraDistance = 14f * density.density
            },
    ) {
        val scale = maxWidth.value / REFERENCE_WIDTH_DP
        val cardHeight = maxHeight
        // Fixed size whatever the user's font scale: this becomes an image, not a screen.
        val sp: (Float) -> TextUnit = { with(density) { (it * scale).dp.toSp() } }

        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    captureLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(captureLayer)
                },
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(Color.Black)
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
            ) {
                art(read, scale)
                Box(Modifier.fillMaxSize().background(shade))
                HoloFoil(colors = frameColors, tilt = read, modifier = Modifier.fillMaxSize())
            }

            Box(Modifier.fillMaxSize().border(5.dp * scale, Brush.linearGradient(frameColors), shape))

            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp * scale, start = 34.dp * scale, end = 34.dp * scale)
                    .parallax(read, 3f * scale),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BasicText(
                        text = title,
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(minFontSize = sp(14f), maxFontSize = sp(34f)),
                        style =
                            MaterialTheme.typography.displaySmall.copy(
                                brush = Brush.verticalGradient(listOf(nameTop, nameBottom)),
                                textAlign = TextAlign.Center,
                                shadow = Shadow(Color.Black.copy(alpha = 0.85f), Offset(0f, 4f), 10f),
                            ),
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = sp(13f),
                                    lineHeight = sp(17f),
                                    color = Color.White.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center,
                                    shadow = Shadow(Color.Black, Offset(0f, 2f), 8f),
                                ),
                        )
                    }
                }
            }

            CardStrip(
                genreName = stringResource(genre.title),
                number = number,
                accent = nameTop,
                size = sp(11f),
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 100.dp * scale, end = 12.dp * scale)
                        .heightIn(max = cardHeight * 0.4f)
                        .parallax(read, 1.5f * scale),
            )

            if (!body.isNullOrBlank()) {
                Text(
                    text = body,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontSize = sp(15f),
                            lineHeight = sp(21f),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            shadow = Shadow(Color.Black.copy(alpha = 0.9f), Offset(0f, 2f), 10f),
                        ),
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 22.dp * scale, vertical = 20.dp * scale)
                            .parallax(read, 2f * scale),
                )
            }
        }
    }
}

/** The 90° strip on the right: the Sagas mark, the genre, and the number. Reads top to bottom. */
@Composable
private fun CardStrip(
    genreName: String,
    number: String,
    accent: Color,
    size: TextUnit,
    modifier: Modifier = Modifier,
) {
    val label =
        MaterialTheme.typography.bodyMedium.copy(
            fontSize = size,
            color = Color.White.copy(alpha = 0.85f),
            shadow = Shadow(Color.Black, Offset(0f, 2f), 6f),
        )
    CompositionLocalProvider(LocalContentColor provides accent) {
        Row(
            modifier.rotatedClockwise(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SagaTitle(
                textStyle =
                    MaterialTheme.typography.titleMedium.copy(
                        fontSize = size * 1.25f,
                        color = accent,
                        shadow = Shadow(Color.Black, Offset(0f, 2f), 6f),
                    ),
            )
            Text(genreName, style = label, maxLines = 1, softWrap = false)
            Text(number, style = label, maxLines = 1, softWrap = false)
        }
    }
}

/** Rotates content 90° clockwise and lays it out as the rotated shape, so it takes the room it uses. */
private fun Modifier.rotatedClockwise(): Modifier =
    layout { measurable, constraints ->
        val placeable =
            measurable.measure(
                Constraints(
                    minWidth = constraints.minHeight,
                    maxWidth = constraints.maxHeight,
                    minHeight = constraints.minWidth,
                    maxHeight = constraints.maxWidth,
                ),
            )
        layout(placeable.height, placeable.width) {
            placeable.placeWithLayer(
                x = (placeable.height - placeable.width) / 2,
                y = (placeable.width - placeable.height) / 2,
            ) { rotationZ = 90f }
        }
    }

/** Slides a layer with the tilt, so text floats above the art at its own depth. */
private fun Modifier.parallax(
    tilt: () -> Offset,
    distanceDp: Float,
): Modifier =
    graphicsLayer {
        val t = tilt()
        translationX = t.x * distanceDp * density
        translationY = t.y * distanceDp * density
    }

/** Mixes toward white just far enough to reach [target] luminance, so dark genre colours stay readable. */
private fun Color.lightenTo(target: Float): Color {
    var amount = 0f
    var result = this
    while (result.luminance() < target && amount < 0.85f) {
        amount += 0.05f
        result = lerp(this, Color.White, amount)
    }
    return result
}
