#!/usr/bin/env python3
"""Round 10 of the icon study: the Fantasy variant, a gothic helm over the upper half of the face.

The skin, eyes and nostrils are the approved base in the genre's primary colour (`generate_genres.py`). The accessory is
a dark gunmetal helm in its own colour (not the theme's), so it reads as an object on the skin. Version 3 (this file),
after the user's review of v2 (a crown outline with horns): no crown and no horns, the plate covers the whole top of the
tile like v1, a darker and more sombre metal with diagonal reflections, a much longer needle-like visor between the eyes,
and a faceted prism on the forehead instead of a round gem.

The eyes are painted first and the helm over them, so the brim cuts their tops and they read as seen from under it.

    python3 generate_fantasy.py          # writes svg/fantasy_*.svg and fantasy_preview.html
    python3 generate_fantasy.py --png    # also rasterises with headless Chrome
"""
import subprocess
import sys

import generate as g
import generate_flat as fl
import generate_genres as gen
import generate_zoom as z

HERE, SVG, PNG = g.HERE, g.SVG, g.PNG
PRIMARY = "#8B2635"
PLATE = {"light": ("#2A2C38", "#0D0E14"), "dark": ("#585D72", "#202230")}
EDGE = {"light": "#7F86A3", "dark": "#BCC1D6"}
SETTING = {"light": "#0B0C11", "dark": "#1B1D28"}
# the prism, a tall octahedron in the Fantasy palette's reds: four facets, each a gradient (light edge to deep edge) that
# fakes a reflection sliding over the glass
FACETS = {"tl": ("#FFC2CD", "#E5566F"), "tr": ("#E5566F", "#A12A41"), "bl": ("#C93A54", "#86203A"), "br": ("#8F2237", "#4A0E1C")}
PRISM = {"top": (512, 332), "l": (466, 432), "r": (558, 432), "bot": (512, 540), "f": (512, 458)}

# The whole top of the tile, then the brim from the right edge to the left: it slants down towards the middle and ends
# in a long needle between the eyes.
PLATE_D = ("M-20,-20 H1044 V520 L880,530 L640,600 L602,642 Q566,700 546,752 L512,858 L478,752 Q458,700 422,642 "
           "L384,600 L144,530 L-20,520 Z")
LINES_D = "M512,562 V812 M470,504 L388,596 M554,504 L636,596"


def helm_defs(mode):
    a, b = PLATE[mode]
    return (f'<linearGradient id="steel" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{a}"/>'
            f'<stop offset="1" stop-color="{b}"/></linearGradient>'
            # diagonal bands of light, like reflections sliding over curved metal
            '<linearGradient id="sheen" gradientUnits="userSpaceOnUse" x1="40" y1="0" x2="760" y2="560">'
            '<stop offset="0" stop-color="#fff" stop-opacity="0"/><stop offset=".16" stop-color="#fff" stop-opacity=".20"/>'
            '<stop offset=".30" stop-color="#fff" stop-opacity="0"/><stop offset=".46" stop-color="#fff" stop-opacity="0"/>'
            '<stop offset=".58" stop-color="#fff" stop-opacity=".12"/><stop offset=".68" stop-color="#fff" stop-opacity="0"/>'
            '<stop offset="1" stop-color="#fff" stop-opacity="0"/></linearGradient>'
            f'<clipPath id="plate"><path d="{PLATE_D}"/></clipPath>{prism_defs()}')


def prism_defs():
    """One gradient per facet, running across it so the light and deep edges meet the neighbours."""
    dirs = {"tl": (0, 0, 1, 1), "tr": (0, 0, 1, 1), "bl": (0, 0, 1, 1), "br": (0, 0, 1, 1)}
    return "".join(
        f'<linearGradient id="f{k}" x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}"><stop offset="0" stop-color="{a}"/>'
        f'<stop offset="1" stop-color="{b}"/></linearGradient>'
        for k, ((a, b), (x1, y1, x2, y2)) in ((k, (FACETS[k], dirs[k])) for k in FACETS))


def prism(mode):
    """A plumbob: two pyramids base to base, the front vertex a little below the middle so the facets read as 3D."""
    p = PRISM

    def poly(*keys):
        return " ".join(f"{p[k][0]},{p[k][1]}" for k in keys)
    return (f'<polygon points="{poly("top", "r", "bot", "l")}" fill="{SETTING[mode]}" stroke="{SETTING[mode]}" '
            f'stroke-width="26" stroke-linejoin="round"/>'
            f'<polygon points="{poly("top", "l", "f")}" fill="url(#ftl)"/>'
            f'<polygon points="{poly("top", "r", "f")}" fill="url(#ftr)"/>'
            f'<polygon points="{poly("l", "f", "bot")}" fill="url(#fbl)"/>'
            f'<polygon points="{poly("r", "f", "bot")}" fill="url(#fbr)"/>'
            # a thin glint along the upper left edge
            f'<polygon points="512,346 480,424 489,426 512,372" fill="#fff" opacity=".55"/>')


def helm(mode):
    edge = EDGE[mode]
    return (f'<path d="{PLATE_D}" fill="url(#steel)"/>'
            '<g clip-path="url(#plate)">'
            '<rect x="-20" y="-20" width="1064" height="900" fill="url(#sheen)"/>'
            '<rect x="-20" y="-20" width="532" height="900" fill="#fff" opacity=".05"/></g>'
            f'<path d="{PLATE_D}" fill="none" stroke="{edge}" stroke-width="6" stroke-linejoin="round"/>'
            f'<path d="{LINES_D}" fill="none" stroke="{edge}" stroke-width="6" stroke-linecap="round"/>'
            f'{prism(mode)}')


def svg(mode, paths):
    p = gen.palette(PRIMARY, mode)
    eyes = "".join(f'<path d="{d}" fill="#FFFFFF"/>' for d in paths)  # white in both modes under the dark helm
    return g.wrap(f'{fl.skin_defs(p)[:-7]}{helm_defs(mode)}</defs><clipPath id="tclip"><path d="{g.TILE}"/></clipPath>'
                  f'<path d="{g.TILE}" fill="url(#skin)"/><g clip-path="url(#tclip)">{eyes}{helm(mode)}{fl.details(p, "focinho")}</g>')


def build():
    SVG.mkdir(exist_ok=True)
    paths = fl.eye_paths(z.shipped_eyes())
    for mode in ("light", "dark"):
        (SVG / f"fantasy_{mode}.svg").write_text(svg(mode, paths))
    big = ""
    for mode, label in (("light", "claro"), ("dark", "escuro")):
        big += (f'<div class="row"><figure><img src="reference/shipped_fantasy_{mode}.png"><figcaption>Atual ({label})</figcaption></figure>'
                f'<span class="arrow">→</span><figure><img src="svg/fantasy_v3_{mode}.svg"><figcaption>Elmo v3, cubo ({label})</figcaption></figure>'
                f'<span class="arrow">→</span><figure><img src="svg/fantasy_{mode}.svg"><figcaption>Elmo v4, prisma ({label})</figcaption></figure></div>')
    small = "".join(
        f'<div class="sm"><span>{label}</span>' + "".join(f'<img src="svg/fantasy_{mode}.svg" style="width:{s}px;height:{s}px">' for s in (144, 96, 72, 48)) + "</div>"
        for mode, label in (("light", "Sombrio claro"), ("dark", "Sombrio escuro")))
    html = f"""<!doctype html><meta charset="utf-8"><title>Fantasia</title>
<style>
body{{margin:0;padding:28px;background:#0b0b0d;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:24px 0 12px}} .row{{display:flex;gap:10px;align-items:center;margin-bottom:16px}}
figure{{margin:0;padding:10px;border-radius:16px;background:#000;text-align:center}} figure img{{width:300px;height:300px;display:block;border-radius:22%}}
figcaption{{margin-top:8px;font-size:12px;opacity:.85}} .arrow{{font-size:22px;opacity:.5}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin-bottom:12px}} .sm span{{width:110px;font-size:13px;opacity:.75}} .sm img{{border-radius:22%}}
</style>
<h2>Fantasia: do ícone atual ao elmo sobre a pele</h2>{big}<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{small}"""
    (HERE / "fantasy_preview.html").write_text(html)


def render():
    PNG.mkdir(exist_ok=True)
    for mode in ("light", "dark"):
        name = f"fantasy_{mode}"
        subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                        "--window-size=1024,1024", f"--screenshot={PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
                       check=True, capture_output=True)
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1150,1350",
                    f"--screenshot={HERE / 'fantasy_preview.png'}", f"file://{HERE / 'fantasy_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
