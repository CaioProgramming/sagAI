package com.ilustris.sagai.core.database

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ilustris.sagai.core.database.backup.DatabaseBackupService
import com.ilustris.sagai.core.datastore.DataStorePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseCallback(
    private val context: Context,
    private val preferences: DataStorePreferences,
) : RoomDatabase.Callback() {
    lateinit var database: SagaDatabase

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)

        val prefs = context.getSharedPreferences("db_version", Context.MODE_PRIVATE)
        val lastKnownVersion = prefs.getInt("last_version", -1)
        val currentVersion = db.version

        if (lastKnownVersion != -1 && lastKnownVersion != currentVersion) {
            Log.i(
                "DatabaseCallback",
                "Version change detected: $lastKnownVersion → $currentVersion",
            )

            CoroutineScope(Dispatchers.IO).launch {
                val backupService = DatabaseBackupService(context, preferences, database)
                val result = backupService.createBackup()

                if (result.isSuccess) {
                    Log.i("DatabaseCallback", "Auto-backup created successfully")
                } else {
                    Log.e("DatabaseCallback", "Auto-backup failed", result.exceptionOrNull())
                }
            }
        }

        prefs.edit().putInt("last_version", currentVersion).apply()
    }
}
