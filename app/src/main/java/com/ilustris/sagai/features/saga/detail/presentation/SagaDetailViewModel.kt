package com.ilustris.sagai.features.saga.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SagaDetailViewModel
    @Inject
    constructor(
        private val sagaDetailUseCase: SagaDetailUseCase,
        private val remoteConfig: FirebaseRemoteConfig,
        private val billingService: BillingService,
    ) : ViewModel() {
        private val _state = MutableStateFlow<State>(State.Loading)
        val state: StateFlow<State> = _state.asStateFlow()
        val saga = MutableStateFlow<SagaContent?>(null)
        val isGenerating = MutableStateFlow(false)
        val emotionalCardReference = MutableStateFlow<String>(emptyString())
        val showIntro = MutableStateFlow(false)
        val showReview = MutableStateFlow(false)

        val showPremiumSheet = MutableStateFlow(false)

        fun fetchEmotionalCardReference() {
            viewModelScope.launch(Dispatchers.IO) {
                remoteConfig.fetchAndActivate()
                emotionalCardReference.value = remoteConfig.getString(EMOTIONAL_CARD_CONFIG)
            }
        }

        fun togglePremiumSheet() {
            showPremiumSheet.value = !showPremiumSheet.value
        }

        fun fetchSagaDetails(sagaId: String) {
            showIntro.value = true
            viewModelScope.launch(Dispatchers.IO) {
                emotionalCardReference.value = remoteConfig.getString(EMOTIONAL_CARD_CONFIG)
                _state.value = State.Loading
                sagaDetailUseCase.fetchSaga(sagaId.toInt()).collectLatest { saga ->
                    saga?.let { data ->
                        _state.value = State.Success(data)
                        this@SagaDetailViewModel.saga.value = data
                        if (data.data.isEnded) {
                            fetchEmotionalCardReference()
                            if (data.data.emotionalReview.isNullOrEmpty()) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    createSagaEmotionalReview()
                                }
                            }
                        }
                        launchIntroSequence()
                    }
                }
            }
        }

        fun deleteSaga(saga: Saga) {
            viewModelScope.launch {
                sagaDetailUseCase.deleteSaga(saga)
            }
        }

        fun closeReview() {
            showReview.value = false
        }

        fun createReview() {
            if (isGenerating.value) {
                return
            }
            val currentSaga = saga.value ?: return
            if (currentSaga.data.isEnded.not()) {
                isGenerating.value = false
                return
            }
            if (currentSaga.data.review != null) {
                isGenerating.value = false
                showReview.value = true
                return
            }
            isGenerating.value = true
            viewModelScope.launch(Dispatchers.IO) {
                sagaDetailUseCase
                    .createReview(currentSaga)
                isGenerating.value = false
                showReview.emit(true)
            }
        }

        fun regenerateIcon() {
            val currentSaga = saga.value ?: return
            val isPremium = billingService.isPremium()
            if (isPremium.not()) {
                showPremiumSheet.value = true
            }
            isGenerating.value = true
            viewModelScope.launch(Dispatchers.IO) {
                sagaDetailUseCase.regenerateSagaIcon(
                    currentSaga,
                )
                isGenerating.value = false
            }
        }

        fun resetReview() {
            val currentSaga = saga.value ?: return

            viewModelScope.launch(Dispatchers.IO) {
                sagaDetailUseCase.resetReview(currentSaga)
                delay(500)
                createReview()
            }
        }

        fun createEmotionalReview(timelineContent: TimelineContent) {
            val currentSaga = saga.value ?: return
            viewModelScope.launch {
                isGenerating.value = true
                sagaDetailUseCase.createTimelineReview(currentSaga, timelineContent)
                isGenerating.value = false
            }
        }

        fun createSagaEmotionalReview() {
            val currentSaga = saga.value ?: return
            viewModelScope.launch(Dispatchers.IO) {
                isGenerating.value = true
                sagaDetailUseCase.createSagaEmotionalReview(currentSaga)
                isGenerating.value = false
            }
        }

        private fun launchIntroSequence() {
            viewModelScope.launch {
                delay(2.seconds)
                showIntro.value = false
            }
        }
    }

private const val EMOTIONAL_CARD_CONFIG = "mental_card_icon"
