package com.ilustris.sagai.features.saga.chat.presentation

import MessageStatus
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.LruCache
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.media.MediaPlayerManager
import com.ilustris.sagai.core.media.MediaPlayerManagerImpl
import com.ilustris.sagai.core.media.SagaMediaService
import com.ilustris.sagai.core.media.model.PlaybackMetadata
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.notifications.ScheduledNotificationService
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.saga.chat.data.manager.ChatNotificationManager
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.data.mapper.SagaContentUIMapper
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoStatus
import com.ilustris.sagai.features.saga.chat.data.usecase.GetInputSuggestionsUseCase
import com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.saga.chat.ui.components.audio.AudioPlaybackState
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.components.SnackBarState
import com.ilustris.sagai.ui.components.snackBar
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
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
        private val visualConfigService: GenreVisualConfigService,
        private val remoteConfigService: RemoteConfigService,
    ) : ViewModel(),
        DefaultLifecycleObserver {
        private val stateManager = ChatStateManager()
        val uiState = stateManager.uiState

        val segmentedImageCache = LruCache<String, Bitmap?>(5 * 1024 * 1024) // 5MB cache
        private var audioProgressJob: kotlinx.coroutines.Job? = null
        private val audioMediaPlayerManager: MediaPlayerManager = MediaPlayerManagerImpl(context)
        var isForeground = true
        private val messageDelay = 7.seconds
        private var sendJob: kotlinx.coroutines.Job? = null

        private var lastActId: Int = 0
        private var lastChapterId: Int = 0
        private var lastEventId: Int = 0
        private var lastMessageCount: Int = 0

        private var sagaObserverJob: kotlinx.coroutines.Job? = null
        private var milestoneObserverJob: kotlinx.coroutines.Job? = null
        private var musicObserverJob: kotlinx.coroutines.Job? = null
        private var preferencesObserverJob: kotlinx.coroutines.Job? = null
        private var snackBarObserverJob: kotlinx.coroutines.Job? = null
        private var mediaObserverJob: kotlinx.coroutines.Job? = null
        private var processingObserverJob: kotlinx.coroutines.Job? = null
        private var sceneSummaryObserverJob: kotlinx.coroutines.Job? = null

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
                    requestNewCharacter(action.name)
                }

                is ChatUiAction.ReviewEvent -> {
                    reviewEvent(action.timelineContent)
                }

                is ChatUiAction.ReviewChapter -> {
                    reviewChapter(action.chapterContent)
                }

                is ChatUiAction.UpdateCharacter -> {
                    updateCharacter(action.characterContent)
                }

                is ChatUiAction.ShowCharacter -> {
                    revealCharacter(action.characterContent)
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

                is ChatUiAction.DismissSnackBar -> {
                    dismissSnackBar()
                }

                is ChatUiAction.EnableBackup -> {
                    enableBackup(action.uri)
                }

                is ChatUiAction.DismissCharacterReveal -> {
                    dismissCharacter()
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
            }
        }

        private fun revealCharacter(characterContent: CharacterContent?) {
            stateManager.updateState {
                it.copy(revealCharacter = characterContent)
            }
        }

        private fun observeMileStone() =
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.milestoneUpdate.collect {
                    stateManager.updateMilestone(it)
                }
            }

        private var loadFinished = false
        private var currentSagaIdForService: String? = null

        private var currentActCountForService: Int = 0

        private fun updateSnackBar(snackBarState: SnackBarState) {
            viewModelScope.launch(Dispatchers.IO) {
                stateManager.updateSnackBar(snackBarState)
                delay(10.seconds)
                stateManager.updateSnackBar(null)
                stateManager.updateLoading(false)
            }
        }

        fun initChat(
            sagaId: String?,
            isDebug: Boolean = false,
        ) {
            if (sagaId == null) {
                return
            }
            stateManager.updateLoading(true)
            notificationManager.clearNotifications()
            currentSagaIdForService = sagaId
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

            musicObserverJob?.cancel()
            musicObserverJob = observeAmbientMusicServiceControl()

            preferencesObserverJob?.cancel()
            preferencesObserverJob = observePreferences()

            snackBarObserverJob?.cancel()
            snackBarObserverJob = observeSnackBarUpdates()

            mediaObserverJob?.cancel()
            mediaObserverJob = observeMediaState()

            processingObserverJob?.cancel()
            processingObserverJob = observeProcessingState()

            sceneSummaryObserverJob?.cancel()
            sceneSummaryObserverJob = observeSceneSummary()

            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.loadSaga(sagaId)
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

        private fun observeSceneSummary() =
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.sceneSummary.collect { summary ->
                    stateManager.updateState { it.copy(sceneSummary = summary) }
                }
            }

        fun observeSnackBarUpdates() =
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.snackBarUpdate.collect { state ->
                    state?.let { snackBarState ->
                        if (snackBarState.showInUi) {
                            updateSnackBar(snackBarState)
                        }

                        uiState.value.sagaContent?.let { currentSaga ->
                            notificationManager.sendNotification(
                                saga = currentSaga.data,
                                title = currentSaga.data.title,
                                content = snackBarState.message,
                                smallIcon = snackBarState.icon,
                                largeIcon = null,
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
                        updateSnackBar(
                            snackBarState =
                                snackBar(context.getString(R.string.backup_disabled_notification)) {
                                    action {
                                        configureBackup()
                                    }
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

        fun retryAiResponse(message: Message?) {
            val currentSaga = uiState.value.sagaContent ?: return
            viewModelScope.launch(Dispatchers.IO) {
                updateLoading(true)
                message?.let {
                    messageUseCase.updateMessage(message.copy(status = MessageStatus.LOADING))

                    val summary = messageUseCase.getSceneContext(currentSaga).getSuccess()
                    replyMessage(message, summary, false)
                } ?: run {
                    updateLoading(false)
                }
            }
        }

        fun requestNewCharacter(name: String) {
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
            )
        }

        fun reviewEvent(timelineContent: TimelineContent) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.reviewEvent(timelineContent)
            }
        }

        fun reviewChapter(chapterContent: ChapterContent) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.reviewChapter(chapterContent)
            }
        }

        fun updateCharacter(characterContent: CharacterContent?) {
            viewModelScope.launch(Dispatchers.IO) {
                stateManager.updateCharacter(characterContent)
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
                        androidx.compose.ui.text
                            .TextRange(message.text.length),
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

                updateSnackBar(
                    snackBar(context.getString(R.string.message_deleted_regenerate)) {
                        action {
                            // No specific action needed for now, as the previous message is now in error state
                        }
                    },
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

        private fun observeSaga() =
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.content
                    .distinctUntilChanged { old, new ->
                        if (old == null || new == null) return@distinctUntilChanged old == new
                        // Only ignore updates if ONLY playtime changed
                        old.data.copy(playTimeMs = 0) == new.data.copy(playTimeMs = 0) &&
                            old.acts == new.acts &&
                            old.characters == new.characters &&
                            old.mainCharacter == new.mainCharacter &&
                            old.wikis == new.wikis &&
                            old.relationships == new.relationships
                    }.collectLatest { sagaContent ->
                        if (sagaContent != null && sagaContent.data.id.toString() != currentSagaIdForService) {
                            Log.w(
                                "ChatViewModel",
                                "observeSaga: Ignored stale saga content (${sagaContent.data.id} != $currentSagaIdForService)",
                            )
                            return@collectLatest
                        }
                        Log.d(
                            "ChatViewModel",
                            "observeSaga triggered for genre: ${sagaContent?.data?.genre}",
                        )
                        if (sagaContent == null) {
                            if (loadFinished) {
                                updateLoading(false)
                                stateManager.updateState { it.copy(chatState = ChatState.Error("Saga not found.")) }
                            }
                            return@collectLatest
                        }

                        // Fetch visual config for current genre
                        val visualConfig =
                            visualConfigService.getVisualConfig(sagaContent.data.genre)
                        Log.d("ChatViewModel", "Fetched visual config: $visualConfig")
                        stateManager.updateVisualConfig(visualConfig)

                        val rules =
                            remoteConfigService.getJson<NarrativeRules>("narrative_rules") ?: run {
                                Log.e(
                                    this@ChatViewModel.javaClass.simpleName,
                                    "observeSaga: Couldn't fetch rules",
                                )
                                return@collectLatest
                            }

                        // Cache flatMessages to avoid multiple traversals
                        val flatMessages = sagaContent.flatMessages()
                        val currentMessageCount = flatMessages.size
                        val messagesChanged = currentMessageCount != lastMessageCount

                        val messages =
                            SagaContentUIMapper.mapToActDisplayData(sagaContent.acts, rules)

                        // Only re-sort characters when message count changes (expensive operation)
                        val characters =
                            if (messagesChanged || uiState.value.characters.isEmpty()) {
                                sortCharactersByMessageCount(
                                    sagaContent.getCharacters(),
                                    flatMessages,
                                )
                            } else {
                                uiState.value.characters
                            }

                        stateManager.updateState {
                            it.copy(
                                sagaContent = sagaContent,
                                messages = messages,
                                characters = characters,
                                chatState = ChatState.Success,
                                isLoading = false,
                            )
                        }

                        lastMessageCount = currentMessageCount

                        if (uiState.value.selectedCharacter == null) {
                            sagaContent.mainCharacter?.let { updateCharacter(it) }
                        }

                        checkIfUpdatesService(sagaContent)

                        // Only validate character updates when messages changed
                        if (messagesChanged) {
                            validateCharacterMessageUpdates(sagaContent)
                        }

                        updateProgress(sagaContent)

                        loadFinished = true

                        // Only validate message status when messages changed
                        if (messagesChanged) {
                            validateMessageStatus(sagaContent)
                        }
                    }
            }

        private fun validateMessageStatus(sagaContent: SagaContent) {
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

        private fun updateProgress(sagaContent: SagaContent) {
            viewModelScope.launch {
                val rules =
                    remoteConfigService.getJson<NarrativeRules>("narrative_rules") ?: return@launch
                val progress =
                    when {
                        sagaContent.isComplete(rules) -> {
                            1f
                        }

                        sagaContent.getCurrentTimeLine() != null -> {
                            val updateLimit = rules?.loreUpdateLimit ?: 10
                            val messageCount = sagaContent.getCurrentTimeLine()?.messages?.size ?: 0
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

        private fun checkIfUpdatesService(sagaContent: SagaContent) {
            val newActCount = sagaContent.acts.size
            if (newActCount != currentActCountForService && currentSagaIdForService != null) {
                currentActCountForService = newActCount
                sagaContentManager.ambientMusicFile.value?.let { musicFile ->
                    if (musicFile.exists()) {
                        val playbackMetadata =
                            PlaybackMetadata(
                                sagaId = sagaContent.data.id,
                                sagaTitle = sagaContent.data.title,
                                sagaIcon = sagaContent.data.icon,
                                color =
                                    sagaContent.data.genre
                                        .resolveColor(null)
                                        .toArgb(),
                                currentActNumber = newActCount.coerceAtLeast(1),
                                currentChapter =
                                    sagaContent.getCurrentTimeLine()?.let { timeline ->
                                        sagaContent.chapterNumber(
                                            sagaContent
                                                .flatChapters()
                                                .find { it.data.id == timeline.data.chapterId }
                                                ?.data,
                                        )
                                    } ?: 0,
                                totalActs = sagaContent.acts.size,
                                timelineObjective =
                                    sagaContent.getCurrentTimeLine()?.data?.currentObjective
                                        ?: "Unknown Objective",
                                mediaFilePath = musicFile.absolutePath,
                                genre = sagaContent.data.genre,
                            )
                        controlMediaPlayerService(SagaMediaService.ACTION_PLAY, playbackMetadata)
                        Log.d("ChatViewModel", "New act ($newActCount). Updated SagaMediaService.")
                    }
                }
            }
        }

        private fun controlMediaPlayerService(
            action: String,
            metadata: PlaybackMetadata? = null,
        ) {
            if (action != SagaMediaService.ACTION_PLAY && action != SagaMediaService.ACTION_STOP &&
                action != SagaMediaService.ACTION_PAUSE_MUSIC &&
                action != SagaMediaService.ACTION_RESUME_MUSIC
            ) {
                Log.w("ChatViewModel", "Invalid action for SagaMediaService: $action")
                return
            }
            val serviceIntent =
                Intent(context, SagaMediaService::class.java).apply {
                    this.action = action
                    metadata?.let {
                        putExtra(SagaMediaService.EXTRA_SAGA_CONTENT_JSON, it.toJsonFormat())
                    }
                }
            try {
                context.startService(serviceIntent)
                Log.d(
                    "ChatViewModel",
                    "Sent $action to SagaMediaService. Metadata: ${metadata?.toJsonFormat()}",
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error controlling SagaMediaService with action $action", e)
                updateSnackBar(
                    snackBar(
                        "Não foi possível iniciar o serviço áudio. Verifique as permissões",
                    ),
                )
            }
        }

        private fun observeAmbientMusicServiceControl() =
            viewModelScope.launch(Dispatchers.IO) {
                if (uiState.value.sagaContent == null) return@launch
                sagaContentManager.ambientMusicFile.collect { musicFile ->

                    Log.i(
                        javaClass.simpleName,
                        "observeAmbientMusicServiceControl: file updated -> $musicFile",
                    )
                    if (musicFile == null && uiState.value.isPlaying.not()) {
                        Log.w(
                            javaClass.simpleName,
                            "observeAmbientMusicServiceControl: Music not found skipping player",
                        )
                        return@collect
                    }

                    val currentSagaData =
                        uiState.value.sagaContent?.data // Use data from current saga content

                    if (currentSagaData != null && musicFile != null && musicFile.exists()) {
                        if (currentSagaIdForService == currentSagaData.id.toString()) {
                            val playbackMetadata =
                                PlaybackMetadata(
                                    sagaId = currentSagaData.id,
                                    sagaTitle = currentSagaData.title,
                                    sagaIcon = currentSagaData.icon,
                                    currentActNumber =
                                        uiState.value.sagaContent
                                            ?.acts
                                            ?.size
                                            ?.coerceAtLeast(1) ?: 1,
                                    currentChapter =
                                        uiState.value.sagaContent
                                            ?.getCurrentTimeLine()
                                            ?.let { timeline ->
                                                uiState.value.sagaContent?.chapterNumber(
                                                    uiState.value.sagaContent
                                                        ?.flatChapters()
                                                        ?.find { it.data.id == timeline.data.chapterId }
                                                        ?.data,
                                                )
                                            } ?: 0,
                                    totalActs =
                                        uiState.value.sagaContent
                                            ?.acts
                                            ?.size ?: 1,
                                    timelineObjective =
                                        uiState.value.sagaContent
                                            ?.getCurrentTimeLine()
                                            ?.data
                                            ?.currentObjective ?: "Unknown Objective",
                                    mediaFilePath = musicFile.absolutePath,
                                    color = currentSagaData.genre.resolveColor(null).toArgb(),
                                    genre = currentSagaData.genre,
                                )
                            currentActCountForService = playbackMetadata.currentActNumber
                            controlMediaPlayerService(SagaMediaService.ACTION_PLAY, playbackMetadata)
                            Log.d(
                                "ChatViewModel",
                                "Ambient music file available. Instructing SagaMediaService to play.",
                            )
                        }
                    } else {
                        if (currentSagaIdForService != null && (musicFile == null || !musicFile.exists())) {
                            Log.d(
                                "ChatViewModel",
                                "Ambient music file null/invalid for current saga. Instructing service to stop.",
                            )
                            controlMediaPlayerService(SagaMediaService.ACTION_STOP)
                        }
                    }
                }
            }

        private var startTime: Long = 0L

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            startTime = System.currentTimeMillis()
            scheduledNotificationService.cancelScheduledNotifications()
            Log.d(
                "ChatViewModel",
                "Lifecycle: onResume. SagaMediaService manages its own state. Start time: $startTime",
            )
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            val saga = uiState.value.sagaContent
            if (saga != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    scheduledNotificationService.scheduleNotification(saga.data.id)
                }
            }
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            Log.d("ChatViewModel", "Lifecycle: onPause called. Music continues via service if playing.")
            isForeground = false
            viewModelScope.launch(Dispatchers.IO) {
                if (startTime != 0L) {
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    val currentSaga = uiState.value.sagaContent
                    if (currentSaga != null && duration > 0) {
                        Log.d(
                            "ChatViewModel",
                            "Updating playtime for saga ${currentSaga.data.id}: +${duration}ms",
                        )
                        sagaContentManager.updatePlaytime(currentSaga.data.id, duration)
                    }
                    startTime = 0L
                }
                sagaContentManager.backupSaga()
            }
        }

        override fun onCleared() {
            super.onCleared()
            Log.d("ChatViewModel", "onCleared called. Instructing SagaMediaService to stop.")
            controlMediaPlayerService(SagaMediaService.ACTION_STOP)
            audioMediaPlayerManager.release()
        }

        private suspend fun validateCharacterMessageUpdates(content: SagaContent) {
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
                            characterId = it.data.id,
                        ),
                    )
                }
            }
        }

        fun dismissSnackBar() {
            stateManager.updateSnackBar(null)
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
            val mainCharacter = uiState.value.selectedCharacter?.data ?: saga.mainCharacter?.data
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

            Log.d(
                javaClass.simpleName,
                "Smart Suggestions status: ${uiState.value.smartSuggestionsEnabled}",
            )

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
                uiState.value.selectedCharacter?.data ?: saga.mainCharacter?.data ?: return
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
                    val totalSteps = 70
                    val delayStep = messageDelay.inWholeMilliseconds / totalSteps
                    for (i in 1..totalSteps) {
                        stateManager.updateSendingProgress(i.toFloat() / totalSteps)
                        delay(delayStep)
                    }
                    stateManager.updateSendingPending(false)
                    stateManager.updateSendingProgress(0f)
                    val isActuallyAudio = isAudio || uiState.value.isAudioInput
                    stateManager.updateAudioInput(false)
                    sendMessage(message, true, null, isActuallyAudio)
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
                        message.characterId ?: characterReference?.data?.id
                    }

                val speaker =
                    if (message.senderType == SenderType.NARRATOR) {
                        null
                    } else {
                        characterReference?.data?.name ?: message.speakerName
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
                                replyMessage(savedMessage, sceneSummaryData, isAudio)
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
                        updateSnackBar(
                            snackBar(
                                "Ocorreu um erro ao salvar a mensagem",
                            ) {
                                action {
                                    resendMessage(message)
                                }
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
                sagaContentManager.setProcessing(isLoading)
            }
        }

        private fun generateSuggestions(sceneSummary: SceneSummary? = null) {
            viewModelScope.launch(Dispatchers.IO) {
                if (sagaContentManager.isInDebugMode()) {
                    return@launch
                }

                delay(5.seconds)

                Log.d(
                    javaClass.simpleName,
                    "generateSuggestions: checking if is generating -> ${uiState.value.isGenerating}",
                )
                if (uiState.value.isGenerating || uiState.value.isLoading) {
                    return@launch
                }

                val currentSaga = uiState.value.sagaContent ?: return@launch
                if (currentSaga.data.isEnded) return@launch
                val currentTimeline = currentSaga.getCurrentTimeLine() ?: return@launch
                if (currentTimeline.messages.isEmpty()) return@launch
                if (uiState.value.isLoading || uiState.value.isGenerating) return@launch
                suggestionUseCase(
                    currentTimeline.messages,
                    currentSaga.mainCharacter?.data,
                    currentSaga,
                    sceneSummary,
                ).onSuccess {
                    stateManager.updateState { s -> s.copy(suggestions = it) }
                }
            }
        }

        private fun replyMessage(
            message: Message,
            sceneSummary: SceneSummary?,
            isAudio: Boolean,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val saga = uiState.value.sagaContent ?: return@launch
                val timeline = saga.getCurrentTimeLine()
                if (timeline == null) {
                    sagaContentManager.checkNarrativeProgression(saga)
                    return@launch
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
                    ).onSuccessAsync { generatedMessage ->

                        val speakerName = generatedMessage.speakerName
                        val characterExists =
                            saga.findCharacter(speakerName ?: generatedMessage.speakerName) != null

                        val newCharacter =
                            if (speakerName != null &&
                                !characterExists &&
                                generatedMessage.senderType != SenderType.NARRATOR
                            ) {
                                val contextDescription =
                                    buildString {
                                        appendLine("Character name: $speakerName")
                                        appendLine("Character context on story:")
                                        appendLine("The user said: ${message.text}")
                                        appendLine("And the new character replied: ${generatedMessage.text}")
                                    }
                                val character =
                                    sagaContentManager
                                        .generateCharacter(
                                            contextDescription,
                                        )
                                character.onFailureAsync {
                                    updateSnackBar(
                                        snackBar(
                                            message = "Ocorreu um erro ao criar o personagem",
                                        ) {
                                            action {
                                                retryCharacter(contextDescription)
                                            }
                                        },
                                    )
                                }

                                character.getSuccess()
                            } else {
                                null
                            }

                        sendMessage(
                            message =
                                generatedMessage.copy(
                                    timelineId = timeline.data.id,
                                    id = 0,
                                    status = MessageStatus.OK,
                                    audible = isAudio,
                                    speakerName = newCharacter?.name ?: speakerName,
                                    characterId = null,
                                ),
                            isFromUser = false,
                            sceneSummary = sceneSummary,
                            isAudio = isAudio,
                        )

                        if (newMessage.message.status != MessageStatus.OK) {
                            messageUseCase.updateMessage(
                                message.copy(
                                    status = MessageStatus.OK,
                                ),
                            )
                        }
                        updateLoading(false)
                        sceneSummary?.let {
                            launch(Dispatchers.IO) {
                                generateSuggestions(sceneSummary)
                                sagaContentManager.getCurrentObjective(sceneSummary)
                            }
                        }
                    }.onFailureAsync {
                        messageUseCase.updateMessage(
                            message.copy(
                                status = MessageStatus.ERROR,
                            ),
                        )
                        updateLoading(false)
                        sagaContentManager.setProcessing(false)
                        stateManager.updateLoading(false)
                        updateSnackBar(
                            snackBar(
                                context.getString(R.string.message_reply_error),
                            ) {
                                action {
                                    resendMessage(message)
                                }
                            },
                        )
                    }
            }
        }

        fun createCharacter(contextDescription: String) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager
                    .generateCharacter(
                        contextDescription,
                    ).onFailureAsync {
                        updateSnackBar(
                            snackBar(
                                message = "Ocorreu um erro ao criar o personagem",
                            ) {
                                action {
                                    retryCharacter(contextDescription)
                                }
                            },
                        )
                    }
            }
        }

        private fun enableDebugMode(enabled: Boolean) {
            sagaContentManager.setDebugMode(enabled)
            messageUseCase.setDebugMode(enabled)
            Log.i("ChatViewModel", "Debug mode set to: $enabled")
        }

        fun sendFakeUserMessages(count: Int) {
            val currentSaga = uiState.value.sagaContent ?: return
            val mainCharacterId = currentSaga.mainCharacter?.data?.id ?: return
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
                Log.d("ChatViewModel", "Finished enqueuing $count fake messages.")
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
            controlMediaPlayerService(SagaMediaService.ACTION_PAUSE_MUSIC)
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
                    Log.e("ChatViewModel", "Audio playback error", exception)
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
                    controlMediaPlayerService(SagaMediaService.ACTION_RESUME_MUSIC)
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
            controlMediaPlayerService(SagaMediaService.ACTION_RESUME_MUSIC)
        }

        private fun resumeAudio() {
            controlMediaPlayerService(SagaMediaService.ACTION_PAUSE_MUSIC)
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
            controlMediaPlayerService(SagaMediaService.ACTION_RESUME_MUSIC)
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
