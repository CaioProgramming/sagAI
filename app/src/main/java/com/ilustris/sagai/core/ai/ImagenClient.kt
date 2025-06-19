package com.ilustris.sagai.core.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImagenInlineImage
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.network.CloudflareApiService
import com.ilustris.sagai.core.network.body.StableDiffusionRequest
import com.ilustris.sagai.core.network.response.StableDiffusionResponse
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
interface ImagenClient {
    suspend fun generateImage(prompt: String): ImagenInlineImage?

    suspend fun generateWithStableDiffusion(request: StableDiffusionRequest): StableDiffusionResponse?
}

@OptIn(PublicPreviewAPI::class)
class ImagenClientImpl
    @Inject
    constructor(
        val service: CloudflareApiService,
    ) : ImagenClient {
        val model by lazy {
            Firebase.ai.imagenModel(
                "imagen-3.0-generate-002",
            )
        }

        override suspend fun generateImage(prompt: String): ImagenInlineImage? =
            try {
                Log.i(javaClass.simpleName, "generateImage: Generating image with prompt:\n$prompt")
                val response = model.generateImages(prompt.trimIndent())
                response.images.first()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

        override suspend fun generateWithStableDiffusion(request: StableDiffusionRequest) =
            try {
                service.generateImage(request)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
    }

private const val NEGATIVE_PROMPT =
    "digital illustration, sharp lines, clean edges, vector art," +
        "smooth gradients, excessive saturation, cartoon, comic book, modern render, pixel art, photography, 3D render"
