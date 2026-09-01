package com.ilustris.sagai.features.premium.data

import com.android.billingclient.api.ProductDetails
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.StringResourceHelper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns what Play returns into plans this app can show.
 *
 * All the reading of Google's shapes lives here, so the screen never has to know that a price can
 * arrive as three phases or none at all. Prices and names come back already localised to the
 * buyer's account, so nothing here translates them; only the period and the trial wording are ours.
 */
@Singleton
class BillingPlanMapper
    @Inject
    constructor(
        private val strings: StringResourceHelper,
    ) {
        fun map(products: List<ProductDetails>): List<PremiumPlan> = products.mapNotNull(::mapProduct)

        private fun mapProduct(product: ProductDetails): PremiumPlan? {
            product.oneTimePurchaseOfferDetails?.let { oneTime ->
                return PremiumPlan(
                    productId = product.productId,
                    offerToken = oneTime.offerToken,
                    name = product.name,
                    description = product.description,
                    // A one-time purchase has no phases and no period: the price is the whole story.
                    priceLine = oneTime.formattedPrice,
                    isOneTime = true,
                )
            }

            val offer = product.subscriptionOfferDetails?.firstOrNull() ?: return null
            val phases = offer.pricingPhases.pricingPhaseList
            // The last paid phase is what the buyer pays from then on. Counting phases instead
            // would break on a promotional price, where three of them exist and only the last is
            // the steady one.
            val steady = phases.lastOrNull { it.priceAmountMicros > 0 } ?: return null

            return PremiumPlan(
                productId = product.productId,
                offerToken = offer.offerToken,
                // `title` arrives with the app name appended in brackets; `name` does not.
                name = product.name,
                description = product.description,
                priceLine = buildPriceLine(phases, steady),
                isOneTime = false,
            )
        }

        private fun buildPriceLine(
            phases: List<ProductDetails.PricingPhase>,
            steady: ProductDetails.PricingPhase,
        ): String {
            val steadyLine =
                strings.getString(
                    R.string.plan_price_period,
                    steady.formattedPrice,
                    periodLabel(steady.billingPeriod),
                )

            phases.firstOrNull { it.priceAmountMicros == 0L }?.let { trial ->
                return strings.getString(
                    R.string.plan_trial_then,
                    durationLabel(trial.billingPeriod, trial.billingCycleCount),
                    steadyLine,
                )
            }

            phases.firstOrNull { it.priceAmountMicros > 0 && it !== steady }?.let { intro ->
                val introLine =
                    strings.getString(
                        R.string.plan_price_period,
                        intro.formattedPrice,
                        periodLabel(intro.billingPeriod),
                    )
                return strings.getString(
                    R.string.plan_intro_then,
                    introLine,
                    durationLabel(intro.billingPeriod, intro.billingCycleCount),
                    steadyLine,
                )
            }

            return steadyLine
        }

        /** "P1M" becomes "month": the unit alone, for prices written as "R$ 23,90/month". */
        private fun periodLabel(billingPeriod: String?): String {
            val (_, unit) = parsePeriod(billingPeriod) ?: return ""
            return strings.getString(
                when (unit) {
                    'D' -> R.string.plan_period_day
                    'W' -> R.string.plan_period_week
                    'Y' -> R.string.plan_period_year
                    else -> R.string.plan_period_month
                },
            )
        }

        /**
         * How long a phase lasts, counted rather than named: "3 months".
         *
         * A trial of three months can be configured either as one P3M phase or as a P1M phase
         * repeated three times, so the cycle count has to be folded in or the second form would
         * read as one month.
         */
        private fun durationLabel(
            billingPeriod: String?,
            billingCycleCount: Int,
        ): String {
            val (amount, unit) = parsePeriod(billingPeriod) ?: return ""
            val total = amount * billingCycleCount.coerceAtLeast(1)
            return strings.getQuantityString(
                when (unit) {
                    'D' -> R.plurals.plan_duration_days
                    'W' -> R.plurals.plan_duration_weeks
                    'Y' -> R.plurals.plan_duration_years
                    else -> R.plurals.plan_duration_months
                },
                total,
                total,
            )
        }

        /** ISO-8601 durations, which is how Play states every billing period. */
        private fun parsePeriod(billingPeriod: String?): Pair<Int, Char>? {
            val match = PERIOD_PATTERN.find(billingPeriod.orEmpty()) ?: return null
            val amount = match.groupValues[1].toIntOrNull() ?: return null
            return amount to match.groupValues[2].first()
        }

        private companion object {
            val PERIOD_PATTERN = Regex("""P(\d+)([DWMY])""")
        }
    }
