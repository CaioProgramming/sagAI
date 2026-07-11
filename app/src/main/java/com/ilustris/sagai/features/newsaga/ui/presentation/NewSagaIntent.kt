package com.ilustris.sagai.features.newsaga.ui.presentation

import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.UniverseEcho

sealed interface NewSagaIntent {
    data class SubmitPrompt(
        val prompt: String,
    ) : NewSagaIntent

    data class SelectSaga(
        val draft: SagaDraft,
    ) : NewSagaIntent

    data class SelectCharacter(
        val info: CharacterInfo,
    ) : NewSagaIntent

    data object UnlockSaga : NewSagaIntent

    data object UnlockCharacter : NewSagaIntent

    data object SaveSaga : NewSagaIntent

    data class UpdateSaga(
        val id: String?,
        val titleInput: String,
        val descriptionInput: String,
    ) : NewSagaIntent

    data class UpdateCharacter(
        val id: String?,
        val nameInput: String,
        val descriptionInput: String,
    ) : NewSagaIntent

    data class SelectEcho(
        val echo: UniverseEcho,
    ) : NewSagaIntent

    data object LoadMore : NewSagaIntent

    data object NavigateBack : NewSagaIntent
}
