package com.ilustris.sagai.features.newsaga.data.model

import com.ilustris.sagai.core.utils.removeBlankSpace

enum class CharacterFormFields(
    val description: String,
) {
    NAME("Character Name (NAME) - missing if empty or less than 2 characters"),
    BACKSTORY(
        "Character Backstory (BACKSTORY) - missing if empty or less than 15 words",
    ),
    APPEARANCE(
        "Character Appearance (APPEARANCE) - missing if empty or less than 10 words",
    ),
    ALL_FIELDS_COMPLETE(
        "ALL FIELDS ARE COMPLETE",
    ),
    ;

    companion object {
        fun getByKey(key: String) =
            CharacterFormFields.entries.find {
                it.name == key.removeBlankSpace()
            }

        fun fieldPriority() =
            """
            Priority of information needed:
            ${
                CharacterFormFields.entries.joinToString("\n") {
                    "${it.ordinal + 1}. ${it.description}"
                }
            }
            Based on the Character Data and the priorities, return the token for the FIRST piece of information that is missing or insufficient.
            If all are sufficiently filled, return ALL_FIELDS_COMPLETE.
            YOUR SOLE OUTPUT MUST BE ONE OF THESE TOKENS AS A SINGLE STRING.
            """.trimIndent()
    }
}
