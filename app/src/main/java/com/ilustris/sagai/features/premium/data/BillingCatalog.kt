package com.ilustris.sagai.features.premium.data

import androidx.annotation.Keep

/**
 * Which products to ask Play about, from `billing_products` in Remote Config.
 *
 * Play Billing has no client API that lists an app's own catalogue — `queryProductDetails` takes an
 * explicit list of ids and returns only those, and enumerating products exists solely in the
 * server-side Play Developer API. So the ids have to come from somewhere on our side, and Remote
 * Config is the one that does not need a release to add a plan.
 *
 * Only identity lives here. Name, description, price, currency and billing period all come back
 * from Play already localised to the buyer's account, and duplicating any of them here would
 * guarantee that one day the screen shows a price the buyer is not charged.
 */
@Keep
data class BillingCatalog(
    val products: List<BillingProductEntry> = emptyList(),
)

@Keep
data class BillingProductEntry(
    val id: String = "",
    /** `SUBS` or `INAPP`; anything else is ignored rather than guessed at. */
    val type: String = "",
    /**
     * Marks the plan the app puts forward. The label itself is an app string, not config: it
     * changes far less often than which plan carries it, and keeping it in `strings.xml` means a
     * new locale is translated with the rest of the app instead of as another Remote Config
     * conditional to keep in sync.
     */
    val featured: Boolean = false,
)
