# Saga Wrapped — Genre Presentation Templates (Implementation Status)

> Companion to `task.md` in this same folder. `task.md` scopes a **premium, video-export**
> version of a themed Wrapped (Signature tier) — that's still just an idea, not started, and
> blocked on a video-capability spike (no Media3/MediaCodec in the project today).
>
> **This document is different**: it tracks the **free, in-app** SagaReview per-genre visual
> templates — the actual screens players see when they open a saga's review, redesigned to feel
> native to each genre instead of one generic layout for all 9. This work is real and in
> progress, built across a long session on branch `claude/sagareview-theme-per-saga-86ov14`.

## Architecture (already built, don't re-derive)

- `ReviewTemplate` enum (`features/saga/detail/review/ui/ReviewTemplate.kt`) — `DEFAULT`,
  `TERMINAL`, `BOOK`, `CRIME`, one per shared visual style. Multiple `Genre`s can point at the
  same template (Cyberpunk+Space Opera → TERMINAL, Fantasy+Shinobi → BOOK).
- `Genre.reviewTemplate()` (`GenreReviewTemplateMapping.kt`) maps each genre to a template;
  unmapped genres fall back to `DEFAULT`.
- `ReviewExperienceFactory` (`ReviewExperienceFactory.kt`) resolves `Genre -> ReviewExperience`,
  with a `bespokeByGenre` escape hatch for a genre that later needs a fully custom experience
  instead of a shared template — additive, no interface changes needed when that day comes.
- `ReviewExperience.navigationStyle` (`ReviewExperience.kt`) — `VerticalSwipe` (today's default
  Stories-style pager), `TerminalSwipe` (Terminal — same `VerticalPager` mechanics as Default,
  just terminal-styled chrome on top), `HorizontalPageFlip` (unused — see Book below),
  `ContinuousScroll` (hands-free auto-scroll, Book), `ChatScroll` (pinned-to-latest simulated
  chat, Crime). `SagaReview.kt` picks the right container composable
  (`DefaultReviewContainer`/`TerminalReviewContainer`/`BookReviewContainer`/
  `ContinuousScrollReviewContainer`/`ChatScrollReviewContainer`) based on this — all five still
  speak the same `ReviewAction` contract, so `SagaReviewViewModel`/`ReviewGenerationCoordinator`
  never needed to change.
- Each template's pages live under
  `features/saga/detail/review/ui/templates/{terminal,book,crime}/`.

## Rule: before building a new genre template

Compare the new template's page list against `DefaultReviewExperience`, stage by stage. For every
data-driven visual Default renders (a chart, a stat counter, an avatar, a photo collage, a cast
illustration, a Share action) decide explicitly: **reuse it** (reskinned to the template's own
idiom), **replace it** with an equivalent in that idiom, or **omit it deliberately** — and say why
in the page's doc comment. Never let a stage "port" as plain title/subtitle text just because that
was the fastest thing to wire up — that's how a template ends up with real functionality (a stat,
a photo, a share action) quietly missing instead of just looking different. This is what the Book
retrofit (below) had to fix after first shipping this way.

## Shipped

- **Cyberpunk → Terminal**: monospace, self-contained CRT scanline `Canvas` (not the
  Remote-Config-gated shader pipeline — works identically on every device/API level), a
  periodic glitch VFX overlay, and a real `VerticalPager` (not a plain tap target) so swipe
  is discoverable and consistent with the rest of the app; a bottom progress bar plus explicit
  Continue/Share buttons cover users who don't swipe.
- **Fantasy → Book**: serif on parchment, 3D page-flip via `HorizontalPager` + `graphicsLayer`
  (same recipe the real `BookReader.kt` already uses for reading Acts).
- **Book retrofit** (2026-08-14): Book originally only ported each stage's title/subtitle text,
  dropping every data-driven visual Default has for that stage. Closed, one dedicated page class
  per stage instead of the generic `BookTextPage`:
  - `BookExpressivenessPage` — reuses `VibeShapeDrawing` (emotional-tone shape), inked instead of
    shimmered (parchment doesn't shimmer; the pen-drawn shape is the flourish).
  - `BookPlaystylePage` — reuses `AnimatedPlaytimeCounter`, serif-styled. (Default's own
    `ReviewPlaystylePage` has no Share button either despite `ShareType.PLAYSTYLE` being fully
    wired end-to-end — a real gap, but in Default too, so left alone here; worth its own fix.)
  - `BookCharactersPage` — added `CharacterAvatar` per cast row (was name-only text) + Share link.
  - `BookJourneyPlatePage` (new) — multi-image "plate" collage, sepia-framed, replacing the
    single-image `BookIllustrationPage` insert; not a reuse of Default's `JourneyCollage`/
    `ChapterCardView`, which lean on `MaterialTheme.colorScheme.primary`/`sagaShape()` and would
    look neon/holographic against parchment.
  - `BookConclusionPage` (new) — reuses `SagaLegendLayout` (the cast mosaic), which needed
    `cellBorderColor`/`cellShape` params added to `SagaLegendLayout`/`GtaCell` in
    `ReviewComponents.kt` (defaults preserve Default's black-border GTA look) so Book could pass a
    sepia rounded frame instead.
  - `BookFarewellsPage` — added `CharacterAvatar` per farewell row (was name-only text).
  - New shared `BookShareLink` (`templates/book/BookComponents.kt`) — an italic serif clickable
    `Text`, not a filled `Button`; Book never uses filled buttons anywhere (see
    `BookSummaryPage`'s Restart/Regenerate links), so Share needed to match that idiom rather than
    import Default's `ButtonDefaults.elevatedButtonColors()` styling.
  - Added `SagaContent.notableChapterImageSources(limit)` to `ReviewImageSources.kt` (existing
    `notableChapterImageSource()` — singular — stays, still used by the Characters stage's
    top-character portrait insert).
- **Farewells stage**: a 7th review step — after Conclusion, the saga's 3-4 most-talked-about
  characters each get a short AI-generated farewell message. `Review.farewells: List<Farewell>?`
  (new, required for `isComplete()`), generated via a one-shot `gemmaClient.generate<FarewellSet>`
  call special-cased inside `SagaReviewUseCaseImpl.generateStep` (bypasses the streaming
  `ReviewStage` path the other 6 steps use). Present in Default, Terminal, and Book.
- **Images in Terminal + Book**: both templates were 100% typographic; added a shared
  `ReviewImageSources.kt` resolver (`coverImageSource`/`topCharacterImageSource`/
  `notableChapterImageSource`, all reusing existing fields — no new AI image generation) plus a
  cover page and two illustration insertion points (after Characters, after Journey) per
  template — Book as "plate illustrations", Terminal as a progressive "decoding" image wipe.
- **`ReviewNavigationStyle.ContinuousScroll`** (2026-08-14) — the hands-free continuous-scroll
  style Crime/Cowboy need, built ahead of time as infrastructure: `AutoScrollLazyColumn`
  (`ui/animations/AutoScrollLazyColumn.kt`) is a `LazyColumn` that drifts forward on its own via
  `withFrameNanos` + `ScrollableState.scrollBy` (per-frame delta, not one long `animateScrollBy`
  like `AnimatedChapterGridBackground`'s background loop — stops cleanly at list end, no bounce
  needed since this reads once instead of looping); a touch-down (`pointerInteropFilter`, same
  pause mechanic `DefaultReviewContainer` already uses) pauses it instantly, and it waits 20s
  after the finger lifts before drifting again (no delay on the very first auto-scroll, before
  any touch ever happened). `ContinuousScrollReviewContainer` in `SagaReview.kt` wires this into
  the same `ReviewAction`/`ReviewPage` contract the other three containers use.
- **Fantasy → Book switched from `HorizontalPageFlip` to `ContinuousScroll`** (2026-08-14) —
  `BookReviewExperience.navigationStyle` now points at the scroll style instead of the 3D
  page-turn; fits the "unrolling a scroll" reading metaphor better than page-turning did.
  `BookReviewContainer` (`HorizontalPageFlip`'s container in `SagaReview.kt`) is untouched and
  still wired into the `when` — just currently unreachable since no `ReviewExperience` returns
  `HorizontalPageFlip` anymore. Left in place rather than deleted, in case page-turn comes back
  for a different genre later.
- **Newspaper-layout pass on real-device feedback** (2026-08-14) — screenshots surfaced several
  issues once actually seen on a phone:
  - `BookCharactersPage` cast list changed from a vertical `Column` of rows to a horizontal
    `LazyRow` (avatar → full name → message count, stacked per character) — reads like a
    newsroom masthead contributor strip.
  - The most-talked-about-character portrait (`BookIllustrationPage`, fed by
    `topCharacterImageSource()`) moved from *after* the cast list to right after the Characters
    hook epigraph, *before* the list — and switched from a rectangular plate to a `CircleShape`
    cameo (`BookReviewExperience.kt`'s `review.topCharacters` block).
  - `BookConclusionPage` had text overlaid on the `SagaLegendLayout` photo mosaic inside an
    `aspectRatio` `Box` — illegible once the page was no longer full-screen. Reworked to a linear
    stack: title → mosaic → caption → share, no overlay.
  - `BookFarewellsPage` changed from uniform avatar-left rows to alternating left/right (odd/even
    index), portrait always on the outer edge, like a newspaper opinion column.
  - `review_farewells_title` ("The Send-Off") was the **only** `review_*` string key missing a
    `values-pt-rBR` entry (verified by diffing all `review_*` keys between the two files) — added
    "A Despedida". Not a systemic gap, just this one string; if a similar mixed-language report
    comes up again, that diff approach is the fast way to confirm scope before assuming it's
    widespread.
  - `BookCoverPage` went from an `aspectRatio(0.8f)` block to a full `screenHeightDp` hero (the
    one deliberate exception to "no page is full-screen" — it's the masthead), and its scrim
    switched from a flat black `Brush.verticalGradient` to `fadeGradientBottom(colorScheme.background)`
    — the same transparent-to-background fade `SagaDetailView`/`CharacterDetailsView` use over
    their own hero images, so the title sits on the theme's actual background color instead of
    pure black regardless of light/dark.
  - Dropped the fixed warm-parchment palette entirely (`BookBackground`'s cream gradient +
    every page's hardcoded `private val Ink = Color(0xFF3B2E1F)`). Background is now
    `MaterialTheme.colorScheme.surfaceContainer` with a faint `onSurface`-tinted grain +
    vignette layered on top (still Canvas-drawn, same "aged page" texture recipe, just
    theme-derived colors instead of fixed ones so it still reads right in dark mode). Body text
    color is `LocalContentColor.current` (the actual ambient default — deliberately *not*
    `MaterialTheme.colorScheme.onSurface` explicitly, since the whole point was "stop pinning a
    color, let the default do its job") captured once per page as a local `ink` val, reused for
    `.copy(alpha = X)` de-emphasis. Genre `accent` (title color, dividers) is untouched — that's
    deliberate genre identity, not the parchment problem that was flagged.
- **Book article pass** (2026-08-14) — first attempt at `ContinuousScroll` rendered each page as
  one screen-height item with its own `Background()`, which read as a slow-motion pager, not an
  article. Reworked, both in `ContinuousScrollReviewContainer` (generic) and every Book page
  (specific):
  - Items now wrap content height (`fillMaxWidth`, never `fillMaxSize`) instead of
    `fillParentMaxHeight` — sections stack like paragraphs of one continuous page, not screens.
  - Background is drawn once (`pages.firstOrNull()?.Background()`) behind the whole scroll
    surface instead of once per item — correct for Book specifically since every Book page
    returns the identical `BookBackground` anyway, so per-item drawing was pure waste.
  - `BookCoverPage`/`BookConclusionPage` (the two "hero" compositions with text overlaid on
    imagery) switched from full-screen framing to `aspectRatio`-bound blocks (`0.8f`/`0.75f`) —
    still reads as a banner/mosaic moment, just not screen-height.
  - `BookFarewellsPage`'s inner `LazyColumn` (nesting scrollables inside an unbounded-height
    parent crashes) became a plain `Column` — farewells are ~3-4 items, no laziness needed.
  - Removed every hardcoded `fontFamily = FontFamily.Serif` across all Book pages. `Theme.kt`'s
    `dynamicTypography` already swaps `MaterialTheme.typography`'s header/body fonts to the
    genre's remote-configured font when the theme loads (see `SagAITheme`/`resolvedFonts`) —
    hardcoding `FontFamily.Serif` silently overrode that per-genre font with a generic system
    serif. Book text now just uses `MaterialTheme.typography.*` styles directly, like Default
    does everywhere, so Fantasy's actual configured font shows up instead of a fallback.
  - Flowing body paragraphs (`BookTextPage`'s content variant, `BookExpressivenessPage`/
    `BookPlaystylePage` subtitles) now reveal via `SimpleTypewriterText` instead of a static
    `Text`, duration scaled to text length (`length * 16ms`, clamped 800–4000ms) rather than one
    fixed duration for every paragraph regardless of how long it is — meant to read as a living
    article typing itself in as you scroll to it, not Default's staged multi-second
    `AnimatedVisibility` reveal sequence transplanted verbatim. Titles, stat counters
    (`AnimatedPlaytimeCounter`), and the `VibeShapeDrawing` flourish keep their own existing
    animations — typewriter is for prose specifically, not every element.
- **Shinobi → Book, Space Opera → Terminal** (2026-08-14) — both reuse an existing template
  wholesale instead of new page classes: one line in `GenreReviewTemplateMapping.kt` each.
  `BookBackground` reads `LocalSagaGenre` and swaps its grain texture for scattered washi-paper
  fiber strands when the genre is Shinobi (same `surfaceContainer`/`onSurface` theme-color base,
  just a different procedural pattern). `TerminalBackground` does the same for Space Opera: skips
  its own manual scanline grid and layers `Modifier.spaceVoyage(true)` on top instead — the
  genre's own existing CRT/VHS VFX (phosphor glow, jitter, scanlines, interference; already used
  everywhere `genreVfx(genre)` applies for Space Opera) — so it reads as a ship console panel
  instead of a borrowed Cyberpunk terminal.
- **Crime → new `CRIME` template, `ChatScroll` navigation style** (2026-08-14) — a simulated text
  thread, not a reskin of Book/Terminal. `ReviewNavigationStyle.ChatScroll` (`SagaReview.kt`'s
  `ChatScrollReviewContainer`) is a genuinely different mechanic from `ContinuousScroll`: a plain
  `LazyColumn` (no autoplay drift), a `revealedCount` state that ticks up over time, and
  `animateScrollToItem(revealedCount - 1)` forced on every tick so the view always snaps to the
  newest message. Manual scroll-up to reread still works; the next reveal just pulls back down.
  - List granularity changed from "one item per review stage" (Book/Terminal) to "one item per
    chat bubble" — `CrimeReviewExperience` flattens every stage's hook/content `ReviewText` into
    its own `CrimeTextMessagePage`, alternating sides on a single toggle threaded through the
    *whole* thread (not reset per stage), so it reads as one continuous conversation rather than N
    separate exchanges.
  - No avatars on the alternating bubbles by design (1:1 thread, not group chat) — Farewells is
    the one exception, since each message there already has a real `characterId` attribution.
  - Character portraits / chapter stills are `CrimeAttachmentMessagePage`s emitted immediately
    after their related content bubble, inheriting that bubble's same side — reads as "sent a
    photo right after the text," not a random insert.
  - Both bubble page classes pop in (scale 0.6→1 with `EaseOutBack` overshoot + fade, transform
    origin at the bubble's own tail corner) on first composition via a local
    `LaunchedEffect(Unit) { visible = true }` — since items only enter the LazyColumn when
    `revealedCount` includes them, "first composition" and "just revealed" are the same moment, no
    extra plumbing needed.
  - Reuses `Genre.bubble()`'s existing `CurvedChatBubbleShape` for Crime — the *same* shape the
    real in-game chat already renders — rather than a new bubble shape.
  - `CrimeBackground` is `MaterialTheme.colorScheme.background` (adapts to theme) with a soft
    corner wash in `compiledColorPalette().first()` at low alpha, not a hardcoded noir palette.
- **Crime chat pacing + data parity retrofit** (2026-08-15) — Crime shipped covering every stage's
  *text*, but (same class of gap Book had before its own retrofit) silently dropped every
  data-driven visual Default shows: playtime, the most-present emotional vibe, the chapter-still
  grid, and the cast roster with per-character message counts. Closed:
  - `ReviewPage.estimatedRevealDurationMs: Long get() = 0L` added to the interface — a page's own
    opinion of how long its entrance animation takes to settle, `0` meaning "no opinion." Only
    `ChatScroll` reads it (`ChatScrollReviewContainer`'s `delayBeforeReveal`): waits for the prior
    message's real typing duration (or 800ms if the page doesn't report one) plus a fixed 2s pause
    before revealing the next, replacing the old flat 1600ms tick that let a new bubble pop in
    mid-typewrite of the previous one. `Restart`/`Regenerate` now also bump a `restartTrigger` key
    so the reveal `LaunchedEffect` actually restarts (state alone doesn't retrigger a coroutine
    that already ran to completion).
  - `CrimeTitleCardPage` (new) — opens the thread before any bubble, centered `HandwrittenText` of
    the saga's title, the same beat of charm `BookCoverPage` gives the continuous-scroll journal;
    Crime has no in-thread cover art to fade in, so this stands in for it.
  - Stage visuals now ride along as "attachments" — a page emitted right after that stage's content
    bubble, same side/turn, like sending a photo right after a text (mirrors how
    `CrimeAttachmentMessagePage` already worked for chapter/character photos):
    `CrimeVibeStatPage` (`VibeShapeDrawing` + emotional-tone title, card-sized not full-page),
    `CrimePlaystyleStatPage` (`AnimatedPlaytimeCounter`, same stat Book/Default show).
  - `CrimeContactCardMessagePage` (new) — the cast, sent as a "shared group link" card: collapsed
    to an overlapping 3-avatar stack + count (`saga_detail_section_subtitle_characters`), tapping
    expands inline into the real list (avatar + name + `messages_count_label` per character) —
    matches how a messaging app actually surfaces a group's membership, not a roster page.
  - `CrimeAlbumMessagePage` (new) — chapter stills as a photo-stack attachment: collapsed to 2-3
    overlapping, slightly rotated frames; tapping expands into the same 2-column grid
    `BookJourneyPlatePage` uses on parchment. Both this and the contact card share one new
    expand/collapse idiom: a local `expanded` `remember { mutableStateOf(false) }`, a
    `round_arrow_forward_ios_24` chevron rotating 0→90° (`FAQCard.kt`'s existing pattern, not a new
    one), and `review_tap_to_expand`/`review_tap_to_collapse` string resources (both locales).
  - `CrimeBubbleFrame` (new, `CrimeBubbleFrame.kt`) — extracted the Row/AnimatedVisibility pop-in/
    shape/avatar chrome shared by every bubble type (text, single-photo attachment, and now the
    four new attachment types) so each page class only supplies its inner content. Two real bugs
    fixed during the extraction, not just refactor-noise: the bubble `Column` had been sized with
    `Modifier.weight(1f)` (forces the bubble to stretch to the *entire* remaining row width
    regardless of text length — wrong for a chat bubble) instead of `widthIn(max = maxWidth)`
    (caps at 280dp, still wraps to actual content otherwise); and the declared `contentColor`
    param was never applied, so bubble text read in the screen's ambient `LocalContentColor`
    instead of the color chosen to contrast that bubble's own background.
  - `CrimeBubbleFrame`'s content `Column` also gained
    `Modifier.animateContentSize(tween(450, FastOutSlowInEasing))` — the typewriter reveal grows
    the bubble's measured content every frame; without this the bubble snapped to each new size
    instead of visibly keeping pace with the text.
- **Crime bubble chrome pass** (2026-08-15) — `CrimeBubbleFrame` revised again after review:
  - `contentColor` is now handed to `content` as an explicit lambda parameter
    (`content: @Composable ColumnScope.(contentColor: Color) -> Unit`) instead of a
    `CompositionLocalProvider(LocalContentColor provides contentColor)` wrap — matches
    `ChatBubble`'s own convention of deriving `textColor` from the same `isUser`/`isMe` boolean and
    passing it explicitly, rather than an ambient override. Every Crime page's content lambda now
    takes that color as its first param instead of reading `LocalContentColor.current`.
  - No more fixed `widthIn(max = 280.dp)`. `CrimeBubbleFrame` instead reserves a margin
    (`reservedMargin`, default 56.dp) on the side opposite the bubble via asymmetric `Modifier.padding`
    on the outer `Row` — the same mechanism `ChatBubble` uses
    (`Modifier.weight(1f).padding(end = 50.dp)`) — so the available width scales with the actual
    screen instead of a dp constant that would either waste space on a tablet or do nothing on a
    small phone. `CRIME_AVATAR_SLOT`/`CRIME_BUBBLE_RESERVED_MARGIN`/`CRIME_BUBBLE_HORIZONTAL_PADDING`/
    `CRIME_BUBBLE_ROW_PADDING` exposed as `internal val`s so `CrimeTextMessagePage`'s block splitter
    can approximate the same real width without duplicating magic numbers.
  - `CrimeBubbleFrame` gained `showTail`/`showAvatar` (both default `true`) and `canAnimate` (default
    `true`) params:
    - `showTail`/`showAvatar` exist for message-block splitting (next bullet) — only the last block
      of a split message draws the bubble tail or the avatar; earlier blocks render a blank
      `Spacer(Modifier.size(CRIME_AVATAR_SLOT))` in the avatar's place so every block still lines up
      on the same left edge instead of the ones without an avatar sitting closer to the margin.
    - `canAnimate` gates the pop-in (`scaleIn`/`fadeIn`) itself. Previously the pop-in was
      unconditional (`remember { mutableStateOf(false) }` + `LaunchedEffect(Unit)`), so a bubble that
      scrolled far enough to be dropped from the `LazyColumn`'s composition and later scrolled back
      into view composed fresh and replayed its entrance — several at once if the reader scrolled
      back past a run of them. Fixed at the source in `ChatScrollReviewContainer`
      (`SagaReview.kt`): a `remember { mutableStateSetOf<Int>() }` of indices that have ever been
      composed once now drives `canAnimate = !alreadyAnimated` per item (was hardcoded `true`), so a
      re-entering item renders already-settled instead of retyping/re-popping. The two shared stat
      components Crime's attachments reuse had no skip-animation path at all and needed one:
      `VibeShapeDrawing` gained `isAnimated: Boolean = true` (snaps `Animatable` straight to `1f`
      when false instead of animating), `AnimatedPlaytimeCounter` gained the same (`tween` duration
      collapses to `0` when false). `HandwrittenText` already had `isAnimated` and needed no change.
  - `CrimeTextMessagePage` now replicates `ChatBubble`'s message-block splitting instead of always
    rendering one bubble per stage line: reuses
    `com.ilustris.sagai.features.saga.chat.ui.components.rememberMessageBlocks` (the *same* splitter
    the real in-game chat uses — a public, plain-parameter utility with no dependency on
    `MessageContent`/reactions/audio, so reusing it cost nothing) inside a `BoxWithConstraints` to
    measure the real available width, then reveals blocks one at a time via its own internal
    `revealedBlocks` state + `LaunchedEffect`, each in its own `CrimeBubbleFrame` call — still one
    `ReviewPage` / one `LazyColumn` item, the split is purely visual exactly like
    `MessageBlockSplitter`'s own doc comment describes for the real chat. Typing budget
    (`typingDurationMs`) divides across blocks proportionally to length, floored at 400ms per block
    (mirrors `ChatBubble`'s `MIN_BLOCK_DURATION`). `estimatedRevealDurationMs` stays the page-level
    approximation (`320L + typingDurationMs`) since that property has no `@Composable` context to
    actually run the splitter in — acceptable because most review paragraphs are short enough to
    stay a single block anyway (the splitter's own fast path skips measurement under ~5 lines).

## Not started yet — backlog, two genres at a time

- **Cowboy** — vertical scroll styled like movie end credits: heterogeneous blocks (centered
  title card, photo-left/text-right, rotated "attachment" photos, cast list). Only borrows the
  *idea* of continuous auto-scroll from `AnimatedChapterGridBackground` (`ui/animations/`) — its
  uniform image grid doesn't fit; content needs its own heterogeneous-block template. Possibly
  shares the continuous-scroll navigation style with Crime.
- **Horror** — pages framed as forensic evidence photos (Polaroid/evidence-tag layout), camera-
  flash transition between pages. This is where a proper **image-availability validation +
  "lost media/corrupted" placeholder** belongs (it's a deliberate stylistic choice here, not a
  generic fallback) — build it here, consider back-porting to earlier templates. Also wanted:
  Post-it-note styling for message quotes.
- **Punk Rock** — stop-motion collage. MLKit subject segmentation is **already a dependency**
  (`core/segmentation/ImageSegmentationHelper.kt`, already used elsewhere in the app —
  `processImage(url)` returns `(original, transparentCutoutBitmap)`) — turns character/chapter
  photos into "stickers", no new ML integration needed. Images jump with no easing every ~3s for
  the stop-motion feel. Still a pager — each page only needs ~4-5 stickers (a couple characters
  in the corners + one central element), not the whole saga's images at once, so the pager's lazy
  composition already bounds the work. Each sticker should use its own
  `hiltViewModel(key = imageUrl)` — mirror `DepthLayoutViewModel`'s pattern but **with an
  explicit key**; `DepthLayout` today calls `hiltViewModel()` with no key, which only works
  because it's used once per screen — several stickers on screen at once need distinct keys or
  they collide into one shared ViewModel. Distributing into keyed per-sticker ViewModels solves
  state collision and takes load off a central ViewModel, but does **not** by itself solve
  "redo segmentation every time the screen is revisited" — both a central and distributed VM
  share the same screen-scoped `ViewModelStore` lifetime; avoiding repeat MLKit runs across
  separate visits needs an actual disk-persisted cache of the segmented PNG, keyed by source URL
  — a separate decision from the VM-shape one. Avoid the `gradientFill` modifier for any
  stop-motion text effect — it uses `BlendMode.SrcAtop` without forcing an offscreen
  `compositingStrategy`, which blends against whatever's behind the text instead of just the
  glyph shapes; use solid/shadowed text (`StrokedText`-style) instead.
- **Space Opera** — shipped a baseline (see above: Terminal template + `spaceVoyage` panel), not
  this original vision. Still on the table as a future upgrade: opening-only Star Wars-style crawl
  (tilted, receding text scrolling upward — same `graphicsLayer`/`cameraDistance` recipe as Book's
  page-flip, just `rotationX` instead of `rotationY`, continuous scroll instead of discrete flip),
  over a starfield (`ui/animations/ConstellationCanvas.kt` already draws twinkling stars, reusable
  without its `chapterClusters` param). After the opening, the rest of the stages would switch to
  a sci-fi HUD/"transmission received" framing around photos (console frame + scanline) — real
  movies cut from the crawl to normal scenes rather than crawling throughout, and this mirrors
  that. Would need its own template (not just a `TerminalBackground` branch) if pursued.
- **Heroes** — motion comic: swipe drives a camera pan/zoom across static panel art, styled with
  a comic-panel border. The camera-coordination problem has a proven precedent already in this
  app: `features/brain/ui/components/BrainCanvas.kt`'s focus-follow mechanism — three
  `Animatable` values (scale, offsetX, offsetY) driving one `graphicsLayer` "camera" over static
  content, animated together (`coroutineScope { launch { animateTo(...) } }` ×3, ~1.4s
  `FastOutSlowInEasing`) whenever the focused target changes. For Heroes, each story beat defines
  an ordered list of camera "shots" over one panel image; swipe advances the shot index and the
  same 3-`Animatable` fly-to pattern moves the camera there — not a literal reuse of
  `BrainCanvas` (it's tied to the knowledge graph), but the exact same technique.

## Gotchas worth not re-discovering

- `Chapter.coverImage: String` is the actual chapter cover image URL. `Chapter.artwork: String?`
  is a *text description* of how the AI envisions the chapter's art, not an image — easy to
  confuse, already got mixed up once this session.
- This sandbox's network policy blocks `dl.google.com`, so `./gradlew` can't resolve the Android
  Gradle Plugin here — no full build/test run was possible from this environment; verification
  was careful manual review. Build/test locally or in CI.
- When merging `origin/develop` into this branch, `SagaDatabase`'s Room `version` and
  `DatabaseMigrations.kt` collided with develop's own independent migrations (both happened to
  land on `MIGRATION_25_26`). Resolved by renumbering the Farewells migration to
  `MIGRATION_27_28` (version 28) — if you pull `develop` again and get another migration
  conflict, same fix: keep develop's migration numbers, append ours after.
