#!/usr/bin/env python3
"""Round 4 of the icon study: the middle point of a scale, starting from the CURRENT icon.

The user's clarification: the new icon is today's minimalist Gota silhouette expanded a little, sitting
between two extremes: the plain silhouette (3 shapes: head, two eyes) and the old fully detailed Duo-style
dragon (about 24 marks). So this does not simplify the detailed icon downward. It takes the real shipped
silhouette and eyes (vectorised from `res/drawable-nodpi/ic_dragon_fg_light.png`, nothing redrawn by eye) and adds
just enough: volume, a pupil and a glint in each eye and one pair of nostrils.

  current  head + 2 eyes                                  = 3 shapes
  middle   head + 2 eyes + 2 pupils + 2 glints + 1 nostril pair = 8 shapes (soft shading, rim light and glow are treatment)
  old      crests, eyes, pupils, glints, lids, ridges,
           snout, nostrils, mouth, ticks, chin, whiskers  = about 24 marks

    python3 generate_middle.py          # writes svg/mid_*.svg, reference/current_*.png, mid_preview.html
    python3 generate_middle.py --png    # also rasterises with headless Chrome
"""
import pathlib
import subprocess
import sys

HERE = pathlib.Path(__file__).parent
SVG = HERE / "svg"
PNG = HERE / "png"
REF = HERE / "reference"
RES = HERE.parents[2] / "app/src/main/res/drawable-nodpi"
CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
SIZE = 432  # the shipped layers are 432px (108dp at xxxhdpi), so the study works in the same units
SCALE = 1.10  # how much the head is enlarged about the centre: the face needs room, but the spike tips and ears must stay inside the 66dp safe circle


# ---------------------------------------------------------------------------------------------
# Vectorising a mask: boundary edges around the "inside" pixels, stitched into loops, simplified.
# ---------------------------------------------------------------------------------------------
def read_pgm(path):
    data = pathlib.Path(path).read_bytes()
    parts = data.split(maxsplit=4)
    w, h = int(parts[1]), int(parts[2])
    pix = parts[4] if len(parts) > 4 else b""
    pix = data[-w * h:]
    return w, h, pix


def loops(w, h, pix):
    inside = lambda x, y: 0 <= x < w and 0 <= y < h and pix[y * w + x] > 127
    nxt = {}
    for y in range(h):
        for x in range(w):
            if not inside(x, y):
                continue
            if not inside(x, y - 1):
                nxt.setdefault((x, y), []).append((x + 1, y))
            if not inside(x + 1, y):
                nxt.setdefault((x + 1, y), []).append((x + 1, y + 1))
            if not inside(x, y + 1):
                nxt.setdefault((x + 1, y + 1), []).append((x, y + 1))
            if not inside(x - 1, y):
                nxt.setdefault((x, y + 1), []).append((x, y))
    out = []
    while nxt:
        start = next(iter(nxt))
        loop, cur = [start], start
        while True:
            ends = nxt[cur]
            step = ends.pop()
            if not ends:
                del nxt[cur]
            if step == start:
                break
            loop.append(step)
            cur = step
            if cur not in nxt:
                break
        out.append(loop)
    return out


def area(poly):
    return abs(sum(poly[i][0] * poly[(i + 1) % len(poly)][1] - poly[(i + 1) % len(poly)][0] * poly[i][1] for i in range(len(poly)))) / 2


def rdp(points, eps):
    if len(points) < 3:
        return points
    (x1, y1), (x2, y2) = points[0], points[-1]
    dx, dy = x2 - x1, y2 - y1
    norm = (dx * dx + dy * dy) ** 0.5 or 1e-9
    far, idx = 0, 0
    for i in range(1, len(points) - 1):
        d = abs(dy * points[i][0] - dx * points[i][1] + x2 * y1 - y2 * x1) / norm
        if d > far:
            far, idx = d, i
    if far <= eps:
        return [points[0], points[-1]]
    return rdp(points[: idx + 1], eps)[:-1] + rdp(points[idx:], eps)


def simplify(poly, eps=0.9):
    """Douglas-Peucker on a closed loop, then Chaikin corner cutting on the gentle vertices only, so
    curves lose the pixel staircase while the spike tips and the ear points stay sharp."""
    half = max(range(len(poly)), key=lambda i: (poly[i][0] - poly[0][0]) ** 2 + (poly[i][1] - poly[0][1]) ** 2)
    a = rdp(poly[: half + 1], eps)
    b = rdp(poly[half:] + [poly[0]], eps)
    pts = a[:-1] + b[:-1]
    out = []
    n = len(pts)
    for i in range(n):
        p0, p1, p2 = pts[i - 1], pts[i], pts[(i + 1) % n]
        v1, v2 = (p1[0] - p0[0], p1[1] - p0[1]), (p2[0] - p1[0], p2[1] - p1[1])
        n1, n2 = (v1[0] ** 2 + v1[1] ** 2) ** 0.5 or 1, (v2[0] ** 2 + v2[1] ** 2) ** 0.5 or 1
        cos = (v1[0] * v2[0] + v1[1] * v2[1]) / (n1 * n2)
        if cos > 0.80:  # gentle turn: cut the corner
            out += [(p1[0] - v1[0] * .25, p1[1] - v1[1] * .25), (p1[0] + v2[0] * .25, p1[1] + v2[1] * .25)]
        else:  # a real point: keep it
            out.append(p1)
    return out


def to_path(poly):
    return "M" + " L".join(f"{x:.1f},{y:.1f}" for x, y in poly) + " Z"


def bbox(poly):
    xs, ys = [p[0] for p in poly], [p[1] for p in poly]
    return min(xs), min(ys), max(xs), max(ys)


def eye_spec(poly):
    """Where a shipped eye sits: centre and size. Its outline is not reused; the dragon eye is redrawn on it."""
    x0, y0, x1, y1 = bbox(poly)
    return (x0 + x1) / 2, (y0 + y1) / 2, x1 - x0, y1 - y0


def extract():
    """Head outline and the position of the two eyes, from the shipped light foreground."""
    fg = RES / "ic_dragon_fg_light.png"
    tmp = HERE / ".mask"
    tmp.mkdir(exist_ok=True)
    subprocess.run(["magick", str(fg), "-alpha", "extract", "-resize", "400%", "-blur", "0x3", "-threshold", "55%", "-depth", "8", f"pgm:{tmp / 'head.pgm'}"], check=True)
    subprocess.run(["magick", str(fg), "-fx", "min(r,min(g,b))*a", "-colorspace", "gray",
                    "-resize", "400%", "-blur", "0x3", "-threshold", "62%", "-depth", "8", f"pgm:{tmp / 'eyes.pgm'}"], check=True)
    w, h, pix = read_pgm(tmp / "head.pgm")
    head = max(loops(w, h, pix), key=area)
    w, h, pix = read_pgm(tmp / "eyes.pgm")
    eyes = sorted(sorted(loops(w, h, pix), key=area)[-2:], key=lambda p: bbox(p)[0])
    for f in tmp.iterdir():
        f.unlink()
    tmp.rmdir()
    # the masks were upscaled 4x so the staircase of the source pixels is a quarter pixel; bring the result back to 432 units
    down = lambda poly: [(x / 4, y / 4) for x, y in poly]
    return down(simplify(head, 2.0)), [eye_spec(down(simplify(e, 1.6))) for e in eyes]


# ---------------------------------------------------------------------------------------------
# The middle icon
# ---------------------------------------------------------------------------------------------
EYE_W, EYE_H = 1.55, 1.45  # dragon eye size against the shipped slit it replaces
EYE_RISE = -6              # the eyes move up a little so the lower face has room for a snout and a mouth


def dragon_eye(cx, cy, w, h, left, hx):
    """An almond eye swept up toward the outside: a sharp outer tip, an arched lid, a flatter lower lid and
    a lower inner corner. Drawn for the left eye and mirrored about the head's centre line for the right."""
    W, H = w * EYE_W, h * EYE_H
    cy += EYE_RISE
    pts = {
        "tip": (cx - W / 2, cy - H * .30),
        "inner": (cx + W / 2, cy + H * .16),
        "top": (cx - W * .06, cy - H * .86),
        "low": (cx + W * .12, cy + H * .62),
    }
    if not left:
        pts = {k: (2 * hx - x, y) for k, (x, y) in pts.items()}
    f = lambda k: f"{pts[k][0]:.1f},{pts[k][1]:.1f}"
    return f"M{f('tip')} Q{f('top')} {f('inner')} Q{f('low')} {f('tip')} Z", (cx, cy, W, H)


def face_parts(head, eyes, dark):
    hb = bbox(head)
    hx = (hb[0] + hb[2]) / 2
    parts = {"head": to_path(head), "eyes": [], "pupils": []}
    for left, (ecx, ecy, ew, eh) in zip((True, False), eyes):
        cx = ecx if left else 2 * hx - ecx
        d, (_, cy, W, H) = dragon_eye(cx, ecy, ew, eh, left, hx)
        parts["eyes"].append(d)
        ex = cx if left else 2 * hx - cx
        inward = 1 if left else -1
        # a tall narrow pupil, the reptile cue, set a little toward the nose; (cy - .09H) is the almond's own middle
        parts["pupils"].append((ex + inward * W * .08, cy - H * .06, W * .16, H * .27))
    # snout: a bridge that starts narrow between the eyes and widens to a rounded tip, with two slanted
    # nostril cut-outs (a round pad with round nostrils read as a pig), then a smile under it
    top, base = hb[3] - 68, hb[3] - 18
    f = lambda x, y: f"{x:.1f},{y:.1f}"
    bridge = (f"M{f(hx - 8, top)} L{f(hx + 8, top)} C{f(hx + 9, top + 18)} {f(hx + 27, top + 22)} {f(hx + 27, base - 12)} "
              f"Q{f(hx + 26, base)} {f(hx, base)} Q{f(hx - 26, base)} {f(hx - 27, base - 12)} "
              f"C{f(hx - 27, top + 22)} {f(hx - 9, top + 18)} {f(hx - 8, top)} Z")
    ny = base - 14
    nostril = lambda s: (f"M{f(hx + s * 14, ny)} Q{f(hx + s * 9, ny - 4)} {f(hx + s * 6, ny + 2)} Q{f(hx + s * 10, ny + 5)} {f(hx + s * 14, ny)} Z")
    parts["snout"] = bridge + " " + nostril(-1) + " " + nostril(1)
    my = hb[3] - 14
    parts["mouth"] = f"M{f(hx - 32, my)} Q{f(hx, my + 13)} {f(hx + 32, my)}"
    return parts


def middle_layers(head, eyes, dark):
    p = face_parts(head, eyes, dark)
    cxm = SIZE / 2
    tf = f'transform="translate({cxm} {cxm}) scale({SCALE}) translate({-cxm} {-cxm})"'
    if dark:
        head_a, head_b, rim, eye_fill, pupil_fill, glow = "#EADFFF", "#9A6CFA", "#FFFFFF", "#F6F0FF", "#241552", "#A98BFF"
        snout_fill, mouth_stroke = "#F6EEFF", "#5A3BB5"
        bg = ("#1A0E2E", "#0A0512")
    else:
        head_a, head_b, rim, eye_fill, pupil_fill, glow = "#4A3C86", "#0E0922", "#BFA9FF", "#FFFFFF", "#150C2E", "#8F73F2"
        snout_fill, mouth_stroke = "#4E4192", "#A58EF0"
        bg = ("#EFE6FF", "#B398FF")

    defs = f"""<defs>
  <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="{bg[0]}"/><stop offset="1" stop-color="{bg[1]}"/></linearGradient>
  <linearGradient id="head" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{head_a}"/><stop offset="1" stop-color="{head_b}"/></linearGradient>
  <radialGradient id="fore" cx="216" cy="150" r="96" gradientUnits="userSpaceOnUse"><stop offset="0" stop-color="#fff" stop-opacity="{.20 if dark else .16}"/><stop offset="1" stop-color="#fff" stop-opacity="0"/></radialGradient>
  <linearGradient id="chin" x1="0" y1="0" x2="0" y2="1"><stop offset=".55" stop-color="#000" stop-opacity="0"/><stop offset="1" stop-color="#000" stop-opacity="{.30 if dark else .42}"/></linearGradient>
  <linearGradient id="rim" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="{rim}" stop-opacity="{.55 if dark else .6}"/><stop offset=".5" stop-color="{rim}" stop-opacity="0"/></linearGradient>
  <filter id="glow" x="-30%" y="-30%" width="160%" height="160%"><feGaussianBlur stdDeviation="9"/></filter>
  <clipPath id="clip_head"><path d="{p['head']}"/></clipPath>
  <clipPath id="clip_el"><path d="{p['eyes'][0]}"/></clipPath>
  <clipPath id="clip_er"><path d="{p['eyes'][1]}"/></clipPath>
</defs>"""

    def pupil(i, clip):
        x, y, rx, ry = p["pupils"][i]
        # one glint per eye, on the same side in both (not mirrored)
        return (f'<g clip-path="url(#{clip})"><ellipse cx="{x:.1f}" cy="{y:.1f}" rx="{rx:.1f}" ry="{ry:.1f}" fill="{pupil_fill}"/>'
                f'<circle cx="{x - rx * .28:.1f}" cy="{y - ry * .38:.1f}" r="{rx * .42:.1f}" fill="#FFFFFF"/></g>')

    glow_svg = f'<path d="{p["head"]}" fill="{glow}" opacity=".6" filter="url(#glow)"/>' if dark else (
        f'<path d="{p["head"]}" fill="{glow}" opacity=".35" filter="url(#glow)" transform="translate(0 7)"/>')
    face = (
        f'<path d="{p["head"]}" fill="url(#head)"/>'
        f'<g clip-path="url(#clip_head)"><rect width="{SIZE}" height="{SIZE}" fill="url(#fore)"/>'
        f'<rect width="{SIZE}" height="{SIZE}" fill="url(#chin)"/>'
        f'<path d="{p["head"]}" fill="none" stroke="url(#rim)" stroke-width="5"/></g>'
        f'<path d="{p["eyes"][0]}" fill="{eye_fill}"/><path d="{p["eyes"][1]}" fill="{eye_fill}"/>'
        + pupil(0, "clip_el") + pupil(1, "clip_er")
        + f'<path fill-rule="evenodd" d="{p["snout"]}" fill="{snout_fill}"/>'
        + f'<path d="{p["mouth"]}" fill="none" stroke="{mouth_stroke}" stroke-width="3.6" stroke-linecap="round"/>'
    )
    bg_svg = f'<rect width="{SIZE}" height="{SIZE}" fill="url(#bg)"/>'
    fg_svg = f'<g {tf}>{glow_svg}{face}</g>'
    mono = (
        f'<g {tf}><path fill-rule="evenodd" fill="#000" d="{p["head"]} {p["eyes"][0]} {p["eyes"][1]}"/></g>'
    )
    return defs, bg_svg, fg_svg, mono


def wrap(body, size=1024):
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {SIZE} {SIZE}" width="{size}" height="{size}">{body}</svg>'


def build():
    SVG.mkdir(exist_ok=True)
    REF.mkdir(exist_ok=True)
    head, eyes = extract()
    for mode in ("light", "dark"):
        defs, bg, fg, mono = middle_layers(head, eyes, mode == "dark")
        (SVG / f"mid_{mode}.svg").write_text(wrap(defs + bg + fg))
        (SVG / f"mid_adaptive_background_{mode}.svg").write_text(wrap(defs + bg))
        (SVG / f"mid_adaptive_foreground_{mode}.svg").write_text(wrap(defs + fg))
        if mode == "light":
            (SVG / "mid_adaptive_monochrome.svg").write_text(wrap(defs + mono))
        # the shipped icon, composed from its own layers: the left end of the scale
        subprocess.run(["magick", str(RES / f"ic_dragon_bg_{mode}.png"), str(RES / f"ic_dragon_fg_{mode}.png"),
                        "-composite", "-resize", "1024x1024", str(REF / f"current_gota_{mode}.png")], check=True)

    def card(src, title, note, radius=True):
        r = ' style="border-radius:22%"' if radius else ""
        return f'<figure><img src="{src}"{r}><figcaption><b>{title}</b><br>{note}</figcaption></figure>'

    scale = ""
    for mode, label, old in (("light", "claro", "reference/old_duo_light.webp"), ("dark", "escuro", "reference/old_duo_dark.webp")):
        scale += (
            '<div class="row">'
            + card(f"reference/current_gota_{mode}.png", f"Atual ({label})", "3 formas: cabeça e 2 olhos")
            + '<span class="arrow">→</span>'
            + card(f"svg/mid_{mode}.svg", f"Meio-termo ({label})", "8 formas: cabeça, 2 olhos, 2 pupilas, 2 brilhos, 1 par de narinas")
            + '<span class="arrow">→</span>'
            + card(old, f"Antigo ({label})", "cerca de 24 marcas", radius=False)
            + "</div>"
        )
    sizes = ""
    for label, src in (("Atual", "reference/current_gota_light.png"), ("Meio-termo", "svg/mid_light.svg"),
                       ("Meio-termo escuro", "svg/mid_dark.svg"), ("Antigo", "reference/old_duo_light.webp")):
        sizes += f'<div class="sm"><span>{label}</span>' + "".join(
            f'<img src="{src}" width="{s}" height="{s}" style="border-radius:22%">' for s in (144, 96, 72, 48)) + "</div>"
    masks = ""
    for label, css, mode in (("círculo", "50%", "light"), ("squircle", "30%", "light"), ("quadrado", "10%", "light"), ("escuro", "50%", "dark")):
        masks += (f'<div class="ad"><div class="mask" style="border-radius:{css}"><img src="svg/mid_adaptive_background_{mode}.svg">'
                  f'<img src="svg/mid_adaptive_foreground_{mode}.svg"><div class="safe"></div></div><span>{label}</span></div>')
    masks += ('<div class="ad"><div class="mask" style="border-radius:50%;background:#2c2540">'
              '<img src="svg/mid_adaptive_monochrome.svg" style="filter:invert(1) sepia(1) saturate(3) hue-rotate(220deg) brightness(1.1)">'
              '</div><span>temático (mono)</span></div>')
    html = f"""<!doctype html><meta charset="utf-8"><title>Meio-termo do dragão</title>
<style>
body{{margin:0;padding:32px;background:#0b0b0d;color:#e8e6ef;font:15px/1.4 system-ui,sans-serif}}
h2{{font-weight:600;margin:30px 0 12px}} .row{{display:flex;gap:14px;align-items:center;margin-bottom:18px}}
figure{{margin:0;padding:14px;border-radius:20px;text-align:center;background:#000;width:290px}} figure img{{width:290px;height:290px;display:block}}
figcaption{{margin-top:10px;font-size:13px;opacity:.85}} .arrow{{font-size:28px;opacity:.5}}
.ad{{text-align:center}} .mask{{position:relative;width:190px;height:190px;overflow:hidden;background:#222}} .mask img{{position:absolute;inset:0;width:100%;height:100%}}
.ad span,.sm span{{display:block;font-size:13px;opacity:.75;margin-top:6px}} .safe{{position:absolute;left:50%;top:50%;width:61%;height:61%;transform:translate(-50%,-50%);border:2px dashed #fff8;border-radius:50%}}
.sm{{display:flex;align-items:flex-end;gap:16px;margin:0 0 12px}} .sm span{{width:130px;margin:0}} .ad{{display:inline-block;margin-right:14px}}
</style>
<h2>A escala: do ícone atual ao antigo, com o meio-termo no centro</h2>{scale}
<h2>Legibilidade em 144, 96, 72 e 48 px</h2>{sizes}
<h2>Adaptive icon do meio-termo (tracejado = área segura de 66dp)</h2><div>{masks}</div>
"""
    (HERE / "mid_preview.html").write_text(html)


def render():
    PNG.mkdir(exist_ok=True)
    for name in ("mid_light", "mid_dark"):
        subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                        "--window-size=1024,1024", f"--screenshot={PNG / (name + '.png')}", f"file://{SVG / (name + '.svg')}"],
                       check=True, capture_output=True)
    subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--window-size=1100,1950",
                    f"--screenshot={HERE / 'mid_preview.png'}", f"file://{HERE / 'mid_preview.html'}"], check=True, capture_output=True)


if __name__ == "__main__":
    build()
    if "--png" in sys.argv:
        render()
    print("ok")
