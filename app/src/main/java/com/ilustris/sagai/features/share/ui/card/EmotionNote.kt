package com.ilustris.sagai.features.share.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.lerp
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

/**
 * The emotional share: no image, no 3D. A page torn from the saga's book, in the genre's own
 * colours and fonts, with the title, a divider with the Sagas spark, the text under a drop cap, and
 * the caption beside the Sagas mark.
 *
 * It is as wide as it can be rather than tall: every size is a share of the card's width, so a
 * longer text makes a slightly smaller type instead of a taller card. It does not depend on the
 * device's light or dark theme, because the picture has to look the same wherever it is opened.
 */
@Composable
fun EmotionNote(
    genre: Genre?,
    title: String,
    body: String,
    caption: String,
    captureLayer: GraphicsLayer,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val shape = MaterialTheme.shapes.extraLarge
    val primary = MaterialTheme.colorScheme.primary
    val palette = genre?.colorPalette() ?: iridescentGradient
    val accent = remember(primary) { primary.lightenTo(0.5f) }
    val accentSoft = remember(primary) { primary.lightenTo(0.3f) }
    val backdrop =
        remember(palette) {
            val byLuminance = palette.sortedBy { it.luminance() }
            val darkest = byLuminance.first()
            val second = byLuminance.getOrElse(1) { darkest }
            lerp(second, darkest, 0.55f) to darkest
        }
    val edge = remember(accent) { accent.copy(alpha = 0.35f) }

    BoxWithConstraints(modifier.recordTo(captureLayer)) {
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
            TextStyle(fontFamily = bodyFont, fontSize = bodySize, lineHeight = bodySize * 1.5f, color = Color(0xFFEFE8F3))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Brush.linearGradient(listOf(backdrop.first, backdrop.second)))
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            listOf(primary.copy(alpha = 0.28f), Color.Transparent),
                            center = Offset(size.width * 0.2f, 0f),
                            radius = size.width * 0.9f,
                        ),
                    )
                    drawRect(
                        Brush.radialGradient(
                            listOf(accent.copy(alpha = 0.16f), Color.Transparent),
                            center = Offset(size.width, size.height),
                            radius = size.width * 0.8f,
                        ),
                    )
                    drawRect(
                        Brush.radialGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                            center = center,
                            radius = size.maxDimension * 0.75f,
                        ),
                    )
                }.border(1.dp, edge, shape),
        ) {
            Text(
                text = "“",
                modifier = Modifier.align(Alignment.TopEnd).offset(x = dp(-3f), y = dp(-4f)),
                style =
                    TextStyle(
                        fontFamily = headerFont,
                        fontSize = sp(34f, 120f, 240f),
                        color = accent.copy(alpha = 0.08f),
                    ),
            )

            Column(
                Modifier.padding(horizontal = dp(7f), vertical = dp(6.5f)),
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
                            brush = Brush.linearGradient(listOf(accent, accentSoft)),
                        ),
                )

                SparkDivider(accent)

                if (body.isNotBlank()) {
                    DropCapText(
                        text = body,
                        bodyStyle = bodyStyle,
                        capStyle =
                            TextStyle(
                                fontFamily = headerFont,
                                fontSize = bodySize * 3.3f,
                                lineHeight = bodySize * 1.5f * 1.1f,
                                color = accent,
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
                                color = accentSoft,
                            ),
                    )
                    CompositionLocalProvider(LocalContentColor provides accent) {
                        SagaTitle(
                            textStyle =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontSize = sp(4.4f, 15f, 24f),
                                    color = accent,
                                ),
                        )
                    }
                }
            }
        }
    }
}

/** A hairline that fades out on both sides of the Sagas spark. */
@Composable
private fun SparkDivider(color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, color))))
        Icon(painterResource(R.drawable.ic_spark), contentDescription = null, tint = color, modifier = Modifier.size(width = 15.dp, height = 19.dp))
        Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(color, Color.Transparent))))
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
