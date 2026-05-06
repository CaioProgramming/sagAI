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
data class CharacterDetailKey(
    val sagaId: String,
    val characterId: Int,
) : NavKey

@Serializable
data class SagaChaptersKey(
    val sagaId: String,
) : NavKey

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
                CharacterDetailKey(parts[0], parts[1].toIntOrNull() ?: 0)
            } else {
                null
            }
        }

        this.startsWith("saga://saga_chapters/") -> {
            SagaChaptersKey(this.removePrefix("saga://saga_chapters/"))
        }

        else -> {
            null
        }
    }
}
