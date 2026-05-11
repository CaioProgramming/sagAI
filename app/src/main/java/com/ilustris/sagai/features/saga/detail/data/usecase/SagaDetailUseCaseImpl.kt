package com.ilustris.sagai.features.saga.detail.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.Book
import com.ilustris.sagai.features.act.data.source.ActDao
import com.ilustris.sagai.features.act.data.source.BookDao
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.ChapterInfo
import com.ilustris.sagai.features.chapter.data.source.ChapterDao
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.source.CharacterDao
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaInfo
import com.ilustris.sagai.features.home.data.model.toSaga
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.saga.datasource.MessageDao
import com.ilustris.sagai.features.saga.detail.data.model.SagaDetailResume
import com.ilustris.sagai.features.stories.data.model.StoryDailyBriefing
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.data.model.TimelineWithAct
import com.ilustris.sagai.features.timeline.data.source.TimelineDao
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.source.WikiDao
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
        private val characterDao: CharacterDao,
        private val wikiDao: WikiDao,
        private val timelineDao: TimelineDao,
        private val bookDao: BookDao,
        private val chapterDao: ChapterDao,
        private val actDao: ActDao,
        private val messageDao: MessageDao,
    ) : SagaDetailUseCase {
        override suspend fun regenerateSagaIcon(sagaId: Int): RequestResult<Saga> {
            val saga =
                sagaRepository.getSagaById(sagaId).first()
                    ?: return Exception("Saga not found").asError()
            val topCharacters = saga.mainCharacter?.let { listOf(it.data) } ?: emptyList()
            return sagaRepository.generateSagaIcon(saga.data, topCharacters)
        }

        override fun regenerateSagaIconStream(sagaId: Int): Flow<StreamingState<Saga>> =
            kotlinx.coroutines.flow.flow {
                val saga = sagaRepository.getSagaById(sagaId).first()
                if (saga == null) {
                    emit(StreamingState.Error("Saga not found"))
                    return@flow
                }
                val topCharacters = saga.mainCharacter?.let { listOf(it.data) } ?: emptyList()
                sagaRepository.generateSagaIconStream(saga.data, topCharacters).collect {
                    emit(it)
                }
            }

        override suspend fun fetchSaga(sagaId: Int) = sagaRepository.getSagaById(sagaId)

        override fun getSagaResume(sagaId: Int): Flow<SagaDetailResume> {
            val sagaInfoFlow = sagaRepository.getSagaInfo(sagaId)
            val topCharactersFlow = characterDao.getTopCharacters(sagaId, 4)
            val wikisFlow = wikiDao.getLatestWikis(sagaId, 4)
            val latestEventFlow = timelineDao.getLatestEventBySaga(sagaId)
            val booksFlow = bookDao.getBooksBySaga(sagaId)
            val chaptersCountFlow = chapterDao.getChaptersCount(sagaId)
            val timelineCountFlow = timelineDao.getTimelineCountBySaga(sagaId)
            val actsFlow = actDao.getActContentsForSaga(sagaId)
            val charactersCountFlow = characterDao.getCharactersCount(sagaId)
            val chaptersInfoFlow = chapterDao.getChaptersInfoBySaga(sagaId)
            val fullChaptersFlow = chapterDao.getChaptersBySaga(sagaId)
            val messagesCountFlow = messageDao.getMessagesCount(sagaId)

            return combine(
                sagaInfoFlow,
                topCharactersFlow,
                wikisFlow,
                latestEventFlow,
                booksFlow,
                chaptersCountFlow,
                timelineCountFlow,
                actsFlow,
                charactersCountFlow,
                chaptersInfoFlow,
                fullChaptersFlow,
                messagesCountFlow,
            ) { flows: Array<Any?> ->
                val sagaInfo = flows[0] as SagaInfo?
                val topCharacters = flows[1] as List<CharacterContent>
                val wikis = flows[2] as List<Wiki>
                val event = flows[3] as TimelineWithAct?
                val books = flows[4] as List<Book>
                val chaptersCount = flows[5] as Int
                val timelineCount = flows[6] as Int
                val acts = flows[7] as List<ActContent>
                val charCount = flows[8] as Int
                val chapters = flows[9] as List<ChapterInfo>
                val fullChapters = flows[10] as List<ChapterContent>
                val messagesCount = flows[11] as Int

                val narrativeRules = remoteConfigService.getNarrativeRules()
                SagaDetailResume(
                    saga = sagaInfo?.toSaga() ?: Saga(),
                    starringCharacter = topCharacters.firstOrNull(),
                    topCharacters = topCharacters,
                    latestWikis = wikis,
                    latestEvent = event,
                    generatedBooks = books,
                    chapters = chapters,
                    fullChapters = fullChapters,
                    chaptersCount = chaptersCount,
                    eventsCount = timelineCount,
                    charactersCount = charCount,
                    messagesCount = messagesCount,
                    playtime = sagaInfo?.playTimeMs ?: 0L,
                    completedActsCount = acts.count { it.isComplete(narrativeRules) },
                    hasActs = acts.isNotEmpty(),
                )
            }
        }

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

        override suspend fun createEmotionalConclusion(sagaId: Int): RequestResult<Saga> =
            executeRequest {
                val saga = sagaRepository.getSagaById(sagaId).first()!!
                val request =
                    emotionalUseCase.generateEmotionalConclusion(saga).getSuccess()!!

                sagaRepository
                    .updateSaga(
                        saga.data.copy(
                            emotionalProfile = request.emotionalProfile,
                            emotionalReview = request.emotionalProfile.emotionalContent,
                        ),
                    )
                sagaRepository.getSagaById(sagaId).first()!!.data
            }

        override suspend fun generateTimelineContent(
            sagaId: Int,
            timelineContent: TimelineContent,
        ): RequestResult<Unit> {
            val saga = sagaRepository.getSagaById(sagaId).first()!!
            return timelineUseCase.generateTimelineContent(saga, timelineContent)
        }

        override suspend fun reviewWiki(
            sagaId: Int,
            wikis: List<Wiki>,
        ) {
            val saga = sagaRepository.getSagaById(sagaId).first()!!
            wikiUseCase.mergeWikis(saga, wikis)
        }

        override fun getBackupEnabled() = backupService.backupEnabled()

        override suspend fun generateStoryBriefing(sagaId: Int): RequestResult<StoryDailyBriefing> =
            executeRequest {
                val saga = sagaRepository.getSagaById(sagaId).first()!!
                val prompt =
                    SagaPrompts.generateStoryBriefing(
                        promptService,
                        saga,
                        genreConfigService.conversationBlueprint(saga.data.genre),
                    )
                textGenClient.generate<StoryDailyBriefing>(prompt)!!
            }
    }
