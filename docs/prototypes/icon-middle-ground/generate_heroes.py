#!/usr/bin/env python3
"""Round 16 of the icon study: the Heroes variant, a Robin / Nightwing style eye mask.

The skin and nostrils are the approved base in Heroes' primary colour (`generate_genres.py`). The accessory is one
object, from the user's references (Nightwing, Red Robin, Batwoman): a black domino mask across the eyes, wide, with a
pointed spike at each outer corner and a notch at the nose bridge. The eyes sit behind it, as narrow white slits.

    python3 generate_heroes.py          # writes svg/heroes_*.svg and heroes_preview.html
    python3 generate_heroes.py --png    # also rasterises with headless Chrome
"""
import subprocess
import sys

import generate as g
import generate_flat as fl
import generate_genres as gen
import generate_zoom as z

HERE, SVG, PNG = g.HERE, g.SVG, g.PNG
PRIMARY = "#2845D6"
MASK = "#080A16"
SLIT = 0.6  # eye height relative to the base drop: the eyes are slits seen through the mask
EYE_CY = 615
TILT = 9  # degrees: the eyes lean down towards the nose (outer corner high), a fiercer look
EYE_CX = (268, 756)  # centres of the left and right base eyes

# Left half of a Zorro style mask (v3): one plain band that runs the full width of the face, off both edges of the tile,
# with a soft arch along the top that dips a little at the brow, and a lower edge that sweeps down over the cheeks and
# lifts over the nose bridge. No spikes. It is closed past the centre line (x=540) so that the mirrored half overlaps it
# without a seam.
# v4 (between Zorro and Nightwing): at the outer end the top edge swoops UP in a concave curve, as the user sketched on
# v3, rising to the tile edge, and the lower edge drops a little at the outer end for balance.
HALF = ("M-20,322 Q96,440 220,438 Q390,430 512,476 L540,476 L540,722 L512,722 Q430,758 330,790 Q230,822 -20,868 Z")


def mask_path():
    return HALF


def svg(mode, paths):
    p = gen.palette(PRIMARY, mode)
    mirrored = f'<g transform="translate(1024,0) scale(-1,1)"><path d="{HALF}"/></g>'
    eyes = ""
    for d, cx, tilt in zip(paths, EYE_CX, (TILT, -TILT)):
        eyes += (f'<g transform="translate({cx},{EYE_CY}) rotate({tilt}) scale(1,{SLIT}) translate({-cx},{-EYE_CY})">'
                 f'<path d="{d}" fill="#FFFFFF"/></g>')
    return g.wrap(f'{fl.skin_defs(p)}<clipPath id="tclip"><path d="{g.TILE}"/></clipPath>'
                  f'<path d="{g.TILE}" fill="url(#skin)"/><g clip-path="url(#tclip)">{fl.details(p, "focinho")}'
                  f'<g fill="{MASK}"><path d="{mask_path()}"/>{mirrored}</g>{eyes}</g>')


def build():
    SVG.mkdir(exist_ok=True)
    paths = fl.eye_paths(z.shipped_eyes())
    for mode in ("light", "dark"):
        (SVG / f"heroes_{mode}.svg").write_text(svg(mode, paths))
    big = ""
    for mode, label in (("light", "claro"), ("dark", "escuro")):
        big += (f'<div class="row"><figure><img src="svg/genre_heroes_{mode}.svg"><figcaption>Base ({label})</figcaption></figure>'
                f'<span class="arrow">→</span><figure><img src="svg/heroes_{mode}.svg"><figcaption>Máscara ({label})</figcaption></figure></div>')
    small = "".join(
        f'<div class="sm"><span>{label}</span>' + "".join(f'<img src="svg/heroes_{mode}.svg" style="width:{s}px;height:{s}px">' for s in (144, 96, 72, 48)) + "</div>"
        for mode, label in (("light", "Heróis claro"), ("dark", "Heróis escuro")))
    html = f"""<!doctype html><meta charset="utf-8"><title>Heróis</title>
<style>
body{{margin:0;padding:28px;background:#0b0b0d;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:24px 0 12px}} .row{{display:flex;gap:10px;align-items:center;margin-bottom:16px}}
figure{{margin:0;padding:10px;border-radius:16px;background:#000;text-align:center}} figure img{{width:300px;height:300px;display:block;border-radius:22%}}
figcaption{{margin-top:8px;font-size:12px;opacity:.85}} .arrow{{font-size:22px;opacity:.5}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin-bottom:12px}} .sm span{{width:110px;font-size:13px;opacity:.75}} .sm img{{border-radius:22%}}
</style>
<h2>Heróis: da base à máscara de herói</h2>{big}<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{small}"""
    (HERE / "heroes_preview.html").write_text(html)


def render():
    PNG.mkdir(exist_ok=True)
    for mode in ("light", "dark"):
        name = f"heroes_{mode}"
        subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                        "--window-size=1024,1024", f"--screenshot={PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
                       check=True, capture_output=True)
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1150,1100",
                    f"--screenshot={HERE / 'heroes_preview.png'}", f"file://{HERE / 'heroes_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
