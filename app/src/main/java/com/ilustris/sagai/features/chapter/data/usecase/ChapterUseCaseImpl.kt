package com.ilustris.sagai.features.chapter.data.usecase

import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.ChapterPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterGen
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import javax.inject.Inject

class ChapterUseCaseImpl
    @Inject
    constructor(
        private val chapterRepository: ChapterRepository,
        private val textGenClient: TextGenClient,
        private val gemmaClient: GemmaClient,
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
            lastAddedEvents: List<Timeline>,
        ) = try {
            textGenClient
                .generate<ChapterGen>(
                    prompt =
                        generateChapterPrompt(
                            saga = saga,
                            lastAddedEvents = lastAddedEvents,
                        ),
                    requireTranslation = true,
                    customSchema = ChapterGen.toSchema(),
                )!!
                .asSuccess()
        } catch (e: Exception) {
            e.asError()
        }

        @OptIn(PublicPreviewAPI::class)
        override suspend fun generateChapterCover(
            chapter: Chapter,
            saga: SagaContent,
            characters: List<Character>,
        ): RequestResult<Exception, Chapter> =
            try {
                val promptGeneration =
                    gemmaClient.generate<String>(
                        ChapterPrompts.coverDescription(
                            saga,
                            chapter,
                            characters,
                        ),
                        requireTranslation = false,
                    )
                val genCover =
                    imagenClient
                        .generateImage(ChapterPrompts.coverGeneration(saga.data, promptGeneration!!))!!
                val coverFile =
                    fileHelper.saveFile(chapter.title, genCover, path = "${saga.data.id}/chapters/")
                val newChapter =
                    chapter.copy(coverImage = coverFile!!.path)

                chapterRepository.updateChapter(newChapter).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        private fun generateChapterPrompt(
            saga: SagaContent,
            lastAddedEvents: List<Timeline>,
        ) = ChapterPrompts.chapterGeneration(saga, lastAddedEvents)
    }
