package com.ilustris.sagai.features.act.data.model

import com.ilustris.sagai.features.newsaga.data.model.Genre

sealed interface BookGenerationUiState {
    data object Idle : BookGenerationUiState

    data class Generating(
        val sagaId: Int,
        val sagaTitle: String,
        val actId: Int,
        val actTitle: String,
        val genre: Genre,
        val reasoning: String?,
    ) : BookGenerationUiState

    data class Error(
        val sagaId: Int,
        val actId: Int,
        val message: String,
    ) : BookGenerationUiState
}
