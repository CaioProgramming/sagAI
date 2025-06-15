package com.ilustris.sagai.core.network

import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.network.body.StableDiffusionRequest
import com.ilustris.sagai.core.network.response.StableDiffusionResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface CloudflareApi {
    companion object {
        const val BASE_URL = "https://api.cloudflare.com/client/v4/accounts/"
    }

    @POST("${BuildConfig.ACCOUNTID}/ai/run/@cf/bytedance/stable-diffusion-xl-lightning")
    suspend fun generateImage(
        @Body body: StableDiffusionRequest,
    ): StableDiffusionResponse
}

class CloudflareApiService {
    private val api: CloudflareApi by lazy {
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
                                    .addHeader("Authorization", "Bearer ${BuildConfig.APIKEY}")
                                    .build()
                            chain.proceed(request)
                        },
                    ).build(),
            ).baseUrl(CloudflareApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudflareApi::class.java)
    }

    suspend fun generateImage(promptRequest: StableDiffusionRequest) = api.generateImage(promptRequest)
}
