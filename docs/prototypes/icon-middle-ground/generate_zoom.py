#!/usr/bin/env python3
"""Round 5 of the icon study: today's icon with no head shape, the whole tile is the dragon's skin.

The user's brief, after two wrong turns (a silhouette on a ground, then invented eyes, snout and mouth that lost
the look): take the CURRENT icon and imagine the head outline no longer exists. The entire tile is the dragon's
skin, the way the pumpkin and the mummy fill theirs. So nothing here is invented: the eyes are the shipped eyes
(traced from `reference/shipped_gota_fg_light.png`) scaled up about 6x, on the shipped skin (the dark glossy
head in light mode, the lilac head in dark mode), with the same soft gloss and rim light.

  A  pure zoom       skin + 2 eyes                                                       = 3 shapes (what the icon has now)
  B  zoom + a face   A + 2 pupils + 2 glints + 1 pair of nostrils + 1 mouth              = 9 shapes (7 in dark: no glints)

The extra marks of B are tone on tone, in the language of the shipped icon (the dark icon already carries a small
nose mark), not pads or outlines in a new colour.

    python3 generate_zoom.py          # writes svg/zoom_*.svg and zoom_preview.html
    python3 generate_zoom.py --png    # also rasterises with headless Chrome
"""
import pathlib
import subprocess
import sys

import generate as g
import generate_middle as m

HERE, SVG, PNG = g.HERE, g.SVG, g.PNG
S = 6.0            # zoom against the 432px art
EYE_Y = 440        # where the middle of the eyes lands in the tile
HX = 512

LIGHT = dict(top="#33276E", mid="#1D1442", bottom="#0C0720", rim="#C4B0FF", rim_o=.55, hi=.13,
             eye_a="#E4DAFF", eye="#FFFFFF", pupil="#150C2E", glint="#FFFFFF",
             nostril="#4B3E94", mouth="#6A55C4", mouth_o=.85)
DARK = dict(top="#F0E6FF", mid="#C6A8FF", bottom="#9360F6", rim="#FFFFFF", rim_o=.7, hi=.30,
            eye_a="#2B1A60", eye="#150B34", pupil="#F1EAFF", glint=None,
            nostril="#8057E6", mouth="#6A3FD0", mouth_o=.9)


def shipped_eyes():
    """The two eye outlines of the shipped icon, in the 432px art's own coordinates."""
    fg = HERE / "reference" / "shipped_gota_fg_light.png"  # not the app's file: this pack replaced it
    tmp = pathlib.Path(HERE / ".eyes.pgm")
    subprocess.run(["magick", str(fg), "-fx", "min(r,min(g,b))*a", "-colorspace", "gray", "-resize", "400%",
                    "-blur", "0x3", "-threshold", "62%", "-depth", "8", f"pgm:{tmp}"], check=True)
    w, h, pix = m.read_pgm(tmp)
    tmp.unlink()
    polys = sorted(sorted(m.loops(w, h, pix), key=m.area)[-2:], key=lambda p: m.bbox(p)[0])
    return [[(x / 4, y / 4) for x, y in m.simplify(p, 1.6)] for p in polys]


def to_tile(poly, oy):
    """432px art coordinates to tile coordinates: zoom S about the face's centre line, eyes centred on EYE_Y."""
    return [(HX + (x - 216) * S, EYE_Y + (y - oy) * S) for x, y in poly]


def features(p, eyes, variant, mono=False):
    oy = sum(m.bbox(eyes[0])[i] for i in (1, 3)) / 2
    tiles = [to_tile(e, oy) for e in eyes]
    out = []
    for i, poly in enumerate(tiles):
        d = m.to_path(poly)
        x0, y0, x1, y1 = m.bbox(poly)
        w, h = x1 - x0, y1 - y0
        left = i == 0
        # the round head of the drop is at the outer top; the pupil sits there, not toward the point
        px = (x0 + .38 * w) if left else (x1 - .38 * w)
        py, rx, ry = y0 + .42 * h, .15 * w, .25 * h
        if mono:
            hole = f"M{px - rx:.1f},{py:.1f} a{rx:.1f},{ry:.1f} 0 1 0 {2 * rx:.1f},0 a{rx:.1f},{ry:.1f} 0 1 0 {-2 * rx:.1f},0 Z" if variant == "B" else ""
            out.append(f'<path fill-rule="evenodd" fill="#000" d="{d} {hole}"/>')
            continue
        out.append(f'<linearGradient id="zg{i}" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{p["eye_a"]}"/>'
                   f'<stop offset=".4" stop-color="{p["eye"]}"/></linearGradient><path d="{d}" fill="url(#zg{i})"/>')
        if variant == "B":
            out.append(f'<clipPath id="zc{i}"><path d="{d}"/></clipPath><g clip-path="url(#zc{i})">'
                       f'<ellipse cx="{px:.1f}" cy="{py:.1f}" rx="{rx:.1f}" ry="{ry:.1f}" fill="{p["pupil"]}"/>'
                       + (f'<circle cx="{px - rx * .30:.1f}" cy="{py - ry * .36:.1f}" r="{rx * .40:.1f}" fill="{p["glint"]}"/>' if p["glint"] else "")
                       + "</g>")
    if variant == "B":
        f = lambda a, b: f"{a:.1f},{b:.1f}"
        nostril = lambda s: f"M{f(HX + s * 66, 690)} Q{f(HX + s * 40, 676)} {f(HX + s * 34, 716)} Q{f(HX + s * 54, 732)} {f(HX + s * 66, 690)} Z"
        mouth = f"M{f(HX - 190, 800)} Q{f(HX, 880)} {f(HX + 190, 800)}"
        if mono:
            out.append(f'<path d="{nostril(-1)} {nostril(1)}" fill="#000"/><path d="{mouth}" fill="none" stroke="#000" stroke-width="26" stroke-linecap="round"/>')
        else:
            out.append(f'<path d="{nostril(-1)} {nostril(1)}" fill="{p["nostril"]}"/>')
            out.append(f'<path d="{mouth}" fill="none" stroke="{p["mouth"]}" stroke-width="14" stroke-linecap="round" opacity="{p["mouth_o"]}"/>')
    return "".join(out)


def skin_defs(p):
    extra = (f'<linearGradient id="skin" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{p["top"]}"/>'
             f'<stop offset=".5" stop-color="{p["mid"]}"/><stop offset="1" stop-color="{p["bottom"]}"/></linearGradient>'
             f'<radialGradient id="gloss2" cx="512" cy="200" r="540" gradientUnits="userSpaceOnUse">'
             f'<stop offset="0" stop-color="#fff" stop-opacity="{p["hi"]}"/><stop offset="1" stop-color="#fff" stop-opacity="0"/></radialGradient>')
    return g.common_defs(p["top"], p["bottom"], rim=p["rim"], rim_opacity=p["rim_o"], extra=extra)


def tile(p, eyes, variant):
    return (skin_defs(p) + f'<path d="{g.TILE}" fill="url(#skin)"/><g clip-path="url(#tile)">'
            + '<rect width="1024" height="1024" fill="url(#gloss2)"/>' + features(p, eyes, variant)
            + '</g>' + f'<path d="{g.TILE}" fill="none" stroke="url(#rim)" stroke-width="10"/>')


SAFE = .80  # keeps the eyes and mouth inside the 66dp circle


def adaptive(p, eyes, variant):
    bg = skin_defs(p) + '<rect width="1024" height="1024" fill="url(#skin)"/><rect width="1024" height="1024" fill="url(#gloss2)"/>'
    return bg, g.scaled(features(p, eyes, variant), SAFE), g.scaled(features(p, eyes, variant, mono=True), SAFE)


def build():
    SVG.mkdir(exist_ok=True)
    eyes = shipped_eyes()
    for mode, p in (("light", LIGHT), ("dark", DARK)):
        for v in ("A", "B"):
            (SVG / f"zoom{v}_{mode}.svg").write_text(g.wrap(tile(p, eyes, v)))
        bg, fg, mono = adaptive(p, eyes, "B")
        (SVG / f"zoom_adaptive_background_{mode}.svg").write_text(g.wrap(bg))
        (SVG / f"zoom_adaptive_foreground_{mode}.svg").write_text(g.wrap(fg))
        if mode == "light":
            (SVG / "zoom_adaptive_monochrome.svg").write_text(g.wrap(mono))

    def card(src, title, note, radius=True):
        r = ' style="border-radius:22%"' if radius else ""
        return f'<figure><img src="{src}"{r}><figcaption><b>{title}</b><br>{note}</figcaption></figure>'

    scale = ""
    for mode, label, old in (("light", "claro", "reference/old_duo_light.webp"), ("dark", "escuro", "reference/old_duo_dark.webp")):
        shapes_b = "9 formas" if mode == "light" else "7 formas"
        scale += ('<div class="row">' + card(f"reference/current_gota_{mode}.png", f"Atual ({label})", "silhueta: 3 formas")
                  + '<span class="arrow">→</span>' + card(f"svg/zoomA_{mode}.svg", f"A, zoom puro ({label})", "pele + 2 olhos: 3 formas")
                  + '<span class="arrow">→</span>' + card(f"svg/zoomB_{mode}.svg", f"B, zoom + rosto ({label})", shapes_b)
                  + '<span class="arrow">→</span>' + card(old, f"Antigo ({label})", "cerca de 24 marcas", radius=False) + "</div>")
    sizes = ""
    for label, src in (("Atual", "reference/current_gota_light.png"), ("A claro", "svg/zoomA_light.svg"), ("B claro", "svg/zoomB_light.svg"),
                       ("A escuro", "svg/zoomA_dark.svg"), ("B escuro", "svg/zoomB_dark.svg"), ("Antigo", "reference/old_duo_light.webp")):
        sizes += f'<div class="sm"><span>{label}</span>' + "".join(
            f'<img src="{src}" width="{s}" height="{s}" style="border-radius:22%">' for s in (144, 96, 72, 48)) + "</div>"
    masks = ""
    for label, css, mode in (("círculo", "50%", "light"), ("squircle", "30%", "light"), ("quadrado", "10%", "light"), ("escuro", "50%", "dark")):
        masks += (f'<div class="ad"><div class="mask" style="border-radius:{css}"><img src="svg/zoom_adaptive_background_{mode}.svg">'
                  f'<img src="svg/zoom_adaptive_foreground_{mode}.svg"><div class="safe"></div></div><span>{label}</span></div>')
    masks += ('<div class="ad"><div class="mask" style="border-radius:50%;background:#2c2540">'
              '<img src="svg/zoom_adaptive_monochrome.svg" style="filter:invert(1) sepia(1) saturate(3) hue-rotate(220deg) brightness(1.1)">'
              '</div><span>temático (mono)</span></div>')
    html = f"""<!doctype html><meta charset="utf-8"><title>Zoom do dragão</title>
<style>
body{{margin:0;padding:32px;background:#0b0b0d;color:#e8e6ef;font:15px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:30px 0 12px}} .row{{display:flex;gap:10px;align-items:center;margin-bottom:18px}}
figure{{margin:0;padding:12px;border-radius:20px;text-align:center;background:#000;width:250px}} figure img{{width:250px;height:250px;display:block}}
figcaption{{margin-top:10px;font-size:13px;opacity:.85}} .arrow{{font-size:26px;opacity:.5}}
.ad{{display:inline-block;margin-right:14px;text-align:center}} .mask{{position:relative;width:190px;height:190px;overflow:hidden;background:#222}} .mask img{{position:absolute;inset:0;width:100%;height:100%}}
.ad span,.sm span{{display:block;font-size:13px;opacity:.75;margin-top:6px}} .safe{{position:absolute;left:50%;top:50%;width:61%;height:61%;transform:translate(-50%,-50%);border:2px dashed #fff8;border-radius:50%}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin:0 0 12px}} .sm span{{width:110px;margin:0}}
</style>
<h2>O ícone atual sem a forma da cabeça: o tile inteiro é a pele do dragão</h2>{scale}
<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{sizes}
<h2>Adaptive icon da versão B (tracejado = área segura de 66dp)</h2><div>{masks}</div>
"""
    (HERE / "zoom_preview.html").write_text(html)


def render():
    PNG.mkdir(exist_ok=True)
    for name in ("zoomA_light", "zoomA_dark", "zoomB_light", "zoomB_dark"):
        subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                        "--window-size=1024,1024", f"--screenshot={PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
                       check=True, capture_output=True)
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1200,2300",
                    f"--screenshot={HERE / 'zoom_preview.png'}", f"file://{HERE / 'zoom_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
