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
  `TERMINAL`, `BOOK`, one per shared visual style. Multiple `Genre`s can point at the same
  template.
- `Genre.reviewTemplate()` (`GenreReviewTemplateMapping.kt`) maps each genre to a template;
  unmapped genres fall back to `DEFAULT`.
- `ReviewExperienceFactory` (`ReviewExperienceFactory.kt`) resolves `Genre -> ReviewExperience`,
  with a `bespokeByGenre` escape hatch for a genre that later needs a fully custom experience
  instead of a shared template — additive, no interface changes needed when that day comes.
- `ReviewExperience.navigationStyle` (`ReviewExperience.kt`) — `VerticalSwipe` (today's default
  Stories-style pager), `TapToAdvance` (Terminal), `HorizontalPageFlip` (Book). `SagaReview.kt`
  picks the right container composable (`DefaultReviewContainer`/`TerminalReviewContainer`/
  `BookReviewContainer`) based on this — all three still speak the same `ReviewAction` contract,
  so `SagaReviewViewModel`/`ReviewGenerationCoordinator` never needed to change.
- Each template's pages live under `features/saga/detail/review/ui/templates/{terminal,book}/`.

## Shipped

- **Cyberpunk → Terminal**: monospace, self-contained CRT scanline `Canvas` (not the
  Remote-Config-gated shader pipeline — works identically on every device/API level), tap
  anywhere to advance instead of swipe.
- **Fantasy → Book**: serif on parchment, 3D page-flip via `HorizontalPager` + `graphicsLayer`
  (same recipe the real `BookReader.kt` already uses for reading Acts).
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

## Not started yet — backlog, two genres at a time

- **Crime** — SagaReview as an iMessage conversation; bubbles + "attachments" for photos. Needs a
  new continuous-scroll `ReviewNavigationStyle` (not a discrete pager).
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
- **Space Opera** — opening-only Star Wars-style crawl (tilted, receding text scrolling upward —
  same `graphicsLayer`/`cameraDistance` recipe as Book's page-flip, just `rotationX` instead of
  `rotationY`, continuous scroll instead of discrete flip), over a starfield
  (`ui/animations/ConstellationCanvas.kt` already draws twinkling stars, reusable without its
  `chapterClusters` param). After the opening, the rest of the stages switch to a sci-fi
  HUD/"transmission received" framing around photos (console frame + scanline) — real movies cut
  from the crawl to normal scenes rather than crawling throughout, and this mirrors that.
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
