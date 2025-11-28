package com.ilustris.sagai.features.saga.chat.domain.model

import com.ilustris.sagai.features.saga.chat.data.model.SenderType

// Import the correct SenderType

data class Suggestion(
    val text: String,
    val type: SenderType, // Using the SenderType from domain.usecase.model
)

data class SuggestionsReponse(
    val suggestions: List<Suggestion>,
) {
    companion object {
        fun example() =
            SuggestionsReponse(
                listOf(
                    Suggestion("", SenderType.USER),
                ),
            )
    }
}
