package com.ilustris.sagai.di

import android.content.Context
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.database.DatabaseBuilder
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepositoryImpl
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCaseImpl
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import com.ilustris.sagai.features.characters.domain.CharacterUseCaseImpl
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.characters.repository.CharacterRepositoryImpl
import com.ilustris.sagai.features.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.chat.data.usecase.MessageUseCaseImpl
import com.ilustris.sagai.features.chat.repository.MessageRepository
import com.ilustris.sagai.features.chat.repository.MessageRepositoryImpl
import com.ilustris.sagai.features.chat.repository.SagaRepository
import com.ilustris.sagai.features.chat.repository.SagaRepositoryImpl
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun bindsImagenClient() = ImagenClient()

    @Provides
    @Singleton
    fun bindsFileHelper(
        @ApplicationContext context: Context,
    ) = FileHelper(context)
}

@InstallIn(ViewModelComponent::class)
@Module
abstract class UseCaseModule {
    @Binds
    abstract fun providesSagaHistoryUseCase(sagaHistoryUseCaseImpl: SagaHistoryUseCaseImpl): SagaHistoryUseCase

    @Binds
    abstract fun providesMessageUseCase(messageUseCaseImpl: MessageUseCaseImpl): MessageUseCase

    @Binds
    abstract fun providesChapterUseCase(chapterUseCaseImpl: ChapterUseCaseImpl): ChapterUseCase

    @Binds
    abstract fun providesCharacterUseCase(characterUseCaseImpl: CharacterUseCaseImpl): CharacterUseCase
}

@InstallIn(ViewModelComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindsSagaRepository(sagaRepositoryImpl: SagaRepositoryImpl): SagaRepository

    @Binds
    abstract fun bindsMessageRepository(messageRepositoryImpl: MessageRepositoryImpl): MessageRepository

    @Binds
    abstract fun bindsChapterRepository(chapterRepositoryImpl: ChapterRepositoryImpl): ChapterRepository

    @Binds
    abstract fun bindsCharacterRepository(characterRepositoryImpl: CharacterRepositoryImpl): CharacterRepository
}
