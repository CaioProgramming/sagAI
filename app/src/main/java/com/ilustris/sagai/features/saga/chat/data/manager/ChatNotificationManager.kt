package com.ilustris.sagai.features.saga.chat.domain.manager

import android.graphics.Bitmap
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent

interface ChatNotificationManager {
    fun sendMessageNotification(
        saga: SagaContent,
        message: MessageContent,
    )

    fun sendNotification(
        saga: SagaContent,
        title: String,
        content: String,
        smalIcon: Bitmap?,
        largeIcon: Bitmap?,
    )

    fun clearNotifications()
}
