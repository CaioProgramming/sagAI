package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.home.data.model.ChapterMetadata
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradient
import kotlinx.coroutines.delay

private val RailWidth = 36.dp
private val RailRowHeight = 28.dp
private val TitleMaxWidth = 180.dp
private const val HIDE_DELAY_MS = 1500L

/**
 * Fast-scroll index for the chat, in the spirit of the A–Z rail in a contacts app: one Roman
 * numeral per chapter, oldest at the top to match the reading order of the (reversed) message list.
 *
 * Idle it is invisible *and* transparent to touch — the strip sits over the message bubbles, so
 * intercepting there would steal taps and long-presses from them. Scrolling the list reveals it and
 * arms the gestures; it fades out again shortly after the list settles.
 *
 * Only the numerals column is interactive. The active row grows leftward to spell out the chapter
 * title, so the rail as a whole is wider than what it hit-tests.
 *
 * @param chapters chronological, as [com.ilustris.sagai.features.home.data.model.flatChapters] returns them.
 */
@Composable
fun ChapterScrollRail(
    chapters: List<ChapterMetadata>,
    currentChapterId: Int?,
    genre: Genre,
    isListScrolling: Boolean,
    onChapterSelected: (chapterId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (chapters.size < 2) return

    val rowHeightPx = with(LocalDensity.current) { RailRowHeight.toPx() }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isListScrolling, draggedIndex != null) {
        if (isListScrolling || draggedIndex != null) {
            isVisible = true
        } else {
            delay(HIDE_DELAY_MS)
            isVisible = false
        }
    }

    val railAlpha by animateFloatAsState(if (isVisible) 1f else 0f, tween(300), label = "railAlpha")

    fun indexAt(y: Float) = (y / rowHeightPx).toInt().coerceIn(chapters.indices)

    Box(modifier, contentAlignment = Alignment.CenterEnd) {
        Column(
            modifier = Modifier.alpha(railAlpha),
            horizontalAlignment = Alignment.End,
        ) {
            chapters.forEachIndexed { index, chapter ->
                ChapterRailRow(
                    number = index + 1,
                    title = chapter.data.title,
                    isActive =
                        draggedIndex?.let { it == index }
                            ?: (chapter.data.id == currentChapterId),
                    genre = genre,
                )
            }
        }

        // Hit target kept to the numerals only, and armed only while visible: a wide or invisible
        // strip would swallow interactions meant for the bubbles underneath. Same height as the
        // column above (rows are a fixed height), so a touch maps to a row by simple division.
        Box(
            Modifier
                .width(RailWidth)
                .height(RailRowHeight * chapters.size)
                .then(
                    if (!isVisible) {
                        Modifier
                    } else {
                        Modifier.pointerInput(chapters) {
                            // Hand-rolled rather than detectTapGestures + detectVerticalDragGestures:
                            // those only report a drag once touch slop is crossed, so a press that
                            // stays put wouldn't mark the rail as engaged and the idle timer would
                            // fade it out — disarming the gestures mid-touch, under the finger.
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                down.consume()
                                var index = indexAt(down.position.y)
                                draggedIndex = index
                                onChapterSelected(chapters[index].data.id)

                                while (true) {
                                    val change =
                                        awaitPointerEvent().changes.firstOrNull() ?: break
                                    if (!change.pressed) break
                                    val movedTo = indexAt(change.position.y)
                                    if (movedTo != index) {
                                        index = movedTo
                                        draggedIndex = index
                                        onChapterSelected(chapters[index].data.id)
                                    }
                                    change.consume()
                                }
                                draggedIndex = null
                            }
                        }
                    },
                ),
        )
    }
}

@Composable
private fun ChapterRailRow(
    number: Int,
    title: String,
    isActive: Boolean,
    genre: Genre,
) {
    val activeStyle =
        MaterialTheme.typography.labelMedium.copy(
            brush = genre.gradient(),
            fontWeight = FontWeight.Black,
        )

    // Idle numerals float straight over the bubbles, so they carry a halo of the background
    // colour to stay readable; the active row gets a real surface instead.
    val idleStyle =
        MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontWeight = FontWeight.Normal,
            shadow = Shadow(color = MaterialTheme.colorScheme.background, blurRadius = 12f),
        )

    Row(
        Modifier
            .height(RailRowHeight)
            .then(
                if (isActive) {
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(genre.cornerSize()),
                        )
                        // Start only: end padding would nudge the numeral sideways as rows
                        // activate, making the whole rail twitch during a drag.
                        .padding(start = 12.dp)
                } else {
                    Modifier
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        // The title is the numeral's own label growing out of it, not a separate chip: same style,
        // same gradient, so the two read as one element. Untitled chapters expand to nothing —
        // their fallback label is the numeral already sitting next to it.
        AnimatedVisibility(
            visible = isActive && title.isNotBlank(),
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End),
        ) {
            Text(
                title,
                style = activeStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .widthIn(max = TitleMaxWidth)
                        .padding(end = 6.dp),
            )
        }

        Box(
            Modifier.width(RailWidth),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                number.toRoman(),
                style = if (isActive) activeStyle else idleStyle,
            )
        }
    }
}
