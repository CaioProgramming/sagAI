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
