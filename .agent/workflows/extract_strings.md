---
description: Extract hardcoded strings and generate localized resources
---

# Extract Strings Workflow

This workflow identifies hardcoded strings in Kotlin and XML files, generates string resources in
English and Portuguese, and refactors the code to use these resources.

## Usage

```
/extract_strings [path]
```

- `[path]` (optional): Specific file or directory to scan. Defaults to `app/src/main/`

## Steps

### 1. Scan for Hardcoded Strings

Analyze the target files to identify user-facing hardcoded strings:

**Kotlin Files (`.kt`):**

- Look for strings in Compose UI components: `Text()`, `Button()`, `TextField()`, etc.
- Check Toast messages, Dialog content, Snackbar messages
- Identify ViewModel state properties with UI text

**XML Files (`.xml`):**

- Find `android:text`, `android:hint`, `android:contentDescription`, `android:title`
- Skip strings already using `@string/` references
- Ignore `tools:text` and `translatable="false"` attributes

**Exclusions:**

- Debug/Log statements
- Internal identifiers (variable names, keys)
- File paths and URLs
- JSON keys and technical strings
- Comments
- Strings marked with `// no-extract`

### 2. Generate String Resource Names

Create descriptive resource names following the pattern:

```
<context>_<purpose>_<content_snippet>
```

Examples:

- `backup_sheet_button_export_saga` for "Export Saga" button
- `chat_view_hint_type_message` for "Type your message..." hint
- `character_details_title` for "Character Details" title

### 3. Translate to Portuguese

Use AI to generate Portuguese translations for each identified string. Mark translations with
`<!-- TODO: Review translation -->` comment for manual review.

### 4. Update String Resource Files

Add new string resources to:

- `app/src/main/res/values/strings.xml` (English)
- `app/src/main/res/values-pt-rBR/strings.xml` (Portuguese)

Maintain alphabetical ordering and add grouping comments.

### 5. Refactor Source Files

Replace hardcoded strings with resource references:

**Kotlin (Compose):**

```kotlin
// Before
Text("Hello World")

// After
Text(stringResource(R.string.greeting_hello_world))
```

**Kotlin (Non-Compose):**

```kotlin
// Before
val message = "Success!"

// After
val message = context.getString(R.string.message_success)
```

**XML:**

```xml
<!-- Before -->
<TextView android:text="Welcome" />

<!-- After -->
<TextView android:text="@string/welcome_title" />
```

Add necessary imports (`androidx.compose.ui.res.stringResource`).

### 6. Validate Changes

// turbo
Run compilation check:

```bash
./gradlew assembleDebug
```

Verify:

- No compilation errors
- All R.string references are valid
- Imports are correctly added

### 7. Generate Summary Report

Provide a summary including:

- Number of strings extracted
- Files modified
- String resources added
- Any warnings or issues
- List of strings requiring translation review

## Notes

- Start with a small scope for initial testing
- Review generated string names for clarity
- Manually verify Portuguese translations are contextually appropriate
- Handle special cases: plurals, string arrays, formatted strings
- Consider dynamic content vs. hardcoded strings

## Integration

This workflow can be integrated into:

- **Pre-commit hook**: Detect new hardcoded strings before commit
- **`/deliver_feature`**: Ensure features are properly localized before PR
- **Standalone**: Clean up existing code on-demand
