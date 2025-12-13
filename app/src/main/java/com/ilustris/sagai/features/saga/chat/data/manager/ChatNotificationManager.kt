package com.ilustris.sagai.features.saga.chat.data.manager

import android.graphics.Bitmap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent

interface ChatNotificationManager {
    fun sendMessageNotification(
        saga: SagaContent,
        message: MessageContent,
    )

    fun sendNotification(
        saga: Saga,
        title: String,
        content: String,
        smallIcon: Bitmap?,
        largeIcon: Bitmap?,
    )

    fun clearNotifications()

    fun sendChatNotification(
        saga: Saga,
        title: String,
        character: Character?,
        message: String,
        largeIcon: Bitmap?,
    )
}
