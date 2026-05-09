package com.ilustris.sagai.features.sos.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.features.sos.presentation.SOSViewModel
import com.ilustris.sagai.ui.theme.SagAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SOSActivity : ComponentActivity() {
    private val viewModel: SOSViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE) ?: "Unknown system failure"
        val isDatabaseError = intent.getBooleanExtra(EXTRA_IS_DATABASE_ERROR, false)
        val exceptionClass = intent.getStringExtra(EXTRA_EXCEPTION_CLASS) ?: "Exception"

        setContent {
            SagAITheme {
                SOSScreen(
                    errorMessage = errorMessage,
                    isDatabaseError = isDatabaseError,
                    exceptionClass = exceptionClass,
                    viewModel = viewModel,
                    onRestart = {
                        val intent =
                            Intent(this, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                        startActivity(intent)
                        finish()
                    },
                )
            }
        }
    }

    companion object {
        const val EXTRA_ERROR_MESSAGE = "extra_error_message"
        const val EXTRA_IS_DATABASE_ERROR = "extra_is_database_error"
        const val EXTRA_EXCEPTION_CLASS = "extra_exception_class"
    }
}
