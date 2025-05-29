package com.ilustris.sagai.features.home.di

import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
abstract class HomeModule {
    @Binds
    abstract fun providesSagaHistoryUseCase(sagaHistoryUseCaseImpl: SagaHistoryUseCaseImpl): SagaHistoryUseCase
}
