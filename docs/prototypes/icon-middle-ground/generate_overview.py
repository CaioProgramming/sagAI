#!/usr/bin/env python3
"""The whole pack on one sheet, to judge the nine genre variants as a set: large in light and dark, then at launcher sizes.

Reads the SVGs the genre generators wrote, so run those first.

    python3 generate_overview.py        # writes overview.html and overview.png
"""
import subprocess

import generate as g

HERE, SVG = g.HERE, g.SVG
PACK = (("fantasy", "Fantasia"), ("crime", "Crime"), ("cyberpunk", "Cyberpunk"), ("horror", "Terror"), ("shinobi", "Shinobi"),
        ("cowboy", "Cowboy"), ("heroes", "Heróis"), ("space_opera_b", "Space Opera"), ("punk_rock", "Punk Rock"))


def row(mode, size, labels=False):
    cells = ""
    for key, name in PACK:
        caption = f"<figcaption>{name}</figcaption>" if labels else ""
        cells += f'<figure><img src="svg/{key}_{mode}.svg" style="width:{size}px;height:{size}px">{caption}</figure>'
    return f'<div class="row">{cells}</div>'


def build():
    missing = [f"{key}_{mode}" for key, _ in PACK for mode in ("light", "dark") if not (SVG / f"{key}_{mode}.svg").exists()]
    if missing:
        raise SystemExit(f"missing SVGs: {missing}")
    html = f"""<!doctype html><meta charset="utf-8"><title>Pacote de ícones</title>
<style>
body{{margin:0;padding:28px;background:#0b0b0d;color:#e8e6ef;font:14px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:24px 0 12px}} .row{{display:flex;gap:18px;align-items:flex-end;margin-bottom:16px}}
figure{{margin:0;text-align:center}} img{{display:block;border-radius:22%}} figcaption{{margin-top:6px;font-size:12px;opacity:.8}}
.light{{background:#E9E6EF;color:#222;padding:16px;border-radius:14px;width:max-content}}
</style>
<h2>Claro</h2><div class="light">{row("light", 170, True)}</div>
<h2>Escuro</h2>{row("dark", 170, True)}
<h2>96 px</h2><div class="light">{row("light", 96)}</div>{row("dark", 96)}
<h2>48 px</h2><div class="light">{row("light", 48)}</div>{row("dark", 48)}"""
    (HERE / "overview.html").write_text(html)


def render():
    subprocess.run([g.CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1800,1120",
                    f"--screenshot={HERE / 'overview.png'}", f"file://{HERE / 'overview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    render()
    print("ok")
