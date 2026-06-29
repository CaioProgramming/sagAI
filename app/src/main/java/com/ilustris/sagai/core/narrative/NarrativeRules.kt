package com.ilustris.sagai.core.narrative

import com.google.gson.annotations.SerializedName

/**
 * NarrativeRules defines the logical limits for saga generation.
 * Managed via Remote Config under the 'narrative_rules' key.
 */
data class NarrativeRules(
    @SerializedName("MAX_ACTS_LIMIT")
    val maxActsLimit: Int = 3,
    @SerializedName("ACT_UPDATE_LIMIT")
    val actUpdateLimit: Int = 3,
    @SerializedName("LORE_UPDATE_LIMIT")
    val loreUpdateLimit: Int = 15,
    @SerializedName("CHAPTER_UPDATE_LIMIT")
    val chapterUpdateLimit: Int = 5,
    @SerializedName("CONTINUITY_RECENT_CHAPTERS")
    val continuityRecentChapters: Int = 3,
    @SerializedName("CONTINUITY_DISTANT_FACTS_LIMIT")
    val continuityDistantFactsLimit: Int = 20,
)
