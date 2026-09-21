package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R

private val ControlSize = 32.dp

/**
 * In-place search over the open saga's messages: a field plus a stepper, no result list — you walk
 * the hits with the arrows and the chat scrolls under you, so the reading surface stays the result.
 *
 * Everything except the close button lives inside the field's own container, which keeps the row
 * reading as one search box rather than a strip of loose controls.
 *
 * @param currentMatch 1-based position of the highlighted hit; 0 when there are none.
 * @param onOlder walks toward the start of the story, [onNewer] back toward the live end.
 */
@Composable
fun ChatSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    matchCount: Int,
    currentMatch: Int,
    recentSearches: List<String>,
    onRecentSelected: (String) -> Unit,
    onOlder: () -> Unit,
    onNewer: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialTheme.shapes.large,
                    ).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onClose, modifier = Modifier.size(ControlSize)) {
                Icon(
                    painterResource(R.drawable.round_close_24),
                    contentDescription = stringResource(R.string.chat_search_close),
                    modifier = Modifier.size(20.dp),
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier =
                    Modifier
                        .weight(1f)
                        .height(ControlSize)
                        .focusRequester(focusRequester),
                decorationBox = { field ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    stringResource(R.string.chat_search_placeholder),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color =
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .4f),
                                )
                            }
                            field()
                        }
                    }
                },
            )

            if (query.isNotBlank()) {
                // One drawable for both directions — the "newer" arrow is the same glyph
                // turned over, so the pair can never drift apart visually.
                IconButton(
                    onClick = onOlder,
                    enabled = matchCount > 0,
                    modifier = Modifier.size(ControlSize),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_arrow_up),
                        contentDescription = stringResource(R.string.chat_search_older),
                        modifier = Modifier.size(12.dp),
                    )
                }
                IconButton(
                    onClick = onNewer,
                    enabled = matchCount > 0,
                    modifier = Modifier.size(ControlSize),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_arrow_up),
                        contentDescription = stringResource(R.string.chat_search_newer),
                        modifier =
                            Modifier
                                .size(12.dp)
                                .rotate(180f),
                    )
                }

                Text(
                    text =
                        if (matchCount == 0) {
                            stringResource(R.string.chat_search_no_results)
                        } else {
                            stringResource(R.string.chat_search_counter, currentMatch, matchCount)
                        },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f),
                )
            }

            Icon(
                painterResource(R.drawable.search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f),
                modifier = Modifier.size(18.dp),
            )
        }

        if (query.isBlank() && recentSearches.isNotEmpty()) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                recentSearches.forEach { term ->
                    SuggestionChip(
                        onClick = { onRecentSelected(term) },
                        label = { Text(term, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
        }
    }
}
