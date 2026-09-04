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

        /**
         * Why the last attempt did not even reach Play.
         *
         * These two cases used to be bare returns, which left the confirm button looking like it
         * had simply ignored the tap. They mean the catalogue and Play disagree about what this
         * plan is, and the only way anyone finds that out is if the screen says so.
         */
        private val _localError = MutableStateFlow<String?>(null)
        val localError: StateFlow<String?> = _localError.asStateFlow()

        fun select(plan: PremiumPlan) {
            _selectedKey.value = plan.key
            _localError.value = null
        }

        init {
            // Play's flow ends in the listener, not in the call that opened it, so the button
            // would otherwise spin forever on a cancelled or failed purchase.
            viewModelScope.launch {
                billingService.purchaseFlowResult.collect { result ->
                    if (result != BillingService.PurchaseFlowResult.Idle) {
                        _isPurchasing.value = false
                    }
                    if (result is BillingService.PurchaseFlowResult.Error) {
                        _localError.value = result.message
                    }
                }
            }
        }

        fun purchase(
            plan: PremiumPlan,
            activity: MainActivity?,
        ) {
            _localError.value = null
            val product =
                rawProductFor(plan) ?: run {
                    _localError.value = "Product ${plan.productId} is not in Play's answer."
                    return
                }
            // Both kinds carry a token now: a one-time product's purchase option has one just as
            // a base plan does. Without it Play cannot tell which option is being bought.
            val offerToken =
                plan.offerToken ?: run {
                    _localError.value = "No offer for ${plan.productId}/${plan.optionId.orEmpty()}."
                    return
                }

            if (activity == null) {
                _localError.value = "Could not reach the activity to open Play's payment sheet."
                return
            }

            viewModelScope.launch {
                _isPurchasing.value = true
                billingService.resetPurchaseFlowResult()
                // A false here means Play refused to open the sheet at all; the reason has already
                // gone through purchaseFlowResult, which this ViewModel is collecting.
                if (!billingService.purchase(activity, product, offerToken)) {
                    _isPurchasing.value = false
                }
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
