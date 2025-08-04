package com.ilustris.sagai.features.home.data.usecase

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeUseCaseImpl @Inject constructor(
    private val sagaRepository: SagaRepository,
    private val gemmaClient: GemmaClient
) : HomeUseCase {

    override fun getSagas(): Flow<List<SagaContent>> {
        return sagaRepository.getChats().map { content ->
            processSagaContent(content)
        }
    }

    override suspend fun fetchDynamicNewSagaTexts(): DynamicSagaPrompt? {
        Log.d("HomeUseCaseImpl", "Fetching new dynamic saga texts...")
        val prompt = "Generate a JSON object with two fields: 'title' and 'subtitle'. The 'title' should be a very short, catchy, and inviting call to action for a user to start writing a new story. The 'subtitle' should be a slightly longer, cozy, funny, and engaging message that sparks curiosity and encourages them to begin their narrative adventure. Ensure the tone is welcoming and optimistic. The response should be suitable for direct display in a user interface."
        return try {
            val result = gemmaClient.generate<DynamicSagaPrompt>(prompt, requireTranslation = true)
            if (result == null) {
                Log.e("HomeUseCaseImpl", "GemmaClient returned null for dynamic saga texts.")
            } else {
                Log.d("HomeUseCaseImpl", "Dynamic texts received: ${result.title}")
            }
            result
        } catch (e: Exception) {
            Log.e("HomeUseCaseImpl", "Error fetching dynamic saga texts: ${e.message}", e)
            null
        }
    }

    override suspend fun createFakeSaga(): RequestResult<Exception, Saga> = // Added this method
        try {
            sagaRepository
                .saveChat(
                    Saga(
                        title = "Debug Saga",
                        description = "This saga was created for debug purposes only.",
                        genre = Genre.entries.random(),
                        isDebug = true,
                    ),
                ).asSuccess()
        } catch (e: Exception) {
            e.asError()
        }

    private fun processSagaContent(content: List<SagaContent>): List<SagaContent> {
        val mappedSagas =
            content.map { saga ->
                saga.copy(
                    messages = saga.messages.sortedByDescending { it.message.timestamp },
                )
            }
        return mappedSagas.sortedByDescending { saga ->
            saga.messages
                .firstOrNull()
                ?.message
                ?.timestamp ?: 0L
        }
    }
}
