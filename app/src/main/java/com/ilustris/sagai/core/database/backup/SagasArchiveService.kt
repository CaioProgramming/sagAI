package com.ilustris.sagai.core.database.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.net.Uri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.database.requireSqliteDatabaseFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * Full `.sagas` backup: the database plus everything under `filesDir/sagas` (images, chat and
 * audiobook audio), so a restore brings back the whole library at once.
 *
 * Needs no persistent storage permission: the caller hands over a one-shot document [Uri].
 */
class SagasArchiveService(
    private val context: Context,
    private val database: SagaDatabase,
    private val preferences: DataStorePreferences,
) {
    companion object {
        private const val LOG_TAG = "SagasArchive"
        private const val MEDIA_DIR = "sagas"
        private const val STAGING_DIR = "restore_staging"
        private const val OLD_SUFFIX = ".pre_restore"
        private const val LAST_BACKUP_AT_KEY = "ARCHIVE_LAST_BACKUP_AT"
        private const val LAST_BACKUP_MESSAGES_KEY = "ARCHIVE_LAST_BACKUP_MESSAGES"
        private const val LAST_REMINDER_AT_KEY = "ARCHIVE_LAST_REMINDER_AT"
        private const val REMINDER_COOLDOWN_MS = 7L * 24 * 60 * 60 * 1000

        /** Debug-only table that holds almost all of the database size and is not worth backing up. */
        private const val EXCLUDED_TABLE = "ai_audit_logs"

        /** Columns that store absolute paths into `filesDir/sagas`. */
        private val PATH_COLUMNS =
            listOf(
                "sagas" to "icon",
                "Chapter" to "coverImage",
                "Characters" to "image",
                "messages" to "audioPath",
                "book_audio_segments" to "audioPath",
                "epilogue_messages" to "audioPath",
            )
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _exportRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emits when something outside the settings screen (e.g. the reminder snackbar) wants the user to pick a destination. */
    val exportRequests = _exportRequests.asSharedFlow()

    fun requestExport() {
        _exportRequests.tryEmit(Unit)
    }

    /** Epoch millis of the last completed backup, 0 when the player never made one. */
    val lastBackupAt: Flow<Long> = preferences.getLong(LAST_BACKUP_AT_KEY)

    /** Nothing Settings or the reminder starts is tied to a screen: the export keeps running if it is left. */
    fun exportInBackground(
        destination: Uri,
        onResult: (RequestResult<ArchiveManifest>) -> Unit,
    ) {
        scope.launch { onResult(export(destination)) }
    }

    /** True at most once a week, and only when there is chat history the last backup does not hold. */
    suspend fun shouldRemind(): Boolean {
        val messages = database.openHelper.readableDatabase.count("messages")
        if (messages <= preferences.getIntNow(LAST_BACKUP_MESSAGES_KEY)) return false
        val last = maxOf(preferences.getLongNow(LAST_BACKUP_AT_KEY), preferences.getLongNow(LAST_REMINDER_AT_KEY))
        return System.currentTimeMillis() - last >= REMINDER_COOLDOWN_MS
    }

    suspend fun markReminded() = preferences.setLong(LAST_REMINDER_AT_KEY, System.currentTimeMillis())

    private val filesDir get() = context.filesDir
    private val mediaDir get() = File(filesDir, MEDIA_DIR)
    private fun dbFile() = context.getDatabasePath(SagaDatabase.NAME)

    suspend fun export(
        destination: Uri,
        onProgress: (Float) -> Unit = {},
    ): RequestResult<ArchiveManifest> =
        databaseBackupLock.withLock {
            executeRequest {
                withContext(Dispatchers.IO) { performExport(destination, onProgress) }
            }.also { logFailure("export", it) }
        }

    suspend fun peek(source: Uri): RequestResult<ArchiveManifest> =
        executeRequest {
            withContext(Dispatchers.IO) { SagasArchive.readManifest(openInput(source)) }
        }

    suspend fun restore(
        source: Uri,
        onProgress: (Float) -> Unit = {},
    ): RequestResult<Unit> =
        databaseBackupLock.withLock {
            executeRequest {
                withContext(Dispatchers.IO) { performRestore(source, onProgress) }
            }.also { logFailure("restore", it) }
        }

    private fun performExport(
        destination: Uri,
        onProgress: (Float) -> Unit,
    ): ArchiveManifest {
        FirebaseCrashlytics.getInstance().log("sagasArchive_export_start")
        val live = database.openHelper.writableDatabase
        live.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }

        val snapshotDir = File(context.cacheDir, "sagas_export").apply { deleteRecursively() }
        snapshotDir.mkdirs()
        try {
            val snapshot = File(snapshotDir, SagaDatabase.NAME)
            check(context.cacheDir.usableSpace > dbFile().length()) { "Not enough free space" }
            dbFile().copyTo(snapshot)
            File(dbFile().path + "-wal").takeIf { it.exists() }?.copyTo(File(snapshot.path + "-wal"))
            trimSnapshot(snapshot)

            val manifest =
                ArchiveManifest(
                    formatVersion = SagasArchive.FORMAT_VERSION,
                    dbVersion = live.version,
                    appVersion = BuildConfig.VERSION_NAME,
                    createdAt = System.currentTimeMillis(),
                    sagaCount = live.count("sagas"),
                    messageCount = live.count("messages"),
                    totalBytes = snapshot.length() + mediaDir.walkTopDown().filter { it.isFile }.sumOf { it.length() },
                )

            val output =
                context.contentResolver.openOutputStream(destination, "w")
                    ?: error("Could not open the destination file")
            output.use {
                SagasArchive.write(it, manifest, snapshot, filesDir, MEDIA_DIR) { written ->
                    onProgress((written.toFloat() / manifest.totalBytes.coerceAtLeast(1)).coerceAtMost(1f))
                }
            }
            FirebaseCrashlytics.getInstance().log("sagasArchive_export_success")
            runBlocking {
                preferences.setLong(LAST_BACKUP_AT_KEY, manifest.createdAt)
                preferences.setInt(LAST_BACKUP_MESSAGES_KEY, manifest.messageCount)
            }
            return manifest
        } finally {
            snapshotDir.deleteRecursively()
        }
    }

    /** Drops the excluded table and leaves a single-file database (no wal) ready to be zipped. */
    private fun trimSnapshot(snapshot: File) {
        SQLiteDatabase.openDatabase(snapshot.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            db.rawQuery("PRAGMA journal_mode=DELETE", null).use { it.moveToFirst() }
            db.execSQL("DELETE FROM $EXCLUDED_TABLE")
            db.execSQL("VACUUM")
        }
        File(snapshot.path + "-wal").delete()
        File(snapshot.path + "-shm").delete()
    }

    private fun performRestore(
        source: Uri,
        onProgress: (Float) -> Unit,
    ) {
        FirebaseCrashlytics.getInstance().log("sagasArchive_restore_start")
        val manifest = SagasArchive.readManifest(openInput(source))
        check(manifest.formatVersion <= SagasArchive.FORMAT_VERSION) { "Backup needs a newer app version" }
        check(manifest.dbVersion <= database.openHelper.readableDatabase.version) {
            "Backup needs a newer app version"
        }
        check(filesDir.usableSpace > manifest.totalBytes + manifest.totalBytes / 10) { "Not enough free space" }

        val staging = File(filesDir, STAGING_DIR).apply { deleteRecursively() }
        staging.mkdirs()
        try {
            SagasArchive.extract(openInput(source), staging) { read ->
                onProgress((read.toFloat() / manifest.totalBytes.coerceAtLeast(1)).coerceAtMost(1f))
            }

            val stagedDb = File(staging, SagasArchive.DB_PREFIX + SagaDatabase.NAME)
            requireSqliteDatabaseFile(stagedDb)
            prepareStagedDatabase(stagedDb)

            swapIn(stagedDb, File(staging, SagasArchive.FILES_PREFIX + MEDIA_DIR))
            FirebaseCrashlytics.getInstance().log("sagasArchive_restore_success")
        } finally {
            staging.deleteRecursively()
        }
    }

    /** Points the stored media paths at this install's `filesDir`, whatever user or device wrote them. */
    private fun prepareStagedDatabase(stagedDb: File) {
        SQLiteDatabase.openDatabase(stagedDb.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            PATH_COLUMNS.forEach { (table, column) ->
                try {
                    db.execSQL(
                        "UPDATE $table SET $column = ? || substr($column, instr($column, '/files/') + 6) " +
                            "WHERE $column LIKE '/data/%/files/$MEDIA_DIR/%'",
                        arrayOf(filesDir.absolutePath),
                    )
                } catch (e: SQLiteException) {
                    Timber.tag(LOG_TAG).w(e, "Skipping $table.$column")
                }
            }
            db.rawQuery("PRAGMA journal_mode=DELETE", null).use { it.moveToFirst() }
        }
    }

    /** Moves the restored data in; the previous data is only deleted once the swap succeeded. */
    private fun swapIn(
        stagedDb: File,
        stagedMedia: File,
    ) {
        val db = dbFile()
        val dbFiles = listOf(db, File(db.path + "-wal"), File(db.path + "-shm"))
        val oldMedia = File(filesDir, MEDIA_DIR + OLD_SUFFIX).apply { deleteRecursively() }

        database.close()
        dbFiles.forEach { File(it.path + OLD_SUFFIX).delete() }

        try {
            dbFiles.filter { it.exists() }.forEach { check(it.renameTo(File(it.path + OLD_SUFFIX))) }
            if (mediaDir.exists()) check(mediaDir.renameTo(oldMedia))
            if (stagedMedia.exists()) check(stagedMedia.renameTo(mediaDir)) else mediaDir.mkdirs()
            if (!stagedDb.renameTo(db)) stagedDb.copyTo(db, overwrite = true)
        } catch (e: Exception) {
            rollback(dbFiles, oldMedia)
            throw e
        }

        dbFiles.forEach { File(it.path + OLD_SUFFIX).delete() }
        oldMedia.deleteRecursively()
    }

    private fun rollback(
        dbFiles: List<File>,
        oldMedia: File,
    ) {
        dbFiles.forEach { it.delete() }
        dbFiles.forEach { File(it.path + OLD_SUFFIX).renameTo(it) }
        if (oldMedia.exists()) {
            mediaDir.deleteRecursively()
            oldMedia.renameTo(mediaDir)
        }
    }

    private fun openInput(uri: Uri) =
        context.contentResolver.openInputStream(uri) ?: error("Could not open the backup file")

    private fun androidx.sqlite.db.SupportSQLiteDatabase.count(table: String): Int =
        query("SELECT COUNT(*) FROM $table").use { if (it.moveToFirst()) it.getInt(0) else 0 }

    private fun logFailure(
        step: String,
        result: RequestResult<*>,
    ) {
        if (result.isFailure) Timber.tag(LOG_TAG).e(result.error.value, "$step failed")
    }
}
