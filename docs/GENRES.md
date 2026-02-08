# sagAI Genres and Thematic Guidelines

This document enumerates the current themes (genres) available in sagAI and summarizes their core guidelines so artists, writers, and prompt engineers can align visual style, rules, conversation tone, and naming conventions consistently across the project.

Source of truth in code:
- Enum: `features.newsaga.data.model.Genre`
- Prompt helpers: `core.ai.prompts.GenrePrompts`

Genres currently defined:
- FANTASY
- SCI_FI (Cyberpunk)
- HORROR
- HEROES (Urban Hero)

---

## FANTASY
- Display title: "Fantasia"
- Visual identity: warm reds (MaterialColor.Red800, palettes of red/orange), white icons; epic and ethereal mood.
- Ambient music key: `fantasy_ambient_music_url`

Art style
- Classical oil painting with rich impasto texture, visible brushstrokes, strong chiaroscuro, natural lighting, ethereal aesthetic, harmonious painterly colors.

Color/lighting emphasis
- Background dominated by evocative red/orange (scarlet, crimson, burgundy) with dramatic clouds and epic glow.
- CRUCIAL: Character skin/hair/primary clothing keep natural hues (do not tint fully red). Red accents are isolated and clearly defined.

Covers and portraits
- Cover: Simple background in celestial tones with subtle stylized fantasy landmark.
- Portrait: Ethereal epic background with a divine mood.

Styling presets
- Character styling: Vintage style with gold glow, dramatic lighting, portrait framing (defaults from GenrePrompts).
- Chapter cover styling: Fantasy style, gold glow, studio lighting, varied framing.
- Negative prompt: All styles except FANTASY.

Conversation style (narrative and NPCs)
- Vocab: Magic, mythical creatures, ancient kingdoms, weaponry, fantastical concepts.
- Formality: From archaic/formal (nobles, mages) to rustic (villagers). Avoid modern slang.
- Phrasing: Occasional archaic terms used sparingly.
- Tone: Epic/heroic, mystical/respectful, wisdom and lore; measured pacing.

Naming conventions
- Evoke magic, ancient lore, epic adventure, or mystical origins.
- Influences: High fantasy, classical mythology, medieval European, melodious unique sounds.
- Avoid: Overtly modern or tech-sounding names.
- Try common names in current app language when appropriate.

Mood notes
- Dark and moody with dramatic lighting, strong contrast and selective crimson highlights; mystery or impending doom as needed.

---

## SCI_FI (Cyberpunk)
- Display title: "Cyberpunk"
- Visual identity: deep purple/teal neon palette (MaterialColor.DeepPurpleA200 and related palette), white icons; retro anime cyberpunk.
- Ambient music key: `scifi_ambient_music_url`

Art style
- True 80s–90s retro anime cel look: bold, clean inked outlines, limited flat colors, hard-edged shadows, subtle film/cel grain.
- Inspirations: Akira, Ghost in the Shell (1995), Bubblegum Crisis.
- Backgrounds can be detailed yet stylized with industrial/melancholic feel; for icons, simplify backgrounds to keep character dominant.

Color/lighting emphasis
- Background: bold deep purple; minimalist graphic elements allowed.
- Character: small neon-purple accents only (eyes/implants, thin circuits, subtle hair streaks, minor tech highlights).
- CRUCIAL: Preserve natural base colors (skin/hair/clothes). Purple accents are discrete and isolated.

Covers and portraits
- Cover: Minimalist cold-toned background with a single big stylized kanji behind characters.
- Portrait: Subtle cold, melancholic city background.

Styling presets
- Character styling: Anime style, cold neon color, dramatic lighting, close-up framing.
- Chapter cover styling: Cyberpunk style, cold neon, volumetric lighting, varied framing.
- Negative prompt: Exclude non-cyberpunk/anime styles.

Conversation style (narrative and NPCs)
- Vocab: Tech jargon, hacking terms, corporate slang, futuristic street argot.
- Tone: Cynical, weary, guarded; blunt, terse dialogue; fast-paced urgency.
- Profanity: Allowed in moderation for grit and realism.

Naming conventions
- Blend futuristic/cyberpunk with slightly exotic influences (e.g., Japanese, gritty Western phonetics).
- Avoid: Overtly heroic or melodramatic names.
- Prefer names common to the current app language.

Mood notes
- Limited neon-purple palette with dramatic directional lighting, silhouettes, and subtle human–machine fusion (face/neck/eyes/lips implants) without overpowering humanity.

---

## HORROR
- Display title: "Terror"
- Visual identity: desaturated blue-gray palette (MaterialColor.BlueGray200 and palette), black icon color; haunted, mystique tone.
- Ambient music key: `horror_ambient_music_url`

Art style
- Retro pixel art (32-bit), blocky shading; haunted and mystique dark aesthetic; pale blue backgrounds.

Color/lighting emphasis
- Background: dark desaturated blue-gray or near-black.
- Character: extremely minimal, desaturated accents on very small details only.
- CRUCIAL: Largely devoid of vibrant color; pale or shadowed skin; maintain monochrome feel.

Covers and portraits
- Not specifically constrained beyond the above in prompts; follow horror palette and atmosphere.

Styling presets
- Character styling: Vintage style with gold glow, dramatic lighting, portrait framing (fallback defaults).
- Chapter cover styling: Cyberpunk/cold neon/volumetric (fallback default for non-fantasy). Adjust with horror palette in prompts as needed.
- Negative prompt: Not specified; use empty or context-specific.

Conversation style (narrative and NPCs)
- Tone: Psychological dread, paranoia, mundane vs sinister contrast; slow build then accelerate at climax.
- Vocab: Occult and cosmic-horror hints mixed with everyday language; clinical narration when describing horror.
- Profanity: Sparing and contextual.

Naming conventions
- Humans: Common, simple, contemporary names in current app language (horror emerges from the mundane).
- Creatures/entities: Descriptive, guttural, unsettling; can be phrases or folk-like titles.
- Avoid: Overly heroic, futuristic, melodramatic.

Mood notes
- Limited blue-gray palette, moonlight-like highlights, strong directional lighting, dread and forbidden knowledge; focus on environment and supernatural elements.

---

## HEROES (Urban Hero)
- Display title: "Heróis"
- Visual identity: vivid blues/teals (MaterialColor.Blue900 and palette), white icons; dynamic urban night mood.
- Ambient music key: `heroes_ambient_music_url`

Art style
- Comic book art: clean/detailed line work, dynamic poses, cross-hatching/textural shading, dramatic lighting. Palette primarily teal/white with darker tones.

Color/lighting emphasis
- Urban night cityscape with glowing streetlights and silhouetted buildings; cool blue light dominating.

Covers and portraits
- Not specially constrained for cover composition beyond urban cues; follow mood and palette.

Styling presets
- Character styling: Vintage style with gold glow, dramatic lighting, portrait framing (fallback defaults).
- Chapter cover styling: Cyberpunk/cold neon/volumetric (fallback default for non-fantasy); adjust with hero palette/mood in prompts.
- Negative prompt: Not specified; use context-specific.

Conversation style (narrative and NPCs)
- Vocab: Contemporary slang and street jargon; terms of urban life, parkour, light tech; local landmarks.
- Tone: Street-smart, resourceful, direct and authentic; fast-paced; cynicism with a glimmer of hope.
- Profanity: Moderate, strategic for impact.

Naming conventions
- Grounded and contemporary, reflecting diverse urban environment; cool/edgy with subtle mystery; common names in current app language with modern twists or nicknames. Consider agility/speed/resourcefulness; names that could be a street tag.

Mood notes
- Dynamic and energetic atmosphere; strong contrast with electric blue highlights; subtle energy effects around hands/suit or faint aura. Emphasize movement, agility, determination; blurred cityscape suggesting speed.

---

Implementation notes
- Enum values and assets live in `features.newsaga.data.model.Genre` (colors, icons, backgrounds, music keys).
- Prompt texts and style presets originate from `core.ai.prompts.GenrePrompts` (artStyle, getColorEmphasisDescription, coverComposition, portraitStyle, negativePrompt, characterStyling, chapterCoverStyling, nameDirectives, conversationDirective, moodDescription).
- When adding a new genre, update both the enum and these prompt helpers, then mirror the structure here.
