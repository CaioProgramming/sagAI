package com.ilustris.sagai.di

import com.ilustris.sagai.core.network.GeminiApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"

    // Timeout for audio generation (can take longer than typical requests)
    private const val AUDIO_TIMEOUT_SECONDS = 120L

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): okhttp3.Interceptor =
        okhttp3.Interceptor { chain ->
            val request = chain.request()
            val isStreaming = request.url.encodedPath.contains("streamGenerateContent")
            val logger =
                HttpLoggingInterceptor().apply {
                    level =
                        if (isStreaming) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.BODY
                    redactHeader("x-goog-api-key")
        }
        logger.intercept(chain)
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: okhttp3.Interceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(AUDIO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(AUDIO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(AUDIO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @GeminiRetrofit
    fun provideGeminiRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(GEMINI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideGeminiApiService(
        @GeminiRetrofit retrofit: Retrofit,
    ): GeminiApiService = retrofit.create(GeminiApiService::class.java)
}
