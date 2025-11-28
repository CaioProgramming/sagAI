package com.ilustris.sagai.core.file.backup.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.file.BACKUP_PERMISSION
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.backup.RestorableSaga
import com.ilustris.sagai.features.saga.chat.repository.SagaBackupService
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class BackupViewModel
    @Inject
    constructor(
        private val backupService: BackupService,
        private val sagaBackupService: SagaBackupService,
        private val sagaRepository: SagaRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Dimissed)
        val uiState = _uiState.asStateFlow()

        val backupEnabled = backupService.backupEnabled()
        val sagas = sagaRepository.getChats()

        fun observeBackupStatus() {
            viewModelScope.launch {
                backupService.backupEnabled().collect {
                    checkBackup(it)
                }
            }
        }

        fun recoverBackups() {
            viewModelScope.launch {
                _uiState.value = BackupUiState.Loading("Recuperando conteudo...")

                val backups =
                    (backupService.getBackedUpSagas().getSuccess()) ?: run {
                        _uiState.emit(
                            BackupUiState.Empty("Ocorreu um erro inesperado, não foi possível recuperar os conteudos de backup :("),
                        )
                        delay(5.seconds)
                        _uiState.emit(BackupUiState.Dimissed)
                        return@launch
                    }

                val validSagas = sagaBackupService.filterValidSagas(backups).getSuccess() ?: emptyList()

                if (validSagas.isEmpty()) {
                    _uiState.emit(BackupUiState.Empty("Parece que esta tudo em ordem!"))
                    delay(3.seconds)
                    _uiState.emit(BackupUiState.Dimissed)
                } else {
                    _uiState.emit(BackupUiState.Loading("Encontramos algumas coisinhas.."))
                    delay(3.seconds)
                    _uiState.emit(BackupUiState.ShowBackups(validSagas))
                }
            }
        }

        private fun checkBackup(isEnabled: Boolean) =
            viewModelScope.launch {
                _uiState.emit(
                    if (isEnabled) {
                        BackupUiState.BackupEnabled
                    } else {
                        BackupUiState.RequiresPermission()
                    },
                )
            }

        fun saveBackupFolder(
            uri: Uri?,
            displayBackups: Boolean,
        ) {
            viewModelScope.launch {
                _uiState.emit(BackupUiState.Loading("Habilitando backup..."))
                backupService
                    .enableBackup(uri)
                    .onSuccessAsync {
                        _uiState.emit(BackupUiState.Loading("Tudo pronto! Aproveite suas histórias \uD83D\uDC9C"))
                        delay(3.seconds)
                        _uiState.emit(BackupUiState.Dimissed)
                        if (displayBackups) {
                            recoverBackups()
                        }
                    }.onFailureAsync {
                        _uiState.emit(
                            BackupUiState.Empty("Não foi possivel habilitar o backup. Sentimos muito por isso vamos tentar de novo?"),
                        )
                        delay(3.seconds)
                        _uiState.emit(BackupUiState.Dimissed)
                    }
            }
        }

        fun restoreSaga(restorableSaga: RestorableSaga) {
            viewModelScope.launch {
                _uiState.value =
                    BackupUiState.Loading("Restaurando ${restorableSaga.manifest.title}...")
                sagaBackupService.restoreContent(restorableSaga)
                delay(2.seconds)
            }
        }

        fun restoreAllBackups(backups: List<RestorableSaga>) {
            viewModelScope.launch {
                backups.forEach {
                    _uiState.emit(BackupUiState.Loading("Restaurando ${it.manifest.title}..."))
                    sagaBackupService.restoreContent(it)
                    delay(2.seconds)
                }
            }
        }

        fun dismiss() {
            _uiState.value = BackupUiState.Dimissed
        }
    }

sealed class BackupUiState {
    object Dimissed : BackupUiState()

    object CheckingUp : BackupUiState()

    data class Loading(
        val message: String,
    ) : BackupUiState()

    data class RequiresPermission(
        val permission: String = BACKUP_PERMISSION,
    ) : BackupUiState()

    object BackupEnabled : BackupUiState()

    data class ShowBackups(
        val backups: List<RestorableSaga>,
    ) : BackupUiState()

    data class Empty(
        val message: String,
    ) : BackupUiState()
}
