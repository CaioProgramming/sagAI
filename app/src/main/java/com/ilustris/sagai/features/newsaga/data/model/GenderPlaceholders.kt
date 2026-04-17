package com.ilustris.sagai.features.newsaga.data.model

import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.Gender

data class GenderPlaceholders(
    val male: String = "",
    val female: String = "",
)

typealias GenderPlaceholderMap = Map<String, GenderPlaceholders>

fun GenderPlaceholderMap.resolveUrl(
    genre: Genre,
    gender: Gender,
): String {
    val placeholders = this[genre.name] ?: this["DEFAULT"] ?: return ""
    return when (gender) {
        Gender.MALE -> placeholders.male
        Gender.FEMALE -> placeholders.female
        Gender.Other -> emptyString()
    }
}
