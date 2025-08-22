package com.ilustris.sagai.features.chapter.data.usecase

import android.content.Context
import coil3.ImageLoader
import coil3.request.ImageRequest
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.ChapterPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.GenreReferenceHelper
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.ChapterGen
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ChapterUseCaseImpl
    @Inject
    constructor(
        @ApplicationContext
        private val context: Context,
        private val chapterRepository: ChapterRepository,
        private val textGenClient: TextGenClient,
        private val gemmaClient: GemmaClient,
        private val imagenClient: ImagenClient,
        private val fileHelper: FileHelper,
        private val genreReferenceHelper: GenreReferenceHelper,
    ) : ChapterUseCase {
        override suspend fun saveChapter(chapter: Chapter): Chapter = chapterRepository.saveChapter(chapter)

        override suspend fun deleteChapter(chapter: Chapter) = chapterRepository.deleteChapter(chapter)

        override suspend fun updateChapter(chapter: Chapter) = chapterRepository.updateChapter(chapter)

        override suspend fun deleteChapterById(chapterId: Int) = chapterRepository.deleteChapterById(chapterId)

        override suspend fun deleteAllChapters() = chapterRepository.deleteAllChapters()

        override suspend fun generateChapter(
            saga: SagaContent,
            chapterContent: ChapterContent,
        ) = try {
            textGenClient
                .generate<ChapterGen>(
                    prompt =
                        generateChapterPrompt(
                            saga = saga,
                            currentChapter = chapterContent,
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
            chapter: ChapterContent,
            saga: SagaContent,
        ): RequestResult<Exception, Chapter> =
            try {
                val characters = chapter.fetchCharacters(saga).ifEmpty { listOf(saga.mainCharacter) }.filterNotNull()
                val coverReference = genreReferenceHelper.getCoverReference(saga.data.genre).getSuccess()
                val promptGeneration =
                    gemmaClient.generate<String>(
                        ChapterPrompts.coverDescription(
                            saga,
                            chapter.data,
                            characters,
                        ),
                        references = listOf(coverReference),
                        requireTranslation = false,
                    )
                val genCover =
                    imagenClient
                        .generateImage(promptGeneration!!)
                val coverFile =
                    fileHelper.saveFile(chapter.data.title, genCover, path = "${saga.data.id}/chapters/")
                val newChapter =
                    chapter.data.copy(
                        coverImage = coverFile!!.path,
                    )

                chapterRepository.updateChapter(newChapter).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        private fun generateChapterPrompt(
            saga: SagaContent,
            currentChapter: ChapterContent,
        ) = ChapterPrompts.chapterGeneration(saga, currentChapter)
    }
