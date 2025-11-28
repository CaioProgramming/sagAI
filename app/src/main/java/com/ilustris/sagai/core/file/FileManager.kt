package com.ilustris.sagai.core.file

import android.content.Context
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.settings.domain.StorageBreakdown
import com.ilustris.sagai.features.settings.ui.SagaStorageInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileManager(
    private val fileHelper: FileHelper,
    @ApplicationContext private val context: Context,
) {
    fun getAppStorageUsage(): Long =
        fileHelper.getDirectorySize(context.cacheDir) +
            fileHelper.getDirectorySize(context.filesDir)

    fun getSagaStorageUsage(sagaId: Int): Long = fileHelper.getDirectorySize("${context.filesDir}/sagas/$sagaId")

    fun fetchSagasStorage(sagas: List<Saga>) =
        sagas.map {
            SagaStorageInfo(
                it,
                getSagaStorageUsage(it.id),
            )
        }

    suspend fun getStorageBreakdown(): StorageBreakdown {
        val cacheSize = fileHelper.getDirectorySize(context.cacheDir)
        val sagaRoot = File(context.filesDir, "sagas")
        val sagaContentSize = fileHelper.getDirectorySize(sagaRoot)
        val totalSize = getAppStorageUsage()
        val otherSize = totalSize - cacheSize - sagaContentSize
        return StorageBreakdown(cacheSize, sagaContentSize, otherSize)
    }
}
