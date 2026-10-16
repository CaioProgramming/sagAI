package com.ilustris.sagai.features.progression.domain

import com.google.gson.annotations.SerializedName

/**
 * Numbers of the character progression system, managed via Remote Config under the
 * 'character_progression' key. Remote-only: there are no compiled fallbacks, so the feature stays
 * off until the config is published. Every value is tuned without a release.
 */
data class ProgressionConfig(
    @SerializedName("ENABLED")
    val enabled: Boolean = false,
    @SerializedName("LEVEL_CAP")
    val levelCap: Int = 100,
    @SerializedName("LEVEL_XP")
    val levelXp: LevelCurve = LevelCurve(),
    /** XP granted by each impact tier, indexed by tier (0 to 5). */
    @SerializedName("TIER_XP")
    val tierXp: List<Int> = emptyList(),
    /** Share of the full XP kept for the 1st, 2nd, 3rd... feat of the same kind in a chapter. */
    @SerializedName("REPEAT_DECAY")
    val repeatDecay: List<Double> = listOf(1.0),
    /** Most XP one event may give a character, as a share of the span of the level they are on. */
    @SerializedName("EVENT_XP_CAP_PCT")
    val eventXpCapPct: Double = 1.0,
    /** Most XP one chapter may give a character, as a share of the span of the level they are on. */
    @SerializedName("CHAPTER_XP_CAP_PCT")
    val chapterXpCapPct: Double = 1.0,
    @SerializedName("PLAYER_START_LEVEL_CAP")
    val playerStartLevelCap: Int = 25,
    @SerializedName("MAX_SKILLS")
    val maxSkills: Int = 12,
    @SerializedName("MEDAL_MIN_TIER")
    val medalMinTier: Int = 3,
    @SerializedName("LEGACY_MIN_TIER")
    val legacyMinTier: Int = 4,
    @SerializedName("MAX_MEDALS_PER_CHARACTER_PER_CHAPTER")
    val maxMedalsPerCharacterPerChapter: Int = 3,
    @SerializedName("MAX_MEDALS_PER_CHAPTER")
    val maxMedalsPerChapter: Int = 5,
    @SerializedName("MAX_TITLES_PER_ACT")
    val maxTitlesPerAct: Int = 2,
    @SerializedName("LEVEL_UP_ROWS_MAX")
    val levelUpRowsMax: Int = 4,
    @SerializedName("LEVEL_BANDS")
    val levelBands: List<LevelBand> = emptyList(),
    @SerializedName("POWER_GAP_BANDS")
    val powerGapBands: List<PowerGapBand> = emptyList(),
)

/** Cumulative XP to reach a level N is `BASE * (N - 1) ^ EXPONENT`. */
data class LevelCurve(
    @SerializedName("BASE")
    val base: Double = 4.3,
    @SerializedName("EXPONENT")
    val exponent: Double = 1.6,
)

data class LevelBand(
    @SerializedName("FROM")
    val from: Int,
    @SerializedName("TO")
    val to: Int,
    @SerializedName("LABEL")
    val label: String,
)

data class PowerGapBand(
    @SerializedName("MAX_GAP")
    val maxGap: Int,
    @SerializedName("SENTENCE")
    val sentence: String,
)
