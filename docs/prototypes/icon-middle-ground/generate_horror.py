#!/usr/bin/env python3
"""Round 13 of the icon study: the Horror (Terror) variant, a dragon skeleton.

The skin, eyes and nostrils are the approved base in Horror's primary colour (`generate_genres.py`), a cold grey-blue
that already reads as bone. The user's idea: make Horror more of a dragon skeleton. Not a drawn skull on top of the face
but the same few parts turned into bone features:

  - eye sockets: each eye gets a dark halo shaped like itself, so the white eye glows from an empty socket
  - a nasal cavity: the two nostril drops, bigger and black
  - fangs: five pointed teeth rising from the bottom edge of the tile, in bone white (the fangs are what say "dragon"
    and not "human skull")
  - one crack in the forehead, a thin angular line from the top edge (the shipped icon already has one)

The shipped drips are left out: they say "monster", not "skeleton".

    python3 generate_horror.py          # writes svg/horror_*.svg and horror_preview.html
    python3 generate_horror.py --png    # also rasterises with headless Chrome
"""
import subprocess
import sys

import generate as g
import generate_flat as fl
import generate_genres as gen
import generate_zoom as z

HERE, SVG, PNG = g.HERE, g.SVG, g.PNG
PRIMARY = "#748B97"
INK = "#0A141A"
BONE = "#EAF6F7"
HX = 512
EYE_CY = 614  # the middle of the eyes, to transform them round their own centre
# A skeleton's orbit is sharper than the living eye: a pointed almond with a point at both ends, drawn along its own
# axis (x runs from the outer-top tip to the inner-bottom tip), fuller on the upper side like the base eye, and placed
# by a rotation (45 degrees on the left, 135 and a flip on the right).
ALMOND = "M-128,0 C-58,-98 64,-70 128,0 C58,58 -64,64 -128,0 Z"


def jaw_y(x):
    """The top edge of the mouth: a wide grin, higher at the corners and lowest in the middle."""
    t = (x + 20) / 1064
    return 836 + 120 * t * (1 - t)


def jaw(mode):
    """The user found the teeth crude (v1 fangs, v2 grin) and asked for contours that only suggest the mandible, like the
    jaw line in the dragon-skull references. Same language as the crack: thin angular-curved ink strokes, no fills.
      - the jaw line: a wide curve across the bottom, the edge between the upper skull and the mandible
      - the chin: a second, shorter curve under it, the lower edge of the mandible
      - the hinges: a short stroke at each side corner rising towards the eye, where the jaw joins the skull"""
    line = f'M-20,{jaw_y(-20):.0f} Q512,956 1044,{jaw_y(1044):.0f}'
    chin = 'M170,934 Q512,1034 854,934'
    hinge = 'M96,862 L150,790 M928,862 L874,790'
    ink = INK if mode == "light" else "#A9BDC6"  # on the dark skin the near-black ink vanishes: use a light cool grey
    return (f'<g fill="none" stroke="{ink}" stroke-linecap="round" stroke-linejoin="round">'
            f'<path d="{line}" stroke-opacity=".78" stroke-width="14"/>'
            f'<path d="{chin}" stroke-opacity=".5" stroke-width="12"/>'
            f'<path d="{hinge}" stroke-opacity=".6" stroke-width="12"/></g>')


def crack():
    """A thin angular crack in the forehead, entering from the top edge, with one short branch."""
    return (f'<path d="M548,-20 L522,110 L566,196 L530,296 L552,378 M566,196 L616,226" fill="none" stroke="{INK}" '
            f'stroke-opacity=".7" stroke-width="12" stroke-linejoin="round" stroke-linecap="round"/>')


RIM = {"light": "#627886", "dark": "#7C919C"}  # the lit wall of a socket: a mid grey between the skin and the ink
# With no white in the eyes the black sockets vanish on the usual dark skin, so Horror's dark mode stays lighter.
DARK_SKIN = ("#8AA2AE", "#5E7480", "#36464F")


def nasal_cavity(mode):
    """The nostril drops, scaled up, as deep cavities like the nose of the skull reference: an ink outline, a grey inner
    wall, and a black hollow pushed to the top so a crescent of wall shows underneath."""
    out = ""
    for k in (-1, 1):
        cx, cy = HX + k * 58, 790
        out += (f'<g transform="translate({cx},{cy}) scale(1.42) translate({-cx},{-cy})">'
                f'<path d="{fl.drop(k)}" fill="{RIM[mode]}" stroke="{INK}" stroke-width="9" stroke-linejoin="round"/></g>'
                f'<g transform="translate({cx},{cy - 9}) scale(1.08) translate({-cx},{-cy})">'
                f'<path d="{fl.drop(k)}" fill="{INK}"/></g>')
    return out


def svg(mode, paths):
    p = gen.palette(PRIMARY, mode)
    if mode == "dark":
        p = {**p, "skin": DARK_SKIN}
    # A skeleton's orbit is sharper than a living eye: both the socket and the white eye are stretched along the
    # outer-top to inner-bottom axis (45 degrees on the left, 135 on the right) and narrowed across it, which sharpens
    # the almond. The white eye is 90% of the socket, so the socket reads as a cavity with a little light in it.
    ends = ((270, 45, ""), (754, 135, " scale(1,-1)"))
    sockets = "".join(
        f'<g transform="translate({cx},{EYE_CY}) rotate({deg}){flip}">'
        f'<path d="{ALMOND}" fill="{RIM[mode]}" stroke="{INK}" stroke-width="26" stroke-linejoin="miter"/>'
        f'<path d="{ALMOND}" transform="translate(-6,-22) scale(.72)" fill="{INK}"/></g>' for cx, deg, flip in ends)
    eyes = ""
    return g.wrap(f'{fl.skin_defs(p)[:-7]}</defs><clipPath id="tclip"><path d="{g.TILE}"/></clipPath>'
                  f'<path d="{g.TILE}" fill="url(#skin)"/><g clip-path="url(#tclip)">{crack()}{sockets}{eyes}{nasal_cavity(mode)}</g>')


def build():
    SVG.mkdir(exist_ok=True)
    paths = fl.eye_paths(z.shipped_eyes())
    for mode in ("light", "dark"):
        (SVG / f"horror_{mode}.svg").write_text(svg(mode, paths))
    big = ""
    for mode, label in (("light", "claro"), ("dark", "escuro")):
        big += (f'<div class="row"><figure><img src="reference/shipped_horror_{mode}.png"><figcaption>Atual ({label})</figcaption></figure>'
                f'<span class="arrow">→</span><figure><img src="svg/horror_v3_{mode}.svg"><figcaption>v3, mandíbula ({label})</figcaption></figure>'
                f'<span class="arrow">→</span><figure><img src="svg/horror_{mode}.svg"><figcaption>v4, sem mandíbula ({label})</figcaption></figure></div>')
    small = "".join(
        f'<div class="sm"><span>{label}</span>' + "".join(f'<img src="svg/horror_{mode}.svg" style="width:{s}px;height:{s}px">' for s in (144, 96, 72, 48)) + "</div>"
        for mode, label in (("light", "Terror claro"), ("dark", "Terror escuro")))
    html = f"""<!doctype html><meta charset="utf-8"><title>Terror</title>
<style>
body{{margin:0;padding:28px;background:#0b0b0d;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:24px 0 12px}} .row{{display:flex;gap:10px;align-items:center;margin-bottom:16px}}
figure{{margin:0;padding:10px;border-radius:16px;background:#000;text-align:center}} figure img{{width:300px;height:300px;display:block;border-radius:22%}}
figcaption{{margin-top:8px;font-size:12px;opacity:.85}} .arrow{{font-size:22px;opacity:.5}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin-bottom:12px}} .sm span{{width:110px;font-size:13px;opacity:.75}} .sm img{{border-radius:22%}}
</style>
<h2>Terror: do ícone atual ao esqueleto de dragão</h2>{big}<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{small}"""
    (HERE / "horror_preview.html").write_text(html)


def render():
    PNG.mkdir(exist_ok=True)
    for mode in ("light", "dark"):
        name = f"horror_{mode}"
        subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                        "--window-size=1024,1024", f"--screenshot={PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
                       check=True, capture_output=True)
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1150,1350",
                    f"--screenshot={HERE / 'horror_preview.png'}", f"file://{HERE / 'horror_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
