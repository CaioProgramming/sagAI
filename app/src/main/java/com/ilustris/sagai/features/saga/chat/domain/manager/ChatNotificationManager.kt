package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent

interface ChatNotificationManager {
    fun sendMessageNotification(
        saga: SagaContent,
        message: MessageContent,
    )
}
