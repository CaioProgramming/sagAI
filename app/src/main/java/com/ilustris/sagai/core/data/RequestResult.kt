package com.ilustris.sagai.core.data

import com.google.firebase.crashlytics.FirebaseCrashlytics

sealed class RequestResult<out L, out R> {
    data class Error<out L>(
        val value: L,
    ) : RequestResult<L, Nothing>()

    data class Success<out R>(
        val value: R,
    ) : RequestResult<Nothing, R>()

    val isSuccess = this is Success

    val isFailure = this is Error

    fun onSuccess(block: (R) -> Unit): RequestResult<L, R> {
        if (this is Success) {
            block(value)
        }
        return this
    }

    suspend fun onSuccessAsync(block: suspend (R) -> Unit): RequestResult<L, R> {
        if (this is Success) {
            block(value)
        }
        return this
    }

    fun onFailure(block: (L) -> Unit): RequestResult<L, R> {
        if (this is Error) {
            block(value)
        }
        return this
    }

    suspend fun onFailureAsync(block: suspend (L) -> Unit): RequestResult<L, R> {
        if (this is Error) {
            block(value)
        }
        return this
    }

    val success get() = this as Success
    val error get() = this as Error

    fun getSuccess() =
        try {
            success.value!!
        } catch (e: Exception) {
            null
        }
}

fun <R> R.asSuccess() = RequestResult.Success(this)

fun <L : Exception> L.asError(sendToCrashlytics: Boolean = true): RequestResult.Error<L> {
    this.printStackTrace()
    if (sendToCrashlytics) {
        FirebaseCrashlytics.getInstance().recordException(this)
    }

    return RequestResult.Error(this)
}

suspend fun <R> executeRequest(block: suspend () -> R): RequestResult<Exception, R> =
    try {
        block().asSuccess()
    } catch (e: Exception) {
        e.asError()
    }
