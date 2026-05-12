package com.ilustris.sagai.features.saga.chat.repository

import android.net.Uri
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaInfo
import kotlinx.coroutines.flow.Flow

interface SagaRepository {
    fun getChats(): Flow<List<SagaContent>>

    fun getSagaSummaries(): Flow<List<com.ilustris.sagai.features.home.data.model.SagaSummary>>

    fun getSagaById(id: Int?): Flow<SagaContent?>

    fun getSagaMetadata(id: Int): Flow<com.ilustris.sagai.features.home.data.model.SagaMetadata?>

    fun getSagaInfo(id: Int): Flow<SagaInfo?>

    fun getAllSagas(): Flow<List<Saga>>

    fun getPlaythroughData(): Flow<List<com.ilustris.sagai.features.playthrough.data.model.SagaPlaythrough>>

    suspend fun saveChat(saga: Saga): Saga

    suspend fun updateSaga(saga: Saga): Saga

    suspend fun deleteChat(saga: Saga)

    suspend fun deleteChatById(id: String)

    suspend fun deleteAllChats()

    suspend fun generateSagaIcon(
        saga: Saga,
        characters: List<Character>,
    ): RequestResult<Saga>

    fun generateSagaIconStream(
        saga: Saga,
        characters: List<Character>,
    ): Flow<StreamingState<Saga>>

    suspend fun backupSaga(sagaContent: SagaContent): RequestResult<Uri>
}
