package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.faq.data.model.FAQContent

data class FAQArgs(
    val query: String,
    val faqContext: String,
)

object FAQPrompts {
    const val FAQ_ASK_AI_BLUEPRINT = "faq_ask_ai_blueprint"

    suspend fun getAskAiPrompt(
        promptService: PromptService,
        query: String,
        context: FAQContent,
    ): String {
        val args =
            FAQArgs(
                query = query,
                faqContext = context.toAINormalize(),
            )
        return promptService.buildRemotePrompt(FAQ_ASK_AI_BLUEPRINT, args)
    }
}
