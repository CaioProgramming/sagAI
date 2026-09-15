package com.ilustris.sagai.features.audiobook.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.ui.PageItem
import com.ilustris.sagai.ui.components.QuotaLimitNotice

/** One narratable beat of prose, split for the karaoke-style scroller — usually a sentence. */
private data class LyricLine(
    val pageIndex: Int,
    val text: String,
    /** This line's char offset range inside its page's full text, to match against [AudioHighlight]. */
    val range: IntRange,
)

private val SENTENCE_SPLIT = Regex("(?<=[.!?…])\\s+")
private const val MAX_BLUR_DP = 10
private const val GLOW_BLUR_RADIUS = 24f

/** Fraction of the readable area, starting at the anchor, that stays perfectly sharp before the
 * blur/dim ramp begins. */
private const val SHARP_ZONE_FRACTION = 0.5f

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
                            LyricLine(pageIndex = page.pageIndex, text = it, range = start..end)
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
    val glowColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    val annotated =
        remember(line, highlight, glowColor, onPrimaryColor) {
            buildAnnotatedString {
                append(line.text)

                // No per-character styling — just the word being spoken right now, lit in
                // onPrimary with a soft glow from the theme's primary color behind it.
                highlight?.let { h ->
                    val start = (h.charStart - line.range.first).coerceIn(0, line.text.length)
                    val end = (h.charEnd - line.range.first).coerceIn(start, line.text.length)
                    if (end > start) {
                        addStyle(
                            SpanStyle(
                                color = onPrimaryColor,
                                shadow = Shadow(glowColor, blurRadius = GLOW_BLUR_RADIUS),
                            ),
                            start,
                            end,
                        )
                    }
                }
            }
        }

    Text(
        text = annotated,
        style = baseStyle.copy(color = baseColor, textAlign = TextAlign.Start),
        modifier =
            Modifier
                .alpha(dimAlpha)
                .let { if (blurRadius > 0.dp) it.blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded) else it },
    )
}
