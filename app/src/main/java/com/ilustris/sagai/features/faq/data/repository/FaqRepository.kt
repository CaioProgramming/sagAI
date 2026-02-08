package com.ilustris.sagai.features.faq.data.repository

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.faq.data.model.FAQContent

interface FaqRepository {
    suspend fun getFaqs(): RequestResult<FAQContent>

    suspend fun askAi(
        query: String,
        context: FAQContent,
    ): RequestResult<String>
}
