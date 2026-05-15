package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaMetadata

data class NarrativeExecutionEnvironment(
    val getSagaMetadata: () -> SagaMetadata?,
    val getSagaContent: suspend () -> SagaContent?,
    val fetchNarrativeRules: suspend () -> NarrativeRules,
    val onReasoningChunk: (String?) -> Unit,
    val dismissMilestone: () -> Unit,
    val isDebugMode: () -> Boolean,
    val getMessageCount: suspend (sagaId: Int) -> Int,
)
