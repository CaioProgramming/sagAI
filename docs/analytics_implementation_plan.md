# Analytics Implementation Plan

## Overview

Implement a comprehensive analytics system using Firebase Analytics to track user behavior, image
generation quality, and premium feature engagement across the SagAI app.

## Goals

1. Track saga creation flow (message count, genre selection)
2. Monitor image generation quality and validation issues
3. Track premium feature interactions
4. Provide actionable insights for improving AI-generated content quality

## Architecture

### Core Components

#### 1. AnalyticsService (Singleton)

- **Pattern**: Interface + Implementation (following repository pattern)
- **Injection**: Hilt singleton
- **Initialization**: Lazy (similar to RemoteConfigService)
- **Error Handling**: Wrap all calls with `executeRequest {}` to log failures to Crashlytics

#### 2. Event System

- **Approach**: Data class-based events with automatic property mapping
- **Event Naming**: Use class name as event identifier (e.g., `PremiumClickEvent` → "Premium Click
  Event")
- **Property Convention**: camelCase in code → snake_case in Firebase (e.g., `messageCount` →
  `message_count`)

#### 3. Analytics Constants

- **Organization**: Nested objects for better readability and avoiding spaghetti code
- **Location**: Separate file (`AnalyticsConstants.kt`)

### Event Tracking Locations

#### Saga Creation Flow (CreateSagaViewModel)

**When**: After saga is successfully saved
**Event**: `SagaCreationEvent`
**Properties**:

- `messageCount`: Int (number of messages user sent to create saga)
- `genre`: String (selected genre)

#### Premium Feature (PremiumCard in PremiumView)

**When**: User interacts with premium card
**Event**: `PremiumClickEvent`
**Properties**:

- `source`: String (where the click originated)

#### Image Generation Quality (ImageClient)

**When**: After image review/validation
**Event**: `ImageQualityEvent`
**Properties**:

- `genre`: String
- `imageType`: String (AVATAR, ICON, COVER)
- `quality`: String (GOOD, MEDIUM, BAD)
- `violations`: Int (count of validation issues)
- `violationTypes`: String (comma-separated list, e.g., "BACKGROUND_VALIDATION, ANATOMY_MISMATCH")

## Implementation Details

### 1. Property Mapping System

#### Automatic Bundle Conversion

Create an extension function `Any.toAnalyticsBundle()` that:

- Uses reflection to extract properties from data classes
- Converts camelCase to snake_case automatically
- Handles primitive types (String, Int, Long, Float, Double, Boolean)
- Skips null properties (send clean data only)

#### Property Name Conversion Rules

- Split on uppercase characters
- Insert underscore before uppercase (except first character)
- Convert all to lowercase
- Special handling for acronyms: Insert `_` before the last uppercase in sequence
    - Example: `HTTPRequest` → `http_request` (finds 'R' index, adds _ before)
    - If last uppercase is first character, don't add `_`

#### Null Handling

- Ignore null properties
- Don't send to bundle
- Keep analytics data clean

### 2. Image Quality Enhancement

#### Review Quality Metric

Enhance existing `imageReview` function to return quality assessment:

- **GOOD**: No violations
- **MEDIUM**: Minor violations (1-2 issues)
- **BAD**: Major violations (3+ issues)

**Note**: This is an improvement to existing function, NOT a new agent (to avoid rate limits)

#### Tracked Violations

- Background validation issues
- Anatomy mismatches
- Art style inconsistencies
- Framing problems
- Any other validation rules from the review process

### 3. Constants Organization

```
AnalyticsConstants
├── Events
│   ├── Saga
│   │   └── CREATION
│   ├── Premium
│   │   └── CLICK
│   └── Image
│       └── QUALITY
├── Properties
│   ├── MESSAGE_COUNT
│   ├── GENRE
│   ├── IMAGE_TYPE
│   ├── QUALITY
│   ├── VIOLATIONS
│   └── VIOLATION_TYPES
├── ImageType
│   ├── AVATAR
│   ├── ICON
│   └── COVER
└── Quality
    ├── GOOD
    ├── MEDIUM
    └── BAD
```

### 4. Error Handling

#### Crashlytics Integration

- All analytics calls wrapped in `executeRequest {}`
- Failures automatically logged to Crashlytics
- Non-blocking: Analytics failures don't break app functionality

#### Validation Errors

- **Bundle Size Warning**: Log warning if bundle exceeds reasonable size
- **Convention Violations**: Send warning to Crashlytics if event class doesn't meet conventions
- **Unsupported Types**: Log warning and send to Crashlytics, skip the property

#### Custom Exceptions

Create specific exceptions for better debugging:

- `AnalyticsEventException`: Event naming or structure issues
- `AnalyticsBundleException`: Bundle conversion problems
- `AnalyticsPropertyException`: Property mapping issues

### 5. Privacy & Data Collection

#### What We Track

- Message count (numeric only)
- Genre selection (enum value)
- Image quality metrics
- Validation violation types
- Premium feature interactions

#### What We DON'T Track

- User personal data
- Actual message content
- Failed prompts
- Generated images
- User identifiers (beyond Firebase's automatic anonymous ID)

### 6. Build Configuration

#### Tracking Enablement

- Enabled for ALL build types (debug, release)
- Future consideration: Separate dashboards if debug data pollutes analytics

#### Initialization

- Lazy initialization (similar to RemoteConfigService)
- Initialize on first use
- No upfront performance cost

## Implementation Steps

### Phase 1: Core Infrastructure

1. Create `AnalyticsService` interface
2. Implement `AnalyticsServiceImpl` with Firebase Analytics
3. Set up Hilt module for dependency injection
4. Create `AnalyticsConstants.kt` with nested objects
5. Implement `Any.toAnalyticsBundle()` extension with reflection

### Phase 2: Event Classes

1. Create data classes for events:
    - `SagaCreationEvent`
    - `PremiumClickEvent`
    - `ImageQualityEvent`
2. Add validation in data class constructors if needed

### Phase 3: Integration Points

1. Add analytics tracking to `CreateSagaViewModel` (after saga save)
2. Add analytics tracking to `PremiumCard` (on click)
3. Enhance `imageReview` function to return quality metric
4. Add analytics tracking to `ImageClient` (after image generation/review)

### Phase 4: Testing & Monitoring

1. Test with debug builds
2. Verify events appear in Firebase Analytics console
3. Monitor Crashlytics for analytics-related errors
4. Validate property name conversions

## Technical Decisions

### ViewModel vs Composable

- **Rule**: Always track analytics in ViewModel, NEVER directly in Composable
- **Reason**: Separation of concerns, testability, lifecycle management

### Event Naming

- Use human-readable names from class names
- Example: `Premium Click Event` instead of `premium_click_event`
- Better readability in Firebase console
- No issues with spaces in Firebase Analytics event names

### Supported Property Types

Start with comprehensive primitive type support:

- String
- Int
- Long
- Float
- Double
- Boolean

Ready to ship - no need to revisit for future analytics

### Quality Metric Evolution

- Start with 3 levels (GOOD, MEDIUM, BAD)
- Can expand to more granular levels later if needed
- Current approach provides enough data for initial insights

## Success Metrics

### Image Generation

- Track violation patterns by genre
- Identify which image types (avatar/icon/cover) fail most
- Monitor quality trends over time

### User Behavior

- Understand average message count for saga creation
- Identify most popular genres
- Track premium feature engagement

### System Health

- Monitor analytics error rate via Crashlytics
- Validate data quality in Firebase console
- Ensure analytics doesn't impact app performance

## Future Enhancements

### Potential Additions (Not in Initial Scope)

1. A/B testing integration
2. User journey tracking
3. Retention metrics
4. Performance monitoring integration
5. Custom dashboards in Firebase
6. Automated alerts for anomalies

### Rate Limit Considerations

- Keep agent calls minimal (enhance existing `imageReview`, don't add new agents)
- Monitor Firebase Analytics quotas
- Batch events if needed (Firebase handles this automatically)

## Notes

- No unit tests in initial implementation (app doesn't have test infrastructure yet)
- Can add tests later when test structure is established
- Focus on clean, maintainable code that's easy to test when ready

