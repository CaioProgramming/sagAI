package com.ilustris.sagai.features.saga.chat.data.model

import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.saga.chat.ui.components.ExpressiveTag

enum class SenderType(
    val tag: ExpressiveTag? = null,
) {
    USER,
    CHARACTER,
    THOUGHT(ExpressiveTag.THINK),
    ACTION(ExpressiveTag.ACTION),
    NARRATOR(ExpressiveTag.NARRATOR),
    ;

    companion object {
        fun filterUserInputTypes() =
            SenderType.entries.filter {
                it.tag != null
            }

        fun senderForTag(tag: ExpressiveTag) =
            SenderType.entries.firstOrNull {
                it.tag == tag
            }
    }
}

fun SenderType.isCharacter() = this == SenderType.CHARACTER

fun SenderType.rules() =
    when (this) {
        SenderType.ACTION -> {
            emptyString()
        }

        SenderType.NARRATOR -> {
            """
        **CRITICAL RULE: The NARRATOR MUST NEVER include direct or indirect dialogue from any character (NPCs or player).
            Narration should describe actions, environments, and non-verbal reactions only.
            All character speech must be in a '''CHARACTER''' senderType.**
        """
        }

        SenderType.THOUGHT -> {
            """
            Use when the player's character is thinking and not talking directly.
            OR Use RARELY for an impactful **NPC** internal thought. If used for an NPC, the `speakerName` field will identify the NPC.
            The AI should not generate this type for the player.
            """
        }

        SenderType.USER,
        -> {
            """
        You, as the AI, must NEVER generate a message with this '''Type'''.
        NEVER USE THIS TYPE, IT WILL BE SEND EXCLUSIVELY LOCALLY ONLYON THE APP.    
        """
        }

        SenderType.CHARACTER -> {
            """
            Use for characters(NPCS) who is already
            established in the story or listed in '''CURRENT SAGA CAST'''.' 
            """
        }
    }
