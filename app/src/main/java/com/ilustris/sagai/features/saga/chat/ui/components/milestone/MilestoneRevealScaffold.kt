package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestonePhase
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestonePhaseVisibility
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestoneTransitions
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.advanceAfter
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.rememberMilestonePhaseController
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.rememberVectorShape
import com.ilustris.sagai.ui.theme.themeIconVector
import com.ilustris.sagai.ui.theme.themePainter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun MilestoneRevealScaffold(
    genre: Genre,
    label: String,
    title: String,
    dashboardItems: List<MilestoneDashboardItem>,
    onDismiss: () -> Unit,
    onRevealStarted: () -> Unit = {},
    onDetailAction: (MilestoneDetailAction) -> Unit = {},
    sparkModifier: Modifier = Modifier,
    sparkHold: Duration = 1.4.seconds,
    revealHold: Duration = 2.8.seconds,
    sparkContent: (@Composable () -> Unit)? = null,
) {
    val phaseController = rememberMilestonePhaseController(MilestonePhase.Spark)
    val coroutineScope = rememberCoroutineScope()
    val morphBrush = Brush.horizontalGradient(morphingGradient())

    LaunchedEffect(Unit) {
        phaseController.advance(MilestonePhase.Spark)
        phaseController.advanceAfter(coroutineScope, hold = sparkHold, to = MilestonePhase.Reveal)
    }

    LaunchedEffect(phaseController.currentPhase) {
        if (phaseController.currentPhase == MilestonePhase.Reveal) {
            onRevealStarted()
            phaseController.advanceAfter(
                coroutineScope,
                hold = revealHold,
                to = MilestonePhase.Ready,
            )
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        if (phaseController.currentPhase == MilestonePhase.Spark) {
            MilestonePhaseVisibility(
                visible = true,
                enter = MilestoneTransitions.sparkEnter,
                exit = MilestoneTransitions.fadeExit,
                modifier = Modifier.align(Alignment.Center),
            ) {
                sparkContent?.invoke() ?: DefaultMilestoneSpark(sparkModifier, morphBrush)
            }
        }

        if (phaseController.isAtLeast(MilestonePhase.Reveal)) {
            MilestoneScrollableReceipt(
                label = label,
                title = title,
                genre = genre,
                items = dashboardItems,
                showContent = true,
                showContinue = phaseController.isAtLeast(MilestonePhase.Ready),
                onContinue = onDismiss,
                onDetailAction = onDetailAction,
                sparkContent = {
                    sparkContent?.invoke() ?: DefaultMilestoneSpark(sparkModifier, morphBrush)
                },
            )
        }
    }
}

@Composable
private fun DefaultMilestoneSpark(
    sparkModifier: Modifier,
    morphBrush: Brush,
) {
    Icon(
        themePainter(),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.background,
        modifier =
            sparkModifier
                .size(56.dp)
                .dropShadow(rememberVectorShape(themeIconVector())) {
                    brush = morphBrush
                    radius = 24f
                    spread = 1f
                },
    )
}
