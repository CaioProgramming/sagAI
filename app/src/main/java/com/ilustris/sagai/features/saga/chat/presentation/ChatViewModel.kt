package com.ilustris.sagai.features.saga.chat.presentation

import MessageStatus
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.R
import com.ilustris.sagai.core.media.MediaPlayerManager
import com.ilustris.sagai.core.media.MediaPlayerService
import com.ilustris.sagai.core.media.model.PlaybackMetadata
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.data.mapper.SagaContentUIMapper
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.data.model.TypoStatus
import com.ilustris.sagai.features.saga.chat.data.usecase.GetInputSuggestionsUseCase
import com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.saga.chat.domain.manager.ChatNotificationManager
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.components.SnackBarState
import com.ilustris.sagai.ui.components.snackBar
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        mediaPlayerManager: MediaPlayerManager,
        private val settingsUseCase: SettingsUseCase,
    ) : ViewModel(),
        DefaultLifecycleObserver {
        val state = MutableStateFlow<ChatState>(ChatState.Loading)

        val content = sagaContentManager.content
        val messages = MutableStateFlow<List<ActDisplayData>>(emptyList())
        val isGenerating = sagaContentManager.narrativeProcessingUiState
        val isLoading = MutableStateFlow(false)
        val characters = MutableStateFlow<List<Character>>(emptyList())
        val showTitle = MutableStateFlow(true)
        val loreUpdateProgress = MutableStateFlow(0f)
        val isPlaying: StateFlow<Boolean> = mediaPlayerManager.isPlaying
        val snackBarMessage = MutableStateFlow<SnackBarState?>(null)
        val suggestions = MutableStateFlow<List<Suggestion>>(emptyList())
        val inputValue = MutableStateFlow(TextFieldValue())
        val sendType = MutableStateFlow(SenderType.CHARACTER)

        val typoFixMessage: MutableStateFlow<TypoFix?> = MutableStateFlow(null)

        private var aiTurns = 0

        val selectedCharacter: MutableStateFlow<CharacterContent?> = MutableStateFlow(null)
        private var loadFinished = false
        private var currentSagaIdForService: String? = null
        private var currentActCountForService: Int = 0

        val notificationsEnabled = MutableStateFlow(true)
        val smartSuggestionsEnabled = MutableStateFlow(true)

        private fun updateSnackBar(snackBarState: SnackBarState) {
            viewModelScope.launch(Dispatchers.IO) {
                snackBarMessage.value = snackBarState
                delay(10.seconds)
                snackBarMessage.value = null
                isLoading.value = false
            }
        }

        fun initChat(
            sagaId: String?,
            isDebug: Boolean = false,
        ) {
            if (sagaId == null) {
                return
            }
            currentSagaIdForService = sagaId
            state.value = ChatState.Loading
            enableDebugMode(isDebug)
            observeSaga()
            observeAmbientMusicServiceControl()
            observePreferences()
            observeSnackBarUpdates()
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.loadSaga(sagaId)
            }
        }

        fun observeSnackBarUpdates() =
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.snackBarUpdate.collect {
                    it?.let { snackBarState -> sendSnackBarMessage(snackBarState) }
                }
            }

        private fun observePreferences() {
            viewModelScope.launch(Dispatchers.IO) {
                settingsUseCase.getSmartSuggestionsEnabled().collect {
                    smartSuggestionsEnabled.emit(it)
                }

                settingsUseCase.getNotificationsEnabled().collect {
                    notificationsEnabled.emit(it)
                }

                settingsUseCase.backupEnabled().collect {
                    if (it.not()) {
                        updateSnackBar(
                            snackBarState =
                                snackBar("Backup desativado. Verifique as permissões, para nao perder o progresso.") {
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
            inputValue.value = value
        }

        fun updateSendType(type: SenderType) {
            sendType.value = type
        }

        fun reviewWiki(wikiItems: List<Wiki>) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.reviewWiki(wikiItems)
            }
        }

        fun appendWiki(wiki: Wiki) {
            viewModelScope.launch(Dispatchers.IO) {
                inputValue.value = inputValue.value.copy(text = inputValue.value.text + " " + wiki.title)
            }
        }

        fun checkSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.checkNarrativeProgression(content.value)
            }
        }

        fun retryAiResponse(message: Message?) {
            message?.let {
                replyMessage(message, null)
            }
        }

        fun requestNewCharacter(name: String) {
            val saga = content.value ?: return
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

        fun updateCharacter(characterContent: CharacterContent) {
            viewModelScope.launch(Dispatchers.IO) {
                selectedCharacter.emit(characterContent)
            }
        }

        private fun observeSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                content
                    .collectLatest { sagaContent ->
                        if (sagaContent == null) {
                            if (loadFinished) {
                                state.value = ChatState.Error("Saga not found.")
                            }
                            return@collectLatest
                        }

                        val previousValue = content.value
                        messages.value =
                            SagaContentUIMapper
                                .mapToActDisplayData(sagaContent.acts)

                        characters.value =
                            sortCharactersByMessageCount(
                                sagaContent.getCharacters(),
                                sagaContent.flatMessages(),
                            )

                        if (selectedCharacter.value == null) {
                            sagaContent.mainCharacter?.let { updateCharacter(it) }
                        }

                        checkIfUpdatesService(sagaContent)
                        validateCharacterMessageUpdates(sagaContent)
                        updateProgress(sagaContent)
                        if (previousValue?.flatMessages()?.size != sagaContent.flatMessages().size) {
                            notifyIfNeeded()
                        }
                        loadFinished = true
                        content.emit(sagaContent)
                        state.emit(ChatState.Success)

                        if (showTitle.value) {
                            titleAnimation()
                        }
                    }
            }
        }

        private fun titleAnimation() {
            viewModelScope.launch(Dispatchers.IO) {
                delay(3.seconds)
                showTitle.emit(false)
            }
        }

        private fun updateProgress(sagaContent: SagaContent) {
            loreUpdateProgress.value =
                when {
                    sagaContent.isComplete() -> 1f
                    (
                        sagaContent.getCurrentTimeLine()?.messages?.size
                            ?: 0
                    ) < UpdateRules.LORE_UPDATE_LIMIT -> {
                        val messageCount = sagaContent.getCurrentTimeLine()?.messages?.size ?: 0
                        val maxCount = UpdateRules.LORE_UPDATE_LIMIT
                        messageCount.toFloat() / maxCount.toFloat()
                    }

                    else -> 0f
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
                                    sagaContent.data.genre.color
                                        .toArgb(),
                                currentActNumber = newActCount.coerceAtLeast(1),
                                mediaFilePath = musicFile.absolutePath,
                            )
                        controlMediaPlayerService(MediaPlayerService.ACTION_PLAY, playbackMetadata)
                        Log.d("ChatViewModel", "New act ($newActCount). Updated MediaPlayerService.")
                    }
                }
            }
        }

        private fun notifyIfNeeded() {
            viewModelScope.launch(Dispatchers.IO) {
                if (notificationsEnabled.value) {
                    val currentSaga = content.value ?: return@launch
                    if (currentSaga.data.isEnded) {
                        return@launch
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        val messages = currentSaga.flatMessages()
                        if (messages.isNotEmpty()) {
                            notificationManager.sendMessageNotification(
                                currentSaga,
                                currentSaga.flatMessages().last(),
                            )
                        }
                    }
                } else {
                    Log.w(javaClass.simpleName, "notifyIfNeeded: Notifications disabled by user")
                }
            }
        }

        private fun controlMediaPlayerService(
            action: String,
            metadata: PlaybackMetadata? = null,
        ) {
            val serviceIntent =
                Intent(context, MediaPlayerService::class.java).apply {
                    this.action = action
                    metadata?.let {
                        putExtra(MediaPlayerService.EXTRA_SAGA_CONTENT_JSON, it.toJsonFormat())
                    }
                }
            try {
                context.startService(serviceIntent)
                Log.d(
                    "ChatViewModel",
                    "Sent $action to MediaPlayerService. Metadata: ${metadata?.toJsonFormat()}",
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error controlling MediaPlayerService with action $action", e)
                updateSnackBar(
                    snackBar(
                        "Não foi possível iniciar o serviço áudio. Verifique as permissões",
                    ),
                )
            }
        }

        private fun observeAmbientMusicServiceControl() {
            viewModelScope.launch(Dispatchers.IO) {
                if (content.value == null) return@launch
                sagaContentManager.ambientMusicFile.collect { musicFile ->

                    Log.i(
                        javaClass.simpleName,
                        "observeAmbientMusicServiceControl: file updated -> $musicFile",
                    )
                    if (musicFile == null && isPlaying.value.not()) {
                        Log.w(
                            javaClass.simpleName,
                            "observeAmbientMusicServiceControl: Music not found skipping player",
                        )
                        return@collect
                    }

                    val currentSagaData = content.value?.data // Use data from current saga content

                    if (currentSagaData != null && musicFile != null && musicFile.exists()) {
                        if (currentSagaIdForService == currentSagaData.id.toString()) {
                            val playbackMetadata =
                                PlaybackMetadata(
                                    sagaId = currentSagaData.id,
                                    sagaTitle = currentSagaData.title,
                                    sagaIcon = currentSagaData.icon,
                                    currentActNumber =
                                        content.value
                                            ?.acts
                                            ?.size
                                            ?.coerceAtLeast(1) ?: 1,
                                    mediaFilePath = musicFile.absolutePath,
                                    color = currentSagaData.genre.color.toArgb(),
                                )
                            currentActCountForService = playbackMetadata.currentActNumber
                            controlMediaPlayerService(MediaPlayerService.ACTION_PLAY, playbackMetadata)
                            Log.d(
                                "ChatViewModel",
                                "Ambient music file available. Instructing MediaPlayerService to play.",
                            )
                        }
                    } else {
                        if (currentSagaIdForService != null && (musicFile == null || !musicFile.exists())) {
                            Log.d(
                                "ChatViewModel",
                                "Ambient music file null/invalid for current saga. Instructing service to stop.",
                            )
                            controlMediaPlayerService(MediaPlayerService.ACTION_STOP)
                        }
                    }
                }
            }
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            Log.d("ChatViewModel", "Lifecycle: onResume. MediaPlayerService manages its own state.")
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            Log.d("ChatViewModel", "Lifecycle: onPause called. Music continues via service if playing.")
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.backupSaga()
            }
        }

        override fun onCleared() {
            super.onCleared()
            Log.d("ChatViewModel", "onCleared called. Instructing MediaPlayerService to stop.")
            controlMediaPlayerService(MediaPlayerService.ACTION_STOP)
        }

        private suspend fun validateCharacterMessageUpdates(content: SagaContent) {
            val updatableMessages =
                content.flatMessages().filter { messageContent ->
                    messageContent.character == null &&
                        messageContent.message.senderType == SenderType.CHARACTER &&
                        messageContent.message.speakerName != null &&
                        content
                            .getCharacters()
                            .find { it.name == messageContent.message.speakerName } != null
                }

            updatableMessages.forEach { message ->
                val character =
                    content.getCharacters().find { it.name.equals(message.message.speakerName, true) }
                character?.let {
                    messageUseCase.updateMessage(
                        message.message.copy(
                            characterId = it.id,
                            speakerName = it.name,
                        ),
                    )
                }
            }
        }

        private fun sendSnackBarMessage(snackBarState: SnackBarState) {
            viewModelScope.launch(Dispatchers.IO) {
                snackBarMessage.value = snackBarState
                delay(15.seconds)
                snackBarMessage.value = null
            }
        }

        fun dismissSnackBar() {
            snackBarMessage.value = null
        }

        fun sendInput(userConfirmed: Boolean = false) {
            if (inputValue.value.text.isBlank()) {
                Log.e(javaClass.simpleName, "sendInput: Input is empty")
                return
            }

            val text = inputValue.value
            val sendType = sendType.value

            val saga = content.value ?: return
            val mainCharacter = selectedCharacter.value?.data ?: saga.mainCharacter?.data
            if (mainCharacter == null) return
            val currentTimeline = content.value?.getCurrentTimeLine()
            if (currentTimeline == null) {
                sagaContentManager.checkNarrativeProgression(content.value)
                return
            }

            Log.d(javaClass.simpleName, "Smart Suggestions status: ${smartSuggestionsEnabled.value}")

            isLoading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                if (userConfirmed || smartSuggestionsEnabled.value.not()) {
                    val message =
                        Message(
                            text = text.text,
                            speakerName = mainCharacter.name,
                            senderType = sendType,
                            characterId = mainCharacter.id,
                            timelineId = currentTimeline.data.id,
                        )
                    sendMessage(message, true, null)
                    return@launch
                }

                val typoCheck =
                    messageUseCase.checkMessageTypo(
                        saga.data.genre,
                        text.text,
                        content.value
                            ?.flatMessages()
                            ?.lastOrNull()
                            ?.joinMessage()
                            ?.formatToString(true),
                    )

                typoCheck.getSuccess()?.let {
                    val shouldDisplay = it.suggestedText != text.text
                    if (shouldDisplay) {
                        typoFixMessage.value = typoCheck.getSuccess()
                        when (it.status) {
                            TypoStatus.OK -> {
                                sendInput(true)
                            }

                            TypoStatus.FIX -> {
                                delay(5.seconds)
                                if (typoFixMessage.value != null) {
                                    inputValue.value =
                                        TextFieldValue(it.suggestedText ?: inputValue.value.text)
                                    sendInput(true)
                                }
                            }

                            TypoStatus.ENHANCEMENT -> doNothing()
                        }
                    }
                } ?: run {
                    sendInput(true)
                }
            }
        }

        private fun sendMessage(
            message: Message,
            isFromUser: Boolean = false,
            sceneSummary: SceneSummary?,
        ) {
            updateLoading(isFromUser)
            viewModelScope.launch(Dispatchers.IO) {
                val saga = content.value ?: return@launch
                val mainCharacter = content.value!!.mainCharacter?.data
                val characters = content.value!!.getCharacters()
                val characterReference =
                    characters.find {
                        message.speakerName?.equals(it.name, true) == true ||
                            (mainCharacter != null && message.characterId == mainCharacter.id)
                    }

                inputValue.value = TextFieldValue()

                resetSuggestions()
                val characterId =
                    if (message.senderType == SenderType.NARRATOR) {
                        null
                    } else {
                        message.characterId ?: characterReference?.id
                    }
                val sceneSummary = sceneSummary ?: messageUseCase.getSceneContext(saga).getSuccess()

                messageUseCase
                    .saveMessage(
                        saga,
                        message.copy(
                            characterId = characterId,
                            sagaId = saga.data.id,
                        ),
                        isFromUser,
                        sceneSummary,
                    ).onSuccessAsync {
                        resetSuggestions()
                        updateLoading(false)
                        handleNewMessage(
                            message = it,
                            isFromUser = isFromUser,
                            sceneSummary = sceneSummary,
                        )
                        withContext(Dispatchers.IO) {
                            messageUseCase.generateReaction(
                                saga,
                                message = it,
                                sceneSummary = sceneSummary,
                            )
                        }
                    }.onFailureAsync {
                        snackBar(
                            "Ocorreu um erro ao salvar a mensagem",
                        ) {
                            action {
                                resendMessage(message)
                            }
                        }
                        typoFixMessage.value = null
                        updateLoading(false)
                    }
            }
        }

        private fun resetSuggestions() {
            typoFixMessage.value = null
            suggestions.value = emptyList()
        }

        private fun updateLoading(isLoading: Boolean) {
            this.isLoading.value = isLoading
            sagaContentManager.setProcessing(isLoading)
        }

        private fun handleNewMessage(
            message: Message,
            isFromUser: Boolean,
            sceneSummary: SceneSummary?,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                updateLoading(false)
                if (isFromUser) {
                    replyMessage(message, sceneSummary)
                }
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
                    "generateSuggestions: checking if is generating -> ${isGenerating.value}",
                )
                if (isGenerating.value || isLoading.value) {
                    return@launch
                }

                val currentSaga = content.value ?: return@launch
                if (currentSaga.data.isEnded) return@launch
                val currentTimeline = currentSaga.getCurrentTimeLine() ?: return@launch
                if (currentTimeline.messages.isEmpty()) return@launch

                suggestionUseCase(
                    currentTimeline.messages,
                    currentSaga.mainCharacter?.data,
                    currentSaga,
                    sceneSummary,
                ).onSuccess {
                    suggestions.value = it
                }
            }
        }

        private fun replyMessage(
            message: Message,
            sceneSummary: SceneSummary?,
        ) {
            updateLoading(true)
            viewModelScope.launch(Dispatchers.IO) {
                val saga = content.value ?: return@launch
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
                    ).onSuccessAsync { genMessage ->

                        if (genMessage.shouldCreateCharacter && genMessage.newCharacter != null) {
                            createCharacter(
                                buildString {
                                    appendLine("New character context:")
                                    appendLine(genMessage.newCharacter.toJsonFormat())
                                    newMessage.let {
                                        appendLine("Previous Message context:")
                                        appendLine(newMessage.toJsonFormat())
                                    }
                                },
                            )
                        }

                        sendMessage(
                            genMessage.message.copy(
                                characterId = null,
                                timelineId = timeline.data.id,
                                id = 0,
                                status = MessageStatus.OK,
                                speakerName = if (genMessage.shouldCreateCharacter) genMessage.newCharacter?.name else genMessage.message.speakerName,
                            ),
                            false,
                            sceneSummary,
                        )
                        if (newMessage.message.status == MessageStatus.ERROR) {
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
                        aiTurns = 0
                        messageUseCase.updateMessage(
                            message.copy(
                                status = MessageStatus.ERROR,
                            ),
                        )
                        sagaContentManager.setProcessing(false)
                        isLoading.value = false
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
            updateLoading(true)

            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager
                    .generateCharacter(
                        contextDescription,
                    ).onSuccessAsync {
                        updateSnackBar(
                            snackBar(
                                context.getString(
                                    R.string.new_character_message,
                                    it.name,
                                ),
                            ) {
                                action {
                                    icon = it.image
                                    openDetails(it)
                                }
                            },
                        )
                        updateLoading(false)
                    }.onFailureAsync {
                        updateSnackBar(
                            snackBar(
                                message = "Ocorreu um erro ao criar o personagem",
                            ) {
                                action {
                                    retryCharacter(contextDescription)
                                }
                            },
                        )
                        updateLoading(false)
                    }
            }
        }

        private fun enableDebugMode(enabled: Boolean) {
            sagaContentManager.setDebugMode(enabled)
            messageUseCase.setDebugMode(enabled)
            Log.i("ChatViewModel", "Debug mode set to: $enabled")
        }

        fun sendFakeUserMessages(count: Int) {
            val currentSaga = content.value ?: return
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
                            text = "Fake Message #\${it + 1} of $count",
                            senderType = SenderType.USER,
                            characterId = mainCharacterId,
                            sagaId = currentSaga.data.id,
                            timelineId = timeline.data.id,
                        )
                    sendMessage(fakeUserMessage, false, null)
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
    }
