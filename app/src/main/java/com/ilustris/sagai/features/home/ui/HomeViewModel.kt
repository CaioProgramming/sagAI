package com.ilustris.sagai.features.home.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.lifecycle.AppLifecycleManager
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaSummary
import com.ilustris.sagai.features.home.data.usecase.HomeUseCase
import com.ilustris.sagai.features.stories.data.model.StoryDailyBriefing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val SPLASH_MIN_DURATION_MS = 3_000L
private const val SPLASH_SPARK_FINALE_MS = 1_000L

/** Retained for [com.ilustris.sagai.features.stories.ui.StorySheet]; no longer used from home. */
data class SagaBriefing(
    val saga: SagaContent,
    val briefing: StoryDailyBriefing,
    val segmentationPair: Pair<Bitmap, Bitmap>? = null,
)

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val homeUseCase: HomeUseCase,
        private val backupService: BackupService,
        private val stringResourceHelper: StringResourceHelper,
        private val appLifecycleManager: AppLifecycleManager,
        private val _userIdentityUseCase: com.ilustris.sagai.features.player.domain.UserIdentityUseCase,
    ) : ViewModel() {
        private val stateManager = HomeStateManager()
        val uiState = stateManager.uiState
        val userName = _userIdentityUseCase.observeName()
        val userIdentityUseCase: com.ilustris.sagai.features.player.domain.UserIdentityUseCase
            get() = _userIdentityUseCase

        private val sagas = MutableStateFlow<List<SagaSummary>>(emptyList())
        private val debugEnabled = MutableStateFlow(false)
        private val sagasLoaded = MutableStateFlow(false)

        private val _navigationEvent =
            MutableSharedFlow<HomeNavigationEvent>(extraBufferCapacity = 1)
        val navigationEvent = _navigationEvent.asSharedFlow()

        private val billingState = homeUseCase.billingState
        private var dynamicPromptsRequested = false

        init {
            loadSagas()
            runSplashSequence()
            checkDebug()
            observeDynamicPromptsWhenActive()
            observeVisibleSagas()
            observePremiumState()
            checkForBackups()
            autoBackup()
        }

        private fun observeVisibleSagas() {
            viewModelScope.launch {
                combine(sagas, debugEnabled) { sagaList, debug ->
                    homeUseCase.filterVisibleSagas(sagaList, debug)
                }.collect { stateManager.setVisibleSagas(it) }
            }
        }

        private fun observePremiumState() {
            stateManager.setIsPremium(homeUseCase.isPremium())
            viewModelScope.launch {
                billingState.collect {
                    stateManager.setIsPremium(homeUseCase.isPremium())
                }
            }
        }

        fun handleAction(action: HomeUiAction) {
            when (action) {
                HomeUiAction.CreateNewSaga -> onCreateNewSaga()
                is HomeUiAction.SelectSaga -> onSelectSaga(action.saga)
                HomeUiAction.OpenPremium -> stateManager.setShowPremiumOnboarding(true)
                HomeUiAction.RecoverSagas -> stateManager.setShowBackupSheet(true)
                HomeUiAction.DismissPremiumOnboarding -> stateManager.setShowPremiumOnboarding(false)
                HomeUiAction.DismissBackupSheet -> stateManager.setShowBackupSheet(false)
                HomeUiAction.CreateFakeSaga -> createFakeSaga()
                is HomeUiAction.SaveName -> saveName(action.name)
            }
        }

        private fun saveName(name: String) {
            viewModelScope.launch {
                _userIdentityUseCase.setName(name)
            }
        }

        suspend fun shouldPromptName(): Boolean = userIdentityUseCase.shouldPromptName()

        private fun onCreateNewSaga() {
            val activeCount = sagas.value.count { !it.data.isEnded }
            if (homeUseCase.canCreateNewSaga(activeCount)) {
                viewModelScope.launch {
                    _navigationEvent.emit(HomeNavigationEvent.NewSaga)
                }
            } else {
                stateManager.setShowPremiumOnboarding(true)
            }
        }

        private fun onSelectSaga(saga: Saga) {
            if (!homeUseCase.canOpenSaga(saga, debugEnabled.value)) return
            viewModelScope.launch {
                _navigationEvent.emit(
                    HomeNavigationEvent.Saga(
                        sagaId = saga.id.toString(),
                        isDebug = saga.isDebug,
                    ),
                )
            }
        }

        private fun runSplashSequence() {
            viewModelScope.launch {
                val startedAt = System.currentTimeMillis()
                while (!sagasLoaded.value) {
                    delay(200)
                }
                val elapsed = System.currentTimeMillis() - startedAt
                val minRemaining = (SPLASH_MIN_DURATION_MS - elapsed).coerceAtLeast(0)
                delay(minRemaining)
                delay(SPLASH_SPARK_FINALE_MS)
                stateManager.setScreen(HomeScreen.Content)
            }
        }

        private fun loadSagas() {
            viewModelScope.launch(Dispatchers.IO) {
                homeUseCase.getSagas().collect { sagaList ->
                    sagas.value = sagaList
                    if (!sagasLoaded.value) {
                        sagasLoaded.value = true
                    }
                }
            }
        }

        override fun onCleared() {
            super.onCleared()
            Timber.d("HomeViewModel: onCleared")
        }

        fun checkForBackups() {
            viewModelScope.launch {
                /*backupService.getBackedUpSagas().onSuccessAsync {
                    val availableSagas = sagas.first()
                    stateManager.setBackupAvailable(
                        it.filterBackups(availableSagas.map { it.data }).isNotEmpty(),
                    )
                }*/
            }
        }

        private fun observeDynamicPromptsWhenActive() {
            viewModelScope.launch {
                appLifecycleManager.isAppInForeground.collect { inForeground ->
                    if (inForeground && !dynamicPromptsRequested) {
                        dynamicPromptsRequested = true
                        getDynamicPrompts()
                    }
                }
            }
        }

        private fun getDynamicPrompts() {
            viewModelScope.launch {
                homeUseCase
                    .requestDynamicCall()
                    .onSuccessAsync {
                        stateManager.setDynamicNewSagaTexts(it)
                    }.onFailureAsync {
                        stateManager.setDynamicNewSagaTexts(
                            DynamicSagaPrompt(
                                stringResourceHelper.getString(R.string.home_create_new_saga_title),
                                stringResourceHelper.getString(R.string.home_create_new_saga_subtitle),
                            ),
                        )
                    }
            }
        }

        private fun checkDebug() {
            viewModelScope.launch {
                val enabled = homeUseCase.checkDebugBuild()
                debugEnabled.value = enabled
                stateManager.setShowDebugButton(enabled)
            }
        }

        fun createFakeSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                val result = homeUseCase.createFakeSaga()
                if (result is RequestResult.Success) {
                    _navigationEvent.emit(
                        HomeNavigationEvent.Saga(
                            sagaId = result.value.id.toString(),
                            isDebug = true,
                        ),
                    )
                }
            }
        }

        private var isBackingUp = false

        fun autoBackup() {
            if (isBackingUp) return
            viewModelScope.launch(Dispatchers.IO) {
                isBackingUp = true
                homeUseCase.autoBackup()
                isBackingUp = false
            }
        }
    }
