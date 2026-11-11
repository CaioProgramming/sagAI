#!/usr/bin/env python3
"""Generates the dragon app-icon candidates for the "middle ground" study.

The brief: something between today's minimalist Gota head and the cancelled Duolingo-style rig,
following a Halloween reference set (pumpkin and mummy): the tile IS the face, flat colour with
one faint texture, two big dark eyes, a minimal mouth, soft inner shading, no outlines.

Everything is plain SVG (gradients, one pattern, clip paths) so it can be re-tuned by editing the
numbers below. Nothing here touches the real app icon: output goes to ./svg and ./png.

    python3 generate.py            # writes svg/ and preview.html
    python3 generate.py --png      # also rasterises with headless Chrome (png/ and preview.png)
"""
import math
import pathlib
import subprocess
import sys

HERE = pathlib.Path(__file__).parent
SVG = HERE / "svg"
PNG = HERE / "png"
CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"


def squircle(a=480, n=5.0, steps=360, cx=512, cy=512):
    """Superellipse |x|^n + |y|^n = a^n, the soft-cornered tile of the reference."""
    pts = []
    for i in range(steps):
        t = 2 * math.pi * i / steps
        c, s = math.cos(t), math.sin(t)
        x = cx + a * math.copysign(abs(c) ** (2 / n), c)
        y = cy + a * math.copysign(abs(s) ** (2 / n), s)
        pts.append(f"{x:.1f},{y:.1f}")
    return "M" + " L".join(pts) + " Z"


TILE = squircle()


def common_defs(top, bottom, rim="#ffffff", rim_opacity=0.5, extra=""):
    return f"""<defs>
  <clipPath id="tile"><path d="{TILE}"/></clipPath>
  <linearGradient id="base" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="{top}"/><stop offset="1" stop-color="{bottom}"/>
  </linearGradient>
  <linearGradient id="shade" x1="0" y1="0" x2="0" y2="1">
    <stop offset=".6" stop-color="#000" stop-opacity="0"/><stop offset="1" stop-color="#000" stop-opacity=".24"/>
  </linearGradient>
  <linearGradient id="gloss" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="#fff" stop-opacity=".16"/><stop offset=".34" stop-color="#fff" stop-opacity="0"/>
  </linearGradient>
  <linearGradient id="rim" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="{rim}" stop-opacity="{rim_opacity}"/><stop offset=".45" stop-color="{rim}" stop-opacity="0"/>
  </linearGradient>
  {extra}
</defs>"""


def scaled(inner, s):
    if s == 1:
        return inner
    return f'<g transform="translate(512 512) scale({s}) translate(-512 -512)">{inner}</g>'


def eye_rr(idn, cx, cy, w, h, r, base, inner, dx, dy):
    """Rounded-square eye with the pumpkin's offset inner shadow: a second, lighter shape
    nudged down-right and clipped to the socket, so a darker crescent stays top-left."""
    x, y = cx - w / 2, cy - h / 2
    return (
        f'<clipPath id="{idn}"><rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}"/></clipPath>'
        f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{base}"/>'
        f'<g clip-path="url(#{idn})"><rect x="{x + dx}" y="{y + dy}" width="{w}" height="{h}" rx="{r}" fill="{inner}"/></g>'
    )


# ---------------------------------------------------------------------------------------------
# A: pumpkin-style purple dragon. Vertical ridges stand in for the pumpkin boards.
# ---------------------------------------------------------------------------------------------
A_BASE = ("#7D55F2", "#6340D6")
A_RIDGE = "#5A36C4"
A_EYE = ("#1B0A42", "#2E1470")


def a_ridges():
    out = []
    for x, bow in ((196, -34), (354, -18), (512, 0), (670, 18), (828, 34)):
        out.append(
            f'<path d="M{x},40 Q{x + bow},512 {x},984" fill="none" stroke="{A_RIDGE}" '
            f'stroke-width="9" stroke-linecap="round" opacity=".5"/>'
        )
    return "".join(out)


def horns(tag, top, bottom):
    """Two short forehead horns: the one dragon cue that survives at 48px. They stay inside the
    adaptive safe zone, so they live in the foreground layer rather than on the tile edge."""
    return (
        f'<linearGradient id="horn_{tag}" x1="0" y1="0" x2="0" y2="1">'
        f'<stop offset="0" stop-color="{top}"/><stop offset="1" stop-color="{bottom}"/></linearGradient>'
        f'<path d="M296,344 Q292,246 334,164 Q394,246 414,344 Z" fill="url(#horn_{tag})"/>'
        f'<path d="M728,344 Q732,246 690,164 Q630,246 610,344 Z" fill="url(#horn_{tag})"/>'
    )


def a_features(dark=False):
    base, inner = A_EYE if not dark else ("#EDE3FF", "#C2AAFF")
    horn = horns("a", "#C8B2FF", "#8E6CF8") if not dark else horns("ad", "#D9C8FF", "#8E6CF8")
    mouth = "#1B0A42" if not dark else "#EDE3FF"
    eyes = eye_rr("a_eyeL", 322, 520, 250, 282, 86, base, inner, 28, 24)
    eyes += eye_rr("a_eyeR", 702, 520, 250, 282, 86, base, inner, 28, 24)
    mouth_svg = (
        '<clipPath id="a_mouth"><path d="M452,772 Q512,868 572,772 Z"/></clipPath>'
        f'<path d="M452,772 Q512,868 572,772 Z" fill="{mouth}"/>'
        '<g clip-path="url(#a_mouth)"><ellipse cx="512" cy="840" rx="30" ry="18" fill="#FF6F91"/></g>'
    )
    return horn + eyes + mouth_svg


def candidate_a():
    return (
        common_defs(*A_BASE)
        + f'<path d="{TILE}" fill="url(#base)"/>'
        + '<g clip-path="url(#tile)">'
        + a_ridges()
        + scaled(a_features(), 1)
        + '<rect width="1024" height="1024" fill="url(#shade)"/>'
        + '<rect width="1024" height="1024" fill="url(#gloss)"/>'
        + "</g>"
        + f'<path d="{TILE}" fill="none" stroke="url(#rim)" stroke-width="10"/>'
    )


# ---------------------------------------------------------------------------------------------
# B: mummy-style pale lavender dragon. Faint scales stand in for the bandage strips.
# ---------------------------------------------------------------------------------------------
B_BASE = ("#E4DDF7", "#CFC5EE")


def candidate_b():
    pattern = (
        '<pattern id="scales" width="128" height="84" patternUnits="userSpaceOnUse">'
        '<path d="M0 0 A64 42 0 0 0 128 0" fill="none" stroke="#9D8EDB" stroke-width="5" opacity=".42"/>'
        '<path d="M-64 42 A64 42 0 0 0 64 42 M64 42 A64 42 0 0 0 192 42" fill="none" stroke="#9D8EDB" stroke-width="5" opacity=".42"/>'
        "</pattern>"
    )
    eyes = ""
    for i, cx in enumerate((328, 696)):
        eyes += (
            f'<ellipse cx="{cx}" cy="568" rx="120" ry="152" fill="#0C0A14" transform="rotate({-5 if i == 0 else 5} {cx} 568)"/>'
            f'<circle cx="{cx + 6}" cy="600" r="13" fill="none" stroke="#7E7C86" stroke-width="5"/>'
        )
    nostrils = (
        '<ellipse cx="478" cy="812" rx="14" ry="9" fill="#8E80C9"/>'
        '<ellipse cx="546" cy="812" rx="14" ry="9" fill="#8E80C9"/>'
    )
    return (
        common_defs(*B_BASE, rim="#ffffff", rim_opacity=0.7, extra=pattern)
        + f'<path d="{TILE}" fill="url(#base)"/>'
        + '<g clip-path="url(#tile)">'
        + '<rect width="1024" height="1024" fill="url(#scales)"/>'
        + horns("b", "#BDB0EE", "#9283D6")
        + eyes
        + nostrils
        + '<rect width="1024" height="1024" fill="url(#shade)"/>'
        + '<rect width="1024" height="1024" fill="url(#gloss)"/>'
        + "</g>"
        + f'<path d="{TILE}" fill="none" stroke="url(#rim)" stroke-width="10"/>'
    )


# ---------------------------------------------------------------------------------------------
# C: closest to today's Gota: dark glossy tile, the two white leaf eyes, a tone-on-tone crest.
# ---------------------------------------------------------------------------------------------
C_BASE = ("#2B2060", "#120B2C")


def candidate_c():
    pattern = (
        '<pattern id="cscales" width="128" height="84" patternUnits="userSpaceOnUse">'
        '<path d="M0 0 A64 42 0 0 0 128 0" fill="none" stroke="#ffffff" stroke-width="5" opacity=".06"/>'
        '<path d="M-64 42 A64 42 0 0 0 64 42 M64 42 A64 42 0 0 0 192 42" fill="none" stroke="#ffffff" stroke-width="5" opacity=".06"/>'
        "</pattern>"
    )
    crest = (
        '<path d="M214,32 L300,236 L392,60 L452,262 L512,84 L572,262 L632,60 L724,236 L810,32 Z" '
        'fill="#3A2B84" opacity=".85"/>'
    )
    left = "M268,470 C376,438 458,520 440,606 C420,684 336,692 296,642 C252,590 240,516 268,470 Z"
    right = "M756,470 C648,438 566,520 584,606 C604,684 688,692 728,642 C772,590 784,516 756,470 Z"
    eyes = (
        f'<path d="{left}" fill="#FFFFFF"/><path d="{right}" fill="#FFFFFF"/>'
        f'<clipPath id="c_l"><path d="{left}"/></clipPath><clipPath id="c_r"><path d="{right}"/></clipPath>'
        '<g clip-path="url(#c_l)"><rect x="200" y="420" width="300" height="70" fill="#2B2060" opacity=".16"/></g>'
        '<g clip-path="url(#c_r)"><rect x="524" y="420" width="300" height="70" fill="#2B2060" opacity=".16"/></g>'
    )
    return (
        common_defs(*C_BASE, rim="#B9A6FF", rim_opacity=0.55, extra=pattern)
        + f'<path d="{TILE}" fill="url(#base)"/>'
        + '<g clip-path="url(#tile)">'
        + '<rect width="1024" height="1024" fill="url(#cscales)"/>'
        + crest
        + eyes
        + '<rect width="1024" height="1024" fill="url(#shade)"/>'
        + '<rect width="1024" height="1024" fill="url(#gloss)"/>'
        + "</g>"
        + f'<path d="{TILE}" fill="none" stroke="url(#rim)" stroke-width="10"/>'
    )


# ---------------------------------------------------------------------------------------------
# D: candidate A in dark mode: same face, backlit like the approved dark Gota (violet edge light).
# ---------------------------------------------------------------------------------------------
def candidate_d():
    glow = '<filter id="glow" x="-20%" y="-20%" width="140%" height="140%"><feGaussianBlur stdDeviation="16"/></filter>'
    return (
        common_defs("#1D1048", "#0B0620", rim="#C4B0FF", rim_opacity=0.9, extra=glow)
        + f'<path d="{TILE}" fill="none" stroke="#9B7BFF" stroke-width="22" opacity=".75" filter="url(#glow)"/>'
        + f'<path d="{TILE}" fill="url(#base)"/>'
        + '<g clip-path="url(#tile)">'
        + a_ridges().replace(A_RIDGE, "#3A2A8C")
        + a_features(dark=True)
        + '<rect width="1024" height="1024" fill="url(#shade)"/>'
        + "</g>"
        + f'<path d="{TILE}" fill="none" stroke="url(#rim)" stroke-width="12"/>'
    )


# ---------------------------------------------------------------------------------------------
# E: genre proofs on candidate A. One simple idea each, the dragon stays purple.
# ---------------------------------------------------------------------------------------------
def candidate_heroes():
    mask = (
        '<path d="M70,470 Q120,380 300,392 Q512,430 724,392 Q904,380 954,470 '
        'Q930,640 760,676 Q620,690 512,640 Q404,690 264,676 Q94,640 70,470 Z" fill="#7C96FF"/>'
        '<path d="M70,470 Q120,380 300,392 Q512,430 724,392 Q904,380 954,470 '
        'Q930,640 760,676 Q620,690 512,640 Q404,690 264,676 Q94,640 70,470 Z" fill="none" stroke="#fff" stroke-opacity=".25" stroke-width="6"/>'
    )
    eyes = eye_rr("h_eyeL", 322, 530, 214, 190, 70, *A_EYE, 22, 18) + eye_rr(
        "h_eyeR", 702, 530, 214, 190, 70, *A_EYE, 22, 18
    )
    return (
        common_defs(*A_BASE)
        + f'<path d="{TILE}" fill="url(#base)"/>'
        + '<g clip-path="url(#tile)">'
        + a_ridges()
        + horns("h", "#C8B2FF", "#8E6CF8")
        + mask
        + eyes
        + '<clipPath id="a_mouth"><path d="M452,772 Q512,868 572,772 Z"/></clipPath>'
        + '<path d="M452,772 Q512,868 572,772 Z" fill="#1B0A42"/>'
        + '<g clip-path="url(#a_mouth)"><ellipse cx="512" cy="840" rx="30" ry="18" fill="#FF6F91"/></g>'
        + '<rect width="1024" height="1024" fill="url(#shade)"/><rect width="1024" height="1024" fill="url(#gloss)"/>'
        + "</g>"
        + f'<path d="{TILE}" fill="none" stroke="url(#rim)" stroke-width="10"/>'
    )


def candidate_crime():
    lens_l = "M170,430 Q330,410 450,450 Q470,560 400,640 Q300,690 210,630 Q150,540 170,430 Z"
    lens_r = "M854,430 Q694,410 574,450 Q554,560 624,640 Q724,690 814,630 Q874,540 854,430 Z"
    glass = (
        f'<path d="{lens_l}" fill="#15121F" stroke="#E2B94F" stroke-width="16" stroke-linejoin="round"/>'
        f'<path d="{lens_r}" fill="#15121F" stroke="#E2B94F" stroke-width="16" stroke-linejoin="round"/>'
        '<path d="M450,470 Q512,440 574,470" fill="none" stroke="#E2B94F" stroke-width="16" stroke-linecap="round"/>'
        '<path d="M226,478 L312,456 M236,520 L270,512 M798,478 L712,456 M788,520 L754,512" '
        'stroke="#fff" stroke-opacity=".28" stroke-width="12" stroke-linecap="round"/>'
    )
    return (
        common_defs(*A_BASE)
        + f'<path d="{TILE}" fill="url(#base)"/>'
        + '<g clip-path="url(#tile)">'
        + a_ridges()
        + horns("c", "#C8B2FF", "#8E6CF8")
        + glass
        + '<clipPath id="a_mouth"><path d="M452,772 Q512,868 572,772 Z"/></clipPath>'
        + '<path d="M452,772 Q512,868 572,772 Z" fill="#1B0A42"/>'
        + '<g clip-path="url(#a_mouth)"><ellipse cx="512" cy="840" rx="30" ry="18" fill="#FF6F91"/></g>'
        + '<rect width="1024" height="1024" fill="url(#shade)"/><rect width="1024" height="1024" fill="url(#gloss)"/>'
        + "</g>"
        + f'<path d="{TILE}" fill="none" stroke="url(#rim)" stroke-width="10"/>'
    )


# ---------------------------------------------------------------------------------------------
# Adaptive layers for candidate A. The launcher draws a 108dp canvas and always shows the inner
# 66dp circle, so the face is shrunk to sit inside it. The background layer is full bleed.
# ---------------------------------------------------------------------------------------------
SAFE = 0.80


def adaptive_bg():
    return (
        '<defs><linearGradient id="abase" x1="0" y1="0" x2="0" y2="1">'
        f'<stop offset="0" stop-color="{A_BASE[0]}"/><stop offset="1" stop-color="{A_BASE[1]}"/></linearGradient></defs>'
        '<rect width="1024" height="1024" fill="url(#abase)"/>'
        + a_ridges()
    )


def adaptive_fg():
    return scaled(a_features(), SAFE)


def adaptive_mono():
    """Themed-icon layer: one flat shape the system tints, so eyes and mouth only."""
    eyes = "".join(
        f'<rect x="{cx - 125}" y="{520 - 141}" width="250" height="282" rx="86" fill="#000"/>' for cx in (322, 702)
    )
    mouth = '<path d="M452,772 Q512,868 572,772 Z" fill="#000"/>'
    horn = (
        '<path d="M296,344 Q292,246 334,164 Q394,246 414,344 Z" fill="#000"/>'
        '<path d="M728,344 Q732,246 690,164 Q630,246 610,344 Z" fill="#000"/>'
    )
    return scaled(horn + eyes + mouth, SAFE)


def wrap(body, size=1024):
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="{size}" height="{size}">'
        + body
        + "</svg>"
    )


CANDIDATES = {
    "A_abobora_dragao": ("A  Abóbora", candidate_a),
    "B_mumia_dragao": ("B  Múmia", candidate_b),
    "C_gota_tile": ("C  Gota em tile", candidate_c),
    "D_abobora_escuro": ("A no escuro", candidate_d),
    "E_heroes_mascara": ("A + Heroes", candidate_heroes),
    "F_crime_aviador": ("A + Crime", candidate_crime),
}


def build_svgs():
    SVG.mkdir(exist_ok=True)
    for name, (_, fn) in CANDIDATES.items():
        (SVG / f"{name}.svg").write_text(wrap(fn()))
    (SVG / "A_adaptive_background.svg").write_text(wrap(adaptive_bg()))
    (SVG / "A_adaptive_foreground.svg").write_text(wrap(adaptive_fg()))
    (SVG / "A_adaptive_monochrome.svg").write_text(wrap(adaptive_mono()))


def build_preview():
    def card(name, label, bg="#000"):
        return (
            f'<figure style="background:{bg}"><img src="svg/{name}.svg" alt="{label}"><figcaption>{label}</figcaption></figure>'
        )

    tiles = "".join(card(n, lbl) for n, (lbl, _) in list(CANDIDATES.items())[:4])
    genre = "".join(card(n, lbl) for n, (lbl, _) in list(CANDIDATES.items())[4:])

    masks = [("círculo", "border-radius:50%"), ("squircle", "border-radius:30%"), ("quadrado", "border-radius:10%")]
    adaptive = ""
    for label, css in masks:
        adaptive += (
            f'<div class="ad"><div class="mask" style="{css}">'
            '<img src="svg/A_adaptive_background.svg"><img src="svg/A_adaptive_foreground.svg">'
            '<div class="safe"></div></div><span>{}</span></div>'.format(label)
        )
    adaptive += (
        '<div class="ad"><div class="mask mono" style="border-radius:50%;background:#2c2540">'
        '<img src="svg/A_adaptive_monochrome.svg" style="filter:invert(1) sepia(1) saturate(3) hue-rotate(220deg) brightness(1.1)">'
        "</div><span>temático (mono)</span></div>"
    )

    small = ""
    for n, (lbl, _) in list(CANDIDATES.items())[:3]:
        small += f'<div class="sm"><span>{lbl}</span>' + "".join(
            f'<img src="svg/{n}.svg" width="{s}" height="{s}">' for s in (144, 96, 72, 48)
        ) + "</div>"

    html = f"""<!doctype html><meta charset="utf-8"><title>Ícone: meio-termo</title>
<style>
body{{margin:0;padding:32px;background:#0b0b0d;color:#e8e6ef;font:15px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:36px 0 12px}} .row{{display:flex;gap:20px;flex-wrap:wrap}}
figure{{margin:0;padding:18px;border-radius:20px;text-align:center}} figure img{{width:240px;height:240px;display:block}}
figcaption{{margin-top:8px;font-size:13px;opacity:.75}}
.ad{{text-align:center}} .mask{{position:relative;width:216px;height:216px;overflow:hidden;background:#222}}
.mask img{{position:absolute;inset:0;width:100%;height:100%}} .ad span,.sm span{{display:block;font-size:13px;opacity:.75;margin-top:6px}}
.safe{{position:absolute;left:50%;top:50%;width:61%;height:61%;transform:translate(-50%,-50%);border:2px dashed #fff8;border-radius:50%}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin:0 0 16px}} .sm span{{width:120px;margin:0}}
</style>
<h2>Candidatos (tile = rosto)</h2><div class="row">{tiles}</div>
<h2>Prova por gênero (o dragão continua roxo, só o acessório muda)</h2><div class="row">{genre}</div>
<h2>Adaptive icon do candidato A (a linha tracejada é a área segura de 66dp)</h2><div class="row">{adaptive}</div>
<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{small}
"""
    (HERE / "preview.html").write_text(html)


def render_png():
    PNG.mkdir(exist_ok=True)
    for svg in sorted(SVG.glob("*.svg")):
        out = PNG / (svg.stem + ".png")
        subprocess.run(
            [CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
             "--window-size=1024,1024", f"--screenshot={out}", f"file://{svg}"],
            check=True, capture_output=True,
        )
    subprocess.run(
        [CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1500,1700",
         f"--screenshot={HERE / 'preview.png'}", f"file://{HERE / 'preview.html'}"],
        check=True, capture_output=True,
    )


if __name__ == "__main__":
    build_svgs()
    build_preview()
    if "--png" in sys.argv:
        render_png()
    print("ok")
