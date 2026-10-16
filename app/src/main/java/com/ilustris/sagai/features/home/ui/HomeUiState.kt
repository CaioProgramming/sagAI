package com.ilustris.sagai.features.home.ui

import androidx.compose.runtime.Immutable
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.SagaSummary

sealed interface HomeScreen {
    data object Splash : HomeScreen

    data object Content : HomeScreen
}

@Immutable
data class HomeUiState(
    val screen: HomeScreen = HomeScreen.Splash,
    val visibleSagas: List<SagaSummary> = emptyList(),
    val activeSagas: List<SagaSummary> = emptyList(),
    val completedSagas: List<SagaSummary> = emptyList(),
    val showDebugButton: Boolean = false,
    val dynamicNewSagaTexts: DynamicSagaPrompt? = null,
    val isPremium: Boolean = false,
    val showPremiumOnboarding: Boolean = false,
    /** Epoch millis of the last full backup; 0 = never, null = not loaded yet. */
    val lastBackupAt: Long? = null,
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
) {
    val isLoadingDynamicPrompts: Boolean
        get() = dynamicNewSagaTexts == null
}
