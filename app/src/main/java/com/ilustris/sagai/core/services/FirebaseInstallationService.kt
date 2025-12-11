package com.ilustris.sagai.core.services

import android.util.Log
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseInstallationService
    @Inject
    constructor() {
        private val _installationId = MutableStateFlow<String?>(null)
        val installationId: StateFlow<String?> = _installationId.asStateFlow()

        init {
            fetchInstallationId()
        }

        private fun fetchInstallationId() {
            FirebaseInstallations
                .getInstance()
                .id
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val id = task.result
                        _installationId.value = id
                        Log.d("FirebaseInstallations", "COPY THIS TOKEN (FID Token) ->")
                        Log.d("FirebaseInstallations", "$id")
                        Log.d("FirebaseInstallations", "<- END OF TOKEN")
                    } else {
                        Log.e("FirebaseInstallations", "Failed to get Installation ID", task.exception)
                        _installationId.value = null
                    }
                }
        }

        fun getInstallationIdFlow(): Flow<String?> =
            callbackFlow {
                FirebaseInstallations
                    .getInstance()
                    .id
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            trySend(task.result)
                        } else {
                            trySend(null)
                        }
                    }

                awaitClose { }
            }

        fun getCurrentInstallationId(): String? = _installationId.value
    }
