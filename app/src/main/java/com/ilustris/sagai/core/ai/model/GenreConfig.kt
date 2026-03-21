package com.ilustris.sagai.core.ai.model

import com.google.gson.annotations.SerializedName

data class GenreConfig(
    @SerializedName("ambientMusicUrl")
    val ambientMusicUrl: String = "",
    @SerializedName("reviewerStrictness")
    val reviewerStrictness: ReviewerStrictness? = null,
    @SerializedName("variations")
    val variations: Map<String, VariationConfig>? = null,
    @SerializedName("iconAspectRatio")
    val iconAspectRatio: String? = null,
    @SerializedName("coverAspectRatio")
    val coverAspectRatio: String? = null,
    val imageUrl: String = "",
) {
    data class VariationConfig(
        val name: String = "",
        val description: String = "",
    )
}
