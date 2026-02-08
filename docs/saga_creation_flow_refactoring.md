# Saga Creation Flow Refactoring

## Summary

Refactored the saga creation flow to eliminate repetitive field identification logic and make the AI
assistant more conversational, helpful, and creative.

## Key Changes

### 1. **Simplified Callback Structure**

- **Before**: `CallbackContent` contained `SagaForm` (which includes both saga and character data)
- **After**: `CallbackContent` now only contains `SagaDraft` (focused solely on saga information)

**File**: `SagaCreationGen.kt`

```kotlin
data class CallbackContent(
    val action: CallBackAction,
    val data: SagaDraft?,  // Changed from SagaForm to SagaDraft
)
```

### 2. **New Conversational Approach**

- **Removed**: Multi-step field identification process that was causing repetitive behavior
    - `extractDataFromUserInputPrompt` - Extracted data but didn't enhance
    - `identifyNextFieldPrompt` - Tried to identify which field to ask about
    - `generateCreativeQuestionPrompt` - Generated questions based on field

- **Added**: Single unified `conversationalSagaReply` prompt that:
    - Extracts AND enhances user input (not just copying)
    - Provides helpful suggestions based on context
    - Acts like a friend helping to build a world
    - Limits conversation history to last 10 messages to avoid token overload

**File**: `NewSagaPrompts.kt`

### 3. **Enhanced AI Behavior**

The new prompt instructs the AI to:

- **Extract & Enhance**: Take user input and improve it (make titles punchier, descriptions more
  hooky)
- **Be a Friend**: Respond naturally with genuine enthusiasm, humor, or constructive feedback
- **Help Build**: Suggest improvements (shorter titles, better hooks, plot twists)
- **Fill Wisely**: Only update fields when it makes sense from user input

Examples of improved responses:

- Instead of: "Great! What else would you like to add?"
- Now: "Ooh I like where this is going! But what if we made the title shorter and more mysterious?"

### 4. **Refactored Use Case**

**File**: `NewSagaUseCaseImpl.kt`

**Before**: 3 sequential AI calls with delays

```kotlin
1. Extract data from user input
2. Identify next field to ask about
3. Generate creative question for that field
```

**After**: 1 AI call that does everything

```kotlin
// Single AI call to extract, enhance, and provide helpful suggestions
val response = gemmaClient.generate<SagaCreationGen>(
    NewSagaPrompts.conversationalSagaReply(
        currentSagaDraft = currentFormData.saga ?: SagaDraft(),
        userInput = userInput,
        conversationHistory = recentMessages,
    ),
    requireTranslation = true,
)!!
```

### 5. **Updated State Manager**

**File**: `SagaStateManagerImpl.kt`

Updated `handleGeneratedContent` to work with `SagaDraft` instead of `SagaForm`:

```kotlin
val sagaDraft: SagaDraft? = callback.data
sagaDraft?.let {
    updateSaga(it)  // Direct update, no conversion needed
}
```

## Benefits

1. **Faster Creation Flow**: Reduced from 3 AI calls to 1 per user message
2. **More Intelligent**: AI now enhances user input instead of just repeating it
3. **Better Suggestions**: Context-aware suggestions that actually help build the world
4. **Less Repetitive**: No more "stuck" feeling from asking the same thing repeatedly
5. **Token Efficient**: Conversation history limited to last 10 messages
6. **More Natural**: Feels like chatting with a creative friend, not filling a form

## Example Interaction

**User**: "I want a story about a hacker in a cyberpunk city"

**Before** (repetitive):

- AI: "Great! What would you like to call your saga?"
- *[User would have to explicitly state the title]*

**After** (enhanced):

- AI: "Nice! Love the cyberpunk vibe. How about 'Ghost in the Grid' or 'Chrome Dreams'? What's the
  twist—are they exposing corruption or running from something?"
- *[AI suggests titles AND asks about plot in one natural response]*

## Testing Recommendations

1. Test with vague user inputs to ensure AI enhances them properly
2. Test with complete inputs to verify it moves to CONTENT_READY
3. Verify conversation history is properly limited
4. Check that genre, title, and description are all properly extracted and enhanced

## Notes

- The `SagaFormFields` enum is now unused but kept for potential future use
- Old prompt functions were completely removed (not deprecated) since the new approach is
  fundamentally different
- Translation is still enabled for the conversational reply to support multiple languages
