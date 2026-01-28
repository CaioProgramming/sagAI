# Saga Wrapped: Feature Documentation

## Overview

**Saga Wrapped** is a premium, end-of-saga retrospective experience inspired by *Spotify Wrapped*.
It transforms a completed story into a series of dynamic, visually stunning "slides" that celebrate
the shared journey between the player and the AI.

## 🤖 The Persona: "Ride-or-Die Partner"

Unlike a neutral summary, the AI adopts a warm, nostalgic, and humorous persona. It addresses the
player as a partner-in-crime, using their character name and reminiscing about specific chaotic
moments.

- **Language**: Punchy titles (max 3 words) and joky subtitles (max 8 words).
- **Tone**: Informal, appreciative, and deeply personal.

## 🎨 Visual Design & Aesthetics

- **Spotify Aesthetic**: High-contrast typography, large content areas, and minimalist layouts.
- **Dynamic Linework**: A signature sketchy animation that draws sweeping, randomized curves in the
  background, making the UI feel "alive."
- **Genre Integration**: Colors, fonts, and specific shape-drawing logic (vibe shapes) adapt
  automatically to the Saga's genre.
- **Micro-Animations**: Uses `PopIn` scaling, `Typewriter` text reveal, and `ReactiveShimmer` for a
  high-end feel.

## 📱 The "Wrapped" Experience

The journey is divided into several "stages":

1. **The Intro**: A cinematic hook revealing the saga title and a personal greeting.
2. **The Vibe (Expressiveness)**: Analyzes the player's emotional profile using message-based
   sentiment analysis.
3. **The Playstyle**: Identifies the player's core archetype (e.g., "The Architect", "The Chaos
   Bringer").
4. **The Squad**: A visual collage of the top characters the player spent time with.
5. **The Journey**: A grid-based "Memories Collage" of key chapters and events.
6. **The Conclusion**: A grand summary of the shared legacy and the "beautiful mess" left behind.

## 📊 Summary Dashboard

The final page acts as an interactive retrospective hub:

- **Hero Card**: A vertical portrait card summarizing top companions, dominant emotions, and total
  playtime.
- **Interactive Grid**: Dynamically generates cards for every captured review stage.
- **Robust Navigation**: Tapping any card uses an Enum-based navigation system (`ReviewPageType`) to
  jump back to that specific moment in the Wrapped experience.

## 🛠️ Technical Implementation Highlights

- **Architecture**: Decoupled `ReviewExperience` factory pattern allows for conditional page
  generation based on available saga data.
- **Navigation**: Enum-based routing ensures that even if pages are missing (e.g., no characters
  tracked), navigation links always find the correct target index.
- **AI Prompts**: Structured using `buildString` to provide the AI with full story context (
  `SagaPrompts.mainContext`) while enforcing strict word-count constraints.
- **Compose Optimization**: Heavy use of `remember` and custom `Canvas` drawing for performant
  animations.
