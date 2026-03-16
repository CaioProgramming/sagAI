package com.ilustris.sagai.core.ai.prompts

data class PromptDirectives(
    val directives: Map<String, String> = emptyMap(),
) {
    fun get(key: String): String = directives[key] ?: ""

    val roleDefinition get() = get("ROLE_DEFINITION")
    val namingDirective get() = get("NAMING_DIRECTIVE")
    val continuityFactsRules get() = get("CONTINUITY_MANDATE")
    val individualKnowledgeRules get() = get("KNOWLEDGE_BOUNDARIES")
    val progressionDirective get() = get("INTENT_RECOGNITION")
    val preferredLanguage get() = get("PREFERRED_LANGUAGE")
    val outputJsonDirective get() = get("OUTPUT_DIRECTIVE")
    val structureDirective get() = get("STRUCTURE_DIRECTIVE")
    val jsonStringIntegrity get() = get("JSON_STRING_INTEGRITY")

    // Feature specific directives (to be moved to feature prompts)
    val recentContext get() = get("recentContext")
    val characterRelationshipLabel get() = get("characterRelationshipLabel")
    val notificationRoleMain get() = get("notificationRoleMain")
    val notificationRoleNPC get() = get("notificationRoleNPC")
    val conversationHistory get() = get("conversationHistory")
    val characterResumeNoEvents get() = get("characterResumeNoEvents")
    val characterResumeNoRelationships get() = get("characterResumeNoRelationships")
}
