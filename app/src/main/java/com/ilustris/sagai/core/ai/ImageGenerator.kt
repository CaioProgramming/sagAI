package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.asImageOrNull
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.toJsonFormat
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(PublicPreviewAPI::class)
interface ImageGenerator {
    suspend fun generateImage(prompt: String): Bitmap?

    suspend fun generateImageRequest(prompt: String): RequestResult<Bitmap>
}

@OptIn(PublicPreviewAPI::class)
@Singleton
class ImageGeneratorImpl
    @Inject
    constructor(
        private val billingService: BillingService,
        private val remoteConfigService: RemoteConfigService,
        private val debugImageFallbackService: DebugImageFallbackService,
    ) : ImageGenerator {
        private suspend fun modelName() =
            remoteConfigService.getString("imageGenModelPremium")
                ?: error("Couldn't find model for Image generation")

        override suspend fun generateImage(prompt: String): Bitmap? {
            val modelName = modelName()
            val trimmedPrompt = prompt.trim()
            Timber.tag(TAG).i("Generating image with ➡ $modelName")
            Timber
                .tag(TAG)
                .i("\uD83D\uDE80 TEST THIS PROMPT ON GEMINI: https://gemini.google.com/app")
            Timber.tag(TAG).i("--- COPY START ---")
            Timber.tag(TAG).i(trimmedPrompt)
            Timber.tag(TAG).i("--- COPY END ---")

            if (BuildConfig.DEBUG && !billingService.isPremium()) {
                return debugImageFallbackService.awaitManualImage(trimmedPrompt)
            }

            val apiBitmap =
                billingService.runPremiumRequest {
                    val imageModel =
                        Firebase.ai().generativeModel(
                            modelName = modelName,
                            generationConfig =
                                generationConfig {
                                    responseModalities =
                                        listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
                                },
                        )
                    val promptBuilder =
                        content {
                            text(trimmedPrompt)
                        }

                    val content = imageModel.generateContent(promptBuilder)
                    Timber
                        .tag(TAG)
                        .d("generateImage: Token data: ${content.usageMetadata?.toJsonFormat()}")

                    content.candidates
                        .firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull()
                        ?.asImageOrNull()
                }

            return apiBitmap ?: if (BuildConfig.DEBUG) {
                debugImageFallbackService.awaitManualImage(trimmedPrompt)
            } else {
                null
            }
        }

        override suspend fun generateImageRequest(prompt: String): RequestResult<Bitmap> =
            executeRequest {
                generateImage(prompt) ?: error("Failed to generate image bitmap")
            }

        companion object {
            private const val TAG = "\uD83D\uDDBC\uFE0F ImageGenerator"
        }
    }
