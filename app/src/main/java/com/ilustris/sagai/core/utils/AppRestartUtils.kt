package com.ilustris.sagai.core.utils

import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess

fun Context.restartApp() {
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    startActivity(intent)
    exitProcess(0)
}
