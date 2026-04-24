package com.ilustris.sagai.features.newsaga.data.service

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.CharacterIdeationArgs
import com.ilustris.sagai.core.ai.prompts.NewSagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.CharacterIdeas
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import javax.inject.Inject

class CharacterIdeationService
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
    ) {
        suspend fun suggestCharacters(
            sagaDraft: SagaDraft,
            userPrompt: String,
        ): kotlinx.coroutines.flow.Flow<com.ilustris.sagai.core.ai.StreamingState<CharacterIdeas>> {
            val blueprint =
                promptService.buildRemotePrompt(
                    NewSagaPrompts.CHARACTER_IDEATION_BLUEPRINT,
                    CharacterIdeationArgs(
                        sagaName = sagaDraft.title,
                        sagaDescription = sagaDraft.description,
                        userPrompt = userPrompt,
                        themeStyle = genreConfigService.conversationBlueprint(sagaDraft.genre),
                    ),
                )
            return gemmaClient.generateStreaming<CharacterIdeas>(
                blueprint,
                requirement = GemmaClient.ModelRequirement.MEDIUM,
            )
        }
    }
