package com.ilustris.sagai

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.utils.NotificationUtils
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SagaApp : Application() {
    @Inject
    lateinit var billingService: BillingService

    override fun onCreate() {
        super.onCreate()
        fetchRemoteConfig()
        NotificationUtils.createChatNotificationChannel(this)
        NotificationUtils.createMediaNotificationChannel(this)
        CoroutineScope(Dispatchers.IO).launch {
            billingService.checkPurchases()
        }
    }

    private fun fetchRemoteConfig() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings =
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3000
            }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
    }
}
