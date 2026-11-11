#!/usr/bin/env python3
"""Round 15 of the icon study: the Cowboy variant, a polka-dot bandana over the lower face.

The skin and eyes are the approved base in Cowboy's primary colour (`generate_genres.py`), eyes white in both modes. The
accessory is one object, like the shipped icon: a red bandana pulled up over the nose and mouth, with cream polka dots.
It covers the nostrils (the same trade as the Shinobi mask) and its upper edge sits just under the eyes. A darker hem
runs along that edge; the dots are a staggered grid clipped to the cloth.

    python3 generate_cowboy.py          # writes svg/cowboy_*.svg and cowboy_preview.html
    python3 generate_cowboy.py --png    # also rasterises with headless Chrome
"""
import subprocess
import sys

import generate as g
import generate_flat as fl
import generate_genres as gen
import generate_zoom as z

HERE, SVG, PNG = g.HERE, g.SVG, g.PNG
PRIMARY = "#8B4513"
# fill, hem, print. The terracotta (the user's pick) comes from the genre palette (#A84520 #FCE8A8 #D4612F #8B3A0F); the
# print is a warm white, as on a real western bandana.
OPTIONS = {"": {"light": ("#D4612F", "#8B3A0F", "#FFF3D6"), "dark": ("#A84520", "#5E2208", "#F7E6B8")}}
# Raised after the review of the whole pack, up to just under the eyes: at launcher sizes the lower version was a thin strip.
EDGE = "M-20,772 Q512,716 1044,772"
BORDER = "M-20,820 Q512,764 1044,820"  # a dotted border following the hem, like the ring of dots on real bandanas
# One paisley: a teardrop with a curled tip, drawn round (0,0), about 70 units tall, with a small dot in its belly.
PAISLEY = "M-2,-34 C30,-20 32,22 4,32 C-24,40 -38,6 -18,-8 C-8,-15 6,-8 0,2"
STEP_X = 170


def paisley_row(colour):
    """The print: two staggered rows of white paisleys alternating tilt, with a small dot between each pair."""
    out = ""
    for row, (y, shift) in enumerate(((868, 0), (962, STEP_X // 2))):
        for i, x in enumerate(range(-40 + shift, 1100, STEP_X)):
            tilt = -24 if (i + row) % 2 else 24
            out += (f'<g transform="translate({x},{y}) rotate({tilt}) scale(1.1)">'
                    f'<path d="{PAISLEY}" fill="none" stroke="{colour}" stroke-width="9" stroke-linecap="round" stroke-linejoin="round"/>'
                    f'<circle cx="6" cy="14" r="7" fill="{colour}"/></g>'
                    f'<circle cx="{x + STEP_X / 2}" cy="{y}" r="9" fill="{colour}"/>')
    return out


def bandana(mode, option=""):
    fill, hem, ink = OPTIONS[option][mode]
    return (f'<clipPath id="cloth"><path d="{EDGE} V1044 H-20 Z"/></clipPath>'
            f'<path d="{EDGE} V1044 H-20 Z" fill="{fill}"/>'
            f'<g clip-path="url(#cloth)">'
            f'<path d="{BORDER}" fill="none" stroke="{ink}" stroke-width="11" stroke-linecap="round" stroke-dasharray="1 24"/>'
            f'{paisley_row(ink)}</g>'
            f'<path d="{EDGE}" fill="none" stroke="{hem}" stroke-width="30" stroke-linecap="round"/>')


def svg(mode, paths, option=""):
    p = gen.palette(PRIMARY, mode)
    eyes = "".join(f'<path d="{d}" fill="#FFFFFF"/>' for d in paths)
    return g.wrap(f'{fl.skin_defs(p)}<clipPath id="tclip"><path d="{g.TILE}"/></clipPath>'
                  f'<path d="{g.TILE}" fill="url(#skin)"/><g clip-path="url(#tclip)">{eyes}{bandana(mode, option)}</g>')


def build():
    SVG.mkdir(exist_ok=True)
    paths = fl.eye_paths(z.shipped_eyes())
    for mode in ("light", "dark"):
        for option in OPTIONS:
            (SVG / f"cowboy_{option}{mode}.svg").write_text(svg(mode, paths, option))
    big = ""
    for mode, label in (("light", "claro"), ("dark", "escuro")):
        big += (f'<div class="row"><figure><img src="svg/genre_cowboy_{mode}.svg"><figcaption>Base ({label})</figcaption></figure>'
                f'<span class="arrow">→</span><figure><img src="svg/cowboy_{mode}.svg"><figcaption>Terracota, paisley ({label})</figcaption></figure></div>')
    small = "".join(
        f'<div class="sm"><span>{label}</span>' + "".join(f'<img src="svg/cowboy_{mode}.svg" style="width:{s}px;height:{s}px">' for s in (144, 96, 72, 48)) + "</div>"
        for mode, label in (("light", "Cowboy claro"), ("dark", "Cowboy escuro")))
    html = f"""<!doctype html><meta charset="utf-8"><title>Cowboy</title>
<style>
body{{margin:0;padding:28px;background:#0b0b0d;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:24px 0 12px}} .row{{display:flex;gap:10px;align-items:center;margin-bottom:16px}}
figure{{margin:0;padding:10px;border-radius:16px;background:#000;text-align:center}} figure img{{width:300px;height:300px;display:block;border-radius:22%}}
figcaption{{margin-top:8px;font-size:12px;opacity:.85}} .arrow{{font-size:22px;opacity:.5}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin-bottom:12px}} .sm span{{width:110px;font-size:13px;opacity:.75}} .sm img{{border-radius:22%}}
</style>
<h2>Cowboy: da base à bandana de bolinhas</h2>{big}<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{small}"""
    (HERE / "cowboy_preview.html").write_text(html)


def render():
    PNG.mkdir(exist_ok=True)
    for mode in ("light", "dark"):
        name = f"cowboy_{mode}"
        subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                        "--window-size=1024,1024", f"--screenshot={PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
                       check=True, capture_output=True)
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1150,1100",
                    f"--screenshot={HERE / 'cowboy_preview.png'}", f"file://{HERE / 'cowboy_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
