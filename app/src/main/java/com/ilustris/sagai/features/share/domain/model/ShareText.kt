package com.ilustris.sagai.features.share.domain.model

data class ShareText(
    val title: String,
    val text: String,
)

enum class ShareType {
    PLAYSTYLE,
    HISTORY,
    RELATIONS,
    EMOTIONS,
}
