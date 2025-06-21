package com.ilustris.sagai.features.chapter.data.usecase

import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.chapterPrompt
import com.ilustris.sagai.core.ai.coverPrompt
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import javax.inject.Inject

class ChapterUseCaseImpl
    @Inject
    constructor(
        private val chapterRepository: ChapterRepository,
        private val textGenClient: TextGenClient,
        private val imagenClient: ImagenClient,
        private val fileHelper: FileHelper,
    ) : ChapterUseCase {
        override fun getChaptersBySagaId(sagaId: Int) = chapterRepository.getChaptersBySagaId(sagaId)

        override suspend fun getChapterBySagaAndMessageId(
            sagaId: Int,
            messageId: Int,
        ) = chapterRepository.getChapterBySagaAndMessageId(sagaId, messageId)

        override suspend fun saveChapter(chapter: Chapter): Chapter = chapterRepository.saveChapter(chapter)

        override suspend fun deleteChapter(chapter: Chapter) = chapterRepository.deleteChapter(chapter)

        override suspend fun updateChapter(chapter: Chapter) = chapterRepository.updateChapter(chapter)

        override suspend fun deleteChapterById(chapterId: Int) = chapterRepository.deleteChapterById(chapterId)

        override suspend fun deleteAllChapters() = chapterRepository.deleteAllChapters()

        override suspend fun generateChapter(
            saga: SagaContent,
            messageReference: Message,
            messages: List<MessageContent>,
        ) = try {
            val genText =
                textGenClient.generate<Chapter>(
                    generateChapterPrompt(
                        saga = saga,
                        messages = messages,
                    ),
                    true,
                )
            val chapterCover =
                generateChapterCover(
                    chapter = genText!!,
                    saga = saga.data,
                    characters = saga.characters,
                )
            val coverFile =
                fileHelper.saveFile(genText.title, chapterCover!!, path = "${saga.data.id}/chapters/")

            saveChapter(
                genText.copy(
                    messageReference = messageReference.id,
                    sagaId = saga.data.id,
                    coverImage = coverFile?.absolutePath ?: emptyString(),
                ),
            ).asSuccess()
        } catch (e: Exception) {
            e.asError()
        }

        @OptIn(PublicPreviewAPI::class)
        suspend fun generateChapterCover(
            chapter: Chapter,
            saga: SagaData,
            characters: List<Character>,
        ): ByteArray? =
            try {
                val genCover = imagenClient.generateImage(chapter.coverPrompt(saga, characters))
                genCover!!.data
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

        private fun generateChapterPrompt(
            saga: SagaContent,
            messages: List<MessageContent>,
        ) = chapterPrompt(
            sagaData = saga,
            messages = messages,
        )
    }
