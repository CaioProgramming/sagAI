package com.ilustris.sagai.features.settings.domain

import android.content.Context
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

interface SettingsUseCase {
    fun getNotificationsEnabled(): Flow<Boolean>

    suspend fun setNotificationsEnabled(enabled: Boolean)

    fun getSmartSuggestionsEnabled(): Flow<Boolean>

    suspend fun setSmartSuggestionsEnabled(enabled: Boolean)

    suspend fun getAppStorageUsage(): Long

    suspend fun getSagaStorageUsage(sagaId: Int): Long

    suspend fun wipeAppData()

    suspend fun isUserPro(): Boolean

    suspend fun getSagas(): Flow<List<SagaContent>>

    suspend fun getStorageBreakdown(): StorageBreakdown

    suspend fun clearCache()
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
    ) : SettingsUseCase {
        companion object {
            const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
            const val SMART_SUGGESTIONS_ENABLED_KEY = "smart_suggestions_enabled"
        }

        override fun getNotificationsEnabled(): Flow<Boolean> = dataStorePreferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, true)

        override suspend fun setNotificationsEnabled(enabled: Boolean) = dataStorePreferences.setBoolean(NOTIFICATIONS_ENABLED_KEY, enabled)

        override fun getSmartSuggestionsEnabled(): Flow<Boolean> = dataStorePreferences.getBoolean(SMART_SUGGESTIONS_ENABLED_KEY, true)

        override suspend fun setSmartSuggestionsEnabled(enabled: Boolean) =
            dataStorePreferences.setBoolean(SMART_SUGGESTIONS_ENABLED_KEY, enabled)

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

        override suspend fun getSagas() = sagaRepository.getChats()

        override suspend fun getStorageBreakdown(): StorageBreakdown =
            withContext(Dispatchers.IO) {
                val cacheSize = fileHelper.getDirectorySize(context.cacheDir)
                val sagaRoot = File(context.filesDir, "sagas")
                val sagaContentSize = fileHelper.getDirectorySize(sagaRoot)
                val totalSize = getAppStorageUsage()
                val otherSize = totalSize - cacheSize - sagaContentSize
                StorageBreakdown(cacheSize, sagaContentSize, otherSize)
            }

        override suspend fun clearCache() {
            context.cacheDir.deleteRecursively()
        }
    }
