package com.ilustris.sagai.di

import com.ilustris.sagai.core.lifecycle.AppLifecycleManager
import com.ilustris.sagai.core.lifecycle.AppLifecycleManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LifecycleModule {

    @Binds
    @Singleton
    abstract fun bindAppLifecycleManager(
        appLifecycleManagerImpl: AppLifecycleManagerImpl
    ): AppLifecycleManager
}
