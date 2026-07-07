package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.ui.animations.genreVfx
import kotlin.time.Duration.Companion.seconds

@Composable
fun CharacterMilestoneOverlay(
    milestone: SagaMilestone.NewCharacter,
    genre: Genre,
    dashboardItems: List<MilestoneDashboardItem>,
    onDismiss: () -> Unit,
    onRevealStarted: () -> Unit = {},
    onDetailAction: (MilestoneDetailAction) -> Unit = {},
) {
    val characterName = milestone.subtitle.trim()

    MilestoneRevealScaffold(
        genre = genre,
        label = stringResource(milestone.title),
        title = characterName,
        dashboardItems = dashboardItems,
        onDismiss = onDismiss,
        onRevealStarted = onRevealStarted,
        onDetailAction = onDetailAction,
        sparkHold = 1.2.seconds,
        sparkContent = {
            CharacterMilestoneHero(
                character = milestone.character,
                genre = genre,
            )
        },
    )
}

@Composable
private fun CharacterMilestoneHero(
    character: Character,
    genre: Genre,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(180.dp),
    ) {
        if (character.image.isNotBlank()) {
            CharacterAvatar(
                character = character,
                genre = genre,
                modifier = Modifier.size(180.dp),
            )
        } else {
            Image(
                painter = painterResource(genre.icon),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(120.dp)
                        .genreVfx(genre),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
