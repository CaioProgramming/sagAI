# Feat: Implement Punk Rock Theme & Enhance Milestone Storytelling

## Description
This PR introduces the **Punk Rock** genre theme and significantly refines the AI storytelling prompts for milestones to be more genre-specific and distinct. It also includes localization updates for the review summary and milestone overlay.

### Key Changes
- **Punk Rock Theme**: Added `Genre.PUNK_ROCK` with a vibrant green/black/red color palette, Gorillaz-inspired art style prompts (sketchbook/zine aesthetic), and rebellious persona.
- **Milestone Prompts Overhaul**:
    - **Reduced Persona**: Streamlined prompts to reduce token usage and improve focus.
    - **Genre-Specific Tone**: Implemented `getGenreConversationalTone` and `buildPersonaForGenre` to give the AI narrator a distinct voice for each genre (e.g., sarcastic hacker for Cyberpunk, mock-heroic for Heroes).
    - **Localized UI**: Extracted hardcoded strings in `MilestoneOverlay.kt` and `ReviewSummaryPage.kt` to `strings.xml` (EN/PT-BR).
- **UI Updates**:
    - Updated `MilestoneOverlay` to use localized string resources.
    - Updated `ReviewSummaryPage` to display localized review stage titles (Your Vibe, Your Style, The Cast, The Journey, The Legacy).

## Testing
- **Visual**: Verify the Punk Rock theme colors and fonts in the `Create Saga` flow (if selectable) or via genre switching.
- **Storytelling**: Trigger milestones (New Character, Chapter Finished, Act Finished) and verify the AI's congratulatory message matches the active genre's persona.
- **Localization**: Switch device language to Portuguese and verify the new strings in Review Summary and Milestone Overlay.

## Documentation
- [Punk Rock Theme Plan](docs/feature_planning/punk_rock_theme_plan.md)
- [Milestone & Storytelling Refinement](docs/milestone_and_storytelling_refinement.md)
