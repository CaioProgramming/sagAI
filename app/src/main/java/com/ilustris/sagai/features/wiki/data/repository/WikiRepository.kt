package com.ilustris.sagai.features.wiki.data.repository

import com.ilustris.sagai.features.wiki.data.model.Wiki
import kotlinx.coroutines.flow.Flow

interface WikiRepository {
    fun getWikisBySaga(sagaId: Int): Flow<List<Wiki>>
    suspend fun getWikiById(wikiId: Int): Wiki?
    suspend fun insertWiki(wiki: Wiki): Long
    suspend fun updateWiki(wiki: Wiki)
    suspend fun deleteWiki(wikiId: Int)
    suspend fun deleteWikisBySaga(sagaId: Int)
}
