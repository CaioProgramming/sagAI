package com.ilustris.sagai.features.characters.data.model

enum class CharacterExpression(
    val description: String,
) {
    NEUTRAL("A neutral, unreadable expression"),
    SMILING_GENTLY("Smiling gently, a hint of warmth"),
    LAUGHING_HEARTILY("Laughing heartily, full of joy"),
    FROWNING_SLIGHTLY("Frowning slightly, a touch of concern or disapproval"),
    SCOWLING_DEEPLY("Scowling deeply, radiating anger or intense displeasure"),
    SAD_TEARFUL("Looking sad, possibly with tears welling up"),
    DETERMINED_GRIT("A determined look, with gritted teeth or a firm jaw"),
    CONFIDENT_SMIRK("A confident smirk, a hint of arrogance or self-assurance"),
    CURIOUS_HEAD_TILT("A curious expression, perhaps with a slight head tilt"),
    EXCITED_EAGER("Excited and eager, eyes sparkling with anticipation"),
    ;

    companion object {
        fun random(): CharacterExpression = entries.random()
    }
}
