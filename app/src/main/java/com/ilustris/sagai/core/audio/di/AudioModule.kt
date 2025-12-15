package com.ilustris.sagai.core.audio.di

import com.ilustris.sagai.core.audio.AudioPermissionManager
import com.ilustris.sagai.core.audio.AudioService
import com.ilustris.sagai.core.file.FileCacheService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    @Singleton
    @Provides
    fun provideAudioService(fileCacheService: FileCacheService): AudioService = AudioService(fileCacheService)

    @Singleton
    @Provides
    fun provideAudioPermissionManager(permissionManager: AudioPermissionManager): AudioPermissionManager = permissionManager
}
