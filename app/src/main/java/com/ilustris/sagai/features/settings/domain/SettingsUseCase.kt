package com.ilustris.sagai.features.settings.domain

import android.Manifest
import android.content.Context
import android.net.Uri
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.database.backup.DatabaseBackupService
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.file.FileManager
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.core.permissions.PermissionStatus
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.settings.ui.SagaStorageInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface SettingsUseCase {
    fun getNotificationsEnabled(): Flow<Boolean>

    fun getSmartSuggestionsEnabled(): Flow<Boolean>

    fun getMessageEffectsEnabled(): Flow<Boolean>

    fun getShowTutorials(): Flow<Boolean>

    fun backupEnabled(): Flow<Boolean>

    suspend fun setSmartSuggestionsEnabled(enabled: Boolean)

    suspend fun setMessageEffectsEnabled(enabled: Boolean)

    suspend fun setShowTutorials(enabled: Boolean)

    suspend fun getAppStorageUsage(): Long

    suspend fun getSagaStorageUsage(sagaId: Int): Long

    suspend fun wipeAppData()

    suspend fun isUserPro(): Boolean

    fun getSagas(): Flow<List<SagaStorageInfo>>

    suspend fun getStorageBreakdown(): StorageBreakdown

    suspend fun clearCache()

    suspend fun disableBackup()

    suspend fun enableBackup(uri: Uri?): RequestResult<Unit>

    suspend fun hasSagasWithChapters(): Boolean

    suspend fun exportDatabase(destinationUri: Uri): RequestResult<Unit>

    suspend fun importDatabase(sourceUri: Uri): RequestResult<Unit>

    suspend fun clearPreferences()

    fun getMusicEnabled(): Flow<Boolean>

    suspend fun setMusicEnabled(enabled: Boolean)
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
        private val databaseBackupService: DatabaseBackupService,
        private val database: SagaDatabase,
    ) : SettingsUseCase {
        companion object {
            const val SMART_SUGGESTIONS_ENABLED_KEY = "smart_suggestions_enabled"
            const val MESSAGE_EFFECTS_ENABLED_KEY = "message_effects_enabled"
            const val TUTORIALS_ENABLED_KEY = "tutorials_enabled"
            const val MUSIC_ENABLED_KEY = "music_enabled"
        }

        override fun getNotificationsEnabled(): Flow<Boolean> =
            permissionService
                .observePermission(Manifest.permission.POST_NOTIFICATIONS)
                .map { it == PermissionStatus.GRANTED }

        override fun getSmartSuggestionsEnabled(): Flow<Boolean> = dataStorePreferences.getBoolean(SMART_SUGGESTIONS_ENABLED_KEY, true)

        override fun getMessageEffectsEnabled(): Flow<Boolean> = dataStorePreferences.getBoolean(MESSAGE_EFFECTS_ENABLED_KEY, true)

        override fun getShowTutorials(): Flow<Boolean> = dataStorePreferences.getBoolean(TUTORIALS_ENABLED_KEY, true)

        override fun backupEnabled() = backupService.backupEnabled()

        override suspend fun setSmartSuggestionsEnabled(enabled: Boolean) =
            dataStorePreferences.setBoolean(SMART_SUGGESTIONS_ENABLED_KEY, enabled)

        override suspend fun setMessageEffectsEnabled(enabled: Boolean) =
            dataStorePreferences.setBoolean(MESSAGE_EFFECTS_ENABLED_KEY, enabled)

    override suspend fun setShowTutorials(enabled: Boolean) = dataStorePreferences.setBoolean(TUTORIALS_ENABLED_KEY, enabled)

        override fun getMusicEnabled(): Flow<Boolean> = dataStorePreferences.getBoolean(MUSIC_ENABLED_KEY, true)

        override suspend fun setMusicEnabled(enabled: Boolean) = dataStorePreferences.setBoolean(MUSIC_ENABLED_KEY, enabled)

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

        override suspend fun hasSagasWithChapters(): Boolean =
            withContext(Dispatchers.IO) {
                val sagas = sagaRepository.getChats().firstOrNull() ?: return@withContext false
                sagas.any { saga ->
                    saga.acts.any { act -> act.chapters.isNotEmpty() }
                }
            }

        override suspend fun exportDatabase(destinationUri: Uri): RequestResult<Unit> =
            executeRequest {
                database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")

                val dbFile = context.getDatabasePath("SagaDatabase")
                if (!dbFile.exists()) error("Database file not found")

                context.contentResolver.openOutputStream(destinationUri, "w")?.use { output ->
                    dbFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                } ?: error("Could not open output stream for destination URI")
            }

        override suspend fun importDatabase(sourceUri: Uri): RequestResult<Unit> =
            executeRequest {
                val backupResult = databaseBackupService.createBackup()
                if (backupResult.isFailure) {
                    error("Failed to create backup before import: ${backupResult.exceptionOrNull()?.message}")
                }

                database.close()

                val dbFile = context.getDatabasePath("SagaDatabase")
                val walFile = java.io.File(dbFile.path + "-wal")
                val shmFile = java.io.File(dbFile.path + "-shm")

                walFile.delete()
                shmFile.delete()

                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: error("Could not open input stream for source URI")
            }

        override suspend fun clearPreferences() {
            dataStorePreferences.clearAll()
        }
    }
