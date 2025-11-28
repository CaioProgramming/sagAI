package com.ilustris.sagai.core.data

import com.google.firebase.crashlytics.FirebaseCrashlytics

// Changed: Only one generic type for Success
sealed class RequestResult<out R> {
    // Changed: Error type is fixed to Exception
    data class Error(
        val value: Exception, // Changed: L to Exception
    ) : RequestResult<Nothing>()

    data class Success<out R>(
        val value: R,
    ) : RequestResult<R>()

    val isSuccess = this is Success

    val isFailure = this is Error

    // Changed: L removed from signature
    fun onSuccess(block: (R) -> Unit): RequestResult<R> {
        if (this is Success) {
            block(value)
        }
        return this
    }

    // Changed: L removed from signature
    suspend fun onSuccessAsync(block: suspend (R) -> Unit): RequestResult<R> {
        if (this is Success) {
            block(value)
        }
        return this
    }

    // Changed: L removed from signature, block takes Exception
    fun onFailure(block: (Exception) -> Unit): RequestResult<R> {
        if (this is Error) {
            block(value)
        }
        return this
    }

    // Changed: L removed from signature, block takes Exception
    suspend fun onFailureAsync(block: suspend (Exception) -> Unit): RequestResult<R> {
        if (this is Error) {
            block(value)
        }
        return this
    }

    // Success getter remains the same conceptually, but type is now RequestResult.Success<R>
    val success get() = this as Success<R>

    // Error getter remains the same conceptually, but type is now RequestResult.Error
    val error get() = this as Error

    fun getSuccess(): R? =
        try {
            if (this is Success) {
                this.value
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
}

// asSuccess remains largely the same
fun <R> R.asSuccess(): RequestResult.Success<R> = RequestResult.Success(this)

// asError now returns RequestResult.Error (non-generic)
fun Exception.asError(sendToCrashlytics: Boolean = true): RequestResult.Error {
    this.printStackTrace()
    if (sendToCrashlytics) {
        FirebaseCrashlytics.getInstance().recordException(this)
    }
    return RequestResult.Error(this)
}

suspend fun <R> executeRequest(
    reportCrash: Boolean = true,
    block: suspend () -> R,
): RequestResult<R> =
    try {
        block().asSuccess()
    } catch (e: Exception) {
        e.asError(reportCrash)
    }
