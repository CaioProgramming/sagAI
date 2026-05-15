package com.ilustris.sagai.features.saga.chat.data.model

import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.utils.toAINormalize

fun NewCharacterDiscovery.toGenerationDescription(
    userMessage: Message,
    replyMessage: Message,
): String =
    buildString {
        append(toAINormalize())
        appendLine()
        append(userMessage.toAINormalize(ChatPrompts.messageExclusions))
        appendLine()
        append(replyMessage.toAINormalize(ChatPrompts.messageExclusions))
    }
