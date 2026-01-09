# Anti-Hallucination Enhancement for ChatPrompts

## Summary

Enhanced both `replyMessagePrompt` and `sceneSummarizationPrompt` to emphasize the importance of the
latest message and prevent AI hallucinations, particularly around character deaths, departures, and
permanent story events.

## Changes Made

### 1. `replyMessagePrompt` Enhancement

#### Added "ANTI-HALLUCINATION PROTOCOL" Section

Inserted before the "CURRENT PLAYER TURN" section with critical rules:

1. **DEATH IS FINAL**: Characters who died cannot reappear unless explicitly resurrected
2. **DEPARTURE IS REAL**: Characters who left are gone unless they explicitly returned
3. **DESTRUCTION IS PERMANENT**: Destroyed objects cannot be used unless restored
4. **TRUST THE LATEST MESSAGE**: The latest message is absolute truth
5. **NO RETCONS**: Never ignore or undo recent events

#### Enhanced "LATEST MESSAGE" Section

Transformed from simple label to prominent warning block:

- Added `⚠️ THIS IS THE MOST IMPORTANT CONTEXT` header
- Clear bullet points explaining what the AI must do:
    - React DIRECTLY to the message content
    - Respect ALL consequences (deaths, departures, changes)
    - Build the next moment FROM this exact situation
    - NEVER contradict this message
- Positioned at the END of the prompt for maximum impact

### 2. `sceneSummarizationPrompt` Enhancement

#### Added "[MOST RECENT MESSAGE]" Section

Inserted after "Recent Activity" and before "Technical Extraction Parameters":

- Extracts the latest message by timestamp
- Displays it with clear warning emphasis
- Provides explicit mandate with concrete examples:
    - Character dying → NOT in `charactersPresent`
    - Movement to new location → `currentLocation` MUST change
    - Dramatic event → MUST appear in `worldStateChanges`
    - Clarifies this defines what happens NEXT, not BEFORE

#### Added "ANTI-HALLUCINATION RULES FOR SUMMARIZATION"

Five critical rules for the summarization process:

1. **DEAD CHARACTERS VANISH**: Mark deaths in `worldStateChanges`
2. **LOCATION TRANSITIONS ARE IMMEDIATE**: Update location and presence immediately
3. **NO INVENTED PRESENCE**: Only list explicitly shown characters
4. **EVENTS ARE FINAL**: Reflect all consequences in `worldStateChanges`
5. **PRIORITIZE RECENCY**: Latest message outweighs older context

## Impact

### Problem Solved

- **Character Death Hallucinations**: AI can no longer ignore character deaths and make them
  reappear
- **Location Confusion**: Clear rules about character presence after location changes
- **Event Permanence**: Destroyed objects and completed actions are now final
- **Context Priority**: Latest message is treated as absolute truth

### Prompt Structure

Both prompts now follow a clear hierarchy:

1. General context (saga, characters, relationships)
2. Historical/conversation history
3. **Latest message** (most prominent, positioned strategically)
4. Anti-hallucination rules (reinforcement)
5. Output instructions

### Technical Notes

- Latest message is extracted using `saga.flatMessages().maxByOrNull { it.message.timestamp }`
- Message is normalized using `toAINormalize(messageExclusions)` to remove technical fields
- All additions follow Kotlin line length conventions (max 140 characters)
- Existing functionality remains unchanged

## Testing Recommendations

1. **Death Scenarios**: Test that characters who die are removed from subsequent scenes
2. **Location Changes**: Verify characters don't follow protagonist unless explicitly stated
3. **Permanent Events**: Confirm destroyed items don't reappear
4. **Contradiction Handling**: Test that latest message overrides older context when conflicts occur
5. **Multi-character Scenes**: Verify correct character tracking across location transitions

## Future Enhancements

Consider adding:

- Timestamp validation to ensure chronological consistency
- Event log for tracking permanent changes across acts
- Character state tracking (alive/dead/absent) in scene summary
- Location history to track movements more explicitly

