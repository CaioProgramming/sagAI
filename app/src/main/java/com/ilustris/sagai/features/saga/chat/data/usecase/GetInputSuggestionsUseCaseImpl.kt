package com.ilustris.sagai.features.saga.chat.data.usecase

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.SuggestionPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SuggestionGen
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import javax.inject.Inject

class GetInputSuggestionsUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
    ) : GetInputSuggestionsUseCase {
        override suspend fun invoke(
            chatMessages: List<MessageContent>,
            currentUserCharacter: Character?,
            saga: SagaContent,
            sceneSummary: SceneSummary?,
        ): RequestResult<List<Suggestion>> =
            executeRequest(false) {
                val contextSummary = sceneSummary!!
                val prompt =
                    SuggestionPrompts.generateSuggestionsPrompt(
                        character = currentUserCharacter!!,
                        contextSummary,
                    )

                Log.d("GetInputSuggestions", "Sending prompt to GemmaClient for suggestions.")
                gemmaClient.generate<SuggestionGen>(prompt)!!.suggestions
            }
    }
