package com.ilustris.sagai.features.saga.detail.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.stories.data.model.StoryDailyBriefing
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import javax.inject.Inject

class SagaDetailUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val fileHelper: FileHelper,
        private val textGenClient: GemmaClient,
        private val timelineUseCase: TimelineUseCase,
        private val emotionalUseCase: EmotionalUseCase,
        private val wikiUseCase: WikiUseCase,
        private val backupService: BackupService,
        private val genreConfigService: GenreConfigService,
        private val promptService: PromptService,
        private val remoteConfigService: RemoteConfigService,
    ) : SagaDetailUseCase {
        override suspend fun regenerateSagaIcon(saga: SagaContent): RequestResult<Saga> {
            val topCharacters = listOf(saga.mainCharacter!!.data)
            return sagaRepository.generateSagaIcon(saga.data, topCharacters)
        }

        override suspend fun fetchSaga(sagaId: Int) = sagaRepository.getSagaById(sagaId)

        override suspend fun deleteSaga(saga: Saga) {
            fileHelper.deletePath(saga.id)
            sagaRepository.deleteChat(saga)
        }

        override suspend fun resetReview(content: SagaContent) {
            sagaRepository.updateSaga(
                content.data.copy(
                    review = null,
                ),
            )
        }

        override suspend fun createEmotionalConclusion(currentSaga: SagaContent) =
            executeRequest {
                val request =
                    emotionalUseCase.generateEmotionalConclusion(currentSaga).getSuccess()!!

                sagaRepository
                    .updateSaga(
                        currentSaga.data.copy(
                            emotionalProfile = request,
                            emotionalReview = request.emotionalContent,
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

        override fun getBackupEnabled() = backupService.backupEnabled()

        override suspend fun generateStoryBriefing(saga: SagaContent): RequestResult<StoryDailyBriefing> =
            executeRequest {
                val prompt =
                    SagaPrompts.generateStoryBriefing(
                        promptService,
                        saga,
                        genreConfigService.conversationBlueprint(saga.data.genre),
                    )
                textGenClient.generate<StoryDailyBriefing>(prompt)!!
            }
    }
