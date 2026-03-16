package com.ilustris.sagai.core.narrative

data class NarrativeRules(
    val rules: Map<String, Any> = emptyMap(),
) {
    fun getString(key: String): String = rules[key]?.toString() ?: ""

    fun getInt(
        key: String,
        default: Int,
    ): Int = (rules[key] as? Number)?.toInt() ?: default

    val maxActsLimit get() = getInt("MAX_ACTS_LIMIT", 3)
    val actUpdateLimit get() = getInt("ACT_UPDATE_LIMIT", 5)
    val loreUpdateLimit get() = getInt("LORE_UPDATE_LIMIT", 10)
    val chapterUpdateLimit get() = getInt("CHAPTER_UPDATE_LIMIT", 3)

    val firstActPurpose get() = getString("FIRST_ACT_PURPOSE")
    val secondActPurpose get() = getString("SECOND_ACT_PURPOSE")
    val thirdActPurpose get() = getString("THIRD_ACT_PURPOSE")

    val firstActDirectives get() = getString("FIRST_ACT_DIRECTIVES")
    val secondActDirectives get() = getString("SECOND_ACT_DIRECTIVES")
    val thirdActDirectives get() = getString("THIRD_ACT_DIRECTIVES")

    val act1IntroWithContext get() = getString("ACT_1_INTRO_WITH_CONTEXT")
    val act1IntroWithoutContext get() = getString("ACT_1_INTRO_WITHOUT_CONTEXT")
    val transitionalActIntroWithContext get() = getString("TRANSITIONAL_ACT_INTRO_WITH_CONTEXT")
    val transitionalActIntroWithoutContext get() = getString("TRANSITIONAL_ACT_INTRO_WITHOUT_CONTEXT")

    val firstChapterIntroInstruction get() = getString("FIRST_CHAPTER_INTRO_INSTRUCTION")
    val sequentialChapterIntroInstruction get() = getString("SEQUENTIAL_CHAPTER_INTRO_INSTRUCTION")

    val firstChapterContextTemplate get() = getString("FIRST_CHAPTER_CONTEXT_TEMPLATE")
    val storyProgressionContextTemplate get() = getString("STORY_PROGRESSION_CONTEXT_TEMPLATE")
}
