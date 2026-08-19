package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val SCRAP_STAGGER_MS = 550L
private const val TITLE_TEAR_DELAY_MS = 300L

/** Photo positions, deliberately clear of the middle band where the title strip lands. */
private data class ScatterSlot(
    val anchor: Alignment,
    val size: Dp,
    val paddingStart: Dp = 0.dp,
    val paddingTop: Dp = 0.dp,
    val paddingEnd: Dp = 0.dp,
    val paddingBottom: Dp = 0.dp,
    val rotation: Float,
)

private val SCATTER_SLOTS =
    listOf(
        ScatterSlot(Alignment.TopStart, size = 132.dp, paddingStart = 10.dp, paddingTop = 26.dp, rotation = -7f),
        ScatterSlot(Alignment.TopEnd, size = 116.dp, paddingEnd = 14.dp, paddingTop = 96.dp, rotation = 6f),
        ScatterSlot(Alignment.TopCenter, size = 100.dp, paddingStart = 96.dp, paddingTop = 186.dp, rotation = -4f),
        ScatterSlot(Alignment.BottomStart, size = 118.dp, paddingStart = 16.dp, paddingBottom = 150.dp, rotation = 5f),
        ScatterSlot(Alignment.BottomEnd, size = 138.dp, paddingEnd = 10.dp, paddingBottom = 34.dp, rotation = -6f),
        ScatterSlot(Alignment.BottomStart, size = 104.dp, paddingStart = 34.dp, paddingBottom = 22.dp, rotation = 8f),
    )

/**
 * Shared body for the Collage template's two "wall of images" stages — the chapter-cover journey
 * and the closing cast page. Photos land one at a time as torn scraps scattered around the frame,
 * with the stage title ripping across the middle on a full-width strip and its copy sitting just
 * under it as a loose note.
 *
 * Not draggable, unlike [CollageFarewellsPage]: there the notes *are* the content and rummaging
 * through them is the point, whereas here the title has to stay legible over the pile.
 */
@Composable
fun CollageScatterLayout(
    imageUrls: List<String>,
    title: String?,
    note: String?,
    canAnimate: Boolean,
    seedBase: Int,
    modifier: Modifier = Modifier,
) {
    val shown = remember(imageUrls) { imageUrls.take(SCATTER_SLOTS.size) }
    var visibleCount by remember { mutableIntStateOf(if (canAnimate) 0 else shown.size) }

    LaunchedEffect(canAnimate, shown.size) {
        if (canAnimate) {
            repeat(shown.size) { index ->
                visibleCount = index + 1
                delay(SCRAP_STAGGER_MS)
            }
        }
    }

    val titleReveal = rememberTearReveal(canAnimate, TITLE_TEAR_DELAY_MS)

    Box(modifier.fillMaxSize()) {
        shown.forEachIndexed { index, url ->
            if (index >= visibleCount) return@forEachIndexed
            val slot = SCATTER_SLOTS[index]
            AssemblingPiece(
                modifier =
                    Modifier
                        .align(slot.anchor)
                        .padding(
                            start = slot.paddingStart,
                            top = slot.paddingTop,
                            end = slot.paddingEnd,
                            bottom = slot.paddingBottom,
                        ),
                rotation = slot.rotation,
                delayMs = 0,
                canAnimate = canAnimate,
                seed = seedBase + index,
            ) {
                TornPhotoScrap(
                    imageUrl = url,
                    seed = seedBase + index,
                    modifier = Modifier.size(slot.size),
                )
            }
        }

        // Drawn after the scraps so the headline always sits on top of the pile.
        Column(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth(1.14f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            title?.let {
                AssemblingPiece(
                    rotation = -1.8f,
                    delayMs = TITLE_TEAR_DELAY_MS,
                    canAnimate = canAnimate,
                    seed = seedBase + 40,
                    entranceOffset = Offset(0f, 26f),
                    scaleFrom = 1f,
                ) {
                    TornPaperStrip(
                        seed = seedBase + 40,
                        modifier = Modifier.fillMaxWidth(),
                        revealProgress = titleReveal,
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 30.dp),
                    ) {
                        Text(
                            text = it,
                            color = PAPER_INK,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            note?.let {
                AssemblingPiece(
                    rotation = 3f,
                    delayMs = TITLE_TEAR_DELAY_MS + 900L,
                    canAnimate = canAnimate,
                    seed = seedBase + 41,
                ) {
                    TornPaperScrap(
                        seed = seedBase + 41,
                        modifier = Modifier.widthIn(max = 300.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    ) {
                        Text(
                            text = it,
                            color = PAPER_INK,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
