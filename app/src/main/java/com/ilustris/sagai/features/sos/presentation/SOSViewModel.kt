package com.ilustris.sagai.features.sos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.database.backup.BackupMetadata
import com.ilustris.sagai.core.database.backup.DatabaseBackupService
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.backup.RestorableSaga
import com.ilustris.sagai.features.saga.chat.repository.SagaBackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SOSState(
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val dbBackups: List<BackupMetadata> = emptyList(),
    val sagaBackups: List<RestorableSaga> = emptyList(),
    val recoverySuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SOSViewModel
    @Inject
    constructor(
        private val databaseBackupService: DatabaseBackupService,
        private val backupService: BackupService,
        private val sagaBackupService: SagaBackupService,
    ) : ViewModel() {
        private val _state = MutableStateFlow(SOSState())
        val state = _state.asStateFlow()

        fun loadBackups() {
            viewModelScope.launch {
                val dbBackups = databaseBackupService.getAllBackups()
                val sagaBackupsResult = backupService.getBackedUpSagas()

                _state.update {
                    it.copy(
                        dbBackups = dbBackups,
                        sagaBackups = sagaBackupsResult.getSuccess() ?: emptyList(),
                    )
                }
            }
        }

        fun importSagaBackups() {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true, loadingMessage = "Preparing system...") }

                // 1. Clear database
                val clearResult = databaseBackupService.clearDatabase()
                if (clearResult.isFailure) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to clear corrupted database.",
                        )
                    }
                    return@launch
                }

                // 2. Import each saga
                val sagaBackups = _state.value.sagaBackups
                var successCount = 0

                sagaBackups.forEach { saga ->
                    _state.update { it.copy(loadingMessage = "Restoring ${saga.manifest.title}...") }
                    val result = sagaBackupService.restoreContent(saga)
                    if (result.isSuccess) successCount++
                }

                if (successCount > 0 || sagaBackups.isEmpty()) {
                    _state.update { it.copy(isLoading = false, recoverySuccess = true) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to restore any sagas.") }
                }
            }
        }

        fun restoreFullDatabase(backup: BackupMetadata) {
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        isLoading = true,
                        loadingMessage = "Restoring full database...",
                    )
                }
                val result = databaseBackupService.restoreBackup(backup)
                if (result.isSuccess) {
                    _state.update { it.copy(isLoading = false, recoverySuccess = true) }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to restore database backup.",
                        )
                    }
                }
            }
        }

        fun freshStart() {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true, loadingMessage = "Clearing all data...") }
                val result = databaseBackupService.clearDatabase()
                if (result.isSuccess) {
                    _state.update { it.copy(isLoading = false, recoverySuccess = true) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to clear database.") }
                }
            }
        }
    }
