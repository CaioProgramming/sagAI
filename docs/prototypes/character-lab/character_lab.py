#!/usr/bin/env python3
"""Character lab: tune the character blueprints from the terminal, without the app.

Pulls the Remote Config template (hmg by default) through the Firebase CLI, rebuilds the prompts the
app sends for character generation and for the design review, calls the Gemini REST API and prints the
sheets. The prompt is an approximation of the app's: persona, directives, rules and examples of each
blueprint, the theme aesthetic, the appearance blueprint with its drawn examples and the cast so far.
It does not add the app's global instructions or the output schema it derives from the Kotlin class.

  GEMINI_API_KEY=... ./character_lab.py --genre heroes --count 4
  ./character_lab.py --genre heroes --dry-run                       # prints the prompts, no network
  ./character_lab.py --appearance-file my_heroes_appearance.json    # try a local blueprint before publishing

Nothing is written to Remote Config or to any database.
"""
import argparse, json, os, random, subprocess, sys, tempfile, urllib.request, urllib.error

GEN_KEY, REVIEW_KEY = "character_generation_blueprint", "character_design_review_blueprint"
SHEET_FIELDS = ["name", "lastName", "nicknames", "backstory", "race", "gender", "ethnicity", "age", "height", "weight",
                "hair", "eyes", "mouth", "distinctiveMarks", "jawline", "buildAndPosture", "skinAppearance",
                "distinguishFeatures", "outfitDescription", "accessories", "carriedItems", "skillsAndProficiencies",
                "uniqueOrSignatureTalents", "occupation", "personality", "artwork"]
REVIEW_FIELDS = ["hair", "eyes", "mouth", "distinctiveMarks", "distinguishFeatures", "buildAndPosture",
                 "outfitDescription", "accessories", "personality", "artwork", "rationale"]
SHOWN = ["gender", "hair", "outfitDescription", "accessories", "distinctiveMarks", "personality"]
COLORS = ["coral", "teal", "mustard", "violet", "emerald", "crimson", "indigo", "amber", "magenta", "forest green"]


def load_template(project, path):
    if path:
        return json.load(open(path))
    out = os.path.join(tempfile.mkdtemp(), "template.json")
    subprocess.run(["firebase", "remoteconfig:get", "--project", project, "-o", out], check=True, capture_output=True)
    return json.load(open(out))


def param(template, key):
    for group in template.get("parameterGroups", {}).values():
        if key in group["parameters"]:
            return group["parameters"][key]["defaultValue"]["value"]
    if key in template.get("parameters", {}):
        return template["parameters"][key]["defaultValue"]["value"]
    raise SystemExit(f"Remote Config key not found: {key}")


def blueprint(template, key, override=None):
    return json.load(open(override)) if override else json.loads(param(template, key))


def draw_examples(bp):
    ex = bp.get("examples", [])
    n = bp.get("examplesPerCall")
    return random.sample(ex, min(n, len(ex))) if n else ex


def draw_pools(bp):
    """bp['pools'] is {name: [items]}; each call shows bp['poolsPerCall'] random items of every pool."""
    n = bp.get("poolsPerCall")
    return {k: (random.sample(v, min(n, len(v))) if n else v) for k, v in bp.get("pools", {}).items()}


def render(value, level=2):
    if isinstance(value, dict):
        return "\n".join(f"{'#' * level} {k}\n{render(v, level + 1)}" for k, v in value.items())
    if isinstance(value, list):
        return "\n".join(f"- {render(v, level + 1)}" if not isinstance(v, dict) else "- " + "; ".join(f"{k}: {x}" for k, x in v.items()) for v in value)
    return str(value)


NEG = r"(never|forbidden|avoid|not |no |fail|redo|stock|default|cliche|don't|nunca)"


def strip_negatives(bp):
    """Ablation: drop every sentence of the directives and rules that forbids or warns."""
    import re
    def clean(text):
        sentences = re.split(r"(?<=[.!?])\s+", text)
        kept = [x for x in sentences if not re.search(NEG, x, re.I)]
        return " ".join(kept)
    out = dict(bp)
    for key in ("directives", "rules"):
        out[key] = {k: c for k, c in ((k, clean(v)) for k, v in bp.get(key, {}).items()) if c.strip()}
    return out


def system_text(*blueprints, extra=None):
    parts = []
    for bp in blueprints:
        if bp.get("role"):
            parts.append(f"# Persona\n{bp['role']}")
        if bp.get("directives"):
            parts.append(f"# Directives\n{render(bp['directives'])}")
        if bp.get("rules"):
            parts.append(f"# rules\n{render(bp['rules'])}")
        examples = draw_examples(bp)
        if examples:
            parts.append(f"# examples\n{render(examples)}")
        pools = draw_pools(bp)
        if pools:
            parts.append(f"# pools\n{render(pools)}")
    if extra:
        parts.append(extra)
    return "\n\n".join(parts)


def call(key, model, system, user, temperature, fields, thinking=None):
    body = {
        "systemInstruction": {"parts": [{"text": system + f"\n\nAnswer with one JSON object whose keys are exactly: {', '.join(fields)}. "
                                                         "Write the values in Brazilian Portuguese."}]},
        "contents": [{"role": "user", "parts": [{"text": user}]}],
        "generationConfig": {"temperature": temperature, "responseMimeType": "application/json"},
    }
    if thinking:
        body["generationConfig"]["thinkingConfig"] = {"thinkingLevel": thinking}
    req = urllib.request.Request(f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent",
                                 json.dumps(body).encode(), {"Content-Type": "application/json", "x-goog-api-key": key})
    for attempt in range(4):
        try:
            data = json.load(urllib.request.urlopen(req, timeout=180))
            text = data["candidates"][0]["content"]["parts"][-1]["text"]
            return {k: "; ".join(map(str, v)) if isinstance(v, list) else v for k, v in json.loads(text).items()}
        except urllib.error.HTTPError as e:
            if e.code in (429, 500, 503) and attempt < 3:
                import time; time.sleep(8 * (attempt + 1)); continue
            raise SystemExit(f"HTTP {e.code}: {e.read().decode()[:300]}")


def judge(key, model, cast, thinking):
    """LLM judge: how recognizable is each sheet from across a room, and which pairs could be confused."""
    sheets = [{"n": i + 1, "hair": c.get("hair", ""), "outfit": c.get("outfitDescription", ""),
               "accessories": c.get("accessories", ""), "marks": c.get("distinctiveMarks", "")} for i, c in enumerate(cast)]
    system = ("You are a comic-book art director. For each character sheet, rate from 1 to 5 how recognizable the character is "
              "from across a room by silhouette, dominant color and one iconic element alone (1 = a forgettable extra, "
              "5 = instantly iconic). Also list the pairs of characters that could be confused at a distance. Be strict.")
    out = call(key, model, system, json.dumps(sheets, ensure_ascii=False), 0.2, ["scores", "confusable_pairs"], thinking)
    return out


EXCLUDED_FROM_IMAGE_CONTEXT = {"artwork"}


def image_prompt(template, genre, aesthetic, sheet, key, model, thinking):
    """The app's two image stages: a text model writes the final prompt from the icon blueprint, the theme's rendering
    blueprint and the sheet; the caller then sends that prompt to the image model."""
    unified = blueprint(template, "unified_icon_blueprint")
    rendering = blueprint(template, f"{genre}_rendering_blueprint")
    sheet_text = "\n".join(f"{k}: {v}" for k, v in sheet.items() if k not in EXCLUDED_FROM_IMAGE_CONTEXT and v)
    context = f"### CONCEPT ART DIRECTION\n{sheet.get('artwork', '')}\n\n### CHARACTER\n{sheet_text}"
    system = system_text(unified, rendering, extra=f"# ThemeAesthetic\n{aesthetic}")
    user = f"# TASK CONTEXT\ncontext:\n  context: {context}\n  aesthetic: {aesthetic}\n  genre: {genre.upper()}"
    body = {"systemInstruction": {"parts": [{"text": system}]}, "contents": [{"role": "user", "parts": [{"text": user}]}],
            "generationConfig": {"temperature": 1.0}}
    if thinking:
        body["generationConfig"]["thinkingConfig"] = {"thinkingLevel": thinking}
    req = urllib.request.Request(f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent",
                                 json.dumps(body).encode(), {"Content-Type": "application/json", "x-goog-api-key": key})
    data = json.load(urllib.request.urlopen(req, timeout=180))
    return data["candidates"][0]["content"]["parts"][-1]["text"].strip()


def render_image(key, model, prompt, path):
    """Sends the final prompt to the image model as the app does; returns None when the key cannot use image models."""
    import base64
    body = {"contents": [{"parts": [{"text": prompt}]}], "generationConfig": {"responseModalities": ["TEXT", "IMAGE"]}}
    req = urllib.request.Request(f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent",
                                 json.dumps(body).encode(), {"Content-Type": "application/json", "x-goog-api-key": key})
    try:
        data = json.load(urllib.request.urlopen(req, timeout=240))
    except urllib.error.HTTPError as e:
        return f"HTTP {e.code}: {e.read().decode()[:200]}"
    for part in data["candidates"][0]["content"]["parts"]:
        if "inlineData" in part:
            open(path, "wb").write(base64.b64decode(part["inlineData"]["data"]))
            return path
    return "no image in the response"


def cast_looks(cast):
    return [{"name": f"{c.get('name', '')} {c.get('lastName', '')}".strip(), "hair": c.get("hair", "")[:160],
             "outfit": c.get("outfitDescription", "")[:160], "accessories": c.get("accessories", "")[:160],
             "marks": "; ".join(x for x in (c.get("distinctiveMarks", ""), c.get("distinguishFeatures", "")) if x)[:160],
             "personality": c.get("personality", "")[:160]} for c in cast]


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--genre", default="heroes")
    ap.add_argument("--count", type=int, default=3)
    ap.add_argument("--seed", default="A new character who could believably exist in this world.", help="the discovery context")
    ap.add_argument("--project", default="sagai-hmg")
    ap.add_argument("--template", help="a local Remote Config template json instead of the CLI download")
    ap.add_argument("--appearance-file", help="local json replacing the genre appearance blueprint")
    ap.add_argument("--generation-file")
    ap.add_argument("--review-file")
    ap.add_argument("--no-review", action="store_true")
    ap.add_argument("--no-cast-looks", action="store_true", help="ablation: the generation sees the cast names but not their looks")
    ap.add_argument("--image-prompts", metavar="DIR", help="also build the final image prompt of every sheet and write it to DIR")
    ap.add_argument("--image", action="store_true", help="with --image-prompts, also render it with the image model")
    ap.add_argument("--image-model", default="gemini-3.1-flash-image")
    ap.add_argument("--minimal", action="store_true", help="ablation: only the persona, the theme aesthetic and the seed")
    ap.add_argument("--strip-negatives", action="store_true", help="ablation: drop the sentences that forbid or warn")
    ap.add_argument("--model", default="gemini-3.5-flash-lite")
    ap.add_argument("--thinking", choices=["minimal", "low", "medium", "high"], help="thinking level, as the app's HIGH tier sets it")
    ap.add_argument("--judge", action="store_true", help="ask the model to rate how recognizable each sheet of a cast is from afar")
    ap.add_argument("--dry-run", action="store_true", help="print the prompts and exit, no network")
    args = ap.parse_args()

    template = load_template(args.project, args.template)
    gen = blueprint(template, GEN_KEY, args.generation_file)
    review = blueprint(template, REVIEW_KEY, args.review_file)
    aesthetic = json.loads(param(template, f"{args.genre}_config")).get("aesthetic", "")
    key = os.environ.get("GEMINI_API_KEY")
    if not key and not args.dry_run:
        raise SystemExit("Set GEMINI_API_KEY, or use --dry-run.")

    cast = []
    for i in range(args.count):
        appearance = blueprint(template, f"{args.genre}_appearance_blueprint", args.appearance_file)
        theme = f"# ThemeAesthetic\n{aesthetic}"
        if args.minimal:
            gen_system = f"# Persona\n{gen['role']}\n\n{theme}"
        elif args.strip_negatives:
            gen_system = system_text(strip_negatives(gen), strip_negatives(appearance), extra=theme)
        else:
            gen_system = system_text(gen, appearance, extra=theme)
        gen_user = ("# TASK CONTEXT\ncontext:\n  SagaContext: A mock saga set in this world.\n"
                    f"  CharactersCast: {', '.join(c.get('name', '') for c in cast) or 'none yet'}\n"
                    f"{'' if args.no_cast_looks else '  CastLooks: ' + json.dumps(cast_looks(cast), ensure_ascii=False) + chr(10)}"
                    f"  NewCharacterContext: {args.seed}\n  NewCharacterFavoriteColor: {random.choice(COLORS)}")
        if args.dry_run:
            print(f"===== GENERATION SYSTEM ({len(gen_system)} chars)\n{gen_system}\n\n===== GENERATION USER\n{gen_user}\n")
        sheet = {} if args.dry_run else call(key, args.model, gen_system, gen_user, 1.0, SHEET_FIELDS, args.thinking)
        reviewed = dict(sheet)
        if not args.no_review:
            rev_system = system_text(review, appearance)
            rev_user = ("# TASK CONTEXT\ncontext:\n  NewCharacterSheet: " + json.dumps(sheet, ensure_ascii=False) +
                        f"\n  NewCharacterContext: {args.seed}\n  CastLooks: {json.dumps(cast_looks(cast), ensure_ascii=False)}")
            if args.dry_run:
                print(f"===== REVIEW SYSTEM ({len(rev_system)} chars)\n{rev_system}\n")
                break
            changes = call(key, args.model, rev_system, rev_user, 0.7, REVIEW_FIELDS, args.thinking)
            reviewed.update({k: v for k, v in changes.items() if k != "rationale" and v})
            rationale = changes.get("rationale") or "-"
        if args.dry_run:
            break
        print(f"\n#{i + 1} {sheet.get('name', '?')} {sheet.get('lastName', '')}")
        for f in SHOWN:
            print(f"  {f}: {sheet.get(f, '')}")
        if not args.no_review:
            changed = [f for f in REVIEW_FIELDS[:-1] if reviewed.get(f) != sheet.get(f)]
            print(f"  REVIEW ({rationale}): {len(changed)} field(s)")
            for f in changed:
                if f != "artwork":
                    print(f"    {f} -> {reviewed[f]}")
        cast.append(reviewed)
        if args.image_prompts and not args.dry_run:
            os.makedirs(args.image_prompts, exist_ok=True)
            slug = f"{args.seed[:18].strip().replace(' ', '_')}_{i + 1}_{reviewed.get('name', 'x').split()[0]}"
            final = image_prompt(template, args.genre, aesthetic, reviewed, key, args.model, args.thinking)
            open(os.path.join(args.image_prompts, slug + ".txt"), "w").write(final)
            print(f"  IMAGE PROMPT -> {slug}.txt ({len(final)} chars)")
            if args.image:
                print("  IMAGE ->", render_image(key, args.image_model, final, os.path.join(args.image_prompts, slug + ".png")))
        if args.judge and (i + 1) == args.count:
            verdict = judge(key, args.model, cast, args.thinking)
            print(f"JUDGE scores={verdict.get('scores')} confusable={verdict.get('confusable_pairs')}")


if __name__ == "__main__":
    main()
