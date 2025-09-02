package com.ilustris.sagai.features.chapter.data.usecase

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImageReference
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
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import javax.inject.Inject

class ChapterUseCaseImpl
    @Inject
    constructor(
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
            gemmaClient
                .generate<Chapter>(
                    prompt =
                        generateChapterPrompt(
                            saga = saga,
                            currentChapter = chapterContent,
                        ),
                    requireTranslation = true,
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
                val characters = chapter.fetchCharacters(saga).ifEmpty { listOf(saga.mainCharacter!!.data) }
                val coverBitmap = genreReferenceHelper.getCoverReference(saga.data.genre).getSuccess()
                val coverReference =
                    coverBitmap?.let {
                        ImageReference(
                            it,
                            "Cover composition aesthetic and reference",
                        )
                    }
                val charactersIcons =
                    characters.mapNotNull { character ->

                        val characterBitmap =
                            fileHelper
                                .readFile(character.image)
                                ?.decodeToImageBitmap()
                                ?.asAndroidBitmap()

                        characterBitmap?.let {
                            ImageReference(
                                it,
                                "Character ${character.name} visual reference.",
                            )
                        }
                    }
                val imageReferences = listOf(coverReference).plus(charactersIcons).filterNotNull()
                val promptGeneration =
                    gemmaClient.generate<String>(
                        ChapterPrompts.coverDescription(
                            saga,
                            chapter.data,
                            characters,
                        ),
                        references = imageReferences,
                        requireTranslation = false,
                    )
                val genCover =
                    imagenClient
                        .generateImage(promptGeneration!!, imageReferences)
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
