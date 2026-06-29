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
            val params =
                QueryPurchasesParams
                    .newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
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
            val hasActiveSignature =
                purchasesResult.purchasesList.any { purchase ->
                    purchase.products.contains(SAGA_SIGNATURE_ID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.isAcknowledged
                }
            if (hasActiveSignature) {
                state.emit(BillingState.SignatureEnabled)
            } else {
                val pendingPurchase =
                    purchasesResult.purchasesList.firstOrNull { purchase ->
                        purchase.products.contains(SAGA_SIGNATURE_ID) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                            !purchase.isAcknowledged
                    }
                if (pendingPurchase != null) {
                    acknowledgePurchase(pendingPurchase)
                } else {
                    loadSignatureProduct()
                }
            }
        }

        suspend fun purchaseSignature(
            activity: MainActivity,
            productDetails: ProductDetails,
            offerToken: String,
        ): Boolean {
            if (productDetails.productId != SAGA_SIGNATURE_ID) {
                handleBillingFailure(
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                    "Invalid product id",
                    duringPurchase = true,
                )
                return false
            }
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
                        .setOfferToken(offerToken)
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

        suspend fun loadSignatureProduct(duringPurchase: Boolean = false): Boolean {
            val connectionResult = ensureConnected()
            if (connectionResult.responseCode != BillingClient.BillingResponseCode.OK) {
                handleBillingFailure(
                    connectionResult.responseCode,
                    connectionResult.debugMessage,
                    duringPurchase = duringPurchase,
                )
                return false
        }
            val params =
                QueryProductDetailsParams
                    .newBuilder()
                    .setProductList(
                        listOf(
                            QueryProductDetailsParams.Product
                                .newBuilder()
                                .setProductId(SAGA_SIGNATURE_ID)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build(),
                        ),
                    ).build()
            val productDetailsResult = billingClient.queryProductDetails(params)
            if (productDetailsResult.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                handleBillingFailure(
                    productDetailsResult.billingResult.responseCode,
                    productDetailsResult.billingResult.debugMessage,
                    duringPurchase = duringPurchase,
                )
                return false
        }
            val products = productDetailsResult.productDetailsList ?: emptyList()
            if (products.isEmpty()) {
                handleBillingFailure(
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                    "Product not found",
                    duringPurchase = duringPurchase,
            )
            return false
        }
            state.emit(BillingState.SignatureDisabled(products))
            return true
        }

        suspend fun syncSubscription(): Boolean {
            checkPurchases()
            if (isPremium()) {
                purchaseFlowResult.emit(PurchaseFlowResult.Success)
                return true
            }
            return false
        }

        suspend fun simulatePurchase(confirmed: Boolean) {
            if (!BuildConfig.DEBUG) return
            if (confirmed) {
                purchaseFlowResult.emit(PurchaseFlowResult.DebugSimulationSuccess)
            } else {
                purchaseFlowResult.emit(PurchaseFlowResult.Cancelled)
        }
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
                    purchase.products.contains(SAGA_SIGNATURE_ID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
            if (signaturePurchase == null) {
                purchaseFlowResult.emit(PurchaseFlowResult.Cancelled)
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
            state.emit(BillingState.BillingError(responseCode, message))
            if (!duringPurchase) return
            if (BuildConfig.DEBUG) {
                purchaseFlowResult.emit(
                    PurchaseFlowResult.DebugFallback(
                        "Test environment: Play Billing is unavailable on this DEBUG build. ($message)",
                    ),
                )
            } else {
                purchaseFlowResult.emit(PurchaseFlowResult.Error(message))
            }
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

        data object DebugSimulationSuccess : PurchaseFlowResult

        object Cancelled : PurchaseFlowResult

        data class Error(
            val message: String,
        ) : PurchaseFlowResult

        data class DebugFallback(
            val reason: String,
        ) : PurchaseFlowResult
        }
    }

private const val SAGA_SIGNATURE_ID = "saga_signature"
private const val PREMIUM_TESTER = "premiumTester"
