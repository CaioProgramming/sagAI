package com.ilustris.sagai.core.utils

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StringResourceHelper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun getString(
            @StringRes resId: Int,
        ): String = context.getString(resId)

        fun getString(
            @StringRes resId: Int,
            vararg formatArgs: Any,
        ): String = context.getString(resId, *formatArgs)

        fun getQuantityString(
            @StringRes resId: Int,
            quantity: Int,
        ): String = context.resources.getQuantityString(resId, quantity)

        fun getQuantityString(
            @StringRes resId: Int,
            quantity: Int,
            vararg formatArgs: Any,
        ): String = context.resources.getQuantityString(resId, quantity, *formatArgs)

    fun getStringArray(
        @StringRes resId: Int,
    ): Array<String> = context.resources.getStringArray(resId)
}
