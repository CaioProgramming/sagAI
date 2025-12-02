package com.ilustris.sagai.core.file.backup.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.BACKUP_PERMISSION
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaBackupService
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    fun importBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(BackupUiState.Loading("Importando backup..."))

            val type = backupService.determineBackupType(uri)
            val result = when (type) {
                BackupService.BackupType.SINGLE_SAGA -> {
                    sagaBackupService.restoreSagaFromUri(uri)
                }

                BackupService.BackupType.FULL_BACKUP -> {
                    executeRequest {
                        // Extract all sagas from the full backup
                        val sagasToRestore = backupService.restoreFullBackup(uri).getSuccess()
                            ?: error("Failed to extract sagas from backup")

                        val restoredSagas = mutableListOf<SagaContent>()

                        // Restore each saga individually using sagaBackupService
                        sagasToRestore.forEach { sagaContent ->
                            try {
                                // Create a temporary single-saga backup for this saga
                                val tempUri =
                                    backupService.exportSagaToCache(sagaContent).getSuccess()
                                if (tempUri != null) {
                                    val restored =
                                        sagaBackupService.restoreSagaFromUri(tempUri).getSuccess()
                                    if (restored != null) {
                                        restoredSagas.add(restored)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "BackupViewModel",
                                    "Failed to restore saga: ${sagaContent.data.title}",
                                    e
                                )
                            }
                        }

                        restoredSagas
                    }
                }

                BackupService.BackupType.UNKNOWN -> {
                    error("Tipo de arquivo não suportado")
                }
            }

            result.onSuccessAsync {
                _uiState.emit(BackupUiState.Loading("Backup importado com sucesso! 💜"))
                delay(2.seconds)
                _uiState.emit(BackupUiState.Dimissed)
            }.onFailureAsync {
                _uiState.emit(BackupUiState.Empty("Erro ao importar backup: ${it.message}"))
                delay(3.seconds)
                _uiState.emit(BackupUiState.Dimissed)
            }
        }
    }

        fun setImportConfirmation(uri: Uri) {
            viewModelScope.launch {
                _uiState.emit(BackupUiState.Loading("Analisando arquivo..."))
                val metadata = backupService.getFileMetadata(uri)
                val type = backupService.determineBackupType(uri)
                val sagas = sagaRepository.getChats().first()

                when (type) {
                    BackupService.BackupType.SINGLE_SAGA -> {
                        val sagaContent =
                            withContext(Dispatchers.IO) { backupService.unzipAndParseSaga(uri) }
                        if (sagaContent == null) {
                            _uiState.emit(BackupUiState.Empty("Arquivo de backup inválido."))
                            delay(3.seconds)
                            _uiState.emit(BackupUiState.Dimissed)
                            return@launch
                        }
                        val isExistingSaga = sagas.any {
                            it.data.id == sagaContent.data.id &&
                                    it.data.title == sagaContent.data.title &&
                                    it.data.createdAt == sagaContent.data.createdAt
                        }

                        if (isExistingSaga) {
                            _uiState.emit(BackupUiState.Empty("Esta saga já existe e não pode ser sobrescrita."))
                            delay(3.seconds)
                            _uiState.emit(BackupUiState.Dimissed)
                            return@launch
                        }
                        // Now we know it does not exist, let's get the preview
                        val preview = backupService.previewSagaBackup(uri).getSuccess()?.let {
                            BackupPreview.Single(it)
                        }
                        if (preview == null) {
                            _uiState.emit(BackupUiState.Empty("Não foi possível pré-visualizar o backup."))
                            delay(3.seconds)
                            _uiState.emit(BackupUiState.Dimissed)
                            return@launch
                        }
                        _uiState.value = BackupUiState.ImportConfirmation(
                            uri,
                            metadata.size,
                            metadata.date,
                            preview
                        )

                    }

                    BackupService.BackupType.FULL_BACKUP -> {
                        val preview = backupService.previewFullBackup(uri).getSuccess()?.let {
                            val backedSagas = it.filter { backupSaga ->
                                sagas.none {
                                    it.data.id == backupSaga.id &&
                                            it.data.title == backupSaga.title &&
                                            it.data.createdAt == backupSaga.createdAt
                                }

                            }
                            if (backedSagas.isEmpty()) {
                                _uiState.emit(BackupUiState.Empty("Nenhuma saga nova para importar."))
                                delay(3.seconds)
                                null
                            } else {
                                BackupPreview.Full(backedSagas)
                            }
                        }
                        if (preview == null) {
                            _uiState.emit(BackupUiState.Empty("Não foi possível pré-visualizar o backup."))
                            delay(3.seconds)
                            _uiState.emit(BackupUiState.Dimissed)
                            return@launch
                        }
                        _uiState.value = BackupUiState.ImportConfirmation(
                            uri,
                            metadata.size,
                            metadata.date,
                            preview
                        )
                    }

                    BackupService.BackupType.UNKNOWN -> {
                        _uiState.emit(BackupUiState.Empty("Tipo de backup desconhecido."))
                        delay(3.seconds)
                        _uiState.emit(BackupUiState.Dimissed)
                    }
                }
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
