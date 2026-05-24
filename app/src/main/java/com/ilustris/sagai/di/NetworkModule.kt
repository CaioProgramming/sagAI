package com.ilustris.sagai.di

import com.ilustris.sagai.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/** OkHttp client for binary downloads (fonts, audio) — no HTTP logging. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DownloadOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val AUDIO_TIMEOUT_SECONDS = 120L

    @Provides
    @Singleton
    @DownloadOkHttpClient
    fun provideDownloadOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder =
            OkHttpClient
                .Builder()
                .connectTimeout(AUDIO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(AUDIO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(AUDIO_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor { chain ->
                val request = chain.request()
                val isStreaming = request.url.encodedPath.contains("streamGenerateContent")
                HttpLoggingInterceptor().apply {
                    level =
                        if (isStreaming) {
                            HttpLoggingInterceptor.Level.HEADERS
                        } else {
                            HttpLoggingInterceptor.Level.BODY
                        }
                    redactHeader("x-goog-api-key")
                }.intercept(chain)
            }
        }

        return builder.build()
    }
}
