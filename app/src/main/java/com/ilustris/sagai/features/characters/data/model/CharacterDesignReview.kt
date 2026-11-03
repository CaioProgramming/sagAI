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

/** The sheet fields the design review can rewrite, by name, to show what a review changed. */
fun Character.designFields() =
    listOf(
        "hair" to details.physicalTraits.facialDetails.hair,
        "eyes" to details.physicalTraits.facialDetails.eyes,
        "mouth" to details.physicalTraits.facialDetails.mouth,
        "distinctiveMarks" to details.physicalTraits.facialDetails.distinctiveMarks,
        "distinguishFeatures" to details.physicalTraits.bodyFeatures.distinguishFeatures,
        "buildAndPosture" to details.physicalTraits.bodyFeatures.buildAndPosture,
        "outfitDescription" to details.clothing.outfitDescription,
        "accessories" to details.clothing.accessories,
        "personality" to profile.personality,
        "artwork" to artwork.orEmpty(),
    )

/** The [designFields] that differ between this sheet and its [reviewed] version, as (name, before, after). */
fun Character.designChangesTo(reviewed: Character) =
    designFields().zip(reviewed.designFields()).filter { it.first.second != it.second.second }.map { (before, after) ->
        Triple(before.first, before.second, after.second)
    }
