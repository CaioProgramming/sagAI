package com.ilustris.sagai.features.progression.domain

import com.ilustris.sagai.features.characters.data.model.Character

/**
 * Brings a generated character in line with the progression rules. Off (null [config]): the
 * placement fields are cleared so a saga without progression never carries them. On: the level is
 * kept inside 1..cap (the protagonist also under the start cap, so they always have room to grow)
 * and the skills are tidied and capped.
 */
fun Character.withPlacement(
    config: ProgressionConfig?,
    isProtagonist: Boolean,
): Character {
    if (config == null) return copy(baseLevel = null, levelRationale = null, skills = emptyList())
    val ceiling = if (isProtagonist) minOf(config.levelCap, config.playerStartLevelCap) else config.levelCap
    return copy(
        baseLevel = baseLevel?.coerceIn(1, ceiling),
        levelRationale = levelRationale?.trim()?.takeIf { it.isNotEmpty() },
        skills = skills.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }.take(config.maxSkills),
    )
}
