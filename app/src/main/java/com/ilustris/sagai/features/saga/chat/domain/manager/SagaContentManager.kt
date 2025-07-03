package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import kotlinx.coroutines.flow.MutableStateFlow

interface SagaContentManager {
    val content: MutableStateFlow<SagaContent?>

    suspend fun createAct(): RequestResult<Exception, Act>

    suspend fun loadSaga(sagaId: String)

    suspend fun createNewChapter(): Chapter?

    suspend fun updateChapter(chapter: Chapter)

    suspend fun checkForChapter(): RequestResult<Exception, Chapter>

    suspend fun updateLore(
        reference: Message,
        messageSubList: List<MessageContent>,
    ): RequestResult<Exception, Timeline>

    suspend fun generateCharacter(message: Message): RequestResult<Exception, Character>

    fun getDirective(): String
    suspend fun updateAct() : RequestResult<Exception, Act>
}
