package com.ilustris.sagai.features.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.services.BillingState
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
    ) : ViewModel() {
        private val _billingState = MutableStateFlow<BillingState?>(null)
        val billingState: StateFlow<BillingState?> = _billingState.asStateFlow()

        init {
            viewModelScope.launch {
                billingService.loadSignatureProduct()
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
            viewModelScope.launch {
                billingService.purchaseSignature(activity as com.ilustris.sagai.MainActivity, productDetails, offerToken)
            }
        }

        fun restorePurchases() {
            viewModelScope.launch {
                billingService.loadSignatureProduct()
            }
        }
    }
