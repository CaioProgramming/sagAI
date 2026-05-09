package com.ilustris.sagai.core.error

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import com.ilustris.sagai.features.sos.ui.SOSActivity
import timber.log.Timber

class SagasExceptionHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(
        thread: Thread,
        throwable: Throwable,
    ) {
        Timber.tag("SagasExceptionHandler").e(throwable, "Uncaught exception detected")

        val isDatabaseError = isDatabaseError(throwable)

        try {
            val intent =
                Intent(context, SOSActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra(SOSActivity.EXTRA_ERROR_MESSAGE, throwable.localizedMessage)
                    putExtra(SOSActivity.EXTRA_IS_DATABASE_ERROR, isDatabaseError)
                    putExtra(SOSActivity.EXTRA_EXCEPTION_CLASS, throwable.javaClass.simpleName)
                }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.tag("SagasExceptionHandler").e(e, "Failed to launch SOS activity")
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Kill the current process to ensure a clean state for the SOS activity
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(10)
    }

    private fun isDatabaseError(throwable: Throwable): Boolean {
        var cause: Throwable? = throwable
        while (cause != null) {
            if (cause is SQLiteException) return true
            if (cause.message?.contains(
                    "Room cannot verify the data integrity",
                    ignoreCase = true,
                ) == true
            ) {
                return true
            }
            if (cause.message?.contains(
                    "database disk image is malformed",
                    ignoreCase = true,
                ) == true
            ) {
                return true
            }
            cause = cause.cause
        }
        return false
    }
}
