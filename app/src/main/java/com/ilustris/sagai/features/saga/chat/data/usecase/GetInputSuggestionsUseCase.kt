package com.ilustris.sagai.features.saga.chat.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion

interface GetInputSuggestionsUseCase {
    suspend operator fun invoke(
        chatHistory: List<MessageContent>,
        currentUserCharacter: Character?,
        saga: Saga,
    ): RequestResult<List<Suggestion>>
}
