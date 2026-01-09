# Creation Process Refactoring - Implementation Summary

## Date: January 8, 2026

## Overview

Successfully refactored the saga creation flow from a dual-ViewModel architecture to a unified
ViewModel with dual state managers, improving separation of concerns and state management.

## Architecture Changes

### New Components Created

1. **CharacterFormFields.kt**
    - Enum defining character-specific fields (NAME, BACKSTORY, APPEARANCE, ALL_FIELDS_COMPLETE)
    - Mirrors SagaFormFields structure for consistency
    - Location: `/app/src/main/java/com/ilustris/sagai/features/newsaga/data/model/`

2. **CharacterPrompts.kt**
    - Dedicated prompts for character creation AI interactions
    - Methods: `extractCharacterDataPrompt()`, `identifyNextCharacterFieldPrompt()`,
      `generateCharacterQuestionPrompt()`, `characterIntroPrompt()`
    - Includes CONTENT_READY callback logic for completion detection
    - Location: `/app/src/main/java/com/ilustris/sagai/core/ai/prompts/`

3. **NewCharacterUseCase.kt & NewCharacterUseCaseImpl.kt**
    - Interface and implementation for character-specific AI operations
    - Methods: `generateCharacterIntroduction()`, `replyCharacterForm()`
    - Uses simplified single-step prompt flow
    - Location: `/app/src/main/java/com/ilustris/sagai/features/newsaga/data/usecase/`

4. **SagaStateManager.kt & SagaStateManagerImpl.kt**
    - Manages saga-specific state (chat, form, ready flag)
    - StateFlows: `sagaDraft`, `currentGenre`, `sagaReady`, `chatMessages`, `isGenerating`,
      `formState`
    - Methods: `updateSaga()`, `updateGenre()`, `handleCallback()`, `sendMessage()`, `startChat()`,
      `prepareSagaData()`
    - Location: `/app/src/main/java/com/ilustris/sagai/features/newsaga/data/manager/`

5. **CharacterStateManager.kt & CharacterStateManagerImpl.kt**
    - Manages character-specific state (chat, form, ready flag)
    - StateFlows: `characterInfo`, `characterReady`, `chatMessages`, `isGenerating`, `currentHint`,
      `suggestions`
    - Methods: `updateCharacter()`, `handleCallback()`, `sendMessage()`, `startCharacterCreation()`,
      `prepareCharacterData()`
    - Location: `/app/src/main/java/com/ilustris/sagai/features/newsaga/data/manager/`

6. **NewSagaViewModel.kt**
    - Unified ViewModel coordinating both managers
    - Exposes wrapped StateFlows from both managers (private manager properties)
    - Handles save orchestration with loading messages and error handling
    - Methods: `startSagaChat()`, `startCharacterCreation()`, `sendSagaMessage()`,
      `sendCharacterMessage()`, `updateGenre()`, `saveSaga()`, `retry()`, `reset()`
    - Location: `/app/src/main/java/com/ilustris/sagai/features/newsaga/ui/presentation/`

### Modified Components

1. **SagaCreationGen.kt**
    - Added `CONTENT_READY` to `CallBackAction` enum
    - Used to signal when content has sufficient data for saving

2. **SagaFormFields.kt**
    - Removed character-related fields (CHARACTER_NAME, CHARACTER_BACKSTORY, CHARACTER_OCCUPATION,
      CHARACTER_APPEARANCE)
    - Now only contains saga-specific fields: TITLE, DESCRIPTION, GENRE, ALL_FIELDS_COMPLETE

3. **NewSagaPrompts.kt**
    - Updated `generateCreativeQuestionPrompt()` to include CONTENT_READY callback logic
    - Removed character backstory special case
    - Simplified for saga-only operations

4. **NewSagaModule.kt**
    - Added DI bindings for:
        - `NewCharacterUseCase`
        - `SagaStateManager`
        - `CharacterStateManager`
    - All scoped to ViewModelComponent

5. **NewSagaView.kt**
    - Refactored to use single `NewSagaViewModel`
    - Updated FlowPages.isPageComplete() to use ready flags
    - Updated TopBarContent to accept genre and ready flags instead of form
    - Added error dialog with retry functionality
    - Updated LaunchedEffect to initialize both saga chat and character creation
    - Updated loading states to combine saga and character generation states

## Key Features Implemented

### 1. AI-Validated Completion

- Both managers use `handleCallback()` with `when(action)` to update ready flags
- `CONTENT_READY` callback sets ready flag to true
- Other callbacks (UPDATE_DATA, AWAITING_CONFIRMATION) set ready flag to false
- Ready flags persist until AI explicitly returns different callback

### 2. Centralized Save Orchestration

- `NewSagaViewModel.saveSaga()` coordinates:
    1. Create saga via `sagaManager.prepareSagaData()`
    2. Create character via `characterManager.prepareCharacterData()`
    3. Update saga with character ID
    4. Generate saga icon
    5. Track analytics
    6. Navigate to saga
- Loading messages generated via `NewSagaUseCase.generateProcessMessage()`
- Automatic reset after successful save

### 3. Error Handling

- `savingError: String?` exposed from ViewModel
- Error dialog in UI with retry and cancel options
- `retry()` function resets error and calls `saveSaga()` again

### 4. Reactive State Management

- `isReadyToSave` computed from: `sagaReady && characterReady`
- Genre changes propagate automatically via `currentGenre` StateFlow
- Page-specific states (hints, suggestions) properly isolated

### 5. LaunchedEffect Initialization

- Saga chat: `LaunchedEffect(Unit)` calls `viewModel.startSagaChat()`
- Character creation: `LaunchedEffect(pagerState.currentPage)` calls
  `viewModel.startCharacterCreation()` when page becomes visible

## Data Flow

```
User Input → ViewModel.sendSagaMessage() → SagaStateManager.sendMessage()
          ↓
   NewSagaUseCase.replyAiForm() → AI Generation
          ↓
   SagaStateManager.handleCallback() → Updates sagaReady flag
          ↓
   ViewModel.isReadyToSave (computed) → UI enables save button
          ↓
   ViewModel.saveSaga() → Orchestrates full save flow
          ↓
   Success: Navigate to saga | Error: Show retry dialog
```

## Migration Status

### ✅ Completed

- Core architecture (managers, ViewModel, use cases)
- Saga creation flow
- Character creation backend logic
- Save orchestration
- Error handling
- Loading states
- Genre management
- DI registration
- Prompt updates

### ⚠️ Needs Completion

- CharacterCreationView component refactoring (currently shows placeholder)
- Audio recording integration with new ViewModel
- Full testing of character creation UI flow

## Technical Decisions

1. **Why Two Managers Instead of One?**
    - Clear separation of concerns
    - Each manager focuses on its domain (saga vs character)
    - Easier to test and maintain
    - Allows independent evolution of each feature

2. **Why Private Manager Properties in ViewModel?**
    - Encapsulation: View only sees what it needs
    - ViewModel can aggregate/transform states before exposing
    - Prevents direct manager manipulation from UI

3. **Why Pass SagaForm as Parameter?**
    - On-demand context passing (no need to store in character manager)
    - Prevents stale state issues
    - Clear data dependency

4. **Why ViewModelComponent Scope?**
    - Managers lifecycle tied to ViewModel
    - Automatic cleanup when leaving creation flow
    - Both managers shared within same ViewModel instance

## Files to Review/Update Next

1. `/app/src/main/java/com/ilustris/sagai/features/newsaga/ui/components/CharacterCreationView.kt`
    - Remove ViewModel injection
    - Accept state parameters directly
    - Use callbacks for actions

2. `/app/src/main/java/com/ilustris/sagai/features/newsaga/ui/components/NewSagaChat.kt`
    - Review callback parameter usage
    - May need to remove if no longer needed

3. Old ViewModels (can be deleted after verification):
    -
    `/app/src/main/java/com/ilustris/sagai/features/newsaga/ui/presentation/CreateSagaViewModel.kt`
    -
    `/app/src/main/java/com/ilustris/sagai/features/newsaga/ui/presentation/CharacterCreationViewModel.kt`

## Testing Checklist

- [ ] Saga creation flow end-to-end
- [ ] Character creation flow end-to-end
- [ ] Genre switching updates UI correctly
- [ ] Ready badges show/hide correctly
- [ ] Save button enables only when both ready
- [ ] Loading overlay with process messages
- [ ] Error dialog with retry functionality
- [ ] Navigation after successful save
- [ ] Reset functionality
- [ ] Audio recording integration
- [ ] Back button during generation

## Notes

- All compilation successful (only warnings present)
- Architecture follows SagaContentManager pattern
- Ready for integration testing
- Character creation UI component needs final update to complete migration

