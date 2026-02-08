package com.ilustris.sagai.core.database

import android.content.Context
import androidx.room.Room
import com.ilustris.sagai.core.datastore.DataStorePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DatabaseBuilder
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val preferences: DataStorePreferences,
    ) {
        fun buildDataBase(): SagaDatabase {
            val builder =
                Room
                    .databaseBuilder(
                        context = context,
                        klass = SagaDatabase::class.java,
                        name = SagaDatabase::class.java.simpleName,
                    ).addMigrations(*DatabaseMigrations.getAllMigrations())
                    .fallbackToDestructiveMigration(false)

            val callback = DatabaseCallback(context, preferences)
            builder.addCallback(callback)

            val database = builder.build()
            callback.database = database
        return database
    }
    }
