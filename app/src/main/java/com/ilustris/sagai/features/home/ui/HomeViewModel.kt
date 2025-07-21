package com.ilustris.sagai.features.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val sagaHistoryUseCase: SagaHistoryUseCase,
        private val remoteConfig: FirebaseRemoteConfig,
    ) : ViewModel() {
        val sagas =
            sagaHistoryUseCase.getSagas().map {
                it
                    .map { saga ->
                        saga.copy(messages = saga.messages.sortedByDescending { m -> m.message.timestamp })
                    }.sortedByDescending { saga ->
                        saga.messages
                            .lastOrNull()
                            ?.message
                            ?.timestamp ?: 0
                    }
            }

        private val _showDebugButton = MutableStateFlow(false)
        val showDebugButton: StateFlow<Boolean> = _showDebugButton.asStateFlow()

        val startDebugSaga = MutableStateFlow<Saga?>(null)

        init {
            loadRemoteConfigFlag()
        }

        private fun loadRemoteConfigFlag() {
            viewModelScope.launch(Dispatchers.IO) {
                remoteConfig.fetchAndActivate().addOnCompleteListener {
                    val debuggerFlag = remoteConfig.getValue("isDebugger").asBoolean()
                    Log.i(javaClass.simpleName, "loadRemoteConfigFlag: Debbuger flagg is $debuggerFlag")
                    _showDebugButton.value = debuggerFlag
                }
            }
        }

        fun createFakeSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaHistoryUseCase.createFakeSaga().onSuccessAsync {
                    startDebugSaga.emit(it)
                    delay(3.seconds)
                    startDebugSaga.emit(null)
                }
            }
        }
    }
