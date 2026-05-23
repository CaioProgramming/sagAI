package com.ilustris.sagai.features.playthrough.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PlayThroughData(
    @SerializedName("title")
    val title: String,
    @SerializedName("review")
    val review: String,
)
