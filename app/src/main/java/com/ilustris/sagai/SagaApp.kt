package com.ilustris.sagai

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.mlkit.common.sdkinternal.MlKitContext
import com.ilustris.sagai.core.error.SagasExceptionHandler
import com.ilustris.sagai.core.permissions.NotificationUtils
import com.ilustris.sagai.core.services.BillingService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
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
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(SagasExceptionHandler(this, defaultHandler))
        FirebaseApp.initializeApp(this)
        MlKitContext.initializeIfNeeded(this)
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        if (!isSOSProcess()) {
            fetchRemoteConfig()
            NotificationUtils.createChatNotificationChannel(this)
            CoroutineScope(Dispatchers.IO).launch {
                billingService.checkPurchases()
            }
        }
    }

    private fun isSOSProcess(): Boolean {
        val processName =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getProcessName()
            } else {
                val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
                am.runningAppProcesses?.find { it.pid == android.os.Process.myPid() }?.processName
            }
        return processName?.endsWith(":sos") == true
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
            },
        )
    }
}
