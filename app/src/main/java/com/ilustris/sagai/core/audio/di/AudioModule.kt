package com.ilustris.sagai.core.audio.di

import android.content.Context
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.audio.AudioPermissionManager
import com.ilustris.sagai.core.audio.AudioService
import com.ilustris.sagai.core.audio.AudioTranscriptionService
import com.ilustris.sagai.core.permissions.PermissionService
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
        @ApplicationContext
        context: Context,
        permissionService: PermissionService,
        gemmaClient: GemmaClient,
    ): AudioService = AudioService(context, permissionService, gemmaClient)

    @Singleton
    @Provides
    fun provideAudioPermissionManager(permissionManager: AudioPermissionManager): AudioPermissionManager = permissionManager

    @Singleton
    @Provides
    fun provideAudioTranscriptionService(gemmaClient: GemmaClient): AudioTranscriptionService = AudioTranscriptionService(gemmaClient)
}
