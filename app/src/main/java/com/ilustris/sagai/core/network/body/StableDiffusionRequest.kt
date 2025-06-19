package com.ilustris.sagai.core.network.body

data class StableDiffusionRequest(
    val prompt: String,
    val negativePrompt: String,
    val width: Int,
    val height: Int,
    val image: List<String> = emptyList(),
    val mask: List<String> = emptyList(),
)
