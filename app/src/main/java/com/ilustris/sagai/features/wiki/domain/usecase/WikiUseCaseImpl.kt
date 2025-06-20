package com.ilustris.sagai.features.wiki.domain.usecase

import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.repository.WikiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WikiUseCaseImpl
    @Inject
    constructor(
        private val wikiRepository: WikiRepository,
    ) : WikiUseCase {
        override fun getWikisBySaga(sagaId: Int): Flow<List<Wiki>> = wikiRepository.getWikisBySaga(sagaId)

        override suspend fun getWikiById(wikiId: Int): Wiki? = wikiRepository.getWikiById(wikiId)

        override suspend fun saveWiki(wiki: Wiki): Long = wikiRepository.insertWiki(wiki)

        override suspend fun updateWiki(wiki: Wiki) = wikiRepository.updateWiki(wiki)

        override suspend fun deleteWiki(wikiId: Int) {
            wikiRepository.deleteWiki(wikiId)
        }

        override suspend fun deleteWikisBySaga(sagaId: Int) {
            wikiRepository.deleteWikisBySaga(sagaId)
        }
    }
