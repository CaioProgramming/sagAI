# Character Diversity & Fashion Enhancement

**Date:** January 15, 2026  
**Objective:** Eliminate AI repetitiveness, ensure radical diversity head-to-toe, and create
characters with "AURA" through outstanding fashion.

**Philosophy:** ENRICH, don't ban. Go deeper on descriptions, preserve context details, and make
every character visually memorable.

**Scope:** Affects BOTH character generation (NPC creation, protagonist design) AND image
generation (visual representation).

---

## 🎯 Core Problem Addressed

The app was generating repetitive characters:

- ❌ Every character had an "athletic build"
- ❌ Female characters defaulted to "black hair in a bun"
- ❌ Generic clothing descriptions like "black leather jacket"
- ❌ Limited diversity in facial features, body types, and fashion
- ❌ Characters felt AI-generated and boring, not unique personalities

**Goal:** Make users say "WOW, that character has AURA" - unique stories and personalities in EVERY
way.

---

## ✨ What Changed

### 1. **Guidelines.kt - Character Data Structure Rules**

#### Added Comprehensive Diversity Mandates:

- **Anti-Repetition for Physical Builds:**
    - Vary body composition:
      slim/athletic/curvy/stocky/broad-shouldered/pear-shaped/rectangular/heavyset
    - Vary height: petite/average/tall/towering
    - Vary posture: slouched/military-straight/relaxed/tense/graceful

#### Added Facial Diversity Requirements:

- **HAIR:** Cycle through lengths, textures, styles, colors
    - **BANNED:** "Black hair in a bun" as default for female characters
    - Encourage: buzzed/pixie/bob/long, straight/wavy/curly/coily,
      loose/braided/dreadlocks/bun/ponytail/mohawk
    - Creative colors (if genre-appropriate): blue/purple/green/pink/silver/split-dye

- **EYES:** Not just color - describe SHAPE and expression
    - Shapes: almond/round/hooded/upturned/downturned/monolid/deep-set
    - Colors: brown/amber/hazel/green/blue/gray + unique variations (heterochromia, flecks, rings)

- **FACIAL FEATURES:** Make each face memorable
    - Nose: button/aquiline/broad/narrow/upturned/crooked/pierced
    - Lips: thin/full/bow-shaped/asymmetrical/scarred
    - Jaw: square/rounded/sharp/soft/defined
    - Distinctive marks: freckles/moles/scars/tattoos/piercings/birthmarks - be SPECIFIC

#### Fashion = Personality Mandate:

- **NO generic descriptions** like "leather jacket" or "casual clothes"
- Every outfit needs 3+ SPECIFIC details
- Consider: cut/silhouette, texture, color palette, condition (worn/pristine/patched), customization
- Mix and match: layered/asymmetrical/oversized/fitted/statement pieces

---

### 2. **GenrePrompts.kt - Cyberpunk Fashion Revolution**

#### Complete Fashion System for Cyberpunk:

**"WOW, THAT CHARACTER HAS AURA" MANDATE** - Fashion must be OUTSTANDING, UNIQUE, MEMORABLE.

#### Style Elements to Mix (choose 3-5 per character):

**RETRO FUSION (70s/80s/90s callbacks):**

- Bomber jackets with patches and pins
- Vintage band tees layered under tech vests
- Retro windbreakers in bold color blocks
- Leather trench coats with asymmetrical cuts
- Vinyl/PVC materials mixed with technical fabrics

**Y2K AESTHETIC (Late 90s/early 2000s future-nostalgia):**

- Low-rise cargo pants with excessive pockets and straps
- Metallic fabrics (silver, holographic, iridescent)
- Chunky platform boots or sneakers
- Tiny sunglasses or wraparound visors
- Crop tops with tech harnesses
- Butterfly clips, chokers with tech elements
- Baggy pants + fitted tops (or inverse)

**NEO-TOKYO STREET (Japanese urban fashion influence):**

- Techwear with multiple straps and buckles
- Oversized silhouettes mixed with fitted pieces
- Harajuku-inspired layers and accessories
- Graphic tees with kanji or futuristic logos
- Utility vests over long sleeves
- Mask accessories (face masks, cybernetic respirators)

**TECH-ENHANCED STREETWEAR:**

- LED-embedded fabrics with programmable patterns
- Armored panels integrated into fashion pieces
- Smart fabrics that change color/texture
- Modular accessories (detachable sleeves, convertible jackets)
- Fiber-optic threading in seams
- Reactive materials (shifts with temperature/mood)

**ACCESSORY AURA-BUILDERS:**

- Statement belts with tech buckles
- Multiple chain accessories (wallet chains, key chains)
- Customized gloves (fingerless, cyber-enhanced, tactical)
- Unique eyewear (colored lenses, HUD glasses, custom frames)
- Tech jewelry (holographic earrings, circuit bracelets)
- Bags that show personality (messenger, sling, tactical)

#### Color Palette Diversity:

DON'T make everyone wear black/gray/dark. Mix bold colors:

- Electric blues, hot pinks, acid greens, neon oranges
- Deep purples, blood reds, cyber yellows
- Metallics: chrome, gold, copper, holographic
- Color-blocking: unexpected combinations that POP

#### Outfit Construction Rules:

1. **LAYERING:** Mix 2-4 layers with different textures/cuts
2. **CONTRAST:** Combine fitted + oversized, sleek + rugged, vintage + futuristic
3. **ASYMMETRY:** Uneven hemlines, one-shoulder, diagonal zippers, mismatched sleeves
4. **TEXTURE MIX:** Leather + mesh, denim + metallic, rubber + fabric
5. **PERSONALITY STAMP:** Every outfit has 1-2 signature pieces that scream "THIS IS ME"

#### Example "AURA" Outfits:

✓ "Oversized holographic bomber jacket over cropped black turtleneck, low-rise cargo pants with
chrome buckles, chunky platform boots with LED soles, tiny pink-tinted visor, chrome left arm
visible"

✓ "Asymmetrical leather trench coat (one long sleeve, one short), vintage band tee, high-waisted
utility pants with excessive straps, combat boots with neon laces, neural ports glowing at temples"

✓ "Y2K aesthetic: metallic silver crop top with tech harness, baggy cargo pants in electric blue,
fingerless gloves with circuit patterns, artificial eyes with amber glow, tiny rectangular
sunglasses"

✓ "Neo-Tokyo layers: oversized tech vest with kanji patches over fitted mesh long-sleeve, techwear
pants with multiple pockets, tactical boots, face mask with respirator elements, chrome spine
visible at neck"

#### What to AVOID:

- ✗ GENERIC/BORING FASHION: "black leather jacket" with no details (LAZY)
- ✗ ALL-BLACK EVERYTHING: Makes characters blend together (REPETITIVE)
- ✗ COOKIE-CUTTER CYBERPUNK: trench coat + katana + mirrored shades (CLICHÉ)

---

### 3. **CharacterPrompts.kt - Character Generation Updates**

#### Added Anti-Repetition Enforcement:

Every character in a saga should be visually DISTINCT. NO two characters should share the same:

- Build: Avoid defaulting to "athletic". Mix: slim/stocky/curvy/lanky/muscular/heavyset/petite
- Hair: DON'T repeat styles. Vary length, texture, color, and styling
- Eye shape AND color: Not just "brown eyes" - describe the SHAPE and unique qualities
- Facial structure: Unique nose, lips, jaw, distinctive marks
- Fashion sense: Each character needs a SIGNATURE look that shows personality

#### Head-to-Toe Uniqueness:

Use ALL the data fields to create truly unique characters:

- **PhysicalTraits:** height, weight, build, ethnicity, skin tone, facial features
- **FacialFeatures:** hair (length/texture/color/style), eyes (shape/color), mouth, jawline,
  distinctive marks
- **BodyFeatures:** build/posture, skin appearance, distinguishing features
- **Clothing:** outfit style, accessories, carried items - ALL should reflect personality

**Reminder:** These fields exist to ensure NO character feels generic or AI-generated.

#### Updated Character Intro Suggestions:

- Added diversity mandate: Each of the 3 suggestions MUST have DIFFERENT ethnicity, body type, hair
  style, fashion sense
- NO "athletic build" defaults - vary body types
- NO repeated hair styles
- Clothing should show personality - NO generic descriptions

#### Cyberpunk Character Suggestions:

Now requires BOTH:

1. **Visible Cyberware** (2-3 augmentations)
2. **Outstanding Fashion** (Y2K/retro/neo-Tokyo mix with 2-3 specific outfit details)

Example format: "[Personality] with [cyberware detail],
wearing [specific outfit with 2+ details], [backstory hook]"

---

## 📊 Impact on Character Generation

### Before:

- "Athletic build female character with black hair in a bun wearing a black leather jacket"
- "Athletic build male character with short dark hair wearing tactical gear"
- Generic, repetitive, boring

### After:

- "Stocky netrunner with chrome left arm and artificial amber eyes, wearing an oversized holographic
  bomber over low-rise cargo pants with chrome buckles and chunky platform boots"
- "Lanky fixer with neural ports at temples and wrist interface, dressed in asymmetrical leather
  trench coat over vintage band tee, high-waisted utility pants with neon-laced combat boots"
- Unique, memorable, has AURA

---

## 🎨 Next Steps for Other Genres

While this update focused heavily on **Cyberpunk**, the diversity and uniqueness principles apply to
ALL genres:

### Future Enhancements Needed:

1. **Fantasy:** Add Renaissance-inspired fashion diversity (not just robes - vary silhouettes,
   colors, accessories)
2. **Horror:** Emphasize worn/distressed clothing with personal history
3. **Heroes:** Urban streetwear with signature superhero elements
4. **Crime:** High-end luxury resort wear with individual style
5. **Space Opera:** Retro-futuristic fashion with personality
6. **Shinobi:** Traditional ninja attire with unique customization
7. **Cowboy:** Western wear with personal touches

Each genre should receive similar fashion depth to ensure characters feel unique and alive.

---

## 🔧 Implementation Notes

### Files Modified:

1. `/app/src/main/java/com/ilustris/sagai/core/ai/prompts/Guidelines.kt`
    - Added comprehensive diversity rules
    - Added facial diversity requirements
    - Added fashion = personality mandate
    - **Used by:** Character generation AI

2. `/app/src/main/java/com/ilustris/sagai/core/ai/prompts/GenrePrompts.kt`
    - Complete cyberpunk fashion system
    - Y2K, retro fusion, neo-Tokyo aesthetics
    - Color palette diversity
    - Outfit construction rules
    - **Used by:** BOTH character generation AND image generation

3. `/app/src/main/java/com/ilustris/sagai/core/ai/prompts/CharacterPrompts.kt`
    - Anti-repetition enforcement
    - Head-to-toe uniqueness requirements
    - Updated character intro suggestions
    - Enhanced cyberpunk character examples
    - **Used by:** Character generation AI (NPC creation, protagonist design)

4. `/app/src/main/java/com/ilustris/sagai/core/ai/prompts/ImagePrompts.kt` ✨ **NEW**
    - Now includes `appearanceGuidelines()` in image generation prompts
    - Ensures fashion details (Y2K/retro/neo-Tokyo) are applied to visual output
    - **Used by:** Image generation AI (Art Director, visual composition)

---

## 🎨 How Fashion Affects Image Generation

### Before Integration:

- ❌ Character data had detailed fashion (from CharacterPrompts)
- ❌ Image generation only got `artStyle()` (rendering technique)
- ❌ Fashion details were lost during image generation
- ❌ Result: Characters generated with great fashion descriptions, but images didn't reflect those
  details

### After Integration:

- ✅ Character data has detailed fashion (from CharacterPrompts + appearanceGuidelines)
- ✅ Image generation NOW gets `appearanceGuidelines()` + `artStyle()`
- ✅ Fashion system (Y2K, retro, neo-Tokyo) is passed to the Art Director AI
- ✅ Result: Images reflect the same fashion diversity and "aura" as character descriptions

### Example Flow:

**Character Generation (NPC):**

```
AI creates: "Stocky netrunner with chrome left arm and artificial amber eyes, 
wearing oversized holographic bomber over low-rise cargo pants with chrome 
buckles and chunky platform boots"
```

**Image Generation (for that character):**

```
Art Director AI receives:
- Character traits (stocky build, chrome arm, amber eyes)
- appearanceGuidelines() → Y2K/retro/neo-Tokyo fashion rules
- artStyle() → 1980s OVA cel-shading technique

Result: Image shows the character with the exact fashion details, 
rendered in proper cyberpunk style with visible cyberware and Y2K aesthetic
```

---

- **ENRICH, DON'T BAN:** Add depth to basic descriptions, don't prohibit them
- **CONTEXT FIRST:** Preserve user/discovery seed details, expand on them with specificity
- **OPTIONAL DETAILS:** Not every character needs scars/tattoos - only add if it enhances the
  character
- **Use ALL data fields** in Character.kt to ensure uniqueness
- **Fashion reflects personality** - transform simple descriptions into vivid, memorable outfits
- **Diversity is head-to-toe** - not just racial diversity, but variety in every aspect
- **Make users say "WOW"** - characters should have aura

---

## ✅ Success Criteria

Characters generated should:

1. ✅ Have unique physical builds (not all athletic)
2. ✅ Have diverse hair styles, textures, and colors
3. ✅ Have specific facial features with memorable details
4. ✅ Wear outstanding, personality-driven outfits with 3+ specific details
5. ✅ Feel like real people with depth, not AI-generated templates
6. ✅ Make users think "I want to know more about this person"
7. ✅ For Cyberpunk: Mix retro/Y2K/neo-Tokyo fashion + visible cyberware

---

## 📝 Maintenance

When adding new genres or updating existing ones:

- Always include **anti-repetition** guidelines
- Define **3-5 fashion elements** to mix and match
- Provide **specific examples** of outstanding outfits
- Emphasize **color diversity** and **personality expression**
- Remind AI to use **ALL character data fields** for uniqueness

This ensures the app never feels repetitive or AI-boring - every character is a unique personality.

