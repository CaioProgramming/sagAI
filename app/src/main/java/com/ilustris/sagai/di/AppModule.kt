package com.ilustris.sagai.di

import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.database.DatabaseBuilder
import com.ilustris.sagai.core.database.SagaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Provides
    @Singleton
    fun provideSagaDatabase(databaseBuilder: DatabaseBuilder): SagaDatabase = databaseBuilder.buildDataBase()

    @Provides
    @Singleton
    fun bindsTextGenClient() = TextGenClient()
}
