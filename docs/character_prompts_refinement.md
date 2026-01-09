# Character Prompts Refinement - January 9, 2026

## Overview

Refined character creation prompts to push users to imagine a complete, living protagonist with
personality, appearance, backstory, and depth—making it feel like a real character form, not just
simple field-filling.

## Philosophy

**Treat character creation like bringing a person to life**, not filling out a database. Push for:

- **Personality traits** that make them unique
- **Physical details** that reveal character
- **Backstory hooks** that create intrigue
- **Internal conflicts** that make them relatable
- **Quirks and mannerisms** that make them memorable

## Changes Made

### 1. Character Introduction Prompt (`characterIntroPrompt`)

**Goal**: Provide rich character profiles instead of simple 6-word concepts

**Before**:

- Suggestions were basic archetypes: "Reluctant hero haunted by past mistakes"
- 6 words max, no depth
- InputHint was simple: "A warrior with a secret"

**After**:

- **15-25 word CHARACTER PROFILES** that include:
    - A personality trait or defining characteristic
    - A hint of appearance or physical presence
    - A compelling backstory hook or internal conflict
    - Something that makes them feel ALIVE and relatable

- **InputHint as inner-thought**: Character-focused incomplete thoughts
    - Examples: "What if they're haunted by...", "Maybe someone who never learned to...", "What
      about a person who can see..."
    - Trails off naturally (under 50 characters)
    - Sparks character imagination, not just plot

**Example character profiles**:

- "A silver-tongued diplomat with burn scars she refuses to hide, who uses charm to mask the guilt
  of a treaty that destroyed her homeland."
- "A towering blacksmith with gentle eyes, who forges weapons by day but secretly writes poetry at
  night to cope with loneliness."
- "A scrappy street thief with a photographic memory and a limp, haunted by remembering every face
  they've ever stolen from."

**Why it works**:

- ✅ Immediately establishes personality
- ✅ Physical description reveals history/character
- ✅ Backstory hook creates intrigue
- ✅ Feels like a real person you'd want to know more about
- ✅ Fits naturally into the saga world

### 2. Character Question Prompt (`generateCharacterQuestionPrompt`)

**Goal**: Help users imagine a REAL PERSON, not just fill a character sheet

**Key improvements**:

- **Tone shift**: From "imperative, action-oriented" to "like helping a friend build their D&D
  character"
- **Question style**: Conversational, excited to learn about this person (under 120 characters)
- **InputHint style**: Inner thoughts about the character - "What if they...", "Maybe someone
  who...", "What about..."
- **Field-specific suggestions with depth**:

**NAME**:

- Generate 3 names that feel authentic to genre/world
- Consider: cultural fit, nickname potential, how it sounds when shouted or whispered
- Each name should have personality baked in

**BACKSTORY**:

- Generate 3 backstory hooks (10-15 words) combining origin, motivation, and conflict
- Focus on: formative events, shaping relationships, secrets carried, goals pursued
- Examples:
    - "Raised by assassins but refuses to kill, seeking redemption through protecting others"
    - "Former noble stripped of title, now building a criminal empire to reclaim their inheritance"

**APPEARANCE**:

- Generate 3 descriptions (10-15 words) that reveal personality and history through physical details
- Focus on: distinctive features that tell stories, scars/marks with meaning, style choices that
  reveal character
- Examples:
    - "Weathered hands and kind eyes, always in practical clothes with hidden pockets"
    - "Sharp features framed by wild hair, moves like a dancer, dresses to intimidate"

**CONTENT_READY message**:

- Changed from formal to excited: "Wow, they're really coming to life! Ready to jump into the saga,
  or want to add some final touches?"
- Makes it clear they've created someone compelling

### 3. Extract Character Data Prompt (`extractCharacterDataPrompt`)

**Goal**: Capture character essence, not just data points

**Changes**:

- Role: "friendly AI character-building assistant helping a user bring their protagonist to life"
- Guidelines emphasize capturing what makes the character REAL:
    - "Capture personality, appearance, backstory, motivations—anything that makes this person REAL"
    - "If they describe physical traits, mannerisms, or quirks, note them"
    - "Remember: we're building a living character, not filling a database"

### 4. Identify Next Field Prompt (`identifyNextCharacterFieldPrompt`)

**Goal**: Focus on character completeness and "realness"

**Changes**:

- Opening: "You're helping identify what's still needed to make this character feel complete and
  alive"
- Key question: "Would a reader/player feel like they know this character?"
- Clearer criteria for completion: "If the character has enough depth and personality to feel like a
  real person"

## Technical Alignment with Character Data Model

### Mapping to Character Fields:

The prompts push for details that map to the full `Character` data class:

**NAME** → `name`, `lastName`, `nicknames`

- Suggestions encourage thinking about full names and nickname potential

**BACKSTORY** → `backstory`, `profile.occupation`, `details.abilities`

- Hooks should naturally include what they do, their skills, their history

**APPEARANCE** → `details.physicalTraits`, `details.clothing`, `image` generation data

- Descriptions cover: race, gender, build, facial features, distinctive marks, clothing style
- Physical details that reveal character (scars, weathered hands, etc.)

**Implicit personality data** → `profile.personality`, `knowledge`, `emojified` traits

- The way we ask about backstory/appearance pushes personality revelation
- AI can extract personality traits from how users describe their character

## Impact

### User Experience:

- 🎭 **Feels like character creation, not data entry**: Questions spark imagination about a real
  person
- 💡 **Better character depth**: Suggestions push personality, appearance, and backstory together
- 🎨 **Visual imagination**: Appearance suggestions help users "see" their character
- 😄 **Engaging tone**: Like brainstorming with a friend, not filling a form
- 🌍 **World integration**: Characters naturally fit the saga context

### AI Behavior:

- More context-aware (uses saga world and existing character data)
- Suggestions feel like real people, not archetypes
- Captures character essence beyond required fields
- Better at extracting personality from user descriptions

## Examples in Action

### User says: "A gruff old sailor"

**AI might respond**:

- Question: "Love it! What drove them to the sea—and what made them so gruff?"
- Hint: "Maybe someone who lost..."
- Suggestions:
    - "Weathered by decades at sea, salt-and-pepper beard, haunted by a storm that took their crew"
    - "Rough exterior hiding a poet's soul, covered in naval tattoos, each one a story of survival"
    - "Scarred hands that know every knot, gruff because trust got them betrayed by a first mate"

### User says: "She's a thief but has a code"

**AI might respond**:

- Question: "Ooh, a thief with principles! What's the line she won't cross, and why?"
- Hint: "What if she only steals from..."
- Suggestions:
    - "Only steals from those who exploit the poor, raised in slums, sees herself as redistributing
      wealth"
    - "Refuses to steal from families, orphaned young, knows what losing everything feels like"
    - "Never uses violence, believes there's honor in the craft, trained by a master thief who
      taught restraint"

## Next Steps

- [x] Saga prompts refined (humorous, engaging, world-building focus)
- [x] Character prompts refined (personality, appearance, depth focus)
- [ ] Test full creation flow end-to-end
- [ ] Verify CONTENT_READY triggers appropriately for both saga and character
- [ ] Ensure character suggestions complement saga world naturally

## Testing Notes

- Test with various character archetypes (hero, villain, antihero, mentor)
- Verify appearance descriptions are diverse and avoid stereotypes
- Check that backstory hooks create intrigue without being too complex
- Ensure name suggestions fit genre aesthetics
- Confirm CONTENT_READY feels celebratory but allows refinement

