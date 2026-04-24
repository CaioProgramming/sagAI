package com.ilustris.sagai.features.wiki.data.mapper

import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.wiki.data.model.WikiGroup

class WikiMapper(
    private val stringResourceHelper: StringResourceHelper,
) {
    suspend fun buildWikiGroups(saga: SagaContent): List<WikiGroup> =
        saga.flatChapters().map {
            WikiGroup(
                it.data.title,
                it.events.flatMap { it.updatedWikis },
                canBeReviewed = it.events.flatMap { it.updatedWikis }.size > 10,
            )
        }
}
