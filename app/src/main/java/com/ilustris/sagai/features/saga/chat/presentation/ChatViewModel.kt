package com.ilustris.sagai.features.saga.chat.presentation

import MessageStatus
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.GuardrailsException
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.media.MediaPlayerManager
import com.ilustris.sagai.core.media.MediaPlayerManagerImpl
import com.ilustris.sagai.core.media.SagaPlaybackService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.notifications.ScheduledNotificationService
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.theme.SagaThemeManager
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.saga.chat.data.manager.ChatNotificationManager
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.data.mapper.SagaMetadataUIMapper
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoStatus
import com.ilustris.sagai.features.saga.chat.data.model.toGenerationDescription
import com.ilustris.sagai.features.saga.chat.data.usecase.GetInputSuggestionsUseCase
import com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.saga.chat.ui.components.audio.AudioPlaybackState
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import com.ilustris.sagai.features.wiki.data.mapper.WikiMapper
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(PublicPreviewAPI::class)
@HiltViewModel
class ChatViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val messageUseCase: MessageUseCase,
        private val sagaContentManager: SagaContentManager,
        private val suggestionUseCase: GetInputSuggestionsUseCase,
        private val notificationManager: ChatNotificationManager,
        private val settingsUseCase: SettingsUseCase,
        private val imageSegmentationHelper: ImageSegmentationHelper,
        private val scheduledNotificationService: ScheduledNotificationService,
        private val remoteConfigService: RemoteConfigService,
        private val mapper: SagaMetadataUIMapper,
        private val wikiUseCase: WikiUseCase,
        private val wikiMapper: WikiMapper,
        private val characterUseCase: CharacterUseCase,
        private val sagaThemeManager: SagaThemeManager,
    ) : ViewModel(),
        DefaultLifecycleObserver {
        private val stateManager = ChatStateManager()
        val uiState = stateManager.uiState

        private val _genreVfxPulse = MutableStateFlow(false)
        val genreVfxPulse = _genreVfxPulse.asStateFlow()

        val segmentedImageCache = LruCache<String, Bitmap?>(5 * 1024 * 1024)
        private var audioProgressJob: kotlinx.coroutines.Job? = null
        private val audioMediaPlayerManager: MediaPlayerManager = MediaPlayerManagerImpl(context)
        var isForeground = true
        private val messageDelay = 7.seconds
        private var sendJob: kotlinx.coroutines.Job? = null

        private var lastActId: Int = 0
        private var lastChapterId: Int = 0
        private var lastEventId: Int = 0
        private var lastMessageCount: Int = 0
        private var wikiObserverJob: kotlinx.coroutines.Job? = null

        private var sagaObserverJob: kotlinx.coroutines.Job? = null
        private var milestoneObserverJob: kotlinx.coroutines.Job? = null
        private var preferencesObserverJob: kotlinx.coroutines.Job? = null
        private var snackBarObserverJob: kotlinx.coroutines.Job? = null
        private var mediaObserverJob: kotlinx.coroutines.Job? = null
        private var processingObserverJob: kotlinx.coroutines.Job? = null
        private var sceneSummaryObserverJob: kotlinx.coroutines.Job? = null
        private var reasoningObserverJob: kotlinx.coroutines.Job? = null
        private var characterObserverJob: kotlinx.coroutines.Job? = null
        private var topCharacterObserverJob: kotlinx.coroutines.Job? = null
        private var fullWikiObserverJob: kotlinx.coroutines.Job? = null
        private var generationJob: kotlinx.coroutines.Job? = null
        private var narrativeObserverJob: kotlinx.coroutines.Job? = null

        init {
            viewModelScope.launch {
                sagaThemeManager.vfxTrigger.collect {
                    _genreVfxPulse.value = true
                    delay(2.seconds)
                    _genreVfxPulse.value = false
                }
            }
        }

        fun handleAction(action: ChatUiAction) {
            when (action) {
                is ChatUiAction.SendInput -> {
                    sendInput(action.userConfirmed, action.isAudio)
                }

                is ChatUiAction.UpdateInput -> {
                    updateInput(action.value)
                }

                is ChatUiAction.UpdateSenderType -> {
                    updateSendType(action.type)
                }

                is ChatUiAction.OpenMessageOptions -> {
                    openMessageOptions(action.message)
                }

                is ChatUiAction.DeleteMessage -> {
                    deleteMessage(action.message)
                }

                is ChatUiAction.EditMessage -> {
                    editMessage(action.message)
                }

                is ChatUiAction.SaveEdit -> {
                    saveEdit()
                }

                is ChatUiAction.CancelEdit -> {
                    cancelEdit()
                }

                is ChatUiAction.RetryAiResponse -> {
                    retryAiResponse(action.message)
                }

                is ChatUiAction.PlayOrPauseAudio -> {
                    playOrPauseAudio(action.messageContent)
                }

                is ChatUiAction.RequestNewCharacter -> {
                    requestNewCharacter(action.name, action.message)
                }

                is ChatUiAction.ReviewEvent -> {
                    reviewEvent(action.timelineContent)
                }

                is ChatUiAction.ReviewChapter -> {
                    reviewChapter(action.chapterContent)
                }

                is ChatUiAction.UpdateCharacter -> {
                    updateCharacter(action.character)
                }

                is ChatUiAction.RegenerateAudio -> {
                    regenerateAudio(action.messageContent)
                }

                is ChatUiAction.ToggleSelectionMode -> {
                    stateManager.toggleSelectionMode()
                }

                is ChatUiAction.ToggleMessageSelection -> {
                    stateManager.toggleMessageSelection(action.messageId)
                }

                is ChatUiAction.ClearSelection -> {
                    stateManager.clearSelection()
                }

                is ChatUiAction.ShareConversation -> {
                    stateManager.updateShareSheetVisibility(action.show)
                }

                is ChatUiAction.Back -> { // UI action
                }

                is ChatUiAction.OpenSagaDetails -> { // UI action
                }

                is ChatUiAction.InjectFakeMessages -> {
                    sendFakeUserMessages(action.count)
                }

                is ChatUiAction.RefreshSaga -> {
                    checkSaga()
                }

                is ChatUiAction.EnableBackup -> {
                    enableBackup(action.uri)
                }

                is ChatUiAction.RequestAudioTranscript -> {
                    stateManager.updateAudioTranscriptVisibility(action.show)
                }

                is ChatUiAction.ReviewWiki -> {
                    reviewWiki(action.wikis)
                }

                is ChatUiAction.AppendWiki -> {
                    appendWiki(action.wiki)
                }

                is ChatUiAction.DismissMilestone -> {
                    sagaContentManager.dismissMilestone()
                }

                is ChatUiAction.ContinueMilestone -> {
                    viewModelScope.launch {
                        sagaContentManager.continueMilestone()
                    }
                }

                is ChatUiAction.ShowObjective -> {
                    viewModelScope.launch {
                        sagaContentManager.showObjective()
                    }
                }

                is ChatUiAction.AdvanceNarrative -> {
                    generationJob =
                        viewModelScope.launch(Dispatchers.IO) {
                            sagaContentManager.advanceNarrative()
                        }
                }

                is ChatUiAction.StopGeneration -> {
                    stopGeneration()
                }

                is ChatUiAction.OpenCharacter -> {
                    doNothing()
                }
            }
        }

        fun stopGeneration() {
            Timber.i("StopGeneration action received")
            generationJob?.cancel()
            generationJob = null
            sendJob?.cancel()
            sendJob = null
            sagaContentManager.stopProcessing()
            updateLoading(false)
            stateManager.updateGenerating(false)
            stateManager.updateSendingPending(false)
            stateManager.updateSendingProgress(0f)
        }

        private fun observeMileStone() =
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.milestoneUpdate.collect {
                    stateManager.updateMilestone(it)
                }
            }

        private var loadFinished = false

        fun initChat(
            sagaId: String?,
            isDebug: Boolean = false,
        ) {
            if (sagaId == null) {
                return
            }
            if (uiState.value.sagaContent
                    ?.data
                    ?.id
                    ?.toString() == sagaId
            ) {
                Timber.d("initChat: Saga $sagaId already initialized, skipping.")
                return
            }
            stateManager.updateLoading(true)
            notificationManager.clearNotifications()
            lastActId = 0
            lastChapterId = 0
            lastEventId = 0
            lastMessageCount = 0
            loadFinished = false
            stateManager.updateState {
                it.copy(
                    chatState = ChatState.Loading,
                    sagaContent = null,
                    messages = emptyList(),
                    characters = emptyList(),
                )
            }
            sagaContentManager.content.value = null
            enableDebugMode(isDebug)

            sagaObserverJob?.cancel()
            sagaObserverJob = observeSaga()

            milestoneObserverJob?.cancel()
            milestoneObserverJob = observeMileStone()

            preferencesObserverJob?.cancel()
            preferencesObserverJob = observePreferences()

            snackBarObserverJob?.cancel()
            snackBarObserverJob = observeNotificationUpdates()

            mediaObserverJob?.cancel()
            mediaObserverJob = observeMediaState()

            processingObserverJob?.cancel()
            processingObserverJob = observeProcessingState()

            narrativeObserverJob?.cancel()
            narrativeObserverJob = observeNarrativeState()

            sceneSummaryObserverJob?.cancel()
            sceneSummaryObserverJob = observeSceneSummary()

            reasoningObserverJob?.cancel()
            reasoningObserverJob = observeReasoning()

            characterObserverJob?.cancel()
            characterObserverJob = observeCharacters(sagaId.toInt())

            topCharacterObserverJob?.cancel()
            topCharacterObserverJob = observeTopCharacters(sagaId.toInt())

            fullWikiObserverJob?.cancel()
            fullWikiObserverJob = observeFullWikis(sagaId.toInt())

            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.loadSaga(sagaId)
                val limit = remoteConfigService.getLong("chat_input_limit") ?: 2000L
                stateManager.updateState { it.copy(maxContentLength = limit.toInt()) }
            }

            wikiObserverJob?.cancel()
            wikiObserverJob =
                viewModelScope.launch {
                    wikiUseCase.getWikisWithChapter(sagaId.toInt()).collectLatest { wikis ->
                        stateManager.updateWikiGroups(wikiMapper.buildWikiGroups(wikis))
                    }
                }
        }

        private fun observeMediaState() =
            viewModelScope.launch(Dispatchers.IO) {
                audioMediaPlayerManager.isPlaying.collect { isPlaying ->
                    stateManager.updateState { it.copy(isPlaying = isPlaying) }
                }
            }

        private fun observeProcessingState() =
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.narrativeProcessingUiState.collect { isGenerating ->
                    stateManager.updateGenerating(isGenerating)
                }
            }

        private fun observeNarrativeState() =
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.narrativeUiState.collect { narrativeState ->
                    stateManager.updateNarrativeUiState(narrativeState)
                }
            }

        private fun observeSceneSummary() =
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.sceneSummary.collect { summary ->
                    stateManager.updateState { it.copy(sceneSummary = summary) }
                }
            }

        private fun observeReasoning() =
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.contentReasoning.collect { reasoning ->
                    stateManager.updateState { it.copy(reasoningChunk = reasoning) }
                }
            }

        private fun observeCharacters(sagaId: Int) =
            viewModelScope.launch(Dispatchers.IO) {
                characterUseCase.getCharactersBySaga(sagaId).collectLatest { characters ->
                    stateManager.updateCharacters(characters.map { it.data })
                }
            }

        private fun observeTopCharacters(sagaId: Int) =
            viewModelScope.launch(Dispatchers.IO) {
                characterUseCase.getTopCharacters(sagaId, 3).collectLatest { topCharacters ->
                    stateManager.updateTopCharacters(topCharacters.map { it.data })
                }
            }

        private fun observeFullWikis(sagaId: Int) =
            viewModelScope.launch(Dispatchers.IO) {
                wikiUseCase.getWikisBySaga(sagaId).collectLatest { wikis ->
                    stateManager.updateWikis(wikis)
                }
            }

        fun observeNotificationUpdates() =
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.notificationUpdate.collect { event ->
                    event?.let { notificationEvent ->
                        uiState.value.sagaContent?.let { currentSaga ->
                            notificationManager.sendSnackBarNotification(
                                saga = currentSaga,
                                event = notificationEvent,
                            )
                        }
                    }
                }
            }

        private fun observePreferences() =
            viewModelScope.launch(Dispatchers.IO) {
                settingsUseCase.getSmartSuggestionsEnabled().collect {
                    stateManager.updateState { s -> s.copy(smartSuggestionsEnabled = it) }
                }

                settingsUseCase.getNotificationsEnabled().collect {
                    stateManager.updateState { s -> s.copy(notificationsEnabled = it) }
                }

                settingsUseCase.getMessageEffectsEnabled().collect {
                    stateManager.updateState { s -> s.copy(messageEffectsEnabled = it) }
                }

                settingsUseCase.backupEnabled().collect {
                    if (it.not()) {
                        sagaThemeManager.showSnackBar(
                            message = context.getString(R.string.backup_disabled_notification),
                            action =
                                context.getString(R.string.configure) to {
                                    enableBackup(null)
                                },
                        )
                    }
                }
            }

        fun updateInput(value: TextFieldValue) {
            stateManager.updateInput(value)
        }

        fun updateSendType(type: SenderType) {
            stateManager.updateSenderType(type)
        }

        fun reviewWiki(wikiItems: List<Wiki>) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.reviewWiki(wikiItems)
            }
        }

        fun appendWiki(wiki: Wiki) {
            viewModelScope.launch(Dispatchers.IO) {
                val currentInput = uiState.value.inputValue
                stateManager.updateInput(currentInput.copy(text = currentInput.text + " " + wiki.title))
            }
        }

        fun checkSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.checkNarrativeProgression(uiState.value.sagaContent)
            }
        }

        fun onOnboardingDismissed() {
            sagaContentManager.isOnboardingVisible.value = false
            stateManager.updateOnboardingType(null)
            sagaContentManager.checkNarrativeProgression(uiState.value.sagaContent)
        }

        fun retryAiResponse(message: Message?) {
            val currentSaga = uiState.value.sagaContent ?: return
            generationJob =
                viewModelScope.launch(Dispatchers.IO) {
                    updateLoading(true)
                    message?.let {
                        messageUseCase.updateMessage(message.copy(status = MessageStatus.LOADING))

                        replyMessage(
                            message,
                            currentSaga.getCurrentTimeLine()?.data?.sceneSummary,
                            false,
                        )
                    } ?: run {
                        updateLoading(false)
                    }
                }
        }

        fun requestNewCharacter(
            name: String,
            message: Message,
        ) {
            val saga = uiState.value.sagaContent ?: return
            val mentions =
                saga.flatMessages().filter {
                    it.message.speakerName?.contains(name, true) == true ||
                        it.message.text.contains(name, true)
                }
            createCharacter(
                buildString {
                    appendLine("Character name: $name")
                    appendLine("Character context on story:")
                    appendLine(mentions.takeLast(5).joinToString(";\n") { it.message.text })
                },
                message,
            )
        }

        fun reviewEvent(timelineContent: com.ilustris.sagai.features.home.data.model.TimelineMetadata) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.reviewEvent(timelineContent)
            }
        }

        fun reviewChapter(chapterContent: com.ilustris.sagai.features.home.data.model.ChapterMetadata) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.reviewChapter(chapterContent)
            }
        }

        fun updateCharacter(character: com.ilustris.sagai.features.characters.data.model.Character?) {
            viewModelScope.launch(Dispatchers.IO) {
                stateManager.updateCharacter(character)
            }
        }

        private fun openMessageOptions(message: Message?) {
            if (message != null && (uiState.value.isLoading || uiState.value.isGenerating)) return
            stateManager.showMessageOptions(message)
        }

        private fun editMessage(message: Message) {
            stateManager.setEditingMessage(message)
            stateManager.updateInput(
                TextFieldValue(
                    message.text,
                    selection =
                        TextRange(message.text.length),
                ),
            )
            stateManager.showMessageOptions(null)
        }

        private fun deleteMessage(message: Message) {
            if (uiState.value.isLoading) return
            viewModelScope.launch(Dispatchers.IO) {
                messageUseCase.deleteMessage(message.id.toLong())
                stateManager.showMessageOptions(null)

                val sagaContent = uiState.value.sagaContent ?: return@launch
                val messages = sagaContent.flatMessages()
                val index = messages.indexOfFirst { it.message.id == message.id }

                if (index > 0) {
                    val previousMessage = messages[index - 1]
                    messageUseCase.updateMessage(previousMessage.message.copy(status = MessageStatus.ERROR))
                }

                sagaThemeManager.showSnackBar(
                    context.getString(R.string.message_deleted_regenerate),
                )
            }
        }

        private fun saveEdit() {
            val editingMessage = uiState.value.editingMessage ?: return
            val newText = uiState.value.inputValue.text
            if (newText.isEmpty()) return

            viewModelScope.launch(Dispatchers.IO) {
                val updatedMessage = editingMessage.copy(text = newText)
                messageUseCase.updateMessage(updatedMessage)

                if (updatedMessage.senderType == SenderType.USER) {
                    retryAiResponse(updatedMessage)
                }

                stateManager.setEditingMessage(null)
                stateManager.updateInput(TextFieldValue(""))
            }
        }

        private fun cancelEdit() {
            stateManager.setEditingMessage(null)
            stateManager.updateInput(TextFieldValue(""))
        }

        private fun observeMainCharacter(characterId: Int) {
            viewModelScope.launch(Dispatchers.IO) {
                characterUseCase.getCharacterContent(characterId).collectLatest { character ->
                    stateManager.updateState { it.copy(mainCharacter = character) }
                }
            }
        }

        private fun observeSaga() =
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.content
                    .distinctUntilChanged { old, new ->
                        if (old == null || new == null) return@distinctUntilChanged old == new
                        old.data.copy(playTimeMs = 0) == new.data.copy(playTimeMs = 0) &&
                            old.acts == new.acts &&
                            old.mainCharacter == new.mainCharacter
                    }.collectLatest { sagaContent ->

                        Timber.tag("ChatViewModel").d("observeSaga triggered for genre: ${sagaContent?.data?.genre}")
                        if (sagaContent == null) {
                            if (loadFinished) {
                                updateLoading(false)
                                stateManager.updateState { it.copy(chatState = ChatState.Error("Saga not found.")) }
                            }
                            return@collectLatest
                        }

                        val isFirstLoad = !loadFinished
                        sagaThemeManager.updateTheme(
                            sagaContent.data.genre,
                            playEntryVfx = isFirstLoad,
                        )

                        val rules =
                            remoteConfigService.getJson<NarrativeRules>("narrative_rules") ?: run {
                                Timber.tag(this@ChatViewModel.javaClass.simpleName).e("observeSaga: Couldn't fetch rules")
                                return@collectLatest
                            }

                        val flatMessages = sagaContent.flatMessages()
                        val currentMessageCount = flatMessages.size
                        val messagesChanged = currentMessageCount != lastMessageCount

                        val messages =
                            mapper.mapToActDisplayData(sagaContent, rules)
                        stateManager.updateState {
                            it.copy(
                                sagaContent = sagaContent,
                                messages = messages,
                                chatState = ChatState.Success,
                                isLoading = if (loadFinished) it.isLoading else false,
                                activeGenre = sagaContent.data.genre,
                                flatEvents = sagaContent.flatEvents().map { it.data },
                                characters = sagaContent.characters,
                            )
                        }

                        lastMessageCount = currentMessageCount

                        if (uiState.value.selectedCharacter == null) {
                            sagaContent.mainCharacter?.let { updateCharacter(it) }
                        }

                        // Only validate character updates when messages changed
                        if (messagesChanged) {
                            validateCharacterMessageUpdates(sagaContent)
                            sagaContentManager.checkNarrativeProgression(sagaContent)
                        }

                        updateProgress(sagaContent)

                        loadFinished = true

                        if (messagesChanged) {
                            validateMessageStatus(sagaContent)
                        }

                        if (sagaContent.flatMessages().isEmpty()) {
                            stateManager.updateOnboardingType(OnboardingType.GAMEPLAY_GUIDE)
                            sagaContentManager.isOnboardingVisible.value = true
                        }

                        sagaContent.mainCharacter?.id?.let {
                            observeMainCharacter(it)
                        }
                    }
            }

        private fun validateMessageStatus(sagaContent: SagaMetadata) {
            viewModelScope.launch(Dispatchers.IO) {
                if (uiState.value.isGenerating) return@launch
                if (uiState.value.isLoading) return@launch

                val messages = sagaContent.flatMessages()
                if (messages.isEmpty()) return@launch

                val lastMessage = messages.last()
                val messagesToUpdate = messages.filter { it.message.status != MessageStatus.OK }

                // Early return if no messages need status updates
                if (messagesToUpdate.isEmpty()) return@launch

                messagesToUpdate.forEach { messageContent ->
                    val newStatus =
                        if (messageContent == lastMessage) {
                            MessageStatus.ERROR
                        } else {
                            MessageStatus.OK
                        }

                    // Only update if the status actually needs to change
                    if (messageContent.message.status != newStatus) {
                        messageUseCase.updateMessage(
                            messageContent.message.copy(status = newStatus),
                        )
                    }
                }
            }
        }

        private fun updateProgress(sagaContent: SagaMetadata) {
            viewModelScope.launch {
                val rules =
                    remoteConfigService.getJson<NarrativeRules>("narrative_rules") ?: return@launch
                val progress =
                    when {
                        sagaContent.isComplete(rules) -> {
                            1f
                        }

                        sagaContent.getCurrentTimeLine() != null -> {
                            val updateLimit = rules.loreUpdateLimit ?: 10
                            val messageCount =
                                sagaContent.getCurrentTimeLine()?.messages?.size ?: 0
                            val maxCount = updateLimit
                            messageCount.toFloat() / maxCount.toFloat()
                        }

                        else -> {
                            0f
                        }
                    }
                stateManager.updateState { it.copy(loreUpdateProgress = progress) }
            }
        }

        // Ambient music controlled by SagaThemeManager and MainActivity

        private var startTime: Long = 0L

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            startTime = System.currentTimeMillis()
            scheduledNotificationService.cancelScheduledNotifications()
            Timber.tag("ChatViewModel").d("Lifecycle: onResume. Informing service to resume.")
            context.startService(
                Intent(context, SagaPlaybackService::class.java).apply {
                    action = SagaPlaybackService.ACTION_RESUME
                },
            )
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            context.startService(
                Intent(context, SagaPlaybackService::class.java).apply {
                    action = SagaPlaybackService.ACTION_STOP
                },
            )
            val saga = uiState.value.sagaContent
            if (saga != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    scheduledNotificationService.scheduleNotification(saga.data.id)
                }
            }
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            Timber.tag("ChatViewModel").d("Lifecycle: onPause called. Informing service to pause.")
            context.startService(
                Intent(context, SagaPlaybackService::class.java).apply {
                    action = SagaPlaybackService.ACTION_PAUSE
                },
            )
            isForeground = false
            viewModelScope.launch(Dispatchers.IO) {
                if (startTime != 0L) {
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    val currentSaga = uiState.value.sagaContent
                    if (currentSaga != null && duration > 0) {
                        Timber.tag("ChatViewModel").d("Updating playtime for saga ${currentSaga.data.id}: +${duration}ms")
                        sagaContentManager.updatePlaytime(currentSaga.data.id, duration)
                    }
                    startTime = 0L
                }
                sagaContentManager.backupSaga()
            }
        }

        override fun onCleared() {
            super.onCleared()
            Timber.tag("ChatViewModel").d("onCleared called. Killing music service.")
            context.startService(
                Intent(context, SagaPlaybackService::class.java).apply {
                    action = SagaPlaybackService.ACTION_STOP
                },
            )
            audioMediaPlayerManager.release()
        }

        private suspend fun validateCharacterMessageUpdates(content: SagaMetadata) {
            val updatableMessages =
                content.flatMessages().filter { messageContent ->
                    messageContent.character == null &&
                        messageContent.message.senderType == SenderType.CHARACTER &&
                        messageContent.message.speakerName != null &&
                        content.findCharacter(messageContent.message.speakerName) != null
                }

            updatableMessages.forEach { message ->
                val character = content.findCharacter(message.message.speakerName)
                character?.let {
                    messageUseCase.updateMessage(
                        message.message.copy(
                            characterId = it.id,
                        ),
                    )
                }
            }
        }

        fun sendInput(
            userConfirmed: Boolean = false,
            isAudio: Boolean = false,
        ) {
            if (uiState.value.isSendingPending) {
                sendJob?.cancel()
                sendJob = null
                stateManager.updateSendingPending(false)
                stateManager.updateSendingProgress(0f)
                stateManager.updateLoading(false)
                return
            }

            val text = uiState.value.inputValue
            uiState.value.senderType

            val saga = uiState.value.sagaContent ?: return
            val mainCharacter = uiState.value.selectedCharacter ?: saga.mainCharacter
            if (mainCharacter == null) return
            val currentTimeline = uiState.value.sagaContent?.getCurrentTimeLine()
            if (currentTimeline == null) {
                sagaContentManager.checkNarrativeProgression(uiState.value.sagaContent)
                return
            }

            if (isAudio && !userConfirmed) {
                stateManager.updateAudioInput(true)
                stateManager.updateLoading(false)
                return
            }

            Timber.d("Smart Suggestions status: ${uiState.value.smartSuggestionsEnabled}")

            viewModelScope.launch(Dispatchers.IO) {
                if (userConfirmed || uiState.value.smartSuggestionsEnabled.not()) {
                    startPendingSend(isAudio)
                    return@launch
                }

                if (isAudio) {
                    checkTypo(isAudio)
                    return@launch
                }

                val shouldSkipBalance = isAudio || kotlin.random.Random.nextFloat() < 0.35f

                if (!shouldSkipBalance) {
                    startPendingSend(isAudio)
                    return@launch
                }

                val typoCheck =
                    messageUseCase.checkMessageTypo(
                        saga,
                        text.text,
                    )

                typoCheck.getSuccess()?.let {
                    val shouldDisplay = it.suggestedText != text.text
                    if (shouldDisplay) {
                        stateManager.updateState { s -> s.copy(typoFixMessage = typoCheck.getSuccess()) }
                        when (it.status) {
                            TypoStatus.OK -> {
                                startPendingSend(isAudio)
                            }

                            TypoStatus.FIX -> {
                                delay(5.seconds)
                                if (uiState.value.typoFixMessage != null) {
                                    stateManager.updateInput(
                                        TextFieldValue(
                                            it.suggestedText ?: uiState.value.inputValue.text,
                                        ),
                                    )
                                    startPendingSend(isAudio)
                                }
                            }

                            TypoStatus.ENHANCEMENT -> {
                                doNothing()
                            }
                        }
                    }
                } ?: run {
                    startPendingSend(isAudio)
                }
            }
        }

        private fun startPendingSend(isAudio: Boolean) {
            val text = uiState.value.inputValue.text
            uiState.value.senderType
            val saga = uiState.value.sagaContent ?: return
            val mainCharacter =
                uiState.value.selectedCharacter ?: saga.mainCharacter ?: return
            val currentTimeline = uiState.value.sagaContent?.getCurrentTimeLine() ?: return

            val message =
                Message(
                    text = text,
                    speakerName = mainCharacter.name,
                    senderType = SenderType.USER,
                    characterId = mainCharacter.id,
                    timelineId = currentTimeline.data.id,
                    status = MessageStatus.LOADING,
                )

            sendJob =
                viewModelScope.launch {
                    stateManager.updateSendingPending(true)
                    stateManager.updateLoading(true)
                    val totalSteps = 10
                    val delayStep = messageDelay.inWholeMilliseconds / totalSteps
                    for (i in 1..totalSteps) {
                        stateManager.updateSendingProgress(i.toFloat() / totalSteps)
                        delay(delayStep)
                    }
                    stateManager.updateSendingPending(false)
                    stateManager.updateSendingProgress(0f)
                    val isActuallyAudio = isAudio || uiState.value.isAudioInput
                    stateManager.updateAudioInput(false)
                    generationJob =
                        viewModelScope.launch {
                            sendMessage(message, true, null, isActuallyAudio)
                        }
                }
        }

        private fun checkTypo(isAudio: Boolean = false) {
            val saga = uiState.value.sagaContent ?: return
            val text = uiState.value.inputValue.text
            viewModelScope.launch {
                val typoCheck =
                    messageUseCase.checkMessageTypo(
                        saga,
                        text,
                    )

                typoCheck.getSuccess()?.let {
                    val shouldDisplay = it.status != TypoStatus.OK
                    if (shouldDisplay) {
                        stateManager.updateState { s -> s.copy(typoFixMessage = typoCheck.getSuccess()) }
                    }
                    when (it.status) {
                        TypoStatus.OK, TypoStatus.ENHANCEMENT -> {
                            stateManager.updateInput(
                                uiState.value.inputValue.copy(
                                    text = it.suggestedText ?: uiState.value.inputValue.text,
                                ),
                            )
                            sendInput(userConfirmed = true, isAudio = isAudio)
                        }

                        TypoStatus.FIX -> {
                            stateManager.updateInput(
                                TextFieldValue(it.suggestedText ?: uiState.value.inputValue.text),
                            )
                            delay(5.seconds)
                            if (uiState.value.typoFixMessage != null) {
                                sendInput(userConfirmed = true, isAudio = isAudio)
                            }
                        }
                    }
                }
            }
        }

        private fun sendMessage(
            message: Message,
            isFromUser: Boolean = false,
            sceneSummary: SceneSummary?,
            isAudio: Boolean,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val saga = uiState.value.sagaContent ?: return@launch
                val characterReference = saga.findCharacter(message.speakerName)

                resetSuggestions()
                val characterId =
                    if (message.senderType == SenderType.NARRATOR) {
                        null
                    } else {
                        message.characterId ?: characterReference?.id
                    }

                val speaker =
                    if (message.senderType == SenderType.NARRATOR) {
                        null
                    } else {
                        characterReference?.name ?: message.speakerName
                    }

                messageUseCase
                    .saveMessage(
                        saga,
                        message.copy(
                            characterId = characterId,
                            speakerName = speaker,
                            sagaId = saga.data.id,
                        ),
                        isFromUser,
                        null,
                    ).onSuccessAsync { savedMessage ->
                        resetSuggestions()
                        stateManager.updateInput(TextFieldValue())

                        stateManager.updateLoading(true)
                        viewModelScope.launch(Dispatchers.IO) {
                            val sceneSummaryData =
                                if (isFromUser) {
                                    messageUseCase
                                        .getSceneContext(saga)
                                        .getSuccess()
                                } else {
                                    sceneSummary
                                }

                            if (isFromUser) {
                                generationJob =
                                    viewModelScope.launch(Dispatchers.IO) {
                                        replyMessage(savedMessage, sceneSummaryData, isAudio)
                                    }
                            }

                            messageUseCase.generateExtraContent(
                                saga = saga,
                                message = savedMessage,
                                sceneSummary = sceneSummaryData,
                                characterReference = characterReference,
                                generateAudio = isAudio && isFromUser.not(),
                                isFromUser = isFromUser,
                            )
                        }
                    }.onFailureAsync {
                        sagaThemeManager.showSnackBar(
                            message = "Ocorreu um erro ao salvar a mensagem",
                            action =
                                context.getString(R.string.try_again) to {
                                    retryAiResponse(message)
                                },
                        )
                        stateManager.updateState { s -> s.copy(typoFixMessage = null) }
                        updateLoading(false)
                        stateManager.updateInput(TextFieldValue())
                    }
            }
        }

        private fun resetSuggestions() {
            stateManager.updateState { it.copy(typoFixMessage = null, suggestions = emptyList()) }
        }

        private fun updateLoading(isLoading: Boolean) {
            viewModelScope.launch {
                stateManager.updateLoading(isLoading)
            }
        }

        private fun generateSuggestions(sceneSummary: SceneSummary? = null) {
            viewModelScope.launch(Dispatchers.IO) {
                if (sagaContentManager.isInDebugMode()) {
                    return@launch
                }

                delay(5.seconds)

                Timber.d("generateSuggestions: checking if is generating -> ${uiState.value.isGenerating}")
                if (uiState.value.isGenerating || uiState.value.isLoading) {
                    return@launch
                }

                val currentSaga = sagaContentManager.getSagaContent() ?: return@launch
                if (currentSaga.data.isEnded) return@launch
                val currentTimeline = currentSaga.getCurrentTimeLine() ?: return@launch
                if (currentTimeline.messages.isEmpty()) return@launch
                if (uiState.value.isLoading || uiState.value.isGenerating) return@launch
                suggestionUseCase(
                    currentTimeline.messages,
                    uiState.value.selectedCharacter,
                    currentSaga,
                    sceneSummary,
                ).onSuccess {
                    stateManager.updateState { s -> s.copy(suggestions = it) }
                }
            }
        }

        private suspend fun replyMessage(
            message: Message,
            sceneSummary: SceneSummary?,
            isAudio: Boolean,
        ) {
            val saga = uiState.value.sagaContent ?: return
            val timeline = saga.getCurrentTimeLine()
            if (timeline == null) {
                sagaContentManager.checkNarrativeProgression(saga)
                return
            }

            val newMessage =
                MessageContent(
                    message = message,
                    character = saga.getCharacters().find { it.id == message.characterId },
                    reactions = emptyList(),
                )

            updateLoading(true)
            messageUseCase
                .generateMessage(
                    saga = saga,
                    message = newMessage,
                    sceneSummary = sceneSummary,
                ).collect { streamingState ->
                    when (streamingState) {
                        is StreamingState.Reasoning -> {
                            stateManager.updateState { it.copy(reasoningChunk = streamingState.chunk) }
                        }

                        is StreamingState.Success -> {
                            stateManager.updateState { it.copy(reasoningChunk = null) }
                            messageUseCase.updateMessage(newMessage.message.copy(status = MessageStatus.OK))
                            val generatedMessage = streamingState.data.message
                            streamingState.data.newCharacter?.let { discovery ->
                                if (generatedMessage.senderType == SenderType.NARRATOR) return@let

                                sagaContentManager
                                    .generateCharacter(
                                        discovery.toGenerationDescription(
                                            message,
                                            generatedMessage,
                                        ),
                                        sceneSummary = sceneSummary,
                                        candidateName = discovery.name,
                                    ).onFailureAsync {
                                        sagaThemeManager.showSnackBar(
                                            message = "Ocorreu um erro ao criar o personagem",
                                            action =
                                                context.getString(R.string.try_again) to {
                                                    requestNewCharacter(
                                                        discovery.name,
                                                        generatedMessage,
                                                    )
                                                },
                                        )
                                    }
                            }

                            messageUseCase.updateMessage(
                                message.copy(
                                    status = MessageStatus.OK,
                                ),
                            )

                            viewModelScope.launch(Dispatchers.IO) {
                                sceneSummary?.let {
                                    generateSuggestions(it)
                                    sagaContentManager.updateSummary(it)
                                }
                                updateLoading(false)
                                sagaContentManager.checkNarrativeProgression(
                                    uiState.value.sagaContent,
                                )
                            }
                        }

                        is StreamingState.Error -> {
                            stateManager.updateState { it.copy(reasoningChunk = null) }

                            updateLoading(false)
                            sagaContentManager.setProcessing(false)
                            if (streamingState.throwable is GuardrailsException) {
                                Timber
                                    .tag("ChatViewModel")
                                    .w("Guardrail block detected. Deleting message and restoring input.")
                                messageUseCase.deleteMessage(message.id.toLong())
                                stateManager.updateInput(
                                    TextFieldValue(
                                        text = message.text,
                                        selection = TextRange(message.text.length),
                                    ),
                                )
                            } else {
                                sagaThemeManager.showSnackBar(
                                    message = context.getString(R.string.message_reply_error),
                                    action =
                                        context.getString(R.string.try_again) to {
                                            retryAiResponse(message)
                                        },
                                )
                                messageUseCase.updateMessage(
                                    message.copy(
                                        status = MessageStatus.ERROR,
                                    ),
                                )
                            }
                            stateManager.updateLoading(false)
                        }
                    }
                }
        }

        fun createCharacter(
            contextDescription: String,
            message: Message,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager
                    .generateCharacter(
                        contextDescription,
                        candidateName = message.speakerName,
                    ).onFailureAsync {
                        sagaThemeManager.showSnackBar(
                            message = "Ocorreu um erro ao criar o personagem",
                            action =
                                context.getString(R.string.try_again) to {
                                    requestNewCharacter(
                                        message.speakerName ?: "",
                                        message,
                                    )
                                },
                        )
                    }.onSuccessAsync {
                        messageUseCase.updateMessage(
                            message.copy(
                                characterId = it.id,
                                speakerName = it.name,
                            ),
                        )
                    }
            }
        }

        private fun enableDebugMode(enabled: Boolean) {
            sagaContentManager.setDebugMode(enabled)
            messageUseCase.setDebugMode(enabled)
            Timber.tag("ChatViewModel").i("Debug mode set to: $enabled")
        }

        fun sendFakeUserMessages(count: Int) {
            val currentSaga = uiState.value.sagaContent ?: return
            val mainCharacterId = currentSaga.mainCharacter?.id ?: return
            val timeline = currentSaga.getCurrentTimeLine() ?: return

            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.setProcessing(true)
                repeat(count) {
                    if (it == count - 1) {
                        sagaContentManager.setProcessing(false)
                    }
                    val fakeUserMessage =
                        Message(
                            text = "Fake Message #{it + 1} of $count",
                            senderType = SenderType.USER,
                            characterId = mainCharacterId,
                            sagaId = currentSaga.data.id,
                            timelineId = timeline.data.id,
                        )
                    sendMessage(fakeUserMessage, false, null, false)
                    delay(100)
                }
                Timber.tag("ChatViewModel").d("Finished enqueuing $count fake messages.")
            }
        }

        fun enableBackup(uri: Uri?) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.enableBackup(uri)
            }
        }

        fun segmentSagaCover(url: String) {
            viewModelScope.launch(Dispatchers.IO) {
                val cachedBitmap = segmentedImageCache.get(url)
                if (cachedBitmap != null) {
                    stateManager.updateState { it.copy(segmentedBitmap = cachedBitmap) }
                    return@launch
                }

                imageSegmentationHelper.processImage(url).onSuccessAsync {
                    stateManager.updateState { s ->
                        s.copy(
                            originalBitmap = it.first,
                            segmentedBitmap = it.second,
                        )
                    }
                }
            }
        }

        fun dismissCharacter() {
            stateManager.updateState { it.copy(revealCharacter = null) }
        }

        fun dismissNewCharacterReveal() {
            stateManager.updateState { it.copy(newCharacterReveal = null) }
        }

        fun getSelectedMessages(): List<MessageContent> {
            val saga = uiState.value.sagaContent ?: return emptyList()
            val selectedIds = uiState.value.selectionState.selectedMessageIds
            return saga.flatMessages().filter { it.message.id in selectedIds }
        }

        // Audio Playback Methods

        fun playOrPauseAudio(messageContent: MessageContent) {
            val message = messageContent.message
            val audioPath = message.audioPath ?: return

            viewModelScope.launch(Dispatchers.Main) {
                val currentState = uiState.value.audioPlaybackState

                // If playing the same message, toggle play/pause
                if (currentState?.messageId == message.id) {
                    if (currentState.isPlaying) {
                        pauseAudio()
                    } else {
                        resumeAudio()
                    }
                    return@launch
                }

                stopAudio()
                startAudio(message.id, audioPath)
            }
        }

        private fun startAudio(
            messageId: Int,
            audioPath: String,
        ) {
            audioMediaPlayerManager.prepareDataSource(
                path = audioPath,
                looping = false,
                onPrepared = {
                    val duration =
                        try {
                            audioMediaPlayerManager.mediaPlayer?.duration?.toLong() ?: 0L
                        } catch (e: Exception) {
                            0L
                        }

                    viewModelScope.launch(Dispatchers.Main) {
                        stateManager.updateState {
                            it.copy(
                                audioPlaybackState =
                                    AudioPlaybackState(
                                        messageId = messageId,
                                        isPlaying = true,
                                        currentPosition = 0L,
                                        duration = duration,
                                        audioPath = audioPath,
                                    ),
                            )
                        }
                    }

                    audioMediaPlayerManager.play()
                    startProgressUpdates(messageId)
                },
                onError = { exception ->
                    Timber.tag("ChatViewModel").e(exception, "Audio playback error")
                    viewModelScope.launch(Dispatchers.Main) {
                        stateManager.updateState { it.copy(audioPlaybackState = null) }
                    }
                },
                onCompletion = {
                    viewModelScope.launch(Dispatchers.Main) {
                        stateManager.updateState { s ->
                            s.copy(
                                audioPlaybackState =
                                    s.audioPlaybackState?.copy(
                                        isPlaying = false,
                                        currentPosition = s.audioPlaybackState.duration,
                                    ),
                            )
                        }
                    }
                    stopProgressUpdates()
                },
            )
        }

        private fun pauseAudio() {
            audioMediaPlayerManager.pause()
            viewModelScope.launch(Dispatchers.Main) {
                stateManager.updateState { s ->
                    s.copy(audioPlaybackState = s.audioPlaybackState?.copy(isPlaying = false))
                }
            }
            stopProgressUpdates()
        }

        private fun resumeAudio() {
            audioMediaPlayerManager.play()
            viewModelScope.launch(Dispatchers.Main) {
                stateManager.updateState { s ->
                    s.copy(audioPlaybackState = s.audioPlaybackState?.copy(isPlaying = true))
                }
            }
            uiState.value.audioPlaybackState
                ?.messageId
                ?.let { startProgressUpdates(it) }
        }

        fun stopAudio() {
            audioMediaPlayerManager.stop()
            stopProgressUpdates()
            viewModelScope.launch(Dispatchers.Main) {
                stateManager.updateState { it.copy(audioPlaybackState = null) }
            }
        }

        private fun startProgressUpdates(messageId: Int) {
            stopProgressUpdates()
            audioProgressJob =
                viewModelScope.launch(Dispatchers.Main) {
                    while (uiState.value.audioPlaybackState?.isPlaying == true &&
                        uiState.value.audioPlaybackState?.messageId == messageId
                    ) {
                        val currentPosition =
                            try {
                                audioMediaPlayerManager.mediaPlayer?.currentPosition?.toLong() ?: 0L
                            } catch (e: Exception) {
                                0L
                            }

                        stateManager.updateState { s ->
                            s.copy(audioPlaybackState = s.audioPlaybackState?.copy(currentPosition = currentPosition))
                        }
                        delay(100) // Update every 100ms
                    }
                }
        }

        private fun stopProgressUpdates() {
            audioProgressJob?.cancel()
            audioProgressJob = null
        }

        fun regenerateAudio(message: MessageContent) {
            val saga = uiState.value.sagaContent ?: return
            viewModelScope.launch(Dispatchers.IO) {
                updateLoading(true)
                val characterReference = saga.findCharacter(message.message.speakerName)
                messageUseCase
                    .generateAudio(
                        saga,
                        message.message,
                        characterReference,
                    ).onFailureAsync {
                        messageUseCase.updateMessage(message.message.copy(audible = true))
                    }
                updateLoading(false)
            }
        }
    }
