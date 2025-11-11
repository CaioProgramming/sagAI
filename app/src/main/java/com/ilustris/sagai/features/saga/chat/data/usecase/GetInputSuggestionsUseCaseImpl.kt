package com.ilustris.sagai.features.saga.chat.data.usecase

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.SuggestionPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SuggestionGen
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
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
        ): RequestResult<List<Suggestion>> =
            executeRequest(false) {
                val contextSummary =
                    gemmaClient.generate<SceneSummary>(
                        ChatPrompts.sceneSummarizationPrompt(
                            saga,
                            chatMessages.map { it.joinMessage(true).formatToString(true) },
                        ),
                    )!!
                val prompt =
                    SuggestionPrompts.generateSuggestionsPrompt(
                        character = currentUserCharacter!!,
                        contextSummary,
                    )

                Log.d("GetInputSuggestions", "Sending prompt to GemmaClient for suggestions.")
                gemmaClient.generate<SuggestionGen>(prompt)!!.suggestions
            }
    }
