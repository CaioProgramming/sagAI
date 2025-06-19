package com.ilustris.sagai.core.network.response

data class StableDiffusionResponse(
    val type: String,
    val contentType: String,
    val format: String,
    val description: String,
)

data class BlackForestResponse(
    val type: String,
    val contentType: String,
    val properties: Properties,
)

data class Properties(
    val type: String,
    val description: String,
)
