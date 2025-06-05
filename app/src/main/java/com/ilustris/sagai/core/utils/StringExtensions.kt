package com.ilustris.sagai.core.utils

fun String.addQueryParameter(
    key: String,
    value: String,
): String = this.plus("?$key=$value")

fun emptyString() = ""
