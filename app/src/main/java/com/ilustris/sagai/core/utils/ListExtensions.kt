package com.ilustris.sagai.core.utils

fun <T> List<T>.afterLast(predicate: (T) -> Boolean): List<T> {
    val index = indexOfLast(predicate)
    return if (index != -1 && index < lastIndex) subList(index + 1, size) else emptyList()
}

fun List<Any>.formatToJsonArray() = joinToString(separator = ",\n") { it.toJsonFormat() }
