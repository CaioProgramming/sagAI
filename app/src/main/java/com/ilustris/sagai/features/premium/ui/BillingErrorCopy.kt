package com.ilustris.sagai.features.premium.ui

import androidx.annotation.StringRes
import com.android.billingclient.api.BillingClient
import com.ilustris.sagai.R

/**
 * Our own words for a Play response code.
 *
 * Play answers with a `debugMessage` that is written for the developer and only ever arrives in
 * English, so showing it put "Please ensure the app is signed correctly" in front of a user who
 * can do nothing about it. The code is the part that is stable and finite, so it is what gets
 * translated; the message stays in the log.
 *
 * Codes with no line of their own fall back to the generic one rather than exposing a number.
 */
@StringRes
fun billingErrorCopy(responseCode: Int): Int =
    when (responseCode) {
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
        BillingClient.BillingResponseCode.NETWORK_ERROR,
        -> R.string.billing_error_unreachable

        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
            R.string.billing_error_unsupported

        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
            R.string.billing_error_item_unavailable

        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
            R.string.billing_error_already_owned

        // A configuration problem on our side. The user cannot act on it, so the copy does not
        // pretend they can.
        BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
            R.string.billing_error_misconfigured

        else -> R.string.billing_error_generic_message
    }
