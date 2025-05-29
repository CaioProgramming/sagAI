package com.ilustris.sagai.core.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.PublicPreviewAPI

@OptIn(PublicPreviewAPI::class)
class ImagenClient {
    val model by lazy {
        Firebase.ai.imagenModel("imagen-3.0-generate-002")
    }
}
