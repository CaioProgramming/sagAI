package com.ilustris.sagai.core.database.backup

import android.content.Context
import android.net.Uri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.database.requireSqliteDatabaseFile
import com.ilustris.sagai.core.file.copyContentUriToFile
import com.ilustris.sagai.core.datastore.DataStorePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseBackupService(
    private val context: Context,
    private val preferences: DataStorePreferences,
    private val database: SagaDatabase,
) {
    companion object {
        private const val BACKUP_DIR = "backups"
        private const val METADATA_FILE = "backup_metadata.json"
        private const val MAX_BACKUPS = 2
        private const val LOG_TAG = "DatabaseBackup"
    }

    private fun getBackupDir(): File {
        val dir = File(context.filesDir, BACKUP_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun dbFile(): File = context.getDatabasePath(SagaDatabase.NAME)

    private suspend fun performCreateBackup(): BackupMetadata {
        logOperation("createBackup_start")
        val currentVersion = database.openHelper.readableDatabase.version

        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val sourceDb = dbFile()
        val backupDir = getBackupDir()

        val backupDb = File(backupDir, "backup_v${currentVersion}_$timestamp.db")
        val backupWal = File(backupDir, "backup_v${currentVersion}_$timestamp.db-wal")
        val backupShm = File(backupDir, "backup_v${currentVersion}_$timestamp.db-shm")

        sourceDb.copyTo(backupDb, overwrite = true)

        File(sourceDb.path + "-wal").let {
            if (it.exists()) it.copyTo(backupWal, overwrite = true)
        }
        File(sourceDb.path + "-shm").let {
            if (it.exists()) it.copyTo(backupShm, overwrite = true)
        }

        val checksum = calculateChecksum(backupDb)

        val metadata =
            BackupMetadata(
                version = currentVersion,
                appVersion = BuildConfig.VERSION_NAME,
                timestamp = System.currentTimeMillis(),
                filePath = backupDb.name,
                fileSize = backupDb.length(),
                checksum = checksum,
            )

        saveMetadata(metadata)
        cleanOldBackups()
        logOperation("createBackup_success", metadata.fileSize)
        return metadata
    }

    suspend fun createBackup() =
        databaseBackupLock.withLock {
            executeRequest { performCreateBackup() }.also { result ->
                if (result.isFailure) {
                    Timber.tag(LOG_TAG).e(result.error.value, "createBackup failed")
                }
            }
        }

    suspend fun restoreBackup(metadata: BackupMetadata) =
        databaseBackupLock.withLock {
            executeRequest {
                logOperation("restoreBackup_start", metadata.fileSize)
                val backupDb = File(getBackupDir(), metadata.filePath)
                if (!backupDb.exists()) error("Backup file not found")

                val checksum = calculateChecksum(backupDb)
                if (checksum != metadata.checksum) error("Backup file corrupted")

                database.close()

                val currentDb = dbFile()
                val currentWal = File(currentDb.path + "-wal")
                val currentShm = File(currentDb.path + "-shm")

                currentDb.delete()
                currentWal.delete()
                currentShm.delete()

                backupDb.copyTo(currentDb, overwrite = true)

                val backupWal = File(backupDb.path + "-wal")
                val backupShm = File(backupDb.path + "-shm")
                if (backupWal.exists()) backupWal.copyTo(currentWal, overwrite = true)
                if (backupShm.exists()) backupShm.copyTo(currentShm, overwrite = true)
                logOperation("restoreBackup_success")
            }.also { result ->
                if (result.isFailure) {
                    Timber.tag(LOG_TAG).e(result.error.value, "restoreBackup failed")
                }
            }
        }

    suspend fun importDatabaseFromUri(sourceUri: Uri): RequestResult<Unit> =
        databaseBackupLock.withLock {
            executeRequest {
                FirebaseCrashlytics.getInstance().log("importDatabase_start")
                performCreateBackup()

                val pendingImport = File(context.cacheDir, "pending_db_import.db")
                try {
                    val uriInfo = copyContentUriToFile(context, sourceUri, pendingImport)
                    FirebaseCrashlytics.getInstance().apply {
                        setCustomKey("import_db_bytes", pendingImport.length())
                        uriInfo.displayName?.let { setCustomKey("import_db_name", it) }
                        uriInfo.reportedSizeBytes?.let { setCustomKey("import_db_reported_size", it) }
                    }

                    requireSqliteDatabaseFile(pendingImport)

                    val dbFile = dbFile()
                    val walFile = File(dbFile.path + "-wal")
                    val shmFile = File(dbFile.path + "-shm")

                    database.close()

                    if (walFile.exists()) walFile.delete()
                    if (shmFile.exists()) shmFile.delete()
                    if (dbFile.exists()) dbFile.delete()

                    pendingImport.copyTo(dbFile, overwrite = true)
                    FirebaseCrashlytics.getInstance().log("importDatabase_success")
                } finally {
                    pendingImport.delete()
                }
            }.also { result ->
                if (result.isFailure) {
                    Timber.tag(LOG_TAG).e(result.error.value, "importDatabase failed")
                }
            }
        }

    suspend fun clearDatabase() =
        databaseBackupLock.withLock {
            executeRequest {
                logOperation("clearDatabase_start")
                database.close()
                val dbFile = dbFile()
                val walFile = File(dbFile.path + "-wal")
                val shmFile = File(dbFile.path + "-shm")

                if (dbFile.exists()) dbFile.delete()
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()
                logOperation("clearDatabase_success")
            }.also { result ->
                if (result.isFailure) {
                    Timber.tag(LOG_TAG).e(result.error.value, "clearDatabase failed")
                }
            }
        }

    private fun logOperation(
        step: String,
        fileSize: Long? = null,
    ) {
        FirebaseCrashlytics.getInstance().apply {
            log(step)
            setCustomKey("db_backup_step", step)
            fileSize?.let { setCustomKey("db_backup_file_size", it) }
        }
    }

    private fun calculateChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead = input.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun saveMetadata(newBackup: BackupMetadata) {
        val metadataFile = File(getBackupDir(), METADATA_FILE)
        val gson = Gson()

        val allBackups =
            if (metadataFile.exists()) {
                val json = metadataFile.readText()
                val container = gson.fromJson(json, BackupContainer::class.java)
                container.backups.toMutableList()
            } else {
                mutableListOf()
            }

        allBackups.add(newBackup)
        allBackups.sortByDescending { it.timestamp }

        val container =
            BackupContainer(
                backups = allBackups.take(MAX_BACKUPS),
                maxBackups = MAX_BACKUPS,
            )

        metadataFile.writeText(gson.toJson(container))
    }

    private fun cleanOldBackups() {
        val metadataFile = File(getBackupDir(), METADATA_FILE)
        if (!metadataFile.exists()) return

        val gson = Gson()
        val container = gson.fromJson(metadataFile.readText(), BackupContainer::class.java)
        val backupDir = getBackupDir()

        backupDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("backup_") &&
                container.backups.none { it.filePath == file.name }
            ) {
                file.delete()
            }
        }
    }

    suspend fun getAllBackups(): List<BackupMetadata> {
        return withContext(Dispatchers.IO) {
            val metadataFile = File(getBackupDir(), METADATA_FILE)
            if (!metadataFile.exists()) return@withContext emptyList()

            val gson = Gson()
            val container = gson.fromJson(metadataFile.readText(), BackupContainer::class.java)
            container.backups
        }
    }
}

data class BackupMetadata(
    val version: Int,
    val appVersion: String,
    val timestamp: Long,
    val filePath: String,
    val fileSize: Long,
    val checksum: String,
)

data class BackupContainer(
    val backups: List<BackupMetadata>,
    val maxBackups: Int,
)
