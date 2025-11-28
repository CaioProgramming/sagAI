package com.ilustris.sagai.features.wiki.data.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.source.WikiDao
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class WikiRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : WikiRepository {
        private val wikiDao by lazy {
            database.wikiDao()
        }

        override fun getWikisBySaga(sagaId: Int): Flow<List<Wiki>> = wikiDao.getWikisBySaga(sagaId)

        override suspend fun getWikiById(wikiId: Int): Wiki? = wikiDao.getWikiById(wikiId)

        override suspend fun insertWiki(wiki: Wiki): Wiki =
            wiki.copy(
                id = wikiDao.insertWiki(wiki.copy(id = 0)).toInt(),
                createdAt = Calendar.getInstance().timeInMillis,
            )

        override suspend fun updateWiki(wiki: Wiki): Wiki {
            wikiDao.updateWiki(wiki)
            return wiki
        }

        override suspend fun deleteWiki(wikiId: Int) {
            wikiDao.deleteWiki(wikiId)
        }

        override suspend fun deleteWikisBySaga(sagaId: Int) {
            wikiDao.deleteWikisBySaga(sagaId)
        }
    }
