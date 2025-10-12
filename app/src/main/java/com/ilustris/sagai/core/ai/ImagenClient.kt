package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.asImageOrNull
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.recordException
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ilustris.sagai.core.ai.models.ImageReference
import com.ilustris.sagai.core.network.FreePikApiService
import com.ilustris.sagai.core.network.body.FreepikRequest
import com.ilustris.sagai.core.network.response.FreePikResponse
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.utils.toJsonFormat
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
interface ImagenClient {
    suspend fun generateImage(
        prompt: String,
        references: List<ImageReference> = emptyList(),
        canByPass: Boolean = false,
    ): Bitmap?
}

@OptIn(PublicPreviewAPI::class)
class ImagenClientImpl
    @Inject
    constructor(
        val billingService: BillingService,
        private val firebaseRemoteConfig: FirebaseRemoteConfig,
    ) : ImagenClient {
        companion object {
            const val IMAGE_PREMIUM_MODEL_FLAG = "imageGenModelPremium"
            private const val TAG = "🖼️ Image Generation"
        }

        val modelName by lazy {
            val premiumModel = firebaseRemoteConfig.getString(IMAGE_PREMIUM_MODEL_FLAG)
            premiumModel
        }

        override suspend fun generateImage(
            prompt: String,
            references: List<ImageReference>,
            canByPass: Boolean,
        ): Bitmap? =
            try {
                val isPremiumUser = billingService.isPremium()
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
                if (isPremiumUser.not() && canByPass.not()) {
                    error("Only premium users can generate images")
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
                Log.e(TAG, "generateImage: Image generation failed ${e.message}")
                Log.e(TAG, "generateImage: Requested prompt $prompt")
                Log.e(TAG, "generateImage: ${references.size} references submitted")
                e.printStackTrace()
                null
            }
    }
