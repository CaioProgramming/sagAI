package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.timeline.data.model.LoreGen
import kotlinx.coroutines.flow.MutableStateFlow

interface SagaContentManager {
    val content: MutableStateFlow<SagaContent?>

    suspend fun loadSaga(sagaId: String)

    suspend fun createNewChapter(): Chapter?

    suspend fun updateChapter(chapter: Chapter)

    suspend fun checkForChapter(): RequestResult<Exception, Chapter>

    suspend fun updateLore(
        reference: Message,
        messageSubList: List<MessageContent>,
    ): RequestResult<Exception, LoreGen>

    suspend fun generateCharacter(message: Message): Character?
}
