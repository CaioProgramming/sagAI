# Google Play Store Automation

**Feature**: Automate Google Play Console publishing via CLI
**Priority**: Medium
**Estimated Effort**: 6-8 hours
**Dependencies**: None

---

## Overview

Implement automated Google Play Console publishing using
the [Gradle Play Publisher](https://github.com/Triple-T/gradle-play-publisher) plugin. This will
enable automated releases to internal/alpha/beta/production tracks directly from the command line,
eliminating manual uploads and streamlining the release process.

---

## Goals

- ✅ Automate APK/AAB uploads to Google Play Console
- ✅ Support all release tracks (internal, alpha, beta, production)
- ✅ Automatically attach release notes from markdown files
- ✅ Integrate with existing release workflow
- ✅ Support metadata and screenshot management (future)

---

## Implementation Checklist

### Phase 1: Setup & Configuration (2-3 hours)

- [ ] **Google Cloud Setup**:
    - [ ] Create service account in Google Cloud Console
    - [ ] Grant necessary permissions (Release Manager, Service Account User)
    - [ ] Download JSON key file
    - [ ] Store securely (add to `.gitignore`, consider using environment variables)

- [ ] **Gradle Plugin Integration**:
    - [ ] Add Gradle Play Publisher plugin to `build.gradle.kts`
    - [ ] Configure plugin with service account credentials
    - [ ] Set default track (alpha)
    - [ ] Configure release status (completed vs draft)
    - [ ] Enable AAB publishing (Google's preferred format)

- [ ] **Build Configuration**:
    - [ ] Ensure `bundleRelease` task is configured
    - [ ] Verify signing configuration for release builds
    - [ ] Test local AAB generation

### Phase 2: Release Notes Integration (1-2 hours)

- [ ] **Release Notes Mapping**:
    - [ ] Create `play/` directory structure for release notes
    - [ ] Configure plugin to read from `docs/release_notes/`
    - [ ] Map markdown format to Play Store format
    - [ ] Support multi-language release notes (pt-BR, en-US)

- [ ] **Automation Script**:
    - [ ] Create helper script to convert release notes format
    - [ ] Handle version-specific release notes
    - [ ] Validate release notes before upload

### Phase 3: Workflow Integration (2-3 hours)

- [ ] **Update Release Workflow**:
    - [ ] Modify `.agent/workflows/create_release.md`
    - [ ] Add step for Play Store publishing
    - [ ] Add track selection (alpha/beta/production)
    - [ ] Add rollout percentage configuration (for production)

- [ ] **Gradle Tasks**:
    - [ ] Create custom task for alpha releases
    - [ ] Create custom task for beta releases
    - [ ] Create custom task for production releases
    - [ ] Add task for promoting between tracks

- [ ] **Testing**:
    - [ ] Test upload to internal track
    - [ ] Test upload to alpha track
    - [ ] Verify release notes appear correctly
    - [ ] Test rollback/version management

### Phase 4: Documentation & Polish (1 hour)

- [ ] **Documentation**:
    - [ ] Document service account setup process
    - [ ] Document Gradle commands
    - [ ] Add troubleshooting guide
    - [ ] Update README with new capabilities

- [ ] **Security**:
    - [ ] Ensure service account JSON is gitignored
    - [ ] Document secure credential storage
    - [ ] Add instructions for CI/CD integration (future)

---

## Technical Implementation

### Build Configuration

```kotlin
// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.github.triplet.play") version "3.9.1"
}

play {
    // Path to service account JSON
    serviceAccountCredentials.set(file("../play-store-credentials.json"))
    
    // Default track for releases
    track.set("alpha")
    
    // Release status (completed = immediately available, draft = requires manual approval)
    releaseStatus.set(ReleaseStatus.COMPLETED)
    
    // Use AAB instead of APK
    defaultToAppBundles.set(true)
    
    // Release notes directory
    releaseNotesDirectory.set(file("../docs/release_notes"))
}
```

### Gradle Tasks

```bash
# Publish to internal track
./gradlew publishReleaseBundle --track=internal

# Publish to alpha track
./gradlew publishReleaseBundle --track=alpha

# Publish to beta track
./gradlew publishReleaseBundle --track=beta

# Publish to production with 10% rollout
./gradlew publishReleaseBundle --track=production --rollout-percentage=0.1

# Promote from alpha to beta
./gradlew promoteReleaseArtifact --from-track=alpha --to-track=beta
```

### Updated Release Workflow

Add to `.agent/workflows/create_release.md` after Firebase distribution:

```markdown
13. **Publish to Google Play (Alpha Track)**:
    - Run `./gradlew bundleRelease` to create AAB
    - Run `./gradlew publishReleaseBundle --track=alpha`
    - Verify upload in Google Play Console
    - *Note*: This publishes to Alpha testers automatically
```

---

## Release Notes Format

The plugin expects release notes in the following structure:

```
play/
├── release-notes/
│   ├── en-US/
│   │   └── default.txt
│   └── pt-BR/
│       └── default.txt
```

We'll need a conversion script to transform our markdown format:

```bash
# Convert release notes from markdown to Play Store format
./scripts/convert_release_notes.sh docs/release_notes/release_1.6.1.md
```

---

## Security Considerations

1. **Service Account JSON**:
    - Add to `.gitignore`
    - Store in secure location (not in repo)
    - Consider using environment variables for CI/CD
    - Rotate credentials periodically

2. **Permissions**:
    - Grant minimum required permissions
    - Use separate service accounts for different environments
    - Audit access regularly

3. **Release Control**:
    - Use `draft` status for production releases initially
    - Implement approval workflow for production
    - Test thoroughly in alpha/beta before production

---

## Future Enhancements

1. **Metadata Management**:
    - Automate app description updates
    - Manage screenshots and promotional graphics
    - Support A/B testing for store listings

2. **CI/CD Integration**:
    - GitHub Actions workflow for automated releases
    - Automated testing before publishing
    - Slack/Discord notifications on successful publish

3. **Multi-Track Strategy**:
    - Internal → Alpha → Beta → Production pipeline
    - Automated promotion based on crash-free rate
    - Gradual rollout automation

4. **Analytics Integration**:
    - Track release performance metrics
    - Monitor crash rates per release
    - Automated rollback on critical issues

---

## References

- [Gradle Play Publisher Documentation](https://github.com/Triple-T/gradle-play-publisher)
- [Google Play Developer API](https://developers.google.com/android-publisher)
- [Service Account Setup Guide](https://github.com/Triple-T/gradle-play-publisher#service-account)

---

## Notes

- This feature requires a Google Play Developer account
- Service account setup requires Google Cloud Console access
- Initial setup is one-time, subsequent releases are fully automated
- AAB format is required for new apps on Play Store (since August 2021)
- Consider starting with internal/alpha tracks before automating production releases

---

**When ready to implement**: Create a new feature branch `feature/play-store-automation` and follow
the checklist above phase by phase.
