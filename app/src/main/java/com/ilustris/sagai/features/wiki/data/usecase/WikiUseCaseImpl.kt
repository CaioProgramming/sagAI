package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient // Changed
import com.ilustris.sagai.core.ai.prompts.WikiPrompts
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.repository.WikiRepository
import javax.inject.Inject

class WikiUseCaseImpl
    @Inject
    constructor(
        private val wikiRepository: WikiRepository,
        private val gemmaClient: GemmaClient,
    ) : WikiUseCase {
        override suspend fun saveWiki(wiki: Wiki): Long = wikiRepository.insertWiki(wiki)

        override suspend fun updateWiki(wiki: Wiki) = wikiRepository.updateWiki(wiki)

        override suspend fun deleteWiki(wikiId: Int) {
            wikiRepository.deleteWiki(wikiId)
        }

        override suspend fun deleteWikisBySaga(sagaId: Int) {
            wikiRepository.deleteWikisBySaga(sagaId)
        }

        override suspend fun generateWiki(
            sagaContent: SagaContent,
            event: Timeline,
        ) = executeRequest {
            gemmaClient
                .generate<List<Wiki>>(
                    prompt =
                        WikiPrompts.generateWiki(
                            saga = sagaContent,
                            event = event,
                        ),
                )!!
        }
    }
