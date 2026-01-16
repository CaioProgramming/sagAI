package com.ilustris.sagai.features.characters.ui.components

import ai.atick.material.MaterialColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.lighter

data class AnnotationRule(
    val searchTerm: String,
    val annotationValue: String,
    val spanStyle: SpanStyle,
)

data class AnnotationStyleGroup(
    val tag: String,
    val rules: List<AnnotationRule>,
)

fun transformTextWithContent(
    genre: Genre,
    mainCharacter: Character?,
    characters: List<Character>,
    wiki: List<Wiki>,
    text: String,
    tagBackgroundColor: Color = MaterialColor.Gray500,
    textColor: Color = Color.Unspecified,
): TransformedText {
    // First, transform the text to hide tags and get the mapping
    val (transformedText, offsetMapping) =
        transformExpressiveTags(
            text,
            genre,
            tagBackgroundColor,
            textColor,
        )

    // Then apply character/wiki annotations on the transformed text
    val annotatedString =
        buildWikiAndCharactersAnnotationOnTransformed(
            transformedText,
            genre,
            mainCharacter,
            characters,
            wiki,
            tagBackgroundColor,
        )

    return TransformedText(annotatedString, offsetMapping)
}

/**
 * Transform text by removing tag markers and applying styles to content
 * Returns the transformed AnnotatedString and an OffsetMapping for cursor positioning
 */
private fun transformExpressiveTags(
    text: String,
    genre: Genre,
    tagBackgroundColor: Color,
    textColor: Color,
): Pair<AnnotatedString, OffsetMapping> {
    val actionRegex = Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL)
    val thinkRegex = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
    val narratorRegex = Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL)

    // Find all tag matches
    data class TagMatch(
        val range: IntRange,
        val content: String,
        val type: String,
        val openTagLength: Int,
        val closeTagLength: Int,
    )

    val matches = mutableListOf<TagMatch>()

    actionRegex.findAll(text).forEach { match ->
        matches.add(
            TagMatch(
                range = match.range,
                content = match.groupValues[1],
                type = "action",
                openTagLength = "<action>".length,
                closeTagLength = "</action>".length,
            ),
        )
    }

    thinkRegex.findAll(text).forEach { match ->
        matches.add(
            TagMatch(
                range = match.range,
                content = match.groupValues[1],
                type = "think",
                openTagLength = "<think>".length,
                closeTagLength = "</think>".length,
            ),
        )
    }

    narratorRegex.findAll(text).forEach { match ->
        matches.add(
            TagMatch(
                range = match.range,
                content = match.groupValues[1],
                type = "narrator",
                openTagLength = "<narrator>".length,
                closeTagLength = "</narrator>".length,
            ),
        )
    }

    // Sort by position
    matches.sortBy { it.range.first }

    // Build transformed text and offset mapping
    val transformedBuilder = StringBuilder()
    val originalToTransformed =
        mutableListOf<Int>() // originalToTransformed[originalIndex] = transformedIndex
    var currentOriginalIndex = 0

    for (match in matches) {
        // Add plain text before this match
        if (currentOriginalIndex < match.range.first) {
            val plainText = text.substring(currentOriginalIndex, match.range.first)
            for (char in plainText) {
                originalToTransformed.add(transformedBuilder.length)
                transformedBuilder.append(char)
            }
            currentOriginalIndex = match.range.first
        }

        // Skip the opening tag characters (map them to the start of content in transformed)
        val contentStartInTransformed = transformedBuilder.length
        repeat(match.openTagLength) {
            originalToTransformed.add(contentStartInTransformed)
        }

        // Add the content
        for (char in match.content) {
            originalToTransformed.add(transformedBuilder.length)
            transformedBuilder.append(char)
        }

        // Skip the closing tag characters (map them to the end of content in transformed)
        val contentEndInTransformed = transformedBuilder.length
        repeat(match.closeTagLength) {
            originalToTransformed.add(contentEndInTransformed)
        }

        currentOriginalIndex = match.range.last + 1
    }

    // Add remaining text
    if (currentOriginalIndex < text.length) {
        val remainingText = text.substring(currentOriginalIndex)
        for (char in remainingText) {
            originalToTransformed.add(transformedBuilder.length)
            transformedBuilder.append(char)
        }
    }

    // Add final position
    originalToTransformed.add(transformedBuilder.length)

    val transformedText = transformedBuilder.toString()

    // Build reverse mapping (transformed to original)
    val transformedToOriginal = IntArray(transformedText.length + 1)
    for (originalIdx in originalToTransformed.indices) {
        val transformedIdx = originalToTransformed[originalIdx]
        if (transformedIdx <= transformedText.length) {
            transformedToOriginal[transformedIdx] = originalIdx
        }
    }

    // Build annotated string with styles
    val annotatedString =
        buildAnnotatedString {
            append(transformedText)

            // Re-calculate styled ranges in transformed text
            var transformedIndex = 0
            var originalIndex = 0

            for (match in matches) {
                // Skip plain text before match
                val plainLength = match.range.first - originalIndex
                transformedIndex += plainLength
                originalIndex = match.range.first

                // Style the content in transformed text
                val contentStart = transformedIndex
                val contentEnd = transformedIndex + match.content.length

                when (match.type) {
                    "action" -> {
                        addStyle(
                            SpanStyle(
                                color = MaterialColor.Amber400,
                                fontWeight = FontWeight.Bold,
                            ),
                            contentStart,
                            contentEnd,
                        )
                    }

                    "think" -> {
                        // Use textColor if specified, otherwise use a default gray
                        val thinkColor =
                            if (textColor != Color.Unspecified) {
                                textColor.copy(alpha = 0.5f)
                            } else {
                                Color.Gray.copy(alpha = 0.5f)
                            }
                        addStyle(
                            SpanStyle(
                                color = thinkColor,
                            ),
                            contentStart,
                            contentEnd,
                        )
                    }

                    "narrator" -> {
                        addStyle(
                            SpanStyle(
                                fontStyle = FontStyle.Italic,
                            ),
                            contentStart,
                            contentEnd,
                        )
                    }
                }

                transformedIndex += match.content.length
                originalIndex = match.range.last + 1
            }
        }

    val offsetMapping =
        object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset < originalToTransformed.size) {
                    originalToTransformed[offset]
                } else {
                    transformedText.length
                }

            override fun transformedToOriginal(offset: Int): Int =
                if (offset < transformedToOriginal.size) {
                    transformedToOriginal[offset]
                } else {
                    text.length
                }
        }

    return Pair(annotatedString, offsetMapping)
}

/**
 * Build wiki and character annotations on already-transformed text
 * This is used when the text has already been transformed (tags removed)
 */
private fun buildWikiAndCharactersAnnotationOnTransformed(
    transformedAnnotatedString: AnnotatedString,
    genre: Genre,
    mainCharacter: Character?,
    characters: List<Character>,
    wiki: List<Wiki>,
    shadowColor: Color,
): AnnotatedString {
    val text = transformedAnnotatedString.text

    val characterStyleGroup =
        AnnotationStyleGroup(
            tag = "character_tag",
            rules =
                charactersStyleRules(
                    mainCharacter,
                    characters,
                    genre,
                    shadowColor,
                ),
        )

    val wikiRules =
        wiki.map { wikiItem ->
            AnnotationRule(
                searchTerm = wikiItem.title,
                annotationValue = "wiki:${wikiItem.id}",
                spanStyle =
                    SpanStyle(
                        fontStyle = FontStyle.Italic,
                    ),
            )
        }
    val wikiStyleGroup =
        AnnotationStyleGroup(
            tag = "wiki_tag",
            rules = wikiRules,
        )

    val styleGroups = listOf(characterStyleGroup, wikiStyleGroup)

    // Build on top of existing annotated string
    return buildAnnotatedString {
        append(transformedAnnotatedString)

        // Add character and wiki annotations
        styleGroups.forEach { group ->
            group.rules.forEach { rule ->
                val regex = Regex("\\b${Regex.escape(rule.searchTerm)}\\b", RegexOption.IGNORE_CASE)
                regex.findAll(text).forEach { matchResult ->
                    val startIndex = matchResult.range.first
                    val endIndex = matchResult.range.last + 1
                    addStyle(
                        style = rule.spanStyle,
                        start = startIndex,
                        end = endIndex,
                    )

                    addStringAnnotation(
                        tag = group.tag,
                        annotation = rule.annotationValue,
                        start = startIndex,
                        end = endIndex,
                    )
                }
            }
        }
    }
}

fun buildCharactersAnnotatedString(
    text: String,
    mainCharacter: Character?,
    characters: List<Character>,
    genre: Genre,
) = buildAnnotatedString {
    val annotationRules =
        charactersStyleRules(
            mainCharacter,
            characters,
            genre,
        )
    val annotationStyleGroup =
        AnnotationStyleGroup(
            tag = "character_tag",
            rules = annotationRules,
        )
    return buildStyleAnnotation(
        text,
        listOf(annotationStyleGroup),
    )
}

fun charactersStyleRules(
    mainCharacter: Character?,
    characters: List<Character>,
    genre: Genre,
    shadowColor: Color = Color.Black,
) = characters.flatMap { character ->
    val characterColor = character.hexColor.hexToColor() ?: genre.color.lighter(.3f)
    val shadow =
        Shadow(
            color = shadowColor,
            blurRadius = 2f,
            offset = Offset(.5f, .3f),
        )

    val mainColor = if (character.id == mainCharacter?.id) genre.color else characterColor
    val font = if (character.id == mainCharacter?.id) genre.headerFont() else genre.bodyFont()
    val span =
        SpanStyle(
            fontWeight = FontWeight.Normal,
            fontFamily = font,
            shadow = shadow,
            color = mainColor,
        )

    val nameVariations =
        buildList {
            add(character.name)
            character.lastName?.let {
                add(it)
            }
            character.nicknames?.let {
                addAll(it)
            }
        }

    nameVariations
        .asSequence()
        .filter { it.length >= 3 }
        .distinct()
        .map { name ->
            AnnotationRule(
                searchTerm = name,
                annotationValue = "character:${character.id}",
                spanStyle = span,
            )
        }.toList()
}

fun buildWikiAndCharactersAnnotation(
    text: String,
    genre: Genre,
    mainCharacter: Character?,
    characters: List<Character>,
    wiki: List<Wiki>,
    shadowColor: Color = Color.Black,
): AnnotatedString {
    val characterStyleGroup =
        AnnotationStyleGroup(
            tag = "character_tag",
            rules =
                charactersStyleRules(
                    mainCharacter,
                    characters,
                    genre,
                    shadowColor,
                ),
        )

    val wikiRules =
        wiki.map { wikiItem ->
            AnnotationRule(
                searchTerm = wikiItem.title,
                annotationValue = "wiki:${wikiItem.id}",
                spanStyle =
                    SpanStyle(
                        fontStyle = FontStyle.Italic,
                    ),
            )
        }
    val wikiStyleGroup =
        AnnotationStyleGroup(
            tag = "wiki_tag",
            rules = wikiRules,
        )

    val styleGroups = listOf(characterStyleGroup, wikiStyleGroup)

    val baseAnnotation =
        buildStyleAnnotation(
            text = text,
            styleItems = styleGroups,
        )

    // Apply expressive tag styling on top
    return applyExpressiveTagStyling(baseAnnotation)
}

/**
 * Apply expressive tag styling to input field text
 * Styles tag content for visual feedback while typing:
 * - <action>text</action> → Yellow/amber bold text
 * - <think>text</think> → Faded text (50% alpha)
 * - <narrator>text</narrator> → Italic text
 */
private fun applyExpressiveTagStyling(annotatedString: AnnotatedString): AnnotatedString {
    val text = annotatedString.text

    return buildAnnotatedString {
        append(annotatedString)

        // Apply expressive tag styling
        val actionRegex = Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL)
        val thinkRegex = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
        val narratorRegex = Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL)

        // Style <action> content
        actionRegex.findAll(text).forEach { match ->
            val contentStart = match.range.first + "<action>".length
            val contentEnd = match.range.last - "</action>".length + 1
            if (contentStart < contentEnd) {
                addStyle(
                    SpanStyle(
                        color = MaterialColor.Amber400,
                        fontWeight = FontWeight.Bold,
                    ),
                    contentStart,
                    contentEnd,
                )
            }
        }

        // Style <think> content
        thinkRegex.findAll(text).forEach { match ->
            val contentStart = match.range.first + "<think>".length
            val contentEnd = match.range.last - "</think>".length + 1
            if (contentStart < contentEnd) {
                // Apply semi-transparent gray color for faded appearance
                // In visual transformation we don't have access to the actual text color
                addStyle(
                    SpanStyle(
                        color = Color.Gray,
                    ),
                    contentStart,
                    contentEnd,
                )
            }
        }

        // Style <narrator> content
        narratorRegex.findAll(text).forEach { match ->
            val contentStart = match.range.first + "<narrator>".length
            val contentEnd = match.range.last - "</narrator>".length + 1
            if (contentStart < contentEnd) {
                addStyle(
                    SpanStyle(
                        fontStyle = FontStyle.Italic,
                    ),
                    contentStart,
                    contentEnd,
                )
            }
        }
    }
}

/**
 * Create styled annotated string for suggestion text with expressive tags
 * Used in suggestion chips to show preview of tag styling
 * Tags are removed from display and text is styled appropriately
 *
 * @param text Suggestion text with tags
 * @return AnnotatedString with tags removed and text styled
 */
fun buildSuggestionAnnotatedString(text: String): AnnotatedString {
    // First, remove think tags since they don't get styled
    val thinkRegex = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
    var cleanText = text.replace(thinkRegex, "")

    // Now process styled tags on the cleaned text
    val actionRegex = Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL)
    val narratorRegex = Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL)

    data class StyledSection(
        val start: Int,
        val end: Int,
        val style: SpanStyle,
    )

    val styledSections = mutableListOf<StyledSection>()

    // Find all action matches first
    val actionMatches = actionRegex.findAll(cleanText).toList()
    var offset = 0

    actionMatches.forEach { match ->
        val content = match.groupValues[1]
        val matchStart = match.range.first - offset

        styledSections.add(
            StyledSection(
                matchStart,
                matchStart + content.length,
                SpanStyle(fontWeight = FontWeight.Bold),
            ),
        )

        offset += "<action>".length + "</action>".length
    }

    // Remove all action tags
    cleanText = cleanText.replace(actionRegex, "$1")

    // Find all narrator matches
    val narratorMatches = narratorRegex.findAll(cleanText).toList()
    offset = 0

    narratorMatches.forEach { match ->
        val content = match.groupValues[1]
        val matchStart = match.range.first - offset

        styledSections.add(
            StyledSection(
                matchStart,
                matchStart + content.length,
                SpanStyle(fontStyle = FontStyle.Italic),
            ),
        )

        offset += "<narrator>".length + "</narrator>".length
    }

    // Remove all narrator tags
    cleanText = cleanText.replace(narratorRegex, "$1")

    return buildAnnotatedString {
        append(cleanText)
        styledSections.forEach { section ->
            addStyle(section.style, section.start, section.end)
        }
    }
}

/**
 * Create styled annotated string for message preview text with expressive tags
 * Used in HomeView to show simplified preview of messages with styling
 * Tags are removed from display and text is styled appropriately
 *
 * @param text Message text with tags
 * @return AnnotatedString with tags removed and text styled
 */
fun buildMessagePreviewAnnotatedString(text: String): AnnotatedString {
    // First, remove think tags since they don't get styled
    val thinkRegex = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
    var cleanText = text.replace(thinkRegex, "")

    // Now process styled tags on the cleaned text
    val actionRegex = Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL)
    val narratorRegex = Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL)

    data class StyledSection(
        val start: Int,
        val end: Int,
        val style: SpanStyle,
    )

    val styledSections = mutableListOf<StyledSection>()

    // Find all action matches first
    val actionMatches = actionRegex.findAll(cleanText).toList()
    var offset = 0

    actionMatches.forEach { match ->
        val content = match.groupValues[1]
        val matchStart = match.range.first - offset

        styledSections.add(
            StyledSection(
                matchStart,
                matchStart + content.length,
                SpanStyle(fontWeight = FontWeight.Bold),
            ),
        )

        offset += "<action>".length + "</action>".length
    }

    // Remove all action tags
    cleanText = cleanText.replace(actionRegex, "$1")

    // Find all narrator matches
    val narratorMatches = narratorRegex.findAll(cleanText).toList()
    offset = 0

    narratorMatches.forEach { match ->
        val content = match.groupValues[1]
        val matchStart = match.range.first - offset

        styledSections.add(
            StyledSection(
                matchStart,
                matchStart + content.length,
                SpanStyle(fontStyle = FontStyle.Italic),
            ),
        )

        offset += "<narrator>".length + "</narrator>".length
    }

    // Remove all narrator tags
    cleanText = cleanText.replace(narratorRegex, "$1")

    return buildAnnotatedString {
        append(cleanText)
        styledSections.forEach { section ->
            addStyle(section.style, section.start, section.end)
        }
    }
}

fun buildStyleAnnotation(
    text: String,
    styleItems: List<AnnotationStyleGroup>,
) = buildAnnotatedString {
    append(text)

    styleItems.forEach { group ->
        group.rules.forEach { rule ->
            val regex = Regex("\\b${Regex.escape(rule.searchTerm)}\\b", RegexOption.IGNORE_CASE)
            regex.findAll(text).forEach { matchResult ->
                val startIndex = matchResult.range.first
                val endIndex = matchResult.range.last + 1
                addStyle(
                    style = rule.spanStyle,
                    start = startIndex,
                    end = endIndex,
                )

                addStringAnnotation(
                    tag = group.tag,
                    annotation = rule.annotationValue,
                    start = startIndex,
                    end = endIndex,
                )
            }
        }
    }
}
