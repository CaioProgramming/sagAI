package com.ilustris.sagai.features.wiki.domain.usecase

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki
import kotlinx.coroutines.flow.Flow

interface WikiUseCase {
    fun getWikisBySaga(sagaId: Int): Flow<List<Wiki>>

    suspend fun getWikiById(wikiId: Int): Wiki?

    suspend fun saveWiki(wiki: Wiki): Long

    suspend fun updateWiki(wiki: Wiki)

    suspend fun deleteWiki(wikiId: Int)

    suspend fun deleteWikisBySaga(sagaId: Int)

    suspend fun generateWiki(
        saga: SagaContent,
        events: List<Timeline>,
    ): List<Wiki>
}
