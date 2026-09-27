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
        ): List<SemanticMatch> {
            if (query.isBlank() || topK <= 0) return emptyList()
            return runCatching {
                if (!embeddingClient.isAvailable()) return@runCatching emptyList()
                val candidates = embeddingDao.findBySagaAndTypes(sagaId, types)
                if (candidates.isEmpty()) return@runCatching emptyList()
                val queryVector = embeddingClient.embed(query) ?: return@runCatching emptyList()

                candidates
                    .map { candidate ->
                        SemanticMatch(
                            sourceKey = candidate.sourceKey,
                            sourceType = candidate.sourceType,
                            text = candidate.text,
                            score = cosineSimilarity(queryVector, candidate.vector),
                        )
                    }.filter { it.score >= minScore }
                    .sortedByDescending { it.score }
                    .take(topK)
            }.onFailure { Timber.tag(TAG).w(it, "Semantic search failed for saga $sagaId") }
                .getOrDefault(emptyList())
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
