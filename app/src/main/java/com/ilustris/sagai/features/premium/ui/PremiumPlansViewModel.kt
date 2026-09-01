package com.ilustris.sagai.features.premium.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.features.premium.data.PremiumPlan
import com.ilustris.sagai.features.premium.data.PremiumPlansSource
import com.ilustris.sagai.features.premium.data.PremiumPlansState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumPlansViewModel
    @Inject
    constructor(
        private val billingService: BillingService,
        private val plansSource: PremiumPlansSource,
    ) : ViewModel() {
        val state: StateFlow<PremiumPlansState> =
            billingService.state
                // Null until the first query answers, which is loading rather than unavailable.
                .map { billingState ->
                    billingState?.let(plansSource::plansFor) ?: PremiumPlansState.Loading
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = PremiumPlansState.Loading,
                )

        private val _selectedProductId = MutableStateFlow<String?>(null)
        val selectedProductId: StateFlow<String?> = _selectedProductId.asStateFlow()

        private val _isPurchasing = MutableStateFlow(false)
        val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

        fun select(plan: PremiumPlan) {
            _selectedProductId.value = plan.productId
        }

        fun purchase(
            plan: PremiumPlan,
            activity: MainActivity?,
        ) {
            val product = rawProductFor(plan.productId) ?: return
            val offerToken =
                plan.offerToken
                    ?: product.subscriptionOfferDetails?.firstOrNull()?.offerToken
                    ?: return

            viewModelScope.launch {
                _isPurchasing.value = true
                billingService.resetPurchaseFlowResult()
                val launched =
                    activity?.let { billingService.purchaseSignature(it, product, offerToken) }
                if (launched != true) _isPurchasing.value = false
            }
        }

        /**
         * The [ProductDetails] behind a plan, looked up rather than carried in the model.
         *
         * The billing flow needs Google's own object, but letting the UI hold one is what made the
         * old screen impossible to exercise without Play, so it stays on this side of the line.
         */
        private fun rawProductFor(productId: String): ProductDetails? =
            (billingService.state.value as? BillingService.BillingState.SignatureDisabled)
                ?.products
                ?.firstOrNull { it.productId == productId }
    }
