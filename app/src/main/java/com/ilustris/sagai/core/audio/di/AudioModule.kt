package com.ilustris.sagai.core.audio.di

import android.content.Context
import com.ilustris.sagai.core.audio.AudioPermissionManager
import com.ilustris.sagai.core.audio.AudioService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    @Singleton
    @Provides
    fun provideAudioService(
        @ApplicationContext context: Context,
    ): AudioService = AudioService(context.cacheDir)

    @Singleton
    @Provides
    fun provideAudioPermissionManager(
        @ApplicationContext context: Context,
    ): AudioPermissionManager = AudioPermissionManager(context)
}
