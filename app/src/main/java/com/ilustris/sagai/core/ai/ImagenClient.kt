package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.asImageOrNull
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.ilustris.sagai.core.ai.model.ImagePromptReview
import com.ilustris.sagai.core.ai.model.ImageReference
import com.ilustris.sagai.core.ai.prompts.GenrePrompts
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.analytics.AnalyticsService
import com.ilustris.sagai.core.analytics.ImageQualityEvent
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.newsaga.data.model.Genre
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
interface ImagenClient {
    suspend fun generateImage(
        prompt: String,
        references: List<ImageReference> = emptyList(),
        canByPass: Boolean = false,
    ): Bitmap?

    suspend fun extractComposition(references: List<ImageReference>): RequestResult<String>

    suspend fun reviewAndCorrectPrompt(
        imageType: String,
        visualDirection: String?,
        genre: Genre,
        finalPrompt: String,
    ): RequestResult<ImagePromptReview>
}

@OptIn(PublicPreviewAPI::class)
class ImagenClientImpl
    @Inject
    constructor(
        val billingService: BillingService,
        private val remoteConfigService: RemoteConfigService,
        private val gemmaClient: GemmaClient,
        private val analyticsService: AnalyticsService,
    ) : ImagenClient {
        companion object {
            const val IMAGE_PREMIUM_MODEL_FLAG = "imageGenModelPremium"
            private const val TAG = "🖼️ Image Generation"
        }

        suspend fun modelName() =
            remoteConfigService.getString(IMAGE_PREMIUM_MODEL_FLAG)
                ?: error("Couldn't find model for Image generation")

        override suspend fun generateImage(
            prompt: String,
            references: List<ImageReference>,
            canByPass: Boolean,
        ): Bitmap? {
            val modelName = modelName()
            val logData =
                buildString {
                    append("Generating image with ➡ $modelName\n")
                    appendLine("Prompt \uD83D\uDCC4:")
                    appendLine(prompt)
                    if (references.isNotEmpty()) {
                        appendLine("References \uD83C\uDFDE\uFE0F:\n")
                        references.forEach {
                            appendLine("Bitmap with Description: ${it.description}\n")
                        }
                    }
                }
            Log.i(TAG, logData)
            return billingService.runPremiumRequest {
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
                        text(prompt.trimIndent())
                        references.forEach {
                            image(it.bitmap)
                            text(it.description)
                        }
                    }

                val content = imageModel.generateContent(promptBuilder)
                Log.d(TAG, "generateImage: Token data: ${content.usageMetadata?.toJsonFormat()}")
                Log.d(
                    TAG,
                    "generateImage: Prompt feedback: ${content.promptFeedback?.toJsonFormat()}",
                )

                content
                    .candidates
                    .first()
                    .content.parts
                    .firstNotNullOf { it.asImageOrNull() }
            }
        }

        override suspend fun extractComposition(references: List<ImageReference>) =
            executeRequest {
                gemmaClient.generate<String>(
                    emptyString(),
                    references = references,
                    requireTranslation = false,
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                )!!
            }

        override suspend fun reviewAndCorrectPrompt(
            imageType: String,
            visualDirection: String?,
            genre: Genre,
            finalPrompt: String,
        ) = executeRequest {
            val artStyleValidationRules = GenrePrompts.validationRules(genre)
            val strictness = GenrePrompts.reviewerStrictness(genre)

            val reviewerPrompt =
                ImagePrompts.reviewImagePrompt(
                    visualDirection,
                    artStyleValidationRules,
                    strictness,
                    finalPrompt,
                )

            Log.d(TAG, "reviewAndCorrectPrompt: Starting review with ${strictness.name} strictness")
            val review =
                gemmaClient.generate<ImagePromptReview>(
                    reviewerPrompt,
                    references = emptyList(),
                    requireTranslation = false,
                    useCore = true,
                    requirement = GemmaClient.ModelRequirement.HIGH,
                )!!
            Log.i(TAG, "✏️ Prompt was modified by reviewer: ")
            Log.i(TAG, review.toAINormalize())
            trackImageQuality(
                genre = genre.name,
                imageType = imageType,
                review = review,
            )
            review
        }

        private fun trackImageQuality(
            genre: String,
            imageType: String,
            review: ImagePromptReview,
        ) {
            val violationTypes =
                review.violations
                    .mapNotNull { it.type?.name }
                    .distinct()
                    .joinToString(", ")

            analyticsService.trackEvent(
                ImageQualityEvent(
                    genre = genre,
                    imageType = imageType,
                    quality = review.getQualityLevel(),
                    violations = review.violations.size,
                    violationTypes = violationTypes.ifEmpty { null },
                ),
            )
        }
    }
