package com.ilustris.sagai.core.services

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Wraps the User Messaging Platform (UMP) SDK's consent flow — required for GDPR/UK
 * compliance before requesting personalized ads from EEA/UK users. Fails open on any error:
 * a consent-info failure just leaves [canRequestAds] false, which [AdsService] already treats
 * as "don't load/show an ad" rather than a crash.
 */
class AdsConsentService
    @Inject
    constructor(
        private val context: Context,
    ) {
        private val consentInformation: ConsentInformation by lazy {
            UserMessagingPlatform.getConsentInformation(context)
        }

        suspend fun requestConsentIfNeeded(activity: Activity) {
            val params = ConsentRequestParameters.Builder().build()
            val infoUpdated =
                suspendCancellableCoroutine { continuation ->
                    consentInformation.requestConsentInfoUpdate(
                        activity,
                        params,
                        { if (continuation.isActive) continuation.resume(true) },
                        { error ->
                            Timber.tag("AdsConsentService").d("Consent info update failed: ${error.message}")
                            if (continuation.isActive) continuation.resume(false)
                        },
                    )
                }
            if (!infoUpdated || !consentInformation.isConsentFormAvailable) return
            loadAndShowFormIfRequired(activity)
        }

        fun canRequestAds(): Boolean = consentInformation.canRequestAds()

        private suspend fun loadAndShowFormIfRequired(activity: Activity) {
            suspendCancellableCoroutine<Unit> { continuation ->
                UserMessagingPlatform.loadConsentForm(
                    context,
                    { form ->
                        if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                            form.show(activity) {
                                if (continuation.isActive) continuation.resume(Unit)
                            }
                        } else if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    },
                    { error ->
                        Timber.tag("AdsConsentService").d("Consent form load failed: ${error.message}")
                        if (continuation.isActive) continuation.resume(Unit)
                    },
                )
            }
        }
    }
