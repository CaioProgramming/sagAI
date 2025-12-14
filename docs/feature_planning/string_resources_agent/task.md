# Automated String Resources Agent - Task Plan

## Overview

This agent automates the extraction of hardcoded strings from Kotlin and XML files, generates
corresponding string resources in both English and Portuguese, and refactors the code to use these
resources.

## Objectives

1. **Identify** hardcoded user-facing strings in Kotlin (`.kt`) and XML (`.xml`) files
2. **Generate** string resources in `values/strings.xml` (English) and `values-pt-rBR/strings.xml` (
   Portuguese)
3. **Refactor** source files to replace hardcoded strings with resource references
4. **Validate** that the refactored code compiles and runs correctly

## Implementation Strategy

### Phase 1: String Detection Engine

#### 1.1 File Scanning

- Scan all files in `app/src/main/` directory
- Target file types: `.kt` (Kotlin) and `.xml` (XML layouts)
- Exclude: `strings.xml`, `build/`, `test/`, `.gradle/`

#### 1.2 Kotlin String Detection

**Target Contexts:**

- Compose UI components: `Text()`, `Button()`, `TextField()`, `OutlinedTextField()`
- Toast messages: `Toast.makeText()`
- Dialog content: `AlertDialog`, custom dialogs
- Snackbar messages
- Navigation arguments with user-facing text
- ViewModel state properties that hold UI text

**Exclusion Patterns:**

- Debug/Log statements: `Log.d()`, `Log.e()`, `println()`
- Internal identifiers: variable names, function names, class names
- File paths and URLs (containing `/`, `://`, `.com`, etc.)
- JSON keys and technical identifiers
- Regex patterns
- Comments (single-line `//` and multi-line `/* */`)
- Strings marked with `// no-extract` comment

**Detection Heuristics:**

```kotlin
// EXTRACT - User-facing UI text
Text("Hello World")
Button(onClick = {}) { Text("Click Me") }
Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()

// SKIP - Internal/technical strings
Log.d("TAG", "Debug message")
val key = "user_preference_key"
val url = "https://example.com"
```

#### 1.3 XML String Detection

**Target Contexts:**

- `android:text="..."`
- `android:hint="..."`
- `android:contentDescription="..."`
- `android:title="..."`
- `android:summary="..."`

**Exclusion Patterns:**

- Strings already using `@string/` references
- Attributes with `tools:text` (preview only)
- Strings with `translatable="false"` attribute
- Technical values: IDs, colors, dimensions

### Phase 2: Resource Generation

#### 2.1 String Resource Naming Convention

Generate unique, descriptive resource names based on:

- **Context**: File/component name
- **Content**: Abbreviated string content
- **Purpose**: Action/label/message/title

**Naming Pattern:**

```
<context>_<purpose>_<content_snippet>
```

**Examples:**

```xml
<!-- From BackupSheet.kt: "Export Saga" -->
<string name="backup_sheet_button_export_saga">Export Saga</string>

<!-- From ChatView.kt: "Type your message..." -->
<string name="chat_view_hint_type_message">Type your message...</string>

<!-- From CharacterDetailsView.kt: "Character Details" -->
<string name="character_details_title">Character Details</string>
```

#### 2.2 Resource File Updates

**English (`values/strings.xml`):**

- Add new string resources with proper formatting
- Maintain alphabetical ordering within sections
- Add comments to group related strings

**Portuguese (`values-pt-rBR/strings.xml`):**

- Initial approach: Use AI translation (Gemini API)
- Mark with `<!-- TODO: Review translation -->` comment
- Maintain same structure as English file

**Handling Special Characters:**

- Escape apostrophes: `'` → `\'`
- Escape quotes: `"` → `\"`
- Preserve newlines: `\n`
- Handle placeholders: `%s`, `%d`, `%1$s`

### Phase 3: Code Refactoring

#### 3.1 Kotlin Refactoring

**Compose Context:**

```kotlin
// Before
Text("Hello World")

// After
Text(stringResource(R.string.greeting_hello_world))
```

**Import Addition:**

```kotlin
import androidx.compose.ui.res.stringResource
```

**ViewModel/Non-Compose Context:**

```kotlin
// Before
val message = "Success!"

// After
val message = context.getString(R.string.message_success)
```

#### 3.2 XML Refactoring

```xml
<!-- Before -->
<TextView
    android:text="Welcome"
    android:hint="Enter name" />

<!-- After -->
<TextView
    android:text="@string/welcome_title"
    android:hint="@string/input_hint_enter_name" />
```

### Phase 4: Validation & Testing

#### 4.1 Compilation Check

- Run `./gradlew assembleDebug` to verify no compilation errors
- Check for missing imports
- Verify R.string references are valid

#### 4.2 Runtime Validation

- Ensure all strings display correctly
- Verify Portuguese translations load properly
- Test string formatting with placeholders

#### 4.3 Manual Review Points

- Review generated string names for clarity
- Verify Portuguese translations are contextually appropriate
- Check for any missed hardcoded strings

## Agent Workflow

### Input

- Target directory or file path (default: `app/src/main/`)
- Optional: Specific file patterns to include/exclude

### Process

1. **Scan** files and identify hardcoded strings
2. **Report** findings to user with count and examples
3. **Generate** string resource names and translations
4. **Preview** changes (show before/after for sample files)
5. **Apply** refactoring after user confirmation
6. **Validate** compilation and report results

### Output

- Summary report:
    - Number of strings extracted
    - Files modified
    - String resources added
    - Any warnings or issues
- Updated files:
    - Modified `.kt` and `.xml` files
    - Updated `strings.xml` files

## Integration Points

### Standalone Usage

```
/extract_strings [path]
```

- Run on-demand for specific files or directories
- Useful for cleaning up existing code

### Pre-Commit Hook (Future)

- Automatically detect new hardcoded strings before commit
- Warn developer and suggest extraction
- Optional: Block commit if hardcoded strings found

### Deliver Feature Integration (Future)

- Run as part of `/deliver_feature` workflow
- Ensure all new features are properly localized
- Include string extraction summary in PR description

## Edge Cases & Considerations

### Plurals

```xml
<!-- Detect and suggest plural resources -->
<plurals name="saga_count">
    <item quantity="one">%d saga</item>
    <item quantity="other">%d sagas</item>
</plurals>
```

### String Arrays

```xml
<!-- For lists of related strings -->
<string-array name="genre_names">
    <item>Fantasy</item>
    <item>Sci-Fi</item>
    <item>Cowboys</item>
</string-array>
```

### Formatted Strings

```kotlin
// Before
Text("Welcome, $userName!")

// After
Text(stringResource(R.string.welcome_user, userName))

// strings.xml
<string name="welcome_user">Welcome, %s!</string>
```

### Dynamic Content

```kotlin
// Skip extraction - content is dynamic
val characterName = character.name // Don't extract
Text(characterName) // This is data, not a hardcoded string
```

## Success Metrics

- **Coverage**: 90%+ of user-facing strings extracted
- **Accuracy**: <5% false positives (non-UI strings extracted)
- **Quality**: String names are descriptive and follow conventions
- **Compilation**: 100% success rate after refactoring
- **Translation**: Portuguese translations are contextually appropriate

## Next Steps

1. Implement string detection logic for Kotlin files
2. Implement string detection logic for XML files
3. Create resource name generation algorithm
4. Integrate AI translation for Portuguese
5. Implement code refactoring engine
6. Build validation and testing suite
7. Create agent workflow and user interface
8. Test on existing codebase (e.g., BackupSheet.kt)
9. Document usage and best practices
10. Consider integration with existing workflows

## Notes

- Start with a small test case (e.g., single file) to validate approach
- Prioritize precision over recall initially (better to miss some strings than extract wrong ones)
- Build incrementally: detection → generation → refactoring → validation
- Keep user in the loop for review and confirmation at each major step
