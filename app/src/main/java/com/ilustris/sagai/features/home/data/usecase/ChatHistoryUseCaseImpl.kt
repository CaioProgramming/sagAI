package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.features.chat.repository.ChatRepository
import com.ilustris.sagai.features.home.data.model.ChatData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatHistoryUseCaseImpl @Inject constructor(private val chatRepository: ChatRepository) : ChatHistoryUseCase {
    override fun getChats(): Flow<List<ChatData>> = chatRepository.getChats()
}