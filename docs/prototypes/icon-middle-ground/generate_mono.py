#!/usr/bin/env python3
"""The monochrome (themed icon) layer of the base and the nine genre variants, for review before anything is exported.

A themed icon is one tint with no gradients, so each design is redrawn as a mask from its own shapes: white paints the
tint, black cuts a hole. Colour details become holes (Fantasy's prism, the Heroes' eyes) or are dropped (the helmet's
reflection). The preview also shows the full-colour icons in a circle: an adaptive icon shows 72dp of its 108dp canvas,
and a circular launcher mask touches the tile's edges, so this is what the corners lose.

    python3 generate_mono.py        # writes svg/mono_*.svg, mono_preview.html and mono_preview.png
"""
import subprocess

import generate as g
import generate_cowboy as cowboy
import generate_crime as crime
import generate_cyberpunk as cyber
import generate_fantasy as fantasy
import generate_flat as fl
import generate_heroes as heroes
import generate_horror as horror
import generate_punk as punk
import generate_shinobi as shinobi
import generate_space as space
import generate_zoom as z

HERE, SVG = g.HERE, g.SVG
W, B = "#fff", "#000"
MIRROR = "translate(1024,0) scale(-1,1)"
NOSTRILS = f'<path d="{fl.drop(-1)} {fl.drop(1)}" fill="{W}"/>'
THEMES = {"light": ("#DCE3F7", "#27367A"), "dark": ("#1E2840", "#C8D4FF"), "plan": (None, "#F2F8FF")}  # launcher-style tile and tint
DEBUG_RES = "../../../app/src/debug/res/drawable-nodpi"  # today's blueprint ground for the debug/hmg build


def white_eyes(paths):
    return "".join(f'<path d="{d}" fill="{W}"/>' for d in paths)


def base(paths):
    return white_eyes(paths) + NOSTRILS


def m_fantasy(paths):
    p = fantasy.PRISM
    prism = " ".join(f"{p[k][0]},{p[k][1]}" for k in ("top", "r", "bot", "l"))
    return (white_eyes(paths) + NOSTRILS +
            f'<path d="{fantasy.PLATE_D}" fill="{B}" stroke="{B}" stroke-width="44"/><path d="{fantasy.PLATE_D}" fill="{W}"/>'
            f'<polygon points="{prism}" fill="{B}"/><path d="{fantasy.LINES_D}" fill="none" stroke="{B}" stroke-width="12"/>')


def m_crime(paths):
    def palm(mirror):
        return crime.palm(mirror).replace(crime.PALM, W)
    left, right = crime.lens(False), crime.lens(True)
    return (f'<g fill="none" stroke="{W}" stroke-linejoin="round" stroke-linecap="round">'
            f'<path d="{left}" stroke-width="34"/><path d="{right}" stroke-width="34"/>'
            f'<path d="{crime.brow(False)}" stroke-width="62"/><path d="{crime.brow(True)}" stroke-width="62"/>'
            '<path d="M98,490 L-120,432 M926,490 L1144,432" stroke-width="30"/><path d="M440,578 Q512,548 584,578" stroke-width="30"/></g>'
            f'<clipPath id="ml"><path d="{left}"/></clipPath><clipPath id="mr"><path d="{right}"/></clipPath>'
            f'<g clip-path="url(#ml)">{palm(False)}</g><g clip-path="url(#mr)">{palm(True)}</g>' + NOSTRILS)


def m_cyberpunk(paths):
    gaps = "".join(f'<path d="{d}" fill="{B}" stroke="{B}" stroke-width="28"/>' for d in paths)
    return (f'<g fill="none" stroke="{W}" stroke-width="16" stroke-linecap="round"><path d="{cyber.eye_lines()}"/><path d="{cyber.bridge_trace()}"/></g>'
            f'{gaps}{white_eyes(paths)}<path d="{cyber.bridge()}" fill="{B}" stroke="{B}" stroke-width="24"/><path d="{cyber.bridge()}" fill="{W}"/>' + NOSTRILS)


def m_horror(paths):
    out = horror.crack().replace(horror.INK, W).replace('stroke-opacity=".7"', "")
    for cx, deg, flip in ((270, 45, ""), (754, 135, " scale(1,-1)")):
        out += (f'<g transform="translate({cx},{horror.EYE_CY}) rotate({deg}){flip}">'
                f'<path d="{horror.ALMOND}" fill="{W}" stroke="{W}" stroke-width="26"/>'
                f'<path d="{horror.ALMOND}" fill="{B}" transform="scale(.92)"/>'
                f'<path d="{horror.ALMOND}" fill="{W}" transform="translate(-6,-22) scale(.72)"/></g>')
    return out + NOSTRILS


def m_shinobi(paths):
    seams = ("M-20,372 Q512,488 1044,372", "M-20,832 Q512,730 1044,832")
    dashes = "".join(f'<path d="{d}" fill="none" stroke="{B}" stroke-width="10" stroke-dasharray="26 18"/>' for d in seams)
    return (white_eyes(paths) + f'<path d="{shinobi.HOOD_EDGE} V-20 H-20 Z" fill="{W}"/>'
            f'<path d="{shinobi.MASK_EDGE} V1044 H-20 Z" fill="{W}"/>{dashes}')


def m_cowboy(paths):
    return (white_eyes(paths) + f'<path d="{cowboy.EDGE} V1044 H-20 Z" fill="{W}"/>'
            f'<clipPath id="mc"><path d="{cowboy.EDGE} V1044 H-20 Z"/></clipPath><g clip-path="url(#mc)">'
            f'<path d="{cowboy.BORDER}" fill="none" stroke="{B}" stroke-width="11" stroke-linecap="round" stroke-dasharray="1 24"/>'
            f'{cowboy.paisley_row(B)}</g>')


def m_heroes(paths):
    eyes = ""
    for d, cx, tilt in zip(paths, heroes.EYE_CX, (heroes.TILT, -heroes.TILT)):
        eyes += (f'<g transform="translate({cx},{heroes.EYE_CY}) rotate({tilt}) scale(1,{heroes.SLIT}) translate({-cx},{-heroes.EYE_CY})">'
                 f'<path d="{d}" fill="{B}"/></g>')
    return f'{NOSTRILS}<path d="{heroes.HALF}" fill="{W}"/><path d="{heroes.HALF}" fill="{W}" transform="{MIRROR}"/>{eyes}'


def m_space(paths):
    vents = "".join(f'<rect x="{x}" y="930" width="70" height="20" rx="10" fill="{B}"/>' for x in (377, 477, 577))
    return (white_eyes(paths) + NOSTRILS +
            f'<path fill-rule="evenodd" d="M-20,-20 H1044 V1044 H-20 Z {space.OPENING}" fill="{W}"/>{vents}'
            f'<path d="{space.sparkle(700, 440, 30)}" fill="{W}"/><path d="{space.sparkle(220, 790, 20)}" fill="{W}"/>')


def m_punk(paths):
    one = punk.star(472, 650, 26).replace(punk.INK, W)
    return (f'<path d="{punk.LINER}" fill="{W}"/><path d="{punk.LINER}" fill="{W}" transform="{MIRROR}"/>'
            f'<path d="{punk.EYE}" fill="{B}"/><path d="{punk.EYE}" fill="{B}" transform="{MIRROR}"/>'
            f'{one}<g transform="{MIRROR}">{one}</g>{punk.septum().replace("url(#steel)", W)}{NOSTRILS}')


PACK = (("base", "Base", "ic_dragon", base), ("fantasy", "Fantasia", "fantasy", m_fantasy), ("crime", "Crime", "crime", m_crime),
        ("cyberpunk", "Cyberpunk", "cyberpunk", m_cyberpunk), ("horror", "Terror", "horror", m_horror),
        ("shinobi", "Shinobi", "shinobi", m_shinobi), ("cowboy", "Cowboy", "cowboy", m_cowboy), ("heroes", "Heróis", "heroes", m_heroes),
        ("space_opera", "Space Opera", "space_opera_b", m_space), ("punk_rock", "Punk Rock", "punk_rock", m_punk))


def mono_svg(body, tint):
    """The tile area only, so the circle in the preview touches its edges as a launcher mask would."""
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="32 32 960 960"><mask id="m" maskUnits="userSpaceOnUse" x="-200" y="-200" width="1424" height="1424">'
            f'<rect x="-200" y="-200" width="1424" height="1424" fill="{B}"/>{body}</mask>'
            f'<rect x="-200" y="-200" width="1424" height="1424" fill="{tint}" mask="url(#m)"/></svg>')


def build():
    SVG.mkdir(exist_ok=True)
    paths = fl.eye_paths(z.shipped_eyes())
    for key, _, _, draw in PACK:
        for mode, (_, tint) in THEMES.items():
            (SVG / f"mono_{key}_{mode}.svg").write_text(mono_svg(draw(paths), tint))

    def themed(mode, size):
        bg = THEMES[mode][0]
        return '<div class="row">' + "".join(
            f'<div class="c" style="width:{size}px;height:{size}px;background:{bg}"><img src="svg/mono_{key}_{mode}.svg"></div>' for key, *_ in PACK) + "</div>"

    def colour(mode, size):
        cells = ""
        for key, _, file, _ in PACK:
            src = f"svg/flat_focinho_{mode}.svg" if key == "base" else f"svg/{file}_{mode}.svg"
            cells += f'<div class="c" style="width:{size}px;height:{size}px"><img src="{src}" style="transform:scale(1.067)"></div>'
        return f'<div class="row">{cells}</div>'

    def plan(mode, size):
        """A draft of the debug/hmg blueprint: the mono shapes in white on the ground the debug build already uses. Only the
        middle 72dp of the 108dp ground shows on a launcher, hence the 150% zoom."""
        ground = f"background:url({DEBUG_RES}/ic_dragon_bg_{mode}.png) center/150%"
        return '<div class="row">' + "".join(
            f'<div class="s" style="width:{size}px;height:{size}px;{ground}"><img src="svg/mono_{key}_plan.svg"></div>' for key, *_ in PACK) + "</div>"

    labels = '<div class="row">' + "".join(f'<span style="width:150px">{name}</span>' for _, name, *_ in PACK) + "</div>"
    html = f"""<!doctype html><meta charset="utf-8"><title>Mono</title>
<style>
body{{margin:0;padding:28px;background:#0b0b0d;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:22px 0 10px}} .row{{display:flex;gap:16px;align-items:center;margin-bottom:12px}} span{{font-size:12px;text-align:center;opacity:.8}}
.c{{border-radius:50%;overflow:hidden;flex:none}} .c img,.s img{{display:block;width:100%;height:100%}} .s{{border-radius:22%;overflow:hidden;flex:none}}
</style>
<h2>Mono, tema claro</h2>{labels}{themed("light", 150)}
<h2>Mono, tema escuro</h2>{themed("dark", 150)}
<h2>Mono em 72 e 48 px</h2>{themed("light", 72)}{themed("dark", 48)}
<h2>Colorido cortado em círculo (o que os cantos perdem)</h2>{colour("light", 150)}{colour("dark", 150)}
<h2>Rascunho da planta para debug/hmg</h2>{plan("light", 150)}{plan("dark", 150)}"""
    (HERE / "mono_preview.html").write_text(html)


def render():
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1760,1480",
                    f"--screenshot={HERE / 'mono_preview.png'}", f"file://{HERE / 'mono_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    render()
    print("ok")
