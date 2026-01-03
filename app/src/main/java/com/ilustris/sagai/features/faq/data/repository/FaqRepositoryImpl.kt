package com.ilustris.sagai.features.faq.data.repository

import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.faq.data.model.FAQContent
import javax.inject.Inject

private const val FAQ_DATA_KEY = "faq_data"

class FaqRepositoryImpl
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
    ) : FaqRepository {
        override suspend fun getFaqs() =
            executeRequest {
                remoteConfigService.getJson<FAQContent>(FAQ_DATA_KEY)!!
            }
    }
