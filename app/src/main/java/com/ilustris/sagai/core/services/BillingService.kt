package com.ilustris.sagai.core.services

import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.core.data.SideEffect
import timber.log.Timber
import com.ilustris.sagai.features.premium.data.BillingCatalog
import com.ilustris.sagai.features.premium.data.BillingProductEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class BillingService
    @Inject
    constructor(
        context: Context,
        private val remoteConfigService: RemoteConfigService,
        private val firebaseInstallationService: FirebaseInstallationService,
        private val sideEffectService: SideEffectService,
    ) {
        val state = MutableStateFlow<BillingState?>(null)
        val purchaseFlowResult = MutableStateFlow<PurchaseFlowResult>(PurchaseFlowResult.Idle)

        private val listener: PurchasesUpdatedListener by lazy {
            PurchasesUpdatedListener { result, purchases ->
                CoroutineScope(Dispatchers.IO).launch {
                    when {
                        result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null -> {
                            handleSuccessfulPurchases(purchases)
                        }

                        result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> {
                            purchaseFlowResult.emit(PurchaseFlowResult.Cancelled)
                        }

                        else -> {
                            handleBillingFailure(
                                result.responseCode,
                                result.debugMessage,
                                duringPurchase = true,
                            )
                        }
                    }
                }
            }
        }
        private val billingClient by lazy {
            BillingClient
                .newBuilder(context)
                .setListener(listener)
                .enablePendingPurchases(
                    PendingPurchasesParams
                        .newBuilder()
                        .enablePrepaidPlans()
                        .enableOneTimeProducts()
                        .build(),
                ).enableAutoServiceReconnection()
                .build()
        }

        suspend fun checkPurchases() {
            if (remoteConfigService.getBoolean(PREMIUM_TESTER) == true) {
                state.emit(BillingState.SignatureEnabled)
                return
            }
            val connectionResult = ensureConnected()
            if (connectionResult.responseCode != BillingClient.BillingResponseCode.OK) {
                handleBillingFailure(
                    connectionResult.responseCode,
                    connectionResult.debugMessage,
                    duringPurchase = false,
                )
                return
            }
            // Both types, because ad-free is sold as a subscription and as a one-time purchase,
            // and either one grants it. This asks Play what the user owns rather than checking a
            // single known id: a purchase list needs no ids at all, unlike the catalogue query.
            val owned = mutableListOf<Purchase>()
            for (type in listOf(BillingClient.ProductType.SUBS, BillingClient.ProductType.INAPP)) {
                val params =
                    QueryPurchasesParams
                        .newBuilder()
                        .setProductType(type)
                        .build()
                val purchasesResult = billingClient.queryPurchasesAsync(params)
                if (purchasesResult.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    handleBillingFailure(
                        purchasesResult.billingResult.responseCode,
                        purchasesResult.billingResult.debugMessage,
                        duringPurchase = false,
                    )
                    return
                }
                owned += purchasesResult.purchasesList
            }

            val purchased =
                owned.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            val active = purchased.firstOrNull { it.isAcknowledged }
            if (active != null) {
                state.emit(BillingState.SignatureEnabled)
                return
            }
            // Bought but never acknowledged — a process death between paying and acknowledging, or
            // an acknowledge that failed. Play auto-refunds within three days, so finishing it here
            // is what keeps someone who already paid from losing both the money and the access.
            val unacknowledged = purchased.firstOrNull()
            if (unacknowledged != null) {
                if (acknowledgePurchase(unacknowledged)) {
                    state.emit(BillingState.SignatureEnabled)
                    purchaseFlowResult.emit(PurchaseFlowResult.Success)
                }
                return
            }
            loadProducts()
        }

        /**
         * Launches Play's payment sheet for [productDetails].
         *
         * [offerToken] names which base plan or purchase option is being bought. It used to be a
         * subscription-only concern, back when a one-time product was a single price with nothing
         * to choose between; purchase options gave those a token of their own, so both kinds need
         * it now.
         *
         * This used to also re-check the id against a single constant, which made sense while
         * exactly one product existed and is simply wrong now: the [ProductDetails] came from our
         * own catalogue query, so there is nothing left to validate it against.
         */
        suspend fun purchase(
            activity: MainActivity,
            productDetails: ProductDetails,
            offerToken: String?,
        ): Boolean {
            val connectionResult = ensureConnected()
            if (connectionResult.responseCode != BillingClient.BillingResponseCode.OK) {
                handleBillingFailure(
                    connectionResult.responseCode,
                    connectionResult.debugMessage,
                    duringPurchase = true,
                )
                return false
            }
            val productDetailsParamsList =
                listOf(
                    BillingFlowParams.ProductDetailsParams
                        .newBuilder()
                        .setProductDetails(productDetails)
                        .apply { if (offerToken != null) setOfferToken(offerToken) }
                        .build(),
                )
            val billingFlowParams =
                BillingFlowParams
                    .newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
            val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                handleBillingFailure(
                    billingResult.responseCode,
                    billingResult.debugMessage,
                    duringPurchase = true,
                )
                return false
            }
            return true
        }

        /**
         * Fetches every product named in `billing_products`.
         *
         * One query per product type: Play rejects a mixed list. An id that no longer exists in
         * Play simply does not come back, and that is deliberately not an error — a typo in the
         * config must cost that one plan, never the whole screen.
         */
        suspend fun loadProducts(duringPurchase: Boolean = false): Boolean {
            val connectionResult = ensureConnected()
            if (connectionResult.responseCode != BillingClient.BillingResponseCode.OK) {
                handleBillingFailure(
                    connectionResult.responseCode,
                    connectionResult.debugMessage,
                    duringPurchase = duringPurchase,
                )
                return false
            }

            val catalog = catalogEntries()
            Timber.tag(TAG).d("Configured catalogue: ${catalog.map { "${it.id}/${it.optionId}" }}")
            if (catalog.isEmpty()) {
                handleBillingFailure(
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                    "No products configured",
                    duringPurchase = duringPurchase,
                )
                return false
            }

            val products = mutableListOf<ProductDetails>()
            for ((type, entries) in catalog.groupBy { it.type.lowercase() }) {
                val params =
                    QueryProductDetailsParams
                        .newBuilder()
                        .setProductList(
                            entries.map { entry ->
                                QueryProductDetailsParams.Product
                                    .newBuilder()
                                    .setProductId(entry.id)
                                    .setProductType(type)
                                    .build()
                            },
                        ).build()
                val result = billingClient.queryProductDetails(params)
                if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    handleBillingFailure(
                        result.billingResult.responseCode,
                        result.billingResult.debugMessage,
                        duringPurchase = duringPurchase,
                    )
                    return false
                }
                val returned = result.productDetailsList ?: emptyList()
                Timber
                    .tag(TAG)
                    .d("Play returned ${returned.map { it.productId }} for $type")
                products += returned
            }

            if (products.isEmpty()) {
                handleBillingFailure(
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                    "No configured product is available in Play",
                    duringPurchase = duringPurchase,
                )
                return false
            }
            state.emit(BillingState.SignatureDisabled(products))
            return true
        }

        /** The configured catalogue, minus entries this app would not know what to do with. */
        suspend fun catalogEntries(): List<BillingProductEntry> =
            remoteConfigService
                .getJson<BillingCatalog>(BILLING_PRODUCTS_KEY)
                ?.products
                .orEmpty()
                .filter {
                    // Lowercased, because that is what BillingClient.ProductType actually holds:
                    // SUBS is the string "subs", not "SUBS". Comparing an uppercased config value
                    // against them matched nothing, so every configured product was dropped before
                    // the query and the screen reported "no products configured" while the config
                    // sitting right there listed three.
                    it.id.isNotBlank() &&
                        it.type.lowercase() in
                        setOf(BillingClient.ProductType.SUBS, BillingClient.ProductType.INAPP)
                }

        suspend fun syncSubscription(): Boolean {
            checkPurchases()
            if (isPremium()) {
                purchaseFlowResult.emit(PurchaseFlowResult.Success)
                return true
            }
            return false
        }

    fun resetPurchaseFlowResult() {
        purchaseFlowResult.value = PurchaseFlowResult.Idle
        }

        fun isPremium() = state.value is BillingState.SignatureEnabled

        suspend fun <R> runPremiumRequest(
            bypass: Boolean = false,
            block: suspend () -> R,
        ): R =
            if (isPremium() || (bypass && BuildConfig.DEBUG)) {
                block()
            } else {
                sideEffectService.emit(SideEffect.ShowPremiumOnboarding)
                throw PremiumException(firebaseInstallationService.getCurrentInstallationId())
            }

        private suspend fun ensureConnected(): BillingResult {
            if (billingClient.isReady) {
                return BillingResult
                    .newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.OK)
                    .build()
            }
            return suspendCancellableCoroutine { continuation ->
                billingClient.startConnection(
                    object : BillingClientStateListener {
                        override fun onBillingSetupFinished(billingResult: BillingResult) {
                            if (continuation.isActive) {
                                continuation.resume(billingResult)
                            }
                        }

                        override fun onBillingServiceDisconnected() {
                            // Reconnection is handled by enableAutoServiceReconnection.
                        }
                    },
                )
            }
        }

        private suspend fun handleSuccessfulPurchases(purchases: List<Purchase>) {
            val signaturePurchase =
                purchases.firstOrNull { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
            if (signaturePurchase == null) {
                // Not cancelled in every case: a slow payment method leaves the purchase PENDING,
                // and Play calls back again once it clears.
                val pending =
                    purchases.any { it.purchaseState == Purchase.PurchaseState.PENDING }
                purchaseFlowResult.emit(
                    if (pending) PurchaseFlowResult.Pending else PurchaseFlowResult.Cancelled,
                )
                return
            }
            if (!signaturePurchase.isAcknowledged) {
                val acknowledged = acknowledgePurchase(signaturePurchase)
                if (!acknowledged) return
            }
            state.emit(BillingState.SignatureEnabled)
            purchaseFlowResult.emit(PurchaseFlowResult.Success)
        }

        private suspend fun acknowledgePurchase(purchase: Purchase): Boolean {
            val acknowledgePurchaseParams =
                AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            val result = billingClient.acknowledgePurchase(acknowledgePurchaseParams)
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                handleBillingFailure(
                    result.responseCode,
                    result.debugMessage,
                    duringPurchase = true,
                )
                return false
            }
            return true
        }

        private suspend fun handleBillingFailure(
            responseCode: Int,
            debugMessage: String?,
            duringPurchase: Boolean,
        ) {
            val message = debugMessage ?: billingResponseMessage(responseCode)
            // Logged because the screen can only ever say "could not load the plans", and the
            // causes behind that sentence need completely different fixes: an APK signed with the
            // debug keystore, a product still in draft, an id that does not match the console, an
            // empty catalogue in Remote Config. Without the code there is no way to tell them
            // apart from outside.
            Timber
                .tag(TAG)
                .w("Billing failure ($responseCode ${billingResponseMessage(responseCode)}): $message")
            state.emit(BillingState.BillingError(responseCode, message))
            if (!duringPurchase) return
            // The same error in both builds. Debug used to get a sheet offering to simulate the
            // purchase instead, on the assumption that Play Billing simply does not work in a
            // debug build; it does here, products and all, so that assumption was hiding real
            // failures behind a fake success.
            purchaseFlowResult.emit(PurchaseFlowResult.Error(message))
        }

        private fun billingResponseMessage(responseCode: Int): String =
            when (responseCode) {
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Billing service unavailable"
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Billing unavailable on this device"
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "Subscription product unavailable"
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "Billing configuration error"
            BillingClient.BillingResponseCode.ERROR -> "Billing error"
            BillingClient.BillingResponseCode.USER_CANCELED -> "Purchase cancelled"
            else -> "Billing error (code $responseCode)"
        }

        class PremiumException(
            val deviceId: String? = null,
        ) : Exception(
                buildString {
                    appendLine(" ❌ Premium feature accessed without signature.")
                    if (BuildConfig.DEBUG) {
                        appendLine("If you are a tester, request the developer to bypass this restriction.")
                        appendLine("Send the device ID and try again.")
                        if (deviceId != null) {
                            appendLine("Device ID: $deviceId")
                        }
                    }
                },
            )

        sealed interface BillingState {
            object SignatureEnabled : BillingState

            data class SignatureDisabled(
                val products: List<ProductDetails>,
            ) : BillingState

            data class BillingError(
                val responseCode: Int,
                val debugMessage: String?,
            ) : BillingState
        }

    sealed interface PurchaseFlowResult {
        object Idle : PurchaseFlowResult

        object Success : PurchaseFlowResult

        /** Paid for with a method that clears later, such as a boleto. Access is not granted yet. */
        object Pending : PurchaseFlowResult

        object Cancelled : PurchaseFlowResult

        data class Error(
            val message: String,
        ) : PurchaseFlowResult

        }
    }

private const val TAG = "💳 Billing"
private const val BILLING_PRODUCTS_KEY = "billing_products"
private const val PREMIUM_TESTER = "premiumTester"
