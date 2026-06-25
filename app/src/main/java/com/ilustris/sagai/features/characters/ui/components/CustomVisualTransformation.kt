package com.ilustris.sagai.features.characters.ui.components
import ai.atick.material.MaterialColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.saga.chat.ui.components.ExpressiveTag
import com.ilustris.sagai.features.wiki.data.model.Wiki
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
    mainCharacter: Character?,
    characters: List<Character>,
    wiki: List<Wiki>,
    text: String,
    genreColor: Color,
    tagBackgroundColor: Color = MaterialColor.Gray500,
    textColor: Color = Color.Unspecified,
    headerFont: FontFamily?,
    bodyFont: FontFamily?,
    tagMarkerLabels: Map<String, String> = emptyMap(),
    thinkTagSurfaceColor: Color? = null,
    annotateMentions: Boolean = true,
): TransformedText {
    val (transformedText, offsetMapping) =
        transformExpressiveTags(
            text,
            tagBackgroundColor,
            textColor,
            tagMarkerLabels,
            thinkTagSurfaceColor,
        )

    val annotatedString =
        if (annotateMentions) {
            try {
                buildWikiAndCharactersAnnotationOnTransformed(
                    transformedText,
                    mainCharacter,
                    characters,
                    wiki,
                    genreColor,
                    tagBackgroundColor,
                    headerFont,
                    bodyFont,
                )
            } catch (e: Exception) {
                e.printStackTrace()
                transformedText
            }
        } else {
            transformedText
        }

    return TransformedText(annotatedString, offsetMapping)
}

/**
 * Transform text by replacing tag markup with visible opening markers and styled content.
 * Returns the transformed AnnotatedString and an OffsetMapping for cursor positioning.
 */
private fun findNextExpressiveTagOpen(
    text: String,
    fromIndex: Int,
): Int? {
    val legacyOpen = "<" + "think" + ">"
    val openings =
        ExpressiveTag.entries.map { it.openingTag() } + legacyOpen
    return openings
        .mapNotNull { tag -> text.indexOf(tag, fromIndex).takeIf { it != -1 } }
        .minOrNull()
}

private fun transformExpressiveTags(
    text: String,
    tagBackgroundColor: Color,
    textColor: Color,
    tagMarkerLabels: Map<String, String>,
    thinkTagSurfaceColor: Color?,
): Pair<AnnotatedString, OffsetMapping> {
    val actionRegex = Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL)
    val thinkRegex = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
    val legacyThinkRegex =
        Regex("<" + "think" + ">(.*?)</" + "think" + ">", RegexOption.DOT_MATCHES_ALL)
    val narratorRegex = Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL)

    data class TagMatch(
        val range: IntRange,
        val content: String,
        val type: String,
        val openTagLength: Int,
        val closeTagLength: Int,
        val markerLabel: String,
    )

    val matches = mutableListOf<TagMatch>()

    fun markerFor(type: String): String = tagMarkerLabels[type] ?: type.replaceFirstChar { it.uppercase() }

    actionRegex.findAll(text).forEach { match ->
        matches.add(
            TagMatch(
                range = match.range,
                content = match.groupValues[1],
                type = "action",
                openTagLength = "<action>".length,
                closeTagLength = "</action>".length,
                markerLabel = markerFor("action"),
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
                markerLabel = markerFor("think"),
            ),
        )
    }

    legacyThinkRegex.findAll(text).forEach { match ->
        if (matches.none { it.range == match.range }) {
            matches.add(
                TagMatch(
                    range = match.range,
                    content = match.groupValues[1],
                    type = "think",
                    openTagLength = ("<" + "think" + ">").length,
                    closeTagLength = ("</" + "think" + ">").length,
                    markerLabel = markerFor("think"),
                ),
            )
        }
    }

    narratorRegex.findAll(text).forEach { match ->
        matches.add(
            TagMatch(
                range = match.range,
                content = match.groupValues[1],
                type = "narrator",
                openTagLength = "<narrator>".length,
                closeTagLength = "</narrator>".length,
                markerLabel = markerFor("narrator"),
            ),
        )
    }

    ExpressiveTag.entries.forEach { expressiveTag ->
        val openTag = expressiveTag.openingTag()
        val closeTag = expressiveTag.closingTag()
        val type =
            when (expressiveTag) {
                ExpressiveTag.ACTION -> "action"
                ExpressiveTag.THINK -> "think"
                ExpressiveTag.NARRATOR -> "narrator"
            }
        var searchFrom = 0
        while (true) {
            val openIndex = text.indexOf(openTag, searchFrom)
            if (openIndex == -1) break

            val contentStart = openIndex + openTag.length
            val closeIndex = text.indexOf(closeTag, contentStart)
            val alreadyMatched = matches.any { it.range.first == openIndex }
            if (!alreadyMatched && closeIndex == -1) {
                val contentEnd = findNextExpressiveTagOpen(text, contentStart) ?: text.length
                matches.add(
                    TagMatch(
                        range = openIndex until contentEnd,
                        content = text.substring(contentStart, contentEnd),
                        type = type,
                        openTagLength = openTag.length,
                        closeTagLength = 0,
                        markerLabel = markerFor(type),
                    ),
                )
            }

            searchFrom = openIndex + openTag.length
        }
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

    val thinkSurface =
        thinkTagSurfaceColor
            ?: tagBackgroundColor.copy(alpha = 0.4f)

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

                val markerText = "〔${match.markerLabel}〕 "
                val markerAccentColor =
                    when (match.type) {
                        "action" -> MaterialColor.Amber400
                        "think" ->
                            if (textColor != Color.Unspecified) {
                                textColor.copy(alpha = 0.85f)
                            } else {
                                Color.LightGray
                            }
                        else -> tagBackgroundColor
                    }

                val markerBackground =
                    when (match.type) {
                        "action" -> MaterialColor.Amber400.copy(alpha = 0.12f)
                        "think" -> thinkSurface
                        else -> tagBackgroundColor.copy(alpha = 0.12f)
                    }

                val markerStartInTransformed = transformedBuilder.length
                repeat(match.openTagLength) {
                    originalToTransformed.add(markerStartInTransformed)
                }

                val markerStyleStart = length
                append(markerText)
                val markerStyleEnd = length
                addStyle(
                    SpanStyle(
                        color = markerAccentColor,
                        fontWeight = FontWeight.Medium,
                        background = markerBackground,
                    ),
                    markerStyleStart,
                    markerStyleEnd,
                )
                transformedBuilder.append(markerText)

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
                                        background = MaterialColor.Amber400.copy(alpha = 0.08f),
                                    ),
                                    startStyle,
                                    endStyle,
                                )
                            }

                            "think" -> {
                                val thinkContentColor =
                                    if (textColor != Color.Unspecified) {
                                        textColor
                                    } else {
                                        Color.White.copy(alpha = 0.9f)
                                    }
                                addStyle(
                                    SpanStyle(
                                        color = thinkContentColor,
                                        fontWeight = FontWeight.Medium,
                                        background = thinkSurface,
                                    ),
                                    startStyle,
                                    endStyle,
                                )
                            }

                            "narrator" -> {
                                addStyle(
                                    SpanStyle(
                                        fontStyle = FontStyle.Italic,
                                        background = tagBackgroundColor.copy(alpha = 0.12f),
                                    ),
                                    startStyle,
                                    endStyle,
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                for (char in match.content) {
                    originalToTransformed.add(transformedBuilder.length)
                    transformedBuilder.append(char)
                }

                val contentEndInTransformed = transformedBuilder.length
                if (match.closeTagLength > 0) {
                    repeat(match.closeTagLength) {
                        originalToTransformed.add(contentEndInTransformed)
                    }
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

    val offsetMapping =
        object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                when {
                    offset <= 0 -> 0
                    offset >= originalToTransformed.size -> finalTransformedText.length
                    else -> originalToTransformed[offset]
                }

            override fun transformedToOriginal(offset: Int): Int =
                when {
                    offset <= 0 -> {
                        0
                    }

                    offset >= finalTransformedText.length -> {
                        text.length
                    }

                    else -> {
                        findOriginalOffsetForTransformed(
                            offset,
                        originalToTransformed,
                        text.length,
                        )
                    }
                }
        }

    return Pair(annotatedString, offsetMapping)
}

private fun findOriginalOffsetForTransformed(
    transformedOffset: Int,
    originalToTransformed: List<Int>,
    originalLength: Int,
): Int {
    var low = 0
    var high = originalToTransformed.size - 1
    while (low < high) {
        val mid = (low + high + 1) / 2
        if (originalToTransformed[mid] <= transformedOffset) {
            low = mid
        } else {
            high = mid - 1
        }
    }
    return low.coerceIn(0, originalLength)
}

/**
 * Build wiki and character annotations on already-transformed text
 * This is used when the text has already been transformed (tags removed)
 */
private fun buildWikiAndCharactersAnnotationOnTransformed(
    transformedAnnotatedString: AnnotatedString,
    mainCharacter: Character?,
    characters: List<Character>,
    wiki: List<Wiki>,
    genreColor: Color,
    shadowColor: Color,
    headerFont: FontFamily?,
    bodyFont: FontFamily?,
): AnnotatedString {
    val text = transformedAnnotatedString.text
    if (text.isEmpty()) return transformedAnnotatedString

    val charRules =
        charactersStyleRules(
            mainCharacter,
            characters,
            genreColor,
            shadowColor,
            headerFont,
            bodyFont,
        )

    val wikiRules =
        wiki.map { wikiItem ->
            AnnotationRule(
                searchTerm = wikiItem.title,
                annotationValue = "wiki:${wikiItem.id}",
                spanStyle = SpanStyle(fontStyle = FontStyle.Italic),
            )
        }

    val allRules =
        (charRules + wikiRules)
            .filter { it.searchTerm.length >= 3 }
            .sortedByDescending { it.searchTerm.length }

    if (allRules.isEmpty()) return transformedAnnotatedString

    val patternString = allRules.joinToString("|") { Regex.escape(it.searchTerm) }
    val regex = Regex("\\b($patternString)\\b", RegexOption.IGNORE_CASE)
    val rulesMap = allRules.associateBy { it.searchTerm.lowercase() }

    // Build on top of existing annotated string
    return buildAnnotatedString {
        append(transformedAnnotatedString)

        regex.findAll(text).forEach { matchResult ->
            val matchedText = matchResult.value
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last + 1

            // Find the rule that matches
            val rule = rulesMap[matchedText.lowercase()]

            if (rule != null && startIndex >= 0 && endIndex <= length) {
                try {
                    addStyle(
                        style = rule.spanStyle,
                        start = startIndex,
                        end = endIndex,
                    )

                    val tag =
                        if (rule.annotationValue.startsWith("character:")) "character_tag" else "wiki_tag"
                    addStringAnnotation(
                        tag = tag,
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

fun buildCharactersAnnotatedString(
    text: String,
    mainCharacter: Character?,
    characters: List<Character>,
    genreColor: Color,
    headerFont: FontFamily?,
    bodyFont: FontFamily?,
) = buildAnnotatedString {
    val annotationRules =
        charactersStyleRules(
            mainCharacter,
            characters,
            genreColor,
            Color.Black,
            headerFont,
            bodyFont,
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
    genreColor: Color,
    shadowColor: Color = Color.Black,
    headerFont: FontFamily?,
    bodyFont: FontFamily?,
): List<AnnotationRule> {
    val shadow =
        Shadow(
            color = shadowColor,
            blurRadius = 2f,
            offset = Offset(.5f, .3f),
        )

    return characters.flatMap { character ->
        val characterColor = character.hexColor.hexToColor() ?: genreColor.lighter(.3f)
        val font = if (character.id == mainCharacter?.id) headerFont else bodyFont
        val span =
            SpanStyle(
                fontWeight = FontWeight.Normal,
                shadow = shadow,
                color = characterColor,
                fontFamily = font,
            )

        val nameVariations = mutableListOf<String>()
        nameVariations.add(character.name)
        character.lastName?.let { nameVariations.add(it) }
        character.nicknames?.let { nameVariations.addAll(it) }

        nameVariations
            .filter { it.length >= 3 }
            .distinct()
            .map { name ->
                AnnotationRule(
                    searchTerm = name,
                    annotationValue = "character:${character.id}",
                    spanStyle = span,
                )
            }
    }
}

fun buildWikiAndCharactersAnnotation(
    text: String,
    mainCharacter: Character?,
    characters: List<Character>,
    wiki: List<Wiki>,
    genreColor: Color,
    shadowColor: Color = Color.Black,
    headerFont: FontFamily?,
    bodyFont: FontFamily?,
): AnnotatedString {
    val charRules =
        charactersStyleRules(
            mainCharacter,
            characters,
            genreColor,
            shadowColor,
            headerFont,
            bodyFont,
        )

    val wikiRules =
        wiki.map { wikiItem ->
            AnnotationRule(
                searchTerm = wikiItem.title,
                annotationValue = "wiki:${wikiItem.id}",
                spanStyle = SpanStyle(fontStyle = FontStyle.Italic),
            )
        }

    val allRules =
        (charRules + wikiRules)
            .filter { it.searchTerm.length >= 3 }
            .sortedByDescending { it.searchTerm.length }

    if (allRules.isEmpty()) return AnnotatedString(text)

    val patternString = allRules.joinToString("|") { Regex.escape(it.searchTerm) }
    val regex = Regex("\\b($patternString)\\b", RegexOption.IGNORE_CASE)
    val rulesMap = allRules.associateBy { it.searchTerm.lowercase() }

    val baseAnnotation =
        buildAnnotatedString {
            append(text)
            regex.findAll(text).forEach { matchResult ->
                val matchedText = matchResult.value
                val startIndex = matchResult.range.first
                val endIndex = matchResult.range.last + 1
                val rule = rulesMap[matchedText.lowercase()]

                if (rule != null) {
                    addStyle(rule.spanStyle, startIndex, endIndex)
                    val tag =
                        if (rule.annotationValue.startsWith("character:")) "character_tag" else "wiki_tag"
                    addStringAnnotation(tag, rule.annotationValue, startIndex, endIndex)
                }
            }
        }

    // Apply expressive tag styling on top
    return applyExpressiveTagStyling(baseAnnotation)
}

/**
 * Apply expressive tag styling to input field text
 */
private fun applyExpressiveTagStyling(annotatedString: AnnotatedString): AnnotatedString {
    val text = annotatedString.text

    return buildAnnotatedString {
        append(annotatedString)

        val actionRegex = Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL)
        val thinkRegex = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
        val narratorRegex = Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL)

        actionRegex.findAll(text).forEach { match ->
            val contentStart = match.range.first + "<action>".length
            val contentEnd = match.range.last - "</action>".length + 1
            if (contentStart >= 0 && contentEnd <= text.length && contentStart < contentEnd) {
                addStyle(
                    SpanStyle(color = MaterialColor.Amber400, fontWeight = FontWeight.Bold),
                    contentStart,
                    contentEnd,
                )
            }
        }

        thinkRegex.findAll(text).forEach { match ->
            val contentStart = match.range.first + "<think>".length
            val contentEnd = match.range.last - "</think>".length + 1
            if (contentStart >= 0 && contentEnd <= text.length && contentStart < contentEnd) {
                addStyle(SpanStyle(color = Color.Gray), contentStart, contentEnd)
            }
        }

        narratorRegex.findAll(text).forEach { match ->
            val contentStart = match.range.first + "<narrator>".length
            val contentEnd = match.range.last - "</narrator>".length + 1
            if (contentStart >= 0 && contentEnd <= text.length && contentStart < contentEnd) {
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), contentStart, contentEnd)
            }
        }
    }
}

fun buildSuggestionAnnotatedString(text: String): AnnotatedString {
    val thinkRegex = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
    var cleanText = text.replace(thinkRegex, "")

    val actionRegex = Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL)
    val narratorRegex = Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL)

    data class StyledSection(
        val start: Int,
        val end: Int,
        val style: SpanStyle,
    )
    val styledSections = mutableListOf<StyledSection>()

    val allActionMatches = actionRegex.findAll(cleanText).toList()
    var offset = 0
    allActionMatches.forEach { match ->
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
    cleanText = cleanText.replace(actionRegex, "$1")

    val allNarratorMatches = narratorRegex.findAll(cleanText).toList()
    offset = 0
    allNarratorMatches.forEach { match ->
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
    cleanText = cleanText.replace(narratorRegex, "$1")

    return buildAnnotatedString {
        append(cleanText)
        styledSections.forEach { section ->
            if (section.start >= 0 && section.end <= length) {
                addStyle(section.style, section.start, section.end)
            }
        }
    }
}

fun buildMessagePreviewAnnotatedString(text: String): AnnotatedString? =
    try {
        val thinkRegex = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
        var cleanText = text.replace(thinkRegex, "")

        val actionRegex = Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL)
        val narratorRegex = Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL)

        data class StyledSection(
            val start: Int,
            val end: Int,
            val style: SpanStyle,
        )
        val styledSections = mutableListOf<StyledSection>()

        val allActionMatches = actionRegex.findAll(cleanText).toList()
        var offset = 0
        allActionMatches.forEach { match ->
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
        cleanText = cleanText.replace(actionRegex, "$1")

        val allNarratorMatches = narratorRegex.findAll(cleanText).toList()
        offset = 0
        allNarratorMatches.forEach { match ->
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
        cleanText = cleanText.replace(narratorRegex, "$1")

        buildAnnotatedString {
            append(cleanText)
            styledSections.forEach { section ->
                if (section.start >= 0 && section.end <= length) {
                    addStyle(section.style, section.start, section.end)
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
        if (group.rules.isEmpty()) return@forEach
        val patternString = group.rules.joinToString("|") { Regex.escape(it.searchTerm) }
        val regex = Regex("\\b($patternString)\\b", RegexOption.IGNORE_CASE)
        val rulesMap = group.rules.associateBy { it.searchTerm.lowercase() }

        regex.findAll(text).forEach { matchResult ->
            val matchedText = matchResult.value
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last + 1
            val rule = rulesMap[matchedText.lowercase()]

            if (rule != null) {
                addStyle(rule.spanStyle, startIndex, endIndex)
                addStringAnnotation(group.tag, rule.annotationValue, startIndex, endIndex)
            }
        }
    }
}
