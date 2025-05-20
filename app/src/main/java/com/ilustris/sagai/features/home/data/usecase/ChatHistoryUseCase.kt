package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.features.home.data.model.ChatData
import kotlinx.coroutines.flow.Flow

interface ChatHistoryUseCase {
    fun getChats(): Flow<List<ChatData>>
}