# Pillar 1: Director 🎬

**Function:** `extractComposition()`  
**File:** `ImagePrompts.kt`  
**Role:** Senior Director of Photography (DP)

---

## Purpose

The Director analyzes reference images and extracts the **photographic DNA** — the pure
cinematographic elements that define how the image is captured, independent of subject matter or art
style.

---

## What It Extracts

### TIER 1 - CRITICAL (Must-Have)

#### 1. ANGLE

- Eye-level, low-angle (with degrees), high-angle (with degrees), or dutch-angle (with degrees)
- Specifies camera position relative to subject
- **Example:** `low 45°` = camera 45 degrees below subject eye-line

#### 2. LENS

- Focal length category: 14-24mm (ultra-wide) → 200mm+ (super-tele)
- Defines perspective compression/distortion
- **Example:** `24-35mm wide` = moderate wide-angle with slight distortion

#### 3. FRAMING

- Shot type locked to standard cinematography terms:
    - ECU (Extreme Close-Up): Face only
    - CU (Close-Up): Head to shoulders
    - MCU (Medium Close-Up): Head to chest
    - MS (Medium Shot): Head to waist
    - MWS (Medium Wide Shot): Head to knees
    - FS (Full Shot): Complete body
    - WS (Wide Shot): Body + environment
    - EWS (Extreme Wide Shot): Small subject in vast space
- **Example:** `CU head-shoulders`

#### 4. PLACEMENT

- Horizontal: left/center/right third
- Vertical: upper/center/lower third
- **Example:** `H: center, V: lower third` = centered horizontally, anchored at bottom

---

### TIER 2 - IMPORTANT (Significant Impact)

#### 5. LIGHTING

- Direction: front/side/back/top/under/omni
- Quality: hard/soft
- **Example:** `side hard` = harsh side lighting with sharp shadows

#### 6. COLOR

- Temperature: cool (5500K+), neutral (5000K), warm (2500-3500K)
- Dominant palette description
- **Example:** `warm 3000K, golden orange tones`

#### 7. ENVIRONMENT

- Location type and scale
- Key environmental elements (NOT brand names)
- **Example:** `Urban alley, narrow space, graffiti walls, dumpsters`

#### 8. MOOD

- Emotional tone of the image
- **Example:** `epic`, `intimate`, `oppressive`, `nostalgic`

---

### TIER 3 - REFINEMENT (Polish & Details)

#### 9. DOF (Depth of Field)

- f-stop ranges: f/1.2 (razor thin) → f/16+ (infinite)
- **Example:** `shallow f/2.8` = blurred background

#### 10. ATMOSPHERE

- Environmental conditions
- **Example:** `misty`, `foggy`, `clear`, `dusty`

#### 11. PERSPECTIVE

- Geometric distortion type
- **Example:** `converging`, `parallel`, `foreshortening`

#### 12. TEXTURE

- Image quality characteristics
- **Example:** `film-grain`, `razor-sharp`, `soft-diffused`

#### 13. TIME

- Lighting scenario
- **Example:** `golden-hour`, `midday`, `blue-hour`, `night`

#### 14. SIGNATURE

- One unique unforgettable detail that defines the image
- **Example:** `dramatic rim light separating subject from darkness`

---

## Output Format

The Director outputs **15 parameters** in the format:

```
PARAMETER_NAME: value description
```

Example:

```
1. ANGLE: low 45° / looking upward
2. LENS: 24-35mm wide / slight perspective distortion
3. FRAMING: FS full-body / complete figure visible
4. PLACEMENT: H: center third / V: lower third
5. LIGHTING: side hard / sharp cast shadows
6. COLOR: cool 5500K / blue-grey tones, desaturated
7. ENVIRONMENT: Urban rooftop / cityscape below, antenna arrays, AC units
8. MOOD: Epic and solitary / lone hero above the city
9. DOF: moderate f/4 / subject sharp, background softly blurred
10. ATMOSPHERE: clear with light haze / distant city in soft focus
11. PERSPECTIVE: slight upward convergence / buildings lean inward
12. TEXTURE: digital-clean / crisp with subtle grain
13. TIME: blue-hour / twilight with city lights beginning
14. SIGNATURE: dramatic silhouette against glowing cityscape
```

---

## Validation Checklist

Before outputting, the Director validates:

- ✓ Angle specified with degrees (if not eye-level)?
- ✓ Lens specified with millimeter range?
- ✓ Framing locked to standard cinematography term?
- ✓ NO art style descriptions (only photo terms)?
- ✓ ONLY camera/lighting technical language?

---

## What It Does NOT Extract

❌ Subject identity (person, object, character)  
❌ Art style (painting, cartoon, etc.)  
❌ Genre elements (fantasy, sci-fi, etc.)  
❌ Story or narrative context  
❌ Clothing or character details

**The Director sees ONLY through a camera lens, not an artist's eye.**

---

## Strengths

✅ **Extreme precision** - 15 detailed parameters  
✅ **Professional terminology** - Real cinematography language  
✅ **Structured output** - Consistent format every time  
✅ **Comprehensive coverage** - All visual aspects captured  
✅ **Validation built-in** - Self-checks before output

---

## Design Philosophy

> "The Director speaks the language of cameras, not artists. It doesn't care about 'whimsical' or '
> dark fantasy'—it cares about millimeters, degrees, and f-stops."

The Director provides the **technical blueprint** that the Artist will translate into creative
language.

---

## Usage in System

```
Reference Image
    ↓
[DIRECTOR] extracts 15 cinematography parameters
    ↓
Technical specs passed to Artist
    ↓
Artist translates to visual language (see Translation Layer in Artist pillar)
```

---

## Code Location

**Function:** `ImagePrompts.extractComposition()`  
**File:** `/app/src/main/java/com/ilustris/sagai/core/ai/prompts/ImagePrompts.kt`  
**Lines:** ~38-69

---

## Related Documentation

- [Artist Pillar](./02_artist_pillar.md) - Receives Director output
- [Translation Layer](./02_artist_pillar.md#translation-layer) - How specs are converted
- [System Flow](./system_flow.md) - Complete workflow

---

**Key Takeaway:** The Director is a cinematographer, not an artist. It analyzes images through pure
technical camera parameters, providing the foundation for artistic interpretation.

