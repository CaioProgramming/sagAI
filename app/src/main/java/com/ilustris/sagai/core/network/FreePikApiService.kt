package com.ilustris.sagai.core.network

import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.network.body.FreepikRequest
import com.ilustris.sagai.core.network.response.FreePikResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface FreePikApi {
    companion object {
        const val BASE_URL = "https://api.freepik.com/v1/ai/"
    }

    @POST("text-to-image")
    suspend fun generateImage(
        @Body body: FreepikRequest,
    ): FreePikResponse
}

class FreePikApiService {
    private val api: FreePikApi by lazy {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        Retrofit
            .Builder()
            .client(
                OkHttpClient
                    .Builder()
                    .addInterceptor(logging)
                    .addInterceptor(
                        Interceptor { chain ->
                            val request =
                                chain
                                    .request()
                                    .newBuilder()
                                    .addHeader("x-freepik-api-key", BuildConfig.APIKEY)
                                    .build()
                            chain.proceed(request)
                        },
                    ).build(),
            ).baseUrl(FreePikApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FreePikApi::class.java)
    }

    suspend fun generateImage(promptRequest: FreepikRequest) = api.generateImage(promptRequest)
}
