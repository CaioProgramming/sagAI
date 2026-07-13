package com.ilustris.sagai.features.imagegeneration.model

import android.graphics.Bitmap
import com.ilustris.sagai.core.ai.model.ImageType

enum class IslandExpansion {
    Compact,
    Expanded,
}

sealed interface ImageGenerationUiState {
    data object Idle : ImageGenerationUiState

    data class Generating(
        val label: String?,
        val reasoning: String?,
        val imageType: ImageType,
        val queuePosition: Int = 0,
        val expansion: IslandExpansion = IslandExpansion.Compact,
    ) : ImageGenerationUiState

    data class AwaitingManualFallback(
        val prompt: String,
        val expansion: IslandExpansion = IslandExpansion.Compact,
    ) : ImageGenerationUiState

    data class Reveal(
        val bitmap: Bitmap,
        val imageType: ImageType,
        val label: String?,
    ) : ImageGenerationUiState
}
