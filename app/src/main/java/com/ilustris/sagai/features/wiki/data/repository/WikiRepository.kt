package com.ilustris.sagai.features.wiki.data.repository

import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.model.WikiWithChapter
import kotlinx.coroutines.flow.Flow

interface WikiRepository {
    fun getWikisBySaga(sagaId: Int): Flow<List<Wiki>>

    fun getWikisWithChapter(sagaId: Int): Flow<List<WikiWithChapter>>

    suspend fun getWikiById(wikiId: Int): Wiki?

    suspend fun insertWiki(wiki: Wiki): Wiki

    suspend fun updateWiki(wiki: Wiki): Wiki

    suspend fun deleteWiki(wikiId: Int)

    suspend fun deleteWikisBySaga(sagaId: Int)
}
