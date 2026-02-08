package com.ilustris.sagai.features.faq.ui

import com.ilustris.sagai.features.faq.data.model.FAQContent

sealed interface FAQState {
    data object Loading : FAQState

    data class FaqsRetrieved(
        val faqs: FAQContent,
    ) : FAQState

    data class FaqsError(
        val message: String?,
    ) : FAQState

    data object AiLoading : FAQState

    data class AiReply(
        val reply: String,
    ) : FAQState
}
