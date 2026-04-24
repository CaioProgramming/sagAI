package com.ilustris.sagai.core.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.model.ImagePalette

interface PaletteUseCase {
    suspend fun extractPalette(imageUrl: String): RequestResult<ImagePalette>
}
