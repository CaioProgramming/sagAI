package com.ilustris.sagai.features.premium.data

import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.services.BillingService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * What the plan list should show right now.
 *
 * Release only ever shows what Play returned. When Play cannot be reached there is [Unavailable]
 * and nothing else: a plan invented on the device is a price the buyer might act on, and inventing
 * one is the sort of thing an app has no business doing with money.
 */
sealed class PremiumPlansState {
    object Loading : PremiumPlansState()

    data class Available(
        val plans: List<PremiumPlan>,
        /** True when these came from the debug stand-in rather than from Play. */
        val isSample: Boolean = false,
    ) : PremiumPlansState()

    object Unavailable : PremiumPlansState()
}

@Singleton
class PremiumPlansSource
    @Inject
    constructor(
        private val mapper: BillingPlanMapper,
    ) {
        fun plansFor(state: BillingService.BillingState): PremiumPlansState =
            when (state) {
                is BillingService.BillingState.SignatureDisabled -> {
                    mapper.map(state.products).let { plans ->
                        if (plans.isEmpty()) sampleOrUnavailable() else PremiumPlansState.Available(plans)
                    }
                }

                is BillingService.BillingState.BillingError -> sampleOrUnavailable()

                else -> PremiumPlansState.Loading
            }

        /**
         * Debug builds get stand-in plans so the screen can be built and reviewed before the
         * products exist in Play Console.
         *
         * Reached only where Play itself came back empty or broken, never in front of a real
         * result, so a misconfigured product can never be masked by a plausible-looking fake. The
         * BuildConfig check is what keeps it out of release; the placement is what keeps it honest
         * in debug.
         */
        private fun sampleOrUnavailable(): PremiumPlansState =
            if (BuildConfig.DEBUG) {
                PremiumPlansState.Available(SAMPLE_PLANS, isSample = true)
            } else {
                PremiumPlansState.Unavailable
            }

        private companion object {
            /** Covers the shapes the mapper has to survive: plain, trial, and one-time. */
            val SAMPLE_PLANS =
                listOf(
                    PremiumPlan(
                        productId = "debug_monthly",
                        offerToken = null,
                        name = "Mensal",
                        description = "Sem anúncios enquanto a assinatura estiver ativa.",
                        priceLine = "R$ 9,90/mês",
                        isOneTime = false,
                    ),
                    PremiumPlan(
                        productId = "debug_yearly",
                        offerToken = null,
                        name = "Anual",
                        description = "Sem anúncios o ano inteiro.",
                        priceLine = "1 mês grátis, depois R$ 79,90/ano",
                        isOneTime = false,
                    ),
                    PremiumPlan(
                        productId = "debug_lifetime",
                        offerToken = null,
                        name = "Para sempre",
                        description = "Paga uma vez e nunca mais vê anúncio.",
                        priceLine = "R$ 149,90",
                        isOneTime = true,
                    ),
                )
        }
    }
