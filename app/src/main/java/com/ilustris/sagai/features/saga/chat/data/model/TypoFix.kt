package com.ilustris.sagai.features.saga.chat.data.model

import com.ilustris.sagai.features.saga.chat.domain.model.Message

data class TypoFix(
    val status: TypoStatus,
    val suggestedText: String?,
    val friendlyMessage: String?,
)

enum class TypoStatus {
    OK,
    ENHANCEMENT,
    FIX,
}
