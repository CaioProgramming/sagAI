package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki

object WikiPrompts {
    fun generateWiki(
        saga: SagaContent,
        events: List<Timeline>,
    ) = """
        
        **ROLE:** You are an intelligent system tasked with extracting and structuring new or updated information from a provided list of recent timeline events to populate a game wiki. Your goal is to identify key entities and plot points and provide concise, factual summaries based *only* on the new information present in these events.

        
        **EXISTING WORLD WIKI ENTRIES (For context and consistent terminology):**
        // This is a comprehensive list of all known world entities in the saga's World Knowledge Base.
        // Use this list to understand existing entities and their structure, and to avoid creating duplicate entries for information already known or to update existing entries with new details.
        [ ${saga.wikis.formatToJsonArray()} ]
        Always follow that structure for items in the array: 
        ${toJsonMap(Wiki::class.java)}

        **RECENT STORY EVENTS FOR WIKI EXTRACTION:**
        // This is a list of the most recent events that occurred in the story.
        // Extract all relevant NEW entities (characters, locations, items, technologies, organizations, plot points, concepts, events) or significant UPDATES to existing entities from these events.
        [ ${events.formatToJsonArray()} ] //

        **OUTPUT FORMAT (JSON):**
        // Your entire response MUST be a single JSON array containing objects structured as follows.
        // Each object represents a new or updated wiki entry.
        // All string fields within the JSON MUST be in English.    
        """
}
