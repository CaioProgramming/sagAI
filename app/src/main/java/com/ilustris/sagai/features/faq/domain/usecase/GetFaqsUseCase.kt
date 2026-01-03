package com.ilustris.sagai.features.faq.domain.usecase

import com.ilustris.sagai.features.faq.data.repository.FaqRepository
import javax.inject.Inject

class GetFaqsUseCase
    @Inject
    constructor(
        private val faqRepository: FaqRepository,
    ) {
        suspend operator fun invoke() = faqRepository.getFaqs()
    }
