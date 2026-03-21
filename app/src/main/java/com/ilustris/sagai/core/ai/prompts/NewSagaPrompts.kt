package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.usecase.SagaProcess
import com.ilustris.sagai.features.newsaga.ui.presentation.FlowPages

data class ConversationalSagaReplyArgs(
    val currentSagaDraft: String,
    val conversationHistory: String,
    val userInput: String,
    val availableVariations: String,
    val companionConversationalStyle: String,
)

data class GenerateProcessArgs(
    val companionPersona: String,
    val interludeStyle: String,
    val processName: String,
    val sagaDescription: String,
    val characterDescription: String,
    val processSpecificInstruction: String,
)

data class CreateSagaArgs(
    val companionPersona: String,
    val sagaForm: String,
    val miniChatContent: String,
    val availableVariations: String,
)

data class CharacterSavedArgs(
    val companionPersona: String,
    val characterName: String,
    val characterBackstory: String,
    val sagaTitle: String,
    val sagaDescription: String,
)

data class IntroPromptArgs(
    val companionPersona: String,
    val conversationalStyle: String,
    val genreEnumNames: String,
)

data class GenreAdaptationArgs(
    val companionPersona: String,
    val genreName: String,
    val currentDraft: String,
)

data class GenreSuggestionsArgs(
    val companionPersona: String,
    val genreName: String,
)

data class NewSagaRefineDraftArgs(
    val companionPersona: String,
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
                currentSagaDraft = currentSagaDraft.toAINormalize(),
                conversationHistory =
                    conversationHistory.takeLast(10).joinToString("\n") { msg ->
                        "${msg.sender.name}: ${msg.text}"
                    },
                userInput = userInput,
                availableVariations = variationsBlock,
                companionConversationalStyle = identity,
            )

        return promptService.buildRemotePrompt("conversational_saga_reply_blueprint", args)
    }

    suspend fun generateProcessPrompt(
        promptService: PromptService,
        process: SagaProcess,
        saga: String,
        character: String,
        identity: String = "",
    ): String {
        val processSpecificInstruction =
            when (process) {
                SagaProcess.LISTENING -> "Generate a message about listening to the user's input. Playful tone."
                SagaProcess.CREATING_SAGA -> "Generate a message about building a universe from scratch. Pressure joke."
                SagaProcess.CREATING_CHARACTER -> "Generate a message about crafting a hero. Dramatic or cliché fun."
                SagaProcess.FINALIZING -> "Generate a message about final touches. Impatient or dramatic."
                SagaProcess.SUCCESS -> "Generate a triumphant (and slightly smug) message that the saga is ready."
            }

        val args =
            GenerateProcessArgs(
                companionPersona = identity,
                interludeStyle = identity,
                processName = process.name,
                sagaDescription = saga,
                characterDescription = character,
                processSpecificInstruction = processSpecificInstruction,
            )

        return promptService.buildRemotePrompt("saga_process_interlude_blueprint", args)
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
                sagaForm = sagaForm.toAINormalize(),
                miniChatContent = miniChatContent.joinToString("\n") { "${it.sender.name}: ${it.text}" },
                availableVariations = variationsBlock,
            )

        return promptService.buildRemotePrompt("initial_saga_kickoff_blueprint", args)
    }

    suspend fun characterSavedPrompt(
        promptService: PromptService,
        character: Character,
        saga: Saga,
        identity: String = "",
    ): String {
        val args =
            CharacterSavedArgs(
                companionPersona = identity,
                characterName = character.name,
                characterBackstory = character.backstory,
                sagaTitle = saga.title,
                sagaDescription = saga.description,
            )

        return promptService.buildRemotePrompt("character_saved_blueprint", args)
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

        return promptService.buildRemotePrompt("creation_intro_blueprint", args)
    }

    suspend fun genreAdaptationPrompt(
        promptService: PromptService,
        currentDraft: SagaDraft,
        identity: String = "",
    ): String {
        val args =
            GenreAdaptationArgs(
                companionPersona = identity,
                genreName = currentDraft.genre.name,
                currentDraft = currentDraft.toAINormalize(),
            )

        return promptService.buildRemotePrompt("genre_adaptation_blueprint", args)
    }

    suspend fun genreSuggestionsPrompt(
        promptService: PromptService,
        genre: Genre,
        identity: String = "",
    ): String {
        val args =
            GenreSuggestionsArgs(
                companionPersona = identity,
                genreName = genre.name,
            )

        return promptService.buildRemotePrompt("genre_suggestions_blueprint", args)
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
                rawInput = rawInput,
                genreName = genre.name,
            )

        return promptService.buildRemotePrompt("refine_saga_draft_blueprint", args)
    }

    suspend fun creationAssistPrompt(
        promptService: PromptService,
        flow: FlowPages,
        sagaDraft: SagaDraft?,
        characterInfo: CharacterInfo?,
        genreConfig: GenreConfig?,
        flowSpecificObjectives: String,
    ): String {
        val args =
            CreationAssistArgs(
                companionPersona = "",
                conversationalStyle = "",
                flowPageName = flow.name,
                genreName = sagaDraft?.genre?.name ?: "N/A",
                sagaDraft = sagaDraft?.toAINormalize() ?: "",
                characterInfo = characterInfo?.toAINormalize() ?: "",
                flowSpecificObjectives = flowSpecificObjectives,
            )

        return promptService.buildRemotePrompt("creation_flow_assist_blueprint", args)
    }
}
