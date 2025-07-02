package com.ilustris.sagai.core.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ImagenInlineImage
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.network.FreePikApiService
import com.ilustris.sagai.core.network.body.FreepikRequest
import com.ilustris.sagai.core.network.response.FreePikResponse
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
interface ImagenClient {
    suspend fun generateImage(prompt: String): ImagenInlineImage?

    suspend fun generateWithFreePik(request: FreepikRequest): FreePikResponse?
}

@OptIn(PublicPreviewAPI::class)
class ImagenClientImpl
    @Inject
    constructor(
        val service: FreePikApiService,
    ) : ImagenClient {
        val model by lazy {
            Firebase.ai(backend = GenerativeBackend.vertexAI()).imagenModel(
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

        override suspend fun generateWithFreePik(request: FreepikRequest): FreePikResponse? =
            try {
                Log.i(javaClass.simpleName, "generateImage: Generating freePik image with prompt:\n$request")
                service.generateImage(request)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
    }

private const val NEGATIVE_PROMPT =
    "digital illustration, sharp lines, clean edges, vector art," +
        "smooth gradients, excessive saturation, cartoon, comic book, modern render, pixel art, photography, 3D render"
