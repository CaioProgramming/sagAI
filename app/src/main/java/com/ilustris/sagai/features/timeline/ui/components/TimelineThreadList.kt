package com.ilustris.sagai.features.timeline.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.saga.detail.ui.DetailAction
import com.ilustris.sagai.features.timeline.domain.TimelineViewContent
import com.ilustris.sagai.ui.theme.fadeGradientTop

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineThreadList(
    timelineContent: TimelineViewContent,
    modifier: Modifier = Modifier,
    eventXp: Map<Int, Map<Int, com.ilustris.sagai.features.progression.domain.EventXp>> = emptyMap(),
    focusEventId: Int? = null,
    onAction: (DetailAction) -> Unit = {},
) {
    timelineContent.saga.genre

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    // Each group is a sticky header followed by its events.
    androidx.compose.runtime.LaunchedEffect(focusEventId, timelineContent.groups.size) {
        if (focusEventId == null) return@LaunchedEffect
        var index = 0
        for (group in timelineContent.groups) {
            index += 1
            val inGroup = group.events.indexOfFirst { it.timelineContent.data.id == focusEventId }
            if (inGroup >= 0) {
                listState.animateScrollToItem(index + inGroup)
                return@LaunchedEffect
            }
            index += group.events.size
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        timelineContent.groups.forEach { group ->
            stickyHeader {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(fadeGradientTop())
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    Text(
                        text = group.title,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                            ),
                    )
                }
            }

            items(group.events) { event ->
                TimelineThreadCard(
                    sagaInfo = timelineContent.saga,
                    eventCard = event,
                    eventXp = eventXp[event.timelineContent.data.id].orEmpty(),
                    highlighted = event.timelineContent.data.id == focusEventId,
                    onAction = onAction,
                )
            }
        }
    }
}
