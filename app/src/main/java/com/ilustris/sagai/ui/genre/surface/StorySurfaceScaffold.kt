package com.ilustris.sagai.ui.genre.surface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * A beat is composed in one of two very different contexts, and the difference is structural rather
 * than cosmetic.
 *
 * The Milestone screen gives a beat the whole screen: it can fill the height, scroll its own body,
 * and pin its actions to the bottom. The story review composes beats as items inside its own
 * scrolling container, where the height is unbounded — a `weight(1f)` there is not merely wrong,
 * it fails to measure at all.
 *
 * So surfaces take [embedded] and lay themselves out accordingly, rather than each one guessing
 * from whether the beat happens to carry actions.
 */
internal fun Modifier.storyRoot(embedded: Boolean): Modifier =
    if (embedded) {
        this.fillMaxWidth()
    } else {
        this.fillMaxSize().systemBarsPadding()
    }

/**
 * The scrolling middle of a full-screen beat, or a plain wrap-content column when [embedded] —
 * where the surrounding container already owns the scrolling.
 */
@Composable
internal fun ColumnScope.StoryBody(
    embedded: Boolean,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    val modifier =
        if (embedded) {
            Modifier.fillMaxWidth()
        } else {
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        }
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}
