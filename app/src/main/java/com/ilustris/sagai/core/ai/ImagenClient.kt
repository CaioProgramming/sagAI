package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.asImageOrNull
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig // Added
import com.ilustris.sagai.core.network.FreePikApiService
import com.ilustris.sagai.core.network.body.FreepikRequest
import com.ilustris.sagai.core.network.response.FreePikResponse
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
                "gemini-2.5-flash-image-preview"
            } else {
                "gemini-2.0-flash-preview-image-generation"
            }
        }

        override suspend fun generateImage(
            prompt: String,
            references: List<ImageReference>,
        ): Bitmap? =
            try {
                Log.i(javaClass.simpleName, "Generating image with prompt:\n$prompt")
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
                imageModel
                    .generateContent(promptBuilder)
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
