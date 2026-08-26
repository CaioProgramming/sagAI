package com.ilustris.sagai.ui.genre.recap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * One counted fact about a finished saga, kept as value and label rather than as one pre-formatted
 * sentence.
 *
 * The split is what makes a genre treatment possible at all: Terminal wants `[+] 11 personagens`
 * aligned in a column of output, Collage wants a big **11** on a sticker with the label small
 * underneath, and the unstyled fallback wants the plain sentence. One baked string could only ever
 * serve the last of those.
 */
@Immutable
data class RecapStat(
    val value: String,
    val label: String,
) {
    /** The plain sentence the unstyled card has always shown. */
    val sentence: String get() = "$value $label"
}

/** Non-null while the recap is still being generated — [message] is already localized. */
@Immutable
data class RecapProgress(
    val completed: Int,
    val total: Int,
    val message: String,
)

/**
 * A finished saga's recap, described rather than laid out — the same split
 * [com.ilustris.sagai.ui.genre.surface.StoryBeat] makes for a milestone beat, at the much smaller
 * scale a card needs.
 *
 * The point is that the *rotation* and the *generating* state live here once, in
 * [rememberRecapHeadline], instead of each genre re-deriving them. Without that, giving six genres
 * their own card would mean six copies of a 2.5-second timer and six copies of "what do I show
 * while the review is still being written".
 *
 * Resolves no string resources: every label arrives localized, so this package stays usable from
 * anywhere without owning a string namespace.
 */
@Immutable
data class RecapCard(
    val title: String,
    val stats: List<RecapStat>,
    /** The nudge shown after the stats have cycled once — "Revisite sua saga agora". */
    val callToAction: String,
    val progress: RecapProgress? = null,
    val onClick: () -> Unit = {},
) {
    val isReady: Boolean get() = progress == null
}

/** How long each stat holds before the card rotates to the next one. */
private const val ROTATION_DELAY_MS = 2500L

/**
 * The single line a compact card shows right now: the generation status while the recap is still
 * being written, otherwise each stat in turn and then the call to action.
 *
 * Surfaces with room for the whole list (Terminal's transcript, Collage's poster) read
 * [RecapCard.stats] directly instead and ignore this.
 */
@Composable
fun rememberRecapHeadline(card: RecapCard): String {
    val lines = remember(card.stats, card.callToAction) { card.stats.map { it.sentence } + card.callToAction }
    var index by remember(lines) { mutableIntStateOf(0) }

    LaunchedEffect(card.isReady, lines) {
        if (!card.isReady) return@LaunchedEffect
        while (true) {
            delay(ROTATION_DELAY_MS)
            index = (index + 1) % lines.size
        }
    }

    return card.progress?.message ?: lines[index.coerceIn(lines.indices)]
}
