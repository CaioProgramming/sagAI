# Saga Progress Widget

## Concept

A home screen widget that keeps the user connected to their current saga by displaying a dynamic,
AI-generated summary of recent events. This serves as a "pulse" of the story, reminding the user of
where they left off and enticing them to continue.

## Widget UI Design

The widget will use `Jetpack Glance` for a modern, Compose-like implementation.

* **Layout**: A simple, elegant card.
* **Icon**: The Saga's icon, but **tinted black** (or monochrome deep dark color) to fit the "
  Resume" aesthetic.
* **Title**: The Saga's title at the top.
* **Content**: A short, generated paragraph (resume/recap) summarizing the recent context.
* **Background**: Clean, likely following the system theme or a subtle gradient based on the saga's
  genre.
* **Action**: Tapping the widget opens the app and navigates directly to that Saga's chat screen.

## Technical Implementation

### 1. Data Source & AI Generation

* **GemmaClient**: We will utilize the existing `GemmaClient` to generate the text.
* **Input**: The last few messages or the last "scene summary" from the database for the active
  saga.
* **Prompt**: A specific prompt instructing Gemma to act as a narrator providing a "previously on"
  style recap, but very concise (max 2-3 sentences).
    * *Example Prompt*: "Summarize the current situation of [Character Name] in [Saga Title] based
      on these recent events: [Events]. Keep it under 40 words, engaging, and mysterious."

### 2. Background Updates (WorkManager)

* **Trigger**:
    * App exit/backgrounding (to update the widget with the latest session info).
    * Periodic updates (optional, maybe daily to "refresh" the memory if the user hasn't played).
* **Worker**: Create a `SagaWidgetWorker` that:
    1. **Identify Active Saga**:
        * **Filter**: Select only *unfinished* sagas.
        * **Sort**: Order by the `timestamp` of the *latest message* associated with the saga (
          descending).
        * **Select**: Pick the first result. This ensures that if the user switches between Story 1
          and Story 2, the widget immediately reflects the one they interacted with most recently.
    2. Fetches the chat history/context.
    3. Calls `GemmaClient` to generate the summary.
    4. Saves the summary to `SharedPrefs` or `DataStore` (Widget State).
    5. Requests a widget update via `GlanceAppWidgetManager`.

### 3. Caching & State

* We cannot call AI every time the widget renders. The summary **must** be generated and cached.
* The widget only reads the cached string.

### 4. Edge Cases

* **No Active Saga**: Display a generic "Start your journey" message or a random prompt.
* **Offline**: Display the last cached summary. If none, show the Saga description.
* **Loading**: If generating, show a subtle loading indicator or the previous state.

## Roadmap Integration

* This feature fits into the "Engagement" track, keeping users thinking about their stories even
  when outside the app.
