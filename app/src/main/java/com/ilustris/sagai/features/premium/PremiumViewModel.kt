package com.ilustris.sagai.features.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.analytics.AnalyticsService
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel
    @Inject
    constructor(
        private val billingService: BillingService,
        private val analyticsService: AnalyticsService,
        private val genreVisualConfigService: GenreVisualConfigService,
    ) : ViewModel() {
        private val _billingState = MutableStateFlow<BillingService.BillingState?>(null)
        val billingState: StateFlow<BillingService.BillingState?> = _billingState.asStateFlow()

        private val _genres = MutableStateFlow<List<Pair<Genre, GenreVisualConfig?>>>(emptyList())
        val genres = _genres.asStateFlow()

        init {
            viewModelScope.launch {
                fetchGenres()
                billingService.checkPurchases()
                billingService.state.collect {
                    _billingState.value = it
                }
            }
        }

        fun fetchGenres() {
            viewModelScope.launch {
                _genres.value =
                    Genre.entries.map {
                        it to genreVisualConfigService.getVisualConfig(it)
            }
        }
    }

        fun purchaseSignature(
            activity: Activity,
            productDetails: ProductDetails,
            offerToken: String,
        ) {
            // Track premium click event
            analyticsService.trackEvent(
                com.ilustris.sagai.core.analytics.PremiumClickEvent(
                    source = "premium_view",
                ),
            )

            viewModelScope.launch {
                billingService.purchaseSignature(activity as com.ilustris.sagai.MainActivity, productDetails, offerToken)
            }
        }

        fun restorePurchases() {
            // Track restore purchases click
            analyticsService.trackEvent(
                com.ilustris.sagai.core.analytics.PremiumClickEvent(
                    source = "restore_purchases",
                ),
            )

            viewModelScope.launch {
                billingService.loadSignatureProduct()
            }
        }

        fun cancelSubscription() {
            // Track cancel subscription click
            analyticsService.trackEvent(
                com.ilustris.sagai.core.analytics.PremiumClickEvent(
                    source = "cancel_subscription",
                ),
            )

            viewModelScope.launch {
                // TODO implement cancel subscription
            }
        }
    }
