package com.ilustris.sagai.features.settings.domain

import android.Manifest
import android.content.Context
import android.net.Uri
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.file.FileManager
import com.ilustris.sagai.core.file.backup.RestorableSaga
import com.ilustris.sagai.core.file.backup.SagaManifest
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.core.permissions.PermissionStatus
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaBackupService
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.settings.ui.SagaStorageInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface SettingsUseCase {
    fun getNotificationsEnabled(): Flow<Boolean>

    fun getSmartSuggestionsEnabled(): Flow<Boolean>

    fun getMessageEffectsEnabled(): Flow<Boolean>

    fun backupEnabled(): Flow<Boolean>

    suspend fun setSmartSuggestionsEnabled(enabled: Boolean)

    suspend fun setMessageEffectsEnabled(enabled: Boolean)

    suspend fun getAppStorageUsage(): Long

    suspend fun getSagaStorageUsage(sagaId: Int): Long

    suspend fun wipeAppData()

    suspend fun isUserPro(): Boolean

    fun getSagas(): Flow<List<SagaStorageInfo>>

    suspend fun getStorageBreakdown(): StorageBreakdown

    suspend fun clearCache()

    suspend fun disableBackup()

    suspend fun enableBackup(uri: Uri?): RequestResult<Unit>

    suspend fun restoreSaga(uri: Uri): RequestResult<SagaContent>
}

class SettingsUseCaseImpl
    @Inject
    constructor(
        private val dataStorePreferences: DataStorePreferences,
        @ApplicationContext
        private val context: Context,
        private val sagaRepository: SagaRepository,
        private val billingService: BillingService,
        private val fileHelper: FileHelper,
        private val permissionService: PermissionService,
        private val backupService: BackupService,
        private val fileManager: FileManager,
        private val sagaBackupService: SagaBackupService,
    ) : SettingsUseCase {
        companion object {
            const val SMART_SUGGESTIONS_ENABLED_KEY = "smart_suggestions_enabled"
            const val MESSAGE_EFFECTS_ENABLED_KEY = "message_effects_enabled"
        }

        override fun getNotificationsEnabled(): Flow<Boolean> =
            permissionService
                .observePermission(Manifest.permission.POST_NOTIFICATIONS)
                .map { it == PermissionStatus.GRANTED }

        override fun getSmartSuggestionsEnabled(): Flow<Boolean> = dataStorePreferences.getBoolean(SMART_SUGGESTIONS_ENABLED_KEY, true)

    override fun getMessageEffectsEnabled(): Flow<Boolean> =
        dataStorePreferences.getBoolean(MESSAGE_EFFECTS_ENABLED_KEY, true)

        override fun backupEnabled() = backupService.backupEnabled()

        override suspend fun setSmartSuggestionsEnabled(enabled: Boolean) =
            dataStorePreferences.setBoolean(SMART_SUGGESTIONS_ENABLED_KEY, enabled)

    override suspend fun setMessageEffectsEnabled(enabled: Boolean) =
        dataStorePreferences.setBoolean(MESSAGE_EFFECTS_ENABLED_KEY, enabled)

        override suspend fun getAppStorageUsage(): Long =
            fileHelper.getDirectorySize(context.cacheDir) +
                fileHelper.getDirectorySize(context.filesDir)

        override suspend fun getSagaStorageUsage(sagaId: Int): Long = fileHelper.getDirectorySize("${context.filesDir}/sagas/$sagaId")

        override suspend fun wipeAppData() =
            withContext(Dispatchers.IO) {
                context.cacheDir.deleteRecursively()
                context.filesDir.deleteRecursively()
                sagaRepository.deleteAllChats()
            }

        override suspend fun isUserPro(): Boolean = billingService.isPremium()

        override fun getSagas() =
            sagaRepository.getChats().map {
                fileManager.fetchSagasStorage(it.map { saga -> saga.data })
            }

        override suspend fun getStorageBreakdown(): StorageBreakdown = fileManager.getStorageBreakdown()

        override suspend fun clearCache() {
            context.cacheDir.deleteRecursively()
        }

        override suspend fun disableBackup() = backupService.deleteBackup()

        override suspend fun enableBackup(uri: Uri?) = backupService.enableBackup(uri)

        override suspend fun restoreSaga(uri: Uri): RequestResult<SagaContent> =
            executeRequest {
                // 1. Parse Saga from Zip
                val tempSaga =
                    backupService.unzipAndParseSaga(uri) ?: error("Could not parse saga from zip")

                // 2. Restore content using SagaBackupService
                sagaBackupService
                    .restoreContent(
                        RestorableSaga(
                            SagaManifest(
                                sagaId = tempSaga.data.id,
                                title = tempSaga.data.title,
                                description = tempSaga.data.description,
                                genre = tempSaga.data.genre,
                                iconName = tempSaga.data.icon,
                                lastBackup = System.currentTimeMillis(),
                                zipFileName = uri.toString(),
                            ),
                            null,
                        ),
                    ).getSuccess()!!
            }
    }
