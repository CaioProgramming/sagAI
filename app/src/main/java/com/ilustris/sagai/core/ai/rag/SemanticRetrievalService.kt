package com.ilustris.sagai.core.ai.rag

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class SemanticMatch(
    val sourceKey: String,
    val sourceType: EmbeddingSourceType,
    val text: String,
    val score: Float,
)

/** One named slice of a [SemanticRetrievalService.searchGroups] call: its own type filter and top-k. */
data class RetrievalGroup(
    val types: List<EmbeddingSourceType>,
    val topK: Int,
)

/**
 * Ranks indexed facts against a query by cosine similarity — the search half of the saga's RAG
 * index. Everything here runs in memory: a saga's fact count stays in the hundreds, so scoring the
 * whole candidate pool with a dot product is milliseconds of work, and doesn't need a vector
 * database.
 */
@Singleton
class SemanticRetrievalService
    @Inject
    constructor(
        private val embeddingClient: EmbeddingClient,
        private val embeddingDao: EmbeddingDao,
    ) {
        /**
         * @param minScore facts below this cosine similarity are dropped rather than padding the
         *   top-k with weak, likely-irrelevant matches when the index has few candidates.
         * @return empty when RAG is unavailable (no key, flag off, embedding call failed, or
         *   nothing indexed yet) — callers fall back to their existing recency-based selection.
         */
        suspend fun search(
            sagaId: Int,
            query: String,
            types: List<EmbeddingSourceType>,
            topK: Int,
            minScore: Float = 0.5f,
        ): List<SemanticMatch> =
            searchGroups(sagaId, query, listOf(RetrievalGroup(types, topK)), minScore).firstOrNull().orEmpty()

        /**
         * Runs several [RetrievalGroup]s against one embedding of [query] — one embed call and one
         * DB fetch (the union of every group's types) shared across all of them, instead of each
         * group paying for its own. [ChatPrompts.replyMessagePrompt] needs two groups
         * (`mentionedWikis`, `relevantMemories`) for the *same* player message every chat turn;
         * calling [search] twice would embed that identical text twice for no reason — the
         * embedding call, not indexing, is the retrieval path's actual per-message cost.
         *
         * @return one list per input group, same order, each already filtered/ranked/capped as
         *   that group asked.
         */
        suspend fun searchGroups(
            sagaId: Int,
            query: String,
            groups: List<RetrievalGroup>,
            minScore: Float = 0.5f,
        ): List<List<SemanticMatch>> {
            val empty = groups.map { emptyList<SemanticMatch>() }
            if (query.isBlank() || groups.isEmpty()) return empty

            return runCatching {
                if (!embeddingClient.isAvailable()) return@runCatching empty
                val allTypes = groups.flatMap { it.types }.distinct()
                val candidates = embeddingDao.findBySagaAndTypes(sagaId, allTypes)
                if (candidates.isEmpty()) return@runCatching empty
                val queryVector = embeddingClient.embed(query) ?: return@runCatching empty

                val scored =
                    candidates.map { candidate ->
                        SemanticMatch(
                            sourceKey = candidate.sourceKey,
                            sourceType = candidate.sourceType,
                            text = candidate.text,
                            score = cosineSimilarity(queryVector, candidate.vector),
                        )
                    }

                groups.map { group ->
                    scored
                        .filter { it.sourceType in group.types && it.score >= minScore }
                        .sortedByDescending { it.score }
                        .take(group.topK)
                }
            }.onFailure { Timber.tag(TAG).w(it, "Semantic search failed for saga $sagaId") }
                .getOrDefault(empty)
        }

        /** Both vectors are already L2-normalized on write, so this is a plain dot product. */
        private fun cosineSimilarity(
            a: FloatArray,
            b: FloatArray,
        ): Float {
            if (a.size != b.size) return 0f
            var dot = 0f
            for (i in a.indices) dot += a[i] * b[i]
            return dot
        }

        companion object {
            private const val TAG = "🧭 SemanticRetrieval"
        }
    }
