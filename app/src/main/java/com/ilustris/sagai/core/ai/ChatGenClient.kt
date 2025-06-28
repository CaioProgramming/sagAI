package com.ilustris.sagai.core.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

class ChatGenClient {
    val model: GenerativeModel by lazy {
        Firebase
            .ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash")
    }
}
