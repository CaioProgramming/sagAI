# Genre-Specific Review Experience

## 🎯 Vision

Transform the saga review from a generic recap into a **genre-specific celebration** of the player's
journey. Each genre should have its own unique visual language, animations, and transitions. The
experience culminates in a **"Saga Wrapped"** style dashboard—a swipeable set of shareable cards
that summarize the player's unique story data.

## 🎨 Core Concept

Instead of one-size-fits-all review pages, each genre gets:

- **Custom page layouts** tailored to genre aesthetics
- **Unique animations** that match the genre's mood and energy
- **Genre-appropriate data visualization**
- **Saga Wrapped Dashboard** replacing the standard conclusion

## 📊 Current State Analysis

### Existing Review Structure

The current `SagaReview.kt` has a fixed flow ending in a text-based "Conclusion" scroll.

### Limitations to Address

- ❌ Generic text scrolling is less engaging than visual data.
- ❌ "Conclusion" page feels static compared to modern recap trends (Spotify Wrapped, Apple Replay).
- ❌ Missed opportunity for social sharing of specific stats (Top Characters, Message Counts).

## 🃏 The "Saga Wrapped" Dashboard (New Feature)

Instead of a scrollable text page, the final review stage will be a **Card Dashboard**. This
consists of swipeable, full-screen cards presenting key stats, heavily inspired by Spotify Wrapped's
modular design.

### Card 1: The Cast (Your Co-Stars)

*Analogous to "Top Artists"*

- **Visual**: Large Saga Icon background / Genre-themed texture.
- **Content**: A ranked list of the **Most Present Characters**.
- **Data Source**: Frequency of relationship updates and interactions.
- **Headline**: "Starring in your Story..." or "The Usual Suspects" (Genre dependent).

### Card 2: The Vibe (Emotional Atmosphere)

*Analogous to "Top Songs"*

- **Visual**: **Line Drawing Animation** (`SparkLoader` style). The emotional shape is drawn on
  screen with a thin, elegant stroke that eventually fills or glows.
- **Content**: The top 3-5 emotional tones from the saga (e.g., "Tense", "Joyful", "Melancholic").
- **Headline**: "The Vibe Check" or "Atmospheric Readings".

### Card 3: Your Voice (Story Duration)

*Analogous to "Minutes Listened"*

- **Visual**: Main Character's avatar in a dynamic pose.
- **Content**: **Total Messages Sent** by the main character.
- **Headline**: "You wrote X lines of history..."
- **Twist**: "That's more than [Comparative Fact]!"

### Card 4: Genre Affinity (The Identity)

*Analogous to "Your Music Personality"*

- **Visual**: Iconic, badge-style art for the current Genre.
- **Content**: Highlights this saga's genre and potentially compares it to user's overall
  preference.
- **Status**: *Optional / Explore further.*

---

## 🎭 Genre-by-Genre Vision

### 1️⃣ FANTASY

**Theme**: Epic Storybook / Illuminated Manuscript
**Wrapped Style**: Cards look like pages from an ancient tome or tarot cards.

### 2️⃣ CYBERPUNK

**Theme**: Digital Interface / Hacking Terminal
**Wrapped Style**: Cards look like holographic data shards or intercepted decrypted files.

### 3️⃣ HORROR

**Theme**: Found Footage / Psychological Dossier
**Wrapped Style**: Cards look like evidence photos, case files, or polaroids clipped together.

### 4️⃣ HEROES

**Theme**: Comic Book / Superhero Database
**Wrapped Style**: Cards look like superhero trading cards or comic covers.

### 5️⃣ CRIME

**Theme**: Miami Vice / Neon Noir
**Wrapped Style**: Cards look like "Wanted" posters, case files, or neon signage.

- **The Cast**: Mugshots on a pinboard.
- **The Vibe**: Neon equalizer bars.
- **Your Voice**: A wiretap tape recorder visual.

### 6️⃣ SHINOBI

**Theme**: Ink Painting / Feudal Scroll
**Wrapped Style**: Cards look like individual bamboo slats or ink illustrations on rice paper.

### 7️⃣ SPACE_OPERA

**Theme**: Retro Sci-Fi / Pulp Magazine
**Wrapped Style**: Cards look like punch cards, star maps, or crew personnel files.

### 8️⃣ COWBOYS

**Theme**: Wanted Poster / Western Saloon
**Wrapped Style**: Cards look like playing cards (Ace of Spades, etc.) or wanted posters.

---

## 🛠️ Implementation Strategy

### Phase 1: Architecture Refactor

1. Create `GenreReviewStrategy` interface
2. Implement strategy pattern for genre-specific pages
3. Create `SagaWrappedCard` composable ecosystem.

### Phase 2: Genre-by-Genre Implementation

**Recommended Order**:

1. **CRIME** - Focus on the Neon/Retro aesthetic for the first "Wrapped" cards.
2. **CYBERPUNK** - Similar tech aesthetic.
3. **HEROES** - High visual impact.

### Phase 3: Per-Genre Workflow

1. **Design**: Sketch the "Wrapped" cards for the specific genre.
2. **Build**: Implement specific card backgrounds and fonts.
3. **Verify**: Check data mapping for Characters, Emotions, and Messages.

---

## 🎯 Success Metrics

- **Share Rate**: Users sharing their "Saga Wrapped" cards on social media.
- **Completion**: Users finishing the entire review carousel.
- **Delight**: Positive feedback on the genre immersion.
