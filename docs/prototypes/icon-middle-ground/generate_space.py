#!/usr/bin/env python3
"""Round 17 of the icon study: the Space Opera variant, three options side by side for the user to weigh.

The skin and nostrils are the approved base in Space Opera's primary colour (`generate_genres.py`). The shipped idea
(antennae with stars) grew out of the silhouette and does not fit a full-zoom tile, so:

  A. VR visor: a rounded headset shell over the eyes with a strap, its dark glass holding a nebula and stars. Hides the
     eyes, like the Crime glasses.
  B. Astronaut helmet (the user's pick): the Shinobi idea with hard parts. A plain white shell covers the tile around a
     large glass opening, and the whole face stays visible behind the glass.
  C. Galaxy in the eyes: no accessory. Each eye is a window on a small spiral galaxy.

Colours come from the genre palette: #000510 #001219 #004E63 #0081A7 #00AFB9 #48CAE4 #90E0EF #C9B1FF.

    python3 generate_space.py          # writes svg/space_opera_{a,b,c}_*.svg and space_preview.html
    python3 generate_space.py --png    # also rasterises with headless Chrome
"""
import math
import random
import subprocess
import sys

import generate as g
import generate_flat as fl
import generate_genres as gen
import generate_zoom as z

HERE, SVG, PNG = g.HERE, g.SVG, g.PNG
PRIMARY = "#0081A7"
OPTIONS = (("a", "A, visor VR"), ("b", "B, capacete"), ("c", "C, galáxia nos olhos"))


def sparkle(x, y, r):
    """A four-pointed star."""
    k = r * .16
    return (f"M{x},{y - r} L{x + k},{y - k} L{x + r},{y} L{x + k},{y + k} "
            f"L{x},{y + r} L{x - k},{y + k} L{x - r},{y} L{x - k},{y - k} Z")


def stars(x0, x1, y0, y1, count, seed):
    """A fixed scatter of small dots, with the same seed giving the same sky on every run."""
    rng = random.Random(seed)
    out = ""
    for _ in range(count):
        out += f'<circle cx="{rng.uniform(x0, x1):.0f}" cy="{rng.uniform(y0, y1):.0f}" r="{rng.choice((3, 4, 4, 6)):d}" fill="#fff" fill-opacity="{rng.uniform(.6, 1):.2f}"/>'
    return out


# A: VR visor

VISOR = ("M214,424 H810 C884,424 928,474 928,556 V660 C928,738 876,786 804,786 H684 C640,786 620,750 596,712 "
         "Q512,640 428,712 C404,750 384,786 340,786 H220 C148,786 96,738 96,660 V556 C96,474 140,424 214,424 Z")


def visor_defs(mode):
    shell = ("#F4F8FA", "#9DB2BE") if mode == "light" else ("#C9D6DD", "#6F8491")
    return (f'<linearGradient id="shell" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{shell[0]}"/><stop offset="1" stop-color="{shell[1]}"/></linearGradient>'
            '<linearGradient id="glass" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#000510"/><stop offset=".55" stop-color="#14104A"/><stop offset="1" stop-color="#004E63"/></linearGradient>'
            '<radialGradient id="nebV" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="#C9B1FF" stop-opacity=".75"/><stop offset="1" stop-color="#C9B1FF" stop-opacity="0"/></radialGradient>'
            '<radialGradient id="nebC" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="#48CAE4" stop-opacity=".7"/><stop offset="1" stop-color="#48CAE4" stop-opacity="0"/></radialGradient>'
            '<linearGradient id="streak" gradientUnits="userSpaceOnUse" x1="150" y1="430" x2="470" y2="790"><stop offset=".2" stop-color="#fff" stop-opacity="0"/><stop offset=".3" stop-color="#fff" stop-opacity=".32"/><stop offset=".4" stop-color="#fff" stop-opacity="0"/></linearGradient>'
            f'<clipPath id="visor"><path d="{VISOR}"/></clipPath>')


def option_a(mode, p, eyes):
    strap = '<rect x="-20" y="530" width="1064" height="150" fill="#14232C"/>'
    glass = (f'<path d="{VISOR}" fill="url(#glass)"/><g clip-path="url(#visor)">'
             '<ellipse cx="340" cy="560" rx="260" ry="130" fill="url(#nebV)" transform="rotate(-18 340 560)"/>'
             '<ellipse cx="720" cy="660" rx="280" ry="130" fill="url(#nebC)" transform="rotate(-18 720 660)"/>'
             f'{stars(110, 915, 440, 770, 26, 7)}'
             f'<path d="{sparkle(690, 520, 40)}" fill="#fff"/><path d="{sparkle(258, 668, 26)}" fill="#fff"/>'
             '<rect x="90" y="420" width="844" height="370" fill="url(#streak)"/></g>')
    shell = f'<path d="{VISOR}" fill="none" stroke="url(#shell)" stroke-width="40" stroke-linejoin="round"/>'
    return strap + glass + shell + fl.details(p, "focinho")


# B: astronaut helmet

OPENING = ("M512,352 C720,352 892,384 938,508 C966,596 958,722 898,804 C838,872 680,884 512,884 "
           "C344,884 186,872 126,804 C66,722 58,596 86,508 C132,384 304,352 512,352 Z")


def helmet_defs(mode):
    # In dark mode the shell is a closed grey: a near-white helmet was the only bright icon in the dark pack.
    shell = ("#FBFDFE", "#AEBFC9") if mode == "light" else ("#7F929E", "#3F4E58")
    rim = ("#6C7E8A", "#42535E") if mode == "light" else ("#2F3D46", "#1C262D")
    return (f'<linearGradient id="shell" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{shell[0]}"/><stop offset="1" stop-color="{shell[1]}"/></linearGradient>'
            f'<linearGradient id="rim" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{rim[0]}"/><stop offset="1" stop-color="{rim[1]}"/></linearGradient>'
            '<linearGradient id="sheen" gradientUnits="userSpaceOnUse" x1="120" y1="360" x2="560" y2="880"><stop offset=".12" stop-color="#fff" stop-opacity="0"/><stop offset=".22" stop-color="#fff" stop-opacity=".3"/><stop offset=".34" stop-color="#fff" stop-opacity=".06"/><stop offset=".4" stop-color="#fff" stop-opacity=".22"/><stop offset=".47" stop-color="#fff" stop-opacity="0"/></linearGradient>'
            '<radialGradient id="nebV" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="#C9B1FF" stop-opacity=".9"/><stop offset="1" stop-color="#C9B1FF" stop-opacity="0"/></radialGradient>'
            '<radialGradient id="nebC" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="#90E0EF" stop-opacity=".8"/><stop offset="1" stop-color="#90E0EF" stop-opacity="0"/></radialGradient>'
            f'<clipPath id="opening"><path d="{OPENING}"/></clipPath>')


def reflection():
    """The sky mirrored in the glass, subtle: the nebula and stars of option A, over the face and at low opacity."""
    return ('<g opacity=".5">'
            '<ellipse cx="300" cy="450" rx="290" ry="120" fill="url(#nebV)" transform="rotate(-18 300 450)"/>'
            '<ellipse cx="770" cy="780" rx="260" ry="110" fill="url(#nebC)" transform="rotate(-18 770 780)"/>'
            # the stars stay off the middle of the face, where they read as freckles
            f'{stars(110, 910, 385, 490, 12, 7)}{stars(130, 400, 745, 850, 4, 5)}{stars(630, 890, 745, 850, 4, 9)}'
            f'<path d="{sparkle(700, 440, 30)}" fill="#fff"/><path d="{sparkle(220, 790, 20)}" fill="#fff"/></g>')


def option_b(mode, p, eyes):
    shell = f'<path fill-rule="evenodd" d="M-20,-20 H1044 V1044 H-20 Z {OPENING}" fill="url(#shell)"/>'
    vents = (f'<g fill="{"#3E505B" if mode == "light" else "#1C262D"}">' +"".join(f'<rect x="{x}" y="930" width="70" height="20" rx="10"/>' for x in (377, 477, 577)) + '</g>')
    rim = f'<path d="{OPENING}" fill="none" stroke="url(#rim)" stroke-width="40"/>'
    glass = f'<g clip-path="url(#opening)">{reflection()}<rect x="60" y="340" width="904" height="550" fill="url(#sheen)"/></g>'
    return eyes + fl.details(p, "focinho") + glass + shell + rim + vents


# C: galaxy in the eyes

def spiral_arm(cx, cy, start, r_max, turns=1.15):
    pts = []
    for i in range(41):
        t = i / 40
        a = start + t * turns * 2 * math.pi
        r = 8 + (r_max - 8) * t
        pts.append(f"{cx + r * math.cos(a) * 1.15:.1f},{cy + r * math.sin(a) * .8:.1f}")
    return "M" + " L".join(pts)


GALAXY_C = (250, 592)


def galaxy_defs(paths):
    return ('<radialGradient id="space" gradientUnits="userSpaceOnUse" cx="250" cy="592" r="170"><stop offset="0" stop-color="#3B2A8C"/><stop offset=".55" stop-color="#14205E"/><stop offset="1" stop-color="#000510"/></radialGradient>'
            '<radialGradient id="core" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="#fff"/><stop offset=".35" stop-color="#E8DCFF" stop-opacity=".9"/><stop offset="1" stop-color="#C9B1FF" stop-opacity="0"/></radialGradient>'
            '<filter id="soft" x="-20%" y="-20%" width="140%" height="140%"><feGaussianBlur stdDeviation="5"/></filter>'
            + f'<clipPath id="eyeL"><path d="{paths[0]}"/></clipPath><clipPath id="eyeR"><path d="{paths[1]}"/></clipPath>')


def galaxy(seed):
    """Drawn for the left eye. The arms are soft so they read as light; the stars and core stay sharp."""
    cx, cy = GALAXY_C
    arms = (f'<g fill="none" stroke-linecap="round" filter="url(#soft)">'
            f'<path d="{spiral_arm(cx, cy, 0, 120)}" stroke="#C9B1FF" stroke-width="22" stroke-opacity=".9"/>'
            f'<path d="{spiral_arm(cx, cy, math.pi, 120)}" stroke="#48CAE4" stroke-width="22" stroke-opacity=".9"/></g>')
    return (f'<rect x="120" y="470" width="300" height="290" fill="url(#space)"/>{arms}'
            f'{stars(150, 385, 500, 725, 18, seed)}'
            f'<ellipse cx="{cx}" cy="{cy}" rx="54" ry="42" fill="url(#core)"/>'
            f'<path d="{sparkle(330, 652, 22)}" fill="#fff"/>')


def option_c(mode, p, paths):
    rim = ""
    if mode == "dark":  # a dark galaxy on the dark skin loses the shape of the eye: a thin light edge keeps it
        rim = "".join(f'<path d="{d}" fill="none" stroke="#90E0EF" stroke-opacity=".85" stroke-width="8"/>' for d in paths)
    return (f'<g clip-path="url(#eyeL)">{galaxy(3)}</g>'
            f'<g clip-path="url(#eyeR)"><g transform="translate(1024,0) scale(-1,1)">{galaxy(11)}</g></g>'
            f'{rim}{fl.details(p, "focinho")}')


def svg(option, mode, paths):
    p = gen.palette(PRIMARY, mode)
    white = "".join(f'<path d="{d}" fill="#FFFFFF"/>' for d in paths)
    defs = {"a": lambda: visor_defs(mode), "b": lambda: helmet_defs(mode), "c": lambda: galaxy_defs(paths)}[option]()
    body = {"a": lambda: option_a(mode, p, white), "b": lambda: option_b(mode, p, white), "c": lambda: option_c(mode, p, paths)}[option]()
    return g.wrap(f'{fl.skin_defs(p)[:-7]}{defs}</defs><clipPath id="tclip"><path d="{g.TILE}"/></clipPath>'
                  f'<path d="{g.TILE}" fill="url(#skin)"/><g clip-path="url(#tclip)">{body}</g>')


def build():
    SVG.mkdir(exist_ok=True)
    paths = fl.eye_paths(z.shipped_eyes())
    for key, _ in OPTIONS:
        for mode in ("light", "dark"):
            (SVG / f"space_opera_{key}_{mode}.svg").write_text(svg(key, mode, paths))
    big = ""
    for mode, label in (("light", "claro"), ("dark", "escuro")):
        big += '<div class="row">' + "".join(
            f'<figure><img src="svg/space_opera_{key}_{mode}.svg"><figcaption>{name} ({label})</figcaption></figure>' for key, name in OPTIONS) + "</div>"
    small = "".join(
        f'<div class="sm"><span>{name}</span>' + "".join(
            f'<img src="svg/space_opera_{key}_{mode}.svg" style="width:{s}px;height:{s}px">' for mode in ("light", "dark") for s in (96, 72, 48)) + "</div>"
        for key, name in OPTIONS)
    html = f"""<!doctype html><meta charset="utf-8"><title>Space Opera</title>
<style>
body{{margin:0;padding:28px;background:#0b0b0d;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:24px 0 12px}} .row{{display:flex;gap:10px;align-items:center;margin-bottom:16px}}
figure{{margin:0;padding:10px;border-radius:16px;background:#000;text-align:center}} figure img{{width:300px;height:300px;display:block;border-radius:22%}}
figcaption{{margin-top:8px;font-size:12px;opacity:.85}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin-bottom:12px}} .sm span{{width:140px;font-size:13px;opacity:.75}} .sm img{{border-radius:22%}}
</style>
<h2>Space Opera: três caminhos</h2>{big}<h2>Legibilidade em 96, 72 e 48 px (claro, depois escuro)</h2>{small}"""
    (HERE / "space_preview.html").write_text(html)


def render():
    PNG.mkdir(exist_ok=True)
    for key, _ in OPTIONS:
        for mode in ("light", "dark"):
            name = f"space_opera_{key}_{mode}"
            subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                            "--window-size=1024,1024", f"--screenshot={PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
                           check=True, capture_output=True)
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1100,1240",
                    f"--screenshot={HERE / 'space_preview.png'}", f"file://{HERE / 'space_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
