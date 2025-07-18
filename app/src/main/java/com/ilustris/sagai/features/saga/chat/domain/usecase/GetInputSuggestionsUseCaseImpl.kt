package com.ilustris.sagai.features.saga.chat.domain.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.SuggestionPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import javax.inject.Inject

class GetInputSuggestionsUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
    ) : GetInputSuggestionsUseCase {
        override suspend fun invoke(
            chatMessages: List<MessageContent>,
            currentUserCharacter: Character?,
            sagaData: SagaData,
        ): RequestResult<Exception, List<Suggestion>> =
            // Updated return type
            try {
                val prompt =
                    SuggestionPrompts.generateSuggestionsPrompt(
                        sagaData = sagaData,
                        character = currentUserCharacter!!,
                        chatHistory = chatMessages,
                    )

                val suggestions =
                    gemmaClient.generate<List<Suggestion>>(
                        prompt = prompt,
                        requireTranslation = true,
                    )

                suggestions!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }
    }
