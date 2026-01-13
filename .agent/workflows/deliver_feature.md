---
description: Create a Pull Request for the current feature branch with a cool, tech-focused description.
---

This workflow automates the creation of a Pull Request (PR) for the current branch, focusing on
technical achievements and implementation details.

## ⚠️ IMPORTANT PRE-FLIGHT CHECKS

- **REMEMBER**: Add `docs/` folder to `.gitignore` and push to the branch before proceeding
- **VALIDATE**: Check for uncommitted changes before proceeding. If changes exist, summarize and
  push to the branch
- **CODE QUALITY**: Remove ALL comments from code (see Step 2 for details)

---

## Step 1: Extract Hardcoded Strings (Localization Check)

- **Scan Modified Files**: Run `git diff --name-only develop..HEAD` to get list of changed files.
- **Filter Kotlin/XML Files**: Focus on `.kt` and `.xml` files that may contain hardcoded strings.
- **Detect Hardcoded Strings**: Scan for:
    - Kotlin: `Text("...")`, `Toast.makeText(...)`, `AlertDialog` content, ViewModel state strings
    - XML: `android:text="..."`, `android:hint="..."` (not using `@string/`)
- **Exclusions**: Skip Log statements, debug messages, technical identifiers, URLs
- **Report Findings**: If hardcoded strings are found:
    - List each string with file location and line number
    - Propose string resource names following `<context>_<purpose>_<content>` pattern
    - Generate English and Portuguese translations
    - Ask user if they want to extract them now or proceed with PR
- **Auto-Extract** (if user confirms):
    - Add strings to `values/strings.xml` and `values-pt-rBR/strings.xml`
    - Refactor code to use `stringResource()` or `StringResourceHelper`
    - Commit changes: `git commit -am "chore: extract hardcoded strings to resources"`

---

## Step 2: Code Clean Up (Remove Comments & Extract KDocs)

This step ensures the final code is clean, optimized, and production-ready.

### 2.1 Scan Modified Files

- Run `git diff --name-only develop..HEAD` to get list of changed `.kt` files.
- For each modified Kotlin file, analyze comments and documentation.

### 2.2 Identify Comment Types

- **Regular Comments**: `// single line` and `/* multi-line */` comments
- **KDoc Comments**: `/** ... */` documentation blocks (function/class descriptions)
- **TODO/FIXME**: Temporary markers that should be resolved or removed

### 2.3 Extract KDoc Documentation

For significant KDoc comments that serve as API documentation:

- **Create/Update README**: Generate a `README.md` in the feature's package directory if substantial
  KDocs exist
- **Document Structure**:
  ```markdown
  # [Package/Module Name]
  
  ## Overview
  [Extracted from class-level KDocs]
  
  ## Public API
  ### [FunctionName]
  [Extracted from function KDocs - parameters, return values, usage examples]
  
  ## Architecture Notes
  [Any design decisions from KDocs worth preserving]
  ```
- **Selective Extraction**: Only extract KDocs that provide valuable API documentation, not every
  comment

### 2.4 Remove All Comments from Code

- **DELETE**: All regular comments (`//`, `/* */`)
- **DELETE**: All KDoc blocks (`/** */`) after extracting valuable documentation
- **DELETE**: TODO/FIXME comments (these should be resolved, not shipped)
- **KEEP ONLY**: Code. The final source should be clean and self-documenting through good naming.

### 2.5 Code Quality Checks

- Ensure code is still readable through proper naming conventions
- Verify no orphaned or dead code remains
- Check that imports are clean and unused imports are removed

### 2.6 Commit Clean Code

- `git commit -am "refactor: clean up code - remove comments and extract documentation"`

---

## Step 3: Documentation Consolidation (Clean Up docs/)

This step organizes fragmented documentation into comprehensive, easy-to-understand files.

### 3.1 Scan docs/ Folder

- List all files in the `docs/` directory (including subdirectories).
- Identify files created/modified during this feature development.

### 3.2 Identify Related Documents

Group files by feature name, topic, or common keywords:

- Look for patterns like: `<feature>_plan.md`, `<feature>_implementation.md`, `<feature>_update.md`
- Identify: planning documents, implementation summaries, quick references, update logs, status
  files
- Common groupings: "orientation", "character_creation", "fantasy", "extraction", etc.

### 3.3 Consolidate Related Documents

For each related group, merge into a single comprehensive file:

**Structure for Consolidated Document:**

```markdown
# [Feature Name] - Implementation Documentation

## Overview

[What the feature does - extracted from planning docs]

## Implementation Details

[Technical approach and key decisions - from implementation docs]

## Code Changes

[Main file changes and logic updates - from update docs]

## Architecture Decisions

[Why certain approaches were chosen]

## Testing

[How it was verified - from test docs]

## Quick Reference

[Key points for future reference - from quick reference docs]

## Changelog

[Summary of iterations if multiple update docs existed]
```

**Naming Convention**: `<feature_name>_implementation.md` or `<feature_name>_complete.md`

### 3.4 Clean Up

- **Delete** the individual fragmented files after consolidation
- **Organize** remaining docs into appropriate subdirectories if needed
- **Update** any cross-references in other documentation

### 3.5 Commit Documentation Changes

- `git commit -am "docs: consolidate documentation for [feature]"`

### 3.6 Prepare for PR Description

- **Keep consolidated docs accessible** - these will be used to generate accurate PR descriptions
- Note key achievements and technical highlights for the PR

---

## Step 4: Context Gathering

Gather all necessary information for creating the PR description.

### 4.1 Git Information

- **Current Branch**: Run `git branch --show-current` to get the current branch name.
- **Commits**: Run `git log --oneline develop..HEAD` to see the commits made in this feature branch.
- **Diff Summary**: Run `git diff --stat develop..HEAD` to see which files were modified.

### 4.2 Documentation Context

- **Read Consolidated Docs**: Use the consolidated documentation files from Step 3 to understand:
    - Feature purpose and goals
    - Technical implementation details
    - Key architectural decisions
    - Testing approach
- **Identify Highlights**: Note the most impressive technical achievements for the PR description.

---

## Step 5: Generate PR Content

Create a compelling, tech-focused PR description using all gathered context.

### 5.1 Analysis

- Review commits, file changes, and consolidated documentation
- Identify core technical achievements (new patterns, refactors, AI integrations, etc.)
- **Suggest Label**: Based on branch name (e.g., `feature/genre/shinobi` -> `genre:shinobi`)

### 5.2 Draft Description Structure

**Title**: A concise, imperative title with emoji (e.g., "✦ feat: Implement AI Narrative Engine")

**Body Structure**:

```markdown
## Summary
[Brief paragraph explaining *what* this feature does - use consolidated docs for accuracy]

## Tech Achievements 🚀
[Highlight cool technical details with enthusiasm and emojis]
- Example: "🔄 Refactored the entire chat layer to use a unidirectional data flow"
- Example: "⚡ Integrated Gemini 1.5 Flash for sub-second response times"
- Pull key achievements from consolidated documentation

## Implementation Details 🛠️
[Bullet points of specific file changes and logic updates]
- Reference consolidated docs for accuracy
- Highlight architectural decisions

## Code Quality ✨
- Code cleaned and optimized (all comments removed)
- Documentation extracted to README files where applicable
- Unused code and imports removed

## Localization 🌐
[If strings were extracted, mention the localization improvements]
- X strings extracted to resources
- Portuguese translations added

## Testing ✅
[How was this verified? Include testing details from docs]
```

---

## Step 6: Create Pull Request

### 6.1 Construct PR Command

```bash
gh pr create \
  --base develop \
  --title "[Generated Title]" \
  --body "[Generated Description]" \
  --label "gemini-assisted" \
  --label "[feature-label]"
```

### 6.2 Labels

- **Always Add**: `gemini-assisted`
- **Feature Label**: Based on branch name analysis from Step 5

### 6.3 Final Review

- Verify title is concise and compelling
- Ensure description captures the "coolness" factor
- Confirm all technical achievements are highlighted

### 6.4 Open PR

After creation, run `gh pr view --web` to open the PR in the browser for final review.
