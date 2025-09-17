package com.ilustris.sagai.features.saga.chat.presentation

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.media.MediaPlayerManager
import com.ilustris.sagai.core.media.MediaPlayerService
import com.ilustris.sagai.core.media.model.PlaybackMetadata
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.data.model.TypoStatus
import com.ilustris.sagai.features.saga.chat.domain.manager.ChatNotificationManager
import com.ilustris.sagai.features.saga.chat.domain.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.domain.mapper.SagaContentUIMapper
import com.ilustris.sagai.features.saga.chat.domain.model.Message
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.features.saga.chat.domain.usecase.GetInputSuggestionsUseCase
import com.ilustris.sagai.features.saga.chat.domain.usecase.MessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
        mediaPlayerManager: MediaPlayerManager,
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
        val sendType = MutableStateFlow(SenderType.USER)

        val typoFixMessage: MutableStateFlow<TypoFix?> = MutableStateFlow(null)
        private var loadFinished = false
        private var currentSagaIdForService: String? = null
        private var currentActCountForService: Int = 0

        private fun sendError(
            errorMessage: String,
            action: ChatAction = ChatAction.RESEND,
            data: Any? = null,
            buttonText: String = "Ok",
        ) {
            viewModelScope.launch {
                snackBarMessage.value =
                    SnackBarState(
                        title = "Ocorreu um erro inesperado",
                        text = errorMessage,
                        redirectAction = Triple(action, buttonText, data),
                    )
                delay(15.seconds)
                snackBarMessage.value = null
                isLoading.value = false
            }
        }

        fun initChat(
            sagaId: String?,
            isDebug: Boolean = false,
        ) {
            if (sagaId == null) {
                sendError("Saga not found. Please select a valid saga.")
                return
            }
            currentSagaIdForService = sagaId
            state.value = ChatState.Loading
            enableDebugMode(isDebug)
            observeSaga()
            observeAmbientMusicServiceControl()
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.loadSaga(sagaId)
            }
        }

        fun updateInput(value: TextFieldValue) {
            inputValue.value = value
        }

        fun updateSendType(type: SenderType) {
            sendType.value = type
        }

        fun retryAiResponse(message: Message?) {
            message?.let {
                replyMessage(message)
            }
        }

        private fun observeSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                content.collectLatest { sagaContent ->
                    val isFirstLoading = content.value == null
                    if (sagaContent == null) {
                        if (loadFinished) {
                            state.value = ChatState.Error("Saga not found.")
                        }
                        return@collectLatest
                    }

                    val allMessages = messages.value.flatMap { it.content.chapters.flatMap { it.events.flatMap { it.messages } } }

                    if (allMessages.size != sagaContent.flatMessages().size) {
                        generateSuggestions()
                    }

                    messages.value =
                        SagaContentUIMapper
                            .mapToActDisplayData(sagaContent.acts)

                    characters.value =
                        sortCharactersByMessageCount(sagaContent.getCharacters(), sagaContent.flatMessages())

                    checkIfUpdatesService(sagaContent)

                    validateCharacterMessageUpdates(sagaContent)
                    updateProgress(sagaContent)
                    notifyIfNeeded()
                    state.value = ChatState.Success
                    loadFinished
                    if (isFirstLoading) {
                        delay(3.seconds)
                        showTitle.emit(false)
                    }
                }
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
            val currentSaga = content.value ?: return
            if (currentSaga.data.isEnded) {
                return
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
                sendError("Could not control background audio. Please check app permissions.")
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
                        content.getCharacters().find { it.name == messageContent.message.speakerName } != null
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

        private fun sendSnackbarMessage(snackBarState: SnackBarState) {
            viewModelScope.launch {
                snackBarMessage.value = snackBarState
                delay(15.seconds)
                snackBarMessage.value = null
            }
        }

        fun sendInput(userConfirmed: Boolean = false) {
            if (inputValue.value.text.isBlank()) {
                return
            }

            val text = inputValue.value
            val sendType = sendType.value

            val saga = content.value ?: return
            val mainCharacter = saga.mainCharacter ?: return
            val currentTimeline = content.value?.getCurrentTimeLine()
            if (currentTimeline == null) {
                sagaContentManager.checkNarrativeProgression(content.value)
                return
            }

            isLoading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                if (userConfirmed) {
                    val message =
                        Message(
                            text = text.text,
                            speakerName = mainCharacter.data.name,
                            senderType = sendType,
                            characterId = mainCharacter.data.id,
                            timelineId = currentTimeline.data.id,
                        )
                    sendMessage(message, true)
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
                                    inputValue.value = TextFieldValue(it.suggestedText ?: inputValue.value.text)
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
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val saga = content.value ?: return@launch
                val mainCharacter = content.value!!.mainCharacter?.data
                val characters = content.value!!.getCharacters()
                val characterReference =
                    characters.find {
                        message.speakerName?.contains(it.name, true) == true ||
                            (mainCharacter != null && message.characterId == mainCharacter.id)
                    }

                inputValue.value =
                    inputValue.value.copy(
                        text = emptyString(),
                    )

                isLoading.value = true
                messageUseCase
                    .saveMessage(
                        message.copy(
                            sagaId = saga.data.id,
                            characterId = message.characterId ?: characterReference?.id,
                        ),
                        isFromUser,
                    ).onSuccess {
                        isLoading.value = false
                        typoFixMessage.value = null
                        suggestions.value = emptyList()
                        handleNewMessage(it, isFromUser)
                    }.onFailure {
                        sendError("Ocorreu um erro ao salvar a mensagem")
                        typoFixMessage.value = null
                    }
            }
        }

        private fun handleNewMessage(
            it: Message,
            isFromUser: Boolean,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                when (isFromUser) {
                    true -> {
                        if (sagaContentManager.isInDebugMode().not()) {
                            replyMessage(it)
                        }
                    }

                    else -> doNothing()
                }
            }
        }

        private fun generateSuggestions() {
            if (sagaContentManager.isInDebugMode()) {
                return
            }

            if (isGenerating.value) {
                return
            }

            Log.d(javaClass.simpleName, "generateSuggestions: checking if is generating -> $isGenerating")
            val currentSaga = content.value ?: return
            if (currentSaga.data.isEnded) return
            val currentTimeline = currentSaga.getCurrentTimeLine()
            if (currentTimeline == null) return
            if (currentTimeline.messages.isEmpty()) return
            viewModelScope.launch(Dispatchers.IO) {
                content.value?.data?.let { saga ->
                    suggestions.value = emptyList()

                    delay(500L)
                    suggestionUseCase
                        .invoke(
                            currentTimeline.messages,
                            currentSaga.mainCharacter?.data,
                            saga,
                        ).onSuccess {
                            suggestions.value = it
                        }
                }
            }
        }

        private fun replyMessage(message: Message) {
            viewModelScope.launch(Dispatchers.IO) {
                val saga = content.value ?: return@launch
                val timeline = saga.getCurrentTimeLine()
                if (timeline == null) {
                    sagaContentManager.checkNarrativeProgression(saga)
                    return@launch
                }
                isLoading.emit(true)
                val newMessage =
                    MessageContent(
                        message = message,
                        character = saga.getCharacters().find { it.id == message.characterId },
                    )

                messageUseCase
                    .generateMessage(
                        saga = saga,
                        message = newMessage,
                    ).onSuccessAsync { genMessage ->
                        if (genMessage.shouldCreateCharacter && genMessage.newCharacter != null) {
                            createCharacter(genMessage.newCharacter)
                        }
                        sendMessage(
                            genMessage.message.copy(
                                chapterId = null,
                                actId = null,
                                characterId = null,
                                timelineId = timeline.data.id,
                                id = 0,
                            ),
                        )
                    }.onFailure {
                        sendError(
                            "Ocorreu um erro ao responder sua mensagem.",
                            action = ChatAction.RETRY_AI_RESPONSE,
                            message,
                            buttonText = "Tentar novamente",
                        )
                    }
            }
        }

        fun createCharacter(newCharacter: CharacterInfo) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager
                    .generateCharacter(
                        newCharacter.toJsonFormat(),
                    ).onSuccessAsync {
                        sendSnackbarMessage(
                            SnackBarState(
                                "${it.name} juntou-se a história!",
                                it.backstory,
                                redirectAction = Triple(ChatAction.OPEN_CHARACTER, "Ver mais", it.id),
                            ),
                        )
                        sagaContentManager.generateCharacterImage(
                            it,
                        )

                        delay(20.seconds)
                    }.onFailure {
                        sendError("Ocorreu um erro ao criar o personagem.")
                    }
            }
        }

        fun dismissSnackBar() {
            snackBarMessage.value = null
        }

        private fun enableDebugMode(enabled: Boolean) {
            sagaContentManager.setDebugMode(enabled)
            messageUseCase.setDebugMode(enabled)
            Log.i("ChatViewModel", "Debug mode set to: $enabled")
        }

        fun sendFakeUserMessages(count: Int) {
            val currentSaga = content.value
            if (currentSaga == null) {
                sendError("Saga not loaded, cannot send fake messages.")
                return
            }
            val mainCharacterId = currentSaga.mainCharacter?.data?.id
            if (mainCharacterId == null && currentSaga.data.id != 0) {
                sendError("Main character not found for non-debug saga, cannot send fake messages.")
                return
            }

            val timeline = currentSaga.getCurrentTimeLine()
            if (timeline == null) {
                sendError("Current timeline not found, cannot send fake messages.")
                return
            }

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
                    sendMessage(fakeUserMessage, isFromUser = false)
                    delay(100)
                }
                Log.d("ChatViewModel", "Finished enqueuing $count fake messages.")
            }
        }
    }
