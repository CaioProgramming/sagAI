#!/usr/bin/env python3
"""Round 14 of the icon study: the Shinobi variant, a ninja hood and mask.

The skin and eyes are the approved base in Shinobi's primary colour (`generate_genres.py`), eyes white in both modes.
v1 was a headband with a swirl plate; the user did not want the Naruto look, so v2 is a plain ninja hood and mask:

  - the hood covers the top of the tile, its lower edge slanting down towards the nose (a fierce brow)
  - the mask covers the bottom of the tile, hiding the nostrils, its upper edge rising a little over the nose bridge
  - between them only a strip of the red skin shows, with the eyes: the classic ninja eye slit
  - tone on tone detail only: a dashed seam along both cloth edges (two folds on the mask read as cat whiskers: removed)

    python3 generate_shinobi.py          # writes svg/shinobi_*.svg and shinobi_preview.html
    python3 generate_shinobi.py --png    # also rasterises with headless Chrome
"""
import subprocess
import sys

import generate as g
import generate_flat as fl
import generate_genres as gen
import generate_zoom as z

HERE, SVG, PNG = g.HERE, g.SVG, g.PNG
PRIMARY = "#8B0000"
CLOTH = {"light": ("#1B1D27", "#0E0F15", "#3A3E52"), "dark": ("#14161E", "#08090D", "#343848")}  # fill, shade, seam
HOOD_EDGE = "M-20,412 Q512,528 1044,412"
MASK_EDGE = "M-20,792 Q512,690 1044,792"


def hood(mode):
    fill, shade, seam = CLOTH[mode]
    return (f'<path d="{HOOD_EDGE} V-20 H-20 Z" fill="{fill}"/>'
            f'<path d="{HOOD_EDGE}" fill="none" stroke="{shade}" stroke-width="22" stroke-linecap="round"/>'
            f'<path d="M-20,372 Q512,488 1044,372" fill="none" stroke="{seam}" stroke-width="9" stroke-dasharray="26 18" stroke-linecap="round"/>')


def mask(mode):
    fill, shade, seam = CLOTH[mode]
    return (f'<path d="{MASK_EDGE} V1044 H-20 Z" fill="{fill}"/>'
            f'<path d="{MASK_EDGE}" fill="none" stroke="{shade}" stroke-width="22" stroke-linecap="round"/>'
            f'<path d="M-20,832 Q512,730 1044,832" fill="none" stroke="{seam}" stroke-width="9" stroke-dasharray="26 18" stroke-linecap="round"/>'
            )


def svg(mode, paths):
    p = gen.palette(PRIMARY, mode)
    eyes = "".join(f'<path d="{d}" fill="#FFFFFF"/>' for d in paths)
    return g.wrap(f'{fl.skin_defs(p)}<clipPath id="tclip"><path d="{g.TILE}"/></clipPath>'
                  f'<path d="{g.TILE}" fill="url(#skin)"/><g clip-path="url(#tclip)">{eyes}{hood(mode)}{mask(mode)}</g>')


def build():
    SVG.mkdir(exist_ok=True)
    paths = fl.eye_paths(z.shipped_eyes())
    for mode in ("light", "dark"):
        (SVG / f"shinobi_{mode}.svg").write_text(svg(mode, paths))
    big = ""
    for mode, label in (("light", "claro"), ("dark", "escuro")):
        big += (f'<div class="row"><figure><img src="svg/genre_shinobi_{mode}.svg"><figcaption>Base ({label})</figcaption></figure>'
                f'<span class="arrow">→</span><figure><img src="svg/shinobi_v1_{mode}.svg"><figcaption>v1, testeira ({label})</figcaption></figure>'
                f'<span class="arrow">→</span><figure><img src="svg/shinobi_{mode}.svg"><figcaption>v2, capuz e máscara ({label})</figcaption></figure></div>')
    small = "".join(
        f'<div class="sm"><span>{label}</span>' + "".join(f'<img src="svg/shinobi_{mode}.svg" style="width:{s}px;height:{s}px">' for s in (144, 96, 72, 48)) + "</div>"
        for mode, label in (("light", "Shinobi claro"), ("dark", "Shinobi escuro")))
    html = f"""<!doctype html><meta charset="utf-8"><title>Shinobi</title>
<style>
body{{margin:0;padding:28px;background:#0b0b0d;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:24px 0 12px}} .row{{display:flex;gap:10px;align-items:center;margin-bottom:16px}}
figure{{margin:0;padding:10px;border-radius:16px;background:#000;text-align:center}} figure img{{width:300px;height:300px;display:block;border-radius:22%}}
figcaption{{margin-top:8px;font-size:12px;opacity:.85}} .arrow{{font-size:22px;opacity:.5}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin-bottom:12px}} .sm span{{width:110px;font-size:13px;opacity:.75}} .sm img{{border-radius:22%}}
</style>
<h2>Shinobi: da base ao capuz e máscara ninja</h2>{big}<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{small}"""
    (HERE / "shinobi_preview.html").write_text(html)


def render():
    PNG.mkdir(exist_ok=True)
    for mode in ("light", "dark"):
        name = f"shinobi_{mode}"
        subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                        "--window-size=1024,1024", f"--screenshot={PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
                       check=True, capture_output=True)
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1500,1100",
                    f"--screenshot={HERE / 'shinobi_preview.png'}", f"file://{HERE / 'shinobi_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
