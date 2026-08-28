package com.ilustris.sagai.core.services

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.analytics.AdClickedEvent
import com.ilustris.sagai.core.analytics.AdDismissedEvent
import com.ilustris.sagai.core.analytics.AdFailedToLoadEvent
import com.ilustris.sagai.core.analytics.AdFailedToShowEvent
import com.ilustris.sagai.core.analytics.AdLoadedEvent
import com.ilustris.sagai.core.analytics.AdRequestedEvent
import com.ilustris.sagai.core.analytics.AdShownEvent
import com.ilustris.sagai.core.analytics.AnalyticsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Which closure milestone an ad is tied to. Event closures are the frequent, low-severity beat
 * (a standard interstitial, closable almost immediately); chapter/act closures are rarer and get
 * a [RewardedInterstitialAd] instead — used purely for its forced-video/delayed-close format, no
 * actual reward is granted since there's no in-app currency to give.
 */
enum class AdTier { EVENT, CHAPTER_OR_ACT }

/**
 * Wraps interstitial + rewarded-interstitial ad lifecycle for the narrative milestone screen.
 * Mirrors [BillingService]'s shape: a Hilt singleton, premium/remote-config/consent gated,
 * fails open everywhere (a missing/unloaded/disallowed ad never blocks narrative progression).
 */
class AdsService
    @Inject
    constructor(
        private val context: Context,
        private val remoteConfigService: RemoteConfigService,
        private val billingService: BillingService,
        private val analyticsService: AnalyticsService,
        private val consentService: AdsConsentService,
    ) {
        private var interstitialAd: InterstitialAd? = null
        private var rewardedInterstitialAd: RewardedInterstitialAd? = null

        // Guards show() calls only — the real stacking/invalid-traffic risk is two ad surfaces
        // shown in immediate succession, not two independent ad objects loading in parallel.
        private val showInProgress = MutableStateFlow(false)

        private var currentActivity: Activity? = null

        init {
            (context as? Application)?.registerActivityLifecycleCallbacks(
                object : Application.ActivityLifecycleCallbacks {
                    override fun onActivityResumed(activity: Activity) {
                        currentActivity = activity
                    }

                    override fun onActivityPaused(activity: Activity) {
                        if (currentActivity == activity) currentActivity = null
                    }

                    override fun onActivityCreated(
                        activity: Activity,
                        savedInstanceState: Bundle?,
                    ) = Unit

                    override fun onActivityStarted(activity: Activity) = Unit

                    override fun onActivityStopped(activity: Activity) = Unit

                    override fun onActivitySaveInstanceState(
                        activity: Activity,
                        outState: Bundle,
                    ) = Unit

                    override fun onActivityDestroyed(activity: Activity) = Unit
                },
            )
        }

        suspend fun preload(tier: AdTier) {
            if (!canServeAds()) return
            when (tier) {
                AdTier.EVENT -> preloadInterstitial()
                AdTier.CHAPTER_OR_ACT -> preloadRewardedInterstitial()
            }
        }

        suspend fun showIfReady(
            tier: AdTier,
            onDismissed: () -> Unit,
        ) {
            if (!canServeAds()) {
                onDismissed()
                return
            }
            performShow(tier, onDismissed)
        }

        /**
         * Debug-only escape hatch for a manual "test this ad" button (e.g. in Settings' debug
         * section): loads (awaiting completion, so the button's own loading state has something
         * real to show) and shows a tier immediately, bypassing premium/remote-config/consent
         * gating entirely — a developer verifying unit IDs work shouldn't first have to fight
         * their own premium test account or the `ads_enabled` flag. No-ops outside debug builds.
         */
        suspend fun debugShowTestAd(
            tier: AdTier,
            onDismissed: () -> Unit,
        ) {
            if (!BuildConfig.DEBUG) {
                onDismissed()
                return
            }
            when (tier) {
                AdTier.EVENT -> if (interstitialAd == null) preloadInterstitial()
                AdTier.CHAPTER_OR_ACT -> if (rewardedInterstitialAd == null) preloadRewardedInterstitial()
            }
            performShow(tier, onDismissed)
        }

        private fun performShow(
            tier: AdTier,
            onDismissed: () -> Unit,
        ) {
            val activity = currentActivity
            if (showInProgress.value || activity == null) {
                onDismissed()
                return
            }
            when (tier) {
                AdTier.EVENT -> {
                    val ad = interstitialAd
                    if (ad == null) {
                        onDismissed()
                        return
                    }
                    showInProgress.value = true
                    ad.fullScreenContentCallback = fullScreenCallback(tier) { interstitialAd = null }
                    ad.show(activity)
                }

                AdTier.CHAPTER_OR_ACT -> {
                    val ad = rewardedInterstitialAd
                    if (ad == null) {
                        onDismissed()
                        return
                    }
                    showInProgress.value = true
                    ad.fullScreenContentCallback = fullScreenCallback(tier) { rewardedInterstitialAd = null }
                    // No in-app currency to grant — the format is used purely for its
                    // forced-video/delayed-close UX, so the reward callback is a no-op.
                    ad.show(activity, OnUserEarnedRewardListener { })
                }
            }
            pendingDismissed = onDismissed
        }

        private var pendingDismissed: (() -> Unit)? = null

        private fun fullScreenCallback(
            tier: AdTier,
            clearAd: () -> Unit,
        ) = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                analyticsService.trackEvent(AdShownEvent(tier.name))
            }

            override fun onAdClicked() {
                analyticsService.trackEvent(AdClickedEvent(tier.name))
            }

            override fun onAdDismissedFullScreenContent() {
                clearAd()
                analyticsService.trackEvent(AdDismissedEvent(tier.name))
                finishShow()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                clearAd()
                analyticsService.trackEvent(AdFailedToShowEvent(error.code, error.message))
                finishShow()
            }
        }

        private fun finishShow() {
            showInProgress.value = false
            val callback = pendingDismissed
            pendingDismissed = null
            callback?.invoke()
        }

        private suspend fun canServeAds(): Boolean =
            !billingService.isPremium() &&
                remoteConfigService.getBoolean(ADS_ENABLED_FLAG) == true &&
                consentService.canRequestAds()

        private suspend fun preloadInterstitial() {
            if (interstitialAd != null) return
            analyticsService.trackEvent(AdRequestedEvent(AdTier.EVENT.name))
            suspendCancellableCoroutine<Unit> { continuation ->
                InterstitialAd.load(
                    context,
                    interstitialUnitId(),
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            interstitialAd = ad
                            analyticsService.trackEvent(AdLoadedEvent(AdTier.EVENT.name))
                            if (continuation.isActive) continuation.resume(Unit)
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            interstitialAd = null
                            Timber.tag("AdsService").d("Interstitial failed to load: ${error.message}")
                            analyticsService.trackEvent(AdFailedToLoadEvent(error.code, error.message))
                            if (continuation.isActive) continuation.resume(Unit)
                        }
                    },
                )
            }
        }

        private suspend fun preloadRewardedInterstitial() {
            if (rewardedInterstitialAd != null) return
            analyticsService.trackEvent(AdRequestedEvent(AdTier.CHAPTER_OR_ACT.name))
            suspendCancellableCoroutine<Unit> { continuation ->
                RewardedInterstitialAd.load(
                    context,
                    rewardedInterstitialUnitId(),
                    AdRequest.Builder().build(),
                    object : RewardedInterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedInterstitialAd) {
                            rewardedInterstitialAd = ad
                            analyticsService.trackEvent(AdLoadedEvent(AdTier.CHAPTER_OR_ACT.name))
                            if (continuation.isActive) continuation.resume(Unit)
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            rewardedInterstitialAd = null
                            Timber.tag("AdsService").d("Rewarded interstitial failed to load: ${error.message}")
                            analyticsService.trackEvent(AdFailedToLoadEvent(error.code, error.message))
                            if (continuation.isActive) continuation.resume(Unit)
                        }
                    },
                )
            }
        }

        private fun interstitialUnitId() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_UNIT_ID else PROD_INTERSTITIAL_UNIT_ID

        private fun rewardedInterstitialUnitId() =
            if (BuildConfig.DEBUG) TEST_REWARDED_INTERSTITIAL_UNIT_ID else PROD_REWARDED_INTERSTITIAL_UNIT_ID

        companion object {
            const val ADS_ENABLED_FLAG = "ads_enabled"

            // Google's official public test ad units — safe to ship in debug builds, never in release.
            private const val TEST_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
            private const val TEST_REWARDED_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"

            private const val PROD_INTERSTITIAL_UNIT_ID = "ca-app-pub-8016530757684172/5129807208"
            private const val PROD_REWARDED_INTERSTITIAL_UNIT_ID = "ca-app-pub-8016530757684172/8428712363"
        }
    }
