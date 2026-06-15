package com.ilustris.sagai.core.ai.model

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

/**
 * A Blueprint is a structured prompt configuration fetched from [FirebaseRemoteConfig].
 * It separates the AI's role and rules from the dynamic task template, enabling the
 * **"Split & Merge"** architecture in [com.ilustris.sagai.core.ai.GemmaClient].
 *
 * ## Static vs. Dynamic Split
 *
 * Fields are treated as one of two categories:
 *
 * ### Instructions (Static — sent as `system_instruction`)
 * These never receive `{key}` placeholder substitution. They define the AI persona and governance.
 * - **role**: Defines the AI identity. Maps to the `IDENTITY` instruction bucket.
 * - **directives**: High-level stylistic instructions. Maps to the `MODULE DIRECTIVES` bucket.
 * - **rules**: Narrative/technical constraints. Maps to the `RULES` bucket.
 * - **instructions**: Additional category-keyed instruction buckets (e.g. `"DIALECT"`, `"TONE"`).
 *   Use this to define style or persona layers that are resolved entirely via Remote Config keys.
 *   The outer key becomes a level-1 Markdown header, and the inner key a level-2 header.
 *
 * ### Data (Dynamic — sent as `contents` / user turn)
 * These fields support `{key}` placeholder substitution with runtime values.
 * - **template**: The narrative bridge. The **ONLY** field that supports `{key}` replacement.
 * - **examples**: Few-shot examples rendered after placeholder substitution.
 *
 * ## CRITICAL GUIDELINES
 * 1. **Writer-First**: Role, Directives, and Rules MUST focus on tone, persona, and narrative style.
 * 2. **Context-Only Template**: Only use the template for variable injection (e.g., `{sagaContext}`).
 * 3. **No JSON Meta**: Do NOT instruct the AI on JSON structures or output formats within the blueprint.
 *    The [com.ilustris.sagai.core.ai.GemmaClient] automatically injects the required JSON structure and
 *    formatting rules into the final prompt based on the target data type.
 * 4. **Tone via `instructions`**: Never add a global tone parameter. Tone and style layers must be
 *    defined per-blueprint via the `instructions` map and resolved through Remote Config keys.
 */
data class PromptBlueprint(
    val title: String = "",
    val role: String = "",
    val template: String = "",
    val directives: Map<String, String> = emptyMap(),
    val rules: Map<String, String> = emptyMap(),
    val examples: List<Map<String, String>> = emptyList(),
    val omitHeaders: Boolean = false,
    /**
     * Additional static instruction buckets beyond the standard role/directives/rules.
     *
     * The outer key is the **category name** (e.g. `"DIALECT"`, `"TONE"`, `"WRITING STYLE"`),
     * rendered as a level-1 Markdown header in the merged `system_instruction`.
     * The inner key is the **rule name**, rendered as a level-2 header.
     *
     * Example Remote Config JSON:
     * ```json
     * "instructions": {
     *   "DIALECT": {
     *     "Language Preference": "Always write in the user's native language."
     *   }
     * }
     * ```
     */
    val instructions: Map<String, Any>? = null,
)
