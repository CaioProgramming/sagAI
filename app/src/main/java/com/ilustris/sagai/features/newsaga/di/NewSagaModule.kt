package com.ilustris.sagai.features.newsaga.di

import com.ilustris.sagai.features.newsaga.data.manager.CharacterStateManager
import com.ilustris.sagai.features.newsaga.data.manager.CharacterStateManagerImpl
import com.ilustris.sagai.features.newsaga.data.manager.SagaStateManager
import com.ilustris.sagai.features.newsaga.data.manager.SagaStateManagerImpl
import com.ilustris.sagai.features.newsaga.data.usecase.NewCharacterUseCase
import com.ilustris.sagai.features.newsaga.data.usecase.NewCharacterUseCaseImpl
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
    abstract fun xbindsNewSagaUseCase(newSagaUseCaseImpl: NewSagaUseCaseImpl): NewSagaUseCase

    @Binds
    abstract fun bindsNewCharacterUseCase(newCharacterUseCaseImpl: NewCharacterUseCaseImpl): NewCharacterUseCase

    @Binds
    abstract fun bindsSagaStateManager(sagaStateManagerImpl: SagaStateManagerImpl): SagaStateManager

    @Binds
    abstract fun bindsCharacterStateManager(characterStateManagerImpl: CharacterStateManagerImpl): CharacterStateManager
}
