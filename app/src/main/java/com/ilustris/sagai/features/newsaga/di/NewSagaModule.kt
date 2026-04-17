package com.ilustris.sagai.features.newsaga.di

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
    abstract fun bindsNewSagaUseCase(newSagaUseCaseImpl: NewSagaUseCaseImpl): NewSagaUseCase
}
