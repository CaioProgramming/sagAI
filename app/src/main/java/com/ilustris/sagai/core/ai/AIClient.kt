package com.ilustris.sagai.core.ai

import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.key.ApiUsageTracker
import com.ilustris.sagai.core.ai.key.QuotaStatus
import com.ilustris.sagai.core.ai.key.QuotaStatusService
import com.ilustris.sagai.core.ai.key.UserApiKeyStore
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.core.database.source.AIAuditLogDao
import com.ilustris.sagai.core.services.AgeVerificationService
import com.ilustris.sagai.core.services.RemoteConfigService
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.Locale

enum class ModelRequirement {
    MINIMAL,
    LOW,
    MEDIUM,
    HIGH,
}

/**
 * Fallback mapping from tier to thinking level, for tiers that declare no `thinkingLevel`.
 *
 * The API's values are lowercase `minimal`/`low`/`medium`/`high`, so the enum name can't be sent
 * raw. Measured on `gemma-4-31b-it`, an uppercase level is tolerated rather than rejected — it
 * quietly falls back to the model's default — which is why sending the enum name went unnoticed
 * until a tier was pointed at a Gemini 3 model, where it is a hard 400.
 *
 * [ModelRequirement.MINIMAL] maps to `minimal` rather than up to `low`. It used to map up, because
 * not every model offers `minimal` and a rejection broke the tier; that fear is now handled by
 * [GeminiAIClient.effectiveThinkingLevel], which learns the refusal and degrades on its own. The
 * old mapping also had it backwards in practice — measured on `gemini-3.6-flash`, `low` spends 87
 * reasoning tokens against the model's own default of 69, so the cheapest tier was thinking the
 * hardest.
 */
fun ModelRequirement.toGeminiThinkingLevel(): String =
    when (this) {
        ModelRequirement.MINIMAL -> "minimal"
        ModelRequirement.LOW -> "low"
        ModelRequirement.MEDIUM -> "medium"
        ModelRequirement.HIGH -> "high"
    }

abstract class AIClient(
    protected val remoteConfigService: RemoteConfigService,
    protected val promptService: PromptService,
    protected val ageVerificationService: AgeVerificationService,
    @PublishedApi internal val aiAuditLogDao: AIAuditLogDao,
    @PublishedApi internal val userApiKeyStore: UserApiKeyStore,
    @PublishedApi internal val quotaStatusService: QuotaStatusService,
    @PublishedApi internal val modelCatalog: ModelCatalog,
    @PublishedApi internal val apiUsageTracker: ApiUsageTracker,
    @PublishedApi internal val modelFallbackNotifier: ModelFallbackNotifier,
) {
    fun getLanguage(requireTranslation: Boolean = true): String {
        val locale = if (requireTranslation) Locale.getDefault() else Locale.US
        val languageName = locale.getDisplayName(locale)
        return languageName.ifBlank { locale.toLanguageTag() }
    }

    fun buildDataStructure(
        requirement: ModelRequirement,
        describeOutput: Boolean,
        type: Type,
        filterOutputFields: List<String>,
    ): Pair<String, String> {
        val typeName = validateType(type)
        val structure = validateStructure(describeOutput, filterOutputFields, typeName, type)

        return typeName to structure
    }

    fun validateStructure(
        describeOutput: Boolean,
        filterOutputFields: List<String>,
        dataTypeName: String,
        type: Type,
    ): String =
        if (describeOutput) {
            buildAIPromptOutputStructure(type, filterOutputFields)
        } else {
            dataTypeName
        }

    fun validateType(dataType: Type): String =
        when (dataType) {
            is Class<*> -> {
                dataType.simpleName
            }

            is ParameterizedType -> {
                (dataType.rawType as? Class<*>)?.simpleName
                    ?: dataType.toString()
            }

            else -> {
                dataType.toString().substringAfterLast(".")
            }
        }



    suspend fun buildCorePrompt(
        requirement: ModelRequirement,
        requireTranslation: Boolean,
        dataTypeName: String,
        structure: String,
    ): SplitPrompt {
        val userAge = ageVerificationService.getUserAgeGroup()
        return promptService.buildSplitBlueprint(
            remoteConfigKey = getCoreBlueprintKey(requirement),
            variables =
                mapOf(
                    "type" to dataTypeName,
                    "structure" to structure,
                    "language" to getLanguage(requireTranslation),
                    "userAge" to userAge.name,
                ),
            logEnabled = false,
        )
    }

    suspend fun buildCoreInstructions(
        requirement: ModelRequirement,
        requireTranslation: Boolean,
        dataTypeName: String,
        structure: String,
    ) = buildMap {
        val config = buildCorePrompt(requirement, requireTranslation, dataTypeName, structure)
        put("core", config.renderInstructions())
        put("structure", config.processedTemplate)
    }

    suspend fun buildUnifiedInstructions(
        requirement: ModelRequirement,
        requireTranslation: Boolean,
        dataTypeName: String,
        outputStructure: String,
        userInteraction: Boolean,
        blueprintInstructions: Map<String, Any>,
    ): Map<String, Any> {
        val coreInstructions =
            buildCoreInstructions(requirement, requireTranslation, dataTypeName, outputStructure)
        val safetyInstructions =
            if (userInteraction) {
                buildSafetyInstructions()
            } else {
                emptyMap()
            }

        return buildMap {
            put(
                "CoreDefinitions",
                buildMap {
                    putAll(coreInstructions)
                    if (safetyInstructions.isNotEmpty()) {
                        put("SafetyGuardRails", safetyInstructions)
                    }
                },
            )
            if (blueprintInstructions.isNotEmpty()) {
                put("TaskInstructions", blueprintInstructions)
            }
        }
    }

    suspend fun buildSafetyInstructions(): Map<String, Any> {
        val userAge = ageVerificationService.getUserAgeGroup()
        val safetyPrompt =
            promptService.buildSplitBlueprint(
                "safety_guardrails_blueprint",
                mapOf(
                    "userAge" to userAge.name,
                ),
            )
        return buildMap {
            putAll(safetyPrompt.renderInstructions())
            put("SafetyVerification", safetyPrompt.processedTemplate)
        }
    }

    suspend fun buildBlueprintPrompt(
        remoteConfigKey: String,
        variables: Map<String, String> = emptyMap(),
        mergedInstructionMaps: List<Map<String, Any>> = emptyList(),
        logEnabled: Boolean = true,
    ): SplitPrompt {
        var prompt =
            promptService.buildSplitBlueprint(
                remoteConfigKey,
                variables,
                logEnabled = logEnabled,
            )
        mergedInstructionMaps.forEach { prompt = prompt.mergeInstructions(it) }
        return prompt
    }

    fun getCoreBlueprintKey(requirement: ModelRequirement): String =
        if (requirement == ModelRequirement.HIGH) {
            "core_blueprint"
        } else {
            "core_${requirement.name.lowercase()}_blueprint"
        }

    /**
     * The `thinkingLevel` to send for [requirement] on [model].
     *
     * Read from `model_configs` rather than derived from the tier's name, because the two are not
     * the same question: the tier says how hard the task is, the level says how much the model
     * should deliberate, and only the second is a thing the API accepts.
     *
     * Resolution order, so the existing config keeps working while it migrates:
     *  1. an explicit `thinkingLevel` on the tier — the intended way;
     *  2. `thinkingEnabled: false` → `minimal`;
     *  3. otherwise, the old tier-derived mapping.
     *
     * Note what case 2 fixes. `thinkingEnabled: false` used to send no `thinkingConfig` at all,
     * which does not disable thinking — the model thinks at its own default, and because
     * `includeThoughts` goes unsent with it, we paid for reasoning tokens and got no summaries
     * back. Measured: `gemini-3.6-flash` no config ⇒ 69 thought tokens, `minimal` ⇒ 0;
     * `gemma-4-31b-it` no config ⇒ 33, `minimal` ⇒ 0.
     *
     * That second pair is the one to remember. Gemma 4 honours `thinkingConfig` — older Gemma
     * ignored it, and code comments here used to say so — so the level is not decorative on the
     * Gemma tiers either. It buys latency and output tokens, though not input-quota headroom:
     * thought tokens are output, and the free tier's blocking limit is
     * `generate_content_free_tier_input_token_count`.
     */
    suspend fun thinkingLevel(
        requirement: ModelRequirement,
        model: String,
    ): String {
        val tierConfig =
            (remoteConfigService.getJsonMapStringAny("model_configs") ?: emptyMap())
                .let { it[requirement.name] as? Map<*, *> }

        val configured =
            (tierConfig?.get("thinkingLevel") as? String)
                ?.lowercase()
                ?.takeIf { it in ACCEPTED_THINKING_LEVELS }

        val resolved =
            configured
                ?: if (tierConfig?.get("thinkingEnabled") as? Boolean == false) {
                    MINIMAL_THINKING
                } else {
                    requirement.toGeminiThinkingLevel()
                }

        // `minimal` is not offered by every model — gemini-3.7-flash rejects it outright — so a
        // tier pointed at one of those degrades to the next level up instead of failing. This is
        // the one direction proven in production, kept exactly as it was.
        if (resolved == MINIMAL_THINKING && !modelCatalog.supportsThinkingLevel(model, MINIMAL_THINKING)) {
            return "low"
        }

        // Any other level: a 503 retry can now hand this call a model nobody asked for [resolved]
        // before — the Gemma model backing a fallback normally only ever serves the LOW tier's
        // `minimal`, so whether it tolerates `high` is untested the first time it happens. Step
        // down the ladder toward less reasoning, skipping only levels this exact model has already
        // taught us (via a prior 400) that it rejects, instead of resending the same rejected value
        // until the retry budget runs out.
        val startIndex = THINKING_LEVEL_LADDER.indexOf(resolved)
        if (startIndex < 0) return resolved
        return THINKING_LEVEL_LADDER
            .drop(startIndex)
            .firstOrNull { modelCatalog.supportsThinkingLevel(model, it) }
            ?: THINKING_LEVEL_LADDER.last()
    }

    /**
     * [requirement]'s configured candidates, best first — [availableModelsFor]'s own
     * `availableModels` list when present, else the tier's single `model` as a one-element list.
     * Never empty when the tier config itself is valid; throws/errors the same way [modelName]
     * always did when it isn't.
     */
    private suspend fun candidateModelsFor(requirement: ModelRequirement): List<String> {
        val tierConfig =
            remoteConfigService.getJsonMapStringAny("model_configs") ?: emptyMap()
        return when (val config = tierConfig[requirement.name]) {
            is String -> listOf(config.replace("models/", ""))

            is Map<*, *> -> {
                val enabled = config["enabled"] as? Boolean ?: true
                if (!enabled) {
                    throw ModelOutageException(
                        requirement,
                        config["model"] as? String ?: "UNKNOWN",
                    )
                }
                val primary =
                    config["model"] as? String
                        ?: error("Model name not found in config for ${requirement.name}")
                // Optional, additive: a tier with only `model` set behaves exactly as before.
                // Order is preference — best/most-capable first — read as-is, never resorted.
                val available =
                    (config["availableModels"] as? List<*>)
                        ?.mapNotNull { it as? String }
                        ?.map { it.replace("models/", "") }
                        ?.filter { it.isNotBlank() }
                        ?.takeIf { it.isNotEmpty() }
                available ?: listOf(primary.replace("models/", ""))
            }

            else -> {
                Timber.e("Invalid model configuration for ${requirement.name}: $config")
                error("Invalid model configuration for ${requirement.name}")
            }
        }
    }

    /**
     * [requirement]'s model to actually use right now: the first of [candidateModelsFor] that
     * isn't sitting on a spent daily quota, or the first candidate regardless if every one of them
     * is — same as before this existed, a single-candidate tier always returns that one candidate,
     * daily quota or not, and lets the normal 429 handling explain why.
     */
    suspend fun modelName(requirement: ModelRequirement): String {
        val candidates = candidateModelsFor(requirement)
        return candidates.firstOrNull { !quotaStatusService.isModelDailyExhausted(it) }
            ?: candidates.first()
    }

    /**
     * How many distinct models [requirement] could rotate through — [GeminiGenerationPolicy]'s
     * retry budget is sized against this so a tier with more than one configured candidate is
     * guaranteed at least one attempt per candidate, and one with only its old single `model`
     * field keeps exactly the attempt count it always had.
     */
    @PublishedApi
    internal suspend fun candidateModelCount(requirement: ModelRequirement): Int = candidateModelsFor(requirement).size

    /**
     * [requirement]'s own daily-quota status — [QuotaStatusService.statusForModels] scoped to its
     * configured candidates, so a UI gating input on this only blocks once the whole tier is
     * actually out, not the moment any one of its several models is.
     */
    suspend fun tierQuotaStatus(requirement: ModelRequirement): Flow<QuotaStatus> =
        quotaStatusService.statusForModels(candidateModelsFor(requirement))

    /**
     * Whether the key can generate *anything*, anywhere in the app — every tier's own configured
     * candidates, pooled together, so [QuotaStatus.DailyExhausted] only comes back once none of
     * them have anywhere left to go. What a truly global "you're done for today" notice should
     * gate on: with each tier able to rotate through several models now, one tier alone running
     * dry no longer means the key itself is out — that only follows once all four are.
     */
    suspend fun allTiersQuotaStatus(): Flow<QuotaStatus> {
        val allCandidates = ModelRequirement.entries.flatMap { candidateModelsFor(it) }.distinct()
        return quotaStatusService.statusForModels(allCandidates)
    }

    /**
     * The next candidate after [excluding] in [requirement]'s own `availableModels` — a same-tier,
     * separately-quota'd sibling to try on a 503 before conceding to [fallbackModelName]'s full
     * tier-drop. Null when the tier has no further candidate (no `availableModels` configured,
     * [excluding] is its last/only one, or every remaining candidate is daily-exhausted).
     */
    suspend fun siblingModelName(
        requirement: ModelRequirement,
        excluding: String,
    ): String? {
        val candidates = candidateModelsFor(requirement)
        if (candidates.size < 2) return null
        val normalizedExcluding = excluding.replace("models/", "")
        val currentIndex = candidates.indexOf(normalizedExcluding)
        if (currentIndex == -1) return null
        return candidates
            .drop(currentIndex + 1)
            .firstOrNull { !quotaStatusService.isModelDailyExhausted(it) }
    }

    /**
     * The model a 503 ("this model is currently experiencing high demand") falls over to for
     * [requirement] once [siblingModelName] has nothing left to offer, or null if this tier has
     * nowhere sensible to fall back to.
     *
     * Always LOW's own configured model, never a separate value — the free-tier model backing
     * LOW is already Gemma, a different serving stack from Gemini, and it is presumably the
     * cheapest, most available thing this app already trusts in production, since a tier is
     * already running real traffic against it. Pointing at it *by reference* rather than copying
     * its name into a second config field is deliberate: if the model backing LOW is ever swapped
     * (a future Gemma generation, say), every tier's fallback follows automatically, with nothing
     * to update here or in Remote Config.
     *
     * MINIMAL and LOW have nowhere to fall back to — they already run whatever LOW runs — so this
     * only ever returns something for MEDIUM and HIGH.
     *
     * A disabled LOW tier ([ModelOutageException], the existing per-tier kill switch) is treated
     * as "no fallback available" rather than left to propagate: pulling that switch is a decision
     * about LOW, and it should not also change how an unrelated MEDIUM/HIGH request behaves on a
     * 503 that has nothing to do with it.
     */
    suspend fun fallbackModelName(requirement: ModelRequirement): String? {
        if (requirement == ModelRequirement.MINIMAL || requirement == ModelRequirement.LOW) return null
        return runCatching { modelName(ModelRequirement.LOW) }.getOrNull()
    }

    /**
     * The key every Gemini call runs on — the user's own, decrypted from [UserApiKeyStore].
     *
     * This used to take a `useCore` flag choosing the `SAGA_CORE` credential over `FIREBASE_KEY`,
     * two keys of ours served by Remote Config. Their point was separate quota pools, which never
     * held: quota is per Google Cloud project, so keys of the same project share one budget. Under
     * BYOK there is a single credential and nothing left to choose between.
     */
    suspend fun apiConfig(model: String): String {
        ensureQuotaAvailable(model)
        return userApiKeyStore.getKeyNow()?.takeIf { it.isNotBlank() }
            ?: throw MissingApiKeyException()
    }

    /**
     * Refuses a call [model] specifically has already lost its daily quota on, before it costs
     * anything.
     *
     * Lives here rather than at the generation entry points because there are far more of those
     * than the obvious two — book export, image generation, character creation, milestone beats,
     * the epilogue chat — and gating them one screen at a time guarantees the ones nobody
     * remembered keep failing mutely. Every text generation, sync and streaming alike, resolves its
     * credential through [apiConfig], so this is the one road they all take.
     *
     * Checked per-[model] rather than [QuotaStatusService.activeDailyBlock]'s old aggregate: with a
     * tier now able to name more than one candidate model, "some model somewhere is exhausted" and
     * "the specific model this call is about to use is exhausted" stopped being the same question —
     * the whole point of a same-tier sibling is that it keeps working while another one is spent.
     */
    @PublishedApi
    internal suspend fun ensureQuotaAvailable(model: String) {
        quotaStatusService.dailyExhaustionFor(model)?.let { until ->
            throw QuotaExhaustedException(until = until, model = model)
        }
    }

    @PublishedApi
    internal fun assembleGeminiRequest(block: GeminiRequestBuilder.() -> Unit): GeminiRequestAssembly = geminiRequest(block)

    @PublishedApi
    internal suspend fun recordAudit(
        snapshot: AIAuditSnapshot,
        logEnabled: Boolean = true,
    ) {
        if (!BuildConfig.DEBUG || !logEnabled) return
        persistAuditLog(snapshot.toEntity())
    }

    @PublishedApi
    internal suspend fun persistAuditLog(log: AIAuditLog) {
        try {
            aiAuditLogDao.insertLog(log)
        } catch (e: Exception) {
            Timber.tag(javaClass.simpleName).e("Error saving audit log: ${e.message}")
        }
    }
}

private const val MINIMAL_THINKING = "minimal"

private val ACCEPTED_THINKING_LEVELS = setOf(MINIMAL_THINKING, "low", "medium", "high")

/** Most to least reasoning — the order [thinkingLevel] steps down when a model rejects one. */
internal val THINKING_LEVEL_LADDER = listOf("high", "medium", "low", MINIMAL_THINKING)

val AI_EXCLUDED_FIELDS =
    listOf(
        "text\$delegate",
        "functionResponse\$delegate",
        "functionCall\$delegate",
        "functionCalls\$delegate",
        "\"inlineDataParts\$delegate",
    )
