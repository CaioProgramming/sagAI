#!/usr/bin/env python3
"""Round 3 of the icon study: the dragon base cut down to the reference's minimalism.

Round 2 (generate_base.py) kept too much of the old icon. The rule now, straight from the pumpkin and
the mummy: a flat colour tile, two big eyes, at most ONE small feature, and short horns only because
without them nothing says dragon at 48px (round 1 proved it: a purple tile with two eyes reads as a robot).

Shape budget, counted as drawn marks on the face (tile shading and the eyes' inner shadow are treatment, not marks):

  primary   2 eyes + 2 horns + 1 smile            = 5
  no-horns  2 eyes + 1 smile                      = 3 (kept only to show why a dragon cue stays)

Tried and dropped: a three-spike forehead crest (reads as a crown) and two side fins (illegible small).

    python3 generate_minimal.py          # writes svg/min_*.svg and min_preview.html
    python3 generate_minimal.py --png    # also rasterises with headless Chrome
"""
import subprocess
import sys

import generate as g

HERE, SVG = g.HERE, g.SVG

LIGHT = dict(top="#6C4AE0", bottom="#5A3AC6", eye="#1B0A42", inner="#2F1572", horn_a="#CDB9FF", horn_b="#9B7AF8",
             feat="#1B0A42", rim="#FFFFFF", rim_o=.4)
DARK = dict(top="#1E1248", bottom="#0C0723", eye="#EDE3FF", inner="#C4ADFF", horn_a="#DCCBFF", horn_b="#9B7BFF",
            feat="#EDE3FF", rim="#C4B0FF", rim_o=.9)

EYES = ((322, 540), (702, 540))
EYE_W, EYE_H, EYE_R = 236, 290, 118


def eyes(p, tag):
    return "".join(
        g.eye_rr(f"m_{tag}{i}", cx, cy, EYE_W, EYE_H, EYE_R, p["eye"], p["inner"], 26, 22) for i, (cx, cy) in enumerate(EYES)
    )


def _soft(p, tag, d):
    """A flat-colour mark with rounded tips: the stroke is the same gradient as the fill."""
    return (
        f'<linearGradient id="mh_{tag}" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{p["horn_a"]}"/>'
        f'<stop offset="1" stop-color="{p["horn_b"]}"/></linearGradient>'
        f'<path d="{d}" fill="url(#mh_{tag})" stroke="url(#mh_{tag})" stroke-width="16" stroke-linejoin="round"/>'
    )


def horns(p, tag):
    """Two short, fat, nearly upright horns with rounded tips. Wide-set and swept hard outward they
    read as cat ears, so these lean only a little."""
    return (
        _soft(p, tag + "l", "M276,350 Q266,280 290,214 Q362,276 378,350 Z")
        + _soft(p, tag + "r", "M748,350 Q758,280 734,214 Q662,276 646,350 Z")
    )


def smile(p):
    return f'<path d="M452,778 Q512,850 572,778 Z" fill="{p["feat"]}"/>'


def tile(p, mark, dark):
    glow = '<filter id="glow" x="-20%" y="-20%" width="140%" height="140%"><feGaussianBlur stdDeviation="16"/></filter>'
    body = g.common_defs(p["top"], p["bottom"], rim=p["rim"], rim_opacity=p["rim_o"], extra=glow)
    if dark:
        body += f'<path d="{g.TILE}" fill="none" stroke="#9B7BFF" stroke-width="22" opacity=".7" filter="url(#glow)"/>'
    body += f'<path d="{g.TILE}" fill="url(#base)"/><g clip-path="url(#tile)">'
    body += (mark(p, "t") if mark else "") + eyes(p, "t") + smile(p)
    body += '<rect width="1024" height="1024" fill="url(#shade)"/>'
    body += '<rect width="1024" height="1024" fill="url(#gloss)"/>' if not dark else ""
    return body + f'</g><path d="{g.TILE}" fill="none" stroke="url(#rim)" stroke-width="10"/>'


SAFE = 0.80


def layers(p):
    bg = (
        f'<defs><linearGradient id="b" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{p["top"]}"/>'
        f'<stop offset="1" stop-color="{p["bottom"]}"/></linearGradient></defs><rect width="1024" height="1024" fill="url(#b)"/>'
    )
    fg = g.scaled(horns(p, "a") + eyes(p, "a") + smile(p), SAFE)
    mono_p = dict(p, eye="#000", inner="#000", feat="#000", horn_a="#000", horn_b="#000")
    mono = g.scaled(horns(mono_p, "m") + eyes(mono_p, "m") + smile(mono_p), SAFE)
    return bg, fg, mono


def build():
    SVG.mkdir(exist_ok=True)
    files = {
        "min_light": tile(LIGHT, horns, False),
        "min_dark": tile(DARK, horns, True),
        "min_nohorns_light": tile(LIGHT, None, False),
    }
    for name, body in files.items():
        (SVG / f"{name}.svg").write_text(g.wrap(body))
    for mode, p in (("light", LIGHT), ("dark", DARK)):
        bg, fg, mono = layers(p)
        (SVG / f"min_adaptive_background_{mode}.svg").write_text(g.wrap(bg))
        (SVG / f"min_adaptive_foreground_{mode}.svg").write_text(g.wrap(fg))
        if mode == "light":
            (SVG / "min_adaptive_monochrome.svg").write_text(g.wrap(mono))

    def fig(src, label):
        return f'<figure><img src="{src}" alt="{label}"><figcaption>{label}</figcaption></figure>'

    main = (
        fig("svg/min_light.svg", "Base: 5 formas (2 olhos, 2 chifres, 1 sorriso)")
        + fig("svg/min_dark.svg", "Base, escuro")
        + fig("svg/min_nohorns_light.svg", "Sem chifres: 3 formas, lê como robô")
    )
    sizes = ""
    for label, src in (
        ("Base", "svg/min_light.svg"), ("Base escura", "svg/min_dark.svg"),
        ("Sem chifres", "svg/min_nohorns_light.svg"), ("Antigo", "reference/old_duo_light.webp"),
    ):
        sizes += f'<div class="sm"><span>{label}</span>' + "".join(
            f'<img src="{src}" width="{s}" height="{s}">' for s in (144, 96, 72, 48)
        ) + "</div>"
    masks = ""
    for label, css, mode in (("círculo", "border-radius:50%", "light"), ("squircle", "border-radius:30%", "light"),
                             ("quadrado", "border-radius:10%", "light"), ("escuro", "border-radius:50%", "dark")):
        masks += (
            f'<div class="ad"><div class="mask" style="{css}"><img src="svg/min_adaptive_background_{mode}.svg">'
            f'<img src="svg/min_adaptive_foreground_{mode}.svg"><div class="safe"></div></div><span>{label}</span></div>'
        )
    masks += (
        '<div class="ad"><div class="mask" style="border-radius:50%;background:#2c2540">'
        '<img src="svg/min_adaptive_monochrome.svg" style="filter:invert(1) sepia(1) saturate(3) hue-rotate(220deg) brightness(1.1)">'
        '</div><span>temático (mono)</span></div>'
    )
    html = f"""<!doctype html><meta charset="utf-8"><title>Base mínima do dragão</title>
<style>
body{{margin:0;padding:32px;background:#0b0b0d;color:#e8e6ef;font:15px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:32px 0 12px}} .row{{display:flex;gap:18px;flex-wrap:wrap}}
figure{{margin:0;padding:14px;border-radius:20px;text-align:center;background:#000}} figure img{{width:280px;height:280px;display:block}}
figcaption{{margin-top:8px;font-size:13px;opacity:.75;max-width:280px}}
.ad{{text-align:center}} .mask{{position:relative;width:190px;height:190px;overflow:hidden;background:#222}}
.mask img{{position:absolute;inset:0;width:100%;height:100%}} .ad span,.sm span{{display:block;font-size:13px;opacity:.75;margin-top:6px}}
.safe{{position:absolute;left:50%;top:50%;width:61%;height:61%;transform:translate(-50%,-50%);border:2px dashed #fff8;border-radius:50%}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin:0 0 12px}} .sm span{{width:110px;margin:0}}
</style>
<h2>Base mínima</h2><div class="row">{main}</div>
<h2>Legibilidade em 144, 96, 72 e 48 px (com a versão sem chifres e o ícone antigo)</h2>{sizes}
<h2>Adaptive icon (tracejado = área segura de 66dp)</h2><div class="row">{masks}</div>
"""
    (HERE / "min_preview.html").write_text(html)


def render():
    g.PNG.mkdir(exist_ok=True)
    for name in ("min_light", "min_dark"):
        subprocess.run(
            [g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
             "--window-size=1024,1024", f"--screenshot={g.PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
            check=True, capture_output=True,
        )
    subprocess.run(
        [g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1500,1250",
         f"--screenshot={HERE / 'min_preview.png'}", f"file://{HERE / 'min_preview.html'}"],
        check=True, capture_output=True,
    )


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
