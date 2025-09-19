package com.ilustris.sagai.features.chapter.data.usecase

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.models.ImageReference
import com.ilustris.sagai.core.ai.prompts.ChapterPrompts
import com.ilustris.sagai.core.ai.prompts.ImageGuidelines
import com.ilustris.sagai.core.ai.prompts.ImageRules
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.GenreReferenceHelper
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.ChapterGeneration
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class ChapterUseCaseImpl
    @Inject
    constructor(
        private val chapterRepository: ChapterRepository,
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
        ) = executeRequest {
            gemmaClient
                .generate<ChapterGeneration>(
                    prompt =
                        generateChapterPrompt(
                            saga = saga,
                            currentChapter = chapterContent,
                        ),
                    requireTranslation = true,
                    skipRunning = true,
                )!!
        }

        @OptIn(PublicPreviewAPI::class)
        override suspend fun generateChapterCover(
            chapter: ChapterContent,
            saga: SagaContent,
        ): RequestResult<Chapter> =
            executeRequest {
                val characters = chapter.fetchCharacters(saga).ifEmpty { listOf(saga.mainCharacter!!.data) }
                val coverBitmap = genreReferenceHelper.getCoverReference(saga.data.genre).getSuccess()
                val coverReference =
                    coverBitmap?.let {
                        ImageReference(
                            it,
                            ImageGuidelines.compositionReferenceGuidance,
                        )
                    }
                val styleReference =
                    genreReferenceHelper.getGenreStyleReference(saga.data.genre).getSuccess()?.let {
                        ImageReference(
                            it,
                            ImageGuidelines.styleReferenceGuidance,
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
                                ImageGuidelines.characterVisualReferenceGuidance(character.name),
                            )
                        }
                    }
                val imageReferences =
                    listOf(coverReference, styleReference)
                        .plus(charactersIcons)
                        .filterNotNull()
                val promptGeneration =
                    gemmaClient.generate<String>(
                        ChapterPrompts.coverDescription(
                            saga,
                            chapter.data,
                            characters,
                        ),
                        references = imageReferences,
                        requireTranslation = false,
                        skipRunning = true,
                    )
                val genCover =
                    imagenClient
                        .generateImage(promptGeneration!!.plus(ImageRules.TEXTUAL_ELEMENTS), imageReferences)
                val coverFile =
                    fileHelper.saveFile(chapter.data.title, genCover, path = "${saga.data.id}/chapters/")
                val newChapter =
                    chapter.data.copy(
                        coverImage = coverFile?.path ?: emptyString(),
                    )

                chapterRepository.updateChapter(newChapter)
            }

        private fun generateChapterPrompt(
            saga: SagaContent,
            currentChapter: ChapterContent,
        ) = ChapterPrompts.chapterGeneration(saga, currentChapter)

        override suspend fun generateChapterIntroduction(
            saga: SagaContent,
            chapter: Chapter,
            act: ActContent,
        ): RequestResult<Chapter> =
            executeRequest {
                delay(2.seconds)
                val prompt = ChapterPrompts.chapterIntroductionPrompt(saga, chapter, act)
                val intro = gemmaClient.generate<String>(prompt, requireTranslation = true, skipRunning = true)!!
                val updated = chapter.copy(introduction = intro)
                chapterRepository.updateChapter(updated)
            }
    }
