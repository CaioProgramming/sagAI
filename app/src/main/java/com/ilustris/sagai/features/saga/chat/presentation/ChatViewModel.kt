package com.ilustris.sagai.features.saga.chat.presentation

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.domain.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.domain.usecase.GetInputSuggestionsUseCase
import com.ilustris.sagai.features.saga.chat.domain.usecase.MessageUseCase
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.CharacterInfo
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.joinMessage
import com.ilustris.sagai.features.timeline.data.model.Timeline
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
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
    ) : ViewModel(),
        DefaultLifecycleObserver {
        val state = MutableStateFlow<ChatState>(ChatState.Empty)

        val content = MutableStateFlow<SagaContent?>(null)
        val messages = MutableStateFlow<List<MessageContent>>(emptyList())
        val isGenerating = MutableStateFlow(false)
        val characters = MutableStateFlow<List<Character>>(emptyList())
        val isPlaying = MutableStateFlow(false)
        val snackBarMessage = MutableStateFlow<SnackBarState?>(null)
        val suggestions = MutableStateFlow<List<Suggestion>>(emptyList())
        private var mediaPlayer: MediaPlayer? = null
        private var lastKnownMusicFile: File? = null

        private fun sendError(errorMessage: String) {
            viewModelScope.launch {
                snackBarMessage.value =
                    SnackBarState(
                        title = "Ocorreu um erro inesperado",
                        text = errorMessage,
                        redirectAction = Triple(ChatAction.RESEND, "Ok", null),
                    )
                isGenerating.value = false
                delay(15.seconds)
                snackBarMessage.value = null
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
            state.value = ChatState.Loading
            enableDebugMode(isDebug)
            observeSaga()
            observeContentUpdate()
            observeEnding()
            observeAmbientMusic()
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.loadSaga(sagaId)
            }
        }

        private fun observeEnding() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.endMessage.collect {
                    if (it != null && it.isNotEmpty()) {
                        snackBarMessage.emit(
                            SnackBarState(
                                "Você chegou ao fim.",
                                it,
                            ),
                        )
                    }
                }
            }
        }

        private fun observeSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.content.collect {
                    if (it != null) {
                        content.value = it
                        if ((!sagaContentManager.isInDebugMode() || it.messages.isNotEmpty()) &&
                            it.messages.size != messages.value.size &&
                            isGenerating.value.not()
                        ) {
                            // generateSuggestions(it.messages)
                        }
                        messages.value =
                            it.messages.sortedByDescending { messageContent -> messageContent.message.timestamp }
                        characters.value = sortCharactersByMessageCount(it.characters, it.messages)
                        state.value = ChatState.Success

                        if (it.messages.isEmpty() && isGenerating.value.not()) {
                            generateIntroduction(it.data, it.mainCharacter)
                            sendSnackbarMessage(
                                SnackBarState(
                                    "Gerando introdução",
                                    "",
                                ),
                            )
                        }

                        validateCharacterMessageUpdates(it)
                    }
                }
            }
        }

        private fun observeAmbientMusic() {
            viewModelScope.launch {
                sagaContentManager.ambientMusicFile.collectLatest { musicFile ->
                    lastKnownMusicFile = musicFile
                    if (musicFile != null) {
                        initializeMediaPlayer(musicFile)
                    } else {
                        stopAmbientMusic()
                    }
                }
            }
        }

        private fun initializeMediaPlayer(file: File) {
            try {
                stopAmbientMusic()
                mediaPlayer =
                    MediaPlayer().apply {
                        setDataSource(context, file.toUri())
                        isLooping = true
                        setVolume(.3f, .3f)
                        prepareAsync()
                        setOnPreparedListener {
                            start()
                            Log.i("ChatViewModel", "MediaPlayer prepared and started for: ${file.name}")
                            this@ChatViewModel.isPlaying.value = true
                        }
                        setOnErrorListener { _, what, extra ->
                            Log.e(
                                "ChatViewModel",
                                "MediaPlayer Error: what: $what, extra: $extra for file: ${file.name}",
                            )
                            stopAmbientMusic()
                            true
                        }
                    }
                lastKnownMusicFile = file
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error initializing MediaPlayer for file: ${file.name}", e)
                stopAmbientMusic()
            }
        }

        fun pauseAmbientMusic() {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                Log.d("ChatViewModel", "MediaPlayer paused.")
                this@ChatViewModel.isPlaying.value = false
            }
        }

        fun resumeAmbientMusic() {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
                Log.d("ChatViewModel", "MediaPlayer resumed from paused state.")
            } else if (mediaPlayer == null && lastKnownMusicFile != null) {
                Log.d(
                    "ChatViewModel",
                    "MediaPlayer is null, re-initializing with last known file: ${lastKnownMusicFile!!.name}",
                )
                initializeMediaPlayer(lastKnownMusicFile!!)
            } else {
                Log.d(
                    "ChatViewModel",
                    "MediaPlayer not resumed. isPlaying: ${mediaPlayer?.isPlaying}, mediaPlayer null: ${mediaPlayer == null}, lastKnownFile null: ${lastKnownMusicFile == null}",
                )
            }
        }

        private fun stopAmbientMusic() {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            lastKnownMusicFile = null
            Log.d("ChatViewModel", "MediaPlayer stopped and released. Last known file cleared.")
            this@ChatViewModel.isPlaying.value = false
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            Log.d("ChatViewModel", "Lifecycle: onResume called, resuming music.")
            resumeAmbientMusic()
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            Log.d("ChatViewModel", "Lifecycle: onPause called, pausing music.")
            pauseAmbientMusic()
        }

        override fun onCleared() {
            super.onCleared()
            stopAmbientMusic()
            Log.d("ChatViewModel", "onCleared called, MediaPlayer released.")
        }

        private suspend fun validateCharacterMessageUpdates(content1: SagaContent) {
            val updatableMessages =
                content1.messages.filter { messageContent ->
                    messageContent.character == null &&
                        messageContent.message.senderType == SenderType.CHARACTER &&
                        messageContent.message.speakerName != null &&
                        content1.characters.find { it.name == messageContent.message.speakerName } != null
                }

            updatableMessages.forEach { message ->
                val character =
                    content1.characters.find { it.name.equals(message.message.speakerName, true) }
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

        private fun observeContentUpdate() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.contentUpdateMessages.collect {
                    sendMessage(it)
                }
            }
        }

        private fun generateIntroduction(
            saga: Saga,
            character: Character?,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                isGenerating.value = true
                if (sagaContentManager.isInDebugMode()) {
                    sendMessage(
                        Message(
                            0,
                            "Starting the debug saga!",
                            senderType = SenderType.NARRATOR,
                        ),
                    )
                    sendSnackbarMessage(
                        SnackBarState(
                            "Debug saga iniciada!",
                            "",
                        ),
                    )
                    isGenerating.value = false
                    return@launch
                }
                messageUseCase
                    .generateIntroMessage(saga, character)
                    .onSuccess {
                        sendMessage(
                            it.copy(
                                senderType = SenderType.NARRATOR,
                                characterId = character?.id,
                            ),
                            false,
                        )
                    }.onFailure {
                        sendError(it.message ?: "Unknown error")
                    }

                isGenerating.value = false
                state.value = ChatState.Success
            }
        }

        fun sendInput(
            text: String,
            sendType: SenderType,
        ) {
            val message =
                Message(
                    text = text,
                    speakerName = null,
                    senderType = sendType,
                    characterId = content.value?.mainCharacter?.id,
                )
            sendMessage(message, true)
        }

        private fun sendMessage(
            message: Message,
            isFromUser: Boolean = false,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val saga = content.value ?: return@launch
                val mainCharacter = content.value!!.mainCharacter
                val characters = content.value!!.characters
                val characterReference =
                    characters.find {
                        message.speakerName?.contains(it.name, true) == true ||
                            (mainCharacter != null && message.characterId == mainCharacter.id) // Check against mainCharacter if it exists
                    }
                val sendType =
                    if (mainCharacter != null && characterReference?.id == mainCharacter.id) SenderType.USER else message.senderType
                val speakerId =
                    when (sendType) {
                        SenderType.NARRATOR -> message.characterId
                        SenderType.NEW_CHARACTER,
                        SenderType.NEW_CHAPTER,
                        -> null

                        SenderType.USER -> mainCharacter?.id
                        else -> characterReference?.id
                    }
                if (message.senderType == SenderType.NEW_CHAPTER || message.senderType == SenderType.NEW_ACT) {
                    val lastMessage =
                        content.value?.messages?.getOrNull(content.value!!.messages.size - 2)
                    if (lastMessage != null &&
                        lastMessage.message.senderType == SenderType.NEW_CHAPTER ||
                        lastMessage?.message?.senderType == SenderType.NEW_ACT
                    ) {
                        Log.w(
                            javaClass.simpleName,
                            "sendMessage: Not saving message, almost saved duplicate.",
                        )
                        return@launch
                    }
                }
                messageUseCase
                    .saveMessage(
                        message.copy(
                            sagaId = saga.data.id,
                            characterId = speakerId,
                        ),
                    ).onSuccess {
                        handleNewMessage(it, isFromUser)
                    }.onFailure {
                        sendError("Ocorreu um erro ao salvar a mensagem")
                    }
            }
        }

        private fun handleNewMessage(
            it: Message,
            isFromUser: Boolean,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                when (isFromUser && it.senderType != SenderType.NEW_CHARACTER) {
                    true -> {
                        if (sagaContentManager.isInDebugMode().not()) {
                            replyMessage(it)
                        }
                    }

                    else -> doNothing()
                }
                checkLoreUpdate()
            }
        }

        private fun generateSuggestions(messagesList: List<MessageContent>) {
            suggestions.value = emptyList()
            if (sagaContentManager.isInDebugMode()) {
                return
            }
            viewModelScope.launch(Dispatchers.IO) {
                content.value?.data?.let { saga ->
                    suggestionUseCase
                        .invoke(
                            messagesList
                                .filter {
                                    it.message.senderType != SenderType.NEW_CHAPTER &&
                                        it.message.senderType != SenderType.NEW_ACT &&
                                        it.message.senderType != SenderType.NEW_CHARACTER
                                }.sortedBy { it.message.timestamp }
                                .takeLast(
                                    5,
                                ),
                            content.value?.mainCharacter,
                            saga,
                        ).onSuccess {
                            suggestions.value = it
                        }
                }
            }
        }

        private fun checkLoreUpdate() {
            viewModelScope.launch(Dispatchers.IO) {
                val currentSagaContent = content.value ?: return@launch

                val allMessages = messages.value

                val lastLoreReferenceId =
                    currentSagaContent.timelines.maxByOrNull { it.createdAt }?.messageReference
                val messagesSinceLastLore: List<MessageContent> =
                    if (lastLoreReferenceId != null) {
                        val indexOfLastLoreMessage =
                            allMessages.indexOfFirst { it.message.id == lastLoreReferenceId }
                        if (indexOfLastLoreMessage != -1) {
                            allMessages.subList(0, indexOfLastLoreMessage)
                        } else {
                            allMessages
                        }
                    } else {
                        allMessages
                    }

                if (messagesSinceLastLore.size >= UpdateRules.LORE_UPDATE_LIMIT && !isGenerating.value) {
                    isGenerating.value = true
                    val messagesToProcessForLore =
                        messagesSinceLastLore
                            .filter { it.message.senderType != SenderType.NEW_CHAPTER }
                            .take(UpdateRules.LORE_UPDATE_LIMIT)
                            .reversed()

                    val loreReferenceForUpdate = allMessages.firstOrNull()?.message

                    if (loreReferenceForUpdate != null && messagesToProcessForLore.isNotEmpty()) {
                        sagaContentManager
                            .updateLore(
                                reference = loreReferenceForUpdate,
                                messageSubList = messagesToProcessForLore,
                            ).onSuccess { newEvent ->
                                notifyLoreUpdate(newEvent)
                            }.onFailure {
                                isGenerating.value = false
                                sendError("Ocorreu um erro ao atualizar a história.")
                            }
                    } else {
                        isGenerating.value = false
                    }
                } else {
                    if (sagaContentManager.isInDebugMode()) {
                        sendSnackbarMessage(
                            SnackBarState(
                                "Not enough messages(${messagesSinceLastLore.size}) to update the lore",
                                "",
                            ),
                        )
                    }
                }
            }
        }

        private fun notifyLoreUpdate(newEvent: Timeline) {
            isGenerating.value = false
            viewModelScope.launch(Dispatchers.IO) {
                sendSnackbarMessage(
                    SnackBarState(
                        "História atualizada",
                        newEvent.content,
                        redirectAction = Triple(ChatAction.OPEN_TIMELINE, "Ver mais", newEvent.id),
                    ),
                )
                delay(20.seconds)
                snackBarMessage.value = null
            }
        }

        private fun lastEvents(): List<Timeline> {
            val saga = content.value ?: return emptyList()
            if (sagaContentManager.isInDebugMode()) return emptyList()
            return saga.timelines.sortedBy { it.createdAt }.takeLast(UpdateRules.CHAPTER_UPDATE_LIMIT)
        }

        private fun replyMessage(message: Message) {
            isGenerating.value = true
            viewModelScope.launch(Dispatchers.IO) {
                val saga = content.value ?: return@launch
                val characters = content.value!!.characters
                val directive = sagaContentManager.getDirective()
                val newMessage =
                    MessageContent(
                        message = message,
                        character = characters.find { it.id == message.characterId },
                    )

                messageUseCase
                    .generateMessage(
                        saga = saga,
                        chapter = content.value?.chapters?.lastOrNull(),
                        message = newMessage.joinMessage(),
                        lastEvents = lastEvents(),
                        directive = directive,
                        lastMessages =
                            messages
                                .value
                                .reversed()
                                .takeLast(UpdateRules.LORE_UPDATE_LIMIT)
                                .map { m ->
                                    m.joinMessage()
                                },
                    ).onSuccess { genMessage ->
                        if (genMessage.shouldCreateCharacter && genMessage.newCharacter != null) {
                            createCharacter(genMessage.newCharacter)
                            sendMessage(
                                genMessage.message.copy(
                                    chapterId = null,
                                    actId = null,
                                ),
                            )
                        } else {
                            sendMessage(
                                genMessage.message.copy(
                                    chapterId = null,
                                    actId = null,
                                ),
                            )
                        }
                        if (genMessage.shouldEndSaga) {
                            // endSaga()
                        }
                        isGenerating.value = false
                    }.onFailure {
                        sendError(it.message ?: "Unknown error")
                        isGenerating.value = false
                    }
            }
        }

        private fun endSaga() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaContentManager.endSaga()
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
            val mainCharacterId = currentSaga.mainCharacter?.id
            if (mainCharacterId == null && currentSaga.data.id != 0) { // Allow no main character for true debug saga
                sendError("Main character not found for non-debug saga, cannot send fake messages.")
                return
            }

            viewModelScope.launch(Dispatchers.IO) {
                isGenerating.value = true
                sagaContentManager.setProcessing(true)
                repeat(count) {
                    if (it == count - 1) {
                        isGenerating.value = false
                        sagaContentManager.setProcessing(false)
                    }
                    val fakeUserMessage =
                        Message(
                            text = "Fake Message #${it + 1} of $count",
                            senderType = SenderType.USER,
                            characterId = mainCharacterId,
                            sagaId = currentSaga.data.id,
                        )
                    sendMessage(fakeUserMessage, isFromUser = false)
                    delay(100)
                }

                Log.d("ChatViewModel", "[DEBUG] Finished enqueuing $count fake messages.")
                checkLoreUpdate()
            }
        }
    }
