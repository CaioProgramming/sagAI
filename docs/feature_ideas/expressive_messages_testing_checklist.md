# 🎯 Expressive Messages - Testing Checklist

**Branch:** `feature/expressive-messages`  
**Status:** Ready for Testing  
**Date:** January 9, 2026

---

## ✅ Pre-Testing Verification

- [x] All 4 phases implemented
- [x] Build successful (no errors)
- [x] All files committed
- [x] Documentation complete
- [x] Git status clean

---

## 🧪 Testing Checklist

### Phase 1 & 2: Message Rendering

#### Test 1: Basic Action Tag

**Input:**

```
Hello there! <action>waves warmly</action> Nice to meet you!
```

**Expected Results:**

- [ ] "Hello there!" renders as normal text
- [ ] "waves warmly" renders in **bold italic amber** color
- [ ] "waves warmly" **levitates** up and down (if last message)
- [ ] "waves warmly" **flickers** alpha (0.85 → 1.0)
- [ ] "Nice to meet you!" renders as normal text
- [ ] All text flows naturally in one line

**Old Messages:**

- [ ] Scroll up to see old messages with actions
- [ ] They should be **static** (no animation)
- [ ] Still bold italic amber styling

---

#### Test 2: Basic Think Tag

**Input:**

```
I trust you completely. <think>Do I really though?</think>
```

**Expected Results:**

- [ ] "I trust you completely." renders as normal text
- [ ] **Twinkling stars** (✨) appear where thought should be
- [ ] Stars animate (twinkle/fade in and out)
- [ ] **Tap stars** to reveal thought
- [ ] Stars **fade out** (1 second)
- [ ] Text "Do I really though?" **fades in** (1 second)
- [ ] Revealed text is **italic** in **genre color**

**Old Messages:**

- [ ] Scroll up to old messages with thoughts
- [ ] Thoughts should be **revealed** (no stars)
- [ ] Static italic text in genre color

---

#### Test 3: Basic Narrator Tag

**Input:**

```
She smiled brightly. <narrator>She had no idea what awaited her.</narrator> "Let's go exploring!"
```

**Expected Results:**

- [ ] "She smiled brightly." renders as normal text
- [ ] Narrator text appears in a **bordered box**
- [ ] Box has **1dp border** in genre color
- [ ] Box has **semi-transparent background** (surfaceContainer 30%)
- [ ] Text inside is **italic** and **90% font size**
- [ ] "Let's go exploring!" renders as normal text after
- [ ] Everything flows naturally

---

#### Test 4: Mixed Tags (Complex)

**Input:**

```
<action>looks around nervously</action> The coast seems clear. <narrator>A shadow moves in the distance.</narrator> <think>Something's wrong.</think> We should move quickly.
```

**Expected Results:**

- [ ] Action text levitates and flickers (amber, bold italic)
- [ ] Plain text "The coast seems clear." renders normally
- [ ] Narrator appears in bordered box
- [ ] Think tag shows twinkling stars
- [ ] Plain text "We should move quickly." renders normally
- [ ] **All elements flow naturally** without jarring breaks
- [ ] Layout uses FlowRow (wraps if needed)

---

#### Test 5: Multiple Actions

**Input:**

```
<action>draws sword</action> You won't pass! <action>charges forward</action>
```

**Expected Results:**

- [ ] Both action tags levitate and flicker
- [ ] Dialogue text between them is normal
- [ ] All text flows in natural reading order

---

#### Test 6: Scroll Performance

**Steps:**

1. Send 10+ messages with various tags
2. Scroll up and down quickly
3. Check old messages vs new message

**Expected Results:**

- [ ] Scrolling is **smooth** (60fps feel)
- [ ] **Only last message** has animations
- [ ] All other messages are **static styled text**
- [ ] No stuttering or frame drops
- [ ] No visual glitches

---

### Phase 3: Input Field UI

#### Test 7: Tag Insertion Buttons

**Steps:**

1. Focus input field
2. Type some text: "Hello world"
3. Place cursor after "Hello"
4. Click `<action>` button

**Expected Results:**

- [ ] Text becomes: "Hello<action>|</action> world"
- [ ] Cursor is positioned **between tags** (|)
- [ ] Button click is responsive
- [ ] Can type immediately after insertion

---

#### Test 8: Text Wrapping with Tags

**Steps:**

1. Type: "waves enthusiastically"
2. Select all the text
3. Click `<action>` button

**Expected Results:**

- [ ] Text becomes: "<action>waves enthusiastically</action>"
- [ ] Cursor moves to end of text
- [ ] Selection is cleared

---

#### Test 9: All Three Tag Buttons

**Test each button:**

- [ ] `<action>` button inserts `<action></action>`
- [ ] `<think>` button inserts `<think></think>`
- [ ] `<narrator>` button inserts `<narrator></narrator>`

---

#### Test 10: Tag Autocomplete Trigger

**Steps:**

1. Focus input field
2. Type: "Hello <"

**Expected Results:**

- [ ] Autocomplete tooltip **appears above input**
- [ ] Shows title "Insert Tag"
- [ ] Lists all 3 tags:
    - `<action>` Action - Physical movements
    - `<think>` Think - Internal thoughts
    - `<narrator>` Narrator - Narrator voice
- [ ] Tags have genre color highlighting
- [ ] Tooltip has genre-styled border

---

#### Test 11: Tag Autocomplete Selection

**Steps:**

1. Type: "Test <"
2. Autocomplete appears
3. Click `<action>` in autocomplete

**Expected Results:**

- [ ] The `<` is replaced with `<action></action>`
- [ ] Cursor is positioned between tags
- [ ] Autocomplete tooltip **dismisses**
- [ ] Can continue typing immediately

---

#### Test 12: Autocomplete Dismissal

**Steps:**

1. Type: "Test <"
2. Autocomplete appears
3. Type any other character (e.g., "a")

**Expected Results:**

- [ ] Autocomplete **dismisses**
- [ ] Text becomes: "Test <a"
- [ ] No tooltip visible

---

#### Test 13: Button Visual Feedback

**Check:**

- [ ] Tag buttons have genre gradient styling
- [ ] Buttons are clearly visible
- [ ] Buttons show tag syntax and name
- [ ] Buttons are horizontally scrollable if needed

---

### Phase 4: AI Integration

#### Test 14: AI Generates Tags

**Steps:**

1. Send a message: "What do you think about this plan?"
2. Wait for AI response

**Expected Results:**

- [ ] AI response **may include tags** (not guaranteed every time)
- [ ] If AI uses `<action>` tags, they render with animations
- [ ] If AI uses `<think>` tags, stars appear
- [ ] Tags are used **naturally** (not forced)
- [ ] Plain dialogue is still valid and common

---

#### Test 15: AI Tag Privacy

**Steps:**

1. Send: "I trust you. <think>But I'm not sure why.</think>"
2. Wait for AI response

**Expected Results:**

- [ ] AI **does NOT** reference the thought directly
- [ ] AI responds to dialogue: "I trust you"
- [ ] AI may note body language but NOT the thought content
- [ ] Example good response: "I'm glad to hear that. *smiles*"
- [ ] Example bad response: "Why aren't you sure?" (privacy violation!)

---

#### Test 16: AI Suggestions with Tags

**Steps:**

1. Send any message
2. Check the suggestion chips that appear

**Expected Results:**

- [ ] Suggestions **may include tags**
- [ ] Example: "I'll help. <think>Against my better judgment.</think>"
- [ ] Tags in suggestions are properly formatted
- [ ] Clicking suggestion inserts text with tags
- [ ] Tags work when sent

---

#### Test 17: AI Action Tag Usage

**Steps:**

1. Engage in conversation
2. Look for AI using actions naturally

**Expected Results:**

- [ ] AI uses `<action>` for significant physical movements
- [ ] Example: "<action>steps back</action> Wait, what?"
- [ ] Actions are **meaningful** (not overused)
- [ ] Actions enhance the narrative

---

#### Test 18: AI Think Tag Usage

**Steps:**

1. Have conversation with NPC
2. Look for NPC internal thoughts (rare!)

**Expected Results:**

- [ ] AI **rarely** uses `<think>` for NPCs
- [ ] When used, it's for **dramatic effect**
- [ ] Example: "Of course I believe you. <think>Liar.</think>"
- [ ] Creates tension and depth

---

#### Test 19: AI Narrator Tag Usage

**Steps:**

1. Continue conversation
2. Look for narrator context

**Expected Results:**

- [ ] AI uses `<narrator>` for time passing, scene setting
- [ ] Example: "<narrator>Hours pass...</narrator>"
- [ ] Narrator provides **dramatic context**
- [ ] Not overused (maybe 1 in 10 messages)

---

### Edge Cases & Error Handling

#### Test 20: Malformed Tags

**Input:**

```
Test <action>incomplete
Test <think>no closing
Test </action>wrong order
```

**Expected Results:**

- [ ] App doesn't crash
- [ ] Malformed tags render as plain text
- [ ] Parser handles gracefully

---

#### Test 21: Nested Tags (Not Supported)

**Input:**

```
<action>waves <think>nervously</think></action>
```

**Expected Results:**

- [ ] App doesn't crash
- [ ] Renders reasonably (may not be perfect)
- [ ] No visual glitches

---

#### Test 22: Empty Tags

**Input:**

```
Hello <action></action> world
Test <think></think> test
```

**Expected Results:**

- [ ] Empty tags are ignored or render as plain text
- [ ] No visual artifacts
- [ ] Text flows normally

---

#### Test 23: Very Long Messages

**Input:**

```
[Create a message with 500+ characters including multiple tags]
```

**Expected Results:**

- [ ] Message renders completely
- [ ] All tags work correctly
- [ ] FlowRow wraps text appropriately
- [ ] No performance issues

---

#### Test 24: Rapid Tag Button Clicks

**Steps:**

1. Click `<action>` button rapidly 10 times

**Expected Results:**

- [ ] No crashes or freezes
- [ ] Each click inserts a tag
- [ ] UI remains responsive

---

#### Test 25: Autocomplete Spam

**Steps:**

1. Type: "< < < < <" quickly

**Expected Results:**

- [ ] Autocomplete shows/hides smoothly
- [ ] No crashes or glitches
- [ ] Performance remains good

---

### Performance & Polish

#### Test 26: Battery Usage

**Steps:**

1. Use app for 10 minutes with tag-based messages
2. Check battery drain

**Expected Results:**

- [ ] Battery usage is **reasonable**
- [ ] No excessive drain from animations
- [ ] Similar to pre-tag implementation

---

#### Test 27: Memory Usage

**Steps:**

1. Send 50+ messages with tags
2. Monitor memory in Android Studio

**Expected Results:**

- [ ] Memory usage is **stable**
- [ ] No memory leaks
- [ ] Garbage collection is normal

---

#### Test 28: Cold Start Performance

**Steps:**

1. Force close app
2. Reopen and navigate to chat with tags

**Expected Results:**

- [ ] App starts quickly
- [ ] Messages render immediately
- [ ] No lag or stuttering

---

#### Test 29: Different Genres

**Test in each genre:**

- [ ] Fantasy - purple theme
- [ ] Sci-Fi - cyan theme
- [ ] Mystery - dark theme
- [ ] Romance - pink theme
- [ ] Horror - red theme

**Expected Results:**

- [ ] Narrator boxes use correct genre color
- [ ] Think stars use correct genre color
- [ ] Animations look good with all color schemes

---

#### Test 30: Accessibility

**Check:**

- [ ] Text is readable with tags
- [ ] Color contrast is sufficient
- [ ] Tappable areas are adequate (stars)
- [ ] Screen reader support (if applicable)

---

## 📊 Results Summary

### Visual Quality

- [ ] Animations are smooth and polished
- [ ] Tags enhance immersion
- [ ] No visual bugs or glitches
- [ ] Aesthetically pleasing

### Functional Quality

- [ ] All features work as designed
- [ ] No crashes or errors
- [ ] Edge cases handled gracefully
- [ ] User experience is intuitive

### Performance Quality

- [ ] 60fps scrolling maintained
- [ ] No memory leaks
- [ ] Battery usage acceptable
- [ ] Quick startup times

### AI Quality

- [ ] AI uses tags naturally
- [ ] Privacy rules respected
- [ ] Suggestions are expressive
- [ ] Tag usage enhances narrative

---

## 🐛 Issues Found

Document any issues discovered:

**Issue #1:**

- Description:
- Severity: (Critical/High/Medium/Low)
- Steps to reproduce:
- Expected:
- Actual:

**Issue #2:**

- Description:
- Severity:
- Steps to reproduce:
- Expected:
- Actual:

---

## ✅ Sign-Off

**Tested By:** _______________  
**Date:** _______________  
**Device:** _______________  
**Android Version:** _______________

**Overall Quality:** ☐ Excellent ☐ Good ☐ Needs Work ☐ Not Ready

**Recommendation:** ☐ Ship It! ☐ Minor Fixes ☐ Major Fixes ☐ Block Release

---

## 📝 Notes

Additional observations, feedback, or suggestions:

---

**Total Tests:** 30  
**Tests Passed:** ___/30  
**Pass Rate:** ___%

---

Happy Testing! 🎭✨

