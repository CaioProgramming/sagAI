# Custom File Extensions & Unified Backup Plan

## Goal

Replace the current generic zip/folder backup system with branded, custom file extensions to enhance
user experience and professionalism.

- **`.saga`**: A single saga export (currently `saga_id.zip`).
- **`.sagas`**: A complete backup of all sagas (currently the `sagai_backups` folder).

## User Review Required

> [!IMPORTANT]
> **Backup Strategy Change**: We are moving from a "Sync Folder" approach (where we write individual
> files to a user-selected folder) to a "Monolithic Backup File" approach (`.sagas`).
> * **Pros**: Cleaner for the user (one file), easier to share/move.
> * **Cons**: Updating a backup requires re-writing the entire `.sagas` file (potentially large)
    rather than just updating one small JSON file.

## Proposed Architecture

### 1. File Structures

#### Single Saga Export (`.saga`)

Essentially a renamed ZIP file.

```text
MyStory.saga (ZIP Container)
├── saga.json       // The saga data
└── images/         // Folder containing referenced images
    ├── icon.jpg
    └── char_1.png
```

#### Full Backup (`.sagas`)

A container for the entire app state.

```text
Backup_2025.sagas (ZIP Container)
├── manifest.json   // Global list of sagas, last backup times, etc.
├── saga_1/         // Folder for Saga 1 content
│   ├── saga.json
│   └── images/
└── saga_2/
    ├── saga.json
    └── images/
```

*Alternatively, we could nest `.saga` files inside, but uncompressed folders inside the main zip is
usually more efficient.*

### 2. Android Integration (Intent Filters)

We will register `Sagas` as the handler for these files in `AndroidManifest.xml`.

```xml
<!-- Handle .saga files -->
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="content" />
    <data android:scheme="file" />
    <data android:mimeType="*/*" />
    <data android:host="*" />
    <data android:pathPattern=".*\\.saga" />
</intent-filter>

<!-- Handle .sagas files -->
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    ...
    <data android:pathPattern=".*\\.sagas" />
</intent-filter>
```

### 3. BackupService Refactor

#### Export (`.saga`)

- **Current**: Creates `saga_{id}.zip`.
- **New**: Creates `Title.saga`.
- **Logic**: Same ZIP compression, just different extension.

#### Full Backup (`.sagas`)

- **Current**: `getBackupRoot()` returns a `DocumentFile` (folder). We write separate files into it.
- **New**: `createFullBackup()` will generate a single `.sagas` file.
- **Trigger**: User clicks "Create Backup" -> Selects destination -> App writes one big file.
- **Restore**: User clicks "Restore Backup" -> Selects `.sagas` file -> App wipes/merges local data
  with content from `.sagas`.

### 4. UI/UX & Intent Handling

- **Entry Point**: `MainActivity` receives the `ACTION_VIEW` intent.
- **Flow**:
    1. `MainActivity` checks `intent.data` (the URI).
    2. Passes the URI to `HomeViewModel` (e.g., `viewModel.handleImport(uri)`).
    3. `HomeViewModel` sets a state `showImportDialog = true`.
    4. `HomeView` observes this state and shows `BackupSheet` with a new `ImportConfirmation` state.
    5. User confirms -> `BackupService` processes the file.

### 6. UI Components

- **`BackupSheet.kt`**:
    - Add a new state/composable for "Import Saga" confirmation.
    - Show metadata from the file (Title, Size, Date) before importing.
    - "Import" vs "Cancel" buttons.

### 5. Image Storage Strategy

**Decision: Keep using Binary (Copy Files)**

- **Why not Base64?**
    - **Size**: Base64 increases file size by ~33%.
    - **Performance**: Decoding large Base64 strings is slower and memory-intensive.
    - **Reliability**: We already have a working Zip/Unzip binary flow. It's standard and robust.

## Verification Plan

### Manual Verification

- [x] **Export**: Export a saga, verify the file is created as `.saga`.
- [x] **Import**: Tap the `.saga` file in a file manager, verify Sagas opens and imports it.
- [x] **Full Backup**: Create a `.sagas` backup, uninstall app, reinstall, restore from `.sagas`.
