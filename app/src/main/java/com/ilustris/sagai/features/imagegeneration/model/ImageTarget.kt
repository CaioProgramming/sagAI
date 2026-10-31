package com.ilustris.sagai.features.imagegeneration.model

/** The thing an image is being generated for — what a screen watches to know "my image is on its way". */
sealed interface ImageTarget {
    data class Chapter(
        val id: Int,
    ) : ImageTarget

    data class Saga(
        val id: Int,
    ) : ImageTarget

    data class Character(
        val id: Int,
    ) : ImageTarget
}

/**
 * Where one [ImageTarget] stands, as seen by the screen that shows its image. Unlike
 * [ImageGenerationUiState] (the single global queue head the island renders) this exists per target
 * and only moves on transitions, so any number of screens can observe their own without churn.
 */
sealed interface ImageTargetState {
    data object Idle : ImageTargetState

    /** Accepted, but the model is not drawing yet: preparing the prompt, or waiting behind other work. */
    data object Queued : ImageTargetState

    data object Generating : ImageTargetState

    /** The bitmap exists and is landing on the screen; held briefly so the arrival can animate. */
    data object Revealing : ImageTargetState

    data class Failed(
        val message: String? = null,
    ) : ImageTargetState

    val isActive: Boolean get() = this is Queued || this is Generating
}
