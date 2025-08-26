package com.ilustris.sagai.features.newsaga.data.model

import com.ilustris.sagai.core.utils.removeBlankSpace

enum class SagaFormFields(
    val description: String,
) {
    TITLE("Saga Title (TITLE) - missing if empty or less than 3 characters"),
    DESCRIPTION(
        "Saga Description (DESCRIPTION) - missing if empty or less than 10 words",
    ),
    GENRE(
        "Saga Genre (GENRE) - missing if empty",
    ),
    CHARACTER_NAME(
        "Character Name (CHARACTER_NAME) - missing if empty",
    ),
    CHARACTER_BACKSTORY(
        "Character Backstory (CHARACTER_BACKSTORY) - missing if empty or less than 15 words",
    ),
    CHARACTER_OCCUPATION(
        "Character Occupation (CHARACTER_OCCUPATION) - missing if empty",
    ),
    CHARACTER_APPEARANCE(
        "Character Appearance (CHARACTER_APPEARANCE) - missing if empty or less than 10 words",
    ),
    ALL_FIELDS_COMPLETE(
        "ALL FIELDS ARE COMPLETE",
    ),
    ;

    companion object {
        fun getByKey(key: String) =
            SagaFormFields.entries.find {
                it.name == key.removeBlankSpace()
            }

        fun fieldPriority() =
            """
            Priority of information needed:
            ${
                SagaFormFields.entries.joinToString("\n") {
                    "${it.ordinal + 1}. ${it.description}"
                }}
            Based on the Current Saga Data and the priorities, return the token for the FIRST piece of information that is missing or insufficient.
            If all are sufficiently filled, return ALL_FIELDS_COMPLETE.
            YOUR SOLE OUTPUT MUST BE ONE OF THESE TOKENS AS A SINGLE STRING.
            """.trimIndent()
    }
}
