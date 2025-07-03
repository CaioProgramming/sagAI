package com.ilustris.sagai.features.newsaga.ui.presentation

import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.ui.navigation.Routes

data class CreateSagaState(
    val isLoading: Boolean = false,
    val saga: SagaData? = null,
    val errorMessage: String? = null,
    val continueAction: Pair<String, () -> Unit>? = null,
)

sealed interface Effect {
    data class Navigate(
        val route: Routes,
        val arguments: Map<String, String>,
    ) : Effect
}
