package com.ilustris.sagai.core.ai.model

import android.graphics.Bitmap

data class ImageReference(
    val bitmap: Bitmap,
    val description: String,
)

/**
 * Strictness level for the image prompt reviewer agent.
 * Each level has a description that explains to the AI what its role is.
 */
enum class ReviewerStrictness(
    val description: String,
) {
    /**
     * Flexible review - only fix critical violations that would break the image generation.
     * Allows artistic interpretation within the art style boundaries.
     * Best for styles with inherent flexibility like oil paintings or traditional art.
     */
    LENIENT(
        "You are a LENIENT reviewer. Only fix CRITICAL violations that would break the image: " +
            "wrong framing (e.g., full body when portrait requested), completely missing backgrounds when mandatory, " +
            "or severe art style violations (e.g., '3D render' when style forbids it). " +
            "Allow artistic interpretation and minor deviations. Focus on preventing generation failures, not perfection.",
    ),

    /**
     * Conservative review - fix clear violations but preserve artistic intent.
     * This is the balanced default for most styles.
     */
    CONSERVATIVE(
        "You are a CONSERVATIVE reviewer. Fix clear violations: " +
            "incorrect framing (body parts outside camera view), " +
            "banned terminology from the art style (e.g., 'brown eyes' when dots required), " +
            "missing mandatory elements (backgrounds, environment details when required by style). " +
            "Preserve the original description's artistic intent and personality. " +
            "Only change what clearly violates the rules.",
    ),

    /**
     * Strict review - enforce all art style rules precisely.
     * Best for highly stylized art with specific requirements like cartoon styles.
     */
    STRICT(
        "You are a STRICT reviewer. Enforce ALL art style rules precisely: " +
            "exact framing compliance, zero tolerance for banned terms, mandatory inclusion of all required elements, " +
            "precise anatomy terminology matching the style (e.g., 'cartoon proportions' not 'realistic'), " +
            "complete background descriptions when required. Rewrite sections if needed to achieve full compliance.",
    ),
}

/**
 * Categories of violations that can be detected by the reviewer.
 * Used for analytics and metrics tracking.
 */
@Suppress("unused")
enum class ViolationType {
    // ========== CINEMATOGRAPHY VIOLATIONS ==========

    /** Camera angle not specified when required (e.g., missing 'low-angle' specification) */
    CAMERA_ANGLE_MISSING,

    /** Camera angle contradicts visual direction (e.g., 'eye-level' when 'low-angle 45°' specified) */
    CAMERA_ANGLE_WRONG,

    /** Focal length/perspective characteristics don't match (e.g., no distortion mentioned for wide-angle lens) */
    FOCAL_LENGTH_MISMATCH,

    /** Framing issue - describing body parts not visible in the camera view */
    FRAMING_VIOLATION,

    /** Subject placement in frame not specified or wrong (e.g., missing 'centered' or 'lower third' positioning) */
    PLACEMENT_MISSING,

    /** Depth of field characteristics missing (e.g., no mention of bokeh for shallow DOF) */
    DEPTH_OF_FIELD_MISSING,

    /** Lighting direction or quality contradicts visual direction (e.g., 'soft' when 'hard spotlight' specified) */
    LIGHTING_WRONG,

    /** Lighting direction not specified when required */
    LIGHTING_MISSING,

    /** Color temperature/palette doesn't match visual direction (e.g., 'warm' when 'cool blue' specified) */
    COLOR_PALETTE_WRONG,

    /** Atmospheric quality missing (e.g., no mention of haze, fog, clarity as specified) */
    ATMOSPHERE_MISSING,

    /** Environmental context missing or wrong (e.g., 'studio' when 'urban street' specified) */
    ENVIRONMENT_MISSING,

    /** Perspective distortion not captured (e.g., missing converging lines for low-angle wide shot) */
    PERSPECTIVE_MISSING,

    /** Signature visual detail from direction not mentioned */
    SIGNATURE_DETAIL_MISSING,

    /** Technical jargon (degrees, mm, f-stops) not translated into visual descriptions */
    TECHNICAL_JARGON_NOT_TRANSLATED,

    // ========== ART STYLE VIOLATIONS ==========

    /** Banned terminology - using forbidden words from the art style (e.g., eye colors for PUNK_ROCK) */
    BANNED_TERMINOLOGY,

    /** Missing required elements - background missing when mandatory, environment not described */
    MISSING_ELEMENTS,

    /** Wrong anatomy description - realistic terms when style requires stylized (e.g., 'realistic proportions' in cartoon) */
    ANATOMY_MISMATCH,

    /** Art style contradiction - describing techniques that contradict the medium (e.g., 'soft gradient' in cel-shaded style) */
    STYLE_CONTRADICTION,

    // ========== VISIBILITY VIOLATIONS ==========

    /** Describes body parts/clothing that are out of frame according to visibility matrix (e.g., pants when legs not visible) */
    VISIBILITY_VIOLATION,

    // ========== POSE & EXPRESSION VIOLATIONS ==========

    /** Missing or generic facial expression - no specific emotion described */
    MISSING_FACIAL_EXPRESSION,

    /** Missing or generic dynamic pose - character appears static/posed instead of in motion */
    MISSING_DYNAMIC_POSE,

    /** Expression and pose contradict emotionally (e.g., 'tender smile' with 'aggressive stance') */
    POSE_EXPRESSION_CONTRADICTION,

    /** Character appears posed for portrait rather than caught in a moment */
    POSE_EXPRESSION_VIOLATION,
}

/**
 * Severity level for each violation.
 * CRITICAL = breaks image generation or produces completely wrong output
 * MAJOR = significantly degrades quality or misses key requirements
 * MINOR = small deviation that might affect polish but not core functionality
 */
@Suppress("unused")
enum class ViolationSeverity {
    CRITICAL, // Must fix - will break the image
    MAJOR, // Should fix - significantly wrong but might work
    MINOR, // Nice to fix - small improvement
}

/**
 * A single detected violation with context.
 */
data class PromptViolation(
    val type: ViolationType?,
    val severity: ViolationSeverity,
    val description: String,
    val example: String? = null,
)

/**
 * Result of the prompt review process.
 * Contains the corrected prompt and analytics about what changed.
 */
data class ImagePromptReview(
    val originalPrompt: String,
    val correctedPrompt: String,
    val violations: List<PromptViolation>,
    val changesApplied: List<String>, // Human-readable list of fixes
    val artistImprovementSuggestions: String? = null, // Feedback to help the artist improve future prompts
    val wasModified: Boolean,
) {
    /**
     * Analytics: Was this prompt "completely wrong" or just "improved"?
     * Completely wrong = has CRITICAL violations
     * Improved = only MAJOR or MINOR violations
     */
    val isCompletelyWrong: Boolean
        get() = violations.any { it.severity == ViolationSeverity.CRITICAL }

    @Suppress("unused")
    val violationsBySeverity: Map<ViolationSeverity, Int>
        get() = violations.groupingBy { it.severity }.eachCount()

    /**
     * Determines image quality for analytics based on violation count and severity.
     * GOOD: No violations
     * MEDIUM: 1-2 violations or only minor violations
     * BAD: 3+ violations or has critical violations
     */
    fun getQualityLevel(): String =
        when {
            violations.isEmpty() -> "good"
            isCompletelyWrong -> "bad"
            violations.size >= 3 -> "bad"
            else -> "medium"
        }
}
