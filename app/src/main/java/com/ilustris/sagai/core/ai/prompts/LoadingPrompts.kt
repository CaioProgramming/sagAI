package com.ilustris.sagai.core.ai.prompts

object LoadingPrompts {
    data class LoadingArgs(
        val task: String,
        val conversationStyle: String?,
    )
}
