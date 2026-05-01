package com.ilustris.sagai.features.act.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.Book
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.flow.Flow

interface BookUseCase {
    fun generateBookStream(
        saga: SagaContent,
        actContent: ActContent,
    ): Flow<StreamingState<GeneratedContent<Book>>>

    /**
     * Triggers the eager sequential generation of all completed acts in a saga.
     * Emits each book as it becomes ready.
     */
    fun generateSagaChronicles(saga: SagaContent): Flow<StreamingState<GeneratedContent<Book>>>
}
