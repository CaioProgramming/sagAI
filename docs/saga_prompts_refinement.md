# Saga Prompts Refinement - January 9, 2026

## Overview

Refined saga creation prompts to be more humorous, friendly, engaging, and to subtly push user
imagination with better world-building suggestions.

## Changes Made

### 1. Introduction Prompt (`introPrompt`)

**Goal**: Provide richer mini-story concepts instead of simple 6-word ideas

**Before**:

- Suggestions were 6-word micro-ideas like "Time-traveling chef changes history through food"
- Simple, but not enough context to spark imagination
- InputHint was directive: "Where magic and technology collide"

**After**:

- **15-25 word mini-story pitches** that include:
    - A fascinating world/setting
    - A compelling character type/role
    - An intriguing cliffhanger or mystery
- **InputHint as inner-thought**: Feels like the user thinking out loud, using incomplete phrases
  that invite completion
    - Examples: "What if a depressive cyberpunk mercenary...", "Maybe a world where colors
      have...", "What about someone who can't..."
    - Trails off naturally (under 50 characters)
    - Subtly pushes imagination without being directive

**Example suggestions now**:

- "In a city where dreams are currency, a broke insomniac discovers they can steal nightmares—but
  someone's hunting them."
- "A chef's food brings memories to life. When they cook their grandmother's recipe, they unlock a
  family secret that could destroy everything."
- "On a planet where music controls gravity, a deaf musician finds they can hear one song—and it's
  pulling the moons toward collision."

**Why it works**:

- ✅ Immediately establishes world rules
- ✅ Introduces a character role
- ✅ Ends with a hook that makes you think "what happens next?"
- ✅ Feels like a movie pitch, not just a topic

### 2. Creative Question Prompt (`generateCreativeQuestionPrompt`)

**Goal**: Be a friendly storytelling partner, not a form-filler

**Key improvements**:

- **Tone shift**: From "imperative, action-oriented" to "warm, humorous, encouraging—like a friend
  helping brainstorm"
- **Question style**: From "direct" to "energetic, conversational" (under 120 characters)
- **InputHint style**: Inner-thought prompts using "What if...", "Maybe something about...", "What
  about..." that trail off naturally (under 50 characters)
    - Feels like brainstorming out loud, not instructing
    - Subtly guides creativity without being prescriptive
- **Suggestions refined by field**:

**TITLE**:

- Generate 3 intriguing titles hinting at mystery/adventure/conflict
- 2-5 words, cinematic feel
- Examples: "The Last Starkeeper", "Echoes of the Forgotten", "When the World Breathed"

**DESCRIPTION**:

- Generate 3 world-building hooks (10-15 words)
- Focus on unique settings, intriguing conflicts, mysterious elements
- Examples: "A library where books rewrite themselves at midnight", "The day gravity started working
  backwards in one small town"

**GENRE**:

- Suggest 3 genre combinations or twists
- Unconventional and interesting
- Examples: "Mystery meets mythology", "Sci-fi romance with time loops", "Dark fantasy comedy"

**CONTENT_READY message**:

- Changed from formal ("Your saga looks fantastic!") to casual and friendly
- Example: "Hey, this is shaping up nicely! Ready to bring your character to life, or want to add
  more flavor to the world?"

### 3. Extract Data Prompt (`extractDataFromUserInputPrompt`)

**Goal**: Make the AI feel like a helpful storytelling assistant

**Changes**:

- Role description: "friendly AI storytelling assistant" (not just "AI assistant")
- Guidelines framing: More conversational
    - "If they're being creative or adding world-building details, capture the essence"
    - "Don't force information into fields if it doesn't naturally fit"

### 4. Identify Next Field Prompt (`identifyNextFieldPrompt`)

**Goal**: More conversational internal instruction

**Changes**:

- Opening: "You're helping identify..." (more personal)
- Clearer token output format
- Better explanation of what "sufficient" means

## Technical Changes

### UseCase Update

Updated `NewSagaUseCaseImpl.replyAiForm()`:

- Changed `extractedDataPrompt` type from `SagaForm` to `SagaDraft`
- Wrap `SagaDraft` in `SagaForm` when passing to `generateCreativeQuestionPrompt`
- Ensures type consistency with updated prompts

## Impact

### User Experience:

- 🎭 **More engaging**: Prompts feel like a conversation with an excited friend
- 💡 **Better imagination triggers**: Mini-story suggestions provide complete concepts to riff on
- 🎨 **Subtler guidance**: World-building pushed naturally through examples, not forced
- 😄 **Humor**: Tone is playful without being overwhelming

### AI Behavior:

- More context-aware responses (uses existing saga data to personalize questions)
- Better suggestions that avoid generic tropes
- Clearer completion signals (CONTENT_READY callback)

## Next Steps

- [ ] Refine character creation prompts with similar approach
- [ ] Focus on character-specific aspects (personality, backstory, motivations)
- [ ] Maintain humorous, friendly tone across both saga and character flows
- [ ] Ensure character suggestions complement the saga world

## Testing Notes

- Test with various user inputs to ensure AI picks up on creative details
- Verify suggestions are diverse and avoid repetition
- Check that humor lands well without being too casual
- Ensure CONTENT_READY triggers at appropriate completion level

