package com.ilustris.sagai.features.sos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.database.backup.BackupMetadata
import com.ilustris.sagai.core.database.backup.DatabaseBackupService
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.backup.RestorableSaga
import com.ilustris.sagai.core.utils.StringResourceHelper
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
        private val stringResourceHelper: StringResourceHelper,
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
                _state.update { it.copy(isLoading = true, loadingMessage = stringResourceHelper.getString(R.string.sos_loading_preparing)) }

                // 1. Clear database
                val clearResult = databaseBackupService.clearDatabase()
                if (clearResult.isFailure) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = stringResourceHelper.getString(R.string.sos_error_clear_database_failed),
                        )
                    }
                    return@launch
                }

                // 2. Import each saga
                val sagaBackups = _state.value.sagaBackups
                var successCount = 0

                sagaBackups.forEach { saga ->
                    _state.update {
                        it.copy(
                            loadingMessage =
                                stringResourceHelper.getString(
                                    R.string.backup_loading_restoring_saga,
                                    saga.manifest.title,
                                ),
                        )
                    }
                    val result = sagaBackupService.restoreContent(saga)
                    if (result.isSuccess) successCount++
                }

                if (successCount > 0 || sagaBackups.isEmpty()) {
                    _state.update { it.copy(isLoading = false, recoverySuccess = true) }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = stringResourceHelper.getString(R.string.sos_error_restore_sagas_failed),
                        )
                    }
                }
            }
        }

        fun restoreFullDatabase(backup: BackupMetadata) {
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        isLoading = true,
                        loadingMessage = stringResourceHelper.getString(R.string.backup_loading_restoring_database),
                    )
                }
                val result = databaseBackupService.restoreBackup(backup)
                if (result.isSuccess) {
                    _state.update { it.copy(isLoading = false, recoverySuccess = true) }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = stringResourceHelper.getString(R.string.backup_error_restore_database_failed),
                        )
                    }
                }
            }
        }

        fun freshStart() {
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        isLoading = true,
                        loadingMessage = stringResourceHelper.getString(R.string.sos_loading_clearing_data),
                    )
                }
                val result = databaseBackupService.clearDatabase()
                if (result.isSuccess) {
                    _state.update { it.copy(isLoading = false, recoverySuccess = true) }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = stringResourceHelper.getString(R.string.sos_error_clear_database),
                        )
                    }
                }
            }
        }
    }
