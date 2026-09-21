package com.ilustris.sagai.features.saga.chat.domain.model

// Import the correct SenderType

data class Suggestion(
    val text: String,
)

data class SuggestionsReponse(
    val suggestions: List<Suggestion>,
) {
    companion object {
        fun example() =
            SuggestionsReponse(
                listOf(
                    Suggestion(""),
                ),
            )
    }
}
