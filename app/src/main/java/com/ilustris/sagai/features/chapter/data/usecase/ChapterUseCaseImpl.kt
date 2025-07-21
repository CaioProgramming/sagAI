package com.ilustris.sagai.features.chapter.data.usecase

import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.ChapterPrompts
import com.ilustris.sagai.core.ai.prompts.GenrePrompts
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.network.body.FreepikRequest
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterGen
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
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
            saga: Saga,
            characters: List<Character>,
        ): RequestResult<Exception, Chapter> =
            try {
                if (characters.isEmpty()) {
                    Exception("No characters provided").asError()
                }
                val prompt = ImagePrompts.chapterCover(saga, characters)
                val freepikRequest =
                    FreepikRequest(
                        prompt = prompt,
                        negative_prompt = GenrePrompts.negativePrompt(saga.genre),
                        GenrePrompts.chapterCoverStyling(saga.genre),
                    )
                val promptGeneration =
                    textGenClient.generate<String>(
                        ChapterPrompts.coverDescription(
                            saga,
                            chapter,
                            characters,
                        ),
                        requireTranslation = false,
                    )
                val genCover =
                    imagenClient
                        .generateImage(ChapterPrompts.coverGeneration(saga, promptGeneration!!))!!
                val coverFile =
                    fileHelper.saveFile(chapter.title, genCover, path = "${saga.id}/chapters/")
                updateChapter(chapter.copy(coverImage = coverFile!!.absolutePath)).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        private fun generateChapterPrompt(
            saga: SagaContent,
            lastAddedEvents: List<Timeline>,
        ) = ChapterPrompts.chapterGeneration(saga, lastAddedEvents)
    }
