package com.ilustris.sagai.features.audiobook.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.ui.PageItem
import com.ilustris.sagai.ui.components.QuotaLimitNotice

/** One word of a [LyricLine], with trailing whitespace kept so joining every word's [text] back
 * together reproduces the line exactly — that lets a plain [FlowRow] wrap them with no extra
 * inter-word spacing logic. [start]/[end] are absolute page-content offsets, matching [AudioHighlight]. */
private data class LyricWord(
    val text: String,
    val start: Int,
    val end: Int,
)

/** One narratable beat of prose, split for the karaoke-style scroller — usually a sentence. */
private data class LyricLine(
    val pageIndex: Int,
    val text: String,
    /** This line's char offset range inside its page's full text, to match against [AudioHighlight]. */
    val range: IntRange,
    val words: List<LyricWord>,
)

private val SENTENCE_SPLIT = Regex("(?<=[.!?…])\\s+")
private val WORD_SPLIT = Regex("\\S+\\s*")
private const val MAX_BLUR_DP = 10
private const val GLOW_BLUR_RADIUS = 24f
private const val WORD_REVEAL_ANIM_MS = 350
private const val WORD_GLOW_ANIM_MS = 250
private const val WORD_SCALE_BUMP = 0.06f

/** Fraction of the readable area, starting at the anchor, that stays perfectly sharp before the
 * blur/dim ramp begins. */
private const val SHARP_ZONE_FRACTION = 0.5f

private fun splitWords(
    text: String,
    baseOffset: Int,
): List<LyricWord> =
    WORD_SPLIT.findAll(text).map { match ->
        LyricWord(text = match.value, start = baseOffset + match.range.first, end = baseOffset + match.range.last + 1)
    }.toList()

/**
 * One page per chapter, swipeable like an album's track list. The page currently narrating
 * auto-follows the highlight and auto-advances the pager when narration crosses into the next
 * chapter; swiping or the skip buttons jump playback to the chapter landed on instead — same
 * model as skipping tracks. A chapter without audio yet shows a full-page narrate prompt instead
 * of text. Narration only ever runs front-to-back, so [pageSections] must already be limited to
 * the readable run plus at most one pending chapter.
 */
@Composable
fun AudiobookChapterPager(
    pagerState: PagerState,
    pageSections: List<AudiobookSectionUi>,
    pages: List<PageItem>,
    audiobook: AudiobookUiState?,
    onNarrateSection: (sectionKey: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val highlight = audiobook?.highlight
    val playingSectionKey = audiobook?.playingSectionKey

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) { pageIndex ->
        val section = pageSections.getOrNull(pageIndex) ?: return@HorizontalPager
        if (section.isReady) {
            SectionLyrics(
                sectionKey = section.key,
                pages = pages,
                highlight = highlight?.takeIf { it.sectionKey == section.key },
                isCurrentlyPlaying = section.key == playingSectionKey,
            )
        } else {
            NarrateSectionPrompt(
                quotaResetAt = audiobook?.ttsQuotaResetAt,
                onClick = { onNarrateSection(section.key) },
            )
        }
    }
}

/**
 * Apple-Music-lyrics-style scroller for one chapter's prose: the currently narrated line rests
 * right at the top and sharpens there, everything below it dims and blurs the further away it
 * sits. Only auto-follows the highlight while this chapter is the one actually playing — a page
 * the user swiped to just to read ahead stays put, and the whole page reads 100% sharp whenever
 * there is no active highlight (nothing narrating, or narration for this chapter has finished).
 */
@Composable
private fun SectionLyrics(
    sectionKey: String,
    pages: List<PageItem>,
    highlight: AudioHighlight?,
    isCurrentlyPlaying: Boolean,
) {
    val lines =
        remember(pages, sectionKey) {
            pages
                .filterIsInstance<PageItem.Content>()
                .filter { it.sectionKey == sectionKey }
                .flatMap { page ->
                    val sentences = SENTENCE_SPLIT.split(page.page.content)
                    var cursor = 0
                    sentences.mapNotNull { sentence ->
                        val start =
                            page.page.content
                                .indexOf(sentence, cursor)
                                .takeIf { it >= 0 } ?: cursor
                        val end = start + sentence.length
                        cursor = end
                        sentence.trim().takeIf(String::isNotEmpty)?.let {
                            LyricLine(pageIndex = page.pageIndex, text = it, range = start..end, words = splitWords(it, start))
                        }
                    }
                }
        }

    val currentLineIndex =
        remember(lines, highlight?.pageIndex, highlight?.charStart) {
            if (highlight == null) {
                -1
            } else {
                val exact = lines.indexOfFirst { highlight.charStart in it.range && it.pageIndex == highlight.pageIndex }
                if (exact >= 0) {
                    exact
                } else if (lines.isNotEmpty()) {
                    0
                } else {
                    -1
                }
            }
        }
    // Nothing to focus on: either this chapter isn't the one playing, or narration for it hasn't
    // started/has already finished. Reading is the point here, so leave every line fully sharp.
    val hasActiveHighlight = isCurrentlyPlaying && currentLineIndex >= 0

    val listState = rememberLazyListState()
    var viewportHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    // The anchor sits at contentPadding.top below (16.dp), a small fixed offset. The lyrics area
    // itself is already boxed in between the topbar and the player by the caller's weight(1f), so
    // this component only ever needs to fill that exact space — no manual inset compensation.
    val anchorOffsetPx = remember(density) { with(density) { 16.dp.roundToPx() } }

    LaunchedEffect(currentLineIndex, isCurrentlyPlaying) {
        if (!isCurrentlyPlaying || currentLineIndex < 0) return@LaunchedEffect
        // scrollOffset = 0 lands the item's top edge right at the content area's start.
        listState.animateScrollToItem(currentLineIndex, scrollOffset = 0)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().onSizeChanged { viewportHeightPx = it.height },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(lines.size, key = { "${lines[it].pageIndex}_${lines[it].range.first}" }) { index ->
            val line = lines[index]

            val fraction: Float
            if (hasActiveHighlight) {
                // Measured from the item's TOP, not its center: at displaySmall a wrapped
                // paragraph can be tall enough that half its own height alone would already read
                // as a big "distance" — which blurred the anchored, currently-playing line itself.
                val distanceBelowAnchor by
                    remember {
                        derivedStateOf {
                            val info = listState.layoutInfo.visibleItemsInfo.find { it.index == index }
                            val topY = info?.offset?.toFloat() ?: 0f
                            (topY - anchorOffsetPx).coerceAtLeast(0f)
                        }
                    }
                // Fades out over the readable area only, so lines reach max blur right as they
                // slide off screen. The first SHARP_ZONE_FRACTION of it stays fully sharp — at
                // displaySmall a single line already fills a good chunk of the viewport, so fading
                // from the anchor itself blurred text the reader hadn't scrolled past yet.
                val readableHeightPx = (viewportHeightPx - anchorOffsetPx).coerceAtLeast(1)
                val rawFraction = (distanceBelowAnchor / readableHeightPx).coerceIn(0f, 1f)
                fraction = ((rawFraction - SHARP_ZONE_FRACTION) / (1f - SHARP_ZONE_FRACTION)).coerceIn(0f, 1f)
            } else {
                fraction = 0f
            }
            val blurRadius = (fraction * MAX_BLUR_DP).dp

            LyricLineText(
                line = line,
                highlight = highlight?.takeIf { index == currentLineIndex },
                dimAlpha = 1f - fraction * 0.75f,
                blurRadius = blurRadius,
            )
        }
    }
}

@Composable
private fun NarrateSectionPrompt(
    quotaResetAt: Long?,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        // TTS is already known to be out of quota for the day — showing the button would just
        // invite a request known to fail, so the reset time replaces it instead.
        if (quotaResetAt != null) {
            QuotaLimitNotice(until = quotaResetAt)
            return@Box
        }
        Button(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.audiobook_listen_next_chapter_cd))
        }
    }
}

/**
 * Karaoke-style fill: everything up to the word being spoken right now reads at full brightness
 * (already-heard text stays that way, not just the exact current word), and the current word gets
 * a brief glow + subtle grow as narration reaches it. Lines with no highlight (not the one playing,
 * or read-ahead pages) skip the per-word machinery entirely — a single [Text] is all they need.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LyricLineText(
    line: LyricLine,
    highlight: AudioHighlight?,
    dimAlpha: Float,
    blurRadius: Dp,
) {
    val bodyFontFamily = MaterialTheme.typography.bodyLarge.fontFamily
    val baseStyle = MaterialTheme.typography.headlineLarge.copy(fontFamily = bodyFontFamily)
    val baseColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .85f)

    val modifier =
        Modifier
            .alpha(dimAlpha)
            .let { if (blurRadius > 0.dp) it.blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded) else it }

    if (highlight == null) {
        Text(
            text = line.text,
            style = baseStyle.copy(color = baseColor, textAlign = TextAlign.Start),
            modifier = modifier,
        )
    } else {
        FlowRow(modifier = modifier) {
            line.words.forEach { word ->
                LyricWordText(word = word, highlight = highlight, baseStyle = baseStyle, baseColor = baseColor)
            }
        }
    }
}

@Composable
private fun LyricWordText(
    word: LyricWord,
    highlight: AudioHighlight,
    baseStyle: TextStyle,
    baseColor: Color,
) {
    // "Spoken" covers both words already fully said and the one being said right now — the fill.
    // "Current" is just the leading edge of that fill, the word narration is on at this instant.
    val isSpoken = word.start < highlight.charEnd
    val isCurrent = isSpoken && highlight.charStart < word.end

    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val glowColor = MaterialTheme.colorScheme.primary

    // Independent per-word animations: each word's own reveal/glow plays out on its own and is
    // never interrupted by the next word starting, since it isn't sharing state with any other
    // word — only this word's own isSpoken/isCurrent flip retargets it.
    val brightness by animateFloatAsState(if (isSpoken) 1f else 0f, tween(WORD_REVEAL_ANIM_MS), label = "wordBrightness")
    val glow by animateFloatAsState(if (isCurrent) 1f else 0f, tween(WORD_GLOW_ANIM_MS), label = "wordGlow")

    Text(
        text = word.text,
        style =
            baseStyle.copy(
                color = lerp(baseColor, onPrimaryColor, brightness),
                shadow = Shadow(glowColor.copy(alpha = glow), blurRadius = GLOW_BLUR_RADIUS),
                textAlign = TextAlign.Start,
            ),
        modifier = Modifier.scale(1f + WORD_SCALE_BUMP * glow),
    )
}
