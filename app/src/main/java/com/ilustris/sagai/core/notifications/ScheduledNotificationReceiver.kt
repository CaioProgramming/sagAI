package com.ilustris.sagai.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.features.saga.chat.data.manager.ChatNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScheduledNotificationReceiver : BroadcastReceiver() {
    @Inject
    lateinit var chatNotificationManager: ChatNotificationManager

    @Inject
    lateinit var dataStore: DataStorePreferences

    @Inject
    lateinit var sagaRepository: SagaDatabase

    @Inject
    lateinit var fileHelper: FileHelper

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == "com.ilustris.sagai.SCHEDULED_NOTIFICATION") {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val json =
                        dataStore
                            .getString(
                                ScheduledNotificationServiceImpl.SCHEDULED_NOTIFICATION_JSON_KEY,
                                "",
                            ).first()
                            .ifEmpty { null }

                    if (json != null) {
                        Log.d(javaClass.simpleName, "onReceive: Receiving notification $json")
                        val notification = Gson().fromJson(json, ScheduledNotification::class.java)
                        val saga =
                            sagaRepository.sagaDao().getSaga(notification.sagaId.toInt()).first()!!
                        val characterBitmap = fileHelper.readFile(notification.characterAvatarPath)

                        chatNotificationManager.sendNotification(
                            saga = saga,
                            title = notification.characterName,
                            content = notification.generatedMessage,
                            smallIcon = characterBitmap,
                            largeIcon = characterBitmap,
                        )
                        dataStore.removeKey(
                            ScheduledNotificationServiceImpl.SCHEDULED_NOTIFICATION_JSON_KEY,
                        )
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
