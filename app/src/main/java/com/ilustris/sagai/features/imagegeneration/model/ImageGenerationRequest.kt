package com.ilustris.sagai.features.imagegeneration.model

import android.graphics.Bitmap
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.features.newsaga.data.model.Genre

data class ImageGenerationRequest(
    val genre: Genre,
    val context: String,
    val imageType: ImageType,
    val variationId: String? = null,
    val imageReference: Pair<Bitmap, String>? = null,
    val label: String? = null,
    val silent: Boolean = false,
    val showReveal: Boolean = true,
)
