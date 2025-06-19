package com.ilustris.sagai.features.characters.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.core.graphics.toColorInt
import com.ilustris.sagai.features.characters.data.model.Character

fun transformTextWithCharacters(
    characters: List<Character>,
    text: String,
): TransformedText {
    val annotatedString = buildCharactersAnnotatedString(text, characters)
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
