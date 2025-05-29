package com.ilustris.sagai.features.newsaga.di

import com.ilustris.sagai.features.chat.data.SagaDao
import com.ilustris.sagai.features.chat.data.SagaDaoImpl
import com.ilustris.sagai.features.chat.repository.SagaRepository
import com.ilustris.sagai.features.chat.repository.SagaRepositoryImpl
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class NewSagaModule {
    @Binds
    abstract fun bindsSagaDao(sagaDaoImpl: SagaDaoImpl): SagaDao

    @Binds
    abstract fun bindsSagaRepository(sagaRepository: SagaRepositoryImpl): SagaRepository

    @Binds
    abstract fun bindsNewSagaUseCase(newSagaUseCaseImpl: NewSagaUseCaseImpl): NewSagaUseCase
}
