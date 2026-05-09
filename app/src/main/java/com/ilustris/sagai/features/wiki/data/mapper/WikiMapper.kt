package com.ilustris.sagai.features.wiki.data.mapper

import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.wiki.data.model.WikiGroup
import com.ilustris.sagai.features.wiki.data.model.WikiWithChapter

class WikiMapper(
    private val stringResourceHelper: StringResourceHelper,
) {
    suspend fun buildWikiGroups(saga: SagaContent): List<WikiGroup> =
        saga.flatChapters().map {
            val wikis = it.events.flatMap { it.updatedWikis }
            WikiGroup(
                it.data.title,
                wikis,
                canBeReviewed = wikis.size > 10,
            )
        }.filter { it.wikis.isNotEmpty() }

    fun buildWikiGroups(wikis: List<WikiWithChapter>): List<WikiGroup> =
        wikis
            .groupBy { it.chapterTitle ?: "" }
            .map { (title, wikis) ->
                val wikiItems = wikis.map { it.wiki }
                WikiGroup(
                    title,
                    wikiItems,
                    canBeReviewed = wikiItems.size > 10,
                )
        }.filter { it.wikis.isNotEmpty() }
}
