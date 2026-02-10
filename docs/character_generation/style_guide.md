# Character Style Guide 🎨

This document defines the visual standards, art direction, and rendering techniques for generating characters in SagAI.

---

## 1. General Principles

### 🖌️ Rendering Essence (The "How")
It is not enough to simply name an art style (e.g., "Oil Painting"). We must describe the **rendering characteristics** to avoid generic digital art results.

*   **Brushwork:** Specific instructions like "smooth seamless blending on skin" vs "visible directional brushwork on fabric."
*   **Lighting:** "Warm golden hour lighting with cool luminous blue/purple shadows" creates depth.
*   **Texture:** "Thick impasto highlights" or "translucent glazes" define the surface quality.
*   **Contrast:** "Dramatic chiaroscuro" ensures dynamic range.

### 👁️ Truthful Extraction (The "What")
**ACCURACY > COMPLETENESS.**
The AI must be brutally honest about what is visible in the frame.

*   **Framing Rules:**
    *   **Close-Up (CU):** Only describe head/neck/shoulders. NEVER describe hands or lap.
    *   **Medium Shot (MS):** Head to waist. NEVER describe legs/feet.
    *   **Full Shot (FS):** Full body visible.
*   **No Hallucinations:** If a body part is not visible, do not invent its position. It is better to say "not visible" than to guess.

### 🎨 Character Theme Color
Every character has a `hexColor` that acts as a visual identity guide.

*   **Usage:** Use this color for hair highlights, eyes, accessories, clothing accents, or cyberware LEDs.
*   **Constraint:** Do NOT force this color onto skin tone (respect natural ethnicity).
*   **Flexibility:** It's a theme, not a uniform. Use it where it creates visual cohesion.

---

## 2. Genre-Specific Guides

### 🤖 Cyberpunk: The "Consumption" Aesthetic
**Core Philosophy:** Technology is not just enhancement; it is **consumption**. Characters should look like they've been through hell and come out more machine than flesh.

**Visual Intensity Scale:**
*   **Target:** 30-50% Flesh, 50-70% Chrome.
*   **Mandatory Elements:**
    *   **Heavy Augmentation:** Full limb replacements, not just "accents".
    *   **Ocular Mods:** Mechanical eyes, LED arrays (Crucial).
    *   **Scars & Asymmetry:** Visible seams where flesh meets metal. Mismatched limbs.
    *   **Wear & Tear:** Rust, duct tape, necrosis, infection marks. nothing looks "new".

**Common Mistakes:**
*   ❌ Hiding augmentation under clothing.
*   ❌ Perfect symmetry.
*   ❌ Clean, polished "Apple store" aesthetic (unless specifically Corporate).

### ⚔️ Fantasy: Renaissance Oil Painting
**Core Philosophy:** Ethereal, divine beauty rendered with classical techniques.

**Art Direction:**
*   **Style:** 15th-16th Century Italian Renaissance (Botticelli, Raphael).
*   **Technique:** Soft blended edges (sfumato), layered translucent glazes, visible canvas texture.
*   **Colors:** Dominant **Crimson Red** and **Radiant Gold**.
*   **Banned Terms:** "Digital painting", "concept art", "armor" (unless explicitly required, prefer flowing robes), "8k", "unreal engine".

**Enforcement:**
*   If the prompt sounds like a video game character, it is wrong. It should sound like a museum piece.

### 📷 Realistic / Heroes: Cinematic Photography / Alex Ross Style
**Core Philosophy:** Grounded realism with heightened, dramatic lighting.

**Art Direction:**
*   **Style:** Photorealistic Gouache/Oil (Alex Ross style) or Cinematic Photography.
*   **Lighting:** Strong key light, rim lighting to separate subject from background.
*   **Camera:** Use specific lens lengths (e.g., "85mm portrait lens") and f-stops ("f/1.8 shallow depth of field").

---

## 3. Fashion & Diversity

### 👗 Fashion Enhancement
Clothing is a storytelling device.
*   **Texture:** Describe materials (silk, leather, nanoweave, rusted metal).
*   **Fit:** How does it hang? (Loose, tailored, tattered).
*   **Context:** Does it fit the environment? (e.g., high-collar coat in a rainy cyberpunk city).

### 🌍 Diversity
*   **Skin Tone:** Respect natural ethnicity. Do not whitewash or apply theme colors to skin.
*   **Features:** Describe specific ethnic features (e.g., "monolid eyes", "broad nose", "coily hair") rather than generic terms.
