package com.ilustris.sagai.core.database

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DatabaseBuilder
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun buildDataBase(): SagaDatabase =
            Room
                .databaseBuilder(
                    context = context,
                    klass = SagaDatabase::class.java,
                    name = SagaDatabase::class.java.simpleName,
                ).fallbackToDestructiveMigration()
                .build()
    }
