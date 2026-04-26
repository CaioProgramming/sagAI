package com.ilustris.sagai.core.file.backup.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.database.backup.BackupMetadata
import com.ilustris.sagai.core.database.backup.DatabaseBackupService
import com.ilustris.sagai.core.file.BACKUP_PERMISSION
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.restartApp
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
        private val databaseBackupService: DatabaseBackupService,
        private val settingsUseCase: SettingsUseCase,
        private val stringHelper: StringResourceHelper,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Dimissed)
        val uiState = _uiState.asStateFlow()

        val backupEnabled = backupService.backupEnabled()

        fun observeBackupStatus() {
            viewModelScope.launch {
                backupService.backupEnabled().collect {
                    checkBackup(it)
                }
            }
        }

        fun recoverBackups() {
            viewModelScope.launch {
                _uiState.value =
                    BackupUiState.Loading(stringHelper.getString(R.string.backup_loading_recovering_content))

                val backups = databaseBackupService.getAllBackups()

                if (backups.isEmpty()) {
                    _uiState.emit(BackupUiState.Empty(stringHelper.getString(R.string.backup_message_all_good)))
                    delay(3.seconds)
                    _uiState.emit(BackupUiState.Dimissed)
                } else {
                    _uiState.emit(BackupUiState.Loading(stringHelper.getString(R.string.backup_loading_found_items)))
                    delay(3.seconds)
                    _uiState.emit(BackupUiState.ShowBackups(backups))
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
                _uiState.emit(BackupUiState.Loading(stringHelper.getString(R.string.backup_loading_enabling)))
                backupService
                    .enableBackup(uri)
                    .onSuccessAsync {
                        _uiState.emit(BackupUiState.Loading(stringHelper.getString(R.string.backup_success_enabled)))
                        delay(3.seconds)
                        _uiState.emit(BackupUiState.Dimissed)
                        if (displayBackups) {
                            recoverBackups()
                        }
                    }.onFailureAsync {
                        _uiState.emit(
                            BackupUiState.Empty(stringHelper.getString(R.string.backup_error_enable_failed)),
                        )
                        delay(3.seconds)
                        _uiState.emit(BackupUiState.Dimissed)
                    }
            }
        }

        fun restoreDatabase(backup: BackupMetadata) {
            viewModelScope.launch {
                _uiState.value =
                    BackupUiState.Loading(
                        stringHelper.getString(R.string.backup_loading_restoring_database),
                    )
                databaseBackupService.restoreBackup(backup)
                delay(2.seconds)
                _uiState.emit(BackupUiState.Dimissed)
                context.restartApp()
            }
        }

        fun createBackup() {
            viewModelScope.launch {
                _uiState.value =
                    BackupUiState.Loading(stringHelper.getString(R.string.backup_loading_creating))
                databaseBackupService.createBackup()
                delay(2.seconds)
                _uiState.emit(BackupUiState.Dimissed)
            }
        }

        fun importDatabase(uri: Uri) {
            viewModelScope.launch {
                _uiState.value =
                    BackupUiState.Loading(
                        stringHelper.getString(R.string.backup_loading_restoring_database),
                    )
                settingsUseCase
                    .importDatabase(uri)
                    .onSuccessAsync {
                        _uiState.emit(BackupUiState.Dimissed)
                        context.restartApp()
                    }.onFailureAsync {
                        _uiState.emit(
                            BackupUiState.Empty(
                                it.message ?: "Falha ao importar banco de dados.",
                            ),
                        )
                        delay(3.seconds)
                        _uiState.emit(BackupUiState.Dimissed)
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
        val backups: List<BackupMetadata>,
    ) : BackupUiState()

    data class Empty(
        val message: String,
    ) : BackupUiState()
}
