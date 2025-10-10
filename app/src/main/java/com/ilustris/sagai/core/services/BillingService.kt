package com.ilustris.sagai.core.services

import android.content.Context
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
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

class BillingService
    @Inject
    constructor(
        context: Context,
    ) {
        val state = MutableStateFlow<BillingState?>(null)

        private val listener: PurchasesUpdatedListener by lazy {
            PurchasesUpdatedListener { result, purchases ->
                val purchaseCompleted =
                    result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null
                CoroutineScope(Dispatchers.IO).launch {
                    val newState =
                        if (purchaseCompleted) {
                            BillingState.SignatureEnabled
                        } else {
                            BillingState.SignatureDisabled(emptyList())
                        }
                    state.emit(newState)
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
            if (BuildConfig.DEBUG) {
                state.emit(BillingState.SignatureEnabled)
                return
            }
            val params =
                QueryPurchasesParams
                    .newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            val purchasesResult = billingClient.queryPurchasesAsync(params)
            val hasActiveSignature =
                purchasesResult.purchasesList.any { purchase ->
                    purchase.products.contains(SAGA_SIGNATURE_ID) && purchase.isAcknowledged
                }
            if (purchasesResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK && hasActiveSignature) {
                state.emit(BillingState.SignatureEnabled)
            } else {
                loadSignatureProduct()
            }
        }

        suspend fun purchaseSignature(
            activity: MainActivity,
            productDetails: ProductDetails,
            offerToken: String,
        ) {
            if (productDetails.productId != SAGA_SIGNATURE_ID) {
                state.emit(BillingState.SignatureDisabled(emptyList()))
                return
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
                state.emit(BillingState.SignatureDisabled(emptyList()))
            }
        }

        suspend fun loadSignatureProduct() {
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
            val products = productDetailsResult.productDetailsList ?: emptyList()
            state.emit(BillingState.SignatureDisabled(products))
        }

        fun isPremium() = state.value is BillingState.SignatureEnabled
    }

sealed interface BillingState {
    object SignatureEnabled : BillingState

    data class SignatureDisabled(
        val products: List<ProductDetails>,
    ) : BillingState
}

private const val SAGA_SIGNATURE_ID = "saga_signature"
