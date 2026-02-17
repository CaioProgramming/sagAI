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
    genreColor: Color,
    tagBackgroundColor: Color = MaterialColor.Gray500,
    textColor: Color = Color.Unspecified,
): TransformedText {
    val (transformedText, offsetMapping) =
        transformExpressiveTags(
            text,
            genre,
            tagBackgroundColor,
            textColor,
        )

    // Then apply character/wiki annotations on the transformed text
    val annotatedString =
        try {
            buildWikiAndCharactersAnnotationOnTransformed(
                transformedText,
                genre,
                mainCharacter,
                characters,
                wiki,
                genreColor,
                tagBackgroundColor,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            transformedText
        }

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

    // Sort matches by start position and handle nesting (prefer outermost tags)
    val filteredMatches =
        matches.sortedBy { it.range.first }.let { sorted ->
            val filtered = mutableListOf<TagMatch>()
            var lastEnd = -1
            for (match in sorted) {
                if (match.range.first > lastEnd) {
                    filtered.add(match)
                    lastEnd = match.range.last
                }
            }
            filtered
        }

    // Build transformed text, offset mapping and annotated string in a single pass
    val originalToTransformed = mutableListOf<Int>()
    val transformedBuilder = StringBuilder()
    var currentOriginalIndex = 0

    val annotatedString =
        buildAnnotatedString {
            for (match in filteredMatches) {
                // Add plain text before this match
                if (currentOriginalIndex < match.range.first) {
                    val plainText = text.substring(currentOriginalIndex, match.range.first)
                    append(plainText)
                    for (char in plainText) {
                        originalToTransformed.add(transformedBuilder.length)
                        transformedBuilder.append(char)
                    }
                    currentOriginalIndex = match.range.first
                }

                // Map opening tag to the start of the content
                val contentStartInTransformed = transformedBuilder.length
                repeat(match.openTagLength) {
                    originalToTransformed.add(contentStartInTransformed)
                }

                // Append and style content
                val startStyle = length
                append(match.content)
                val endStyle = length

                if (startStyle < endStyle) {
                    try {
                        when (match.type) {
                            "action" -> {
                                addStyle(
                                    SpanStyle(
                                        color = MaterialColor.Amber400,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    startStyle,
                                    endStyle,
                                )
                            }

                            "think" -> {
                                val thinkColor =
                                    if (textColor != Color.Unspecified) {
                                        textColor.copy(alpha = 0.5f)
                                    } else {
                                        Color.Gray.copy(alpha = 0.5f)
                                    }
                                addStyle(SpanStyle(color = thinkColor), startStyle, endStyle)
                            }

                            "narrator" -> {
                                addStyle(SpanStyle(fontStyle = FontStyle.Italic), startStyle, endStyle)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Update mapping and builder for content
                for (char in match.content) {
                    originalToTransformed.add(transformedBuilder.length)
                    transformedBuilder.append(char)
                }

                // Map closing tag to the end of the content
                val contentEndInTransformed = transformedBuilder.length
                repeat(match.closeTagLength) {
                    originalToTransformed.add(contentEndInTransformed)
                }

                currentOriginalIndex = match.range.last + 1
            }

            // Add remaining text
            if (currentOriginalIndex < text.length) {
                val remainingText = text.substring(currentOriginalIndex)
                append(remainingText)
                for (char in remainingText) {
                    originalToTransformed.add(transformedBuilder.length)
                    transformedBuilder.append(char)
                }
            }

            // Final position mapping
            originalToTransformed.add(transformedBuilder.length)
        }

    val finalTransformedText = transformedBuilder.toString()
    val transformedToOriginal = IntArray(finalTransformedText.length + 1)
    for (originalIdx in originalToTransformed.indices) {
        val transformedIdx = originalToTransformed[originalIdx]
        if (transformedIdx < transformedToOriginal.size) {
            transformedToOriginal[transformedIdx] = originalIdx
        }
    }

    val offsetMapping =
        object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset >= 0 && offset < originalToTransformed.size) {
                    originalToTransformed[offset]
                } else {
                    finalTransformedText.length
                }

            override fun transformedToOriginal(offset: Int): Int =
                if (offset >= 0 && offset < transformedToOriginal.size) {
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
    genreColor: Color,
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
                    genreColor,
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

                    if (startIndex >= 0 && endIndex <= length && startIndex < endIndex) {
                        try {
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
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
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
    genreColor: Color,
) = buildAnnotatedString {
    val annotationRules =
        charactersStyleRules(
            mainCharacter,
            characters,
            genre,
            genreColor,
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
    genreColor: Color,
    shadowColor: Color = Color.Black,
) = characters.flatMap { character ->
    val characterColor = character.hexColor.hexToColor() ?: genreColor.lighter(.3f)
    val shadow =
        Shadow(
            color = shadowColor,
            blurRadius = 2f,
            offset = Offset(.5f, .3f),
        )

    val mainColor = if (character.id == mainCharacter?.id) genreColor else characterColor
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
    genreColor: Color,
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
                    genreColor,
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
            if (contentStart >= 0 && contentEnd <= text.length && contentStart < contentEnd) {
                try {
                    addStyle(
                        SpanStyle(
                            color = MaterialColor.Amber400,
                            fontWeight = FontWeight.Bold,
                        ),
                        contentStart,
                        contentEnd,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Style <think> content
        thinkRegex.findAll(text).forEach { match ->
            val contentStart = match.range.first + "<think>".length
            val contentEnd = match.range.last - "</think>".length + 1
            if (contentStart >= 0 && contentEnd <= text.length && contentStart < contentEnd) {
                // Apply semi-transparent gray color for faded appearance
                // In visual transformation we don't have access to the actual text color
                try {
                    addStyle(
                        SpanStyle(
                            color = Color.Gray,
                        ),
                        contentStart,
                        contentEnd,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Style <narrator> content
        narratorRegex.findAll(text).forEach { match ->
            val contentStart = match.range.first + "<narrator>".length
            val contentEnd = match.range.last - "</narrator>".length + 1
            if (contentStart >= 0 && contentEnd <= text.length && contentStart < contentEnd) {
                try {
                    addStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                        ),
                        contentStart,
                        contentEnd,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
    val allActionMatches = actionRegex.findAll(cleanText).toList()
    val actionMatches = mutableListOf<MatchResult>()
    var lastActionEnd = -1

    allActionMatches.sortedBy { it.range.first }.forEach { match ->
        if (match.range.first > lastActionEnd) {
            actionMatches.add(match)
            lastActionEnd = match.range.last
        }
    }

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
    val allNarratorMatches = narratorRegex.findAll(cleanText).toList()
    val narratorMatches = mutableListOf<MatchResult>()
    var lastNarratorEnd = -1

    allNarratorMatches.sortedBy { it.range.first }.forEach { match ->
        if (match.range.first > lastNarratorEnd) {
            narratorMatches.add(match)
            lastNarratorEnd = match.range.last
        }
    }

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
            if (section.start >= 0 && section.end <= length && section.start < section.end) {
                try {
                    addStyle(section.style, section.start, section.end)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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
fun buildMessagePreviewAnnotatedString(text: String): AnnotatedString? =
    try {
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
        val allActionMatches = actionRegex.findAll(cleanText).toList()
        val actionMatches = mutableListOf<MatchResult>()
        var lastActionEnd = -1

        allActionMatches.sortedBy { it.range.first }.forEach { match ->
            if (match.range.first > lastActionEnd) {
                actionMatches.add(match)
                lastActionEnd = match.range.last
            }
        }

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
        val allNarratorMatches = narratorRegex.findAll(cleanText).toList()
        val narratorMatches = mutableListOf<MatchResult>()
        var lastNarratorEnd = -1

        allNarratorMatches.sortedBy { it.range.first }.forEach { match ->
            if (match.range.first > lastNarratorEnd) {
                narratorMatches.add(match)
                lastNarratorEnd = match.range.last
            }
        }

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

        buildAnnotatedString {
            append(cleanText)
            styledSections.forEach { section ->
                if (section.start >= 0 && section.end <= length && section.start < section.end) {
                    try {
                        addStyle(section.style, section.start, section.end)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
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

                if (startIndex >= 0 && endIndex <= length && startIndex < endIndex) {
                    try {
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
