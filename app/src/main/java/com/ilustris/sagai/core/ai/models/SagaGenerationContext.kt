package com.ilustris.sagai.core.ai.models

import com.ilustris.sagai.features.newsaga.data.model.SagaForm

data class SagaGenerationContext(
    val sagaSetup: SagaForm,
    val initialPlayerInteractionLog: String,
)