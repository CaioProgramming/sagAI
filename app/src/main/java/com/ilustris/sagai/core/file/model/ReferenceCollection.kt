package com.ilustris.sagai.core.file.model

import kotlinx.serialization.Serializable

/**
 * Represents a collection of reference image URLs from Firebase Remote Config.
 *
 * Expected JSON structure:
 * ```json
 * {
 *   "references": [
 *     "https://example.com/image1.jpg",
 *     "https://example.com/image2.jpg",
 *     "https://example.com/image3.jpg"
 *   ]
 * }
 * ```
 *
 * This structure is extensible for future enhancements like:
 * - Weighted selection: Add a `weights` field
 * - Metadata: Add tags, categories, etc.
 * - A/B testing: Add performance tracking fields
 */
@Serializable
data class ReferenceCollection(
    val references: List<String>,
) {
    /**
     * Returns a randomly selected reference URL from the collection.
     * @throws IllegalStateException if the references list is empty
     */
    fun getRandomReference(): String {
        require(references.isNotEmpty()) { "Reference collection is empty" }
        return references.random()
    }

    /**
     * Returns the number of available references.
     */
    val size: Int get() = references.size
}
