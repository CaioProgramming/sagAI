package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.features.home.data.model.ChatData

interface NewSagaUseCase {
    suspend fun saveSaga(chatData: ChatData)
}
