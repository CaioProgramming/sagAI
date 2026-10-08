package com.ilustris.sagai.features.progression.domain

import com.ilustris.sagai.core.data.RequestResult

interface CharacterProgressionUseCase {
    /**
     * Judges what [timelineId] meant for its participants and writes their feats. Idempotent: running
     * it again replaces the event's feats. A no-op success when the system is off (kill switch, no
     * config, or a saga without progression). On failure nothing is written and the event's
     * `experienceGenerated` flag stays false, so it can be reviewed and retried.
     */
    suspend fun evaluateEvent(
        sagaId: Int,
        timelineId: Int,
    ): RequestResult<Unit>

    /**
     * Places the characters of a saga that predates the system (no `baseLevel` yet) in one call, so
     * the cast is judged as a single scale. Characters already placed are left alone.
     */
    suspend fun placeCharacters(sagaId: Int): RequestResult<Unit>

    /** Debug reset: wipes the feats and honors of a saga and clears its generation flags. */
    suspend fun resetSaga(sagaId: Int)
}
