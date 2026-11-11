#!/usr/bin/env python3
"""Round 11 of the icon study: the Crime variant, sunglasses over the eyes.

The skin, eyes and nostrils are the approved base in Crime's primary colour (`generate_genres.py`). The accessory is one
object. Version 5 (this file) follows the user's note that, given Crime's aesthetic, the glasses should be "more
beachy": the genre's vibe is Miami Vice and Vice City (neon, pastel, sunset, palm trees), so:

  - a chunky white retro frame with a heavy browline (wayfarer, 80s) instead of the near-black noir frame
  - lenses with a sunset gradient (deep violet at the top, hot pink, then orange at the bottom)
  - a palm tree silhouette reflected in each lens, the Vice City postcard
  - one bright reflection streak across the glass

The lenses keep the slant of the eyes behind them (high at the outer corner, low at the nose) and stay opaque.

    python3 generate_crime.py          # writes svg/crime_*.svg and crime_preview.html
    python3 generate_crime.py --png    # also rasterises with headless Chrome
"""
import subprocess
import sys

import generate as g
import generate_flat as fl
import generate_genres as gen
import generate_zoom as z

HERE, SVG, PNG = g.HERE, g.SVG, g.PNG
PRIMARY = "#E91E63"
FRAME = "#FFF6EC"
SUNSET = (("0", "#22103F"), (".42", "#B3267A"), (".74", "#FF5E7A"), ("1", "#FFA24A"))
PALM = "#1D0B3A"
# The outline of reference/palm-tree-svgrepo-com.svg (viewBox 0 0 32 32); the user confirmed its licence is free.
PALM_D = ("M11.177 1.287c-1.75 0.007-3.372 0.363-4.163 0.61 0 0 7.174 1.217 7.771 6.17-5.451-7.86-11.397 1.646-11.397 1.646 "
          "1.24-0.325 4.79-1.291 7.16-0.883 1.622 0.279 3.203 1.749 3.135 1.73-3.865 0.951-10.006 18.153-8.963 19.55 "
          "0.93 1.068 1.861 0.704 2.79 0.162 0.46-3.646 4.052-19.641 8.447-18.35 0.001 0 0.001-0 0.002 0 0.37 0.127 "
          "0.72 0.349 0.739 0.688 0.036 0.647-0.381 1.703-0.283 2.437 0.229 1.703 0.562 2.296 1.047 2.89 1.158-0.651 "
          "2.267-2.999 2.496-4.216 1.038 0.455 2.706 1.507 4.068 5.417 0.712-3.664-0.315-6.554-3.683-7.837 1.459-0.631 "
          "7.809 2.56 8.584 4.339 0.754-3.207-4.58-7.131-9.020-6.922 0.851-0.222 1.787 0.037 2.677-0.618 1.195-0.88 "
          "2.588-1.637 3.886-2.451-4.366-1.899-8.395-0.961-8.679 1.004-0.223-4.363-3.608-5.379-6.612-5.366v0z")
PALM_X, PALM_Y, PALM_S = 175, 548, 7.4  # top-left of the 32-unit box in tile coordinates, and its scale

LENS_PTS = [("M", (98, 484)), ("C", (190, 440), (338, 478), (442, 572)), ("C", (452, 650), (444, 712), (402, 762)),
            ("C", (336, 812), (214, 792), (148, 724)), ("C", (100, 672), (86, 560), (98, 484))]


def build_path(pts, mirror):
    def X(x):
        return 1024 - x if mirror else x
    return "".join(c + " ".join(f"{X(x)},{y}" for x, y in xy) + " " for c, *xy in ((p[0], *p[1:]) for p in pts))


def lens(mirror):
    """One lens: high at the outer corner, sloping down to the nose like the eye behind it."""
    return build_path(LENS_PTS, mirror) + "Z"


def brow(mirror):
    """The heavy top edge of a wayfarer: just the first curve of the lens, stroked thicker."""
    return build_path(LENS_PTS[:2], mirror)


def palm(mirror):
    """A natural palm tree silhouette (the user's `reference/palm-tree-svgrepo-com.svg`, a 32x32 outline), placed in the
    lower part of the lens. Mirrored for the right lens."""
    place = f"translate({PALM_X},{PALM_Y}) scale({PALM_S})"
    flip = "translate(1024,0) scale(-1,1) " if mirror else ""
    return f'<g transform="{flip}{place}"><path d="{PALM_D}" fill="{PALM}"/></g>'


def glasses_defs():
    stops = "".join(f'<stop offset="{o}" stop-color="{c}"/>' for o, c in SUNSET)
    return (f'<linearGradient id="glass" x1="0" y1="0" x2="0" y2="1">{stops}</linearGradient>'
            # one bright diagonal streak, running parallel to the slant of the lens
            + "".join(
                f'<linearGradient id="{i}" gradientUnits="userSpaceOnUse" x1="{x1}" y1="450" x2="{x2}" y2="800">'
                '<stop offset="0" stop-color="#fff" stop-opacity="0"/><stop offset=".2" stop-color="#fff" stop-opacity="0"/>'
                '<stop offset=".28" stop-color="#fff" stop-opacity=".6"/><stop offset=".36" stop-color="#fff" stop-opacity="0"/>'
                '<stop offset=".48" stop-color="#fff" stop-opacity="0"/><stop offset=".53" stop-color="#fff" stop-opacity=".25"/>'
                '<stop offset=".58" stop-color="#fff" stop-opacity="0"/></linearGradient>'
                for i, x1, x2 in (("glint", 70, 430), ("glintR", 954, 594))) +
            f'<clipPath id="lensL"><path d="{lens(False)}"/></clipPath><clipPath id="lensR"><path d="{lens(True)}"/></clipPath>')


def glasses():
    left, right = lens(False), lens(True)
    arms = "M98,490 L-120,432 M926,490 L1144,432"
    bridge = "M440,578 Q512,548 584,578"
    return (f'<path d="{left}" fill="url(#glass)"/><path d="{right}" fill="url(#glass)"/>'
            f'<g clip-path="url(#lensL)">{palm(False)}<rect x="60" y="430" width="420" height="400" fill="url(#glint)"/></g>'
            f'<g clip-path="url(#lensR)">{palm(True)}<rect x="544" y="430" width="420" height="400" fill="url(#glintR)"/></g>'
            f'<g fill="none" stroke="{FRAME}" stroke-linejoin="round" stroke-linecap="round">'
            f'<path d="{left}" stroke-width="34"/><path d="{right}" stroke-width="34"/>'
            f'<path d="{brow(False)}" stroke-width="62"/><path d="{brow(True)}" stroke-width="62"/>'
            f'<path d="{arms}" stroke-width="30"/><path d="{bridge}" stroke-width="30"/></g>')


def svg(mode, paths):
    p = gen.palette(PRIMARY, mode)
    eyes = "".join(f'<path d="{d}" fill="#FFFFFF"/>' for d in paths)  # hidden behind the glass; kept for a consistent layer order
    return g.wrap(f'{fl.skin_defs(p)[:-7]}{glasses_defs()}</defs><clipPath id="tclip"><path d="{g.TILE}"/></clipPath>'
                  f'<path d="{g.TILE}" fill="url(#skin)"/><g clip-path="url(#tclip)">{eyes}{glasses()}{fl.details(p, "focinho")}</g>')


def build():
    SVG.mkdir(exist_ok=True)
    paths = fl.eye_paths(z.shipped_eyes())
    for mode in ("light", "dark"):
        (SVG / f"crime_{mode}.svg").write_text(svg(mode, paths))
    big = ""
    for mode, label in (("light", "claro"), ("dark", "escuro")):
        big += (f'<div class="row"><figure><img src="reference/shipped_crime_{mode}.png"><figcaption>Atual ({label})</figcaption></figure>'
                f'<span class="arrow">→</span><figure><img src="svg/crime_v4_{mode}.svg"><figcaption>v4, noir ({label})</figcaption></figure>'
                f'<span class="arrow">→</span><figure><img src="svg/crime_{mode}.svg"><figcaption>v5, Miami Vice ({label})</figcaption></figure></div>')
    small = "".join(
        f'<div class="sm"><span>{label}</span>' + "".join(f'<img src="svg/crime_{mode}.svg" style="width:{s}px;height:{s}px">' for s in (144, 96, 72, 48)) + "</div>"
        for mode, label in (("light", "Crime claro"), ("dark", "Crime escuro")))
    html = f"""<!doctype html><meta charset="utf-8"><title>Crime</title>
<style>
body{{margin:0;padding:28px;background:#0b0b0d;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:24px 0 12px}} .row{{display:flex;gap:10px;align-items:center;margin-bottom:16px}}
figure{{margin:0;padding:10px;border-radius:16px;background:#000;text-align:center}} figure img{{width:300px;height:300px;display:block;border-radius:22%}}
figcaption{{margin-top:8px;font-size:12px;opacity:.85}} .arrow{{font-size:22px;opacity:.5}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin-bottom:12px}} .sm span{{width:110px;font-size:13px;opacity:.75}} .sm img{{border-radius:22%}}
</style>
<h2>Crime: do ícone atual aos óculos Miami Vice sobre a pele</h2>{big}<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{small}"""
    (HERE / "crime_preview.html").write_text(html)


def render():
    PNG.mkdir(exist_ok=True)
    for mode in ("light", "dark"):
        name = f"crime_{mode}"
        subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                        "--window-size=1024,1024", f"--screenshot={PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
                       check=True, capture_output=True)
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1150,1350",
                    f"--screenshot={HERE / 'crime_preview.png'}", f"file://{HERE / 'crime_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
