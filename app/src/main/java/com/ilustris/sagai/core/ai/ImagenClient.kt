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
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.ImageConfigService
import com.ilustris.sagai.core.analytics.AnalyticsService
import com.ilustris.sagai.core.analytics.ImageQualityEvent
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.newsaga.data.model.Genre
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
interface ImagenClient {
    suspend fun generateIntegratedImage(
        genre: Genre,
        imageReference: Pair<Bitmap, String>?,
        context: String,
        imageType: ImageType,
        variationId: String? = null,
    ): RequestResult<Bitmap>
}

@OptIn(PublicPreviewAPI::class)
class ImagenClientImpl
    @Inject
    constructor(
        val billingService: BillingService,
        private val remoteConfigService: RemoteConfigService,
        private val genreConfigService: GenreConfigService,
        private val imageConfigService: ImageConfigService,
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

        private suspend fun generateImage(
            prompt: String,
            references: List<ImageReference>,
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

        override suspend fun generateIntegratedImage(
            genre: Genre,
            imageReference: Pair<Bitmap, String>?,
            context: String,
            imageType: ImageType,
            variationId: String?,
        ): RequestResult<Bitmap> =
            executeRequest {
                Log.d(
                    TAG,
                    "🚀 Starting integrated image generation flow for: ${imageType.name} | Genre: ${genre.name} | Variation: $variationId",
                )

                // 0. FETCH CONFIGS
                val genreConfig = genreConfigService.getGenreConfig(genre, variationId)
                val imageConfig = imageConfigService.getImageConfig()

                // 1. VISUAL DIRECTOR ANALYSIS
                val visualDirection =
                    generateVisualDirection(
                        context,
                        genre,
                        genreConfig,
                        imageConfig,
                        imageType,
                    ).getSuccess()
                Log.d(TAG, "📸 Visual Direction extracted: $visualDirection")

                // 2. ARTISTIC DESCRIPTION
                val artisticPrompt =
                    generateArtisticPrompt(
                        genre,
                        genreConfig,
                        imageConfig,
                        visualDirection,
                        context,
                    ).getSuccess() ?: error("Failed to generate base artistic prompt")

                // 3. REVIEWER CONCLUSION
                val reviewedResult =
                    reviewAndCorrectPrompt(
                        imageType = imageType,
                        visualDirection = visualDirection,
                        genreConfig = genreConfig,
                        imageConfig = imageConfig,
                        genre = genre,
                        finalPrompt = artisticPrompt,
                        context = context,
                    ).getSuccess()

                reviewedResult?.let {
                    Log.d(TAG, "⚖️ Final prompt reviewed.")
                } ?: run {
                    Log.e(TAG, "generateIntegratedImage: Failed to review")
                }
                val finalPrompt =
                    "${genreConfig.renderingInstructions}\n" +
                        (reviewedResult?.correctedPrompt ?: artisticPrompt)

                val generatedImage = generateImage(finalPrompt, references = emptyList())

                // Log.d(TAG, "generateIntegratedImage: Used reference: ${imageReference.second}")

                if (generatedImage == null) {
                    Log.e(TAG, "Failed to generate image")
                } else {
                    Log.i(TAG, "✅ Image successfully generated.")
                }

                generatedImage!!
            }

        private suspend fun generateVisualDirection(
            context: String,
            genre: Genre,
            genreConfig: com.ilustris.sagai.core.ai.model.GenreConfig,
            imageConfig: com.ilustris.sagai.core.ai.model.ImageConfig,
            imageType: ImageType,
        ) = executeRequest {
            gemmaClient.generate<String>(
                ImagePrompts.generateDirectorialVision(
                    genre,
                    genreConfig,
                    imageConfig,
                    context,
                    imageType,
                ),
                temperatureRandomness = 0.8f,
                references = emptyList(),
                requireTranslation = false,
                requirement = GemmaClient.ModelRequirement.HIGH,
            )!!
        }

        private suspend fun generateArtisticPrompt(
            genre: Genre,
            genreConfig: com.ilustris.sagai.core.ai.model.GenreConfig,
            imageConfig: com.ilustris.sagai.core.ai.model.ImageConfig,
            visualDirection: String?,
            context: String,
        ): RequestResult<String> =
            executeRequest {
                val prompt =
                    ImagePrompts.generateArtistPrompt(
                        genre,
                        genreConfig,
                        imageConfig,
                        visualDirection,
                        context,
                    )

                gemmaClient.generate<String>(
                    prompt,
                    references = emptyList(),
                    requireTranslation = false,
                    requirement = GemmaClient.ModelRequirement.HIGH,
                    temperatureRandomness = 1f,
                )!!
            }

        private suspend fun reviewAndCorrectPrompt(
            imageType: ImageType,
            visualDirection: String?,
            genreConfig: com.ilustris.sagai.core.ai.model.GenreConfig,
            imageConfig: com.ilustris.sagai.core.ai.model.ImageConfig,
            genre: Genre,
            finalPrompt: String,
            context: String,
        ) = executeRequest {
            val reviewerPrompt =
                ImagePrompts.reviewImagePrompt(
                    visualDirection,
                    genreConfig,
                    imageConfig,
                    finalPrompt,
                    genre,
                    context,
                )

            Log.d(
                TAG,
                "reviewAndCorrectPrompt: Starting review with ${genreConfig.reviewerStrictness.name} strictness",
            )
            val review =
                gemmaClient.generate<ImagePromptReview>(
                    reviewerPrompt,
                    references = emptyList(),
                    requireTranslation = false,
                    useCore = true,
                    requirement = GemmaClient.ModelRequirement.HIGH,
                )!!
            Log.i(TAG, "✏️Prompt was modified by reviewer: ")
            Log.i(TAG, review.toAINormalize())
            Log.d(
                TAG,
                buildString {
                    appendLine("Suggestions: ")
                    appendLine("Artist Suggestion: ${review.artistImprovementSuggestions}")
                    appendLine("Visual Suggestion: ${review.visualDirectorSuggestions}")
                    appendLine("Rendering Suggestion: ${review.renderingSuggestions}")
                },
            )
            trackImageQuality(
                genre = genre.name,
                imageType = imageType,
                review = review,
            )
            review
        }

        private fun trackImageQuality(
            genre: String,
            imageType: ImageType,
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
                    imageType = imageType.name,
                    quality = review.getQualityLevel(),
                    violations = review.violations.size,
                    violationTypes = violationTypes.ifEmpty { null },
                ),
            )
        }
    }
