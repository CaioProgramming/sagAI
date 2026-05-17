package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestonePhase
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestonePhaseVisibility
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestoneTransitions
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.advanceAfter
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.milestoneTypewriterDuration
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.rememberMilestonePhaseController
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shimmerize
import kotlin.time.Duration.Companion.seconds

@Composable
fun CharacterMilestoneOverlay(
    milestone: SagaMilestone.NewCharacter,
    genre: Genre,
    onDismiss: () -> Unit,
) {
    val character = milestone.character
    val phaseController = rememberMilestonePhaseController(MilestonePhase.Hero)
    val coroutineScope = rememberCoroutineScope()
    val characterName = milestone.subtitle.trim()
    val message = milestone.message?.takeIf { it.isNotBlank() }
    val characterColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        phaseController.advance(MilestonePhase.Hero)
        phaseController.advanceAfter(
            coroutineScope,
            hold = 1.4.seconds,
            to = MilestonePhase.Headline,
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
    ) {
        MilestonePhaseVisibility(
            visible = phaseController.isAtLeast(MilestonePhase.Hero),
            enter = MilestoneTransitions.slamEnter,
        ) {
            CharacterMilestoneHero(
                character = character,
                genre = genre,
            )
        }

        MilestonePhaseVisibility(
            visible = phaseController.isAtLeast(MilestonePhase.Headline),
            enter = MilestoneTransitions.labelEnter,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(milestone.title),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                genre.stylisedText(
                    text = characterName,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .gradientFill(
                                Brush.verticalGradient(characterColor.darkerPalette()),
                            ).reactiveShimmer(
                                isPlaying = true,
                                characterColor.shimmerize(),
                            ),
                )
            }
        }

        LaunchedEffect(phaseController.currentPhase) {
            if (phaseController.currentPhase == MilestonePhase.Headline) {
                kotlinx.coroutines.delay(1.seconds)
                if (message != null) {
                    phaseController.advance(MilestonePhase.Body)
                } else {
                    phaseController.advance(MilestonePhase.Action)
                }
            }
        }

        if (message != null) {
            MilestonePhaseVisibility(
                visible = phaseController.isAtLeast(MilestonePhase.Body),
                enter = MilestoneTransitions.fadeEnter,
            ) {
                SimpleTypewriterText(
                    text = message,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            textAlign = TextAlign.Center,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    duration = milestoneTypewriterDuration(message),
                    onAnimationFinished = {
                        phaseController.advance(MilestonePhase.Action)
                    },
                )
            }
        }

        MilestoneContinueButton(
            genre = genre,
            visible = phaseController.isAtLeast(MilestonePhase.Action),
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun CharacterMilestoneHero(
    character: Character,
    genre: Genre,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(vertical = 16.dp),
    ) {
        if (character.image.isNotBlank()) {
            CharacterAvatar(
                character = character,
                genre = genre,
                modifier = Modifier.size(200.dp),
            )
        } else {
            Image(
                painter = painterResource(genre.icon),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(120.dp)
                        .genreVfx(genre)
                        .reactiveShimmer(isPlaying = true),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
