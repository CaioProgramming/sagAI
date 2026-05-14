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
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.saga.detail.ui.DetailAction
import com.ilustris.sagai.features.timeline.domain.TimelineViewContent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineThreadList(
    timelineContent: TimelineViewContent,
    modifier: Modifier = Modifier,
    onAction: (DetailAction) -> Unit = {},
) {
    timelineContent.saga.genre

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        timelineContent.groups.forEach { group ->
            stickyHeader {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = group.title,
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
                    onAction = onAction,
                )
            }
        }
    }
}
