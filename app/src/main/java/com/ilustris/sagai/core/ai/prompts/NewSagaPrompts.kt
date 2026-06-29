package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.usecase.SagaProcess

data class ConversationalSagaReplyArgs(
    val companionPersona: String,
    val conversationalStyle: String,
    val currentSagaDraft: String,
    val conversationHistory: String,
    val userInput: String,
    val availableVariations: String,
)

data class GenerateProcessArgs(
    val companionPersona: String,
    val conversationDirective: String,
    val sagaBrief: String,
    val characterBrief: String,
    val processName: String,
    val processSpecificInstruction: String,
)

data class CreateSagaArgs(
    val companionPersona: String,
    val conversationalStyle: String,
    val sagaForm: String,
    val miniChatContent: String,
    val availableVariations: String,
)

data class IntroPromptArgs(
    val companionPersona: String,
    val conversationalStyle: String,
    val genreEnumNames: String,
)

data class GenreAdaptationArgs(
    val companionPersona: String,
    val conversationalStyle: String,
    val genreName: String,
    val currentDraft: String,
)

data class GenreSuggestionsArgs(
    val companionPersona: String,
    val conversationalStyle: String,
    val genreName: String,
)

data class NewSagaRefineDraftArgs(
    val companionPersona: String,
    val conversationalStyle: String,
    val rawInput: String,
    val genreName: String,
)

data class CreationAssistArgs(
    val companionPersona: String,
    val conversationalStyle: String,
    val flowPageName: String,
    val genreName: String,
    val sagaDraft: String,
    val characterInfo: String,
    val flowSpecificObjectives: String,
)

data class AgenticWelcomeArgs(
    val availableGenres: String,
)

data class SagaIdeationArgs(
    val userPrompt: String,
    val themes: String,
)

data class CharacterIdeationArgs(
    val sagaName: String,
    val sagaDescription: String,
    val userPrompt: String,
    val themeStyle: String,
)

data class SacredBindingArgs(
    val companionPersona: String,
    val sagaDraft: String,
    val characterInfo: String,
    val genreName: String,
    val themeStyle: String,
)

data class CosmicLibraryArgs(
    val userPrompt: String,
    val themes: String,
    val genreAesthetics: String = "",
)

@Suppress("ktlint:standard:max-line-length")
object NewSagaPrompts {
    const val CONVERSATIONAL_SAGA_REPLY_BLUEPRINT = "conversational_saga_reply_blueprint"
    const val CREATION_FLOW_ASSIST_BLUEPRINT = "creation_flow_assist_blueprint"
    const val CREATION_INTRO_BLUEPRINT = "creation_intro_blueprint"
    const val GENRE_ADAPTATION_BLUEPRINT = "genre_adaptation_blueprint"
    const val GENRE_SUGGESTIONS_BLUEPRINT = "genre_suggestions_blueprint"
    const val INITIAL_SAGA_KICKOFF_BLUEPRINT = "initial_saga_kickoff_blueprint"
    const val REFINE_SAGA_DRAFT_BLUEPRINT = "refine_saga_draft_blueprint"
    const val SAGA_PROCESS_INTERLUDE_BLUEPRINT = "saga_process_interlude_blueprint"

    // Agentic Flow Blueprints
    const val AGENTIC_WELCOME_BLUEPRINT = "agentic_welcome_blueprint"
    const val CHARACTER_IDEATION_BLUEPRINT = "character_ideation_blueprint"
    const val CHARACTER_IDEATION_PROCESS = "character_task"
    const val SAAGA_IDEATION_PROCESS = "ideation_task"
    const val COSMIC_LIBRARY_BLUEPRINT = "cosmic_library_blueprint"
    const val UNIVERSE_ECHOES_BLUEPRINT = "universe_echoes_blueprint"
    const val SACRED_BINDING_BLUEPRINT = "sacred_binding_blueprint"

    suspend fun conversationalSagaReply(
        promptService: PromptService,
        currentSagaDraft: SagaDraft,
        userInput: String,
        conversationHistory: List<ChatMessage>,
        availableVariations: Map<String, GenreConfig.VariationConfig> = emptyMap(),
        identity: String,
    ): SplitPrompt {
        val variationsBlock =
            availableVariations.entries.joinToString("\n") { (id, config) ->
                "- **$id**: ${config.name} - ${config.description}"
            }

        val args =
            ConversationalSagaReplyArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                currentSagaDraft = currentSagaDraft.toAINormalize(),
                conversationHistory =
                    conversationHistory.takeLast(10).joinToString("\n") { msg ->
                        "${msg.sender.name}: ${msg.text}"
                    },
                userInput = userInput,
                availableVariations = variationsBlock,
            )

        return promptService.buildSplitBlueprint(CONVERSATIONAL_SAGA_REPLY_BLUEPRINT, args)
    }

    suspend fun generateProcessPrompt(
        promptService: PromptService,
        process: SagaProcess,
        saga: SagaForm,
        character: CharacterInfo?,
        identity: String = "",
        instruction: String = "",
    ): SplitPrompt {
        val args =
            GenerateProcessArgs(
                companionPersona = identity,
                conversationDirective = identity,
                processName = process.name,
                processSpecificInstruction = instruction,
                sagaBrief = saga.toAINormalize(),
                characterBrief = character.toAINormalize(),
            )

        return promptService.buildSplitBlueprint(SAGA_PROCESS_INTERLUDE_BLUEPRINT, args)
    }

    suspend fun suggestingCharacters(
        sagaDraft: SagaDraft?,
        promptService: PromptService,
    ): SplitPrompt {
        val args =
            mapOf(
                "context" to sagaDraft.toAINormalize(),
            )

        return promptService.buildSplitBlueprint(CHARACTER_IDEATION_PROCESS, args)
    }

    suspend fun suggestingSagas(promptService: PromptService): SplitPrompt =
        promptService.buildSplitBlueprint(UNIVERSE_ECHOES_BLUEPRINT)

    suspend fun createSagaPrompt(
        promptService: PromptService,
        sagaForm: SagaDraft,
        miniChatContent: List<ChatMessage>,
        availableVariations: Map<String, GenreConfig.VariationConfig> = emptyMap(),
        identity: String,
    ): SplitPrompt {
        val variationsBlock =
            availableVariations.entries.joinToString("\n") { (id, config) ->
                "- **$id**: ${config.name} - ${config.description}"
            }

        val args =
            CreateSagaArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                sagaForm = sagaForm.toAINormalize(),
                miniChatContent = miniChatContent.joinToString("\n") { "${it.sender.name}: ${it.text}" },
                availableVariations = variationsBlock,
            )

        return promptService.buildSplitBlueprint(INITIAL_SAGA_KICKOFF_BLUEPRINT, args)
    }

    suspend fun introPrompt(
        promptService: PromptService,
        identity: String = "",
    ): SplitPrompt {
        val args =
            IntroPromptArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                genreEnumNames = Genre.entries.joinToString(", ") { it.name },
            )

        return promptService.buildSplitBlueprint(CREATION_INTRO_BLUEPRINT, args)
    }

    suspend fun genreAdaptationPrompt(
        promptService: PromptService,
        currentDraft: SagaDraft,
        identity: String = "",
    ): SplitPrompt {
        val args =
            GenreAdaptationArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                genreName = currentDraft.genre.name,
                currentDraft = currentDraft.toAINormalize(),
            )

        return promptService.buildSplitBlueprint(GENRE_ADAPTATION_BLUEPRINT, args)
    }

    suspend fun genreSuggestionsPrompt(
        promptService: PromptService,
        genre: Genre,
        identity: String = "",
    ): SplitPrompt {
        val args =
            GenreSuggestionsArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                genreName = genre.name,
            )

        return promptService.buildSplitBlueprint(GENRE_SUGGESTIONS_BLUEPRINT, args)
    }

    suspend fun refineDraftPrompt(
        promptService: PromptService,
        rawInput: String,
        genre: Genre,
        identity: String = "",
    ): SplitPrompt {
        val args =
            NewSagaRefineDraftArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                rawInput = rawInput,
                genreName = genre.name,
            )

        return promptService.buildSplitBlueprint(REFINE_SAGA_DRAFT_BLUEPRINT, args)
    }

    suspend fun sacredBindingPrompt(
        promptService: PromptService,
        sagaDraft: SagaDraft,
        characterInfo: CharacterInfo,
        themeStyle: String = "",
    ): SplitPrompt {
        val args =
            SacredBindingArgs(
                companionPersona = emptyString(),
                sagaDraft = sagaDraft.toAINormalize(),
                characterInfo = characterInfo.toAINormalize(),
                genreName = sagaDraft.genre.name,
                themeStyle = themeStyle,
            )
        return promptService.buildSplitBlueprint(SACRED_BINDING_BLUEPRINT, args)
    }
}
