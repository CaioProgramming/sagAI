#!/usr/bin/env python3
"""Round 12 of the icon study: the Cyberpunk variant, small implant marks on the face.

The skin, eyes and nostrils are the approved base in Cyberpunk's primary colour (`generate_genres.py`). The user's
direction, after two passes that tried to turn the face into a robot ("you tried too hard"): just a few strokes on
regions of the face, as if it were an implant in the skin, like the small marks on the faces of Cyberpunk 2077
characters (references: tiny dashes on the forehead and by the nose, a thin line from under one eye down to a small
cheek plate, a small trapezoid on the nose bridge). No plates the size of the forehead, no screws, no label, no
glowing eye. The drawing is tone on tone with the skin, plus one small accent in the genre's cyan.

  - 2 short dashes high on the forehead and 2 beside the nose (mirrored)
  - a small trapezoid on the nose bridge, filled cyan
  - on the right cheek only (so it reads as an implant, not a mask): a thin line from under the eye to a small open
    cheek plate, with a magenta LED dot at its corner

Light mode: dark purple strokes on the pale lavender skin. Dark mode: the strokes glow cyan on the dark purple.

    python3 generate_cyberpunk.py          # writes svg/cyberpunk_*.svg and cyberpunk_preview.html
    python3 generate_cyberpunk.py --png    # also rasterises with headless Chrome
"""
import subprocess
import sys

import generate as g
import generate_flat as fl
import generate_genres as gen
import generate_zoom as z

HERE, SVG, PNG = g.HERE, g.SVG, g.PNG
PRIMARY = "#6E48AA"
CYAN, MAGENTA = "#00F5FF", "#FF2EFF"
STROKE = {"light": ("#4A2C85", .9), "dark": ("#B796FF", .85)}  # tone on tone with the skin: darker on light, lighter on dark


def eye_lines():
    """One thin angular trace per eye, in the way of the references. It is a single line that runs behind the eye: it
    emerges above it, bends on a diagonal and ends in a short stub, and the same below, so it reads as a trace that
    continues under the eye, an implant in the skin. It is drawn under the eyes, so the middle is hidden by them."""
    d = ""
    for x0, x1, x2 in ((330, 282, 206), (694, 742, 818)):
        d += f"M{x2},392 H{x1} L{x0},440 V790 L{x1},838 H{x2} "
    return d


ACCENT = ("#FFB000", "#E88A00")  # amber, the complement of the purple and the colour of the warning labels in the references


def bridge():
    """A small triangle pointing down on the nose bridge, between the eyes: the one accent colour of the face."""
    return "M464,520 H560 L512,616 Z"


def bridge_trace():
    """On the nose bridge, simply: one horizontal line that crosses the area between the eyes and runs behind the
    triangle (which hides the middle)."""
    return "M380,570 H492 M532,570 H644"


def marks(mode):
    colour, op = STROKE[mode]
    stroke = f'stroke="{colour}" stroke-opacity="{op}"'
    fill, edge = ACCENT
    return (f'<g fill="none" {stroke} stroke-width="14" stroke-linecap="round">'
            f'<path d="{eye_lines()}"/><path d="{bridge_trace()}"/></g>'
            f'<path d="{bridge()}" fill="{fill}" stroke="{edge}" stroke-width="8" stroke-linejoin="round"/>')


def svg(mode, paths):
    p = gen.palette(PRIMARY, mode)
    eyes = "".join(f'<path d="{d}" fill="#FFFFFF"/>' for d in paths)
    return g.wrap(f'{fl.skin_defs(p)[:-7]}</defs><clipPath id="tclip"><path d="{g.TILE}"/></clipPath>'
                  f'<path d="{g.TILE}" fill="url(#skin)"/><g clip-path="url(#tclip)">{marks(mode)}{eyes}{fl.details(p, "focinho")}</g>')


def build():
    SVG.mkdir(exist_ok=True)
    paths = fl.eye_paths(z.shipped_eyes())
    for mode in ("light", "dark"):
        (SVG / f"cyberpunk_{mode}.svg").write_text(svg(mode, paths))
    big = ""
    for mode, label in (("light", "claro"), ("dark", "escuro")):
        big += (f'<div class="row"><figure><img src="reference/shipped_cyberpunk_{mode}.png"><figcaption>Atual ({label})</figcaption></figure>'
                f'<span class="arrow">→</span><figure><img src="svg/cyberpunk_v8_{mode}.svg"><figcaption>v8, trapézio ({label})</figcaption></figure>'
                f'<span class="arrow">→</span><figure><img src="svg/cyberpunk_{mode}.svg"><figcaption>v9, triângulo âmbar ({label})</figcaption></figure></div>')
    small = "".join(
        f'<div class="sm"><span>{label}</span>' + "".join(f'<img src="svg/cyberpunk_{mode}.svg" style="width:{s}px;height:{s}px">' for s in (144, 96, 72, 48)) + "</div>"
        for mode, label in (("light", "Traços claro"), ("dark", "Traços escuro")))
    html = f"""<!doctype html><meta charset="utf-8"><title>Cyberpunk</title>
<style>
body{{margin:0;padding:28px;background:#0b0b0d;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:24px 0 12px}} .row{{display:flex;gap:10px;align-items:center;margin-bottom:16px}}
figure{{margin:0;padding:10px;border-radius:16px;background:#000;text-align:center}} figure img{{width:300px;height:300px;display:block;border-radius:22%}}
figcaption{{margin-top:8px;font-size:12px;opacity:.85}} .arrow{{font-size:22px;opacity:.5}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin-bottom:12px}} .sm span{{width:110px;font-size:13px;opacity:.75}} .sm img{{border-radius:22%}}
</style>
<h2>Cyberpunk: do ícone atual aos traços de implante no rosto</h2>{big}<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{small}"""
    (HERE / "cyberpunk_preview.html").write_text(html)


def render():
    PNG.mkdir(exist_ok=True)
    for mode in ("light", "dark"):
        name = f"cyberpunk_{mode}"
        subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                        "--window-size=1024,1024", f"--screenshot={PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
                       check=True, capture_output=True)
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1150,1350",
                    f"--screenshot={HERE / 'cyberpunk_preview.png'}", f"file://{HERE / 'cyberpunk_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
