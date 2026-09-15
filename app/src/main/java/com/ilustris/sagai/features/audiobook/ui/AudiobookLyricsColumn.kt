package com.ilustris.sagai.features.audiobook.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.ui.PageItem
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.ui.theme.hexToColor

/** One narratable beat of prose, split for the karaoke-style scroller — usually a sentence. */
private data class LyricLine(
    val pageIndex: Int,
    val text: String,
    /** This line's char offset range inside its page's full text, to match against [AudioHighlight]. */
    val range: IntRange,
)

private val SENTENCE_SPLIT = Regex("(?<=[.!?…])\\s+")
private const val HIGHLIGHT_SCALE = 1.25f
private const val MAX_BLUR_DP = 10
private const val HIGHLIGHT_ANIM_MS = 200

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
    characters: List<CharacterContent>,
    audiobook: AudiobookUiState?,
    onNarrateSection: (sectionKey: String) -> Unit,
    topInset: Dp,
    /** Height in px of the bottom transport bar overlapping each page, so the fade-out reaches
     * its max before text disappears under it — not only at the true bottom of the screen. */
    bottomInsetPx: Int = 0,
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
                characters = characters,
                highlight = highlight?.takeIf { it.sectionKey == section.key },
                isCurrentlyPlaying = section.key == playingSectionKey,
                topInset = topInset,
                bottomInsetPx = bottomInsetPx,
            )
        } else {
            NarrateSectionPrompt(topInset = topInset, onClick = { onNarrateSection(section.key) })
        }
    }
}

/**
 * Apple-Music-lyrics-style scroller for one chapter's prose: the currently narrated line rests
 * right under the top bar and sharpens there, everything below it dims and blurs the further away
 * it sits. Only auto-follows the highlight while this chapter is the one actually playing — a page
 * the user swiped to just to read ahead stays put.
 */
@Composable
private fun SectionLyrics(
    sectionKey: String,
    pages: List<PageItem>,
    characters: List<CharacterContent>,
    highlight: AudioHighlight?,
    isCurrentlyPlaying: Boolean,
    topInset: Dp,
    bottomInsetPx: Int,
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
                        val start = page.page.content.indexOf(sentence, cursor).takeIf { it >= 0 } ?: cursor
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
                if (exact >= 0) exact else if (lines.isNotEmpty()) 0 else -1
            }
        }

    val listState = rememberLazyListState()
    var viewportHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    // LazyListItemInfo.offset is measured from the viewport's physical top, which already
    // includes contentPadding.top — so the anchored line (scrollOffset = 0 below) sits at this
    // offset, not at 0. Every fraction below must be measured from here, or the anchor line itself
    // reads as already partway faded and everything past it blurs out far too soon.
    val anchorOffsetPx = remember(topInset, density) { with(density) { (topInset + 16.dp).roundToPx() } }

    LaunchedEffect(currentLineIndex, isCurrentlyPlaying) {
        if (!isCurrentlyPlaying || currentLineIndex < 0) return@LaunchedEffect
        // scrollOffset = 0 lands the item's top edge right at the content area's start, which is
        // exactly the top bar's bottom edge thanks to contentPadding below.
        listState.animateScrollToItem(currentLineIndex, scrollOffset = 0)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().onSizeChanged { viewportHeightPx = it.height },
        contentPadding = PaddingValues(top = topInset + 16.dp, start = 32.dp, end = 32.dp, bottom = 260.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(lines.size, key = { "${lines[it].pageIndex}_${lines[it].range.first}" }) { index ->
            val line = lines[index]

            val distanceBelowAnchor by
                remember {
                    derivedStateOf {
                        val info = listState.layoutInfo.visibleItemsInfo.find { it.index == index }
                        val centerY = info?.let { it.offset + it.size / 2 }?.toFloat() ?: 0f
                        (centerY - anchorOffsetPx).coerceAtLeast(0f)
                    }
                }
            // Fades out over the readable area only (viewport minus the transport bar and the
            // anchor's own offset), so lines reach max blur right as they slide under the player
            // instead of at the screen's edge.
            val readableHeightPx = (viewportHeightPx - bottomInsetPx - anchorOffsetPx).coerceAtLeast(1)
            val fraction = (distanceBelowAnchor / readableHeightPx).coerceIn(0f, 1f)
            val blurRadius = (fraction * MAX_BLUR_DP).dp

            LyricLineText(
                line = line,
                characters = characters,
                highlight = highlight?.takeIf { index == currentLineIndex },
                dimAlpha = 1f - fraction * 0.75f,
                blurRadius = blurRadius,
            )
        }
    }
}

@Composable
private fun NarrateSectionPrompt(
    topInset: Dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(top = topInset, start = 32.dp, end = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
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
    characters: List<CharacterContent>,
    highlight: AudioHighlight?,
    dimAlpha: Float,
    blurRadius: Dp,
) {
    val bodyFontFamily = MaterialTheme.typography.bodyLarge.fontFamily
    val titleFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val baseStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = bodyFontFamily)
    val baseColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val highlightBg = primaryColor.copy(alpha = .15f)

    val annotated =
        remember(line, characters, highlight, primaryColor, titleFontFamily) {
            buildAnnotatedString {
                append(line.text)

                // The narrated word only gets special styling — a character name lights up in
                // their color exactly while it is being spoken, not for the whole line forever.
                highlight?.let { h ->
                    val start = (h.charStart - line.range.first).coerceIn(0, line.text.length)
                    val end = (h.charEnd - line.range.first).coerceIn(start, line.text.length)
                    if (end > start) {
                        val speakingCharacter =
                            characters.firstOrNull { character ->
                                val name = character.data.name
                                name.isNotBlank() &&
                                    line.text.regionMatches(start, name, 0, name.length, ignoreCase = true).let { atStart ->
                                        // Either the highlight sits inside a name occurrence, or a name occurrence
                                        // overlaps the highlighted range — check both directions cheaply.
                                        atStart ||
                                            line.text.indexOf(name, ignoreCase = true).let { found ->
                                                found in 0 until end && found + name.length > start
                                            }
                                    }
                            }
                        if (speakingCharacter != null) {
                            val color = speakingCharacter.data.hexColor.hexToColor() ?: primaryColor
                            addStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    fontFamily = titleFontFamily,
                                    fontSize = baseStyle.fontSize * HIGHLIGHT_SCALE,
                                    shadow = Shadow(color, blurRadius = 18f),
                                ),
                                start,
                                end,
                            )
                        } else {
                            addStyle(SpanStyle(color = primaryColor, background = highlightBg), start, end)
                        }
                    }
                }
            }
        }

    Text(
        text = annotated,
        style = baseStyle.copy(color = baseColor, textAlign = TextAlign.Start),
        modifier =
            Modifier
                // The highlighted character's name grows 1.25x and can push the line to wrap
                // differently — this eases that height change instead of popping straight to it.
                .animateContentSize(animationSpec = tween(HIGHLIGHT_ANIM_MS, easing = EaseIn))
                .alpha(dimAlpha)
                .let { if (blurRadius > 0.dp) it.blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded) else it },
    )
}
