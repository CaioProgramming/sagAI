# App icon: a middle ground between the Gota head and the Duolingo-style dragon

Status: exploring (2026-10-31). Nothing here touches the shipped icon, the mipmaps or the manifest.

## Rounds 9 and 10 (current): genre variants, one accessory at a time

- **Round 9 (`generate_genres.py`):** skin = the genre's Remote Config primary colour (v1113 snapshot, not re-fetched);
  light mode lightens it, dark mode darkens it; eyes dark in light mode and white in dark mode.
- **Order:** go one genre at a time, each a single object from that world worn over the face, simple, full zoom, no
  shadow. Space Opera (antennae with stars) and Punk Rock (mohawk) come last, because their accessories grow out of the
  silhouette, which no longer exists. The shipped icons stay the reference for the idea of each genre.
- **Round 20, export and swap in the app (`generate_export.py`, `export_preview.png`, uncommitted):** set adjustments first:
  Horror's dark skin is lighter, the Cowboy bandana rises to just under the eyes with two paisley rows, and the Space Opera
  shell is a closed grey in dark mode. Colours were checked against the live Remote Config and match. 70 layers replaced
  under the same names in `app/src/{main,debug}/res/drawable-nodpi` (no manifest or code change), the debug figures hatched
  over the unchanged grid; iOS PNGs are in `export/ios/`. `:app:processDebugResources` passes; not seen on a real launcher.
- **Round 19, mono and blueprint review (`generate_mono.py`, `mono_preview.png`):** each design redrawn as a
  one-tint mask, the full-colour icons cropped to a circle, and a draft of the debug/hmg blueprint. Today's debug build
  shares one blue grid ground across genres (light and dark) and draws the icon as a pale translucent figure with a white
  outline and hatched accessory; the solid draft hides that grid on Fantasy, Shinobi, Heroes and Space Opera, so the real
  version needs that translucent and hatched treatment.
- **Round 18, Punk Rock v5 (`generate_punk.py`, APPROVED 2026-11-01 with piercing A, the horns):** the user sketched softer curves over v3. The lid and the top
  of the liner now run in an S, the outer side curves deeper, the eyes are hand-drawn with pointed corners (not the traced
  base eye), and the ring became a steel septum piece without an outline. Two piercings from the user's references are compared: A,
  thin horns curving in from the nostrils (`svg/punk_rock_*`), and B, a thin ring with cones pointing down
  (`svg/punk_rock_ring_*`). A small four-pointed star past each liner's inner end closes the make-up. v3 (`svg/punk_rock_v3_*`) was the
  first continuous liner, one closed shape round each eye; v2 (`svg/punk_rock_v2_*`) used separate strokes. Earlier state: no mohawk, the user chose piercings plus punk make-up and
  furious eyes. The base eyes in white, cut by a lid that drops towards the nose, and a steel septum ring, on the #729656
  skin. v2 follows the user's graphic eyeliner reference: a long tapered wing on the upper lid, a thin line under the eye
  and six fine spikes running down, one long. v1 (`svg/punk_rock_v1_*`) had a black spiked patch and a safety pin; the pin
  was removed as excess.
- **Round 17, Space Opera (`generate_space.py`, option B, APPROVED 2026-11-01):** the shipped antennae do not fit
  a full-zoom tile, so three options were compared in `space_preview.png`: A = VR visor with an opaque nebula glass, B =
  astronaut helmet, C = a spiral galaxy inside each eye. The user picked B: white shell, grey rim, chin vents,
  the whole face on the #0081A7 skin behind the glass, in `svg/space_opera_b_*`. APPROVED 2026-11-01 with a plain forehead: the
  user rejected the cyan stripe, tried the app's spark (`ic_spark.xml`) in its place, and removed it too. Then added a subtle sky reflection on the
  glass at 50% (nebula and stars from A), with the stars kept off the middle of the face. No reflection:
  `svg/space_opera_b_v1_*`. A and C stay as `svg/space_opera_{a,c}_*`.
- **Round 16, Heroes v4 (`generate_heroes.py`, APPROVED 2026-11-01):** a black eye mask on the #2845D6 skin, with the base eyes
  squeezed to 60% height as white slits tilted 9 degrees behind it. Went through a Robin/Nightwing domino with a rim
  (v1), a spiked curvy mask (v2) and a plain Zorro band (v3); the user asked for something between Zorro and
  Nightwing. Final: full-width band, soft dip at the brow, concave swoops up at the outer ends of the top edge (the
  user's sketch), lower edge lifted over the nose bridge so both nostrils stay clear, no rim. Saved:
  `svg/heroes_v1_*`, `v2_*`, `v3_zorro_*`. Dark mode: the mask's lower edge blends into the dark blue skin; not
  checked at 48px.
- **Round 15, Cowboy v3 (`generate_cowboy.py`, APPROVED 2026-11-01):** a bandana over the lower face (hides the nostrils, like
  the Shinobi mask) on the #8B4513 skin. v1 used an off-palette red and cream polka dots (clashed); a dusty pink option
  was worse. Final: terracotta from the genre palette (#D4612F light, #A84520 dark, hem #8B3A0F) with a warm-white
  print of one row of paisleys plus a dotted border along the hem; the user found the polka dots childish. Eyes white
  (base). Saved: `svg/cowboy_v1_*` red dots, `svg/cowboy_v2_dots_*` terracotta dots. Paisleys read a bit like snails;
  not checked at 48px.
- **Round 14, Shinobi v2 (`generate_shinobi.py`, APPROVED 2026-11-01):** the v1 headband with a swirl plate was dropped
  (the user did not want the Naruto look). Final: a plain ninja hood and mask. Charcoal cloth covers the top (lower edge
  slanting down to the nose) and the bottom (hides the nostrils), leaving only a strip of the #8B0000 skin with the white
  base eyes. Dashed tone-on-tone seam along each edge; mask folds were tried and removed (read as cat whiskers).
  v1 saved as `svg/shinobi_v1_*.svg`. Not checked at 48px; only the eye strip carries the theme colour.
- **Round 13, Horror v6 (APPROVED 2026-11-01):** teeth, then the jaw contour, were both dropped as odd. Final: deep
  sockets and nostrils like the skull reference (ink outline, mid-grey wall, black hollow pushed up so a grey crescent
  shows under it; no white eye), plus the forehead crack. v2 teeth, v3 jaw, v4 white-eye version saved as
  `svg/horror_v{2,3,4}_*.svg`. Not checked at 48px.
- **Round 13, Horror v3 (superseded):** the user found the v2 teeth crude too and asked for contours that
  only simulate the mandible (refs: dragon skull photo and ink drawing). v3 drops the dark mouth and teeth: a wide jaw
  line across the bottom, a shorter chin curve under it and a short hinge stroke at each side corner, all thin ink
  strokes like the crack (light mode near-black ink, dark mode light cool grey, since ink vanished on the dark skin).
  Eyes, nostrils and crack unchanged. v2 saved as `svg/horror_v2_*.svg`.
- **Round 13, Horror v2 (superseded by v3):** the user found the rising fangs crude, asked for the white eye at 90%
  of its socket (a cavity rather than an eye) and for a more pointed almond. v2: the eyes are no longer the traced drop but
  a hand-drawn pointed almond (a point at both ends along the same diagonal), socket in black and the white eye at 90%
  of it; the fangs are replaced by a skull grin: a dark mouth opening along the bottom (grin edge, higher at the corners)
  with seven upper teeth hanging from it, the two canines a little longer; the nostril cavity and the forehead crack are
  unchanged. v1 is kept as `svg/horror_v1_*.svg`. The almond is a new shape, not the traced one, and the grin is a bigger
  change than "better teeth", so both need the user's eye.
- **Round 13, Horror v1 (superseded, `generate_horror.py`, `horror_preview.html/png`):** the
  user's idea: Horror as more of a dragon skeleton. Skin is the genre primary (`#748B97`, a cold grey-blue that already
  reads as bone). Parts: eye sockets (the base eye shape, black and a little fatter, with the white eye shrunk to 70%
  inside it, so it glows from an empty socket), a nasal cavity (the nostril drops at 155% and black), five bone-white fangs
  rising from the bottom edge (two big canines, a small one between and one at each end: the fangs say "dragon" and not
  "human skull") and one angular forehead crack with a short branch, from the top edge. The shipped drips are left out.
  About 12 shapes. Open: the crack and fangs are tone on tone in the same style but the fangs are the brightest thing,
  and a round socket ring can read as outlined cartoon eyes.
- **Round 12, Cyberpunk v9 (accepted for now):** the user kept v8 and asked for the middle piece to become a triangle pointing down,
  and allowed an accent colour that goes with the purple there. v9: the trapezoid is now a downward triangle (about 96
  wide) filled amber `#FFB000` (edge `#E88A00`), the complement of the purple and the colour of the warning labels in the
  references; it is the only colour on the face, everything else stays tone on tone. The horizontal line stops at the
  triangle. v8 is kept as `svg/cyberpunk_v8_*.svg`. Open: the triangle is small, it reads from 96px up.
- **Round 12, Cyberpunk v8 (superseded):** the user said the nose bridge can be simpler ("simplicity is everything"), without
  going up vertically: just one horizontal line crossing the nose area. v8: the vertical Y trace is gone; a single
  horizontal line runs between the eyes and behind the trapezoid (the plate hides the middle), at the height of the
  plate. Total face marks: the two eye traces, the trapezoid and one horizontal line. v7 is kept as
  `svg/cyberpunk_v7_*.svg`.
- **Round 12, Cyberpunk v7 (superseded):** the nose bridge follows the same idea as the eye traces. The two flanking dashes
  are gone; a vertical trace runs behind the trapezoid, emerging above it and splitting into two diagonals that end in
  short stubs, and the same below (inverted), the plate hiding the middle. Tone on tone, no cyan. v6 is kept as
  `svg/cyberpunk_v6_*.svg`. Open: everything is symmetrical, so it still reads closer to a mask than to a single implant.
- **Round 12, Cyberpunk v6 (superseded):** the user liked v5's reading and asked for the line above the eye as well, for
  continuity and an implant look. v6: each eye has one single angular trace that runs BEHIND the eye (the eye is drawn on
  top): it emerges above the eye, bends on a diagonal outwards and ends in a short stub, and does the same below, with
  the two halves mirrored about the middle of the eye (so it reads like a bracket framing the eye). The nose bridge
  trapezoid and dashes are unchanged, tone on tone, no cyan. v5 is kept as `svg/cyberpunk_v5_*.svg`.
- **Round 12, Cyberpunk v5 (superseded):** the user said v4 misread the eye lines and sent three more references (a CP2077
  face with a thin line from under the eye and tick marks round the nose; a face with fine panel lines drawn down the
  cheeks from the inner eye corner; a face with angular seams that start at the eye corners and run across the cheek).
  The point: the lines LEAVE the eye and run over the skin, in angular bends like a circuit trace; they never cut through
  the eye. v5: under each eye a thin line starts at the eye's lower edge (hidden behind the eye), goes straight down, bends
  on a diagonal outwards and ends in a short horizontal stub; mirrored left and right; the nose-bridge trapezoid and its
  two dashes are kept; tone on tone, no cyan. v4 is kept as `svg/cyberpunk_v4_*.svg`. Open: the lines are short next to the
  references, where they run along the cheekbone and often wrap the eye socket; asymmetry (a line on one side only) might
  read more like an implant.
- **Round 12, Cyberpunk v4 (superseded):** the user disliked the blue and pink cheek piece and doubted the cyan ("does it
  not draw too much attention?"); they liked the lines that cross the eyes and the marks on the nose bridge. v4 keeps
  only those, tone on tone, no cyan and no magenta anywhere: one thin line per eye, running from above through the eye
  down onto the cheek and slanted outwards (dark ink where it crosses the white of the eye, skin-tone line elsewhere), the
  trapezoid on the nose bridge in a skin-tone outline with a faint fill, and two short dashes flanking it. The forehead
  dashes, the cheek elbow and the nostril-side dashes are gone. Light mode: darker purple; dark mode: lighter lilac.
  v3 is kept as `svg/cyberpunk_v3_*.svg`. Open: the eye lines are long and read a little like claw marks.
- **Round 12, Cyberpunk v3 (superseded):** the user said v2 tried too hard to turn the face into a robot and asked for only
  small strokes on regions of the face, like an implant in the skin (refs: Cyberpunk 2077 faces with tiny dashes, a thin
  line from under one eye down the cheek, a small trapezoid on the nose bridge). v3 has four short dashes (two on the
  forehead, two by the nose, mirrored), a cyan trapezoid on the nose bridge, and on the right cheek only a thin line
  from under the eye with an elbow, ending in a magenta LED dot. No forehead plate, screws, label, glowing eye or
  jaw seam. Light mode dark purple strokes, dark mode cyan strokes. About 9 shapes. v2 is kept as `svg/cyberpunk_v2_*.svg`.
  The first draft of the cheek piece read as a small bucket, so it is a plain elbow line now.
- **Round 12, Cyberpunk v2 (superseded, `generate_cyberpunk.py`, `cyberpunk_preview.html/png`):**
  the user set the direction (refs: "cyber mode" posters with plate seams and screws on the face, anime stills with a
  glowing mechanical eye): not a prop but a face that is a total part replacement, drawn INTO the skin. v1 closed the
  seams into an oval round the lower face and read as a robot-head outline with a plate stuck on top; v2 has a forehead
  plate that bleeds off the tile top with a V notch toward the nose, a stepped seam from each lower plate corner round
  the outer side of the eye (no ring), a bridge line between the eyes, the right eye lit cyan (the other stays white),
  four screws, a magenta warning label and four cyan/magenta LED dots. Light mode: dark purple seams; dark mode: cyan
  seams. v1 is kept as `svg/cyberpunk_v1_*.svg`. About 15 shapes. Open: the seams still arch like a hood; no jaw plate yet.
- **Round 11, Crime v6 (approved):** the user was impressed by v5 and sent a more natural palm tree outline
  (`reference/palm-tree-svgrepo-com.svg`, from SVG Repo) to replace the hand-drawn one: a leaning trunk and a crown of
  drooping fronds, scaled to about 240 units and mirrored in the right lens. Its licence is not recorded in the file:
  check it on SVG Repo before it ships. v5 is kept as `svg/crime_v5_*.svg`.
- **Round 11, Crime v5 (superseded):** the user liked v4's improvement but said Crime's aesthetic is Miami Vice and Vice City,
  so the glasses should be "more beachy". v5: a chunky white retro frame with a heavy browline, lenses with a sunset
  gradient (deep violet, hot pink, orange), a palm tree silhouette reflected in each lens and one bright streak; the
  slanted opaque lens shape from v4 is kept. The white frame also fixes v4's low contrast in dark mode. v4 is kept as
  `svg/crime_v4_*.svg`, v3 as `svg/crime_v3_*.svg`. Open: the top of the tile is still empty skin.
- **Round 11, Crime v4 (superseded, option A):** the user said v3 still did not fit; the diagnosis was a sticker look (thin
  gold wire), lenses unrelated to the eyes' angry slant, a busy palette and a top 40% left empty. v4: thick near-black
  frame (`#150B24`) with a thin cold rim light, lenses slanted like the eyes (high at the outer corner, low at the
  nose), opaque glass that hides the eyes, one bright reflection streak plus a pink glow at the bottom, one thick
  bridge and short thick arms, at the eyes' own height. No gold. v3 is kept as `svg/crime_v3_*.svg`. Open: the top of
  the tile is still empty skin, and in dark mode the near-black frame has low contrast with the skin.
- **Round 11, Crime v3 (superseded):** the user found only the scale of the glasses odd. They are now at 92% of the first
  size (an 85% try made the eye tops stick out around the outer corners like ears), a touch lower (98 units), with a
  slightly thicker frame to keep its weight. The arms are longer so they still reach the tile edge.
- **Round 11, Crime v2 (superseded):** the user dropped the see-through idea. The lenses are now opaque, so the eyes are
  hidden, and the glasses sit lower (105 units), as if slid down the nose, so the tops of the eyes peek out above the
  frame. The glass has gradient reflections: two diagonal bright streaks and a neon pink tint pooling at the bottom of
  each lens. The inner edge of the lens was pulled in so the nostrils stay clear. The text below is the first pass.
- **Round 11, Crime v1 (superseded, `generate_crime.py`, `crime_preview.html/png`):** aviator sunglasses over the eyes, one
  object: a thin champagne-gold frame (own colour, not the theme's), dark purple glass lenses at 62% opacity so the white
  eyes stay as dim shapes behind them (at 86% they vanished), a double bridge and a temple arm to each side of the tile,
  plus a diagonal reflection band on each lens. The lens drop is long on the inner side, the aviator teardrop. About 10
  shapes. Eyes white in both modes, as in Fantasy v4. Not yet reviewed by the user.
- **Round 10, Fantasy v4 (approved):** v3 approved, with two changes. The eyes are white in light
  and dark mode (they sit under the dark helm in both). The cube became a plumbob (a tall octahedron: two pyramids base
  to base, the front vertex a little below the middle), four facets each with its own gradient plus a thin glint on the
  upper left edge, still in the Fantasy palette's reds on a dark setting. The visor lines now start at the plumbob's
  lower tip. v3 (cube) is kept as `svg/fantasy_v3_*.svg`.
- **Round 10, Fantasy v3 (superseded):** after v2 the user dropped the crown and horns, kept the
  pointed visor with the red stone, and asked for it to be sharper, the metal darker and more sombre with gradients that
  imitate reflections, and a prism instead of a round gem. v3: the plate covers the whole top of the tile (as v1), near
  black gunmetal in light mode and a muted steel in dark mode, with two diagonal bands of light and a left half a touch
  lighter (planes); a long needle-like visor between the eyes; a faceted red prism (light top, mid left, deep right face)
  on a dark setting; thin cold edge lines and two facet lines. No gold, no rivets. v1 and v2 are kept as
  `svg/fantasy_v1_*.svg` and `svg/fantasy_v2_*.svg`. The prism reads as a small cube at 96px and up.
- **Round 10, Fantasy v2 (superseded):** the user found the classic helm right in idea but predictable
  and asked for something sinister and gothic (refs: a dark armoured batman-style helm and a spiked crown-like mask with a
  round gem). v2 is a dark gunmetal helm with a crown outline (tall centre spike, two thin curved horns, skin showing
  above it), a brim that slants down to the middle with a pointed visor between the eyes, a round red gem on the
  forehead (the Fantasy palette's `#D44D63`), and three cold edge-lit lines (a ridge and two facets). Gold trim and
  rivets are gone. The classic helm is kept as `svg/fantasy_v1_*.svg` for comparison. About 11 shapes. The horn tips
  touch the tile's rounded corners, so launcher masks that crop harder will clip them.
- **Round 10, Fantasy v1 (superseded):** a steel helm over the top half of the tile, in its
  own colour (not the theme's) so it reads as an object. The brim is a gold trim with a nasal guard dropping between the
  eyes, plus a ridge down the middle and four gold rivets. The eyes are painted first and the helm over them, so the brim
  cuts their tops and they read as seen from under the helm. The shipped horns are left out: they would sit past the tile
  edge. About 11 shapes (2 eyes, 2 nostrils, plate, trim, ridge, 4 rivets).

## Round 8 (superseded by round 9): no mouth, no muzzle, only the two nostril dashes

**Newest:** the nostrils now echo the eyes: two small drops (round at the outer top, a point at the inner bottom,
about 58px wide) in place of the dashes, same tones. The text below is the dash version.

**Previous call:** the chevron muzzle is gone too. The face is the 2 shipped eyes plus two short slanted dashes for the
nostrils (20px round-capped strokes, `#0B0719` in light mode, `#6A3FD0` in dark mode, mono layer black). 5 shapes, no
mouth, no muzzle, no overlay. Light mode is very faint at 96px and below because the skin is near black. The notes below
describe versions that were replaced.

### Replaced: one darker chevron line as the muzzle

**Final call of round 8:** the lighter overlay made no sense; the muzzle is only a short darker line, a chevron that
points down (17px stroke, `#0B0719` in light mode, `#6A3FD0` in dark mode), with the two nostrils above it in the same
tone. Still 5 shapes. Light mode is faint because the skin is already near black; dark mode reads at 96px and up. The
mono layer carries the line and the nostrils. The paragraph below describes the overlay version that was replaced.

### Replaced: the shield-shaped light muzzle

The user's call on round 7: drop the mouth entirely, and keep the muzzle but make it stronger and pointing down.
`generate_flat.py` now makes two versions, **Só os olhos** (3 shapes) and **Com focinho** (5 shapes): the eyes, a soft
muzzle shaped like a shield (round at the top, tapering to a point at the chin) and the pair of nostrils. The muzzle is a
vertical lift of light with no outline (white at 17% fading to 5% in light mode, 34% to 10% in dark mode), about twice as
strong as round 7's. No smile and no smile ticks anywhere. Adaptive: plain gradient background, and the muzzle, nostrils
and eyes in the foreground at 80% inside the 66dp circle (so the muzzle scales with the face); the mono layer is the eyes
and nostrils, because a muzzle is only a tone and does not survive a single colour. Round 7's Leve and Completo outputs
were deleted; the round 7 text below stays as the record. Still open and unverified: the sharper eye points, a real
launcher, themed tinting, the iOS exports.

## Round 7 (superseded by round 8): the small details of the face on the round 6 base

The base is approved (gradient skin, eyes at the shipped height, nothing else); now the small face details, kept in the
shipped icon's language: tone on tone, no outlines, and still no pupils, horns or whiskers. Source: `generate_flat.py`;
look: [`flat_preview.png`](../prototypes/icon-middle-ground/flat_preview.png) with the current icon, the eyes-only base and
two levels of detail, light and dark, then 144 to 48px and the adaptive masks; singles `png/flat_leve_*.png` and
`png/flat_completo_*.png`.

- **Leve (5 shapes):** the eyes, a pair of small slanted nostrils and a wide smile, in a tone just off the skin
  (`#45397F` nostrils and `#6657B8` smile in light mode, `#7248DE` and `#6A3FD0` in dark mode).
- **Completo (8 shapes):** Leve plus a soft muzzle (a lift of light with no edge, so it does not become a pad) and a
  small upturned tick at each end of the smile, the one detail of the old mouth worth keeping.
- Adaptive uses Completo: gradient background with the muzzle, eyes and details at 80% inside the 66dp circle; mono layer
  is the eyes, nostrils and the smile.

What shows at small sizes: the eyes always, the smile down to about 72px, the nostrils and the muzzle only at 96px and
up. That is the tone-on-tone trade; making them louder would leave the shipped icon's look. Noticed while checking: the
traced eyes end in a short flat cut at the inner point (it comes from the source art's bevel), so sharpening that point,
which the user asked for earlier, is still open. Not verified: a real launcher and themed tinting, the iOS exports, and the
dark-mode tile being the brightest on the screen.

## Round 6 (superseded by round 7, which builds on it): the ground painted in the dragon's colour, nothing else

The user stopped round 5 as too much: just make a new icon whose background is the same colour as the dragon, take
away the dragon's shadow, and add nothing (no pupils, nostrils or mouth). With ground and head the same colour the
head outline disappears and only the shipped eyes are left, zoomed about 6x, on one flat skin colour. 3 shapes, the
same count as today's icon. Source: `generate_flat.py`; look: [`flat_preview.png`](../prototypes/icon-middle-ground/flat_preview.png),
singles `png/flat_light.png` and `png/flat_dark.png`.

Two adjustments from the user: the eyes now sit where they do in the shipped icon (about 60% down the tile; the first
zoom had them at 43% and they read as too high), and the skin is a vertical gradient for a little texture, using the shipped
head's own colours: `#382D66` to `#1E1535` to `#0E0920` in light mode, `#E1D2FF` to `#B796FF` to `#9560FD` in dark mode
(eyes `#10081D`, the dark ground the shipped dark icon shows through them). Still no gloss, rim light or shadow.
Adaptive: the gradient as a full-bleed background, eyes at 80% inside the 66dp circle, mono layer is the two eyes.
Levers: how strong the gradient is, how large the eyes are, and sharpening the point of the eyes. Not verified: a real launcher and themed tinting, the iOS exports, and how a bright lilac tile sits in the dark
mode next to other icons. Rounds 2 to 5 below are superseded by this brief.

## Round 5 (superseded): today's icon with no head shape, the whole tile is the dragon's skin

The user's brief after rounds 3 and 4 went wrong: it is a full-zoom icon like the pumpkin and the mummy, so there is
no silhouette any more. Take the CURRENT icon, imagine the head outline gone, and let the entire tile be the dragon's
skin. Two attempts at inventing the face (a long snout, almond eyes drawn from scratch) were rejected as losing the
aesthetic; the user's words were that this path was terrible. The rule that came out of it: do not invent shapes,
zoom into the ones the icon already has, in its own language (dark glossy skin, white leaf eyes, tone-on-tone marks).

Source: `generate_zoom.py`. Look: [`zoom_preview.png`](../prototypes/icon-middle-ground/zoom_preview.png) shows
current, A, B and the old icon side by side, light and dark, then 144 to 48px and the adaptive masks. Singles:
`png/zoomA_light.png`, `png/zoomB_light.png`, `png/zoomA_dark.png`, `png/zoomB_dark.png`.

- **Eyes:** the shipped eyes, traced from `res/drawable-nodpi/ic_dragon_fg_light.png` and scaled about 6x. They are a
  drop: round head at the outer top, a point at the inner bottom next to the nose, a straight lower edge. (An earlier
  guess had the point at the outer end; the trace showed the opposite.)
- **Skin:** the shipped head colour as the whole tile, with the same soft gloss and a rim light: dark violet in light
  mode, the lilac head in dark mode (where the eyes are dark, as in the shipped dark icon).
- **A, pure zoom:** skin plus two eyes, 3 shapes, exactly what the icon has today.
- **B, zoom plus a face:** A plus two pupils (round head of the drop), two glints (light mode only; on the light pupil of
  dark mode a glint disappears), a pair of nostrils and a smile, both tone on tone. 9 shapes in light, 7 in dark.
  The shipped dark icon already carried a small nose mark, so the nostrils are not a new idea.

A reads abstract (two eyes on a dark field) and B reads as a character. The levers if B is the way: how strong the
nostrils and smile are (they fade first at small sizes), how much sharper the point of the eye should get ("a bit more
dragon"), and dropping the glints (7 shapes). Adaptive: skin as the full-bleed background, features at 80% inside the
66dp circle, mono layer is the eyes (with the pupil cut out in B), nostrils and smile.

Not verified: a real launcher and themed tinting, the iOS exports, the colours on a real home screen, whether a
bright lilac tile suits the dark-mode icon (it is faithful to the shipped dark head but is the brightest tile on the
screen), and the eye shape is a trace that needs a human pass before it ships.

## Round 4 (superseded): the shipped silhouette plus a face, still on a ground

The user's clarification after round 3: the new icon is the CURRENT silhouette icon expanded a little, landing
between two extremes: the plain silhouette on one side and the old, fully detailed, zoomed-in Duo-style dragon on
the other. Rounds 2 and 3 went about it the wrong way, simplifying the detailed icon downward (round 2) or
inventing a new face (round 3). Round 4 starts from the shipped art and adds only enough.

The shipped silhouette and eyes are vectorised from `res/drawable-nodpi/ic_dragon_fg_light.png` (nothing redrawn
by eye), then given volume and a face. Source: `generate_middle.py`. Look: [`mid_preview.png`](../prototypes/icon-middle-ground/mid_preview.png)
shows current, middle and old side by side, light and dark, then 144 to 48px and the adaptive masks; the middle
alone is [`png/mid_light.png`](../prototypes/icon-middle-ground/png/mid_light.png) and
[`png/mid_dark.png`](../prototypes/icon-middle-ground/png/mid_dark.png).

| | Shapes | What is on it |
|---|---|---|
| Current (shipped Gota) | 3 | head silhouette, 2 eyes |
| **Middle (this round)** | **8** | head, 2 eyes, 2 pupils, 2 glints, 1 nostril pair |
| Old (Duo-style) | about 24 | crests, eyes, pupils, glints, lids, bridge ridges, snout, nostrils, mouth, ticks, chin, whiskers |

Soft shading (a forehead light, a shade toward the chin), a rim light and the glow are treatment and not counted.
What changed from the current icon: the head is 10% larger, the eyes 22% larger so they can hold a pupil, each pupil
looks a little down and inward, one glint per eye on the same side (not mirrored, as approved earlier), and a small
pair of nostrils near the chin (the shipped dark icon already had a nose mark). Light mode keeps the dark head on the
lavender ground; dark mode keeps the lilac head on near-black and swaps the hollow eyes for bright ones with a dark
pupil, an echo of the old dark icon's glowing eyes. The head never changes silhouette, so the crest, ears and chin the
user already knows stay.

Adaptive: full-bleed background, head scaled to fit inside the 66dp circle (at 1.18 the spike tips touched its
edge, so it is 1.10), mono layer is the silhouette with the eyes cut out. The dragon stays purple.

Not verified: a real launcher and themed tinting, the iOS exports, the colours on a real home screen, and whether
8 shapes is where the user wants the middle (the count is the lever: dropping the glints gives 6, adding a smile
gives 9). The genre accessories on this base are untouched. The vectorised outline is a trace and needs a pass
by a person before it ships.

## Round 3 (superseded): the base at the reference's minimalism

The user's call after round 2: the base is far too detailed. Follow the pumpkin and the mummy: a flat-colour
tile, two big eyes, at most one small feature, short horns only if the dragon would not read without them at
48px. Nothing from the old icon survives unless it is one of those few shapes.

Base: [`png/min_light.png`](../prototypes/icon-middle-ground/png/min_light.png) and
[`png/min_dark.png`](../prototypes/icon-middle-ground/png/min_dark.png); sizes, the no-horns check, the old icon
and the adaptive masks in [`min_preview.png`](../prototypes/icon-middle-ground/min_preview.png). Source:
`generate_minimal.py`.

**The face is 5 shapes:** two eyes, two short horns, one small smile. The tile's soft gradient, rim light and the
eyes' offset inner shadow are treatment, not shapes. Without the horns it is 3 shapes and reads as a robot (the
no-horns tile is in the preview, so the horns are not optional). Palette: tile `#6C4AE0` to `#5A3AC6`, eyes and
smile `#1B0A42`, horns `#CDB9FF` to `#9B7AF8`; dark mode flips the eyes and smile to lilac on a `#1E1248` tile with
the violet edge light. Adaptive: full-bleed background, face scaled to 80% inside the safe circle, mono layer is the
five shapes in one colour.

Tried and dropped inside this round: forehead horns swept hard outward (read as cat ears, so these are shorter,
fatter and nearly upright), a three-spike forehead crest (reads as a crown) and two side fins (illegible small).

Honest limit: with this budget the face reads as a small horned creature, friendly and clear at 48px, but not
unmistakably a dragon the way the old icon's crests, snout and whiskers were. That is the price of the
minimalism. If the user wants more dragon, the cheapest single shape to add back is one of the old icon's, a
snout pad with its nostrils, which would make it 6.

## Round 2 (superseded, too detailed): one solid dragon base first, variants later

The user's call after round 1: do **not** copy the pumpkin's boards or the mummy's strips, they mean nothing
for this app. Keep the reference's *structure* (the tile is the face, big eyes, soft shading, no outlines) and
spend the detail budget on what makes the dragon believable, the way Duolingo's brows and beak make its owl.
Variants (genre accessories, dark, alternate palettes) come only after the base is solid. The old Duo-style
icon is saved in [`../prototypes/icon-middle-ground/reference/`](../prototypes/icon-middle-ground/reference/)
so it is not forgotten.

Base: [`png/base_light.png`](../prototypes/icon-middle-ground/png/base_light.png) and
[`png/base_dark.png`](../prototypes/icon-middle-ground/png/base_dark.png); side by side with the old icon, small
sizes and adaptive masks in [`base_preview.png`](../prototypes/icon-middle-ground/base_preview.png). Source:
`generate_base.py`.

| Detail of the old icon | In the base | Why |
|---|---|---|
| Feathered brow crests over the eyes | Kept, the strongest cue: three tufts on the outer-top, the tallest nearest the middle, then one smooth sweep to the nose. Angles and radii measured off the old icon | It is what says dragon at a glance, like the owl's brows |
| Big eyes with pupil and glint | Kept, a bit larger, pupil nudged down-in but less than before (it read cross-eyed), two glints | The character lives in the eyes |
| Three ridges on the nose bridge | Kept as three thin arcs | Cheap, reads as scales, vanishes gracefully when small |
| Snout pad with two nostrils | Kept, pad lighter than the face so the nostrils read | The pad plus nostrils is the second dragon cue |
| Smile with cheek ticks | Kept as one smooth line (the old mouth plus chin outline became one stroke) | Fewer strokes, same warmth |
| Two long whiskers | Kept, low contrast | Big-size flourish; at 48px they fade and nothing breaks |
| Lilac eyelid, stacked highlights, chin outline | Dropped | Detail that does not survive the small sizes |
| Round 1's textures and forehead horns | Dropped | Not part of this dragon |

Colours: deep violet tile (`#472A90` to `#2E1A62`), crest `#7448C6` to `#4E2C98`, pad `#6A42BC` to `#52309E`. The
dragon stays purple; the genre only changes accessories and the dark-mode light, as already decided. Dark
mode is a backlit violet edge light with a soft glow behind the eyes.

Open questions for the user: whether the face should be brighter and more saturated than the old, duller
purple (it is, on purpose); how wide the smile and whiskers should run; and whether the crest should be
heavier. Not yet done: the genre accessories on this base, the iOS exports, a real launcher.

## Round 1 (superseded): pumpkin and mummy structure

Kept below for the record. Its recommendation (candidate A) is superseded by the round 2 direction.
Candidates and the generator live in [`../prototypes/icon-middle-ground/`](../prototypes/icon-middle-ground/)
(open `preview.png`, or `preview.html` in a browser).

## Why

The shipped Gota icon (a dark glossy dragon silhouette on a lavender ground) is minimal; the cancelled
Duolingo-style redo (2026-10-17) was too detailed for the rig and for image models. The reference
the user brought is a Halloween set (vampire, Frankenstein, pumpkin, mummy). The target is the last
two: the tile **is** the face.

## Style rules read from the reference (pumpkin and mummy)

Colours were sampled from a 1206x708 JPEG, so treat them as ±3.

- **The tile is the head.** The squircle is the character, not a mascot sitting on a background.
  Superellipse corners, roughly n=5.
- **Flat base with a very small vertical gradient.** Pumpkin `#E14E23`..`#EC4C2A`, slightly darker at the
  bottom (`#E04320`). Mummy `#D4CDB0`, bands `#E1DBBB`.
- **One texture, low contrast** (about 5% lightness): 4 to 6 vertical boards on the pumpkin, loose diagonal
  strips on the mummy. Never more than one.
- **Two big dark eyes, no more than one mouth.** Pumpkin eyes are rounded squares of about 32% of the tile width
  and 33% of its height, a gap of about 11%, vertically centred, deep plum `#2F0324` and not black, with an
  offset inner shape for depth. Mummy eyes are near-black ovals (about 29% x 36%), centres 38% apart, about
  13% below centre, with a tiny ring highlight. The pumpkin has a small smile at about 74% height (about 15% wide);
  the mummy has no mouth.
- **Shading is soft and inside the tile:** a lighter top, a darker bottom, a thin light rim on the top-left,
  no outlines, no hard shadows inside the face.
- **At most three shapes make the face.** That is what keeps it readable at 48px.

## What was built

Six SVGs plus the adaptive layers, all generated by `generate.py` (plain SVG, no dependency):

| File | What it is |
|---|---|
| `A_abobora_dragao` | Purple pumpkin-style dragon: vertical ridges (the boards), two plum rounded-square eyes with the inner shadow, a minimal smile, two short forehead horns |
| `B_mumia_dragao` | Mummy-style pale lavender dragon: faint scales (the bandages), two black oval eyes with the ring highlight, tiny nostrils, the same horns |
| `C_gota_tile` | Closest to today's icon: dark glossy tile, the two white leaf-shaped Gota eyes, a tone-on-tone crest, faint scales |
| `D_abobora_escuro` | A in dark mode, backlit with a violet edge light as the approved dark Gota was |
| `E_heroes_mascara`, `F_crime_aviador` | Genre proofs on A: one simple accessory each, the dragon stays purple (a standing decision) |
| `A_adaptive_{background,foreground,monochrome}` | Android adaptive layers for A |

The horns are the only dragon cue that survives at 48px; without them A read as a purple robot.

## Feasibility of producing it

- **Hand-written SVG (done here).** Fits this style well because it is flat shapes, gradients and one
  texture. Deterministic, free, and the nine genres are nine small functions. Rendered with headless Chrome
  (`python3 generate.py --png`).
- **Image model (Gemini / Nano Banana).** Not tried and no paid tool was called. The notes from the cancelled
  Duo attempt say results were too inconsistent and it shrank accessories and baked in rounded corners. The
  flat style is easier for a model than the Duo one, but consistency across nine genres stays the risk.
  Worth using only to sketch accessory ideas that are then drawn as SVG. Check pricing before any use.
- **Compose / AGSL.** Not for the launcher (it needs static resources). Useful only for in-app previews, such
  as the icon sheet.

Shipping format: the current pack is 432px layered PNG/WebP (`ic_dragon_<genre>_{bg,fg}_{light,dark}` plus
`_mono`, wired by `activity-alias`). The same structure can host the new set by swapping files; the
`LauncherIcon` enum and the aliases do not need to change. A native `VectorDrawable` is possible for A alone
(gradients and clip paths are supported) but has no pattern or filter, so B and C would need their scales
flattened into paths. Rasterising the SVG layers keeps the existing pipeline.

## Adaptive icon

The launcher always shows the inner 66dp circle of the 108dp canvas. The background layer is full bleed (colour
plus ridges); the foreground shrinks the face to 80% so the eyes, horns and mouth sit inside the safe circle
(the preview draws it dashed). The monochrome layer is the eyes, horns and mouth as one flat shape. This holds
for circle, squircle and square masks in the preview.

## Recommendation

**A (pumpkin)**, with D as its dark variant. It reads best at 48 to 72px, keeps the dragon purple, and the genre
accessory drops in without clashing with the face. B is the cleaner alternative but a pale icon stands out
against dark launchers; C is the safest if the change should stay small.

## Not verified

- Real launchers (Pixel, Samsung, themed-icon tinting) and the iOS 1024 / dark / tinted exports.
- The colours next to other icons on a real home screen, and how the horns read to players (imp vs dragon).
- The other seven genres and their dark variants; only Heroes and Crime were drawn.
- Any image-model route; no generator was called.
