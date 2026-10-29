package com.ilustris.sagai.core.ai.rag

import com.ilustris.sagai.features.narrative.data.model.ContinuitySummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes atomic facts into the RAG index as they're produced.
 *
 * Every call here is fire-and-forget — mirroring [com.ilustris.sagai.features.saga.chat.data.usecase.ChatGenerationService]'s
 * own singleton-scoped-coroutine pattern, indexing runs on its own [scope] rather than the
 * caller's, so it never slows down or fails the write path that produced the fact (saving a wiki
 * entry, closing a chapter, logging a character event). Failures are swallowed and logged — a
 * missed index entry just means that one fact keeps relying on the old recency-based context until
 * the next write re-indexes it.
 */
@Singleton
class SemanticIndexService
    @Inject
    constructor(
        private val embeddingClient: EmbeddingClient,
        private val embeddingDao: EmbeddingDao,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /** Indexes or re-indexes a single fact under [sourceKey], replacing any prior vector for it. */
        fun index(
            sagaId: Int,
            sourceKey: String,
            sourceType: EmbeddingSourceType,
            text: String,
        ) {
            if (text.isBlank()) return
            scope.launch {
                runCatching {
                    if (!embeddingClient.isAvailable()) return@runCatching
                    val vector = embeddingClient.embed(text) ?: return@runCatching
                    embeddingDao.upsert(
                        EmbeddingEntry(
                            sagaId = sagaId,
                            sourceKey = sourceKey,
                            sourceType = sourceType,
                            text = text,
                            vector = vector,
                            modelId = MODEL_TAG,
                        ),
                    )
                }.onFailure { Timber.tag(TAG).w(it, "Failed to index $sourceKey") }
            }
        }

        /**
         * Indexes a whole list of facts under one [sourceKeyPrefix] (e.g. one chapter's
         * `ContinuitySummary.establishedFacts`), replacing whatever was previously indexed under
         * that prefix — so a fact removed from the list on re-summarization also leaves the index.
         */
        fun indexList(
            sagaId: Int,
            sourceKeyPrefix: String,
            sourceType: EmbeddingSourceType,
            texts: List<String>,
        ) {
            scope.launch {
                runCatching {
                    embeddingDao.deleteBySourcePrefix(sagaId, sourceKeyPrefix)
                    if (texts.isEmpty() || !embeddingClient.isAvailable()) return@runCatching
                    val vectors = embeddingClient.embedBatch(texts)
                    val entries =
                        texts.mapIndexedNotNull { index, text ->
                            val vector = vectors.getOrNull(index) ?: return@mapIndexedNotNull null
                            if (text.isBlank()) return@mapIndexedNotNull null
                            EmbeddingEntry(
                                sagaId = sagaId,
                                sourceKey = "$sourceKeyPrefix:$index",
                                sourceType = sourceType,
                                text = text,
                                vector = vector,
                                modelId = MODEL_TAG,
                            )
                        }
                    if (entries.isNotEmpty()) embeddingDao.upsertAll(entries)
                }.onFailure { Timber.tag(TAG).w(it, "Failed to index list $sourceKeyPrefix") }
            }
        }

        /**
         * Indexes every item of [summary] — never the summary rolled into one blob (see
         * [EmbeddingEntry]'s doc). One [indexList] call per field, so re-summarizing drops facts
         * that fell out of a specific list without touching the others.
         */
        fun indexContinuitySummary(
            sagaId: Int,
            sourceKeyPrefix: String,
            summary: ContinuitySummary,
        ) {
            indexList(sagaId, "$sourceKeyPrefix:establishedFacts", EmbeddingSourceType.CONTINUITY_FACT, summary.establishedFacts)
            indexList(sagaId, "$sourceKeyPrefix:openThreads", EmbeddingSourceType.CONTINUITY_FACT, summary.openThreads)
            indexList(sagaId, "$sourceKeyPrefix:consequences", EmbeddingSourceType.CONTINUITY_FACT, summary.consequences)
            indexList(sagaId, "$sourceKeyPrefix:characterStates", EmbeddingSourceType.CONTINUITY_FACT, summary.characterStates)
            indexList(sagaId, "$sourceKeyPrefix:persistentSetups", EmbeddingSourceType.CONTINUITY_FACT, summary.persistentSetups)
        }

        fun removeSource(
            sagaId: Int,
            sourceKeyPrefix: String,
        ) {
            scope.launch {
                runCatching { embeddingDao.deleteBySourcePrefix(sagaId, sourceKeyPrefix) }
                    .onFailure { Timber.tag(TAG).w(it, "Failed to remove $sourceKeyPrefix") }
            }
        }

        companion object {
            private const val TAG = "🧭 SemanticIndex"

            /**
             * Tag stored alongside each vector, not the literal model id — re-indexing keys off
             * whether the *scheme* changed, not the exact remote-config string, since the
             * embedding model itself may rotate behind the same flag without invalidating vectors
             * of the same dimensionality/version.
             */
            private const val MODEL_TAG = "text-embedding-004"
        }
    }
