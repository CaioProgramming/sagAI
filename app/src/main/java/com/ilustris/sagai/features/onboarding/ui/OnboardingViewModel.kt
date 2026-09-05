package com.ilustris.sagai.features.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.onboarding.data.OnboardingStateMapper
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.data.model.OnboardingContent
import com.ilustris.sagai.features.onboarding.domain.OnboardingUseCase
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val onboardingUseCase: OnboardingUseCase,
        val genreVisualConfigService: GenreVisualConfigService,
        private val onboardingStateMapper: OnboardingStateMapper,
        private val billingService: BillingService,
        private val settingsUseCase: SettingsUseCase,
    ) : ViewModel() {
        private val _onboardingState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
        val onboardingState = _onboardingState.asStateFlow()

        val billingState = billingService.state
        val purchaseFlowResult = billingService.purchaseFlowResult

        private val _isPurchaseInProgress = MutableStateFlow(false)
        val isPurchaseInProgress = _isPurchaseInProgress.asStateFlow()

        val currentConfig = MutableStateFlow<GenreVisualConfig?>(null)

        private var fetchJob: kotlinx.coroutines.Job? = null
        private var currentType: OnboardingType? = null
        private var currentGenre: Genre? = null
        private var currentSaga: Saga? = null

        private val _visualConfigs = MutableStateFlow<Map<Genre, GenreVisualConfig>>(emptyMap())
        val visualConfigs = _visualConfigs.asStateFlow()

        private val cachedContent = mutableMapOf<String, OnboardingContent>()

        init {
            viewModelScope.launch {
                // No checkPurchases() here: SagaApp runs one on every foreground, so this only
                // ever duplicated the query that had just happened.
                loadAllConfigs()
            }
            viewModelScope.launch {
                billingService.state
                    .drop(1)
                    .filter { currentType == OnboardingType.PREMIUM_GUIDE }
                    .collect { refreshPremiumOnboarding() }
            }
            viewModelScope.launch {
                billingService.purchaseFlowResult.collect { result ->
                    if (result != BillingService.PurchaseFlowResult.Idle) {
                        _isPurchaseInProgress.value = false
                    }
                }
            }
        }

        fun clearState() {
            if (onboardingState.value != OnboardingUiState.Idle && onboardingState.value != OnboardingUiState.Loading) {
                _onboardingState.value = OnboardingUiState.Idle
            }
        }

        private suspend fun loadAllConfigs() {
            val configs = mutableMapOf<Genre, GenreVisualConfig>()
            Genre.entries.forEach { genre ->
                genreVisualConfigService.getVisualConfig(genre)?.let {
                    configs[genre] = it
                }
            }
            _visualConfigs.emit(configs)
        }

        fun checkOnboarding(
            type: OnboardingType,
            genre: Genre? = null,
            saga: Saga? = null,
            force: Boolean = false,
        ) {
            clearState()
            val cacheKey = "${type.name}_${genre?.name ?: "default"}"

            if (cachedContent.containsKey(cacheKey)) {
                viewModelScope.launch {
                    val content = cachedContent[cacheKey]!!
                    emitOnboardingState(type, content, genre, saga)
                    currentType = type
                    currentGenre = genre
                    currentSaga = saga
                }
                return
            }

            if (currentType == type && !force) return
            fetchJob?.cancel()
            fetchJob =
                viewModelScope.launch {
                    if (force || onboardingUseCase.shouldShow(type)) {
                        _onboardingState.emit(OnboardingUiState.Loading)
                        currentType = type
                        currentGenre = genre
                        currentSaga = saga
                        onboardingUseCase
                            .getContent(type, genre)
                            .onSuccessAsync { content ->
                                cachedContent[cacheKey] = content
                                emitOnboardingState(type, content, genre, saga)
                                onboardingUseCase.markSeen(type)
                            }.onFailureAsync {
                                _onboardingState.emit(
                                    OnboardingUiState.Error(
                                        type,
                                        it.message ?: "Unknown error",
                                    ),
                                )
                                currentType = null
                                currentGenre = null
                                currentSaga = null
                            }
                    }
                }
        }

        private suspend fun emitOnboardingState(
            type: OnboardingType,
            content: OnboardingContent,
            genre: Genre?,
            saga: Saga?,
        ) {
            _onboardingState.emit(
                onboardingStateMapper.buildOnboardingState(
                    type,
                    content,
                    genre,
                    saga,
                ),
            )
        }

        private suspend fun refreshPremiumOnboarding() {
            val type = currentType ?: return
            if (type != OnboardingType.PREMIUM_GUIDE) return
            val genre = currentGenre
            val saga = currentSaga
            val cacheKey = "${type.name}_${genre?.name ?: "default"}"
            val content = cachedContent[cacheKey] ?: return
            if (onboardingState.value is OnboardingUiState.Content) {
                emitOnboardingState(type, content, genre, saga)
            }
        }

        fun switchVisualConfig(genre: Genre) {
            viewModelScope.launch {
                currentConfig.emit(genreVisualConfigService.getVisualConfig(genre))
            }
        }

        fun markAsSeen(type: OnboardingType) {
            viewModelScope.launch {
                _onboardingState.emit(OnboardingUiState.Idle)
                currentType = null
                currentGenre = null
                currentSaga = null
            }
        }

        fun handleAction(
            action: OnboardingAction,
            activity: MainActivity? = null,
        ) {
            viewModelScope.launch {
                when (action) {
                    is OnboardingAction.Dismiss -> {
                        billingService.resetPurchaseFlowResult()
                        _onboardingState.value = OnboardingUiState.Idle
                        currentType = null
                        currentGenre = null
                        currentSaga = null
                    }

                    is OnboardingAction.DeactivateTutorials -> {
                        settingsUseCase.setShowTutorials(false)
                        _onboardingState.value = OnboardingUiState.Idle
                        currentType = null
                        currentGenre = null
                        currentSaga = null
                    }

                    else -> { // UI-internal actions like Next/Skip are handled by PagerState
                    }
                }
            }
        }

        fun syncSubscription() {
            viewModelScope.launch {
                _isPurchaseInProgress.value = true
                billingService.resetPurchaseFlowResult()
                billingService.syncSubscription()
                _isPurchaseInProgress.value = false
            }
        }


        fun dismissPurchaseResult() {
            billingService.resetPurchaseFlowResult()
        }
    }
