package com.ilustris.sagai.features.premium.data

import com.ilustris.sagai.core.services.BillingService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * What the plan list should show right now.
 *
 * There is no stand-in for a failed load, in any build. A plan invented on the device is a price
 * someone might act on, and debug builds cannot complete a purchase anyway: Play refuses an APK
 * signed with the debug keystore, so a fake plan there would only be a button that lies. Both
 * builds run the same path and both show [Unavailable] when Play has nothing to give.
 */
sealed class PremiumPlansState {
    object Loading : PremiumPlansState()

    data class Available(
        val plans: List<PremiumPlan>,
    ) : PremiumPlansState()

    object Unavailable : PremiumPlansState()
}

@Singleton
class PremiumPlansSource
    @Inject
    constructor(
        private val mapper: BillingPlanMapper,
        private val billingService: BillingService,
    ) {
        suspend fun plansFor(state: BillingService.BillingState): PremiumPlansState =
            when (state) {
                is BillingService.BillingState.SignatureDisabled -> {
                    mapper
                        .map(state.products, billingService.catalogEntries())
                        .let { plans ->
                            if (plans.isEmpty()) {
                                PremiumPlansState.Unavailable
                            } else {
                                PremiumPlansState.Available(plans)
                            }
                        }
                }

                is BillingService.BillingState.BillingError -> PremiumPlansState.Unavailable

                else -> PremiumPlansState.Loading
            }
    }
