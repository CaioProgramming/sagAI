package com.ilustris.sagai.features.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.onboarding.data.OnboardingContent
import com.ilustris.sagai.features.onboarding.data.OnboardingStateMapper
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.domain.OnboardingUseCase
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

        val currentConfig = MutableStateFlow<GenreVisualConfig?>(null)

        private var fetchJob: kotlinx.coroutines.Job? = null
        private var currentType: OnboardingType? = null

        private val _visualConfigs = MutableStateFlow<Map<Genre, GenreVisualConfig>>(emptyMap())
        val visualConfigs = _visualConfigs.asStateFlow()

        private val cachedContent = mutableMapOf<String, OnboardingContent>()

        init {
            viewModelScope.launch {
                billingService.checkPurchases()
                loadAllConfigs()
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
                    _onboardingState.emit(
                        onboardingStateMapper.buildOnboardingState(
                            type,
                            content,
                            genre,
                            saga,
                        ),
                    )
                    currentType = type
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
                        onboardingUseCase
                            .getContent(type, genre)
                            .onSuccessAsync { content ->
                                cachedContent[cacheKey] = content
                                _onboardingState.emit(
                                    onboardingStateMapper.buildOnboardingState(
                                        type,
                                        content,
                                        genre,
                                        saga,
                                    ),
                                )
                                onboardingUseCase.markSeen(type)
                            }.onFailureAsync {
                                _onboardingState.emit(
                                    OnboardingUiState.Error(
                                        type,
                                        it.message ?: "Unknown error",
                                    ),
                                )
                                currentType = null
                            }
                    }
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
            }
        }

        fun handleAction(
            action: OnboardingAction,
            activity: MainActivity? = null,
        ) {
            if (currentType == OnboardingType.PREMIUM_GUIDE && BuildConfig.DEBUG) {
                currentType = null
                handleAction(OnboardingAction.Dismiss)
            }
            viewModelScope.launch {
                when (action) {
                    is OnboardingAction.Subscribe -> {
                        val disabledState =
                            billingService.state.value as? BillingService.BillingState.SignatureDisabled
                        val product = disabledState?.products?.firstOrNull()
                        val offerToken = product?.subscriptionOfferDetails?.firstOrNull()?.offerToken
                        if (activity != null && product != null && offerToken != null) {
                            purchasePremium(activity, product, offerToken)
                        }
                    }

                    is OnboardingAction.Restore -> {
                        restorePurchases()
                    }

                    is OnboardingAction.Dismiss -> {
                        _onboardingState.value = OnboardingUiState.Idle
                        currentType = null
                    }

                    is OnboardingAction.DeactivateTutorials -> {
                        settingsUseCase.setShowTutorials(false)
                        handleAction(OnboardingAction.Dismiss)
                    }

                    else -> { // UI-internal actions like Next/Skip are handled by PagerState
                    }
                }
            }
        }

        fun purchasePremium(
            activity: MainActivity,
            productDetails: ProductDetails,
            offerToken: String,
        ) {
            viewModelScope.launch {
                billingService.purchaseSignature(activity, productDetails, offerToken)
            }
        }

        fun restorePurchases() {
            viewModelScope.launch {
                billingService.loadSignatureProduct()
            }
        }
    }
