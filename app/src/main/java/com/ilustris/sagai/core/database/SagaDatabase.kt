package com.ilustris.sagai.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ilustris.sagai.features.chat.data.ChatDao
import com.ilustris.sagai.features.home.data.model.ChatData

@Database(entities = [ChatData::class], version = 1)
abstract class SagaDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
