package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatMessages
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
        character: CharacterContent,
        characterArcs: List<CharacterArc>,
        sceneSummary: SceneSummary,
        updateLimit: Int,
    ): SplitPrompt {
        val presentCharacters =
            sceneSummary.charactersPresent.mapNotNull {
                saga.findCharacter(it)?.data
            }

        val args =
            buildMap {
                put(
                    "characterContext",
                    buildMap {
                        putAll(character.data.asMap())
                        characterArcs.lastOrNull()?.let {
                            put("lastArc", it.toAINormalize())
                        }
                        character.events.lastOrNull()?.let {
                            put("lastEvent", it.event.toAINormalize())
                        }
                        put(
                            "relationshipsWithPresentCharacters",
                            presentCharacters
                                .mapNotNull {
                                    character.findRelationship(it.id)?.summarizeRelation()
                                }.normalizetoAIItems(),
                        )
                    }.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS),
                )
                put(
                    "storyContext",
                    buildMap {
                        put(
                            "storyState",
                            buildMap {
                                putAll(sceneSummary.asMap())
                                saga.flatMessages().lastOrNull()?.let {
                                    put(
                                        "latestMessage",
                                        it.message.toAINormalize(ChatPrompts.messageExclusions),
                                    )
                                }
                            },
                        )
                    },
                )
            }

        return promptService.buildSplitBlueprint(SAGA_INPUT_SUGGESTIONS_BLUEPRINT, args)
    }
}
