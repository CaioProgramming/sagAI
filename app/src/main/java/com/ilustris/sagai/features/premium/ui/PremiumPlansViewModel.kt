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
                    billingState?.let { plansSource.plansFor(it) } ?: PremiumPlansState.Loading
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = PremiumPlansState.Loading,
                )

        // Keyed on the plan, not the product: one subscription product sells several base
        // plans, so selecting by product id would light up monthly and yearly at once.
        private val _selectedKey = MutableStateFlow<String?>(null)
        val selectedKey: StateFlow<String?> = _selectedKey.asStateFlow()

        private val _isPurchasing = MutableStateFlow(false)
        val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

        fun select(plan: PremiumPlan) {
            _selectedKey.value = plan.key
        }

        fun purchase(
            plan: PremiumPlan,
            activity: MainActivity?,
        ) {
            val product = rawProductFor(plan) ?: return
            // Both kinds carry a token now: a one-time product's purchase option has one just as
            // a base plan does. Without it Play cannot tell which option is being bought.
            val offerToken = plan.offerToken ?: return

            viewModelScope.launch {
                _isPurchasing.value = true
                billingService.resetPurchaseFlowResult()
                val launched =
                    activity?.let { billingService.purchase(it, product, offerToken) }
                if (launched != true) _isPurchasing.value = false
            }
        }

        /**
         * The [ProductDetails] behind a plan, looked up rather than carried in the model.
         *
         * The billing flow needs Google's own object, but letting the UI hold one is what made the
         * old screen impossible to exercise without Play, so it stays on this side of the line.
         */
        private fun rawProductFor(plan: PremiumPlan): ProductDetails? =
            (billingService.state.value as? BillingService.BillingState.SignatureDisabled)
                ?.products
                ?.firstOrNull { it.productId == plan.productId }
    }
