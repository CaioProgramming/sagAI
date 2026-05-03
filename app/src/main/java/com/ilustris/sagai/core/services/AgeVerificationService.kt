package com.ilustris.sagai.core.services

import android.content.Context
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import com.ilustris.sagai.core.ai.model.AgeGroup
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgeVerificationService
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val ageSignalsManager: AgeSignalsManager by lazy {
            AgeSignalsManagerFactory.create(context)
        }

        /**
         * Retrieves the user's age group using Play Age Signals.
         * Brazil thresholds: 18+ is ADULT, 12-17 is TEEN, <12 is CHILD.
         */
        suspend fun getUserAgeGroup(): AgeGroup =
            try {
                val response =
                    ageSignalsManager.checkAgeSignals(AgeSignalsRequest.builder().build()).await()
                Timber.tag("AgeVerification").d("Play Age Signal received: $response")

                val status = response.userStatus()
                val ageLower = response.ageLower()
                val ageUpper = response.ageUpper()

                when {
                    status == AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED ||
                        status == AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING -> AgeGroup.CHILD

                    ageUpper != null && ageUpper < 12 -> AgeGroup.CHILD

                    ageLower != null && ageLower >= 18 -> AgeGroup.ADULT

                    ageLower != null && ageLower >= 12 -> AgeGroup.TEEN

                    status == AgeSignalsVerificationStatus.VERIFIED -> AgeGroup.ADULT

                    status == AgeSignalsVerificationStatus.SUPERVISED -> AgeGroup.TEEN

                    else -> AgeGroup.ADULT
                }
            } catch (e: Exception) {
                Timber
                    .tag("AgeVerification")
                    .e(e, "Failed to get Play Age Signals. Defaulting to ADULT.")
                AgeGroup.ADULT
            }
    }
