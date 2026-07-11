package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.ui.theme.components.mascot.MascotEmotionFace

@Composable
fun EventMilestoneOverlay(
    milestone: SagaMilestone.NewEvent,
    genre: Genre,
    dashboardItems: List<MilestoneDashboardItem>,
    onDismiss: () -> Unit,
    onRevealStarted: () -> Unit = {},
    onDetailAction: (MilestoneDetailAction) -> Unit = {},
) {
    val event = milestone.sagaContent.findTimeline(milestone.timeline.id)
    val emotionalTone = event?.data?.emotionalTone

    MilestoneRevealScaffold(
        genre = genre,
        label = stringResource(milestone.title),
        title = milestone.subtitle,
        dashboardItems = dashboardItems,
        onDismiss = onDismiss,
        onRevealStarted = onRevealStarted,
        onDetailAction = onDetailAction,
        sparkContent = {
            emotionalTone?.let { tone ->
                MascotEmotionFace(
                    milestone.emotionalMascot,
                    tone,
                    modifier = Modifier.size(140.dp),
                )
            }
        },
    )
}
