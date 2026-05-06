package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.model.ImageReference
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.ImageConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.analytics.AnalyticsService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    ): Flow<StreamingState<GeneratedContent<Bitmap>>>

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
        ): Flow<StreamingState<GeneratedContent<Bitmap>>> =
            flow {
                try {
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
                                    GeneratedContent(
                                        generatedImage,
                                        "Image generation complete!",
                                    ),
                                ),
                            )
                        } else {
                            emit(StreamingState.Error("Failed to generate final image bitmap"))
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

                val finalStringPrompt =
                    gemmaClient.generate<String>(
                        prompt = prompt,
                        useCore = true,
                        requirement = GemmaClient.ModelRequirement.HIGH,
                        requireTranslation = false,
                    )!!

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

                imageGenerator.generateImage(
                    prompt = finalStringPrompt,
                    references = referenceList,
                    aspectRatio = finalAspectRatio,
                )!!
            }
    }
