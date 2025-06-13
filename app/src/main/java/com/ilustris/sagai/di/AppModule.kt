package com.ilustris.sagai.di

import android.content.Context
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.database.DatabaseBuilder
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.network.CloudflareApiService
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepositoryImpl
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCaseImpl
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import com.ilustris.sagai.features.characters.domain.CharacterUseCaseImpl
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.characters.repository.CharacterRepositoryImpl
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCaseImpl
import com.ilustris.sagai.features.saga.chat.domain.usecase.MessageUseCase
import com.ilustris.sagai.features.saga.chat.domain.usecase.MessageUseCaseImpl
import com.ilustris.sagai.features.saga.chat.repository.MessageRepository
import com.ilustris.sagai.features.saga.chat.repository.MessageRepositoryImpl
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.saga.chat.repository.SagaRepositoryImpl
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    // TODO: Replace with your actual Cloudflare Base URL
    private const val CLOUDFLARE_BASE_URL = "YOUR_CLOUDFLARE_BASE_URL_HERE"

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

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Or Level.BASIC, Level.HEADERS
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            // Add other interceptors or configurations as needed
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(CLOUDFLARE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Using Gson as an example
            .build()
    }

    @Provides
    @Singleton
    fun provideCloudflareApiService(retrofit: Retrofit): CloudflareApiService {
        return retrofit.create(CloudflareApiService::class.java)
    }
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

    @Binds
    abstract fun providesSagaDetailUseCase(sagaDetailUseCaseImpl: SagaDetailUseCaseImpl): SagaDetailUseCase
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
