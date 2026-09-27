package com.ilustris.sagai.core.ai.rag

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single fact, keyed to its source, alongside its embedding vector.
 *
 * This is the RAG index: one row per atomic, retrievable piece of saga knowledge — a wiki entry, a
 * character event, one item out of a [com.ilustris.sagai.features.narrative.data.model.ContinuitySummary]
 * list, a relationship summary. Never a whole chapter or act rolled into one blob — a vector
 * averaged over many unrelated facts loses the precision that made embedding worth doing in the
 * first place (see the RAG design discussion this table implements).
 *
 * [sourceKey] is a stable, human-readable id such as `"chapter:42:establishedFacts:0"` so a fact
 * can be found and replaced without needing its own auto-increment id lookup, and so re-indexing
 * the same source overwrites rather than accumulates duplicates.
 */
@Entity(
    tableName = "embedding_entries",
    primaryKeys = ["sagaId", "sourceKey"],
    indices = [Index(value = ["sagaId", "sourceType"])],
)
data class EmbeddingEntry(
    // No separate index needed: sagaId leads the composite primary key (usable as a prefix) and
    // the sagaId+sourceType index below, which is what every query here actually filters by.
    val sagaId: Int,
    /** Stable id for this exact fact, e.g. "wiki:12", "characterEvent:88", "chapter:5:openThreads:2". */
    val sourceKey: String,
    val sourceType: EmbeddingSourceType,
    /** The plain text that was embedded — what actually gets read back into the prompt. */
    val text: String,
    /** L2-normalized embedding vector, so retrieval is a plain dot product. */
    val vector: FloatArray,
    /** Embedding model id the vector was produced with — lets a model swap trigger a re-index. */
    val modelId: String,
    val updatedAt: Long = System.currentTimeMillis(),
) {
    override fun equals(other: Any?): Boolean =
        this === other ||
            (
                other is EmbeddingEntry &&
                    sagaId == other.sagaId &&
                    sourceKey == other.sourceKey &&
                    sourceType == other.sourceType &&
                    text == other.text &&
                    vector.contentEquals(other.vector) &&
                    modelId == other.modelId &&
                    updatedAt == other.updatedAt
            )

    override fun hashCode(): Int {
        var result = sagaId
        result = 31 * result + sourceKey.hashCode()
        result = 31 * result + sourceType.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + vector.contentHashCode()
        result = 31 * result + modelId.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}

/** What kind of saga knowledge a row indexes — mirrors the sections [ChatPrompts] builds today. */
enum class EmbeddingSourceType {
    WIKI,
    CHARACTER_EVENT,
    CHARACTER_RELATION,
    CONTINUITY_FACT,

    /** Safe fallback for a stored value that no longer matches a constant above — see
     * [com.ilustris.sagai.core.database.converters.EnumConverters]. Never written on purpose;
     * a row read back as this just won't match any type-filtered search. */
    UNKNOWN,
}
