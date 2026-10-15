package com.ilustris.sagai.features.share.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.iridescentGradient

/** How many lines of the body the drop cap spans. */
private const val DROP_CAP_LINES = 3

/** The note leans less than the art cards: it is a plate of text, not an object to turn over. */
private const val NOTE_MAX_TILT_DEGREES = 9f

/** Palette entries dimmer than this vanish as text on a black plate. */
private const val MIN_FOIL_LUMINANCE = 0.12f

private val NoteBlack = Color(0xFF070709)

/**
 * The emotional share: a black plate whose text is the holographic foil. No image. The title, the
 * text under its drop cap, the divider with the Sagas spark and the caption beside the Sagas mark are
 * all drawn in the genre's palette, and the gradient slides across the letters and the border as the
 * card tilts, like foil under a light.
 *
 * It is as wide as it can be rather than tall: every size is a share of the card's width, so a longer
 * text makes a slightly smaller type instead of a taller card. It does not depend on the device's
 * light or dark theme, because the picture has to look the same wherever it is opened.
 *
 * @param genre null for a share that belongs to the player, which wears the app's own colours.
 * @param tilt the same tilt the art cards use; at rest (while the card is exported) the foil sits
 * mid-card.
 * @param captureLayer records the card flat, outside the 3D rotation, for exporting it as an image.
 */
@Composable
fun EmotionNote(
    genre: Genre?,
    title: String,
    body: String,
    caption: String,
    tilt: CardTiltState,
    captureLayer: GraphicsLayer,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val read = tilt.read
    val shape = MaterialTheme.shapes.extraLarge
    val primary = MaterialTheme.colorScheme.primary
    val palette = genre?.colorPalette() ?: iridescentGradient
    val frameColors = remember(palette) { palette.litForCard() }
    val foilColors = remember(palette, primary) { palette.foilStops(primary) }

    BoxWithConstraints(
        modifier
            .then(tilt.gestures)
            .graphicsLayer {
                val t = read()
                rotationY = t.x * NOTE_MAX_TILT_DEGREES
                rotationX = -t.y * NOTE_MAX_TILT_DEGREES
                cameraDistance = 14f * density.density
            },
    ) {
        // One unit is 1% of the card's width.
        val unit = maxWidth.value / 100f

        fun dp(percent: Float): Dp = (percent * unit).dp

        fun sp(
            percent: Float,
            min: Float,
            max: Float,
        ): TextUnit = with(density) { (percent * unit).coerceIn(min, max).dp.toSp() }

        val headerFont = MaterialTheme.typography.displaySmall.fontFamily
        val bodyFont = MaterialTheme.typography.bodyLarge.fontFamily
        val bodySize = sp(3.1f, 11f, 17f)
        val bodyStyle =
            TextStyle(fontFamily = bodyFont, fontSize = bodySize, lineHeight = bodySize * 1.5f, color = Color.White)

        Box(
            Modifier
                .fillMaxWidth()
                .recordTo(captureLayer)
                .clip(shape)
                .background(NoteBlack)
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.06f), Color.Transparent),
                            center = Offset(size.width * 0.12f, 0f),
                            radius = size.width * 0.9f,
                        ),
                    )
                }.drawWithContent {
                    drawContent()
                    // Drawn over the text; the clip cuts the outer half of the stroke.
                    drawOutline(
                        outline = shape.createOutline(size, layoutDirection, this),
                        brush = foilBrush(frameColors, read(), size.width, size.height),
                        style = Stroke(width = 3.dp.toPx()),
                    )
                },
        ) {
            Column(
                Modifier
                    .padding(horizontal = dp(7f), vertical = dp(6.5f))
                    .foilText(foilColors, read),
                verticalArrangement = Arrangement.spacedBy(dp(3.2f)),
            ) {
                Text(
                    text = title,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        MaterialTheme.typography.displaySmall.copy(
                            fontSize = sp(6.4f, 20f, 36f),
                            lineHeight = sp(6.4f, 20f, 36f) * 1.08f,
                            color = Color.White,
                        ),
                )

                SparkDivider()

                if (body.isNotBlank()) {
                    DropCapText(
                        text = body,
                        bodyStyle = bodyStyle,
                        capStyle =
                            TextStyle(
                                fontFamily = headerFont,
                                fontSize = bodySize * 3.3f,
                                lineHeight = bodySize * 1.5f * 1.1f,
                                color = Color.White,
                            ),
                    )
                }

                Row(
                    Modifier.fillMaxWidth().padding(top = dp(3f)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = caption,
                        modifier = Modifier.weight(1f).padding(end = dp(4f)),
                        style =
                            TextStyle(
                                fontFamily = bodyFont,
                                fontSize = sp(2.7f, 10f, 14f),
                                color = Color.White.copy(alpha = 0.85f),
                            ),
                    )
                    CompositionLocalProvider(LocalContentColor provides Color.White) {
                        SagaTitle(
                            textStyle =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontSize = sp(4.4f, 15f, 24f),
                                    color = Color.White,
                                ),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fills whatever is drawn in the layer's content with the foil: the content is painted white, and the
 * gradient is laid over it keeping only the letters. [tilt] is read when drawing, so the gradient
 * moves with the card without recomposing.
 */
private fun Modifier.foilText(
    colors: List<Color>,
    tilt: () -> Offset,
): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(
                brush = foilBrush(colors, tilt(), size.width, size.height),
                blendMode = BlendMode.SrcIn,
            )
        }

/** A diagonal gradient that slides sideways with the tilt and mirrors at its ends, so it never runs out. */
private fun foilBrush(
    colors: List<Color>,
    tilt: Offset,
    width: Float,
    height: Float,
): Brush {
    val shift = width * tilt.x * 0.8f
    return Brush.linearGradient(
        colors = colors,
        start = Offset(shift - width * 0.5f, 0f),
        end = Offset(shift + width, height * (1f + tilt.y * 0.5f)),
        tileMode = TileMode.Mirror,
    )
}

/**
 * The palette's bright entries, looped back to the first so the foil is seamless. A palette with fewer
 * than three of them (its dark entries would vanish as text on black) is topped up with lighter
 * tints of the genre colour.
 */
private fun List<Color>.foilStops(primary: Color): List<Color> {
    val bright = filter { it.luminance() >= MIN_FOIL_LUMINANCE }
    val stops =
        if (bright.size >= 3) {
            bright
        } else {
            bright + listOf(primary.lightenTo(0.5f), primary.lightenTo(0.3f), primary.lightenTo(0.6f))
        }
    return stops + stops.first()
}

/** A hairline that fades out on both sides of the Sagas spark. */
@Composable
private fun SparkDivider() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White))))
        Icon(painterResource(R.drawable.ic_spark), contentDescription = null, tint = Color.White, modifier = Modifier.size(width = 15.dp, height = 19.dp))
        Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.White, Color.Transparent))))
    }
}

/**
 * A paragraph that opens with a large initial spanning [DROP_CAP_LINES] lines. Compose has no
 * float-around-an-initial layout, so the paragraph is measured at the width left beside the letter,
 * the first lines are cut where that measurement broke them, and the rest is set at full width.
 */
@Composable
private fun DropCapText(
    text: String,
    bodyStyle: TextStyle,
    capStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val cap = text.first().toString()
    val rest = text.drop(1).trimStart()

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val capGap = with(density) { 6.dp.roundToPx() }
        val capWidth = measurer.measure(cap, capStyle).size.width + capGap
        val narrow = (constraints.maxWidth - capWidth).coerceAtLeast(1)
        val layout = measurer.measure(rest, bodyStyle, constraints = Constraints(maxWidth = narrow))

        val head: String
        val tail: String
        if (layout.lineCount <= DROP_CAP_LINES) {
            head = rest
            tail = ""
        } else {
            val cut = layout.getLineEnd(DROP_CAP_LINES - 1)
            head = rest.substring(0, cut).trimEnd()
            tail = rest.substring(cut).trimStart()
        }

        Column {
            Row {
                Text(cap, style = capStyle, modifier = Modifier.padding(end = 6.dp))
                Text(head, style = bodyStyle)
            }
            if (tail.isNotEmpty()) Text(tail, style = bodyStyle)
        }
    }
}
