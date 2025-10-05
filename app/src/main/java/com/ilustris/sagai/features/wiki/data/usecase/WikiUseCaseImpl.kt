package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient // Changed
import com.ilustris.sagai.core.ai.prompts.WikiPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.MergeWikiGen
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

        override suspend fun mergeWikis(
            saga: SagaContent,
            currentChapterContent: ChapterContent,
        ): RequestResult<Unit> =
            executeRequest {
                val prompt =
                    WikiPrompts.mergeWiki(
                        currentChapterContent,
                    )

                val mergedWikis = gemmaClient.generate<MergeWikiGen>(prompt = prompt)!!

                mergedWikis.mergedItems.forEach { mergeWiki ->
                    val firstWiki = saga.wikis.find { it.title.contentEquals(mergeWiki.firstItem, true) }
                    val secondWiki = saga.wikis.find { it.title.contentEquals(mergeWiki.secondItem, true) }
                    if (firstWiki != null && secondWiki != null) {
                        wikiRepository.updateWiki(
                            firstWiki.copy(
                                title = mergeWiki.mergedItem.title,
                                content = mergeWiki.mergedItem.content,
                                type = mergeWiki.mergedItem.type,
                                emojiTag = mergeWiki.mergedItem.emojiTag,
                            ),
                        )
                        wikiRepository.deleteWiki(secondWiki.id)
                    }
                }
            }
    }
