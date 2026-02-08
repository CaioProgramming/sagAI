package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.components.buildWikiAndCharactersAnnotation
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.animations.LevitatingText
import com.ilustris.sagai.ui.animations.ThinkingText
import com.ilustris.sagai.ui.components.NarratorBox
import com.ilustris.sagai.ui.theme.RichTextParser
import com.ilustris.sagai.ui.theme.TextSegment

/**
 * Main composable for rendering expressive messages with inline tag-based formatting
 *
 * This composable parses rich text and renders mixed narrative elements:
 * - Plain dialogue text (with character/wiki annotations)
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
 * @param mainCharacter The main character for annotation styling
 * @param characters List of characters for annotation highlighting
 * @param wiki List of wiki items for annotation highlighting
 * @param shouldAnimate Whether to enable animations (only true for last message)
 * @param onAnnotationClick Callback when a character or wiki annotation is clicked
 * @param onThinkRevealed Callback when a thought is revealed
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpressiveText(
    text: String,
    genre: Genre,
    style: TextStyle,
    modifier: Modifier = Modifier,
    mainCharacter: Character? = null,
    characters: List<Character> = emptyList(),
    wiki: List<Wiki> = emptyList(),
    shouldAnimate: Boolean = false,
    onAnnotationClick: (Any?) -> Unit = {},
    onThinkRevealed: () -> Unit = {},
) {
    val parsedMessage =
        remember(text) {
            RichTextParser.parse(text)
        }

    val annotationBackgroundColor = MaterialTheme.colorScheme.background

    if (parsedMessage.segments.isEmpty()) {
        AnnotatedPlainText(
            text = text,
            genre = genre,
            style = style,
            mainCharacter = mainCharacter,
            characters = characters,
            wiki = wiki,
            annotationBackgroundColor = annotationBackgroundColor,
            onAnnotationClick = onAnnotationClick,
            modifier = modifier,
        )
        return
    }

    // Render segments in a flowing layout
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        parsedMessage.segments.forEach { segment ->
            when (segment) {
                is TextSegment.Plain -> {
                    AnnotatedPlainText(
                        text = segment.text,
                        genre = genre,
                        style = style,
                        mainCharacter = mainCharacter,
                        characters = characters,
                        wiki = wiki,
                        annotationBackgroundColor = annotationBackgroundColor,
                        onAnnotationClick = onAnnotationClick,
                    )
                }

                is TextSegment.Action -> {
                    LevitatingText(
                        text = segment.text,
                        style = style,
                        animate = shouldAnimate,
                    )
                }

                is TextSegment.Think -> {
                    ThinkingText(
                        text = segment.text,
                        style = style,
                        genre = genre,
                        animate = shouldAnimate,
                        onRevealed = onThinkRevealed,
                    )
                }

                is TextSegment.Narrator -> {
                    NarratorBox(
                        text = segment.text,
                        style = style,
                        genre = genre,
                    )
                }
            }
        }
    }
}

/**
 * Plain text with character and wiki annotations that are clickable
 */
@Composable
private fun AnnotatedPlainText(
    text: String,
    genre: Genre,
    style: TextStyle,
    mainCharacter: Character?,
    characters: List<Character>,
    wiki: List<Wiki>,
    annotationBackgroundColor: Color,
    onAnnotationClick: (Any?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val annotatedText =
        remember(text, characters, wiki) {
            buildWikiAndCharactersAnnotation(
                text = text,
                genre = genre,
                mainCharacter = mainCharacter,
                characters = characters,
                wiki = wiki,
                shadowColor = annotationBackgroundColor,
            )
        }

    if (characters.isEmpty() && wiki.isEmpty()) {
        Text(
            text = text,
            style = style,
            modifier = modifier,
        )
    } else {
        ClickableText(
            text = annotatedText,
            style = style,
            modifier = modifier,
            onClick = { offset ->
                // Check for character annotation
                val characterAnnotation =
                    annotatedText
                        .getStringAnnotations(tag = "character_tag", start = offset, end = offset)
                        .firstOrNull()

                // Check for wiki annotation
                val wikiAnnotation =
                    annotatedText
                        .getStringAnnotations(tag = "wiki_tag", start = offset, end = offset)
                        .firstOrNull()

                characterAnnotation?.let { annotation ->
                    val characterId = annotation.item.split(":").lastOrNull()
                    val character = characters.find { it.id.toString() == characterId }
                    onAnnotationClick(character)
                }

                wikiAnnotation?.let { annotation ->
                    val wikiId = annotation.item.split(":").lastOrNull()
                    val wikiData = wiki.find { it.id.toString() == wikiId }
                    onAnnotationClick(wikiData)
                }
            },
        )
    }
}
