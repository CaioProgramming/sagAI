package com.ilustris.sagai.core.file.backup.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.file.BACKUP_PERMISSION
import com.ilustris.sagai.core.file.BackupService
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
                    }.onFailureAsync {
                        _uiState.emit(
                            BackupUiState.Empty("Não foi possivel habilitar o backup. Sentimos muito por isso vamos tentar de novo?"),
                        )
                        delay(3.seconds)
                        _uiState.emit(BackupUiState.Dimissed)
                    }
            }
        }



        fun dismiss() {
            _uiState.value = BackupUiState.Dimissed
        }

        fun setImportConfirmation(uri: Uri) {
            viewModelScope.launch {
                val metadata = backupService.getFileMetadata(uri)
                val extension = metadata.name.substringAfterLast(".")

                val preview = when (extension) {
                    "saga" -> {
                        backupService.previewSagaBackup(uri).getSuccess()?.let {
                            BackupPreview.Single(it)
                        }
                    }

                    "sagas", "zip" -> {
                        backupService.previewFullBackup(uri).getSuccess()?.let {
                            BackupPreview.Full(it)
                        }
                    }

                    else -> null
                }

                _uiState.value =
                    BackupUiState.ImportConfirmation(uri, metadata.size, metadata.date, preview)
            }
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



    data class Empty(
        val message: String,
    ) : BackupUiState()

    data class ImportConfirmation(
        val uri: Uri,
        val size: Long,
        val date: Long,
        val preview: BackupPreview? = null
    ) : BackupUiState()
}

sealed class BackupPreview {
    data class Single(val info: BackupService.SagaPreviewInfo) :
        BackupPreview()

    data class Full(val sagas: List<BackupService.SagaPreviewInfo>) :
        BackupPreview()
}
