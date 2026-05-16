package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestonePhase
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestonePhaseVisibility
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestoneTransitions
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.advanceAfter
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.milestoneTypewriterDuration
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.rememberMilestonePhaseController
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import kotlin.time.Duration.Companion.seconds

@Composable
fun CinematicMilestoneOverlay(
    labelTitle: String,
    stylisedTitle: String,
    message: String?,
    genre: Genre,
    sparkModifier: Modifier,
    onDismiss: () -> Unit,
) {
    val phaseController = rememberMilestonePhaseController(MilestonePhase.Hero)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        phaseController.advance(MilestonePhase.Hero)
        phaseController.advanceAfter(coroutineScope, hold = 1.seconds, to = MilestonePhase.Headline)
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
            enter = MilestoneTransitions.heroEnter,
        ) {
            Image(
                painterResource(genre.icon),
                contentDescription = null,
                modifier =
                    sparkModifier
                        .size(48.dp)
                        .genreVfx(genre),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )
        }

        MilestonePhaseVisibility(
            visible = phaseController.isAtLeast(MilestonePhase.Headline),
            enter = MilestoneTransitions.labelEnter,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = labelTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                )
                MilestoneStylisedTitle(
                    genre = genre,
                    text = stylisedTitle,
                    visible = true,
                )
            }
        }

        val bodyMessage = message?.takeIf { it.isNotBlank() }
        if (bodyMessage != null) {
            LaunchedEffect(phaseController.currentPhase) {
                if (phaseController.currentPhase == MilestonePhase.Headline) {
                    kotlinx.coroutines.delay(800)
                    phaseController.advance(MilestonePhase.Body)
                }
            }

            MilestonePhaseVisibility(
                visible = phaseController.isAtLeast(MilestonePhase.Body),
                enter = MilestoneTransitions.fadeEnter,
            ) {
                SimpleTypewriterText(
                    text = bodyMessage,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            textAlign = TextAlign.Center,
                        ),
                    modifier =
                        Modifier
                            .padding(top = 16.dp, bottom = 8.dp),
                    duration = milestoneTypewriterDuration(bodyMessage),
                    onAnimationFinished = {
                        phaseController.advance(MilestonePhase.Action)
                    },
                )
            }
        } else {
            LaunchedEffect(phaseController.currentPhase) {
                if (phaseController.currentPhase == MilestonePhase.Headline) {
                    kotlinx.coroutines.delay(1.2.seconds)
                    phaseController.advance(MilestonePhase.Action)
                }
            }
        }

        MilestoneContinueButton(
            genre = genre,
            visible = phaseController.isAtLeast(MilestonePhase.Action),
            onDismiss = onDismiss,
        )
    }
}
