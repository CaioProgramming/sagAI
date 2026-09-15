package com.ilustris.sagai.features.audiobook.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.act.ui.PageItem
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.ui.theme.hexToColor

/** One narratable beat of prose, split for the karaoke-style scroller — usually a sentence. */
private data class LyricLine(
    val sectionKey: String,
    val pageIndex: Int,
    val chapterTitle: String,
    val text: String,
    /** This line's char offset range inside its page's full text, to match against [AudioHighlight]. */
    val range: IntRange,
    val isFirstOfSection: Boolean,
)

private val SENTENCE_SPLIT = Regex("(?<=[.!?…])\\s+")
private const val HIGHLIGHT_SCALE = 1.25f
private const val MAX_BLUR_DP = 10

/**
 * Apple-Music-lyrics-style scroller: the currently narrated line rests right under the top bar and
 * sharpens there, everything below it dims and blurs the further away it sits. Only reveals the
 * book up to the first section without audio yet — narration has to happen front-to-back — and
 * offers a single button to narrate exactly that next section.
 */
@Composable
fun AudiobookLyricsColumn(
    pages: List<PageItem>,
    characters: List<CharacterContent>,
    audiobook: AudiobookUiState?,
    onNarrateNextSection: (sectionKey: String) -> Unit,
    topInset: Dp,
    modifier: Modifier = Modifier,
) {
    val sections = audiobook?.sections.orEmpty()
    // Sections are narrated strictly front-to-back: only a run of ready sections from the very
    // start counts as "readable" — a later section flagged ready out of order is ignored too.
    val readableSectionKeys = remember(sections) { sections.takeWhile { it.isReady }.map { it.key }.toSet() }
    val nextSectionKey = remember(sections, readableSectionKeys) { sections.getOrNull(readableSectionKeys.size)?.key }

    val lines =
        remember(pages, readableSectionKeys) {
            val seenSections = mutableSetOf<String>()
            pages
                .filterIsInstance<PageItem.Content>()
                .filter { it.sectionKey in readableSectionKeys }
                .flatMap { page ->
                    val sentences = SENTENCE_SPLIT.split(page.page.content)
                    var cursor = 0
                    sentences.mapIndexedNotNull { index, sentence ->
                        val start = page.page.content.indexOf(sentence, cursor).takeIf { it >= 0 } ?: cursor
                        val end = start + sentence.length
                        cursor = end
                        sentence.trim().takeIf(String::isNotEmpty)?.let {
                            LyricLine(
                                sectionKey = page.sectionKey,
                                pageIndex = page.pageIndex,
                                chapterTitle = page.chapterTitle,
                                text = it,
                                range = start..end,
                                isFirstOfSection = index == 0 && seenSections.add(page.sectionKey),
                            )
                        }
                    }
                }
        }

    val highlight = audiobook?.highlight
    val currentLineIndex =
        remember(lines, highlight?.sectionKey, highlight?.pageIndex, highlight?.charStart) {
            if (highlight == null) {
                -1
            } else {
                val exact =
                    lines.indexOfFirst {
                        it.sectionKey == highlight.sectionKey &&
                            it.pageIndex == highlight.pageIndex &&
                            highlight.charStart in it.range
                    }
                if (exact >= 0) exact else lines.indexOfFirst { it.sectionKey == highlight.sectionKey }
            }
        }

    val listState = rememberLazyListState()
    var viewportHeightPx by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex < 0) return@LaunchedEffect
        // scrollOffset = 0 lands the item's top edge right at the content area's start, which is
        // exactly the top bar's bottom edge thanks to contentPadding below.
        listState.animateScrollToItem(currentLineIndex, scrollOffset = 0)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.onSizeChanged { viewportHeightPx = it.height },
        contentPadding = PaddingValues(top = topInset + 16.dp, start = 32.dp, end = 32.dp, bottom = 260.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(lines.size, key = { "${lines[it].sectionKey}_${lines[it].pageIndex}_${lines[it].range.first}" }) { index ->
            val line = lines[index]

            val distanceBelowAnchor by
                remember {
                    derivedStateOf {
                        val info = listState.layoutInfo.visibleItemsInfo.find { it.index == index }
                        val centerY = info?.let { it.offset + it.size / 2 }?.toFloat() ?: 0f
                        centerY.coerceAtLeast(0f)
                    }
                }
            val fraction = (distanceBelowAnchor / viewportHeightPx.coerceAtLeast(1)).coerceIn(0f, 1f)
            val blurRadius = (fraction * MAX_BLUR_DP).dp

            LyricLineText(
                line = line,
                characters = characters,
                highlight = highlight?.takeIf { index == currentLineIndex },
                dimAlpha = 1f - fraction * 0.75f,
                blurRadius = blurRadius,
            )
        }

        nextSectionKey?.let { key ->
            val title = sections.find { it.key == key }?.title.orEmpty()
            item(key = "narrate_$key") {
                NarrateSectionPrompt(chapterTitle = title, onClick = { onNarrateNextSection(key) })
            }
        }
    }
}

@Composable
private fun NarrateSectionPrompt(
    chapterTitle: String,
    onClick: () -> Unit,
) {
    Box(Modifier.padding(vertical = 12.dp)) {
        Button(onClick = onClick, shape = RoundedCornerShape(50)) {
            Text(chapterTitle)
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
                .alpha(dimAlpha)
                .let { if (blurRadius > 0.dp) it.blur(blurRadius) else it },
    )
}
