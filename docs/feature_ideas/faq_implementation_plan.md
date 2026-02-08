# FAQ Feature Implementation Plan

## Objective

Implement a "Help Center" (FAQ) to assist users with understanding SagAI, improving their
storytelling prompts, and troubleshooting. The system will be dynamic, multi-language, and
AI-assisted.

## Technical Approach

To ensure agility and the ability to update content without releasing new app binaries, the FAQ data
will be hosted on **Firebase Remote Config**.

### Architecture

1. **Source of Truth:** Firebase Remote Config (Keys: `faq_data_en`, `faq_data_pt`).
2. **Data Format:** JSON.
3. **Client Implementation:**
    * Fetch JSON from Remote Config based on user locale.
    * Parse into `List<FAQCategory>`.
    * Display in `FAQView` Composable.

## Data Structure

The JSON structure will be a simple array of objects.

```json
{
  "version": 1,
  "categories": [
    {
      "title": "General",
      "items": [
        {
          "question": "Question text here",
          "answer": "Answer text here"
        }
      ]
    }
  ]
}
```

## Content Strategy & "Insider Secrets"

The FAQ content must be derived from two sources:

1. **Internal Prompts (The "Secret Sauce"):**
    * **Art Styles:** Explain strictly enforced styles (Fantasy = Oil Painting, Punk =
      Cartoon/Gorillaz, Shinobi = Ink Wash).
    * **Mechanics:** NPCs cannot read "Thoughts".
    * **Genre Voices:** How the AI changes personality per genre.
2. **Release Notes (`docs/release_notes/`):**
    * The "What's New" section of the FAQ should reflect the latest features (e.g., Import/Export,
      new Cowboy/Shinobi genres).

## UI Implementation Plan

### 1. Navigation

* **Route:** Add `FAQ` to `Routes.kt` (`showBottomNav = false`).
* **Entry Point:** Add a "Help Center" card in `SettingsView.kt`.
    * **Icon:** `ic_help` (or similar).
    * **Subtitle:** "Master the art of the Saga." or "Tips, Tricks & Secrets."

### 2. FAQView (`features/faq/ui/FAQView.kt`)

* **Search Bar:**
    * **Local Filter:** Real-time filtering of categories/items.
    * **AI Fallback:** If 0 results, show "Ask the Saga Master". Use Gemini to answer the query
      based on the loaded FAQ context + general app knowledge.
* **FAQCard:**
    * Expandable card (Title + Arrow).
    * On expand: Shows answer.

### 3. Localization

* Detect device language.
* Fetch `faq_data_pt` for Portuguese, default to `faq_data_en`.

## Execution Steps

1. **Branching:**
    * Create a new branch: `feature/faq-system`.
2. **Data Generation:**
    * Generate `docs/faq_data_en.json` and `docs/faq_data_pt.json` using the content strategy above.
3. **Implementation:**
    * `Routes.kt`: Add route.
    * `SettingsView.kt`: Add entry point card.
    * `FAQViewModel.kt`: Fetch logic + Search logic.
    * `FAQView.kt`: UI implementation.
4. **Backend:**
    * Upload JSONs to Firebase Remote Config.