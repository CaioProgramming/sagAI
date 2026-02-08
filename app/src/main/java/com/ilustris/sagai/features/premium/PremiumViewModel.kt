package com.ilustris.sagai.features.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.ilustris.sagai.core.services.BillingService
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
        private val analyticsService: com.ilustris.sagai.core.analytics.AnalyticsService,
    ) : ViewModel() {
        private val _billingState = MutableStateFlow<BillingService.BillingState?>(null)
        val billingState: StateFlow<BillingService.BillingState?> = _billingState.asStateFlow()

        init {
            viewModelScope.launch {
                billingService.checkPurchases()
                billingService.state.collect {
                    _billingState.value = it
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
