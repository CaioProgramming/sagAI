package com.ilustris.sagai.features.saga.chat.domain.usecase

import com.google.firebase.ai.type.Schema
import com.ilustris.sagai.core.ai.SummarizationClient
import com.ilustris.sagai.core.ai.prompts.SuggestionPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.toFirebaseSchema
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.domain.model.StructuredSuggestion
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import javax.inject.Inject

class GetInputSuggestionsUseCaseImpl
    @Inject
    constructor(
        private val summarizationClient: SummarizationClient,
    ) : GetInputSuggestionsUseCase {
        override suspend fun invoke(
            chatMessages: List<MessageContent>,
            currentUserCharacter: Character?,
            sagaData: SagaData,
        ): RequestResult<Exception, List<StructuredSuggestion>> = // Updated return type
            try {


                val prompt =
                    SuggestionPrompts.generateSuggestionsPrompt(
                        sagaData = sagaData,
                        character = currentUserCharacter!!,
                        chatHistory = chatMessages,
                    )

                val suggestionItemSchema = toFirebaseSchema(StructuredSuggestion::class.java)
                val suggestionsSchema = Schema.array(suggestionItemSchema)

                val suggestions =
                    summarizationClient.generate<List<StructuredSuggestion>>(
                        prompt = prompt,
                        customSchema = suggestionsSchema,
                        requireTranslation = true,
                    )

                suggestions!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }
    }
