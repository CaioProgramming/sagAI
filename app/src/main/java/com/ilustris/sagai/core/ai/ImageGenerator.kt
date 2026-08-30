package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.ai.key.QuotaStatusService
import com.ilustris.sagai.core.ai.key.UserApiKeyStore
import com.ilustris.sagai.core.ai.model.GeminiContent
import com.ilustris.sagai.core.ai.model.GeminiGenerationConfig
import com.ilustris.sagai.core.ai.model.GeminiPart
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.data.BillableFeature
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.SideEffect
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.network.GeminiApiClient
import com.ilustris.sagai.core.network.GeminiHttpException
import com.ilustris.sagai.core.services.SideEffectService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.toJsonFormat
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface ImageGenerator {
    suspend fun generateImage(prompt: String): Bitmap?

    suspend fun generateImageRequest(prompt: String): RequestResult<Bitmap>
}

/**
 * Image generation over the Gemini REST endpoint, on the user's own API key.
 *
 * This used to call `Firebase.ai().generativeModel(...)`, which carries no API key at all — the
 * Firebase AI Logic SDK bills the request to our Firebase project. That made it the one AI surface
 * BYOK could not reach, so it moved onto the same [GeminiApiClient] path everything else uses.
 *
 * The transport was already in place: [GeminiGenerationConfig.responseModalities] serializes to
 * `response_modalities`, and the codec decodes `inlineData` off the response — the same shape
 * `AudioGenClientImpl` reads its audio bytes from.
 */
@Singleton
class ImageGeneratorImpl
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
        private val debugImageFallbackService: DebugImageFallbackService,
        private val geminiApiClient: GeminiApiClient,
        private val userApiKeyStore: UserApiKeyStore,
        private val quotaStatusService: QuotaStatusService,
        private val sideEffectService: SideEffectService,
    ) : ImageGenerator {
        private suspend fun modelName() =
            remoteConfigService.getString(IMAGE_MODEL_FLAG)
                ?: error("Couldn't find model for Image generation")

        private suspend fun apiKey(): String {
            quotaStatusService.activeDailyBlock()?.let { block ->
                throw QuotaExhaustedException(until = block.until, model = block.model)
            }
            return userApiKeyStore.getKeyNow()?.takeIf { it.isNotBlank() }
                ?: throw MissingApiKeyException()
        }

        override suspend fun generateImage(prompt: String): Bitmap? {
            val modelName = modelName()
            val trimmedPrompt = prompt.trim()
            Timber.tag(TAG).i("Generating image with ➡ $modelName")
            Timber
                .tag(TAG)
                .i("🚀 TEST THIS PROMPT ON GEMINI: https://gemini.google.com/app")
            Timber.tag(TAG).i("--- COPY START ---")
            Timber.tag(TAG).i(trimmedPrompt)
            Timber.tag(TAG).i("--- COPY END ---")

            // No premium gate here any more. A subscription cannot deliver this: image models are
            // "Not available" on the Gemini free tier, so whether it works depends on billing on
            // the user's own Google Cloud project, not on anything we sell. Charging for it would
            // be selling a promise we are not the ones keeping.
            val apiBitmap =
                run {
                    val request =
                        GeminiRequest(
                            contents =
                                listOf(
                                    GeminiContent(parts = listOf(GeminiPart(text = trimmedPrompt))),
                                ),
                            generationConfig =
                                GeminiGenerationConfig(
                                    // Image models reject an IMAGE-only request; TEXT has to ride
                                    // along even though the text part is discarded below.
                                    responseModalities = listOf("TEXT", "IMAGE"),
                                ),
                        )

                    val response =
                        try {
                            geminiApiClient.generateContent(
                                model = modelName.replace("models/", ""),
                                apiKey = apiKey(),
                                request = request,
                            )
                        } catch (e: GeminiHttpException) {
                            // Every image model is "Not available" on the Gemini free tier, so a
                            // key without billing fails here and nowhere else. Detected by which
                            // call we are in rather than by parsing the body: the API expresses it
                            // as a plain 403 or a zero quota, and both would otherwise surface as
                            // "your key was rejected" or "daily limit reached" — alarming, and
                            // both untrue. The same key still writes text fine.
                            if (e.code == 403 || e.code == 429) {
                                sideEffectService.emit(
                                    SideEffect.FeatureNeedsBilling(
                                        BillableFeature.IMAGE_GENERATION,
                                    ),
                                )
                                // In debug this is the common case, not an exception — a dev key is
                                // usually free tier. Falling through to the manual island keeps the
                                // rest of the flow testable instead of dead-ending on a limit that
                                // has nothing to do with the code under test.
                                if (BuildConfig.DEBUG) return@run null
                            }
                            throw e
                        }

                    response.error?.let { error ->
                        Timber.tag(TAG).e("Gemini API error: ${error.code} - ${error.message}")
                        throw Exception("Gemini API error: ${error.message}")
                    }

                    Timber
                        .tag(TAG)
                        .d("generateImage: Token data: ${response.usageMetadata?.toJsonFormat()}")

                    response.candidates
                        ?.firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull { it.inlineData != null }
                        ?.inlineData
                        ?.data
                        ?.let(::decodeBitmap)
                }

            return apiBitmap ?: if (BuildConfig.DEBUG) {
                debugImageFallbackService.awaitManualImage(trimmedPrompt)
            } else {
                null
            }
        }

        private fun decodeBitmap(base64: String): Bitmap? =
            try {
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                Timber.tag(TAG).e("Failed to decode generated image: ${e.message}")
                null
            }

        override suspend fun generateImageRequest(prompt: String): RequestResult<Bitmap> =
            executeRequest {
                generateImage(prompt) ?: error("Failed to generate image bitmap")
            }

        companion object {
            private const val TAG = "🖼️ ImageGenerator"
            private const val IMAGE_MODEL_FLAG = "imageGenModelPremium"
        }
    }
