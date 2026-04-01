package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.usecase.SagaProcess
import com.ilustris.sagai.features.newsaga.ui.presentation.FlowPages

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
    val conversationalStyle: String,
    val sagaDraft: String,
    val characterInfo: String,
    val processName: String,
    val processSpecificInstruction: String,
)

data class CreateSagaArgs(
    val companionPersona: String,
    val conversationalStyle: String,
    val sagaDraft: String,
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

    suspend fun conversationalSagaReply(
        promptService: PromptService,
        currentSagaDraft: SagaDraft,
        userInput: String,
        conversationHistory: List<ChatMessage>,
        availableVariations: Map<String, GenreConfig.VariationConfig> = emptyMap(),
        identity: String,
    ): String {
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

        return promptService.buildRemotePrompt(CONVERSATIONAL_SAGA_REPLY_BLUEPRINT, args)
    }

    suspend fun generateProcessPrompt(
        promptService: PromptService,
        process: SagaProcess,
        saga: SagaForm,
        character: CharacterInfo,
        identity: String = "",
        instruction: String = "",
    ): String {
        val args =
            GenerateProcessArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                processName = process.name,
                processSpecificInstruction = instruction,
                sagaDraft = saga.toAINormalize(),
                characterInfo = character.toAINormalize(),
            )

        return promptService.buildRemotePrompt(SAGA_PROCESS_INTERLUDE_BLUEPRINT, args)
    }

    suspend fun createSagaPrompt(
        promptService: PromptService,
        sagaForm: SagaDraft,
        miniChatContent: List<ChatMessage>,
        availableVariations: Map<String, GenreConfig.VariationConfig> = emptyMap(),
        identity: String = "",
    ): String {
        val variationsBlock =
            availableVariations.entries.joinToString("\n") { (id, config) ->
                "- **$id**: ${config.name} - ${config.description}"
            }

        val args =
            CreateSagaArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                sagaDraft = sagaForm.toAINormalize(),
                miniChatContent = miniChatContent.joinToString("\n") { "${it.sender.name}: ${it.text}" },
                availableVariations = variationsBlock,
            )

        return promptService.buildRemotePrompt(INITIAL_SAGA_KICKOFF_BLUEPRINT, args)
    }

    suspend fun introPrompt(
        promptService: PromptService,
        identity: String = "",
    ): String {
        val args =
            IntroPromptArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                genreEnumNames = Genre.entries.joinToString(", ") { it.name },
            )

        return promptService.buildRemotePrompt(CREATION_INTRO_BLUEPRINT, args)
    }

    suspend fun genreAdaptationPrompt(
        promptService: PromptService,
        currentDraft: SagaDraft,
        identity: String = "",
    ): String {
        val args =
            GenreAdaptationArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                genreName = currentDraft.genre.name,
                currentDraft = currentDraft.toAINormalize(),
            )

        return promptService.buildRemotePrompt(GENRE_ADAPTATION_BLUEPRINT, args)
    }

    suspend fun genreSuggestionsPrompt(
        promptService: PromptService,
        genre: Genre,
        identity: String = "",
    ): String {
        val args =
            GenreSuggestionsArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                genreName = genre.name,
            )

        return promptService.buildRemotePrompt(GENRE_SUGGESTIONS_BLUEPRINT, args)
    }

    suspend fun refineDraftPrompt(
        promptService: PromptService,
        rawInput: String,
        genre: Genre,
        identity: String = "",
    ): String {
        val args =
            NewSagaRefineDraftArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                rawInput = rawInput,
                genreName = genre.name,
            )

        return promptService.buildRemotePrompt(REFINE_SAGA_DRAFT_BLUEPRINT, args)
    }

    suspend fun creationAssistPrompt(
        promptService: PromptService,
        flow: FlowPages,
        sagaDraft: SagaDraft?,
        characterInfo: CharacterInfo?,
        genreConfig: GenreConfig?,
        flowSpecificObjectives: String,
        identity: String = "",
    ): String {
        val args =
            CreationAssistArgs(
                companionPersona = identity,
                conversationalStyle = identity,
                flowPageName = flow.name,
                genreName = sagaDraft?.genre?.name ?: "N/A",
                sagaDraft = sagaDraft?.toAINormalize() ?: "",
                characterInfo = characterInfo?.toAINormalize() ?: "",
                flowSpecificObjectives = flowSpecificObjectives,
            )

        return promptService.buildRemotePrompt(CREATION_FLOW_ASSIST_BLUEPRINT, args)
    }
}
