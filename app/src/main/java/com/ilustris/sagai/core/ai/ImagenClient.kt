package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.model.ImageConfig
import com.ilustris.sagai.core.ai.model.ImagePromptReview
import com.ilustris.sagai.core.ai.model.ImageReference
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.model.ReviewerStrictness
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.ImageConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.analytics.AnalyticsService
import com.ilustris.sagai.core.analytics.ImageQualityEvent
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.newsaga.data.model.Genre
import timber.log.Timber
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

    suspend fun generateIntegratedImageStream(
        genre: Genre,
        imageReference: Pair<Bitmap, String>?,
        context: String,
        imageType: ImageType,
        variationId: String? = null,
    ): kotlinx.coroutines.flow.Flow<StreamingState<com.ilustris.sagai.core.ai.model.GeneratedContent<Bitmap>>>

    suspend fun generateImage(
        prompt: String,
        aspectRatio: String?,
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
        private val promptService: PromptService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
        private val imageGenerator: ImageGenerator,
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
            aspectRatio: String?,
        ): RequestResult<Bitmap> = imageGenerator.generateImageRequest(prompt, aspectRatio)

        override suspend fun generateIntegratedImageStream(
            genre: Genre,
            imageReference: Pair<Bitmap, String>?,
            context: String,
            imageType: ImageType,
            variationId: String?,
        ): kotlinx.coroutines.flow.Flow<StreamingState<com.ilustris.sagai.core.ai.model.GeneratedContent<Bitmap>>> =
            kotlinx.coroutines.flow.flow {
                try {
                    billingService.runPremiumRequest(bypass = true) {
                        val genreConfig = genreConfigService.getGenreConfig(genre, variationId)
                        val imageConfig = imageConfigService.getImageConfig()

                        val prompt =
                            ImagePrompts.buildUnifiedImagePrompt(
                                promptService,
                                genre,
                                genreConfig,
                                imageConfig,
                                imageType,
                                context,
                            )

                        var finalStringPrompt: String? = null
                        val sourceStream =
                            gemmaClient.generateStreaming<String>(
                                prompt = prompt,
                                useCore = false,
                                requirement = GemmaClient.ModelRequirement.HIGH,
                                requireTranslation = false,
                            )

                        reasoningSynthesizerService
                            .synthesizeReasoning(
                                sourceFlow = sourceStream,
                                context = context,
                                conversationStyle = genreConfig.conversationDirective,
                                genre = genre.name,
                            ).collect { state ->
                                when (state) {
                                    is StreamingState.Reasoning -> {
                                        emit(StreamingState.Reasoning(state.chunk))
                                    }

                                    is StreamingState.Success -> {
                                        finalStringPrompt = state.data
                                    }

                                    is StreamingState.Error -> {
                                        emit(StreamingState.Error(state.message, state.throwable))
                                    }
                                }
                            }

                        if (finalStringPrompt != null) {
                            val typeConfig = imageConfig.typeConfigs[imageType.name]

                            val finalAspectRatio =
                                when (imageType) {
                                    ImageType.ICON -> {
                                        genreConfig.iconAspectRatio
                                            ?: typeConfig?.aspectRatio
                                    }

                                    ImageType.COVER -> {
                                        genreConfig.coverAspectRatio
                                            ?: typeConfig?.aspectRatio
                                    }
                                } ?: ""

                            val referenceList =
                                imageReference?.let { listOf(ImageReference(it.first, it.second)) }
                                    ?: emptyList()

                            emit(StreamingState.Reasoning("\nRendering scene..."))
                            val generatedImage =
                                imageGenerator.generateImage(
                                    prompt = finalStringPrompt!!,
                                    references = referenceList,
                                    aspectRatio = finalAspectRatio,
                                )

                            if (generatedImage != null) {
                                emit(
                                    StreamingState.Success(
                                        com.ilustris.sagai.core.ai.model
                                            .GeneratedContent(
                                                generatedImage,
                                                "Image generation complete!",
                                            ),
                                    ),
                                )
                            } else {
                                emit(StreamingState.Error("Failed to generate final image bitmap"))
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(
                        StreamingState
                            .Error(e.message ?: "Unknown error in streaming image generation", e),
                    )
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
                billingService.runPremiumRequest(bypass = true) {
                    Timber.tag(TAG).d(
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
                    Timber.tag(TAG).d("📸 Visual Direction extracted: $visualDirection")

                    // 2. ARTISTIC DESCRIPTION
                    val artisticPrompt =
                        generateArtisticPrompt(
                            genre,
                            genreConfig,
                            imageConfig,
                            imageType,
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
                        Timber.tag(TAG).d("⚖️ Final prompt reviewed.")
                    } ?: run {
                        Timber.tag(TAG).e("generateIntegratedImage: Failed to review")
                    }
                    val typeConfig = imageConfig.typeConfigs[imageType.name]
                    val finalAspectRatio =
                        when (imageType) {
                            ImageType.ICON -> {
                                genreConfig.iconAspectRatio ?: typeConfig?.aspectRatio
                            }

                            ImageType.COVER -> {
                                genreConfig.coverAspectRatio
                                    ?: typeConfig?.aspectRatio
                            }
                        }
                    val finalPrompt =
                        buildString {
                            appendLine(genreConfig.artStyle)
                            appendLine(reviewedResult?.correctedPrompt ?: artisticPrompt)
                            appendLine("Critical rules: ")
                            appendLine(imageConfig.criticalRules)
                            appendLine("Rendering Instructions: ")
                            appendLine(genreConfig.renderingInstructions)
                            appendLine("Aspect Ratio: $finalAspectRatio")
                        }
                    Timber.tag(TAG).d(
                        buildString {
                            appendLine("Image generation pipeline execution: ")
                            appendLine("context: $context")
                            appendLine("genre: ${genre.name}")
                            appendLine("genreConfig: ${genreConfig.toJsonFormat()}")
                            appendLine("imageConfig: ${imageConfig.toJsonFormat()}")
                            appendLine("visualDirection: $visualDirection")
                            appendLine("artisticPrompt: $artisticPrompt")
                            appendLine("finalPrompt: $finalPrompt")
                            appendLine("Aspect Ratio: $finalAspectRatio")
                            appendLine("Revisions: ${reviewedResult.toJsonFormat()}")
                        },
                    )

                    val generatedImage =
                        imageGenerator.generateImage(
                            finalPrompt,
                            references = emptyList(),
                            aspectRatio = finalAspectRatio,
                        )

                    if (generatedImage == null) {
                        Timber.tag(TAG).e("Failed to generate image")
                    } else {
                        Timber.tag(TAG).i("✅ Image successfully generated.")
                    }

                    generatedImage!!
                }
            }

        private suspend fun generateVisualDirection(
            context: String,
            genre: Genre,
            genreConfig: GenreConfig?,
            imageConfig: ImageConfig,
            imageType: ImageType,
        ) = executeRequest {
            gemmaClient.generate<String>(
                ImagePrompts.generateDirectorialVision(
                    genre,
                    genreConfig!!,
                    imageConfig,
                    imageType,
                    context,
                ),
                temperatureRandomness = .5f,
                references = emptyList(),
                requireTranslation = false,
                requirement = GemmaClient.ModelRequirement.LOW,
            )!!
        }

        private suspend fun generateArtisticPrompt(
            genre: Genre,
            genreConfig: GenreConfig?,
            imageConfig: ImageConfig,
            imageType: ImageType,
            visualDirection: String?,
            context: String,
        ): RequestResult<String> =
            executeRequest {
                val prompt =
                    ImagePrompts.generateArtistPrompt(
                        genre,
                        genreConfig!!,
                        imageConfig,
                        imageType,
                        visualDirection,
                        context,
                    )

                gemmaClient.generate<String>(
                    prompt,
                    references = emptyList(),
                    requireTranslation = false,
                    requirement = GemmaClient.ModelRequirement.LOW,
                    temperatureRandomness = 1f,
                )!!
            }

        private suspend fun reviewAndCorrectPrompt(
            imageType: ImageType,
            visualDirection: String?,
            genreConfig: GenreConfig?,
            imageConfig: ImageConfig,
            genre: Genre,
            finalPrompt: String,
            context: String,
        ) = executeRequest {
            val reviewerPrompt =
                ImagePrompts.reviewImagePrompt(
                    visualDirection,
                    genreConfig!!,
                    imageConfig,
                    imageType,
                    finalPrompt,
                    genre,
                    context,
                )

            Timber.tag(TAG).d(
                "reviewAndCorrectPrompt: Starting review with ${(genreConfig.reviewerStrictness ?: ReviewerStrictness.STRICT).name} strictness",
            )
            val review =
                gemmaClient.generate<ImagePromptReview>(
                    reviewerPrompt,
                    references = emptyList(),
                    requireTranslation = false,
                    useCore = true,
                    requirement = GemmaClient.ModelRequirement.HIGH,
                )!!
            Timber.tag(TAG).i("✏️Prompt was modified by reviewer: ")
            Timber.tag(TAG).i(review.toAINormalize())
            Timber.tag(TAG).d(
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
