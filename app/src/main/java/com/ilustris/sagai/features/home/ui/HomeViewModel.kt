package com.ilustris.sagai.features.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ilustris.sagai.core.services.BillingState
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.usecase.HomeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val homeUseCase: HomeUseCase,
        private val remoteConfig: FirebaseRemoteConfig,
    ) : ViewModel() {
        val sagas = homeUseCase.getSagas()

        private val _showDebugButton = MutableStateFlow(false)
        val showDebugButton: StateFlow<Boolean> = _showDebugButton.asStateFlow()

        val startDebugSaga = MutableStateFlow<Saga?>(null)

        private val _dynamicNewSagaTexts = MutableStateFlow<DynamicSagaPrompt?>(null)
        val dynamicNewSagaTexts: StateFlow<DynamicSagaPrompt?> = _dynamicNewSagaTexts.asStateFlow()

        private val _isLoadingDynamicPrompts = MutableStateFlow(false)
        val isLoadingDynamicPrompts: StateFlow<Boolean> = _isLoadingDynamicPrompts.asStateFlow()
        val billingState = homeUseCase.billingState.asStateFlow()

        init {
            loadRemoteConfigFlag()
            fetchDynamicNewSagaTexts()
        }

        private fun loadRemoteConfigFlag() {
            viewModelScope.launch(Dispatchers.IO) {
                remoteConfig.fetchAndActivate().addOnCompleteListener {
                    val debuggerFlag = remoteConfig.getValue("isDebugger").asBoolean()
                    Log.i(javaClass.simpleName, "loadRemoteConfigFlag: Debugger flag is $debuggerFlag")
                    _showDebugButton.value = debuggerFlag
                }
            }
        }

        fun createFakeSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                homeUseCase.createFakeSaga().onSuccessAsync {
                    startDebugSaga.emit(it)
                    delay(3.seconds)
                    startDebugSaga.emit(null)
                }
            }
        }

        private fun fetchDynamicNewSagaTexts() {
            if (_dynamicNewSagaTexts.value != null && _dynamicNewSagaTexts.value?.title?.isNotEmpty() == true) {
                Log.d("HomeViewModel", "Dynamic saga texts already loaded. Skipping fetch.")
                return
            }
            viewModelScope.launch(Dispatchers.IO) {
                _isLoadingDynamicPrompts.value = true
                Log.d("HomeViewModel", "Fetching new dynamic saga texts...")
                homeUseCase.fetchDynamicNewSagaTexts().onSuccess {
                    _dynamicNewSagaTexts.value = it
                }
                _isLoadingDynamicPrompts.value = false
            }
        }
    }
