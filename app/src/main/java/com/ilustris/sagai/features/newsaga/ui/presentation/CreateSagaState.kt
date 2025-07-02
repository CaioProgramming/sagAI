package com.ilustris.sagai.features.newsaga.ui.presentation

import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData

sealed class CreateSagaState {
    data class Success(
        val saga: SagaData,
    ) : CreateSagaState()

    data class Error(
        val exception: Exception,
    ) : CreateSagaState()

    data class GeneratedSaga(
        val saga: SagaData,
    ) : CreateSagaState()

    object Initial : CreateSagaState()

    object Loading : CreateSagaState()
}
