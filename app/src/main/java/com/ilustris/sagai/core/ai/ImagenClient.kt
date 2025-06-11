package com.ilustris.sagai.core.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImagenInlineImage
import com.google.firebase.ai.type.PublicPreviewAPI

@OptIn(PublicPreviewAPI::class)
class ImagenClient {
    val model by lazy {
        Firebase.ai.imagenModel("imagen-3.0-generate-002")
    }

    suspend fun generateImage(prompt: String): ImagenInlineImage? =
        try {
            Log.i(javaClass.simpleName, "generateImage: Generating image with prompt:\n$prompt")
            val response = model.generateImages(prompt.trimIndent())
            response.images.first()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
}
