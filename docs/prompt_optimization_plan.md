# Prompt Optimization Plan

## Current Token Estimates

### extractComposition()

- **Current:** ~2,100 tokens (too verbose with detailed explanations)
- **Target:** ~450 tokens (78% reduction)
- **Strategy:** Condensed bullet format, remove explanatory text, keep technical specs

### iconDescription()

- **Current:** ~3,500 tokens (very comprehensive but repetitive)
- **Target:** ~1,200 tokens (66% reduction)
- **Strategy:** Merge redundant sections, use shorthand notation, reference instead of repeat

### reviewImagePrompt()

- **Current:** ~2,800 tokens (16 validation points with examples)
- **Target:** ~800 tokens (71% reduction)
- **Strategy:** Collapse similar checks, remove verbose examples, keep critical validations

## Total Savings

- **Before:** ~8,400 tokens across all 3 prompts
- **After:** ~2,450 tokens across all 3 prompts
- **Savings:** ~5,950 tokens (71% reduction)

## Optimization Techniques

1. **Remove Decorative Elements**
    - ASCII borders/separators
    - Redundant headers
    - Verbose introductions

2. **Condense Format**
    - Multi-line explanations → Single line with abbreviations
    - Example blocks → Inline examples
    - Repetitive patterns → Referenced once

3. **Use Abbreviations**
    - "CAMERA ANGLE" → "ANGLE"
    - "OUTPUT FORMAT" → removed, implied
    - Technical terms shortened where clear

4. **Merge Similar Sections**
    - Combine related validation checks
    - Group translation examples by category
    - Consolidate output structure requirements

5. **Remove Redundancy**
    - Say it once, reference it elsewhere
    - Remove re-explanations of same concept
    - Cut motivational/instructional fluff

## Implementation Order

1. ✅ extractComposition() - Director
2. ✅ iconDescription() - Artist
3. ✅ reviewImagePrompt() - Reviewer

Each will maintain full functionality with drastically reduced token count.

