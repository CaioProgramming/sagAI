package com.ilustris.sagai.features.saga.chat.data.model

import com.ilustris.sagai.features.saga.chat.domain.model.Message

data class TypoFix(
    val status: String,
    val suggestedText: String?,
    val friendlyMessage: String?,
) {
    fun getStatus() = TypoStatus.valueOf(status)
}

enum class TypoStatus {
    OK,
    ENHANCEMENT,
    FIX,
}
