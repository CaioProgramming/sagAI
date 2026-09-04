package com.ilustris.sagai.features.premium.data

/**
 * A purchasable plan, in this app's own terms.
 *
 * The UI deliberately does not speak `ProductDetails`. That class is final with a package-private
 * JSON constructor, so anything depending on it cannot be built in a test or a preview without
 * forging Google's own wire format and, in effect, testing their parser instead of our screen.
 *
 * Everything here is display-ready: prices arrive from Play already localised to the buyer's
 * currency and language, and [priceLine] is assembled once in the mapper rather than in each
 * place that shows a plan.
 */
data class PremiumPlan(
    val productId: String,
    /** Needed to launch the purchase for a subscription offer; null for a one-time product. */
    val offerToken: String?,
    val name: String,
    val description: String,
    /** e.g. "R$ 23,90/mês", "3 meses grátis, depois R$ 23,90/mês", or a bare price. */
    val priceLine: String,
    val isOneTime: Boolean,
    /** The plan the app puts forward. At most one in a list; the label itself is an app string. */
    val isFeatured: Boolean = false,
)
