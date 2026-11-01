#!/usr/bin/env python3
"""Round 9 of the icon study: the genre variants, step 1 = the skin only.

The skin is the genre's primary colour from Remote Config (snapshot v1113, `sagas-genre-themes` skill, not re-fetched).
Light mode lightens the primary (a pale tint at the top, the primary at the bottom); dark mode darkens it (the primary at
the top, a deep shade at the bottom). The face is the approved base: the shipped eyes and the two drop nostrils. No
accessories yet.

    python3 generate_genres.py          # writes svg/genre_<genre>_{light,dark}.svg and genres_preview.html
    python3 generate_genres.py --png    # also rasterises with headless Chrome
"""
import subprocess
import sys

import generate as g
import generate_flat as fl
import generate_zoom as z

HERE, SVG, PNG = g.HERE, g.SVG, g.PNG

GENRES = (("fantasy", "Fantasia", "#8B2635"), ("crime", "Crime", "#E91E63"), ("cyberpunk", "Cyberpunk", "#6E48AA"),
          ("space_opera", "Space Opera", "#0081A7"), ("horror", "Terror", "#748B97"), ("shinobi", "Shinobi", "#8B0000"),
          ("cowboy", "Cowboy", "#8B4513"), ("heroes", "Heróis", "#2845D6"), ("punk_rock", "Punk Rock", "#729656"))


def rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5))


def mix(a, b, t):
    """a moved towards b by t (0..1)."""
    return "#%02X%02X%02X" % tuple(round(x + (y - x) * t) for x, y in zip(rgb(a), rgb(b)))


def palette(primary, mode):
    if mode == "light":
        skin = (mix(primary, "#FFFFFF", .62), mix(primary, "#FFFFFF", .32), primary)
        eye = mix(primary, "#000000", .88)
    else:
        skin = (primary, mix(primary, "#000000", .42), mix(primary, "#000000", .78))
        eye = "#FFFFFF"
    return dict(skin=skin, eye=eye, nostril=mix(skin[1], "#000000", .45), line=None)


def build():
    SVG.mkdir(exist_ok=True)
    paths = fl.eye_paths(z.shipped_eyes())
    for key, _, primary in GENRES:
        for mode in ("light", "dark"):
            (SVG / f"genre_{key}_{mode}.svg").write_text(g.wrap(fl.tile(palette(primary, mode), paths, "focinho")))
    cells = ""
    for key, name, primary in GENRES:
        cells += (f'<div class="cell"><b>{name}</b> <code>{primary}</code><div class="pair">'
                  f'<img src="svg/genre_{key}_light.svg"><img src="svg/genre_{key}_dark.svg"></div>'
                  f'<div class="pair small">' + "".join(
                      f'<img src="svg/genre_{key}_{m}.svg" style="width:{s}px;height:{s}px">' for m in ("light", "dark") for s in (72, 48))
                  + '</div></div>')
    html = f"""<!doctype html><meta charset="utf-8"><title>Variações temáticas</title>
<style>
body{{margin:0;padding:28px;background:#0b0b0d;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:0 0 16px}} .grid{{display:grid;grid-template-columns:repeat(3,1fr);gap:22px}}
.cell{{background:#000;border-radius:16px;padding:14px}} code{{opacity:.6;font-size:12px}}
.pair{{display:flex;gap:10px;margin-top:10px;align-items:center}} .pair img{{width:200px;height:200px;border-radius:22%}}
</style>
<h2>Pele = cor principal do gênero. Claro clareia, escuro escurece (esquerda: claro, direita: escuro)</h2>
<div class="grid">{cells}</div>"""
    (HERE / "genres_preview.html").write_text(html)


def render():
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1500,1250",
                    f"--screenshot={HERE / 'genres_preview.png'}", f"file://{HERE / 'genres_preview.html'}"],
                   check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
