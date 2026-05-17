package com.ilustris.sagai.features.newsaga.ui.presentation

import androidx.navigation3.runtime.NavKey
import com.ilustris.sagai.features.home.data.model.Saga

data class CreateSagaState(
    val isLoading: Boolean = false,
    val saga: Saga? = null,
    val errorMessage: String? = null,
    val continueAction: Pair<String, () -> Unit>? = null,
)

sealed interface Effect {
    data class Navigate(
        val key: NavKey,
    ) : Effect
}
