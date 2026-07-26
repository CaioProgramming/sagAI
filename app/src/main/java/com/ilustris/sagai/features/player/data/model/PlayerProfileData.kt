package com.ilustris.sagai.features.player.data.model

import com.google.gson.annotations.SerializedName

data class PlayerProfileData(
    @SerializedName("candidate")
    val userName: String = "",
    val topics: List<ProfileTopic> = emptyList(),
)

data class ProfileTopic(
    val title: String = "",
    val content: String = "",
    val comment: String? = null,
)
