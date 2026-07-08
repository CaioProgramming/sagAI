package com.ilustris.sagai.features.home.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingHost
import com.ilustris.sagai.features.onboarding.ui.OnboardingPresentation
import com.ilustris.sagai.features.premium.PremiumTitle
import com.ilustris.sagai.ui.components.taskshell.TaskShellCompactClick
import com.ilustris.sagai.ui.components.taskshell.TaskShellContent
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpansion
import com.ilustris.sagai.ui.components.taskshell.TaskShellScope
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.morphingGradient

class DynamicPromptShellContent(
    private val prompt: DynamicSagaPrompt,
    private val onCreateNewSaga: () -> Unit,
) : TaskShellContent {
    override val isExpandable: Boolean = true
    override val isDraggable: Boolean = false
    override val compactClick: TaskShellCompactClick = TaskShellCompactClick.None

    @Composable
    override fun Compact(scope: TaskShellScope) {
        CreateSagaCard(
            dynamicNewSagaTexts = prompt,
            onCreateNewChat = {
                scope.onToggle()
            },
            modifier =
                Modifier
                    .fillMaxWidth(),
        )
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        SagAITheme(prompt.genre) {
            Column {
                Text(
                    text = prompt.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .clickable {
                                scope.onToggle()
                            }.padding(16.dp)
                            .fillMaxWidth(),
                )

                Button(
                    onClick = onCreateNewSaga,
                    modifier =
                        Modifier
                            .padding(horizontal = 32.dp)
                            .fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.home_create_new_saga_title))
                }
            }
        }
    }
}

class PremiumShellContent(
    private val onDismissPremium: () -> Unit,
) : TaskShellContent {
    override val isExpandable: Boolean = true
    override val isDraggable: Boolean = false
    override val compactClick: TaskShellCompactClick = TaskShellCompactClick.RequestFull

    @Composable
    override fun Compact(scope: TaskShellScope) {
        AnimatedVisibility(
            scope.expansion == TaskShellExpansion.Collapsed,
            modifier = Modifier.fillMaxWidth(),
            enter = fadeIn(tween(700, easing = EaseIn)) + slideInVertically { it },
            exit = fadeOut(tween(900, easing = EaseOut)) + slideOutVertically { -it },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { scope.onToggle() }
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
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
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth(),
        )
    }
}
