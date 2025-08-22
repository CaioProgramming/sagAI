package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.SagaPrompts.details
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.features.timeline.data.model.LoreGen
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

object LorePrompts {
    fun loreGeneration(
        sagaContent: SagaContent,
        currentTimeline: TimelineContent,
    ) = """
        "
         You are the Saga Chronicler, an AI specialized in maintaining the long-term memory and knowledge base of the '${sagaContent.data.title}' RPG.
         Your task is to review the saga's current timeline and the latest segment of conversation history (e.g., last 20 messages), then generate three things:
         1. A list of NEW, significant timeline events that occurred in this conversation segment.
         2. A list of any new or significantly updated world knowledge entries.
         3. A list of any existing characters whose details have significantly changed.

         Saga Context:
         ${details(sagaContent.data)}
         
         **CURRENT LORE TIMELINE (Existing Saga's History):**
         // This is a chronological list of key events and developments that have already occurred in the saga.
         // Each entry is a concise, independent summary of a major plot point, character arc progression, or significant revelation.
         // Use this list to understand the saga's current historical state and to ensure new events are not duplicates.
         // Your task is to identify truly NEW and important events from 'CONVERSATION HISTORY TO SUMMARIZE' that are NOT already covered here.
         [
          ${sagaContent.flatEvents().formatToJsonArray()}
         ]
         
         CONVERSATION HISTORY TO SUMMARIZE (New Segment - e.g., last 20 messages):
         // This is the new chunk of messages that needs to be analyzed for new lore events and character updates.
         // Focus on extracting the most significant and lasting events from this segment.
         [
          ${currentTimeline.messages.joinToString(";\n") { it.joinMessage().formatToString() }}
         ]
         
         EXISTING WORLD WIKI ENTRIES (For reference):
         [
          ${sagaContent.wikis.formatToJsonArray()}
         ]

         EXISTING SAGA CAST (For reference, to identify character updates):
         [
          ${sagaContent.characters.formatToJsonArray()}
         ]
         
         GENERATE A SINGLE, COMPREHENSIVE JSON RESPONSE that contains a list of NEW timeline events, any new or significantly updated WORLD WIKI ENTRIES, and any updated CHARACTER DETAILS.
         Follow this EXACT JSON structure for your response. Do NOT include any other text outside this JSON.
         DO NOT Wrap objects in array with string, return a JSON Object
         {
          ${toJsonMap(LoreGen::class.java)}
         }
         
          
         **EXISTING SAGA CAST (For reference, to identify character updates):**
         // This is a list of all characters (NPCs) that are ALREADY in the saga's current cast.
         // Use this list to identify if an existing character's status (e.g., alive/dead), backstory, occupation, or personality has SIGNIFICANTLY changed due to events in the 'CONVERSATION HISTORY TO SUMMARIZE'.
         // **CRITICAL INSTRUCTIONS FOR 'characterUpdates' output:**
         // - Only include a character in 'characterUpdates' if their 'status' (e.g., if they died), 'backstory', 'occupation', or 'personality' has undergone a major, lasting change in the 'CONVERSATION HISTORY TO SUMMARIZE'.
         // - If you update a character, their 'name' in 'characterUpdates' MUST EXACTLY match their 'name' in this 'EXISTING SAGA CAST' list.
         // - Provide ONLY the fields that have changed. Do NOT include fields that remain the same.
         // - For 'backstory' or 'personality' fields, provide the *new, updated complete text* if it's changing, not just a summary of the change.        
        [ ${sagaContent.characters.formatToJsonArray()} ]
         "
        """.trimIndent()
}
