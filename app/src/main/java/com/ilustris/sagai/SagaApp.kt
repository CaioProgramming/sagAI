package com.ilustris.sagai

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.ilustris.sagai.core.permissions.NotificationUtils
import com.ilustris.sagai.core.services.BillingService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SagaApp :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var billingService: BillingService

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        fetchRemoteConfig()
        NotificationUtils.createChatNotificationChannel(this)
        NotificationUtils.createMediaNotificationChannel(this)
        CoroutineScope(Dispatchers.IO).launch {
            billingService.checkPurchases()
        }
    }

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .build()

    private fun fetchRemoteConfig() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings =
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 60 * 3
            }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
        remoteConfig.addOnConfigUpdateListener(
            object :
                com.google.firebase.remoteconfig.ConfigUpdateListener {
                override fun onUpdate(configUpdate: com.google.firebase.remoteconfig.ConfigUpdate) {
                    remoteConfig.activate()
                }

                override fun onError(error: com.google.firebase.remoteconfig.FirebaseRemoteConfigException) {
                    // Log error if needed
            }
        }
                )
    }
}
