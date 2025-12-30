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
import com.ilustris.sagai.core.media.MediaPlayerManager
import com.ilustris.sagai.core.media.MediaPlayerManagerImpl
import com.ilustris.sagai.core.media.SagaMediaService
import com.ilustris.sagai.core.media.model.PlaybackMetadata
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.notifications.ScheduledNotificationService
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.formatToString
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
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

                is ChatUiAction.Back -> { /* UI action */ }

                is ChatUiAction.OpenSagaDetails -> { /* UI action */ }

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
            }
        }

        private fun revealCharacter(characterContent: CharacterContent?) {
            stateManager.updateState {
                it.copy(revealCharacter = characterContent)
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
            notificationManager.clearNotifications()
            currentSagaIdForService = sagaId
            stateManager.updateState { it.copy(chatState = ChatState.Loading) }
            enableDebugMode(isDebug)
            observeSaga()
            observeAmbientMusicServiceControl()
            observePreferences()
            observeSnackBarUpdates()
            observeMediaState()
            observeProcessingState()
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.loadSaga(sagaId)
            }
        }

        private fun observeMediaState() {
            viewModelScope.launch(Dispatchers.IO) {
                audioMediaPlayerManager.isPlaying.collect { isPlaying ->
                    stateManager.updateState { it.copy(isPlaying = isPlaying) }
                }
            }
        }

        private fun observeProcessingState() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.narrativeProcessingUiState.collect { isGenerating ->
                    stateManager.updateGenerating(isGenerating)
                }
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

        private fun observePreferences() {
            viewModelScope.launch(Dispatchers.IO) {
                settingsUseCase.getSmartSuggestionsEnabled().collect {
                    stateManager.updateState { s -> s.copy(smartSuggestionsEnabled = it) }
                }

                settingsUseCase.getNotificationsEnabled().collect {
                    stateManager.updateState { s -> s.copy(notificationsEnabled = it) }
                }

                settingsUseCase.getMessageEffectsEnabled().collect {
                    stateManager.updateGenerating(it)
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
            message?.let {
                replyMessage(message, null, false)
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

        private fun observeSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.content
                    .collectLatest { sagaContent ->
                        if (sagaContent == null) {
                            if (loadFinished) {
                                stateManager.updateState { it.copy(chatState = ChatState.Error("Saga not found.")) }
                            }
                            return@collectLatest
                        }

                        val messages = SagaContentUIMapper.mapToActDisplayData(sagaContent.acts)
                        val characters =
                            sortCharactersByMessageCount(
                                sagaContent.getCharacters(),
                                sagaContent.flatMessages(),
                            )

                        stateManager.updateState {
                            it.copy(
                                sagaContent = sagaContent,
                                messages = messages,
                                characters = characters,
                                chatState = ChatState.Success,
                            )
                        }

                        if (uiState.value.selectedCharacter == null) {
                            sagaContent.mainCharacter?.let { updateCharacter(it) }
                        }

                        checkIfUpdatesService(sagaContent)
                        validateCharacterMessageUpdates(sagaContent)
                        updateProgress(sagaContent)

                        loadFinished = true

                        if (uiState.value.showTitle) {
                            titleAnimation()
                            validateMessageStatus(sagaContent)
                        }
                    }
            }
        }

        private fun titleAnimation() {
            viewModelScope.launch(Dispatchers.IO) {
                delay(3.seconds)
                stateManager.updateState { it.copy(showTitle = false) }
            }
        }

        private fun validateMessageStatus(sagaContent: SagaContent) {
            viewModelScope.launch(Dispatchers.IO) {
                if (uiState.value.isGenerating) return@launch
                if (uiState.value.isLoading) return@launch
                val messages = sagaContent.flatMessages()
                messages
                    .filter { it.message.status == MessageStatus.LOADING }
                    .forEach { messageContent ->
                        messageUseCase.updateMessage(
                            messageContent.message.copy(
                                status = MessageStatus.ERROR,
                            ),
                        )
                    }
            }
        }

        private fun updateProgress(sagaContent: SagaContent) {
            val progress =
                when {
                    sagaContent.isComplete() -> {
                        1f
                    }

                    (sagaContent.getCurrentTimeLine()?.messages?.size ?: 0) < UpdateRules.LORE_UPDATE_LIMIT -> {
                        val messageCount = sagaContent.getCurrentTimeLine()?.messages?.size ?: 0
                        val maxCount = UpdateRules.LORE_UPDATE_LIMIT
                        messageCount.toFloat() / maxCount.toFloat()
                    }

                    else -> {
                        0f
                    }
                }
            stateManager.updateState { it.copy(loreUpdateProgress = progress) }
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
                                    sagaContent.data.genre.color
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

        private fun observeAmbientMusicServiceControl() {
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

                    val currentSagaData = uiState.value.sagaContent?.data // Use data from current saga content

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
                                    color = currentSagaData.genre.color.toArgb(),
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

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
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

            if (uiState.value.isSendingPending) {
                sendJob?.cancel()
                sendJob = null
                stateManager.updateSendingPending(false)
                stateManager.updateSendingProgress(0f)
                stateManager.updateLoading(false)
                return
            }

            Log.d(
                javaClass.simpleName,
                "Smart Suggestions status: ${uiState.value.smartSuggestionsEnabled}",
            )

            stateManager.updateLoading(true)
            viewModelScope.launch(Dispatchers.IO) {
                if (userConfirmed || uiState.value.smartSuggestionsEnabled.not()) {
                    startPendingSend(isAudio)
                    return@launch
                }

                if (isAudio) {
                    checkTypo(isAudio)
                    return@launch
                }

                val typoCheck =
                    messageUseCase.checkMessageTypo(
                        saga.data.genre,
                        text.text,
                        uiState.value.sagaContent
                            ?.flatMessages()
                            ?.lastOrNull()
                            ?.joinMessage()
                            ?.formatToString(true),
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
            val sendType = uiState.value.senderType
            val saga = uiState.value.sagaContent ?: return
            val mainCharacter =
                uiState.value.selectedCharacter?.data ?: saga.mainCharacter?.data ?: return
            val currentTimeline = uiState.value.sagaContent?.getCurrentTimeLine() ?: return

            val message =
                Message(
                    text = text,
                    speakerName = mainCharacter.name,
                    senderType = sendType,
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
                        saga.data.genre,
                        text,
                        uiState.value.sagaContent
                            ?.flatMessages()
                            ?.lastOrNull()
                            ?.joinMessage()
                            ?.formatToString(true),
                    )

                typoCheck.getSuccess()?.let {
                    val shouldDisplay = it.status != TypoStatus.OK
                    if (shouldDisplay) {
                        stateManager.updateState { s -> s.copy(typoFixMessage = typoCheck.getSuccess()) }
                        when (it.status) {
                            TypoStatus.OK -> {
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

                            TypoStatus.ENHANCEMENT -> {
                                doNothing()
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

                stateManager.updateInput(TextFieldValue())

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
                        characterReference?.data?.name
                    }
                val sceneSummaryData =
                    if (isFromUser.not()) {
                        sceneSummary
                    } else {
                        messageUseCase
                            .getSceneContext(saga)
                            .getSuccess()
                    }

                messageUseCase
                    .saveMessage(
                        saga,
                        message.copy(
                            characterId = characterId,
                            speakerName = speaker ?: message.speakerName,
                            sagaId = saga.data.id,
                        ),
                        isFromUser,
                        sceneSummaryData,
                    ).onSuccessAsync { savedMessage ->
                        resetSuggestions()

                        // If it's from user, trigger AI reply immediately after saving
                        if (isFromUser) {
                            updateLoading(true)
                            replyMessage(savedMessage, sceneSummaryData, isAudio)
                        } else {
                            updateLoading(false)
                        }

                        // Generate reaction asynchronously without blocking
                        withContext(Dispatchers.IO) {
                            messageUseCase.generateReaction(
                                saga,
                                message = savedMessage,
                                sceneSummary = sceneSummaryData,
                            )

                            if (isAudio && isFromUser.not()) {
                                messageUseCase.generateAudio(
                                    saga,
                                    savedMessage,
                                    characterReference,
                                )
                            }
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
                    }
            }
        }

        private fun resetSuggestions() {
            stateManager.updateState { it.copy(typoFixMessage = null, suggestions = emptyList()) }
        }

        private fun updateLoading(isLoading: Boolean) {
            stateManager.updateLoading(isLoading)
            sagaContentManager.setProcessing(isLoading)
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
                updateLoading(true)
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
                                character
                                    .onSuccessAsync {
                                        updateLoading(false)
                                        stateManager.updateState { s -> s.copy(newCharacterReveal = it.id) }
                                        delay(5.seconds)
                                        dismissNewCharacterReveal()
                                    }.onFailureAsync {
                                        updateLoading(false)
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
                        if (newMessage.message.status == MessageStatus.ERROR || newMessage.message.status == MessageStatus.LOADING) {
                            messageUseCase.updateMessage(
                                newMessage.message.copy(
                                    status = MessageStatus.OK,
                                ),
                            )
                        }
                        sagaContentManager.setProcessing(false)
                        delay(3.seconds)
                        generateSuggestions(sceneSummary)
                    }.onFailureAsync {
                        messageUseCase.updateMessage(
                            message.copy(
                                status = MessageStatus.ERROR,
                            ),
                        )
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
                updateLoading(true)
                sagaContentManager
                    .generateCharacter(
                        contextDescription,
                    ).onSuccessAsync {
                        updateLoading(false)
                        stateManager.updateState { s -> s.copy(newCharacterReveal = it.id) }
                        delay(5.seconds)
                        dismissNewCharacterReveal()
                    }.onFailureAsync {
                        updateLoading(false)
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
                    stateManager.updateState { s -> s.copy(originalBitmap = it.first, segmentedBitmap = it.second) }
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
