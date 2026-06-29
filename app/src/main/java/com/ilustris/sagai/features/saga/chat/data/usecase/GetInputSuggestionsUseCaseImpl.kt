package com.ilustris.sagai.features.saga.chat.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.prompts.SuggestionPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SuggestionGen
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetInputSuggestionsUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val remoteConfigService: RemoteConfigService,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
        private val characterUseCase: CharacterUseCase,
    ) : GetInputSuggestionsUseCase {
        override suspend fun invoke(
            chatMessages: List<MessageContent>,
            currentUserCharacter: Character?,
            saga: SagaContent,
            sceneSummary: SceneSummary?,
        ): RequestResult<List<Suggestion>> =
            executeRequest(false) {
                val contextSummary = sceneSummary ?: error("can't generate suggestions without context")
                val narrativeRules =
                    remoteConfigService.getJson<NarrativeRules>("narrative_rules")!!

                val prompt =
                    SuggestionPrompts.generateSuggestionsPrompt(
                        promptService,
                        saga,
                        character = saga.findCharacter(currentUserCharacter?.id)!!,
                        sceneSummary = contextSummary,
                        updateLimit = narrativeRules.loreUpdateLimit,
                        characterArcs =
                            characterUseCase
                                .getCharacterArcs(currentUserCharacter!!.id)
                                .first(),
                    )

                gemmaClient
                    .generate<SuggestionGen>(
                        promptSplit =
                            prompt.mergeInstructions(
                                genreConfigService.conversationInstructions(saga.data.genre),
                            ),
                        requirement = ModelRequirement.MINIMAL,
                    )!!
                    .suggestions
            }
    }
