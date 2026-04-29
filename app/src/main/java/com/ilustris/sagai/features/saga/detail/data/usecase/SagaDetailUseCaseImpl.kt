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
import com.ilustris.sagai.core.services.getNarrativeRules
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
            sagaRepository.updateChat(
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
                    .updateChat(
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

        override suspend fun generateSagaResume(saga: SagaContent): RequestResult<String> =
            executeRequest {
                val narrativeRules = remoteConfigService.getNarrativeRules()

                if (saga.completedChapters(narrativeRules) < 1) {
                    return@executeRequest saga.data.description
                }
                val prompt =
                    SagaPrompts.sagaResume(
                        promptService,
                        saga,
                        genreConfigService.conversationBlueprint(saga.data.genre),
                    )
                textGenClient.generate<String>(
                    prompt,
                    requirement = GemmaClient.ModelRequirement.HIGH,
                ) ?: saga.data.description
            }

        override suspend fun generateCharactersInsight(saga: SagaContent): RequestResult<String> =
            executeRequest {
                val prompt =
                    SagaPrompts.charactersInsight(
                        promptService,
                        saga,
                        genreConfigService.conversationBlueprint(saga.data.genre),
                    )
                textGenClient.generate<String>(
                    prompt,
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                ) ?: ""
            }

        override suspend fun generateWikiInsight(saga: SagaContent): RequestResult<String> =
            executeRequest {
                if (saga.wikis.isEmpty()) return@executeRequest ""
                val prompt =
                    SagaPrompts.wikiInsight(
                        promptService,
                        saga,
                        genreConfigService.conversationBlueprint(saga.data.genre),
                    )
                textGenClient.generate<String>(
                    prompt,
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                ) ?: ""
            }

        override suspend fun generateTimelineInsight(saga: SagaContent): RequestResult<String> =
            executeRequest {
                val narrativeRules = remoteConfigService.getNarrativeRules()
                if (saga.completedEvents(narrativeRules) < 3) return@executeRequest ""
                val prompt =
                    SagaPrompts.timelineInsight(
                        promptService,
                        saga,
                        genreConfigService.conversationBlueprint(saga.data.genre),
                    )
                textGenClient.generate<String>(
                    prompt,
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                ) ?: ""
            }
    }
