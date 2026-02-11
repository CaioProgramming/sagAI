package com.ilustris.sagai.core.services

import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
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
                        Timber.d("COPY THIS TOKEN (FID Token) ->")
                        Timber.d("$id")
                        Timber.d("<- END OF TOKEN")
                    } else {
                        Timber.e(task.exception, "Failed to get Installation ID")
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
