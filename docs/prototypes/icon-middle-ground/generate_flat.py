#!/usr/bin/env python3
"""Rounds 6 and 7 of the icon study: the ground in the dragon's own colour, then the small face details.

Round 6 was the user's simplest brief: the current icon with the ground painted in the dragon's colour (a
vertical gradient with the shipped head's own stops), no shadow, and only the shipped eyes (traced from
`res/drawable-nodpi/ic_dragon_fg_light.png`), zoomed about 6x and placed where they are in the shipped icon.

Round 7 adds the small details of the face, in the language of the shipped icon (tone on tone, no outlines, no
pupils, no horns, no whiskers):

  eyes   the 2 shipped eyes, unchanged
  Leve        + 1 pair of nostrils, 1 smile                                         = 5 shapes
  Completo    + a soft muzzle (a lift of light, no edge) and a small tick at each end of the smile   = 8 shapes

    python3 generate_flat.py          # writes svg/flat_*.svg and flat_preview.html
    python3 generate_flat.py --png    # also rasterises with headless Chrome
"""
import subprocess
import sys

import generate as g
import generate_middle as m
import generate_zoom as z

HERE, SVG, PNG = g.HERE, g.SVG, g.PNG
HX = 512

# Light mode: the dark head. Dark mode: the lilac head, whose eyes show the dark ground behind them, as shipped.
# The skin is a vertical gradient with the shipped head's own stops; there is no gloss, rim light or shadow.
LIGHT = dict(skin=("#382D66", "#1E1535", "#0E0920"), eye="#FFFFFF",
             nostril="#0B0719", line="#0B0719")
DARK = dict(skin=("#E1D2FF", "#B796FF", "#9560FD"), eye="#10081D",
            nostril="#6A3FD0", line="#6A3FD0")
SAFE = .80  # keeps the eyes and mouth inside the 66dp safe circle
# In the shipped art the middle of the eyes sits at 258.5 of 432, about 60% down; the first zoom put them at 43%.
z.EYE_Y = round(1024 * 258.5 / 432)


def skin_defs(p):
    a, b, c = p["skin"]
    return (f'<defs><linearGradient id="skin" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{a}"/>'
            f'<stop offset=".5" stop-color="{b}"/><stop offset="1" stop-color="{c}"/></linearGradient></defs>')


def eye_paths(eyes):
    oy = sum(m.bbox(eyes[0])[i] for i in (1, 3)) / 2
    return [m.to_path(z.to_tile(e, oy)) for e in eyes]


def f(a, b):
    return f"{a:.1f},{b:.1f}"


def drop(k):
    """A small version of the eye's drop: round at the outer top, a point at the inner bottom (k=-1 left, 1 right)."""
    def q(dx, y):
        return f(HX + k * dx, y)
    return (f"M{q(30, 820)} C{q(28, 792)} {q(36, 766)} {q(58, 762)} C{q(82, 758)} {q(88, 792)} {q(68, 804)} "
            f"C{q(56, 810)} {q(42, 816)} {q(30, 820)} Z")


def details(p, level, mono=False):
    """Only the two small drop-shaped nostrils, echoing the eyes; no muzzle, no mouth."""
    if level == "base":
        return ""
    return f'<path d="{drop(-1)} {drop(1)}" fill="{"#000" if mono else p["nostril"]}"/>'


def tile(p, paths, level):
    eyes = "".join(f'<path d="{d}" fill="{p["eye"]}"/>' for d in paths)
    return (f'{skin_defs(p)}<path d="{g.TILE}" fill="url(#skin)"/><g clip-path="url(#tclip)">'
            f'{eyes}{details(p, level)}</g>').replace(
        '<g clip-path="url(#tclip)">', f'<clipPath id="tclip"><path d="{g.TILE}"/></clipPath><g clip-path="url(#tclip)">')


LEVELS = (("base", "Só os olhos", "3 formas"), ("focinho", "Com focinho", "5 formas: + 2 narinas em gota"))


def build():
    SVG.mkdir(exist_ok=True)
    paths = eye_paths(z.shipped_eyes())
    for mode, p in (("light", LIGHT), ("dark", DARK)):
        for level, _, _ in LEVELS:
            (SVG / f"flat_{level}_{mode}.svg").write_text(g.wrap(tile(p, paths, level)))
        eyes = "".join(f'<path d="{d}" fill="{p["eye"]}"/>' for d in paths)
        (SVG / f"flat_adaptive_background_{mode}.svg").write_text(
            g.wrap(f'{skin_defs(p)}<rect width="1024" height="1024" fill="url(#skin)"/>'))
        (SVG / f"flat_adaptive_foreground_{mode}.svg").write_text(
            g.wrap(skin_defs(p) + g.scaled(eyes + details(p, "focinho"), SAFE)))
        if mode == "light":
            mono = "".join(f'<path d="{d}" fill="#000"/>' for d in paths) + details(p, "focinho", mono=True)
            (SVG / "flat_adaptive_monochrome.svg").write_text(g.wrap(g.scaled(mono, SAFE)))

    def card(src, title, note):
        return f'<figure><img src="{src}" style="border-radius:22%"><figcaption><b>{title}</b><br>{note}</figcaption></figure>'

    rows = ""
    for mode, label in (("light", "claro"), ("dark", "escuro")):
        rows += '<div class="row">' + card(f"reference/current_gota_{mode}.png", f"Atual ({label})", "cabeça sobre o fundo")
        for level, title, note in LEVELS:
            rows += '<span class="arrow">→</span>' + card(f"svg/flat_{level}_{mode}.svg", f"{title} ({label})", note)
        rows += "</div>"
    sizes = ""
    for label, src in (("Atual", "reference/current_gota_light.png"), ("Só olhos claro", "svg/flat_base_light.svg"),
                       ("Focinho claro", "svg/flat_focinho_light.svg"),
                       ("Só olhos escuro", "svg/flat_base_dark.svg"), ("Focinho escuro", "svg/flat_focinho_dark.svg")):
        sizes += f'<div class="sm"><span>{label}</span>' + "".join(
            f'<img src="{src}" width="{s}" height="{s}" style="border-radius:22%">' for s in (144, 96, 72, 48)) + "</div>"
    masks = ""
    for label, css, mode in (("círculo", "50%", "light"), ("squircle", "30%", "light"), ("quadrado", "10%", "light"), ("escuro", "50%", "dark")):
        masks += (f'<div class="ad"><div class="mask" style="border-radius:{css}"><img src="svg/flat_adaptive_background_{mode}.svg">'
                  f'<img src="svg/flat_adaptive_foreground_{mode}.svg"><div class="safe"></div></div><span>{label}</span></div>')
    masks += ('<div class="ad"><div class="mask" style="border-radius:50%;background:#2c2540">'
              '<img src="svg/flat_adaptive_monochrome.svg" style="filter:invert(1) sepia(1) saturate(3) hue-rotate(220deg) brightness(1.1)">'
              '</div><span>temático (mono)</span></div>')
    html = f"""<!doctype html><meta charset="utf-8"><title>Detalhes do rosto</title>
<style>
body{{margin:0;padding:32px;background:#0b0b0d;color:#e8e6ef;font:15px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:30px 0 12px}} .row{{display:flex;gap:8px;align-items:center;margin-bottom:18px}}
figure{{margin:0;padding:10px;border-radius:18px;text-align:center;background:#000;width:236px}} figure img{{width:236px;height:236px;display:block}}
figcaption{{margin-top:8px;font-size:12px;opacity:.85}} .arrow{{font-size:22px;opacity:.5}}
.ad{{display:inline-block;margin-right:14px;text-align:center}} .mask{{position:relative;width:190px;height:190px;overflow:hidden;background:#222}} .mask img{{position:absolute;inset:0;width:100%;height:100%}}
.ad span,.sm span{{display:block;font-size:13px;opacity:.75;margin-top:6px}} .safe{{position:absolute;left:50%;top:50%;width:61%;height:61%;transform:translate(-50%,-50%);border:2px dashed #fff8;border-radius:50%}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin:0 0 12px}} .sm span{{width:120px;margin:0}}
</style>
<h2>Do ícone atual ao focinho, tom sobre tom, sem boca, só as narinas</h2>{rows}
<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{sizes}
<h2>Adaptive icon com focinho (tracejado = área segura de 66dp)</h2><div>{masks}</div>
"""
    (HERE / "flat_preview.html").write_text(html)


def render():
    PNG.mkdir(exist_ok=True)
    for level, _, _ in LEVELS:
        for mode in ("light", "dark"):
            name = f"flat_{level}_{mode}"
            subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                            "--window-size=1024,1024", f"--screenshot={PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
                           check=True, capture_output=True)
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1400,1900",
                    f"--screenshot={HERE / 'flat_preview.png'}", f"file://{HERE / 'flat_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
