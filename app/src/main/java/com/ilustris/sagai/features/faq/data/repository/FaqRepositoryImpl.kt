package com.ilustris.sagai.features.faq.data.repository

import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.FAQPrompts
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.faq.data.model.FAQContent
import javax.inject.Inject

private const val FAQ_DATA_KEY = "faq_data"

class FaqRepositoryImpl
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
        private val textGenClient: TextGenClient,
    ) : FaqRepository {
        override suspend fun getFaqs() =
            executeRequest {
                remoteConfigService.getJson<FAQContent>(FAQ_DATA_KEY)!!
            }

        override suspend fun askAi(
            query: String,
            context: FAQContent,
        ) = executeRequest {
            val prompt = FAQPrompts.getAskAiPrompt(query, context)
            textGenClient.generate<String>(prompt, requireTranslation = false)
                ?: "Oops! My crystal ball is a bit foggy. Can you try again?"
    }
    }
