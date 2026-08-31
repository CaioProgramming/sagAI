package com.ilustris.sagai.features.onboarding.ui.apikey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.key.ApiKeyDiagnosis
import com.ilustris.sagai.core.ai.key.UserApiKeyStore
import com.ilustris.sagai.core.ai.key.classifyApiKeyFailure
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.network.GeminiApiClient
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.data.model.OnboardingContent
import com.ilustris.sagai.features.onboarding.domain.OnboardingUseCase
import com.ilustris.sagai.features.player.domain.UserIdentityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ApiKeySetupUiState {
    object Idle : ApiKeySetupUiState()

    object Validating : ApiKeySetupUiState()

    object Saved : ApiKeySetupUiState()

    /** Google turned the key down — a typo, a partial paste, or a revoked key. */
    object Rejected : ApiKeySetupUiState()

    /** We never got an answer, so the key is unproven rather than bad. Do not discard it. */
    object Unreachable : ApiKeySetupUiState()

    object Empty : ApiKeySetupUiState()
}

@HiltViewModel
class ApiKeySetupViewModel
    @Inject
    constructor(
        private val userApiKeyStore: UserApiKeyStore,
        private val geminiApiClient: GeminiApiClient,
        private val dataStorePreferences: DataStorePreferences,
        private val userIdentityUseCase: UserIdentityUseCase,
        private val onboardingUseCase: OnboardingUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<ApiKeySetupUiState>(ApiKeySetupUiState.Idle)
        val uiState: StateFlow<ApiKeySetupUiState> = _uiState.asStateFlow()

        /**
         * "A key was just saved", delivered once and consumed.
         *
         * Not read off [uiState], because this ViewModel is shared: Compose's `Dialog` inherits
         * `LocalViewModelStoreOwner` from its parent, so opening "update key" from Settings gets
         * the very same instance the onboarding gate used — still holding [ApiKeySetupUiState.Saved]
         * from the first time a key was stored. A `LaunchedEffect` keyed on that state fires on the
         * value it finds at composition, not on a transition, so the sheet dismissed itself the
         * instant it opened. An event can only be collected once, so a stale one cannot exist.
         */
        private val _keySaved = Channel<Unit>(Channel.BUFFERED)
        val keySaved = _keySaved.receiveAsFlow()

        /** Clears leftover state so a reopened screen does not show the last run's outcome. */
        fun prepareForEntry() {
            _uiState.value = ApiKeySetupUiState.Idle
        }

        /**
         * Whether this install has been through the app before, which decides between the
         * first-run pitch and the "Sagas changed" migration copy. Anyone who got as far as the
         * name prompt already has sagas worth reassuring them about.
         */
        private val _isMigration = MutableStateFlow(false)
        val isMigration: StateFlow<Boolean> = _isMigration.asStateFlow()

        /**
         * Ask for a name before asking for a key.
         *
         * The name is a warm, no-stakes question; the key is the one that needs the user to leave
         * for Google AI Studio. Leading with the harder ask, to someone who has seen nothing of the
         * app yet, is how you lose them at the door. Skipped for migrations — those users named
         * themselves long ago.
         */
        private val _needsName = MutableStateFlow(false)
        val needsName: StateFlow<Boolean> = _needsName.asStateFlow()

        init {
            viewModelScope.launch {
                _isMigration.value =
                    dataStorePreferences.getBooleanNow("user_name_prompt_seen", false)
                _needsName.value = userIdentityUseCase.shouldPromptName()
            }
        }

        /**
         * The explanatory pages, from `onboarding_fallbacks` in Remote Config.
         *
         * Empty is a valid state, not an error: the key field is the part that cannot be skipped,
         * so a missing or malformed config costs the pitch, never the ability to get in.
         */
        private val _pages = MutableStateFlow<OnboardingContent>(OnboardingContent())
        val pages: StateFlow<OnboardingContent> = _pages.asStateFlow()

        fun loadPages() {
            viewModelScope.launch {
                onboardingUseCase
                    .getContent(OnboardingType.API_KEY_SETUP, null)
                    .onSuccess { _pages.value = it }
            }
        }

        fun saveName(name: String) {
            viewModelScope.launch {
                if (name.isNotBlank()) userIdentityUseCase.setName(name.trim())
                _needsName.value = false
            }
        }

        fun skipName() {
            _needsName.value = false
        }

        fun submit(rawKey: String) {
            val key = rawKey.trim()
            if (key.isBlank()) {
                _uiState.value = ApiKeySetupUiState.Empty
                return
            }

            viewModelScope.launch {
                _uiState.value = ApiKeySetupUiState.Validating
                executeRequest(reportCrash = false) {
                    // Verified before storing: a key that fails here would otherwise sail past the
                    // app gate and only surface as a failed generation deep inside a saga.
                    geminiApiClient.listModels(key)
                    userApiKeyStore.save(key)
                }.onSuccess {
                    _uiState.value = ApiKeySetupUiState.Saved
                    _keySaved.trySend(Unit)
                }.onFailureAsync { error ->
                    _uiState.value =
                        when (classifyApiKeyFailure(error)) {
                            is ApiKeyDiagnosis.Rejected -> ApiKeySetupUiState.Rejected
                            // Quota answers prove the key is real — it is just busy. Accept it and
                            // let the cooldown machinery handle the rest.
                            is ApiKeyDiagnosis.QuotaDaily,
                            is ApiKeyDiagnosis.QuotaMinute,
                            -> {
                                userApiKeyStore.save(key)
                                _keySaved.trySend(Unit)
                                ApiKeySetupUiState.Saved
                            }

                            null -> ApiKeySetupUiState.Unreachable
                        }
                }
            }
        }

        fun resetError() {
            if (_uiState.value != ApiKeySetupUiState.Saved) {
                _uiState.value = ApiKeySetupUiState.Idle
            }
        }
    }
