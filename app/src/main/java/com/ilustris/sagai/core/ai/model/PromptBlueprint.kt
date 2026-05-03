package com.ilustris.sagai.core.ai.model

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

/**
 * A Blueprint is a structured prompt configuration fetched from [FirebaseRemoteConfig].
 * It separates the AI's role and rules from the dynamic task template.
 *
 * **Architecture Principles:**
 * - **role**: Defines the AI identity. Static.
 * - **directives**: High-level stylistic instructions. Static.
 * - **rules**: Dynamic constraints or technical requirements. Static (no {key} replacement).
 * - **template**: The narrative bridge. The ONLY field that supports {key} replacement for dynamic context.
 *
 * **CRITICAL GUIDELINES:**
 * 1. **Writer-First**: Role, Directives, and Rules MUST focus on tone, persona, and narrative style.
 * 2. **Context-Only Template**: Only use the template field for variable injection (e.g., {sagaContext}).
 * 3. **No JSON Meta**: Do NOT instruct the AI on JSON structures or output formats within the blueprint.
 *    The [com.ilustris.sagai.core.ai.GemmaClient] automatically injects the required JSON structure and
 *    formatting rules into the final prompt based on the target data type.
 */
data class PromptBlueprint(
    val title: String = "",
    val role: String = "",
    val template: String = "",
    val directives: Map<String, String> = emptyMap(),
    val rules: Map<String, String> = emptyMap(),
    val examples: List<Map<String, String>> = emptyList(),
    val omitHeaders: Boolean = false,
)
