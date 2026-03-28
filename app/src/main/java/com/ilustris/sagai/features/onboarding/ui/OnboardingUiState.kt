package com.ilustris.sagai.features.onboarding.ui

import androidx.compose.runtime.Composable
import com.ilustris.sagai.features.onboarding.data.OnboardingType

sealed class OnboardingAction {
    data object Next : OnboardingAction()

    data object Skip : OnboardingAction()

    data object Dismiss : OnboardingAction()

    data object Restore : OnboardingAction()

    data class Subscribe(
        val productId: String,
    ) : OnboardingAction()

    data class Finish(
        val metadata: Map<String, Any?> = emptyMap(),
    ) : OnboardingAction()
}

data class OnboardingButton(
    val text: String,
    val action: OnboardingAction,
)

data class OnboardingUiPage(
    val background: @Composable () -> Unit,
    val content: @Composable () -> Unit,
    val primaryButton: OnboardingButton? = null,
    val secondaryButton: OnboardingButton? = null,
)

sealed class OnboardingUiState(
    open val type: OnboardingType? = null,
) {
    data object Idle : OnboardingUiState()

    data object Loading : OnboardingUiState()

    data class Content(
        override val type: OnboardingType,
        val pages: List<OnboardingUiPage>,
        val metadata: Map<String, Any?> = emptyMap(),
    ) : OnboardingUiState(type)

    data class Error(
        override val type: OnboardingType,
        val message: String,
    ) : OnboardingUiState(type)
}
