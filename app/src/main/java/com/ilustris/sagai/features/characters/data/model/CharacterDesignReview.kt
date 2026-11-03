package com.ilustris.sagai.features.characters.data.model

/**
 * What the design reviewer sends back after auditing a freshly generated sheet: only the fields it
 * rewrote. A blank or null field (the model leaves either) means the original passed the audit and
 * stays as it is.
 */
data class CharacterDesignReview(
    val hair: String? = null,
    val eyes: String? = null,
    val mouth: String? = null,
    val distinctiveMarks: String? = null,
    val distinguishFeatures: String? = null,
    val buildAndPosture: String? = null,
    val outfitDescription: String? = null,
    val accessories: String? = null,
    val personality: String? = null,
    val artwork: String? = null,
    /** One line on what failed and what changed; only for logs. */
    val rationale: String? = null,
)

/** Applies the rewritten fields of [review] on this sheet; blank fields keep what the sheet already says. */
fun Character.withDesignReview(review: CharacterDesignReview): Character {
    fun String?.orKeep(current: String) = this?.trim().orEmpty().ifBlank { current }
    val facial = details.physicalTraits.facialDetails
    val body = details.physicalTraits.bodyFeatures
    val clothing = details.clothing
    return copy(
        details =
            details.copy(
                physicalTraits =
                    details.physicalTraits.copy(
                        facialDetails =
                            facial.copy(
                                hair = review.hair.orKeep(facial.hair),
                                eyes = review.eyes.orKeep(facial.eyes),
                                mouth = review.mouth.orKeep(facial.mouth),
                                distinctiveMarks = review.distinctiveMarks.orKeep(facial.distinctiveMarks),
                            ),
                        bodyFeatures =
                            body.copy(
                                distinguishFeatures = review.distinguishFeatures.orKeep(body.distinguishFeatures),
                                buildAndPosture = review.buildAndPosture.orKeep(body.buildAndPosture),
                            ),
                    ),
                clothing =
                    clothing.copy(
                        outfitDescription = review.outfitDescription.orKeep(clothing.outfitDescription),
                        accessories = review.accessories.orKeep(clothing.accessories),
                    ),
            ),
        profile = profile.copy(personality = review.personality.orKeep(profile.personality)),
        artwork = review.artwork.orKeep(artwork.orEmpty()),
    )
}
