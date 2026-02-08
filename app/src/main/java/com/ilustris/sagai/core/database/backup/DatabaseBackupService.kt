package com.ilustris.sagai.core.database.backup

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.datastore.DataStorePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        private const val DB_NAME = "SagaDatabase"
        private const val BACKUP_DIR = "backups"
        private const val METADATA_FILE = "backup_metadata.json"
        private const val MAX_BACKUPS = 2
    }

    private fun getBackupDir(): File {
        val dir = File(context.filesDir, BACKUP_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun createBackup(): Result<BackupMetadata> =
        withContext(Dispatchers.IO) {
            try {
                val currentVersion = database.openHelper.readableDatabase.version

                database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val dbFile = context.getDatabasePath(DB_NAME)
                val backupDir = getBackupDir()

                val backupDb = File(backupDir, "backup_v${currentVersion}_$timestamp.db")
                val backupWal = File(backupDir, "backup_v${currentVersion}_$timestamp.db-wal")
                val backupShm = File(backupDir, "backup_v${currentVersion}_$timestamp.db-shm")

                dbFile.copyTo(backupDb, overwrite = true)

                File(dbFile.path + "-wal").let {
                    if (it.exists()) it.copyTo(backupWal, overwrite = true)
                }
                File(dbFile.path + "-shm").let {
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

                Result.success(metadata)
            } catch (e: Exception) {
                Log.e("DatabaseBackup", "Backup failed", e)
                Result.failure(e)
            }
        }

    suspend fun restoreBackup(metadata: BackupMetadata): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val backupDb = File(getBackupDir(), metadata.filePath)
                if (!backupDb.exists()) error("Backup file not found")

                val checksum = calculateChecksum(backupDb)
                if (checksum != metadata.checksum) error("Backup file corrupted")

                database.close()

                val currentDb = context.getDatabasePath(DB_NAME)
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

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("DatabaseBackup", "Restore failed", e)
                Result.failure(e)
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
