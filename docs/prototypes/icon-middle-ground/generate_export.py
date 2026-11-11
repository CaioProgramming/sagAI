#!/usr/bin/env python3
"""Exports the approved pack to the layers the app and iOS use. Reads the SVGs the genre generators wrote, so run those first.

An adaptive icon is a 108dp canvas of which a launcher shows the middle 72dp, and that 72dp is the study's tile (32 to 992).
So the canvas runs from -208 to 1232, and the shapes that bleed off the tile are continued to its edge (`bleed`), for
launchers that move the layers.

  export/android/         the production layers, named as in app/src/main/res/drawable-nodpi: bg and fg per mode, one mono
  export/android_debug/   the debug/hmg blueprint figure, named as in app/src/debug/res/drawable-nodpi; the grid ground stays
  export/ios/             1024px default, dark and tinted, full bleed (iOS applies the corners)

    python3 generate_export.py             # writes export/
    python3 generate_export.py --install   # also copies the Android layers into app/src/{main,debug}/res/drawable-nodpi
"""
import re
import shutil
import subprocess
import sys

import generate as g
import generate_cowboy as cowboy
import generate_crime as crime
import generate_fantasy as fantasy
import generate_flat as fl
import generate_heroes as heroes
import generate_mono as mono
import generate_shinobi as shinobi
import generate_space as space
import generate_zoom as z

HERE, SVG = g.HERE, g.SVG
OUT = HERE / "export"
RES = HERE.parent.parent.parent / "app" / "src"
CANVAS = 'viewBox="-208 -208 1440 1440" width="432" height="432"'
FULL = 'viewBox="32 32 960 960" width="1024" height="1024"'
W, B = mono.W, mono.B
MIRROR = mono.MIRROR
# Resource prefix, SVG file stem and mono body of each icon. The base keeps its older PNG names.
PACK = (("", "flat_focinho", mono.base), ("fantasy_", "fantasy", mono.m_fantasy), ("crime_", "crime", mono.m_crime),
        ("cyberpunk_", "cyberpunk", mono.m_cyberpunk), ("horror_", "horror", mono.m_horror), ("shinobi_", "shinobi", mono.m_shinobi),
        ("cowboy_", "cowboy", mono.m_cowboy), ("heroes_", "heroes", mono.m_heroes), ("space_opera_", "space_opera_b", mono.m_space),
        ("punk_rock_", "punk_rock", mono.m_punk))


def rects(fill, *boxes):
    return "".join(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{fill}"/>' for x, y, w, h in boxes)


def bleed(prefix, fill):
    """What continues past the tile, under everything else."""
    def sides(y0, y1):
        return (-208, y0, 190, y1 - y0), (1042, y0, 190, y1 - y0)
    if prefix == "fantasy_":
        return rects(fill, (-208, -208, 1440, 190), *sides(-20, 520))
    if prefix == "shinobi_":
        return rects(fill, (-208, -208, 1440, 190), *sides(-20, 412), *sides(792, 1044), (-208, 1042, 1440, 190))
    if prefix == "cowboy_":
        return rects(fill, *sides(772, 1044), (-208, 1042, 1440, 190))
    if prefix == "heroes_":
        return rects(fill, *sides(322, 868))
    if prefix == "space_opera_":
        return rects(fill, (-208, -208, 1440, 190), *sides(-20, 1044), (-208, 1042, 1440, 190))
    return ""


def bleed_colour(prefix, mode):
    if prefix == "fantasy_":
        return fantasy.PLATE[mode][1]
    if prefix == "shinobi_":
        return shinobi.CLOTH[mode][0]
    if prefix == "cowboy_":
        return cowboy.OPTIONS[""][mode][0]
    if prefix == "heroes_":
        return heroes.MASK
    if prefix == "space_opera_":
        return "url(#shell)"
    return "none"


def view(svg, box):
    return re.sub(r'viewBox="0 0 1024 1024" width="\d+" height="\d+"', box, svg, count=1)


def skin_gradient(svg):
    """The skin as in the tile, in user space so it keeps its place on the larger canvas and pads past the tile's edges."""
    grad = re.search(r'<linearGradient id="skin".*?</linearGradient>', svg, re.S).group(0)
    return grad.replace('x1="0" y1="0" x2="0" y2="1"', 'gradientUnits="userSpaceOnUse" x1="0" y1="32" x2="0" y2="992"')


def background(svg, box=CANVAS):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" {box}><defs>{skin_gradient(svg)}</defs>'
            '<rect x="-208" y="-208" width="1440" height="1440" fill="url(#skin)"/></svg>')


def foreground(svg, prefix, mode, box=CANVAS):
    out = view(svg, box).replace(f'<path d="{g.TILE}" fill="url(#skin)"/>', "")
    extra = bleed(prefix, bleed_colour(prefix, mode))
    return out.replace('<g clip-path="url(#tclip)">', f"<g>{extra}", 1)


def ios(svg, prefix, mode):
    """Skin and foreground flattened and full bleed: iOS has one layer and applies the corners itself."""
    out = foreground(svg, prefix, mode, FULL)
    out = re.sub(r'<linearGradient id="skin".*?</linearGradient>', skin_gradient(svg), out, count=1, flags=re.S)
    return out.replace("<g>", '<g><rect x="-208" y="-208" width="1440" height="1440" fill="url(#skin)"/>', 1)


def mask_svg(body, prefix, tint, ground=None, box=CANVAS):
    under = f'<rect x="-208" y="-208" width="1440" height="1440" fill="{ground}"/>' if ground else ""
    return (f'<svg xmlns="http://www.w3.org/2000/svg" {box}>{under}<mask id="m" maskUnits="userSpaceOnUse" x="-208" y="-208" width="1440" height="1440">'
            f'<rect x="-208" y="-208" width="1440" height="1440" fill="{B}"/>{bleed(prefix, W)}{body}</mask>'
            f'<rect x="-208" y="-208" width="1440" height="1440" fill="{tint}" mask="url(#m)"/></svg>')


# The debug/hmg blueprint keeps today's look: a pale figure with a white outline over the grid, and the accessory hatched so
# the grid shows through it. Each entry is (hatched part, solid part); icons not listed here are solid.

def blueprint_parts(prefix, paths, body):
    eyes = mono.white_eyes(paths)
    if prefix == "fantasy_":
        plate = (f'<path d="{fantasy.PLATE_D}" fill="{W}"/>'
                 f'<path d="{fantasy.LINES_D}" fill="none" stroke="{B}" stroke-width="12"/>')
        p = fantasy.PRISM
        prism = " ".join(f"{p[k][0]},{p[k][1]}" for k in ("top", "r", "bot", "l"))
        solid = (eyes + mono.NOSTRILS + f'<path d="{fantasy.PLATE_D}" fill="{B}" stroke="{B}" stroke-width="44"/>'
                 f'<polygon points="{prism}" fill="{W}"/>')
        return plate + f'<polygon points="{prism}" fill="{B}" stroke="{B}" stroke-width="18"/>', solid
    if prefix == "crime_":
        lenses = f'<path d="{crime.lens(False)}" fill="{W}"/><path d="{crime.lens(True)}" fill="{W}"/>'
        return lenses, body
    if prefix == "shinobi_":
        return body.replace(eyes, ""), eyes
    if prefix == "cowboy_":
        return body.replace(eyes, ""), eyes
    if prefix == "heroes_":
        return body.replace(mono.NOSTRILS, ""), ""  # no nostrils: alone under the mask they read as a piercing
    if prefix == "space_opera_":
        shell = f'<path fill-rule="evenodd" d="M-20,-20 H1044 V1044 H-20 Z {space.OPENING}" fill="{W}"/>'
        vents = "".join(f'<rect x="{x}" y="930" width="70" height="20" rx="10" fill="{B}"/>' for x in (377, 477, 577))
        solid = (eyes + mono.NOSTRILS + f'<path d="{space.sparkle(700, 440, 30)}" fill="{W}"/>'
                 f'<path d="{space.sparkle(220, 790, 20)}" fill="{W}"/>')
        return shell + vents, solid
    return "", body


ICE = {"light": ("#FFFFFF", "#D7E6FB"), "dark": ("#E4EEFD", "#A6C2EE")}


def blueprint(prefix, mode, paths, body):
    hatched, solid = blueprint_parts(prefix, paths, body)
    a, b = ICE[mode]
    full = 'x="-208" y="-208" width="1440" height="1440"'

    def mask(i, content, under=""):
        return f'<mask id="{i}" maskUnits="userSpaceOnUse" {full}><rect {full} fill="{B}"/>{under}{content}</mask>'
    return (f'<svg xmlns="http://www.w3.org/2000/svg" {CANVAS}><defs>'
            f'<linearGradient id="ice" gradientUnits="userSpaceOnUse" x1="0" y1="32" x2="0" y2="992"><stop offset="0" stop-color="{a}"/><stop offset="1" stop-color="{b}"/></linearGradient>'
            '<pattern id="hatch" patternUnits="userSpaceOnUse" width="30" height="30" patternTransform="rotate(45)"><rect width="9" height="30" fill="#fff" fill-opacity=".8"/></pattern>'
            '<filter id="edge" filterUnits="userSpaceOnUse" x="-208" y="-208" width="1440" height="1440"><feMorphology in="SourceAlpha" operator="dilate" radius="4" result="grown"/>'
            '<feComposite in="grown" in2="SourceAlpha" operator="out" result="line"/><feFlood flood-color="#fff"/><feComposite in2="line" operator="in"/></filter>'
            f'{mask("ms", solid)}{mask("mh", hatched, bleed(prefix, W) if hatched else "")}</defs>'
            f'<g filter="url(#edge)"><rect {full} fill="#fff" mask="url(#ms)"/><rect {full} fill="#fff" mask="url(#mh)"/></g>'
            f'<rect {full} fill="url(#ice)" fill-opacity=".92" mask="url(#ms)"/>'
            f'<rect {full} fill="#fff" fill-opacity=".14" mask="url(#mh)"/><rect {full} fill="url(#hatch)" mask="url(#mh)"/></svg>')


def shot(svg_text, target, size=432):
    """Chrome renders the SVG with a transparent ground; ImageMagick writes the format the resource name asks for."""
    source = target.with_suffix(".svg")
    source.write_text(svg_text)
    raw = target.with_name(target.stem + "_raw.png")
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                    f"--window-size={size},{size}", f"--screenshot={raw}", f"file://{source}"], check=True, capture_output=True)
    subprocess.run(["magick", str(raw), "-quality", "92", "-define", "webp:alpha-quality=100", str(target)], check=True)
    raw.unlink()


def ext(prefix):
    return "png" if prefix == "" else "webp"


def build():
    paths = fl.eye_paths(z.shipped_eyes())
    for sub in ("android", "android_debug", "ios"):
        (OUT / sub).mkdir(parents=True, exist_ok=True)
    for prefix, stem, draw in PACK:
        body = draw(paths)
        kind = ext(prefix)
        for mode in ("light", "dark"):
            art = (SVG / f"{stem}_{mode}.svg").read_text()
            shot(background(art), OUT / "android" / f"ic_dragon_{prefix}bg_{mode}.{kind}")
            shot(foreground(art, prefix, mode), OUT / "android" / f"ic_dragon_{prefix}fg_{mode}.{kind}")
            shot(blueprint(prefix, mode, paths, body), OUT / "android_debug" / f"ic_dragon_{prefix}fg_{mode}.{kind}")
            shot(ios(art, prefix, mode), OUT / "ios" / f"{(prefix.rstrip('_') or 'default')}{'' if mode == 'light' else '_dark'}.png", 1024)
        shot(mask_svg(body, prefix, W), OUT / "android" / f"ic_dragon_{prefix}mono.{kind}")
        shot(mask_svg(body, prefix, W, "#000", FULL), OUT / "ios" / f"{prefix.rstrip('_') or 'default'}_tinted.png", 1024)
    for stale in OUT.rglob("*.svg"):
        stale.unlink()


def preview():
    """The layers as a launcher composes them, in a circle and in a rounded square, with the blueprint on the current ground."""
    def layered(bg, fg, shape):
        return (f'<div class="i" style="border-radius:{shape};background-image:url({fg}),url({bg})"></div>')
    rows = ""
    for mode in ("light", "dark"):
        for shape in ("50%", "22%"):
            rows += '<div class="r">' + "".join(
                layered(f"export/android/ic_dragon_{p}bg_{mode}.{ext(p)}", f"export/android/ic_dragon_{p}fg_{mode}.{ext(p)}", shape) for p, *_ in PACK) + "</div>"
    plans = ""
    for mode in ("light", "dark"):
        plans += '<div class="r">' + "".join(
            layered(f"../../../app/src/debug/res/drawable-nodpi/ic_dragon_bg_{mode}.png", f"export/android_debug/ic_dragon_{p}fg_{mode}.{ext(p)}", "22%") for p, *_ in PACK) + "</div>"
    html = f"""<!doctype html><meta charset="utf-8"><title>Exportação</title>
<style>body{{margin:0;padding:28px;background:#2a2a30;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}} h2{{font-weight:600;margin:22px 0 10px}}
.r{{display:flex;gap:14px;margin-bottom:12px}} .i{{width:150px;height:150px;background-size:150%;background-position:center;flex:none}}</style>
<h2>Produção: camadas compostas</h2>{rows}<h2>Debug/hmg: planta</h2>{plans}"""
    (HERE / "export_preview.html").write_text(html)
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1680,1120",
                    f"--screenshot={HERE / 'export_preview.png'}", f"file://{HERE / 'export_preview.html'}"], check=True, capture_output=True)


def install():
    for sub, dest in (("android", RES / "main/res/drawable-nodpi"), ("android_debug", RES / "debug/res/drawable-nodpi")):
        for f in sorted((OUT / sub).iterdir()):
            target = dest / f.name
            if not target.exists():
                raise SystemExit(f"{target} does not exist: the layer names no longer match the app")
            shutil.copy(f, target)


if __name__ == "__main__":
    build()
    preview()
    if "--install" in sys.argv:
        install()
    print("ok")
