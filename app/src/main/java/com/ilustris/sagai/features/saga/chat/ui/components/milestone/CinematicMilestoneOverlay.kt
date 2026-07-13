package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.ui.animations.genreVfx

@Composable
fun CinematicMilestoneOverlay(
    milestone: SagaMilestone,
    labelTitle: String,
    stylisedTitle: String,
    genre: Genre,
    dashboardItems: List<MilestoneDashboardItem>,
    sparkModifier: Modifier,
    onDismiss: () -> Unit,
    onRevealStarted: () -> Unit = {},
    onDetailAction: (MilestoneDetailAction) -> Unit = {},
) {
    MilestoneRevealScaffold(
        genre = genre,
        label = labelTitle,
        title = stylisedTitle,
        dashboardItems = dashboardItems,
        onDismiss = onDismiss,
        onRevealStarted = onRevealStarted,
        onDetailAction = onDetailAction,
        sparkModifier = sparkModifier,
        sparkContent = {
            Image(
                painterResource(genre.icon),
                contentDescription = null,
                modifier =
                    sparkModifier
                        .size(56.dp)
                        .genreVfx(genre),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )
        },
    )
}
