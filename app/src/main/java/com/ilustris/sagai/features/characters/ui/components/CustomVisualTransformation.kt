package com.ilustris.sagai.features.characters.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.core.graphics.toColorInt
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.wiki.data.model.Wiki
import kotlin.text.indexOf

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
    characters: List<Character>,
    wiki: List<Wiki>,
    text: String,
): TransformedText {
    val annotatedString = buildWikiAndCharactersAnnotation(text, characters, wiki)
    return TransformedText(annotatedString, OffsetMapping.Identity)
}

fun buildCharactersAnnotatedString(
    text: String,
    characters: List<Character>,
) = buildAnnotatedString {
    append(text)
    characters.forEach { character ->
        val characterName = character.name
        val characterColor = Color(character.hexColor.toColorInt())
        var startIndex = text.indexOf(characterName)
        while (startIndex != -1) {
            val endIndex = startIndex + characterName.length
            addStyle(
                style =
                    SpanStyle(
                        color = characterColor,
                        fontWeight = FontWeight.Bold,
                        background = characterColor.copy(alpha = .2f),
                    ),
                start = startIndex,
                end = endIndex,
            )
            addStringAnnotation(
                tag = "character_tag",
                annotation = character.id.toString(),
                start = startIndex,
                end = endIndex,
            )
            startIndex = text.indexOf(characterName, startIndex + 1)
        }
    }
}

fun buildWikiAndCharactersAnnotation(
    text: String,
    characters: List<Character>,
    wiki: List<Wiki>,
): AnnotatedString {
    val characterRules =
        characters.map { character ->
            val characterColor =
                try {
                    Color(character.hexColor.toColorInt())
                } catch (e: Exception) {
                    Color.Gray
                }
            AnnotationRule(
                searchTerm = character.name,
                annotationValue = "character:${character.id}",
                spanStyle =
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = characterColor,
                        background = characterColor.copy(alpha = .3f),
                    ),
            )
        }
    val characterStyleGroup =
        AnnotationStyleGroup(
            tag = "character_tag",
            rules = characterRules,
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

    return buildStyleAnnotation(
        text = text,
        styleItems = styleGroups,
    )
}

fun buildStyleAnnotation(
    text: String,
    styleItems: List<AnnotationStyleGroup>,
) = buildAnnotatedString {
    append(text)

    styleItems.forEach { group ->
        group.rules.forEach { rule ->
            var startIndex = text.indexOf(rule.searchTerm)
            while (startIndex != -1) {
                val endIndex = startIndex + rule.searchTerm.length
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
                startIndex = text.indexOf(rule.searchTerm, startIndex + 1)
            }
        }
    }
}
