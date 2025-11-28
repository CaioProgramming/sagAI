package com.ilustris.sagai.features.act.ui

fun Int.toRoman(): String {
    val values = listOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    val symbols = listOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
    var num = this
    val result = StringBuilder()

    for (i in values.indices) {
        while (num >= values[i]) {
            num -= values[i]
            result.append(symbols[i])
        }
    }
    return result.toString()
}
