package com.ilustris.sagai.core.ai.model

import android.util.Base64

/**
 * Audio sent inline with a text-generation request (the player's voice turn in live mode). The
 * reply model hears it directly instead of a transcript, so it also gets how it was said.
 */
class AudioAttachment(
    val data: ByteArray,
    val mimeType: String = "audio/wav",
) {
    fun toBase64(): String = Base64.encodeToString(data, Base64.NO_WRAP)
}
