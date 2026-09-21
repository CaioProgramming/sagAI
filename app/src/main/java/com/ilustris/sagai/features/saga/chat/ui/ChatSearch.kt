package com.ilustris.sagai.features.saga.chat.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.ilustris.sagai.features.saga.chat.ui.components.stripExpressiveTags
import java.text.Normalizer

/** A message that matches the current query, with where it sits in the chat list plan. */
data class ChatSearchMatch(
    val planIndex: Int,
    val messageId: Int,
)

/**
 * The term being searched, read far down the tree by the text renderers so they can highlight it
 * without every composable between here and the bubble having to forward it.
 */
val LocalChatSearchTerm = compositionLocalOf { "" }

/**
 * Folds one character to its unaccented lowercase base — "é" to "e", "Ç" to "c".
 *
 * Deliberately one character in, one character out: stripping the combining marks off a whole
 * NFD-normalized string shortens it, and the highlight needs indices that still line up with the
 * text being styled.
 */
private fun Char.foldForSearch(): Char =
    Normalizer
        .normalize(this.toString(), Normalizer.Form.NFD)
        .first()
        .lowercaseChar()

/** Case- and accent-insensitive form, so "cafe" finds "café" — the app's stories are in pt-BR. */
internal fun String.searchNormalized(): String = buildString(length) { this@searchNormalized.forEach { append(it.foldForSearch()) } }

/**
 * Styles every occurrence of [term] in an already-annotated message, on top of the character and
 * wiki annotations it carries. Matching folds accents like the search itself, so a hit found by
 * typing "cafe" is also the one lit up in the bubble.
 */
fun AnnotatedString.withSearchHighlight(
    term: String,
    style: SpanStyle,
): AnnotatedString {
    val needle = term.trim().searchNormalized()
    if (needle.isEmpty()) return this

    val haystack = text.searchNormalized()
    var index = haystack.indexOf(needle)
    if (index < 0) return this

    return buildAnnotatedString {
        append(this@withSearchHighlight)
        while (index >= 0) {
            addStyle(style, index, index + needle.length)
            index = haystack.indexOf(needle, index + needle.length)
        }
    }
}

/**
 * Messages matching [query], in plan order — newest first, which is the order the search stepper
 * walks when you ask for the next result.
 *
 * Matches the message as the reader sees it (tag markers stripped, the words inside `<action>`,
 * `<narrator>` and `<think>` kept — a thought is still story text, even while collapsed) and the
 * speaker's name, so searching a character brings up their lines and not only mentions of them.
 */
fun List<ChatEntry>.findMatches(query: String): List<ChatSearchMatch> {
    val needle = query.trim().searchNormalized()
    if (needle.isEmpty()) return emptyList()

    return mapIndexedNotNull { index, entry ->
        val message = (entry as? ChatEntry.Message)?.content?.message ?: return@mapIndexedNotNull null
        val matches =
            stripExpressiveTags(message.text).searchNormalized().contains(needle) ||
                message.speakerName?.searchNormalized()?.contains(needle) == true
        if (matches) ChatSearchMatch(planIndex = index, messageId = message.id) else null
    }
}
