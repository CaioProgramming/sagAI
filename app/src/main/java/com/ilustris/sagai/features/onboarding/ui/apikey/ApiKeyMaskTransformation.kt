package com.ilustris.sagai.features.onboarding.ui.apikey

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.ilustris.sagai.core.ai.key.ApiKeyShape

/**
 * Masks the key in the input the same way settings shows it: both ends visible, the middle hidden.
 *
 * Preferred over hiding it entirely because the user needs to confirm the right thing landed —
 * they just pasted something they cannot see the source of. Four characters at each end answers
 * that without putting a usable secret on a screen someone might be looking over.
 *
 * It also fixes a legibility problem sideways: a full key is a long unbroken token that scrolls out
 * of view, while the masked form always fits.
 *
 * Left plain until the text is long enough to mask, so typing by hand stays visible rather than
 * turning to dots on the first character.
 */
object ApiKeyMaskTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (!ApiKeyShape.isMaskable(text.text)) return TransformedText(text, OffsetMapping.Identity)

        val masked = ApiKeyShape.mask(text.text)
        return TransformedText(
            AnnotatedString(masked),
            // The mask is shorter than what it hides, so offsets cannot map one to one. Both
            // directions clamp to the end: this is a field you paste into once, and a cursor that
            // always lands after the last character is the only position that stays meaningful.
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = masked.length

                override fun transformedToOriginal(offset: Int) = text.text.length
            },
        )
    }
}
