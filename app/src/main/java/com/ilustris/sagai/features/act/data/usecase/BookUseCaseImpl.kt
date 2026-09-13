package com.ilustris.sagai.features.act.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.prompts.BookPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.Book
import com.ilustris.sagai.features.act.data.model.BookChapterPages
import com.ilustris.sagai.features.act.data.model.BookChapterWriting
import com.ilustris.sagai.features.act.data.model.BookPrologueWriting
import com.ilustris.sagai.features.act.data.model.BookVolumeClosure
import com.ilustris.sagai.features.act.data.source.BookDao
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BookUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val bookDao: BookDao,
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
        private val remoteConfigService: RemoteConfigService,
    ) : BookUseCase {
        override fun writeVolume(
            sagaId: Int,
            actId: Int,
        ): Flow<VolumeProgress> =
            flow {
                val rules = remoteConfigService.getNarrativeRules()
                val readAct = suspend { executeRequest(reportCrash = false) { fetchAct(sagaId, actId).second } }

                var act =
                    when (val result = readAct()) {
                        is RequestResult.Success -> result.value
                        is RequestResult.Error -> return@flow emit(VolumeProgress.Failed(result.value.message.orEmpty()))
                    }

                if (act.needsPrologue()) {
                    emit(VolumeProgress.Prologue)
                    val result = writePrologue(sagaId, actId)
                    if (result is RequestResult.Error) return@flow emit(VolumeProgress.Failed(result.value.message.orEmpty()))
                }

                val ordered = act.bookChapters(rules)
                act.missingBookChapters(rules).forEach { chapter ->
                    val position = ordered.indexOfFirst { it.data.id == chapter.data.id } + 1
                    emit(VolumeProgress.Chapter(chapter.data.title, position, ordered.size))
                    val result = writeChapter(sagaId, chapter.data.id)
                    if (result is RequestResult.Error) return@flow emit(VolumeProgress.Failed(result.value.message.orEmpty()))
                }

                act = (readAct() as? RequestResult.Success)?.value ?: act
                val canClose = act.isComplete(rules) && act.missingBookChapters(rules).isEmpty()
                if (canClose && act.book?.isSealed() != true) {
                    emit(VolumeProgress.Closure)
                    val result = closeVolume(sagaId, actId)
                    if (result is RequestResult.Error) return@flow emit(VolumeProgress.Failed(result.value.message.orEmpty()))
                    act = (readAct() as? RequestResult.Success)?.value ?: act
                }

                emit(VolumeProgress.Done(sealed = act.hasReadableBook()))
            }

        override suspend fun writePrologue(
            sagaId: Int,
            actId: Int,
        ) = executeRequest {
            val (saga, act) = fetchAct(sagaId, actId)
            if (!act.needsPrologue()) return@executeRequest

            val rules = remoteConfigService.getNarrativeRules()
            val prompt = BookPrompts.prologuePrompt(promptService, saga, act, rules)
            val writing =
                generate<BookPrologueWriting>(saga, prompt)
                    ?: error("Prologue generation returned empty")

            val current = bookDao.getBook(actId)
            bookDao.saveBook(
                (current ?: newBook(saga, act)).copy(
                    prologue = writing.pages,
                    prologueNotes = writing.writerNotes,
                ),
            )
        }

        override suspend fun writeChapter(
            sagaId: Int,
            chapterId: Int,
        ) = executeRequest {
            if (bookDao.getChapterPages(chapterId) != null) return@executeRequest

            val saga = fetchSaga(sagaId)
            val act =
                saga.acts.find { act -> act.chapters.any { it.data.id == chapterId } }
                    ?: error("Act not found for chapter $chapterId")
            val chapter = act.chapters.first { it.data.id == chapterId }
            val rules = remoteConfigService.getNarrativeRules()
            check(chapter.isComplete(rules)) { "Chapter $chapterId is not complete yet" }

            val prompt = BookPrompts.chapterPrompt(promptService, saga, act, chapter, rules)
            val writing =
                generate<BookChapterWriting>(saga, prompt)
                    ?: error("Chapter pages generation returned empty")
            check(writing.pages.isNotEmpty()) { "Chapter pages generation returned no pages" }

            bookDao.saveChapterPages(
                BookChapterPages(
                    chapterId = chapterId,
                    pages = writing.pages.mapIndexed { index, page -> page.copy(pageNumber = index + 1) },
                    writerNotes = writing.writerNotes,
                ),
            )
        }

        override suspend fun closeVolume(
            sagaId: Int,
            actId: Int,
        ) = executeRequest {
            val (saga, act) = fetchAct(sagaId, actId)
            if (act.book?.isSealed() == true) return@executeRequest

            val rules = remoteConfigService.getNarrativeRules()
            check(act.isComplete(rules)) { "Act $actId is not complete yet" }
            check(act.missingBookChapters(rules).isEmpty()) { "Act $actId still has unwritten chapters" }

            val prompt = BookPrompts.volumeClosurePrompt(promptService, saga, act, rules)
            val closure =
                generate<BookVolumeClosure>(saga, prompt)
                    ?: error("Volume closure generation returned empty")
            check(closure.coverQuote.isNotBlank()) { "Volume closure returned an empty cover quote" }

            val current = bookDao.getBook(actId)
            bookDao.saveBook(
                (current ?: newBook(saga, act)).copy(
                    actTitle = act.data.title,
                    sagaTitle = saga.data.title,
                    coverQuote = closure.coverQuote,
                    authorNote = closure.authorNote,
                    epilogue = closure.epilogue.takeIf { it.isNotEmpty() },
                ),
            )
        }

        override suspend fun resetVolume(actId: Int) {
            bookDao.deleteChapterPagesForAct(actId)
            bookDao.deleteBookForAct(actId)
        }

        override suspend fun invalidateChapter(chapterId: Int) {
            bookDao.deleteChapterPages(chapterId)
        }

        private suspend inline fun <reified T> generate(
            saga: SagaContent,
            prompt: SplitPrompt,
        ): T? =
            gemmaClient.generate<T>(
                promptSplit =
                    prompt.mergeInstructions(
                        genreConfigService.conversationInstructions(saga.data.genre),
                    ),
                requirement = ModelRequirement.HIGH,
            )

        private fun newBook(
            saga: SagaContent,
            act: ActContent,
        ) = Book(
            actId = act.data.id,
            actTitle = act.data.title,
            sagaTitle = saga.data.title,
        )

        private suspend fun fetchSaga(sagaId: Int): SagaContent = sagaRepository.getSagaById(sagaId).first() ?: error("Saga $sagaId not found")

        private suspend fun fetchAct(
            sagaId: Int,
            actId: Int,
        ): Pair<SagaContent, ActContent> {
            val saga = fetchSaga(sagaId)
            val act = saga.acts.find { it.data.id == actId } ?: error("Act $actId not found")
            return saga to act
        }
    }
