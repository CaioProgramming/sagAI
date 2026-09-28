package com.ilustris.sagai.features.live.ui

import android.os.SystemClock
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.Layout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.audiobook.ui.splitBeats
import com.ilustris.sagai.features.live.presentation.LiveCaption
import com.ilustris.sagai.features.saga.chat.data.voicing.BlockType
import com.ilustris.sagai.features.saga.chat.data.voicing.MessageBlock

/** One lyric-sized line of a caption block: short enough that the spoken word never leaves the screen. */
private class CaptionBeat(
    val block: MessageBlock,
    val blockLength: Int,
    val words: List<CaptionWord>,
)

/** A word and its char range inside its block, so it can be compared with the block's progress. */
private class CaptionWord(
    val text: String,
    val start: Int,
    val end: Int,
)

private val WORD_SPLIT = Regex("\\S+\\s*")

private fun beatsOf(blocks: List<MessageBlock>): List<CaptionBeat> =
    blocks.flatMap { block ->
        splitBeats(block.text).map { range ->
            val words =
                WORD_SPLIT.findAll(block.text.substring(range)).map { match ->
                    CaptionWord(
                        text = match.value,
                        start = range.first + match.range.first,
                        end = range.first + match.range.last + 1,
                    )
                }.toList()
            CaptionBeat(block, block.text.length, words)
        }
    }

/**
 * The line under the blob, karaoke-style — the same fill as the audiobook: what has been said reads
 * bright with a soft halo, the word being said glows in the speaker's color and grows a touch, and
 * what's still ahead sits dim behind a light blur. [progress] is how much of the current block has
 * been spoken; it ticks with playback, so only the words read it, and each one recomposes only when
 * it flips from ahead to spoken.
 */
@Composable
fun LiveKaraokeCaption(
    caption: LiveCaption,
    progress: () -> Float,
    glowColor: Color,
    modifier: Modifier = Modifier,
) {
    val beats = remember(caption.blocks) { beatsOf(caption.blocks) }
    if (beats.isEmpty()) return
    // A line already heard in full (the player's own, or a reply that finished) has no fill left.
    val settled = caption.isPlayerLine || caption.allDone || caption.current < 0

    val currentBeat by remember(beats, caption.current, settled) {
        derivedStateOf {
            if (settled) {
                -1
            } else {
                val blockBeats = beats.withIndex().filter { it.value.block.index == caption.current }
                val spoken = (progress() * (blockBeats.firstOrNull()?.value?.blockLength ?: 0)).toInt()
                blockBeats.lastOrNull { spoken >= it.value.words.first().start }?.index
                    ?: blockBeats.firstOrNull()?.index
                    ?: -1
            }
        }
    }
    // The whole line is scrollable — anything already said can be read back. While the voice is
    // playing the list follows it, keeping one line of context above the current one, unless the
    // player has just scrolled by hand: then it waits a few seconds before taking over again.
    val listState = rememberLazyListState()
    var lastManualScroll by remember { mutableLongStateOf(0L) }
    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start || interaction is DragInteraction.Stop) {
                lastManualScroll = SystemClock.uptimeMillis()
            }
        }
    }
    LaunchedEffect(currentBeat) {
        if (currentBeat < 0) return@LaunchedEffect
        if (SystemClock.uptimeMillis() - lastManualScroll < MANUAL_SCROLL_HOLD_MS) return@LaunchedEffect
        listState.animateScrollToItem((currentBeat - 1).coerceAtLeast(0))
    }
    // A new line (the player's, then the reply) starts from its top.
    LaunchedEffect(caption.blocks) { listState.scrollToItem(0) }

    LazyColumn(
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BEAT_SPACING),
        modifier = modifier.fadeTopEdge(listState.canScrollBackward),
    ) {
        itemsIndexed(beats) { _, beat ->
            CenteredWordWrap(Modifier.fillMaxWidth()) {
                beat.words.forEach { word ->
                    CaptionWordText(
                        word = word,
                        beat = beat,
                        caption = caption,
                        settled = settled,
                        progress = progress,
                        glowColor = glowColor,
                    )
                }
            }
        }
    }
}

/** Fades the top edge once there's something scrolled away above it. */
private fun Modifier.fadeTopEdge(active: Boolean): Modifier =
    if (!active) {
        this
    } else {
        graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    Brush.verticalGradient(0f to Color.Transparent, 0.18f to Color.Black),
                    blendMode = BlendMode.DstIn,
                )
            }
    }

/**
 * Words wrapped into centered lines. Not FlowRow: this screen lives under the app's
 * SharedTransitionLayout, whose lookahead pass made FlowRow place words it hadn't measured in that
 * pass when the caption window changed ("LookaheadDelegate has not been measured yet"). This
 * measures every word on every pass, so there's nothing left unmeasured to place.
 */
@Composable
private fun CenteredWordWrap(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val maxWidth = constraints.maxWidth
        val lines = mutableListOf<MutableList<Int>>(mutableListOf())
        var lineWidth = 0
        placeables.forEachIndexed { index, placeable ->
            if (lineWidth + placeable.width > maxWidth && lines.last().isNotEmpty()) {
                lines.add(mutableListOf())
                lineWidth = 0
            }
            lines.last().add(index)
            lineWidth += placeable.width
        }
        val lineHeights = lines.map { line -> line.maxOfOrNull { placeables[it].height } ?: 0 }
        val lineGap = WRAPPED_LINE_GAP.roundToPx()
        val width = if (constraints.hasBoundedWidth) maxWidth else placeables.sumOf { it.width }
        val height =
            (lineHeights.sum() + lineGap * (lines.size - 1).coerceAtLeast(0))
                .coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(width, height) {
            var y = 0
            lines.forEachIndexed { lineIndex, line ->
                var x = (width - line.sumOf { placeables[it].width }) / 2
                line.forEach { index ->
                    val placeable = placeables[index]
                    placeable.place(x, y + (lineHeights[lineIndex] - placeable.height) / 2)
                    x += placeable.width
                }
                y += lineHeights[lineIndex] + lineGap
            }
        }
    }
}

@Composable
private fun CaptionWordText(
    word: CaptionWord,
    beat: CaptionBeat,
    caption: LiveCaption,
    settled: Boolean,
    progress: () -> Float,
    glowColor: Color,
) {
    val blockIndex = beat.block.index
    // derivedStateOf so the 40ms playback ticks only recompose a word when it actually flips.
    val isSpoken by remember(word, blockIndex, caption.current, settled) {
        derivedStateOf {
            when {
                settled -> true
                blockIndex < caption.current -> true
                blockIndex > caption.current -> false
                else -> progress() * beat.blockLength > word.start
            }
        }
    }
    val isCurrent by remember(word, blockIndex, caption.current, settled) {
        derivedStateOf {
            !settled && blockIndex == caption.current && progress() * beat.blockLength in word.start.toFloat()..word.end.toFloat()
        }
    }

    val brightness by animateFloatAsState(if (isSpoken) 1f else 0f, tween(WORD_REVEAL_MS), label = "captionWordBrightness")
    val glow by animateFloatAsState(if (isCurrent) 1f else 0f, tween(WORD_GLOW_MS), label = "captionWordGlow")
    // Words already said keep a softer halo, so the filled part reads as lit and the current word
    // as the brightest point in it. A settled line (nothing playing) drops the halo entirely.
    val glowAlpha = if (settled) 0f else SPOKEN_GLOW_ALPHA * brightness + (1f - SPOKEN_GLOW_ALPHA) * glow
    val glowRadius = SPOKEN_GLOW_RADIUS + (GLOW_RADIUS - SPOKEN_GLOW_RADIUS) * glow
    // Sharpens into focus as it brightens: ahead stays legible, just softer.
    val wordBlur = (1f - brightness) * WORD_BLUR_DP

    val quiet = beat.block.type == BlockType.ACTION || beat.block.type == BlockType.THINK
    val on = MaterialTheme.colorScheme.onBackground
    val dim = on.copy(alpha = if (quiet) 0.3f else 0.4f)
    val lit = on.copy(alpha = if (quiet) 0.75f else 1f)

    Text(
        text = word.text,
        style =
            TextStyle(
                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                fontSize = if (quiet) 17.sp else 22.sp,
                lineHeight = if (quiet) 26.sp else 32.sp,
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
                fontStyle = if (quiet) FontStyle.Italic else FontStyle.Normal,
                color = lerp(dim, lit, brightness),
                shadow = Shadow(glowColor.copy(alpha = glowAlpha), blurRadius = glowRadius),
            ),
        modifier =
            Modifier
                .scale(1f + WORD_SCALE_BUMP * glow)
                .let { if (wordBlur > 0f) it.blur(wordBlur.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded) else it }
                // Inside the blur's layer on purpose: the layer is only as big as the word, and the
                // genre fonts' tall ascenders and descenders got cut at its edges.
                .padding(vertical = GLYPH_ROOM),
    )
}

private const val MANUAL_SCROLL_HOLD_MS = 3_000L
private val BEAT_SPACING = 12.dp
private val WRAPPED_LINE_GAP = 2.dp
private val GLYPH_ROOM = 4.dp
private const val GLOW_RADIUS = 40f
private const val SPOKEN_GLOW_RADIUS = 24f
private const val SPOKEN_GLOW_ALPHA = 0.4f
private const val WORD_REVEAL_MS = 350
private const val WORD_GLOW_MS = 250
private const val WORD_SCALE_BUMP = 0.06f
private const val WORD_BLUR_DP = 3f
