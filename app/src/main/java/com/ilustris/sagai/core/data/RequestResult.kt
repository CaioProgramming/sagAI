package com.ilustris.sagai.core.data

sealed class RequestResult<out L, out R> {
    data class Error<out L>(
        val value: L,
    ) : RequestResult<L, Nothing>()

    data class Success<out R>(
        val value: R,
    ) : RequestResult<Nothing, R>()

    fun isSuccess(): Boolean = this is Success

    fun isFailure(): Boolean = this is Error

    fun onSuccess(block: (R) -> Unit): RequestResult<L, R> {
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

    val success get() = this as Success
    val error get() = this as Error
}

fun <R> R.asSuccess() = RequestResult.Success(this)

fun <L : Exception> L.asError(): RequestResult.Error<L> {
    this.printStackTrace()
    return RequestResult.Error(this)
}
