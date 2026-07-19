package com.ilustris.sagai.features.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.features.player.data.model.ProfileTopic
import com.ilustris.sagai.features.player.domain.PlayerProfileUseCase
import com.ilustris.sagai.features.player.domain.UserIdentityUseCase
import com.ilustris.sagai.features.playthrough.PlaythroughUseCase
import com.ilustris.sagai.features.playthrough.data.model.PlayThroughData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerProfileUiState(
    val userName: String = "",
    val topics: List<ProfileTopic> = emptyList(),
    val totalPlaytime: Long = 0L,
    val journeyReview: PlayThroughData? = null,
    val isReviewLoading: Boolean = false,
    val isEnrichingProfile: Boolean = false,
    val enrichmentProgress: String? = null,
    val canBuildProfile: Boolean = false,
    val isEmpty: Boolean = true,
)

@HiltViewModel
class PlayerProfileViewModel
    @Inject
    constructor(
        private val playerProfileUseCase: PlayerProfileUseCase,
        private val userIdentityUseCase: UserIdentityUseCase,
        private val playthroughUseCase: PlaythroughUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PlayerProfileUiState())
        val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                combine(
                    userIdentityUseCase.observeName(),
                    playerProfileUseCase.observeProfile(),
                    playthroughUseCase.availableSagas(),
                ) { userName, profile, playthroughs ->
                    val topics = profile?.topics ?: emptyList()
                    val totalPlaytime = playthroughs.sumOf { it.data.playTimeMs }
                    val canBuildProfile =
                        BuildConfig.DEBUG || topics.isEmpty() && playthroughs.any { it.data.isEnded }

                    PlayerProfileUiState(
                        userName = userName,
                        topics = topics,
                        totalPlaytime = totalPlaytime,
                        canBuildProfile = canBuildProfile,
                        isEmpty = topics.isEmpty() && totalPlaytime == 0L,
                    )
                }.collect { state ->
                    _uiState.emit(state)
                }
            }
        }

        fun loadJourneyReview() {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isReviewLoading = true)
                val result = playthroughUseCase.invoke()
                if (result is com.ilustris.sagai.core.data.RequestResult.Success) {
                    _uiState.value =
                        _uiState.value.copy(journeyReview = result.value, isReviewLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isReviewLoading = false)
                }
            }
        }

        val availableEndedSagas =
            playthroughUseCase
                .availableSagas()
                .map { playthroughs ->
                    playthroughs.filter { it.data.isEnded }
                }

        fun buildProfileFromHistory() {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isEnrichingProfile = true)
                val playthroughs =
                    playthroughUseCase.availableSagas().first().filter { it.data.isEnded }

                playthroughs.forEach { playthrough ->
                    val sagaContent =
                        com.ilustris.sagai.features.home.data.model
                            .SagaContent(playthrough.data)
                    _uiState.value =
                        _uiState.value.copy(
                            enrichmentProgress = "Analyzing ${playthrough.data.title}...",
                        )
                    playerProfileUseCase.recordSagaInsight(sagaContent)
                }

                _uiState.value =
                    _uiState.value.copy(isEnrichingProfile = false, enrichmentProgress = null)
            }
    }

    fun saveName(name: String) {
        viewModelScope.launch {
            userIdentityUseCase.setName(name)
        }
    }
}
