# String Extraction Report - BackupViewModel.kt

## Summary

**File**: `app/src/main/java/com/ilustris/sagai/core/file/backup/ui/BackupViewModel.kt`
**Total Hardcoded Strings Found**: 8
**Language**: Portuguese (pt-BR)
**Context**: ViewModel - Status and error messages

---

## Identified Hardcoded Strings

### 1. Line 43 - Loading Message

**Current Code**:

```kotlin
_uiState.value = BackupUiState.Loading("Recuperando conteudo...")
```

**Proposed String Resource**:

```xml
<!-- English (values/strings.xml) -->
<string name="backup_loading_recovering_content">Recovering content...</string>

<!-- Portuguese (values-pt-rBR/strings.xml) -->
<string name="backup_loading_recovering_content">Recuperando conteúdo...</string>
```

**Refactored Code**:

```kotlin
_uiState.value = BackupUiState.Loading(stringHelper.getString(R.string.backup_loading_recovering_content))
```

---

### 2. Line 48 - Error Message

**Current Code**:

```kotlin
BackupUiState.Empty("Ocorreu um erro inesperado, não foi possível recuperar os conteudos de backup :(")
```

**Proposed String Resource**:

```xml
<!-- English -->
<string name="backup_error_recovery_failed">An unexpected error occurred, could not recover backup contents :(</string>

<!-- Portuguese -->
<string name="backup_error_recovery_failed">Ocorreu um erro inesperado, não foi possível recuperar os conteúdos de backup :(</string>
```

**Refactored Code**:

```kotlin
BackupUiState.Empty(stringHelper.getString(R.string.backup_error_recovery_failed))
```

---

### 3. Line 58 - Success Message

**Current Code**:

```kotlin
_uiState.emit(BackupUiState.Empty("Parece que esta tudo em ordem!"))
```

**Proposed String Resource**:

```xml
<!-- English -->
<string name="backup_message_all_good">Looks like everything is in order!</string>

<!-- Portuguese -->
<string name="backup_message_all_good">Parece que está tudo em ordem!</string>
```

**Refactored Code**:

```kotlin
_uiState.emit(BackupUiState.Empty(stringHelper.getString(R.string.backup_message_all_good)))
```

---

### 4. Line 62 - Loading Message

**Current Code**:

```kotlin
_uiState.emit(BackupUiState.Loading("Encontramos algumas coisinhas.."))
```

**Proposed String Resource**:

```xml
<!-- English -->
<string name="backup_loading_found_items">We found some things..</string>

<!-- Portuguese -->
<string name="backup_loading_found_items">Encontramos algumas coisinhas..</string>
```

**Refactored Code**:

```kotlin
_uiState.emit(BackupUiState.Loading(stringHelper.getString(R.string.backup_loading_found_items)))
```

---

### 5. Line 85 - Loading Message

**Current Code**:

```kotlin
_uiState.emit(BackupUiState.Loading("Habilitando backup..."))
```

**Proposed String Resource**:

```xml
<!-- English -->
<string name="backup_loading_enabling">Enabling backup...</string>

<!-- Portuguese -->
<string name="backup_loading_enabling">Habilitando backup...</string>
```

**Refactored Code**:

```kotlin
_uiState.emit(BackupUiState.Loading(stringHelper.getString(R.string.backup_loading_enabling)))
```

---

### 6. Line 89 - Success Message

**Current Code**:

```kotlin
_uiState.emit(BackupUiState.Loading("Tudo pronto! Aproveite suas histórias 💜"))
```

**Proposed String Resource**:

```xml
<!-- English -->
<string name="backup_success_enabled">All set! Enjoy your stories 💜</string>

<!-- Portuguese -->
<string name="backup_success_enabled">Tudo pronto! Aproveite suas histórias 💜</string>
```

**Refactored Code**:

```kotlin
_uiState.emit(BackupUiState.Loading(stringHelper.getString(R.string.backup_success_enabled)))
```

---

### 7. Line 97 - Error Message

**Current Code**:

```kotlin
BackupUiState.Empty("Não foi possivel habilitar o backup. Sentimos muito por isso vamos tentar de novo?")
```

**Proposed String Resource**:

```xml
<!-- English -->
<string name="backup_error_enable_failed">Could not enable backup. We\'re sorry about that, shall we try again?</string>

<!-- Portuguese -->
<string name="backup_error_enable_failed">Não foi possível habilitar o backup. Sentimos muito por isso, vamos tentar de novo?</string>
```

**Refactored Code**:

```kotlin
BackupUiState.Empty(stringHelper.getString(R.string.backup_error_enable_failed))
```

---

### 8. Lines 108, 117 - Formatted Message

**Current Code**:

```kotlin
BackupUiState.Loading("Restaurando ${restorableSaga.manifest.title}...")
```

**Proposed String Resource**:

```xml
<!-- English -->
<string name="backup_loading_restoring_saga">Restoring %s...</string>

<!-- Portuguese -->
<string name="backup_loading_restoring_saga">Restaurando %s...</string>
```

**Refactored Code**:

```kotlin
BackupUiState.Loading(stringHelper.getString(R.string.backup_loading_restoring_saga, restorableSaga.manifest.title))
```

---

## Implementation Plan

### Step 1: Add String Resources

Add all 8 string resources to both `values/strings.xml` and `values-pt-rBR/strings.xml`

### Step 2: Inject StringResourceHelper

Update BackupViewModel constructor:

```kotlin
@HiltViewModel
class BackupViewModel
@Inject
constructor(
    private val backupService: BackupService,
    private val sagaBackupService: SagaBackupService,
    private val sagaRepository: SagaRepository,
    private val stringHelper: StringResourceHelper, // Add this
) : ViewModel() 
```

### Step 3: Replace Hardcoded Strings

Replace all 8 occurrences with `stringHelper.getString()` calls

### Step 4: Verify Compilation

Run `./gradlew assembleDebug` to ensure no errors

---

## Notes

- All strings are user-facing status/error messages
- Strings contain Portuguese text with some emoji (💜)
- One string uses string interpolation (saga title) - requires format argument
- Fixed typo: "conteudo" → "conteúdo" (added accent)
- Fixed typo: "esta" → "está" (added accent)
- Fixed typo: "possivel" → "possível" (added accent)
