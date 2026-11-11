#!/usr/bin/env python3
"""Round 2 of the icon study: one solid dragon base, before any variant.

Round 1 (generate.py) borrowed the pumpkin's boards and the mummy's strips. That texture means
nothing for a dragon, so it is gone. What stays from the reference is the *structure* (the tile is
the face, big eyes, soft inner shading, no outlines); what makes it a dragon comes from the old
Duo-style icon (reference/old_duo_*.webp), the way Duolingo's brows and beak make its owl:

  - the feathered brow crests that sweep over the eyes (the strongest cue),
  - big eyes with a pupil and a glint (the character),
  - the bridge ridges between the eyes,
  - the snout pad with two nostrils,
  - the wide smile with its cheek ticks,
  - the two long whiskers.

What is simplified against the old one: fewer tufts on the crest, no lilac lid, two glints instead of
a stack of highlights, one smooth mouth line instead of mouth plus chin outline, shapes made bolder and
flatter, and the soft shading of the reference put on top.

    python3 generate_base.py          # writes svg/base_*.svg and base_preview.html
    python3 generate_base.py --png    # also rasterises them with headless Chrome
"""
import math
import pathlib
import subprocess
import sys

import generate as g

HERE = g.HERE
SVG = g.SVG


def mirror(inner):
    return f'<g transform="translate(1024 0) scale(-1 1)">{inner}</g>'


def palette(dark):
    if dark:
        return dict(
            top="#1E1245", bottom="#0C0723", crest_a="#5B38B4", crest_b="#37207F", crest_rim="#B9A2FF",
            pad_a="#33207A", pad_b="#25155E", nostril="#080320", ridge="#6B4BC4", mouth="#080320",
            whisker="#9677EA", eye_lo="#C9B6FF", pupil="#07031A",
        )
    return dict(
        top="#472A90", bottom="#2E1A62", crest_a="#7448C6", crest_b="#4E2C98", crest_rim="#A98BFF",
        pad_a="#6A42BC", pad_b="#52309E", nostril="#1A0B40", ridge="#6C46BC", mouth="#1A0D3C",
        whisker="#8157D4", eye_lo="#D8CBF6", pupil="#0B0716",
    )


EYE_C = (322, 472)


def _pt(angle, radius):
    a = math.radians(angle)
    return EYE_C[0] + radius * math.cos(a), EYE_C[1] + radius * math.sin(a)


def _crest():
    """Left-hand brow crest, built on polar points around the eye so the tufts follow its curve:
    four feather tips swept toward the middle of the face, and one smooth inner edge that hugs the
    eye. Returns (closed shape, outer edge only) so the rim light can follow just the tufts."""
    # Angles and radii are read off the old icon (reference/old_duo_light.webp): three tufts packed on
    # the outer-top of the eye, the tallest nearest the middle, then one smooth bulge down to the nose.
    seq = [(193, 138), (221, 216), (230, 166), (240, 240), (251, 178), (263, 268)]
    pts = [_pt(a, r) for a, r in seq]
    ctrl, end = _pt(304, 290), _pt(342, 168)
    outer = "M" + " L".join(f"{x:.0f},{y:.0f}" for x, y in pts) + f" Q{ctrl[0]:.0f},{ctrl[1]:.0f} {end[0]:.0f},{end[1]:.0f}"
    ix, iy = pts[0]
    return f"{outer} A128,128 0 0 0 {ix:.0f},{iy:.0f} Z", outer


CREST, CREST_OUTER = _crest()


def half(side, p, dark):
    eye = 'cx="322" cy="472" rx="100" ry="118"'
    eye_tf = 'transform="rotate(10 322 472)"'
    out = [
        f'<clipPath id="eye_{side}"><ellipse {eye} {eye_tf}/></clipPath>',
        f'<linearGradient id="sclera_{side}" x1="0" y1="0" x2="0" y2="1">'
        f'<stop offset="0" stop-color="{p["eye_lo"]}"/><stop offset=".34" stop-color="#FFFFFF"/></linearGradient>',
    ]
    if dark:
        out.append(f'<ellipse {eye} {eye_tf} fill="#CDB8FF" opacity=".55" filter="url(#glow)"/>')
    out += [
        # crest, with a thin light edge on its outer side
        f'<path d="{CREST}" fill="url(#crest)" stroke="url(#crest)" stroke-width="12" stroke-linejoin="round"/>',
        f'<path d="{CREST_OUTER}" fill="none" stroke="{p["crest_rim"]}" stroke-width="6" stroke-linejoin="round" '
        f'opacity="{.9 if dark else .16}"/>',
        # eye
        f'<ellipse {eye} {eye_tf} fill="url(#sclera_{side})"/>',
        f'<g clip-path="url(#eye_{side})">'
        f'<ellipse cx="338" cy="490" rx="64" ry="78" fill="{p["pupil"]}"/>'
        f'<circle cx="306" cy="454" r="21" fill="#fff"/><circle cx="360" cy="536" r="8" fill="#fff" opacity=".85"/></g>',
        # smile tick and whisker
        f'<path d="M232,636 Q256,626 272,656" fill="none" stroke="{p["mouth"]}" stroke-width="14" stroke-linecap="round"/>',
        f'<path d="M134,752 C226,770 296,716 340,690 C370,672 400,668 430,670" fill="none" '
        f'stroke="{p["whisker"]}" stroke-width="8" stroke-linecap="round" opacity=".8"/>',
        f'<path d="M448,634 Q484,628 490,664 Q452,672 448,634 Z" fill="{p["nostril"]}"/>',
    ]
    return "".join(out)


def center(p):
    ridges = "".join(
        f'<path d="M{512 - w},{y + 14} Q512,{y - 12} {512 + w},{y + 14}" fill="none" stroke="{p["ridge"]}" '
        f'stroke-width="9" stroke-linecap="round" opacity=".9"/>'
        for w, y in ((42, 488), (68, 522), (90, 558))
    )
    pad = (
        '<path d="M418,640 Q420,598 472,600 Q512,588 552,600 Q604,598 606,640 Q596,694 512,698 Q428,694 418,640 Z" '
        'fill="url(#pad)"/>'
        '<path d="M440,614 Q512,590 584,614" fill="none" stroke="#fff" stroke-width="7" stroke-linecap="round" opacity=".2"/>'
    )
    mouth = (
        f'<path d="M262,652 Q300,744 512,752 Q724,744 762,652" '
        f'fill="none" stroke="{p["mouth"]}" stroke-width="15" stroke-linecap="round" stroke-linejoin="round"/>'
    )
    return ridges + pad, mouth


def defs(p, dark):
    extra = (
        f'<linearGradient id="crest" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{p["crest_a"]}"/>'
        f'<stop offset="1" stop-color="{p["crest_b"]}"/></linearGradient>'
        f'<linearGradient id="pad" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{p["pad_a"]}"/>'
        f'<stop offset="1" stop-color="{p["pad_b"]}"/></linearGradient>'
        '<filter id="glow" x="-40%" y="-40%" width="180%" height="180%"><feGaussianBlur stdDeviation="22"/></filter>'
        '<filter id="rimglow" x="-20%" y="-20%" width="140%" height="140%"><feGaussianBlur stdDeviation="16"/></filter>'
    )
    return g.common_defs(
        p["top"], p["bottom"], rim="#C4B0FF" if dark else "#FFFFFF", rim_opacity=0.85 if dark else 0.35, extra=extra
    )


def face(p, dark, s=1.0):
    ridges_pad, mouth = center(p)
    inner = (
        ridges_pad + half("l", p, dark) + mirror(half("r", p, dark)) + mouth
    )
    return g.scaled(inner, s)


def tile(dark=False):
    p = palette(dark)
    body = defs(p, dark)
    if dark:
        body += f'<path d="{g.TILE}" fill="none" stroke="#9B7BFF" stroke-width="22" opacity=".7" filter="url(#rimglow)"/>'
    body += f'<path d="{g.TILE}" fill="url(#base)"/><g clip-path="url(#tile)">'
    body += face(p, dark)
    body += '<rect width="1024" height="1024" fill="url(#shade)"/>'
    body += '<rect width="1024" height="1024" fill="url(#gloss)"/>' if not dark else ""
    body += f'</g><path d="{g.TILE}" fill="none" stroke="url(#rim)" stroke-width="10"/>'
    return body


SAFE = 0.78


def adaptive_bg(dark=False):
    p = palette(dark)
    return (
        f'<defs><linearGradient id="b" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{p["top"]}"/>'
        f'<stop offset="1" stop-color="{p["bottom"]}"/></linearGradient></defs><rect width="1024" height="1024" fill="url(#b)"/>'
    )


def adaptive_fg(dark=False):
    p = palette(dark)
    return defs(p, dark) + face(p, dark, SAFE)


def adaptive_mono():
    eye = '<ellipse cx="322" cy="472" rx="100" ry="118" transform="rotate(10 322 472)" fill="#000"/>'
    inner = (
        f'<path d="{CREST}" fill="#000"/>' + eye
        + mirror(f'<path d="{CREST}" fill="#000"/>' + eye)
        + '<path d="M270,648 Q304,706 376,716 Q442,724 470,742 Q512,756 554,742 Q582,724 648,716 Q720,706 754,648" '
        'fill="none" stroke="#000" stroke-width="22" stroke-linecap="round" stroke-linejoin="round"/>'
    )
    return g.scaled(inner, SAFE)


def build():
    SVG.mkdir(exist_ok=True)
    (SVG / "base_light.svg").write_text(g.wrap(tile(False)))
    (SVG / "base_dark.svg").write_text(g.wrap(tile(True)))
    (SVG / "base_adaptive_background_light.svg").write_text(g.wrap(adaptive_bg(False)))
    (SVG / "base_adaptive_foreground_light.svg").write_text(g.wrap(adaptive_fg(False)))
    (SVG / "base_adaptive_background_dark.svg").write_text(g.wrap(adaptive_bg(True)))
    (SVG / "base_adaptive_foreground_dark.svg").write_text(g.wrap(adaptive_fg(True)))
    (SVG / "base_adaptive_monochrome.svg").write_text(g.wrap(adaptive_mono()))

    def fig(src, label, bg="#000"):
        return f'<figure style="background:{bg}"><img src="{src}" alt="{label}"><figcaption>{label}</figcaption></figure>'

    compare = (
        fig("reference/old_duo_light.webp", "Antigo (Duo), claro")
        + fig("svg/base_light.svg", "Base nova, claro")
        + fig("reference/old_duo_dark.webp", "Antigo (Duo), escuro")
        + fig("svg/base_dark.svg", "Base nova, escuro")
    )
    sizes = ""
    for label, src in (("Antigo", "reference/old_duo_light.webp"), ("Base clara", "svg/base_light.svg"), ("Base escura", "svg/base_dark.svg")):
        sizes += f'<div class="sm"><span>{label}</span>' + "".join(
            f'<img src="{src}" width="{s}" height="{s}">' for s in (144, 96, 72, 48)
        ) + "</div>"
    masks = ""
    for label, css in (("círculo", "border-radius:50%"), ("squircle", "border-radius:30%"), ("quadrado", "border-radius:10%")):
        masks += (
            f'<div class="ad"><div class="mask" style="{css}"><img src="svg/base_adaptive_background_light.svg">'
            f'<img src="svg/base_adaptive_foreground_light.svg"><div class="safe"></div></div><span>{label}</span></div>'
        )
    masks += (
        '<div class="ad"><div class="mask" style="border-radius:50%"><img src="svg/base_adaptive_background_dark.svg">'
        '<img src="svg/base_adaptive_foreground_dark.svg"><div class="safe"></div></div><span>escuro</span></div>'
        '<div class="ad"><div class="mask" style="border-radius:50%;background:#2c2540">'
        '<img src="svg/base_adaptive_monochrome.svg" style="filter:invert(1) sepia(1) saturate(3) hue-rotate(220deg) brightness(1.1)">'
        '</div><span>temático (mono)</span></div>'
    )
    html = f"""<!doctype html><meta charset="utf-8"><title>Base do dragão</title>
<style>
body{{margin:0;padding:32px;background:#0b0b0d;color:#e8e6ef;font:15px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:32px 0 12px}} .row{{display:flex;gap:18px;flex-wrap:wrap}}
figure{{margin:0;padding:14px;border-radius:20px;text-align:center}} figure img{{width:300px;height:300px;display:block}}
figcaption{{margin-top:8px;font-size:13px;opacity:.75}}
.ad{{text-align:center}} .mask{{position:relative;width:200px;height:200px;overflow:hidden;background:#222}}
.mask img{{position:absolute;inset:0;width:100%;height:100%}} .ad span,.sm span{{display:block;font-size:13px;opacity:.75;margin-top:6px}}
.safe{{position:absolute;left:50%;top:50%;width:61%;height:61%;transform:translate(-50%,-50%);border:2px dashed #fff8;border-radius:50%}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin:0 0 14px}} .sm span{{width:110px;margin:0}}
</style>
<h2>Antigo (referência do usuário) contra a base nova</h2><div class="row">{compare}</div>
<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{sizes}
<h2>Adaptive icon (tracejado = área segura de 66dp)</h2><div class="row">{masks}</div>
"""
    (HERE / "base_preview.html").write_text(html)


def render():
    g.PNG.mkdir(exist_ok=True)
    for name in ("base_light", "base_dark"):
        subprocess.run(
            [g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
             "--window-size=1024,1024", f"--screenshot={g.PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
            check=True, capture_output=True,
        )
    subprocess.run(
        [g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1500,1300",
         f"--screenshot={HERE / 'base_preview.png'}", f"file://{HERE / 'base_preview.html'}"],
        check=True, capture_output=True,
    )


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
