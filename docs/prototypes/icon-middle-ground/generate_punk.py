#!/usr/bin/env python3
"""Round 18 of the icon study: the Punk Rock variant, piercings, punk make-up and furious eyes.

The skin, eyes and nostrils are the approved base in Punk Rock's primary colour (`generate_genres.py`). The shipped mohawk
grew out of the silhouette and does not fit a full-zoom tile. The user's direction is no hair at all:

  - make-up: graphic eyeliner from the user's reference, one continuous shape round each eye: a long wing on the upper
    lid and a thick band under the eye that the spikes grow out of, one of them long
  - furious eyes: hand-drawn white eyes with pointed corners and a lid that curves down towards the nose
  - piercing: a steel septum pincher, two spikes meeting in a V, no outline (a ring, a single spike and a safety pin were
    tried and removed)

    python3 generate_punk.py          # writes svg/punk_rock_*.svg and punk_preview.html
    python3 generate_punk.py --png    # also rasterises with headless Chrome
"""
import subprocess
import sys

import generate as g
import generate_flat as fl
import generate_genres as gen

HERE, SVG, PNG = g.HERE, g.SVG, g.PNG
PRIMARY = "#729656"
INK = "#0A0C0F"
# Polished metal: dark and bright bands running along the piece.
STEEL = ('<linearGradient id="steel" x1="0" y1="0" x2="1" y2=".55"><stop offset="0" stop-color="#5C6670"/><stop offset=".28" stop-color="#F4F7FA"/>'
         '<stop offset=".5" stop-color="#7A848E"/><stop offset=".72" stop-color="#E2E7EC"/><stop offset="1" stop-color="#4C555E"/></linearGradient>')

# The left eye, drawn by hand (v4): the user asked for pointed corners and a lid that curves instead of a straight cut. The
# lid runs in an S from the outer point to the inner one: steep, then flatter over the eye, then steep again.
EYE = "M150,540 C195,585 275,590 335,628 C368,650 392,688 408,722 C340,716 250,700 195,655 C165,630 148,590 150,540 Z"
# The liner of the left eye as ONE continuous shape. Clockwise from the upper fine point by the nose: over the lid in the same
# S into the long wing, back in a deep curve round the outer side of the eye, then under it as a thick band that the spikes
# grow out of, with one long drop, ending in a second fine point by the nose.
LINER = ("M458,700 C412,622 330,568 236,538 C150,510 90,500 40,466 Q120,540 110,600 Q92,640 50,684 Q130,652 166,664 "
         "Q164,704 122,784 Q190,724 222,708 Q226,760 214,820 Q252,756 280,740 Q312,792 378,892 Q344,792 332,750 "
         "Q372,762 464,742 Q430,726 458,700 Z")
MIRROR = "translate(1024,0) scale(-1,1)"


def makeup():
    return f'<path d="{LINER}" fill="{INK}"/><path d="{LINER}" fill="{INK}" transform="{MIRROR}"/>'


def eyes():
    return f'<path d="{EYE}" fill="#FFFFFF"/><path d="{EYE}" fill="#FFFFFF" transform="{MIRROR}"/>'


def septum():
    """A septum pincher, from the user's reference: two thick steel horns coming out of the nostrils and curving in, their
    tips almost meeting under the nose. No outline."""
    horn = "M456,790 C452,836 478,864 506,870 C488,854 469,828 468,790 Z"
    glint = "M461,812 C463,834 477,852 492,861"  # a thin bright line along the horn, the reflection on the metal
    one = f'<path d="{horn}" fill="url(#steel)"/><path d="{glint}" fill="none" stroke="#FFFFFF" stroke-width="3.5" stroke-linecap="round"/>'
    return f'{one}<g transform="{MIRROR}">{one}</g>'


def horseshoe():
    """The other reference: a thin ring through the nose, its two ends closed by steel cones that point down."""
    cone = "M462,838 L502,850 L476,902 Z"
    return ('<path d="M484,844 A34,44 0 1 1 540,844" fill="none" stroke="url(#steel)" stroke-width="11"/>'
            f'<path d="{cone}" fill="url(#steel)"/><path d="{cone}" fill="url(#steel)" transform="{MIRROR}"/>')


PIERCINGS = {"": septum, "ring_": horseshoe}


def star(x, y, r):
    """A small four-pointed star with concave sides, like the tattooed ones in the user's reference."""
    c = r * .14
    return (f'<path transform="translate({x},{y}) rotate(14)" fill="{INK}" '
            f'd="M0,{-r} Q{c},{-c} {r},0 Q{c},{c} 0,{r} Q{-c},{c} {-r},0 Q{-c},{-c} 0,{-r} Z"/>')


def stars():
    """One star past the inner end of each liner, towards the nose bridge."""
    one = star(472, 650, 26)
    return f'{one}<g transform="{MIRROR}">{one}</g>'


def svg(mode, piercing=""):
    p = gen.palette(PRIMARY, mode)
    return g.wrap(f'{fl.skin_defs(p)[:-7]}{STEEL}</defs><clipPath id="tclip"><path d="{g.TILE}"/></clipPath>'
                  f'<path d="{g.TILE}" fill="url(#skin)"/><g clip-path="url(#tclip)">{makeup()}{eyes()}{stars()}'
                  f'{PIERCINGS[piercing]()}{fl.details(p, "focinho")}</g>')  # the nostrils go on top: the piercing comes out of them


def build():
    SVG.mkdir(exist_ok=True)
    for mode in ("light", "dark"):
        for piercing in PIERCINGS:
            (SVG / f"punk_rock_{piercing}{mode}.svg").write_text(svg(mode, piercing))
    big = ""
    for mode, label in (("light", "claro"), ("dark", "escuro")):
        big += (f'<div class="row"><figure><img src="svg/punk_rock_{mode}.svg"><figcaption>A, chifres ({label})</figcaption></figure>'
                f'<figure><img src="svg/punk_rock_ring_{mode}.svg"><figcaption>B, argola com cones ({label})</figcaption></figure></div>')
    small = "".join(
        f'<div class="sm"><span>{label}</span>' + "".join(f'<img src="svg/punk_rock_{piercing}{mode}.svg" style="width:{s}px;height:{s}px">' for s in (144, 96, 72, 48)) + "</div>"
        for piercing, mode, label in (("", "light", "A claro"), ("", "dark", "A escuro"), ("ring_", "light", "B claro"), ("ring_", "dark", "B escuro")))
    html = f"""<!doctype html><meta charset="utf-8"><title>Punk Rock</title>
<style>
body{{margin:0;padding:28px;background:#0b0b0d;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:24px 0 12px}} .row{{display:flex;gap:10px;align-items:center;margin-bottom:16px}}
figure{{margin:0;padding:10px;border-radius:16px;background:#000;text-align:center}} figure img{{width:300px;height:300px;display:block;border-radius:22%}}
figcaption{{margin-top:8px;font-size:12px;opacity:.85}} .arrow{{font-size:22px;opacity:.5}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin-bottom:12px}} .sm span{{width:110px;font-size:13px;opacity:.75}} .sm img{{border-radius:22%}}
</style>
<h2>Punk Rock: da base à maquiagem e piercings</h2>{big}<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{small}"""
    (HERE / "punk_preview.html").write_text(html)


def render():
    PNG.mkdir(exist_ok=True)
    for mode in ("light", "dark"):
        name = f"punk_rock_{mode}"
        subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                        "--window-size=1024,1024", f"--screenshot={PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
                       check=True, capture_output=True)
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1150,1600",
                    f"--screenshot={HERE / 'punk_preview.png'}", f"file://{HERE / 'punk_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
