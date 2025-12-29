package com.ilustris.sagai.features.saga.detail.data.usecase

import android.net.Uri
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.features.act.data.usecase.ActUseCase
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.saga.chat.repository.SagaBackupService
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.saga.detail.data.model.Review
import com.ilustris.sagai.features.stories.data.model.StoryDailyBriefing
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class SagaDetailUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val fileHelper: FileHelper,
        private val textGenClient: GemmaClient,
        private val timelineUseCase: TimelineUseCase,
        private val emotionalUseCase: EmotionalUseCase,
        private val chapterUseCase: ChapterUseCase,
        private val actUseCase: ActUseCase,
        private val wikiUseCase: WikiUseCase,
        private val sagaBackupService: SagaBackupService,
        private val backupService: com.ilustris.sagai.core.file.BackupService,
    ) : SagaDetailUseCase {
        override suspend fun regenerateSagaIcon(saga: SagaContent): RequestResult<Saga> =
            sagaRepository
                .generateSagaIcon(saga.data, saga.mainCharacter!!.data)

        override suspend fun fetchSaga(sagaId: Int) = sagaRepository.getSagaById(sagaId)

        override suspend fun deleteSaga(saga: Saga) {
            fileHelper.deletePath(saga.id)
            sagaRepository.deleteChat(saga)
        }

        override suspend fun createReview(content: SagaContent): RequestResult<Saga> =
            executeRequest {
                val prompt =
                    SagaPrompts.reviewGeneration(
                        saga = content,
                    )

                val review =
                    textGenClient.generate<Review>(
                        prompt = prompt,
                        requireTranslation = true,
                    )!!
                sagaRepository
                    .updateChat(
                        content.data.copy(
                            review = review,
                        ),
                    )
            }

        override suspend fun resetReview(content: SagaContent) {
            sagaRepository.updateChat(
                content.data.copy(
                    review = null,
                ),
            )
        }

        override suspend fun createEmotionalConclusion(currentSaga: SagaContent) =
            executeRequest {
                val unReviewedTimelines =
                    currentSaga.flatEvents().filter { it.data.emotionalReview.isNullOrEmpty() }
                val unReviewedChapters =
                    currentSaga.flatChapters().filter { it.data.emotionalReview.isNullOrEmpty() }
                val unReviewedActs =
                    currentSaga.acts.filter { it.data.emotionalReview.isNullOrEmpty() }

                unReviewedTimelines.forEach {
                    delay(3.seconds)
                    timelineUseCase.generateEmotionalReview(currentSaga, it)
                }

                unReviewedChapters.forEach {
                    delay(3.seconds)
                    chapterUseCase.generateEmotionalReview(currentSaga, it)
                }

                unReviewedActs.forEach {
                    delay(3.seconds)
                    actUseCase.generateEmotionalProfile(currentSaga, it)
                }

                delay(3.seconds)

                val request =
                    emotionalUseCase.generateEmotionalConclusion(currentSaga).getSuccess()!!

                sagaRepository
                    .updateChat(
                        currentSaga.data.copy(
                            emotionalReview = request,
                        ),
                    )
            }

        override suspend fun generateTimelineContent(
            saga: SagaContent,
            timelineContent: TimelineContent,
        ) = timelineUseCase.generateTimelineContent(saga, timelineContent)

        override suspend fun reviewWiki(
            currentsaga: SagaContent,
            wikis: List<Wiki>,
        ) {
            wikiUseCase.mergeWikis(currentsaga, wikis)
        }

        override suspend fun exportSaga(
            sagaId: Int,
            destinationUri: Uri,
        ) = sagaBackupService.exportSaga(sagaId, destinationUri)

        override fun getBackupEnabled() = backupService.backupEnabled()

        override suspend fun generateStoryBriefing(saga: SagaContent): RequestResult<StoryDailyBriefing> =
            executeRequest {
                val prompt = SagaPrompts.generateStoryBriefing(saga)
                textGenClient.generate<StoryDailyBriefing>(prompt)!!
            }

        override suspend fun generateSagaResume(saga: SagaContent): RequestResult<String> =
            executeRequest {
                val prompt = SagaPrompts.sagaResume(saga)
                textGenClient.generate<String>(
                    prompt,
                    requirement = GemmaClient.ModelRequirement.HIGH,
                )!!
        }
    }
