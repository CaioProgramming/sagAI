package com.ilustris.sagai.features.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.onboarding.data.OnboardingContent
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.domain.OnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OnboardingUiState {
    data object Idle : OnboardingUiState()

    data object Loading : OnboardingUiState()

    data class Content(
        val content: OnboardingContent,
        val visualConfig: GenreVisualConfig? = null,
    ) : OnboardingUiState()

    data class Error(
        val message: String,
    ) : OnboardingUiState()
}

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val onboardingUseCase: OnboardingUseCase,
        val genreVisualConfigService: GenreVisualConfigService,
        private val billingService: BillingService,
    ) : ViewModel() {
        private val _onboardingState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
        val onboardingState = _onboardingState.asStateFlow()

        val billingState = billingService.state

        val currentConfig = MutableStateFlow<GenreVisualConfig?>(null)

        private var fetchJob: kotlinx.coroutines.Job? = null
        private var currentType: OnboardingType? = null

        private val _visualConfigs = MutableStateFlow<Map<Genre, GenreVisualConfig>>(emptyMap())
        val visualConfigs = _visualConfigs.asStateFlow()

        init {
            viewModelScope.launch {
                billingService.checkPurchases()
                loadAllConfigs()
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
            force: Boolean = false,
        ) {
            if (_onboardingState.value is OnboardingUiState.Loading || (currentType == type && !force)) return
            fetchJob?.cancel()
            fetchJob =
                viewModelScope.launch {
                    if (force || onboardingUseCase.shouldShow(type)) {
                        _onboardingState.emit(OnboardingUiState.Loading)
                        currentType = type
                        onboardingUseCase
                            .getContent(type, genre)
                            .onSuccessAsync { content ->
                                val visualConfig =
                                    genre?.let { genreVisualConfigService.getVisualConfig(it) }
                                _onboardingState.emit(OnboardingUiState.Content(content, visualConfig))
                                onboardingUseCase.markSeen(type)
                                currentConfig.emit(visualConfig)
                            }.onFailureAsync {
                                _onboardingState.emit(
                                    OnboardingUiState.Error(
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
