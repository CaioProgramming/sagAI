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
        /**
         * Builds one plan per catalogue entry, in the catalogue's own order.
         *
         * Driven by the configured entries rather than by what Play returned, because one
         * subscription product can sell several base plans and the product alone no longer says
         * which card is which. Iterating the config also fixes the order, which Play does not
         * guarantee, and lets a configured id that no longer exists in Play fall out quietly
         * instead of failing the screen.
         */
        fun map(
            products: List<ProductDetails>,
            catalog: List<BillingProductEntry>,
        ): List<PremiumPlan> {
            val featured = catalog.firstOrNull { it.featured }
            val byId = products.associateBy { it.productId }
            return catalog.mapNotNull { entry ->
                val product = byId[entry.id] ?: return@mapNotNull null
                mapEntry(product, entry)?.copy(
                    isFeatured = featured != null &&
                        entry.id == featured.id &&
                        entry.basePlanId == featured.basePlanId,
                )
            }
        }

        private fun mapEntry(
            product: ProductDetails,
            entry: BillingProductEntry,
        ): PremiumPlan? {
            product.oneTimePurchaseOfferDetails?.let { oneTime ->
                return PremiumPlan(
                    productId = product.productId,
                    basePlanId = null,
                    offerToken = oneTime.offerToken,
                    name = strings.getString(R.string.plan_term_lifetime),
                    description = product.description,
                    // A one-time purchase has no phases and no period: the price is the whole story.
                    priceLine = oneTime.formattedPrice,
                    isOneTime = true,
                )
            }

            val offer = resolveOffer(product, entry.basePlanId) ?: return null
            val phases = offer.pricingPhases.pricingPhaseList
            // The last paid phase is what the buyer pays from then on. Counting phases instead
            // would break on a promotional price, where three of them exist and only the last is
            // the steady one.
            val steady = phases.lastOrNull { it.priceAmountMicros > 0 } ?: return null

            return PremiumPlan(
                productId = product.productId,
                basePlanId = offer.basePlanId,
                offerToken = offer.offerToken,
                name = termLabel(steady.billingPeriod),
                description = product.description,
                priceLine = buildPriceLine(phases, steady),
                isOneTime = false,
            )
        }

        /**
         * The offer to sell for [basePlanId].
         *
         * A base plan appears once on its own and once more for every promotional offer the buyer
         * is eligible for - Play only returns eligible ones - so a promo present here is a
         * discount this buyer can actually have, and picking it is both the friendlier and the
         * more accurate choice, since the price line is built from whichever offer we return.
         *
         * A null [basePlanId] means the config did not disambiguate, which is correct for a
         * product that sells a single base plan.
         */
        private fun resolveOffer(
            product: ProductDetails,
            basePlanId: String?,
        ): ProductDetails.SubscriptionOfferDetails? {
            val offers = product.subscriptionOfferDetails.orEmpty()
            val forPlan =
                if (basePlanId.isNullOrBlank()) {
                    offers
                } else {
                    offers.filter { it.basePlanId == basePlanId }
                }
            return forPlan.firstOrNull { !it.offerId.isNullOrBlank() } ?: forPlan.firstOrNull()
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

        /**
         * What to call the plan on its card: "Monthly", "Yearly".
         *
         * Not `product.name`, which used to be the title here. Monthly and yearly are base plans of
         * one product now, so they share a name and a description, and two cards reading "Sagas
         * Pro" differing only in price is not a list anyone can choose from. The brand is already
         * above the list; the card's job is to say which term this is.
         *
         * A period nobody has a word for, a quarterly plan say, falls back to counting it out.
         */
        private fun termLabel(billingPeriod: String?): String {
            val (amount, unit) = parsePeriod(billingPeriod) ?: return ""
            if (amount != 1) return durationLabel(billingPeriod, 1)
            return strings.getString(
                when (unit) {
                    'D' -> R.string.plan_term_daily
                    'W' -> R.string.plan_term_weekly
                    'Y' -> R.string.plan_term_yearly
                    else -> R.string.plan_term_monthly
                },
            )
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
