package com.ilustris.sagai.features.wiki.data.usecase

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.WikiPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.MergeWikiGen
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.model.WikiGen
import com.ilustris.sagai.features.wiki.data.repository.WikiRepository
import javax.inject.Inject

class WikiUseCaseImpl
    @Inject
    constructor(
        private val wikiRepository: WikiRepository,
        private val gemmaClient: GemmaClient,
    ) : WikiUseCase {
        override suspend fun saveWiki(wiki: Wiki) = wikiRepository.insertWiki(wiki)

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
                .generate<WikiGen>(
                    prompt =
                        WikiPrompts.generateWiki(
                            saga = sagaContent,
                            event = event,
                        ),
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                )!!
                .wikis
        }

        override suspend fun mergeWikis(
            saga: SagaContent,
            wikiContents: List<Wiki>,
        ): RequestResult<Unit> =
            executeRequest {
                val prompt =
                    WikiPrompts.mergeWiki(
                        wikiContents,
                    )

                val mergedWikis =
                    gemmaClient
                        .generate<MergeWikiGen>(
                            prompt = prompt,
                            requirement = GemmaClient.ModelRequirement.MEDIUM,
                        )!!
                        .mergedItems

                val validItems =
                    mergedWikis.filter { mergedItem ->
                        // Skip items with no merge candidate (secondItem is null or empty)
                        if (mergedItem.secondItem.isNullOrBlank()) {
                            Log.d(
                                javaClass.simpleName,
                                "Skipping item with no merge candidate: ${mergedItem.firstItem}",
                            )
                            return@filter false
                        }

                        val firstWiki =
                            saga.wikis.find { it.title.contentEquals(mergedItem.firstItem, true) }
                        val secondWiki =
                            saga.wikis.findLast {
                                it.title.contentEquals(
                                    mergedItem.secondItem,
                                    true,
                                )
                            }
                        firstWiki != null && secondWiki != null
                    }

                validItems.forEach { mergeWiki ->
                    val firstWiki =
                        saga.wikis.find { it.title.contentEquals(mergeWiki.firstItem, true) }
                    val secondWiki =
                        saga.wikis.find { it.title.contentEquals(mergeWiki.secondItem, true) }

                    firstWiki?.let { wiki ->
                        wikiRepository.updateWiki(
                            firstWiki.copy(
                                title = mergeWiki.mergedItem.title,
                                content = mergeWiki.mergedItem.content,
                                type = mergeWiki.mergedItem.type,
                                emojiTag = mergeWiki.mergedItem.emojiTag,
                            ),
                        )
                        secondWiki?.let {
                            wikiRepository.deleteWiki(secondWiki.id)
                        }
                    }
                }

                Log.d(javaClass.simpleName, "mergeWikis: Updated ${validItems.size} items")
            }
    }
