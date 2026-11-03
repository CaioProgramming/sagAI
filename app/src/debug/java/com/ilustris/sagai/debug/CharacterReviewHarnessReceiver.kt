package com.ilustris.sagai.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ilustris.sagai.features.characters.data.model.designChangesTo
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Runs the character design reviewer over an already saved sheet and logs before/after to logcat
 * (tag [TAG]) without persisting anything. The saga's cast it compares against leaves the reviewed
 * character out, as it would at creation time. `seed` stands in for the discovery context that
 * asked for the character.
 */
@AndroidEntryPoint
class CharacterReviewHarnessReceiver : BroadcastReceiver() {
    @Inject lateinit var characterUseCase: CharacterUseCase

    @Inject lateinit var sagaHistoryUseCase: SagaHistoryUseCase

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val characterId = intent.getIntExtra("characterId", -1)
        val seed = intent.getStringExtra("seed").orEmpty()
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val character = characterUseCase.getCharacterById(characterId)
                val saga = character?.let { sagaHistoryUseCase.getSagaById(it.sagaId).first() }
                if (character == null || saga == null) {
                    Log.w(TAG, "character $characterId not found")
                    return@launch
                }
                val castBeforeIt = saga.copy(characters = saga.characters.filter { it.data.id != characterId })
                val reviewed = characterUseCase.reviewCharacterDesign(castBeforeIt, character, seed)
                val changes = character.designChangesTo(reviewed)
                Log.i(TAG, "=== ${character.name} (id $characterId): ${changes.size} field(s) rewritten ===")
                changes.forEach { (field, before, after) ->
                    Log.i(TAG, "[$field]\n  BEFORE: $before\n  AFTER:  $after")
                }
            } catch (e: Exception) {
                Log.e(TAG, "harness failed", e)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "CharacterReviewHarness"
    }
}
