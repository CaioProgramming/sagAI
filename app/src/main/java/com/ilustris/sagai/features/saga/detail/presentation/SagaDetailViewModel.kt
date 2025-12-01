package com.ilustris.sagai.features.saga.detail.presentation

import android.net.Uri
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
import com.ilustris.sagai.features.wiki.data.model.Wiki
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
        private val _exportLauncher = MutableSharedFlow<String>()
        val exportLauncher = _exportLauncher.asSharedFlow()
        private val _loadingMessage = MutableStateFlow<String?>(null)
        val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()
        val backupEnabled = sagaDetailUseCase.getBackupEnabled()

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
            _loadingMessage.value = "Generating your saga review..."
            viewModelScope.launch(Dispatchers.IO) {
                sagaDetailUseCase
                    .createReview(currentSaga)
                    .onSuccessAsync {
                        showReview.emit(true)
                    }
                isGenerating.value = false
                _loadingMessage.value = null
            }
        }

        fun regenerateIcon() {
            val currentSaga = saga.value ?: return
            val isPremium = billingService.isPremium()
            if (isPremium.not()) {
                showPremiumSheet.value = true
            }
            isGenerating.value = true
            _loadingMessage.value = "Regenerating saga icon..."
            viewModelScope.launch(Dispatchers.IO) {
                sagaDetailUseCase.regenerateSagaIcon(
                    currentSaga,
                )
                isGenerating.value = false
                _loadingMessage.value = null
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

        fun generateTimelineContent(timelineContent: TimelineContent) {
            val currentSaga = saga.value ?: return
            viewModelScope.launch {
                isGenerating.value = true
                _loadingMessage.value = "Generating timeline content..."
                sagaDetailUseCase.generateTimelineContent(currentSaga, timelineContent)
                isGenerating.value = false
                _loadingMessage.value = null
            }
        }

        fun createSagaEmotionalReview() {
            val currentSaga = saga.value ?: return
            viewModelScope.launch(Dispatchers.IO) {
                isGenerating.value = true
                _loadingMessage.value = "Creating emotional review..."
                sagaDetailUseCase.createEmotionalConclusion(currentSaga)
                isGenerating.value = false
                _loadingMessage.value = null
            }
        }

        private fun launchIntroSequence() {
            viewModelScope.launch {
                delay(2.seconds)
                showIntro.value = false
            }
        }

        fun reviewWiki(wikis: List<Wiki>) {
            val currentsaga = saga.value ?: return
            viewModelScope.launch {
                isGenerating.emit(true)
                _loadingMessage.value = "Reviewing wiki entries..."
                sagaDetailUseCase.reviewWiki(currentsaga, wikis)
                isGenerating.emit(false)
                _loadingMessage.value = null
            }
        }

        fun exportSaga() {
            val currentSaga = saga.value ?: return
            viewModelScope.launch {
                val suggestedFileName = "${currentSaga.data.title.replace(" ", "_")}.saga"
                _exportLauncher.emit(suggestedFileName)
            }
        }

        fun handleExportDestination(destinationUri: Uri) {
            val currentSaga = saga.value ?: return
            viewModelScope.launch {
                isGenerating.emit(true)
                _loadingMessage.value = "Packing up your saga..."
                sagaDetailUseCase
                    .exportSaga(currentSaga.data.id, destinationUri)
                    .onSuccessAsync {
                        isGenerating.emit(false)
                        _loadingMessage.value = null
                    }.onFailureAsync {
                        _loadingMessage.value = "Sorry, an unexpected error happened :("
                        delay(3.seconds)
                        isGenerating.emit(false)
                        _loadingMessage.value = null
                    }
            }
        }
    }

private const val EMOTIONAL_CARD_CONFIG = "mental_card_icon"
