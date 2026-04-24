package com.ilustris.sagai.ui.theme.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * Converts a JSON string into an AnnotatedString with syntax highlighting.
 */
fun String.toJsonAnnotatedString(): AnnotatedString =
    buildAnnotatedString {
        val keyRegex = "\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*:".toRegex()
        val stringRegex = "\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"".toRegex()
        val numberRegex = "\\b(-?\\d+(\\.\\d+)?)\\b".toRegex()
        val booleanRegex = "\\b(true|false|null)\\b".toRegex()

        var lastIndex = 0

        // Combine all candidates and sort by match start
        val candidates =
            (
                keyRegex.findAll(this@toJsonAnnotatedString) +
                    stringRegex.findAll(this@toJsonAnnotatedString) +
                    numberRegex.findAll(this@toJsonAnnotatedString) +
                    booleanRegex.findAll(this@toJsonAnnotatedString)
            ).sortedBy { it.range.first }

        candidates.forEach { match ->
            if (match.range.first >= lastIndex) {
                // Append text before match
                append(this@toJsonAnnotatedString.substring(lastIndex, match.range.first))

                val style =
                    when {
                        keyRegex.matches(match.value) -> SpanStyle(color = Color(0xFF9CDCFE))

                        // VSCode-like Light Blue for keys
                        stringRegex.matches(match.value) -> SpanStyle(color = Color(0xFFCE9178))

                        // VSCode-like Orange/Red for strings
                        numberRegex.matches(match.value) -> SpanStyle(color = Color(0xFFB5CEA8))

                        // VSCode-like Green for numbers
                        booleanRegex.matches(match.value) -> SpanStyle(color = Color(0xFF569CD6))

                        // VSCode-like Blue for booleans/null
                        else -> SpanStyle(color = Color.White)
                    }

                withStyle(style) {
                    append(match.value)
                }
                lastIndex = match.range.last + 1
            }
        }
        append(this@toJsonAnnotatedString.substring(lastIndex))
    }
