package com.ilustris.sagai.features.progression.domain

import com.ilustris.sagai.core.data.RequestResult
import kotlinx.coroutines.flow.Flow

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
        silent: Boolean = false,
    ): RequestResult<List<ProgressionChange>>

    /**
     * Places the characters of a saga that predates the system (no `baseLevel` yet) in one call, so
     * the cast is judged as a single scale. Characters already placed are left alone.
     */
    suspend fun placeCharacters(sagaId: Int): RequestResult<List<ProgressionChange>>

    /**
     * Medals for a closed chapter. The AI judges which deeds deserve one; the code validates the
     * judgement against the ledger. Skipped without a call when no feat could back a medal. [silent]
     * writes the honors as already announced (history catch-up).
     */
    suspend fun evaluateChapterHonors(
        sagaId: Int,
        chapterId: Int,
        silent: Boolean = false,
    ): RequestResult<List<ProgressionChange>>

    /** Legacy titles for a closed act, under the same judge-then-validate rule. */
    suspend fun evaluateActLegacy(
        sagaId: Int,
        actId: Int,
        silent: Boolean = false,
    ): RequestResult<List<ProgressionChange>>

    /**
     * The level-gap text for the reply prompt: who of [presentNames] (plus the protagonist) is
     * stronger and by how much. Null when the system is off or fewer than two characters are placed.
     */
    suspend fun powerContext(
        saga: com.ilustris.sagai.features.home.data.model.SagaContent,
        presentNames: List<String>,
    ): String?

    /**
     * Re-weighs the XP of every stored feat with the current config (tier table, decay, caps),
     * replaying the saga in order and without any AI call. Calibration tool: XP is frozen when a
     * feat is written, so a changed config does not reach old feats until this runs.
     */
    suspend fun recomputeXp(sagaId: Int): RequestResult<Unit>

    /** Live progress of one character for the sheet; null when the system is off or they were never placed. */
    fun observeProgress(character: com.ilustris.sagai.features.characters.data.model.Character): kotlinx.coroutines.flow.Flow<CharacterProgress?>

    /** Level, last medal and legacy of every placed character of a saga, for the characters list. */
    fun observeBadges(
        sagaId: Int,
        characters: List<com.ilustris.sagai.features.characters.data.model.Character>,
    ): Flow<Map<Int, CharacterBadge>>

    /** Per event and character: the XP the event paid, the level after it, and the feats behind it, for the timeline. */
    fun observeEventXp(
        sagaId: Int,
        characters: List<com.ilustris.sagai.features.characters.data.model.Character>,
    ): Flow<Map<Int, Map<Int, EventXp>>>

    /**
     * Tells the player what was earned and not yet told: the level-ups of finished events (merged, once) and then
     * the medals and legacies, one announcement at a time. Idempotent: nothing is told twice, and nothing is marked as
     * told while the app is in the background, so it comes up when the player is back.
     */
    suspend fun announcePending(sagaId: Int)

    /** Debug: every character of the saga with level, XP and the feats and honors behind it. */
    suspend fun ledger(sagaId: Int): List<CharacterLedger>

    /**
     * One-shot status catch-up for a saga that predates the system: reads the latest events and marks
     * who is absent, missing or dead. A change needs a reason, like the live flow.
     */
    suspend fun inferStatuses(sagaId: Int): RequestResult<List<ProgressionChange>>

    /** How many finished events, chapters and acts have no successful evaluation yet (a failed or never-run request). */
    suspend fun pendingCount(sagaId: Int): Int

    /** Runs the evaluations that failed or never ran, in story order and without announcing the past. */
    suspend fun retryPending(sagaId: Int): RequestResult<Int>

    /**
     * Quietly finishes what failed or never ran, a few at a time and without announcing the past.
     * Stops at the first failure so an outage or spent quota is not hammered, and does nothing while
     * another catch-up of the same saga runs. The latest event is left to the live flow.
     */
    suspend fun catchUpPending(sagaId: Int)

    /** Debug reset: wipes the feats and honors of a saga and clears its generation flags. */
    suspend fun resetSaga(sagaId: Int)
}

data class CharacterLedger(
    val name: String,
    val baseLevel: Int?,
    val level: Int?,
    val earnedXp: Int,
    val feats: List<com.ilustris.sagai.features.progression.data.model.CharacterFeat>,
    val honors: List<com.ilustris.sagai.features.progression.data.model.CharacterHonor>,
)

/** One visible effect of a progression step, so a long run can say what it is changing. */
sealed interface ProgressionChange {
    data class Xp(
        val characterId: Int,
        val characterName: String,
        val xp: Int,
        val levelBefore: Int,
        val levelAfter: Int,
        val featTitles: List<String>,
    ) : ProgressionChange

    data class Honor(
        val characterId: Int,
        val characterName: String,
        val kind: com.ilustris.sagai.features.progression.data.model.HonorKind,
        val name: String,
        val emoji: String?,
        val description: String = "",
    ) : ProgressionChange

    data class Status(
        val characterName: String,
        val status: com.ilustris.sagai.features.characters.data.model.CharacterStatus,
        val note: String?,
    ) : ProgressionChange

    data class Placed(
        val characterName: String,
        val level: Int,
    ) : ProgressionChange
}

/** Everything the character sheet shows about experience, derived from the ledger and the config. */
data class CharacterProgress(
    val baseLevel: Int,
    val level: Int,
    val bandLabel: String,
    val earnedXp: Int,
    /** XP earned inside the current level, and what the whole level takes; both 0 at the cap. */
    val xpIntoLevel: Int,
    val levelSpan: Int,
    val rationale: String?,
    val feats: List<com.ilustris.sagai.features.progression.data.model.CharacterFeat>,
    val honors: List<com.ilustris.sagai.features.progression.data.model.CharacterHonor>,
    /** Position (1-based) of each chapter and act by id, to say where an honor was earned. */
    val chapterNumbers: Map<Int, Int> = emptyMap(),
    val actNumbers: Map<Int, Int> = emptyMap(),
) {
    val medals get() = honors.filter { it.kind == com.ilustris.sagai.features.progression.data.model.HonorKind.MEDAL }
    val titles get() = honors.filter { it.kind == com.ilustris.sagai.features.progression.data.model.HonorKind.TITLE }
}

/** What the characters list shows on a character: the level, the latest medal's emoji, the legacy and the memorial mark. */
data class CharacterBadge(
    val level: Int,
    val lastMedalEmoji: String?,
    val hasLegacy: Boolean,
    val inMemory: Boolean,
)

/** What one event meant for one character, as the timeline shows it: the XP, the level reached and why. */
data class EventXp(
    val xp: Int,
    val levelAfter: Int,
    val leveledUp: Boolean,
    val feats: List<com.ilustris.sagai.features.progression.data.model.CharacterFeat>,
)
