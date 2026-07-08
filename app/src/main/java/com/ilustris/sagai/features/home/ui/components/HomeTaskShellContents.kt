package com.ilustris.sagai.features.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingHost
import com.ilustris.sagai.features.onboarding.ui.OnboardingPresentation
import com.ilustris.sagai.features.premium.PremiumTitle
import com.ilustris.sagai.ui.components.taskshell.TaskShellChevron
import com.ilustris.sagai.ui.components.taskshell.TaskShellCompactClick
import com.ilustris.sagai.ui.components.taskshell.TaskShellContent
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpansion
import com.ilustris.sagai.ui.components.taskshell.TaskShellScope
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.themeBrushColors

class DynamicPromptShellContent(
    private val prompt: DynamicSagaPrompt,
    private val onCreateNewSaga: () -> Unit,
) : TaskShellContent {
    override val isExpandable: Boolean = false
    override val isDraggable: Boolean = false
    override val compactClick: TaskShellCompactClick = TaskShellCompactClick.None

    @Composable
    override fun Compact(scope: TaskShellScope) {
        CreateSagaCard(
            dynamicNewSagaTexts = prompt,
            onCreateNewChat = onCreateNewSaga,
            modifier =
                Modifier
                    .fillMaxWidth(),
        )
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        Unit
    }
}

class PremiumShellContent(
    private val onDismissPremium: () -> Unit,
) : TaskShellContent {
    override val isExpandable: Boolean = true
    override val isDraggable: Boolean = true
    override val compactClick: TaskShellCompactClick = TaskShellCompactClick.RequestFull

    @Composable
    override fun Compact(scope: TaskShellScope) {
        val interactionSource = remember { MutableInteractionSource() }
        Column(
            modifier =
                Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) {
                        scope.onRequestFull()
                    }.background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .clickable {
                        scope.onRequestFull()
                    }.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PremiumTitle(
                titleStyle = MaterialTheme.typography.labelLarge,
                brush = Brush.linearGradient(morphingGradient()),
            )
            Text(
                text = stringResource(R.string.premium_sign_up),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(.4f),
            )
        }
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        OnboardingHost(
            type = OnboardingType.PREMIUM_GUIDE,
            presentation = OnboardingPresentation.Embedded,
            force = true,
            onDismiss = {
                scope.onMinimize()
                onDismissPremium()
            },
            modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxWidth(),
        )
    }
}
