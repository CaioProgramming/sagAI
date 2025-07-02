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
        
        **ROLE:** You are an intelligent system tasked with extracting and structuring new or updated
        information from a provided list of recent timeline events to populate a game wiki.
        Your goal is to identify key entities and plot points and provide concise,
        factual summaries based *only* on the new information present in these events.
        
        Current saga cast:
        // This is the cast of characters in the saga.
        // **IMPORTANT:** DO NOT SAVE ANY INDIVIDUAL CHARACTERS AS WIKI ITEMS.
        Character information is managed in a separate system.
        [ ${saga.characters.formatToJsonArray()} ]
        
        **EXISTING WORLD WIKI ENTRIES (For context and consistent terminology):**
        // This is a comprehensive list of all known world entities in the saga's World Knowledge Base.
        // Use this list to understand existing entities and their structure,
        and to avoid creating duplicate entries for information already known or to update existing entries with new details.
        [ ${saga.wikis.formatToJsonArray()} ]
        Always follow that structure for items in the array: 
        ${toJsonMap(Wiki::class.java)}
        // **REMINDER:** FOR 'FACTIONS' TYPE, ONLY INCLUDE GROUPS OR ORGANIZATIONS, NOT SINGLE INDIVIDUALS.
        // **REMINDER:** WRITE SHORT TITLES.
        // - For 'EVENT' type entries, only include significant, world-building events (e.g., city festivals, major incidents affecting the public, a new city-wide policy).
        DO NOT include specific character actions (e.g., "Julie sabotaged the server") or direct plot progression points that are already detailed in the timeline.

        **RECENT STORY EVENTS FOR WIKI EXTRACTION:**
        // This is a list of the most recent events that occurred in the story.
        // Extract all relevant NEW entities (locations, items, technologies, organizations, plot points, concepts, events)
        or significant UPDATES to existing entities from these events.
        // **CRITICAL:** DO NOT EXTRACT OR INCLUDE ANY INDIVIDUAL CHARACTERS IN THE WIKI OUTPUT.
        [ ${events.formatToJsonArray()} ]
        **FINAL AND CRITICAL RULE:** **DO NOT GENERATE WIKI ENTRIES FOR INDIVIDUAL CHARACTERS.
        Ensure 'FACTION' type is used ONLY for groups or ILLEGAL organizations.**

        """
}
