package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.animations.LevitatingText
import com.ilustris.sagai.ui.animations.ThinkingText
import com.ilustris.sagai.ui.components.NarratorBox
import com.ilustris.sagai.ui.theme.RichTextParser
import com.ilustris.sagai.ui.theme.TextSegment

/**
 * Main composable for rendering expressive messages with inline tag-based formatting
 *
 * This composable parses rich text and renders mixed narrative elements:
 * - Plain dialogue text
 * - <action> tags with levitation animation
 * - <think> tags with interactive star reveals
 * - <narrator> tags with bordered boxes
 *
 * Only the LAST message in chat should have active animations (shouldAnimate = true).
 * Old messages render as static styled text for performance.
 *
 * Example input:
 * "Hello <action>waves</action> <think>I'm nervous</think>"
 *
 * @param text Raw text with embedded tags
 * @param genre Genre for color theming
 * @param style Base text style for the message
 * @param modifier Optional modifier
 * @param shouldAnimate Whether to enable animations (only true for last message)
 * @param onThinkRevealed Callback when a thought is revealed
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpressiveText(
    text: String,
    genre: Genre,
    style: TextStyle,
    modifier: Modifier = Modifier,
    shouldAnimate: Boolean = false,
    onThinkRevealed: () -> Unit = {}
) {
    // Parse text once and cache result
    val parsedMessage = remember(text) {
        RichTextParser.parse(text)
    }

    // If parsing resulted in no segments, just show plain text
    if (parsedMessage.segments.isEmpty()) {
        Text(
            text = text,
            style = style,
            modifier = modifier
        )
        return
    }

    // Render segments in a flowing layout
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        parsedMessage.segments.forEach { segment ->
            when (segment) {
                is TextSegment.Plain -> {
                    Text(
                        text = segment.text,
                        style = style
                    )
                }

                is TextSegment.Action -> {
                    LevitatingText(
                        text = segment.text,
                        style = style,
                        animate = shouldAnimate
                    )
                }

                is TextSegment.Think -> {
                    ThinkingText(
                        text = segment.text,
                        style = style,
                        genre = genre,
                        animate = shouldAnimate,
                        onRevealed = onThinkRevealed
                    )
                }

                is TextSegment.Narrator -> {
                    NarratorBox(
                        text = segment.text,
                        style = style,
                        genre = genre
                    )
                }
            }
        }
    }
}

