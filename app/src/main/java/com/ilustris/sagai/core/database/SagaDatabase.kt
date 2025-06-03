package com.ilustris.sagai.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ilustris.sagai.features.chat.data.MessageDao
import com.ilustris.sagai.features.chat.data.SagaDao
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.home.data.model.SagaData

@Database(entities = [SagaData::class, Message::class], version = 4, exportSchema = false)
abstract class SagaDatabase : RoomDatabase() {
    abstract fun sagaDao(): SagaDao
    abstract fun messageDao(): MessageDao
}
