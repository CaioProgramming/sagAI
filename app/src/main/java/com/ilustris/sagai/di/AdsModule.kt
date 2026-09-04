package com.ilustris.sagai.di

import android.content.Context
import com.ilustris.sagai.core.analytics.AnalyticsService
import com.ilustris.sagai.core.services.AdsConsentService
import com.ilustris.sagai.core.services.AdsService
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.services.RemoteConfigService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdsModule {
    @Provides
    @Singleton
    fun provideAdsConsentService(
        @ApplicationContext context: Context,
    ): AdsConsentService = AdsConsentService(context)

    @Provides
    @Singleton
    fun provideAdsService(
        @ApplicationContext context: Context,
        remoteConfigService: RemoteConfigService,
        billingService: BillingService,
        analyticsService: AnalyticsService,
        consentService: AdsConsentService,
    ): AdsService = AdsService(context, remoteConfigService, billingService, analyticsService, consentService)
}
