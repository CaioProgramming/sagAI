package com.ilustris.sagai.features.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.onboarding.data.OnboardingStateMapper
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.data.SetupNameStep
import com.ilustris.sagai.features.onboarding.data.model.OnboardingContent
import com.ilustris.sagai.features.onboarding.domain.OnboardingUseCase
import com.ilustris.sagai.features.onboarding.domain.SetupStep
import com.ilustris.sagai.features.onboarding.domain.decideSetupSteps
import com.ilustris.sagai.features.player.domain.UserIdentityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns the first-access flow: works out which steps are pending, loads their content and builds
 * the single pager that replaces the separate intro, name and key screens.
 */
@HiltViewModel
class SetupOnboardingViewModel
    @Inject
    constructor(
        private val onboardingUseCase: OnboardingUseCase,
        private val userIdentityUseCase: UserIdentityUseCase,
        private val stateMapper: OnboardingStateMapper,
    ) : ViewModel() {
        private val _state = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
        val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

        private val nameDraft = MutableStateFlow("")

        /**
         * Reads what is pending afresh each time: this ViewModel outlives the gate, and a user who
         * clears their key later must not be shown an intro they saw weeks ago.
         */
        fun load() {
            viewModelScope.launch {
                _state.value = OnboardingUiState.Loading
                val intro = pagesOf(OnboardingType.APP_INTRO)
                val keyPitch = pagesOf(OnboardingType.API_KEY_SETUP)
                val steps =
                    decideSetupSteps(
                        keyMissing = true,
                        // No configured intro is a skipped intro, like every other remote-only content.
                        introPending =
                            intro.pages.isNotEmpty() && onboardingUseCase.shouldShow(OnboardingType.APP_INTRO),
                        namePending = userIdentityUseCase.shouldPromptName(),
                    )
                if (keyPitch.pages.isEmpty()) {
                    _state.value = OnboardingUiState.Error(OnboardingType.API_KEY_SETUP, "No key content configured")
                    return@launch
                }
                nameDraft.value = ""
                _state.value =
                    stateMapper.buildSetupState(
                        steps = steps,
                        intro = intro,
                        keyPitch = keyPitch,
                        name = SetupNameStep(nameDraft, ::commitName),
                    )
                if (SetupStep.INTRO in steps) onboardingUseCase.markSeen(OnboardingType.APP_INTRO)
            }
        }

        private suspend fun pagesOf(type: OnboardingType): OnboardingContent {
            var content = OnboardingContent()
            onboardingUseCase.getContent(type, null).onSuccessAsync { content = it }
            return content
        }

        private fun commitName() {
            val name = nameDraft.value.trim()
            viewModelScope.launch {
                if (name.isBlank()) userIdentityUseCase.skipNamePrompt() else userIdentityUseCase.setName(name)
            }
        }
    }
