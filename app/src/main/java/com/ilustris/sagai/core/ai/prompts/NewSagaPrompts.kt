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
    val companionPersona: String,
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
    val flowName: String,
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
        companion: GenreConfig.CompanionConfig? = null,
    ): String {
        val variationsBlock =
            availableVariations.entries.joinToString("\n") { (id, config) ->
                "- **$id**: ${config.name} - ${config.description}"
            }

        val args =
            ConversationalSagaReplyArgs(
                companionPersona = companion?.persona ?: "",
                currentSagaDraft = currentSagaDraft.toAINormalize(),
                conversationHistory =
                    conversationHistory.takeLast(10).joinToString("\n") { msg ->
                        "${msg.sender.name}: ${msg.text}"
                    },
                userInput = userInput,
                availableVariations = variationsBlock,
                companionConversationalStyle = companion?.conversationalStyle ?: "",
            )

        return promptService.buildRemotePrompt("conversational_saga_reply_blueprint", args)
    }

    suspend fun generateProcessPrompt(
        promptService: PromptService,
        process: SagaProcess,
        saga: String,
        character: String,
        companion: GenreConfig.CompanionConfig? = null,
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
                companionPersona = companion?.persona ?: "",
                interludeStyle = companion?.interludeStyle ?: "",
                processName = process.name,
                sagaDescription = saga,
                characterDescription = character,
                processSpecificInstruction = processSpecificInstruction,
            )

        return promptService.buildRemotePrompt("saga_process_blueprint", args)
    }

    suspend fun createSagaPrompt(
        promptService: PromptService,
        sagaForm: SagaDraft,
        miniChatContent: List<ChatMessage>,
        availableVariations: Map<String, GenreConfig.VariationConfig> = emptyMap(),
        companion: GenreConfig.CompanionConfig? = null,
    ): String {
        val variationsBlock =
            availableVariations.entries.joinToString("\n") { (id, config) ->
                "- **$id**: ${config.name} - ${config.description}"
            }

        val args =
            CreateSagaArgs(
                companionPersona = companion?.persona ?: "",
                sagaForm = sagaForm.toAINormalize(),
                miniChatContent = miniChatContent.joinToString("\n") { "${it.sender.name}: ${it.text}" },
                availableVariations = variationsBlock,
            )

        return promptService.buildRemotePrompt("create_saga_blueprint", args)
    }

    suspend fun characterSavedPrompt(
        promptService: PromptService,
        character: Character,
        saga: Saga,
        companion: GenreConfig.CompanionConfig? = null,
    ): String {
        val args =
            CharacterSavedArgs(
                companionPersona = companion?.persona ?: "",
                characterName = character.name,
                characterBackstory = character.backstory,
                sagaTitle = saga.title,
                sagaDescription = saga.description,
            )

        return promptService.buildRemotePrompt("character_saved_blueprint", args)
    }

    suspend fun introPrompt(
        promptService: PromptService,
        companion: GenreConfig.CompanionConfig? = null,
    ): String {
        val args =
            IntroPromptArgs(
                companionPersona = companion?.persona ?: "",
                conversationalStyle = companion?.conversationalStyle ?: "",
                genreEnumNames = Genre.entries.joinToString(", ") { it.name },
            )

        return promptService.buildRemotePrompt("new_saga_intro_blueprint", args)
    }

    suspend fun genreAdaptationPrompt(
        promptService: PromptService,
        currentDraft: SagaDraft,
        companion: GenreConfig.CompanionConfig? = null,
    ): String {
        val args =
            GenreAdaptationArgs(
                companionPersona = companion?.persona ?: "",
                genreName = currentDraft.genre.name,
                currentDraft = currentDraft.toAINormalize(),
            )

        return promptService.buildRemotePrompt("genre_adaptation_blueprint", args)
    }

    suspend fun genreSuggestionsPrompt(
        promptService: PromptService,
        genre: Genre,
        companion: GenreConfig.CompanionConfig? = null,
    ): String {
        val args =
            GenreSuggestionsArgs(
                companionPersona = companion?.persona ?: "",
                genreName = genre.name,
            )

        return promptService.buildRemotePrompt("genre_suggestions_blueprint", args)
    }

    suspend fun refineDraftPrompt(
        promptService: PromptService,
        rawInput: String,
        genre: Genre,
        companion: GenreConfig.CompanionConfig? = null,
    ): String {
        val args =
            NewSagaRefineDraftArgs(
                companionPersona = companion?.persona ?: "",
                rawInput = rawInput,
                genreName = genre.name,
            )

        return promptService.buildRemotePrompt("new_saga_refine_draft_blueprint", args)
    }

    suspend fun creationAssistPrompt(
        promptService: PromptService,
        flow: FlowPages,
        sagaDraft: SagaDraft?,
        characterInfo: CharacterInfo?,
        genreConfig: GenreConfig?,
    ): String {
        val flowSpecificObjectives =
            when (flow) {
                FlowPages.CREATE_SAGA -> "Title: Humorous CTA. Subtitle: Witty nudge. Input Hint: Inspiring prompt. Suggestions: 3 wild story seeds."
                FlowPages.CREATE_CHARACTER -> "Title: Character CTA. Subtitle: Funny nudge. Input Hint: Targeted prompt. Suggestions: 3 archetypes."
                FlowPages.SELECT_THEME -> "Title: Welcoming CTA. Subtitle: Witty nudge. Input Hint: None. Suggestions: None."
                else -> ""
            }

        val args =
            CreationAssistArgs(
                companionPersona = genreConfig?.companion?.persona ?: "",
                conversationalStyle =
                    genreConfig?.companion?.let { it.conversationalStyle }
                        ?: genreConfig?.let { it.conversationDirective } ?: "",
                flowName = flow.name,
                genreName = sagaDraft?.genre?.name ?: "N/A",
                sagaDraft = sagaDraft?.toAINormalize() ?: "",
                characterInfo = characterInfo?.toAINormalize() ?: "",
                flowSpecificObjectives = flowSpecificObjectives,
            )

        return promptService.buildRemotePrompt("creation_assist_blueprint", args)
    }
}
