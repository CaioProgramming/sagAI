package com.ilustris.sagai.features.saga.chat.data.usecase

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.SuggestionPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.chat.data.model.SuggestionGen
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.domain.usecase.GetInputSuggestionsUseCase
import javax.inject.Inject

class GetInputSuggestionsUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
    ) : GetInputSuggestionsUseCase {
        override suspend fun invoke(
            chatMessages: List<MessageContent>,
            currentUserCharacter: Character?,
            saga: Saga,
        ): RequestResult<Exception, List<Suggestion>> =
            try {
                val prompt =
                    SuggestionPrompts.generateSuggestionsPrompt(
                        saga = saga,
                        character = currentUserCharacter!!,
                        chatHistory = chatMessages,
                    )

                Log.d("GetInputSuggestions", "Sending prompt to GemmaClient for suggestions.")
                gemmaClient.generate<SuggestionGen>(prompt)!!.suggestions.asSuccess()
            } catch (e: Exception) {
                Log.e(
                    "GetInputSuggestions",
                    "Error generating input suggestions: ${e.message}",
                    e,
                )
                e.asError()
            }
    }
