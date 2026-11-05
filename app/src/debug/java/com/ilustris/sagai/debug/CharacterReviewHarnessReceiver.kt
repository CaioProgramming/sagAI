package com.ilustris.sagai.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ilustris.sagai.features.characters.data.model.designChangesTo
import com.ilustris.sagai.features.characters.data.model.designFields
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.debug.data.CharacterPreviewGenerator
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
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

    @Inject lateinit var characterPreviewGenerator: CharacterPreviewGenerator

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == STEP_GENERATE || intent.action == STEP_REVIEW) {
            previewStep(context, intent)
            return
        }
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

    /**
     * One half of a preview character per broadcast, so each stays under the 60 s a broadcast is allowed
     * and the app keeps its network: STEP_GENERATE writes the sheet as generated to a file, STEP_REVIEW
     * reviews it against the cast so far and adds it to the cast file. `reset` clears the cast.
     */
    private fun previewStep(
        context: Context,
        intent: Intent,
    ) {
        val genre = Genre.valueOf(intent.getStringExtra("genre") ?: Genre.HEROES.name)
        val castFile = File(context.cacheDir, "preview_cast.json")
        val pendingFile = File(context.cacheDir, "preview_pending.json")
        if (intent.getBooleanExtra("reset", false)) castFile.delete()
        val gson = Gson()
        val cast: List<Character> =
            if (castFile.exists()) gson.fromJson(castFile.readText(), object : TypeToken<List<Character>>() {}.type) else emptyList()
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (intent.action == STEP_GENERATE) {
                    val sheet = characterPreviewGenerator.generateSheet(genre, cast)
                    pendingFile.writeText(gson.toJson(sheet))
                    Log.i(
                        TAG,
                        "#${cast.size + 1} ${sheet.name} ${sheet.lastName.orEmpty()} GENERATED\n" +
                            sheet.designFields().filter { it.first != "artwork" }.joinToString("\n") { "  ${it.first}: ${it.second}" },
                    )
                } else {
                    val sheet = gson.fromJson(pendingFile.readText(), Character::class.java)
                    val reviewed = characterPreviewGenerator.review(genre, cast, sheet)
                    val changes = sheet.designChangesTo(reviewed)
                    Log.i(TAG, "#${cast.size + 1} REVIEW: ${changes.size} field(s) rewritten")
                    changes.filter { it.first != "artwork" }.forEach { (field, _, after) -> Log.i(TAG, "  $field -> $after") }
                    castFile.writeText(gson.toJson(cast + reviewed))
                }
                Log.i(TAG, "step ${intent.action} done")
            } catch (e: Exception) {
                Log.e(TAG, "step ${intent.action} failed: ${e.message}", e)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "CharacterReviewHarness"
        const val STEP_GENERATE = "com.ilustris.sagai.debug.PREVIEW_GENERATE"
        const val STEP_REVIEW = "com.ilustris.sagai.debug.PREVIEW_REVIEW"
    }
}
