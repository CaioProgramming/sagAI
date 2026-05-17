package com.ilustris.sagai.features.saga.detail.data.model

import com.ilustris.sagai.features.act.data.model.Book
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.timeline.data.model.TimelineWithAct
import com.ilustris.sagai.features.wiki.data.model.Wiki

data class SagaDetailResume(
    val saga: Saga,
    val starringCharacter: CharacterContent? = null,
    val latestEvent: TimelineWithAct? = null,
    val topCharacters: List<CharacterContent> = emptyList(),
    val latestWikis: List<Wiki> = emptyList(),
    val generatedBooks: List<Book> = emptyList(),
    val chapters: List<com.ilustris.sagai.features.chapter.data.model.ChapterInfo> = emptyList(),
    val fullChapters: List<com.ilustris.sagai.features.chapter.data.model.ChapterContent> = emptyList(),
    val chaptersCount: Int = 0,
    val charactersCount: Int = 0,
    val messagesCount: Int = 0,
    val playtime: Long = 0,
    val eventsCount: Int = 0,
    val completedActsCount: Int = 0,
    val hasActs: Boolean = false,
)
