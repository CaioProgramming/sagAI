package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary

data class SuggestionArgs(
    val characterName: String,
    val sagaContext: String,
    val sceneContext: String,
    val charactersPresent: String,
    val relationships: String,
    val conversationHistory: String,
)

object SuggestionPrompts {
    const val SAGA_INPUT_SUGGESTIONS_BLUEPRINT = "saga_input_suggestions_blueprint"

    @Suppress("ktlint:standard:max-line-length")
    suspend fun generateSuggestionsPrompt(
        promptService: PromptService,
        saga: SagaContent,
        character: Character,
        sceneSummary: SceneSummary,
        updateLimit: Int,
    ): String {
        val charactersInScene =
            sceneSummary.charactersPresent.mapNotNull {
                saga.findCharacter(it)
            }

        val nonMainCharacters =
            charactersInScene
                .filter { it.data.id != saga.mainCharacter?.data?.id }
                .map { it.data }

        val relationshipsBlock =
            charactersInScene.joinToString("\n") { characterContent ->
                "- ${characterContent.data.name} relationships: ${
                    characterContent.summarizeRelationships(
                        1,
                    )
                }"
            }

        val args =
            SuggestionArgs(
                characterName = character.name,
                sagaContext = SagaPrompts.mainContext(saga),
                sceneContext = sceneSummary.toAINormalize(),
                charactersPresent = CharacterPrompts.charactersOverview(nonMainCharacters),
                relationships = relationshipsBlock,
                conversationHistory =
                    ChatPrompts.conversationHistory(
                        updateLimit,
                        saga,
                        updateLimit / 2,
                    ),
            )

        return promptService.buildRemotePrompt(SAGA_INPUT_SUGGESTIONS_BLUEPRINT, args)
    }
}
