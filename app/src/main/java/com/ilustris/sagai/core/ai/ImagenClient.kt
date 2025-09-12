package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.asImageOrNull
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ilustris.sagai.core.ai.models.ImageReference
import com.ilustris.sagai.core.network.FreePikApiService
import com.ilustris.sagai.core.network.body.FreepikRequest
import com.ilustris.sagai.core.network.response.FreePikResponse
import com.ilustris.sagai.core.utils.toJsonFormat
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
interface ImagenClient {
    suspend fun generateImage(
        prompt: String,
        references: List<ImageReference> = emptyList(),
    ): Bitmap?

    suspend fun generateWithFreePik(request: FreepikRequest): FreePikResponse?
}

@OptIn(PublicPreviewAPI::class)
class ImagenClientImpl
    @Inject
    constructor(
        val service: FreePikApiService,
        private val firebaseRemoteConfig: FirebaseRemoteConfig,
    ) : ImagenClient {
        companion object {
            const val IMAGE_MODEL_FLAG = "imageGenModel"
            const val IMAGE_PREMIUM_MODEL_FLAG = "imageGenModelPremium"
            const val DEFAULT_IMAGE_MODEL = "gemini-2.0-flash-preview-image-generation"

            private const val PREMIUM_FLAG = "premiumEnabled"

            private const val TAG = "🖼️ Image Generation"
        }

        private val isPremium by lazy {
            firebaseRemoteConfig.fetchAndActivate()
            firebaseRemoteConfig.getBoolean(PREMIUM_FLAG).let {
                Log.i(TAG, "Premium enabled: $it")
                it
            }
        }

        val modelName by lazy {
            val defaultModel = firebaseRemoteConfig.getString(IMAGE_MODEL_FLAG)
            val premiumModel = firebaseRemoteConfig.getString(IMAGE_PREMIUM_MODEL_FLAG)

            if (isPremium) {
                premiumModel
            } else {
                defaultModel
            }
        }

        override suspend fun generateImage(
            prompt: String,
            references: List<ImageReference>,
        ): Bitmap? =
            try {
                val imageModel =
                    Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                        modelName = modelName,
                        generationConfig =
                            generationConfig {
                                responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
                            },
                    )
                val promptBuilder =
                    content {
                        text(prompt.trimIndent())
                        references.forEach {
                            image(it.bitmap)
                            text(it.description)
                        }
                    }
                val content = imageModel.generateContent(promptBuilder)
                Log.d(TAG, "generateImage: Token data: ${content.usageMetadata?.toJsonFormat()}")
                Log.d(TAG, "generateImage: Prompt feedback: ${content.promptFeedback?.toJsonFormat()}")
                Log.i(javaClass.simpleName, "Generating image($modelName) with prompt:\n${promptBuilder.toJsonFormat()}")

                content
                    .candidates
                    .first()
                    .content.parts
                    .firstNotNullOf { it.asImageOrNull() }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

        override suspend fun generateWithFreePik(request: FreepikRequest): FreePikResponse? =
            try {
                Log.i(
                    javaClass.simpleName,
                    "generateImage: Generating freePik image with prompt:\n$request",
                )
                service.generateImage(request)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
    }
