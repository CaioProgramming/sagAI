package com.ilustris.sagai.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : NavKey

@Serializable
data object ProfileKey : NavKey

@Serializable
data object FAQKey : NavKey

@Serializable
data object NewSagaKey : NavKey

@Serializable
data object AuditLogsKey : NavKey

@Serializable
data object PlaythroughKey : NavKey

@Serializable
data class ChatKey(
    val sagaId: String,
    val isDebug: Boolean = false,
) : NavKey

@Serializable
data class SagaDetailKey(
    val sagaId: String,
) : NavKey

@Serializable
data class SagaCharactersKey(
    val sagaId: String,
) : NavKey

@Serializable
data class SagaWikiKey(
    val sagaId: String,
) : NavKey

@Serializable
data class SagaEventsKey(
    val sagaId: String,
) : NavKey

@Serializable
data class SagaActsKey(
    val sagaId: String,
) : NavKey

@Serializable
data class SagaStoryReaderKey(
    val sagaId: String,
) : NavKey

@Serializable
data class CharacterDetailKey(
    val characterId: Int,
) : NavKey

@Serializable
data class SagaChaptersKey(
    val sagaId: String,
) : NavKey

@Serializable
data class LoreDebugKey(
    val sagaId: String,
) : NavKey

@Serializable
data class BookReaderKey(
    val sagaId: Int,
    val initialActId: Int,
) : NavKey

/**
 * Whether [other] represents the same screen the user is already on (ignores debug flags, etc.).
 */
fun NavKey.isSameDestinationAs(other: NavKey?): Boolean {
    if (other == null) return false
    if (this == other) return true
    return when {
        this is ChatKey && other is ChatKey -> sagaId == other.sagaId
        this is SagaDetailKey && other is SagaDetailKey -> sagaId == other.sagaId
        this is SagaCharactersKey && other is SagaCharactersKey -> sagaId == other.sagaId
        this is SagaWikiKey && other is SagaWikiKey -> sagaId == other.sagaId
        this is SagaEventsKey && other is SagaEventsKey -> sagaId == other.sagaId
        this is SagaActsKey && other is SagaActsKey -> sagaId == other.sagaId
        this is SagaStoryReaderKey && other is SagaStoryReaderKey -> sagaId == other.sagaId
        this is SagaChaptersKey && other is SagaChaptersKey -> sagaId == other.sagaId
        this is CharacterDetailKey && other is CharacterDetailKey -> characterId == other.characterId
        this is LoreDebugKey && other is LoreDebugKey -> sagaId == other.sagaId
        this is BookReaderKey && other is BookReaderKey ->
            sagaId == other.sagaId && initialActId == other.initialActId
        else -> false
    }
}

fun String.findNavKey(): NavKey? {
    val sanitized = this.substringBeforeLast("/")
    return when {
        this.equals("HOME", ignoreCase = true) || sanitized == "saga://home" -> {
            HomeKey
        }

        this.equals("PROFILE", ignoreCase = true) || sanitized == "saga://profile" -> {
            ProfileKey
        }

        this.equals("FAQ", ignoreCase = true) || sanitized == "saga://faq" -> {
            FAQKey
        }

        this.equals("NEW_SAGA", ignoreCase = true) || sanitized == "saga://new_saga" -> {
            NewSagaKey
        }

        this.equals(
            "AUDIT_LOGS",
            ignoreCase = true,
        ) || sanitized == "saga://audit_logs" -> {
            AuditLogsKey
        }

        this.equals(
            "PLAYTHROUGH",
            ignoreCase = true,
        ) || sanitized == "saga://playthrough" -> {
            PlaythroughKey
        }

        this.startsWith("saga://chat/") -> {
            val parts = this.removePrefix("saga://chat/").split("/")
            if (parts.size >= 2) {
                ChatKey(parts[0], parts[1].toBooleanStrictOrNull() ?: false)
            } else if (parts.isNotEmpty()) {
                ChatKey(parts[0])
            } else {
                null
            }
        }

        this.startsWith("saga://saga_detail/") -> {
            SagaDetailKey(this.removePrefix("saga://saga_detail/"))
        }

        this.startsWith("saga://character_detail/") -> {
            val parts = this.removePrefix("saga://character_detail/").split("/")
            if (parts.size >= 2) {
                CharacterDetailKey(parts[1].toIntOrNull() ?: 0)
            } else if (parts.isNotEmpty()) {
                CharacterDetailKey(parts[0].toIntOrNull() ?: 0)
            } else {
                null
            }
        }

        this.startsWith("saga://saga_chapters/") -> {
            SagaChaptersKey(this.removePrefix("saga://saga_chapters/"))
        }

        this.startsWith("saga://lore_debug/") -> {
            LoreDebugKey(this.removePrefix("saga://lore_debug/"))
        }

        this.startsWith("saga://book_reader/") -> {
            val parts = this.removePrefix("saga://book_reader/").split("/")
            if (parts.size >= 2) {
                BookReaderKey(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
            } else {
                null
            }
        }

        this.startsWith("saga://story_reader/") -> {
            SagaStoryReaderKey(this.removePrefix("saga://story_reader/"))
        }

        else -> {
            null
        }
    }
}
