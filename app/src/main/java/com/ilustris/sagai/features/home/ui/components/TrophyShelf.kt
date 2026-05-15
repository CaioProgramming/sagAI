package com.ilustris.sagai.features.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.home.data.model.SagaSummary
import com.ilustris.sagai.features.newsaga.data.model.Genre

private val trophyRowSpacing = 20.dp
private val trophyRowVerticalPadding = 6.dp

@Composable
fun TrophyShelf(
    completedSagas: List<SagaSummary>,
    visualConfigs: Map<Genre, GenreVisualConfig>,
    onCompletedSagaClicked: (SagaSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sorted =
        remember(completedSagas) {
            completedSagas.sortedByDescending { it.data.endedAt }
        }
    if (sorted.isEmpty()) return

    val overflowCount = (sorted.size - TROPHY_MAX_VISIBLE).coerceAtLeast(0)
    val visibleSagas =
        remember(sorted, overflowCount) {
            if (overflowCount > 0) {
                sorted.take(TROPHY_MAX_VISIBLE - 1)
            } else {
                sorted.take(TROPHY_MAX_VISIBLE)
            }
        }

    val rows =
        remember(visibleSagas, overflowCount) {
            buildTrophyRows(visibleSagas, overflowCount)
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
    ) {
        TrophySectionHeader()

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(trophyRowVerticalPadding),
        ) {
            rows.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top,
                ) {
                    row.forEachIndexed { indexInRow, entry ->
                        val globalIndex = rowIndex * TROPHY_COLUMNS + indexInRow
                        val yJitter = yJitterForIndex(globalIndex)
                        when (entry) {
                            is TrophyRowEntry.Saga -> {
                                TrophyPinItem(
                                    saga = entry.saga,
                                    avatarSize = avatarSizeForIndex(globalIndex),
                                    visualConfig = visualConfigs[entry.saga.data.genre],
                                    onClick = { onCompletedSagaClicked(entry.saga) },
                                    modifier =
                                        Modifier
                                            .padding(horizontal = trophyRowSpacing / 2)
                                            .offset(y = yJitter),
                                )
                            }

                            is TrophyRowEntry.Overflow -> {
                                TrophyOverflowPinItem(
                                    overflowCount = entry.count,
                                    avatarSize = avatarSizeForIndex(globalIndex),
                                    modifier =
                                        Modifier
                                            .padding(horizontal = trophyRowSpacing / 2)
                                            .offset(y = yJitter),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed interface TrophyRowEntry {
    data class Saga(
        val saga: SagaSummary,
    ) : TrophyRowEntry

    data class Overflow(
        val count: Int,
    ) : TrophyRowEntry
}

private fun buildTrophyRows(
    visibleSagas: List<SagaSummary>,
    overflowCount: Int,
): List<List<TrophyRowEntry>> {
    val entries =
        buildList {
            visibleSagas.forEach { add(TrophyRowEntry.Saga(it)) }
            if (overflowCount > 0) {
                add(TrophyRowEntry.Overflow(overflowCount))
            }
        }
    return entries.chunked(TROPHY_COLUMNS)
}

private fun avatarSizeForIndex(index: Int) = if (index == 0) 76.dp else 64.dp

@Composable
private fun TrophySectionHeader() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .alpha(.5f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.home_completed_sagas_title),
            style =
                MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
        )
    }
}
