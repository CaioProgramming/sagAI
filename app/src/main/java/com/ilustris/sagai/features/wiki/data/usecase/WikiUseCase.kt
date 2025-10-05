package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki

interface WikiUseCase {
    suspend fun saveWiki(wiki: Wiki): Long

    suspend fun updateWiki(wiki: Wiki)

    suspend fun deleteWiki(wikiId: Int)

    suspend fun deleteWikisBySaga(sagaId: Int)

    suspend fun generateWiki(
        saga: SagaContent,
        event: Timeline,
    ): RequestResult<List<Wiki>>

    suspend fun mergeWikis(
        saga: SagaContent,
        currentChapterContent: ChapterContent,
    ): RequestResult<Unit>
}
