package com.ilustris.sagai.features.home.ui

import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.SagaSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeStateManager {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun updateState(updater: (HomeUiState) -> HomeUiState) {
        _uiState.update(updater)
    }

    fun setScreen(screen: HomeScreen) {
        _uiState.update { it.copy(screen = screen) }
    }

    fun setVisibleSagas(sagas: List<SagaSummary>) {
        _uiState.update {
            it.copy(
                visibleSagas = sagas,
                activeSagas = sagas.filter { summary -> !summary.data.isEnded },
                completedSagas = sagas.filter { summary -> summary.data.isEnded },
            )
        }
    }

    fun setShowDebugButton(show: Boolean) {
        _uiState.update { it.copy(showDebugButton = show) }
    }

    fun setDynamicNewSagaTexts(texts: DynamicSagaPrompt?) {
        _uiState.update { it.copy(dynamicNewSagaTexts = texts) }
    }

    fun setIsPremium(isPremium: Boolean) {
        _uiState.update { it.copy(isPremium = isPremium) }
    }

    fun setShowPremiumOnboarding(show: Boolean) {
        _uiState.update { it.copy(showPremiumOnboarding = show) }
    }

    fun setShowBackupSheet(show: Boolean) {
        _uiState.update { it.copy(showBackupSheet = show) }
    }

    fun setBackupAvailable(available: Boolean) {
        _uiState.update { it.copy(backupAvailable = available) }
    }

    fun setLoading(
        loading: Boolean,
        message: String? = _uiState.value.loadingMessage,
    ) {
        _uiState.update { it.copy(isLoading = loading, loadingMessage = message) }
    }
}
