package com.ilustris.sagai.features.characters.ui.components

import ai.atick.material.MaterialColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.core.graphics.toColorInt
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.lighter
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
    genre: Genre,
    mainCharacter: Character?,
    characters: List<Character>,
    wiki: List<Wiki>,
    text: String,
    tagBackgroundColor: Color = MaterialColor.Gray500,
): TransformedText {
    val annotatedString =
        buildWikiAndCharactersAnnotation(
            text,
            genre,
            mainCharacter,
            characters,
            wiki,
            tagBackgroundColor,
        )
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
    genre: Genre,
    mainCharacter: Character?,
    characters: List<Character>,
    wiki: List<Wiki>,
    shadowColor: Color = Color.Black,
): AnnotatedString {
    val characterRules =
        characters.map { character ->
            val characterColor = character.hexColor.hexToColor() ?: genre.color.lighter(.3f)
            val shadow =
                Shadow(
                    color = shadowColor,
                    blurRadius = 5f,
                    offset =
                        androidx.compose.ui.geometry
                            .Offset(1f, -1f),
                )
            val span =
                if (character.id == mainCharacter?.id) {
                    SpanStyle(
                        fontWeight = FontWeight.Normal,
                        fontFamily = genre.headerFont(),
                        color = genre.color,
                    )
                } else {
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontFamily = genre.bodyFont(),
                        color = characterColor,
                    )
                }
            AnnotationRule(
                searchTerm = character.name,
                annotationValue = "character:${character.id}",
                spanStyle = span,
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
