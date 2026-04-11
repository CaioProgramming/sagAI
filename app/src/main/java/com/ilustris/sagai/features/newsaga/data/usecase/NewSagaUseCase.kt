package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.CreationAssist
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.ui.presentation.FlowPages
import kotlinx.coroutines.flow.Flow

sealed class AgenticFlowResponse {
    data class Log(
        val message: String,
    ) : AgenticFlowResponse()

    data class SagaPitches(
        val ideas: List<SagaDraft>,
        val message: String? = null,
    ) : AgenticFlowResponse()

    data class CharacterPitches(
        val personas: List<CharacterInfo>,
        val message: String? = null,
    ) : AgenticFlowResponse()

    data class RefinedDraft(
        val saga: SagaDraft,
        val character: CharacterInfo,
    ) : AgenticFlowResponse()

    data class Error(
        val throwable: Throwable,
    ) : AgenticFlowResponse()
}

sealed class AgenticUIComponent {
    data class AgentMessage(
        val text: String,
    ) : AgenticUIComponent()

    data class IdeaPitches(
        val ideas: List<Pair<SagaDraft, GenreVisualConfig>>,
    ) : AgenticUIComponent()

    data class ExpandedSaga(
        val draft: SagaDraft,
        val visualConfig: GenreVisualConfig?,
    ) : AgenticUIComponent()

    data class PersonaPitches(
        val personas: List<CharacterInfo>,
        val visuals: Pair<Genre, GenreVisualConfig?>,
    ) : AgenticUIComponent()

    data class ExpandedCharacter(
        val persona: CharacterInfo,
        val visuals: Pair<Genre, GenreVisualConfig?>,
    ) : AgenticUIComponent()
}

sealed class SagaCreationState {
    data class Loading(
        val message: String,
    ) : SagaCreationState()

    data class Success(
        val saga: Saga,
        val character: Character,
    ) : SagaCreationState()

    data class Error(
        val error: Throwable,
    ) : SagaCreationState()

    data class AgenticUpdate(
        val response: AgenticFlowResponse,
    ) : SagaCreationState()
}

enum class SagaProcess {
    CREATING_SAGA,
    CREATING_CHARACTER,
    FINALIZING,
    SUCCESS,
    LISTENING,
    SAVED_CHARACTER,
}

interface NewSagaUseCase {
    fun createCompleteSagaFlow(
        sagaDraft: SagaDraft,
        characterInfo: CharacterInfo,
        sagaMessages: List<ChatMessage>,
    ): Flow<SagaCreationState>

    suspend fun createSaga(saga: Saga): RequestResult<Saga>

    suspend fun updateSaga(saga: Saga): RequestResult<Saga>

    suspend fun deleteSaga(saga: Saga): RequestResult<Unit>

    suspend fun generateSaga(
        sagaForm: SagaDraft,
        miniChatContent: List<ChatMessage>,
    ): RequestResult<Saga>

    suspend fun generateSagaIcon(
        sagaForm: Saga,
        character: Character,
    ): RequestResult<Saga>

    suspend fun replyAiForm(
        currentMessages: List<ChatMessage>,
        latestMessage: String?,
        currentFormData: SagaForm,
    ): RequestResult<SagaCreationGen>

    suspend fun assistCreation(
        flow: FlowPages,
        sagaDraft: SagaDraft?,
        characterInfo: CharacterInfo?,
    ): RequestResult<CreationAssist>

    suspend fun generateIntroduction(): RequestResult<SagaCreationGen>

    suspend fun generateCharacterIntroduction(sagaContext: SagaDraft?): RequestResult<SagaCreationGen>

    suspend fun generateProcessMessage(
        process: SagaProcess,
        saga: SagaForm,
        character: CharacterInfo,
        genre: Genre? = null,
    ): RequestResult<String>

    suspend fun adaptSagaToGenre(sagaDraft: SagaDraft): RequestResult<SagaCreationGen>

    suspend fun generateGenreSuggestions(genre: Genre): RequestResult<SagaCreationGen>

    suspend fun refineDraft(
        rawInput: String,
        genre: Genre,
    ): RequestResult<SagaCreationGen>

    fun executePrompt(
        prompt: String,
        lockedSaga: SagaDraft? = null,
        lockedCharacter: CharacterInfo? = null,
    ): Flow<AgenticFlowResponse>
}
