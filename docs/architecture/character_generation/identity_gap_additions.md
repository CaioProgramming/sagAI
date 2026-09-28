# Character Identity Gaps — Proposed Additions to Existing `{genre}_appearance_blueprint`

No new Remote Config key. Per the decision to keep everything in the existing per-genre
`appearance_blueprint` (already merged into `character_generation_blueprint` at the same call
sites in [CharacterUseCaseImpl.kt](../../../app/src/main/java/com/ilustris/sagai/features/characters/data/usecase/CharacterUseCaseImpl.kt)),
these are **additions only** — new entries to add into each blueprint's existing `directives`/`rules`
maps via the Firebase console or CLI. Nothing existing is changed or removed.

Audited by fetching the live template (`firebase remoteconfig:get --project sagai-ilustris`) and
reading every genre's full `directives`/`rules` text, not just key names.

**Not touched at all**: `crime_appearance_blueprint` — already covers ethnicity/culture (`ORIGIN_LANDSCAPE`),
class (`STATUS_VISIBILITY`), and body range (`SKIN_AND_BODY_RANGE`). No gaps found.

## Round 2 — body proportion

Round 1 covered ethnicity/culture/species/class. It missed a distinct axis: **body-type diversity**
(build, weight, height) as its own explicit mandate — separate from ethnicity or class. Audited
against `heroes_appearance_blueprint`'s `BODY_AND_AGE_RANGE` + `VALIDATION_BODY_DEFAULT` (the
strongest existing example) as the bar. Only `heroes` and `crime` already had this; the other 7 —
including `space_opera`, whose `SPECIES_AS_DESIGN_SPACE` covers alien anatomy but never mentions
human body range — get one new directive each below.

## Code change (not RC): creative freedom at the sampling level

`generateCharacter`/`generateCharacterStream` in
[CharacterUseCaseImpl.kt](../../../app/src/main/java/com/ilustris/sagai/features/characters/data/usecase/CharacterUseCaseImpl.kt)
never set `temperatureRandomness`, so they ran at `GemmaClient`'s default (`.5f`, medium) — the same
setting used for routine, low-variance tasks. Every other genuinely creative/varied generation call
in the codebase (`SagaIdeationService.generateCosmicLibrary`, `ReasoningSynthesizerService`) explicitly
sets `1f`. Character generation is exactly this kind of task — sameness across characters was the
original complaint — so both calls now pass `temperatureRandomness = 1f`, matching that convention.
This is independent of and additive to the prompt-text work below: the model now has both the
explicit permission (directives) and the sampling behavior (temperature) that make deviation from
the obvious default likely instead of just allowed.

**Additions below**, written to match each blueprint's existing voice/register — draft, needs your
read before anything gets published.

---

## fantasy_appearance_blueprint

Currently: entirely about history/weight/specificity of *objects and appearance* — zero mention of
species, magic origin, or culture beyond "kingdom."

**New directives:**
- `SPECIES_BEYOND_HUMAN`: "This world was never only human. Draw freely from beastfolk carrying an animal's instinct in a person's frame, elementals whose bodies are substance more than flesh, fae-touched who never fully returned from wherever they were taken, constructs built rather than born, and bloodlines where the inheritance shows in skin, eyes, or the shape of a hand. A cast that is uniformly human in a world this old and this touched by magic is a failure of imagination, not a neutral choice."
- `MAGIC_ORIGIN_AS_BIOGRAPHY`: "Power was never acquired the same way twice. Born with it and never asked. Studied for it across a decade that cost them everything else. Bargained for it and still paying. Inherited a relic that chose them more than they chose it. Stole it and still flinches when using it. Cursed with it and calls it a gift out of survival. The character's relationship to their own power is a biography, not a stat — and it should read differently from every other spellcaster in the story."
- `KINGDOMS_ARE_NOT_ONE_KINGDOM`: "This world does not have one culture wearing different crowns. Nomadic clans who read the land instead of maps. Desert courts where power is measured in water rights. Jungle city-states built into canopy rather than around it. Mountain orders who took vows nobody outside remembers. Seafaring guilds whose only loyalty is the ledger. A character's origin should be legible in dialect, custom, and craft — not just in the crest they carry."

**New rule:**
- `NO_DEFAULT_HUMAN`: "FORBIDDEN: defaulting to human with no narrative reason in a cast this varied. If a character is human, that itself should be a specific choice — a kingdom, a class, a relationship to magic that a human specifically has — not the unmarked default everyone else deviates from."

## horror_appearance_blueprint

Currently: entirely about the aesthetic of "wrongness" — zero mention of who is vulnerable, who
survives, or their class/background.

**New directives:**
- `WHO_GETS_TO_BE_AFRAID`: "The person this happens to is never chosen for narrative convenience. An exhausted night-shift nurse. A retired man who has outlived everyone he was supposed to grow old with. A teenager nobody believes yet. A mother whose fear reads as competence because panic is not a cost she can afford. Design the person a horror could actually happen to — not the person horror conventionally happens to. Youth and conventional attractiveness are one possibility among many, and rarely the most interesting one."
- `THE_LIFE_BEFORE_THE_WRONGNESS`: "Ground every character in a specific, unglamorous ordinariness before anything arrives — a job with real hours, a class position that shapes what they can and cannot walk away from, a routine the wrongness will interrupt. The horror lands harder against a life that was fully, boringly real."

**New rule:**
- `VULNERABILITY_IS_NOT_A_DEMOGRAPHIC`: "FORBIDDEN: defaulting to the same age, build, and social position for who is threatened and who survives. Fear and endurance are not the property of any one kind of body. An elderly character's fear is not comic relief. A disabled character's vulnerability is not the point of their existence in the story."

## shinobi_appearance_blueprint

Currently: entirely about clan/lineage/marks — zero mention of age range, gender, or loyalty/dissent.

**New directives:**
- `THE_LIFE_HAS_NO_SINGLE_AGE`: "This world is not populated only by prodigies in their prime. A twelve-year-old who awakened something too early. A veteran whose hands still work but whose knees no longer forgive a hard landing. A retired master who trains others because the field finally asked too much of their own body. Skill and narrative weight are not the exclusive property of youth."
- `THE_CODE_IS_NOT_UNIVERSAL`: "Not every shinobi serves their clan without question. Design defectors who left for reasons that were right by their own measure, spies playing both sides for a cause bigger than either, and rogue agents whose skill is exactly as considerable as anyone still wearing the crest. Loyalty in this world is a choice repeated daily, not a birthright."

**New rule:**
- `GENDER_DOES_NOT_LIMIT_THE_DISCIPLINE`: "FORBIDDEN: treating combat leadership, strategic command, or the most feared techniques as a male default. Women and non-binary characters hold every one of these positions as commonly as men, with lineage and marks exactly as considered."

## punk_rock_appearance_blueprint

Currently: entirely about the "assembled self" as individual style — zero mention of who the scene
is actually made of.

**New directives:**
- `THE_SCENE_WAS_NEVER_ONE_DEMOGRAPHIC`: "This scene was built by working-class kids, immigrants' children, queer teenagers who found the one room that didn't ask them to explain themselves, and voices the mainstream never had room for. A cast that reads as uniformly white and suburban has erased the people who built the genre. Draw the crowd from where it actually came from."
- `GENDER_AND_QUEERNESS_ARE_NOT_A_SUBPLOT`: "Gender presentation and queerness in this world are visible, varied, and stated plainly through style rather than treated as a twist or a footnote — the scene has always included this, unapologetically and in the front row."

**New rule:**
- `CLASS_SHOWS_IN_WHAT_WASNT_BOUGHT_NEW`: "Economic reality is part of the assembled self — thrifted, handmade, and repaired-out-of-necessity read differently from repaired-as-aesthetic-choice. Let the difference between broke-and-committed and comfortable-and-slumming-it be visible."

## heroes_appearance_blueprint

Currently: strong on registers (declared/marked/ordinary), body/age range, and "the marked" as
disability-adjacent — but silent on power-origin variety and ethnic diversity.

**New directives:**
- `POWER_DID_NOT_ARRIVE_THE_SAME_WAY_TWICE`: "Vary how the extraordinary entered this character's life across the cast — born with it, built it in a lab or garage, inherited an artifact, survived an accident that should have killed them, or trained a discipline until it became something more. A cast where everyone's origin rhymes is a failure of the premise."
- `THE_DECLARED_COME_FROM_EVERYWHERE`: "A public identity does not erase where someone is from. Names, features, and civilian-life specificity should be drawn from a genuinely wide human range — the mask does not require a default face underneath it."

## cyberpunk_appearance_blueprint

Currently: excellent on modification spectrum and class stratification — silent on human ethnic
diversity specifically.

**New directive:**
- `THE_MEGACITY_HAS_NO_DEFAULT_FACE`: "This city absorbed migration, corporate relocation, and generations of mixing long before the story starts. Names, features, and accents should be drawn from a genuinely global range — draw as freely from Lagos, São Paulo, Seoul, and Mumbai lineages as from anywhere else. A cast that defaults to one ethnicity under all that chrome hasn't earned the word megacity."

## cowboy_appearance_blueprint

Currently: good on class/origin, but only *allusively* ("a piece of jewelry that is not
frontier-made") — never names the actual historical ethnic makeup of the West.

**New directive:**
- `THE_FRONTIER_WAS_NEVER_ONE_COLOR`: "History is specific here: Black cowboys who made up a real share of every cattle drive, Mexican vaqueros whose technique and vocabulary the whole trade borrowed, Chinese laborers who built the railroads threading through this world, and Indigenous peoples whose land this always was first. A cast that defaults to the white gunslinger has erased the actual frontier. Origin should be as concrete as a name and a trade, not just implied through a borrowed trinket."

---

## Round 2 additions — body proportion (one new directive per genre)

- **fantasy** — `BODIES_CARRY_THEIR_LIVES`: "A body in this world is shaped by what it has done, not by an ideal. A warrior can be stocky and low to the ground rather than lean. A sorcerer can be soft and unpracticed with a blade because the mind was always the instrument, not the body. Height, weight, and build should vary as much as the magic does." + rule `NO_UNIFORM_PHYSIQUE`.
- **cyberpunk** — `THE_FLESH_UNDERNEATH_VARIES`: "Chrome sits on top of a body that was never uniform to begin with — heavyset frames modified for durability rather than elegance, small wiry builds built for squeezing through maintenance shafts, aging bodies kept moving by implants that compensate for what flesh no longer can. The modification should read against the specific body it was grafted to, not a single idealized base." + rule `NO_STANDARD_CHASSIS`.
- **horror** — `THE_BODY_WAS_ALREADY_SPECIFIC`: "Before anything is wrong with it, the body belongs to a specific, unremarkable person — heavier, thinner, older, or more tired than a horror-movie default allows. The wrongness reads stronger against an ordinary, particular body than a conventionally fit one, because that body was never braced for what's coming."
- **shinobi** — `THE_DISCIPLINE_DOES_NOT_REQUIRE_ONE_BUILD`: "Technique compensates for the body, not the other way around. A stocky frame built for close-quarters brutality. A slight, unassuming build that survives by never being taken seriously until too late. An aging body that has traded speed for economy of motion. Do not let every practitioner converge on the same lean silhouette."
- **space_opera** — `HUMAN_BODIES_DIDNT_STANDARDIZE_EITHER`: "Ten thousand years of gravity wells, station life, and colony diets produced human bodies as varied as the cultures that shaped them — dense and compact from high-gravity worlds, tall and long-limbed from low-gravity stations, softened by generations without manual labor, weathered by generations of it. Human characters should show this range as clearly as alien characters show their species' design space."
- **cowboy** — `LABOR_SHAPES_EVERY_BODY_DIFFERENTLY`: "The frontier does not build one kind of body. A rancher thickened by decades of physical work. A gunfighter kept lean by a life that rewards speed. A drifter soft in places a harder life would have worn down, because they haven't been out here as long as they claim."
- **punk_rock** — `THE_BODY_ISNT_A_UNIFORM_EITHER`: "The scene never required one body type to belong. Heavier bodies in leather that fits exactly as intended. Small frames that take up more space through sound and presence than physical size. Bodies changed by age, by hard living, or by nothing dramatic at all."

`heroes` and `crime` already had this axis covered and were left untouched.

## Round 3 — visual impact / permission to be iconic

Prompted by reference images across all 9 genres: `crime` (Miami Vice glamour), `heroes` (X-Men,
saturated color), `space_opera` (bold retro sci-fi), `punk_rock` (illustrated band art), and `shinobi`
(the ink-minimalist restraint IS its iconic quality) already grant explicit permission to be visually
bold — this matters because appearance feeds directly into portrait image generation. Three genres
lean hard into "grounded/worn/specific" without a counterbalancing "and also: breathtaking" — added
one directive each:

- **fantasy** — `VISUAL_GRANDEUR_IS_PERMITTED`: "This world's brutality does not require its beauty to be dimmed. A character can be devastatingly striking — armor catching light like it was forged for a painting, hair moving like it has its own weather, a face composed the way religious art composes its saints — without losing any of the wear and history this blueprint demands. Grounded and breathtaking are not opposites here. This character will become a portrait — do not soften them into visual safety out of fear of being too much."
- **cowboy** — `THE_FRONTIER_CAN_BE_CINEMATIC`: "Worn and functional does not mean forgettable. A character can be lit like the hero of the story they are in — dust catching golden-hour light, silhouette sharp against open sky, a stance that reads as legend in the making — while every garment still carries the specific wear this blueprint demands. Authentic and iconic are not opposites on this frontier. This character will become a portrait — give it something worth painting."
- **cyberpunk** — `NO_FEAR_OF_BEAUTIFUL_CHROME`: "High-end modification can be gorgeous, not just functional or scarred — chrome polished to a mirror shine, bioluminescent seams, a face augmented into something more striking than the human default allows. Beauty and modification are not opposites. Do not undersell how visually arresting a well-resourced or well-loved modification can be, alongside the jury-rigged end of the spectrum this blueprint already demands. This character will become a portrait — commit to the version of them that stops a scroll."

`horror` was deliberately left out of this round — its impact comes from unsettling specificity, not
glamour, and "iconic boldness" would work against `PLAUSIBILITY_IS_THE_WEAPON`.

---

## Full merged JSON (existing content + additions), ready to paste per key

Saved locally (not in the repo, since it embeds full existing blueprint text pulled from
production RC) at `/tmp/merged_appearance_blueprints.json` — one full `PromptBlueprint` JSON per
key, ready to paste as that parameter's value via the Firebase console, or publish via
`firebase remoteconfig:get`/edit/`versions:publish`.
