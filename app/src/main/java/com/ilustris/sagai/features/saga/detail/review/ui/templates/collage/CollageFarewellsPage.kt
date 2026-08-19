package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.data.model.Farewell
import com.ilustris.sagai.features.saga.detail.data.model.cleanMessage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.components.HandwrittenText
import com.ilustris.sagai.ui.theme.hexToColor
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

private val NOTE_WIDTH = 208.dp
private const val NOTE_STAGGER_MS = 700L

private val NOTE_PAPER = Color(0xFFFBF8EF)
private val PAPER_PULP_NOTE = Color(0xFFFFFEFA)
private val NOTE_RULE = Color(0xFF8FA9C7)
private val NOTE_MARGIN_RULE = Color(0xFFD97C7C)

/** Resting spots around the centred title — the notes land scattered, then the reader can drag them anywhere. */
private data class NoteSlot(
    val anchor: Alignment,
    val paddingStart: Dp = 0.dp,
    val paddingTop: Dp = 0.dp,
    val paddingEnd: Dp = 0.dp,
    val paddingBottom: Dp = 0.dp,
    val rotation: Float,
)

private val NOTE_SLOTS =
    listOf(
        NoteSlot(Alignment.TopStart, paddingStart = 8.dp, paddingTop = 24.dp, rotation = -6f),
        NoteSlot(Alignment.TopEnd, paddingEnd = 6.dp, paddingTop = 132.dp, rotation = 5f),
        NoteSlot(Alignment.BottomStart, paddingStart = 10.dp, paddingBottom = 118.dp, rotation = 4f),
        NoteSlot(Alignment.BottomEnd, paddingEnd = 8.dp, paddingBottom = 20.dp, rotation = -5f),
        NoteSlot(Alignment.CenterStart, paddingStart = 20.dp, paddingBottom = 8.dp, rotation = 7f),
    )

/**
 * Punk Rock's send-off: each farewell as a note torn out of a ruled notebook, scattered around the
 * centred stage title and **draggable** — the reader can push them around to read whichever they
 * want, so the page becomes something to rummage through rather than a list to scroll.
 *
 * No avatars: a handwritten note is identified by its signature, so each note closes with the
 * character's name in their own [Character.hexColor] instead of a portrait.
 */
class CollageFarewellsPage(
    override val content: SagaContent,
    private val farewells: List<Farewell>,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.FAREWELLS

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val accent = genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary

        val notes =
            remember(farewells) {
                farewells
                    .mapNotNull { farewell ->
                        content.characters
                            .find { it.data.id == farewell.characterId }
                            ?.let { it.data to farewell.cleanMessage(it.data.name) }
                    }.take(NOTE_SLOTS.size)
            }

        var visibleCount by remember { mutableIntStateOf(if (canAnimate) 0 else notes.size) }
        LaunchedEffect(canAnimate, notes.size) {
            if (canAnimate) {
                repeat(notes.size) { index ->
                    visibleCount = index + 1
                    delay(NOTE_STAGGER_MS)
                }
            }
        }

        Box(modifier.fillMaxSize()) {
            genre.stylisedText(
                text = stringResource(R.string.review_farewells_title),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
            )

            notes.forEachIndexed { index, (character, message) ->
                if (index >= visibleCount) return@forEachIndexed
                val slot = NOTE_SLOTS[index]
                DraggableNote(
                    slot = slot,
                    seed = index + 70,
                    canAnimate = canAnimate,
                ) {
                    NotebookNote(
                        message = message,
                        character = character,
                        fallbackColor = accent,
                        seed = index + 70,
                        canAnimate = canAnimate,
                    )
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    }
}

/**
 * Positions a note at its [slot] and lets the reader drag it freely. Drag deltas are consumed so
 * the surrounding review pager doesn't read the gesture as a page swipe, and the note being
 * dragged is lifted above the others via [zIndex] so it can't slide underneath a neighbour
 * mid-drag.
 */
@Composable
private fun BoxScope.DraggableNote(
    slot: NoteSlot,
    seed: Int,
    canAnimate: Boolean,
    content: @Composable () -> Unit,
) {
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        Modifier
            .align(slot.anchor)
            .padding(
                start = slot.paddingStart,
                top = slot.paddingTop,
                end = slot.paddingEnd,
                bottom = slot.paddingBottom,
            ).offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
            .zIndex(if (isDragging) 1f else 0f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                ) { change, dragAmount ->
                    change.consume()
                    dragOffset += dragAmount
                }
            },
    ) {
        AssemblingPiece(
            rotation = slot.rotation,
            delayMs = 0,
            canAnimate = canAnimate,
            seed = seed,
            // Tremor stops while the reader is handling it — a note being held shouldn't twitch.
            idleTremor = !isDragging,
        ) {
            content()
        }
    }
}

@Composable
private fun NotebookNote(
    message: String,
    character: Character,
    fallbackColor: Color,
    seed: Int,
    canAnimate: Boolean,
) {
    val signatureColor = character.hexColor.hexToColor() ?: fallbackColor
    val lineSpacingPx = with(LocalDensity.current) { 22.dp.toPx() }

    Column(
        Modifier
            .width(NOTE_WIDTH)
            .drawBehind { drawNotebookPaper(seed, lineSpacingPx) }
            .padding(start = 26.dp, end = 16.dp, top = 26.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = message,
            color = PAPER_INK,
            fontStyle = FontStyle.Italic,
            style = MaterialTheme.typography.bodySmall,
        )

        HandwrittenText(
            text = character.name,
            color = signatureColor,
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            isAnimated = canAnimate,
            centered = false,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

/**
 * A page ripped out of a pad: ruled lines and a red margin, torn on **every** edge rather than just
 * the binding side — a note with three factory-clean edges reads as a neat rectangle with a
 * decorated top, which is exactly the "perfect paper" look this is meant to avoid. Drawn rather
 * than shipped as a bitmap so it scales to whatever the note's text needs.
 */
private fun DrawScope.drawNotebookPaper(
    seed: Int,
    lineSpacingPx: Float,
) {
    val amplitude = 13f
    val lipPath = buildTornRectPath(size, seed, amplitude, tearSides = true)
    val bodyPath =
        buildTornRectPath(
            size = size,
            seed = seed,
            amplitude = amplitude,
            inset = 3f,
            amplitudeScale = 0.85f,
            tearSides = true,
        )

    drawStackedShadow(lipPath, steps = 5, maxOffset = Offset(7f, 11f))
    drawPath(lipPath, PAPER_PULP_NOTE)
    drawPath(bodyPath, NOTE_PAPER)

    clipPath(bodyPath) {
        var y = amplitude + lineSpacingPx * 1.4f
        while (y < size.height) {
            drawLine(
                color = NOTE_RULE.copy(alpha = 0.45f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
            y += lineSpacingPx
        }

        val marginX = size.width * 0.12f
        drawLine(
            color = NOTE_MARGIN_RULE.copy(alpha = 0.55f),
            start = Offset(marginX, 0f),
            end = Offset(marginX, size.height),
            strokeWidth = 1.4f,
        )

        repeat(90) { i ->
            val r = Random(seed * 53 + i)
            drawCircle(
                color = PAPER_INK.copy(alpha = 0.012f + r.nextFloat() * 0.025f),
                radius = 0.5f + r.nextFloat() * 1.2f,
                center = Offset(r.nextFloat() * size.width, r.nextFloat() * size.height),
            )
        }

        drawRect(
            brush =
                Brush.verticalGradient(
                    0f to PAPER_INK.copy(alpha = 0.14f),
                    0.18f to Color.Transparent,
                    0.82f to Color.Transparent,
                    1f to PAPER_INK.copy(alpha = 0.12f),
                ),
        )
    }
}
