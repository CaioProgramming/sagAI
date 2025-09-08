package com.ilustris.sagai.core.network.response

data class FreePikResponse(
    val data: List<ImageResponse>,
)

data class ImageResponse(
    val base64: String,
)
