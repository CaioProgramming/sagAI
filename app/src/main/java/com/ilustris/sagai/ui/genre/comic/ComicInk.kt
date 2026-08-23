package com.ilustris.sagai.ui.genre.comic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * The comic idiom's drawing kit — ink, paper, boxes and balloons, with no idea what a review page
 * or a milestone is. Split out of the review's own `templates/comic` package so both the story
 * review and the Milestone screen draw a beat with the same hand; the board-layout contracts that
 * only [com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicBoard] can honour —
 * `PanelSpan`, `ComicPanelPage` — deliberately stayed behind with it.
 */

internal val COMIC_INK = Color(0xFF141210)
internal val COMIC_PAPER = Color(0xFFF6E9C9)
internal val COMIC_BALLOON = Color(0xFFFAFAF6)

/**
 * A balloon drawn by whatever is laying the beat out, rather than by the panel it belongs to.
 *
 * Panels clip (see [ComicPanel]), so a caption composed inside a frame can never cross its border.
 * Living one level up is what lets a balloon break out of the frame, and what puts it above every
 * panel in draw order. [alignment] positions it against its own panel's box; [offset] then nudges
 * it, and pushing it past an edge is exactly how a balloon ends up straddling the border.
 *
 * Two very different consumers honour the same spec: the review's comic board places them by
 * absolute board coordinates in a custom measure policy, while a single-beat surface renders them
 * with a plain `Modifier.align(spec.alignment)` inside one `Box`. Same geometry, either way.
 */
data class ComicBalloonSpec(
    val alignment: Alignment = Alignment.BottomCenter,
    val widthFraction: Float = 0.86f,
    val offset: DpOffset = DpOffset(0.dp, 0.dp),
    val content: @Composable () -> Unit,
)

/**
 * A rectangle with its vertical edges pushed off true. Leans are fractions of the frame's width;
 * a positive lean pulls that corner inward, so a row of differently-leaning frames reads as cut
 * rather than tiled.
 */
data class SlantShape(
    val topLeftLean: Float = 0f,
    val topRightLean: Float = 0f,
    val bottomRightLean: Float = 0f,
    val bottomLeftLean: Float = 0f,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline =
        Outline.Generic(
            Path().apply {
                moveTo(size.width * topLeftLean, 0f)
                lineTo(size.width * (1f - topRightLean), 0f)
                lineTo(size.width * (1f - bottomRightLean), size.height)
                lineTo(size.width * bottomLeftLean, size.height)
                close()
            },
        )
}

/**
 * A narration box — the narrator's voice, flat ground and a hard rule, never rounded. Deliberately
 * set at `bodyLarge`: a heading style inside a box that is already a graphic element reads as two
 * competing titles, and at panel scale the box has no room to carry both.
 */
@Composable
fun ComicCaptionBox(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = COMIC_PAPER,
    ink: Color = COMIC_INK,
    italic: Boolean = true,
    align: TextAlign = TextAlign.Center,
    style: TextStyle = MaterialTheme.typography.labelMedium,
) {
    Box(
        modifier
            .background(background)
            .border(2.dp, ink)
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Text(
            text = text,
            color = ink,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            fontWeight = FontWeight.Medium,
            textAlign = align,
            style = style,
        )
    }
}

/**
 * The one loud beat a frame is allowed: a solid block of bold lettering, used where a single word
 * carries the panel and a narration box would undersell it.
 */
@Composable
fun ComicShoutBlock(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = COMIC_INK,
    ink: Color = COMIC_PAPER,
) {
    Box(
        modifier
            .background(background)
            .border(3.dp, ink)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text.uppercase(),
            color = ink,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/**
 * Staggers a frame's own reveal. Panels are only composed once the camera reaches them, so a delay
 * here plays out as the reader arrives rather than having already finished off-screen.
 */
@Composable
fun ComicFadeIn(
    delayMillis: Int = 0,
    durationMillis: Int = 700,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis),
        label = "comicFadeIn",
    )
    Box(modifier.alpha(alpha)) { content() }
}

/**
 * A character speaking, as opposed to the narrator: rounded and white, so dialogue reads as
 * dialogue at a glance without needing the name to be decorated.
 */
@Composable
fun ComicSpeechBalloon(
    text: String,
    modifier: Modifier = Modifier,
    speaker: String? = null,
    speakerColor: Color = COMIC_INK,
) {
    Column(
        modifier
            .background(COMIC_BALLOON, RoundedCornerShape(14.dp))
            .border(2.dp, COMIC_INK, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        speaker?.let {
            Text(
                text = it.uppercase(),
                color = speakerColor,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Text(
            text = text,
            color = COMIC_INK,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/**
 * The small hard-edged tag a comic stamps in a corner — a location, a time, an issue's cover line.
 */
@Composable
fun ComicTag(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .background(COMIC_PAPER)
            .border(2.dp, COMIC_INK)
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text(
            text = text.uppercase(),
            color = COMIC_INK,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
