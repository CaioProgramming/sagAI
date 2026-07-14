package com.ilustris.sagai.features.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.player.domain.PlayerProfileUseCase
import com.ilustris.sagai.features.player.domain.UserIdentityUseCase
import com.ilustris.sagai.features.player.ui.mapper.toSections
import com.ilustris.sagai.features.player.ui.model.ProfileSection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerProfileUiState(
    val userName: String = "",
    val sections: List<ProfileSection> = emptyList(),
    val isEmpty: Boolean = true,
)

@HiltViewModel
class PlayerProfileViewModel
    @Inject
    constructor(
        private val playerProfileUseCase: PlayerProfileUseCase,
        private val userIdentityUseCase: UserIdentityUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PlayerProfileUiState())
        val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                combine(
                        userIdentityUseCase.observeName(),
                        playerProfileUseCase.observeProfile(),
                    ) { userName, profile ->
                        val sections =
                            if (profile != null) {
                                profile.toSections(userName)
                            } else {
                                emptyList()
                            }

                        PlayerProfileUiState(
                            userName = userName,
                            sections = sections,
                            isEmpty = sections.isEmpty(),
                        )
                    }
                    .collect { state ->
                        _uiState.emit(state)
                    }
            }
        }
    }


