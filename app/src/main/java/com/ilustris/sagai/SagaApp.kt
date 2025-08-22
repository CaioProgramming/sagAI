package com.ilustris.sagai

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.ilustris.sagai.core.utils.NotificationUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SagaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        fetchRemoteConfig()
        NotificationUtils.createChatNotificationChannel(this)
        NotificationUtils.createMediaNotificationChannel(this) // Added media channel creation
    }

    private fun fetchRemoteConfig() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings =
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
    }
}
