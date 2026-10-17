"""Sagas UI icon pack, filled + rounded. One geometry definition -> SVG and Android Vector Drawable.
Each icon is a single solid shape (even-odd, with cut-outs) on a 24x24 grid, built with polygon booleans.
Usage: pip install shapely; python3 gen_ui_icons.py <out_dir>   (writes <out_dir>/svg and <out_dir>/android/drawable)
"""
import math, os, re, json, sys
from shapely.geometry import Polygon, LineString, Point, box, MultiPolygon, GeometryCollection
from shapely.ops import unary_union
from shapely import affinity

OUT = sys.argv[1] if len(sys.argv) > 1 else '.'


# ---------- path sampling
def _arc(p0, rx, ry, phi, fa, fs, p1, n=None):
    x1, y1 = p0; x2, y2 = p1
    if rx == 0 or ry == 0: return [p1]
    phi = math.radians(phi); c, s = math.cos(phi), math.sin(phi)
    dx, dy = (x1 - x2) / 2, (y1 - y2) / 2
    x1p, y1p = c * dx + s * dy, -s * dx + c * dy
    rx, ry = abs(rx), abs(ry)
    lam = x1p ** 2 / rx ** 2 + y1p ** 2 / ry ** 2
    if lam > 1: rx *= math.sqrt(lam); ry *= math.sqrt(lam)
    num = rx ** 2 * ry ** 2 - rx ** 2 * y1p ** 2 - ry ** 2 * x1p ** 2
    den = rx ** 2 * y1p ** 2 + ry ** 2 * x1p ** 2
    co = math.sqrt(max(0, num / den)) * (-1 if fa == fs else 1)
    cxp, cyp = co * rx * y1p / ry, -co * ry * x1p / rx
    cx, cy = c * cxp - s * cyp + (x1 + x2) / 2, s * cxp + c * cyp + (y1 + y2) / 2
    def ang(ux, uy, vx, vy):
        a = math.atan2(ux * vy - uy * vx, ux * vx + uy * vy); return a
    t1 = ang(1, 0, (x1p - cxp) / rx, (y1p - cyp) / ry)
    dt = ang((x1p - cxp) / rx, (y1p - cyp) / ry, (-x1p - cxp) / rx, (-y1p - cyp) / ry)
    if not fs and dt > 0: dt -= 2 * math.pi
    if fs and dt < 0: dt += 2 * math.pi
    n = max(6, int(abs(dt) / math.radians(5)))
    pts = []
    for i in range(1, n + 1):
        t = t1 + dt * i / n
        pts.append((cx + rx * math.cos(t) * c - ry * math.sin(t) * s, cy + rx * math.cos(t) * s + ry * math.sin(t) * c))
    pts[-1] = p1
    return pts


def sample(d):
    """path string -> list of (points, closed)"""
    toks = re.findall(r'[MmLlHhVvCcQqAaZz]|-?\d*\.?\d+(?:e-?\d+)?', d)
    i = 0; subs = []; cur = []; pos = (0, 0); start = (0, 0); cmd = None
    def num():
        nonlocal i; v = float(toks[i]); i += 1; return v
    while i < len(toks):
        if re.match(r'[A-Za-z]', toks[i]): cmd = toks[i]; i += 1
        rel = cmd.islower(); C = cmd.upper()
        if C == 'Z':
            subs.append((cur, True)); cur = []; pos = start; continue
        if C == 'M':
            if cur: subs.append((cur, False))
            x, y = num(), num()
            if rel: x += pos[0]; y += pos[1]
            pos = start = (x, y); cur = [pos]; cmd = 'l' if rel else 'L'
        elif C == 'L':
            x, y = num(), num()
            if rel: x += pos[0]; y += pos[1]
            pos = (x, y); cur.append(pos)
        elif C == 'H':
            x = num(); x = x + pos[0] if rel else x; pos = (x, pos[1]); cur.append(pos)
        elif C == 'V':
            y = num(); y = y + pos[1] if rel else y; pos = (pos[0], y); cur.append(pos)
        elif C == 'C':
            v = [num() for _ in range(6)]
            if rel: v = [v[k] + pos[k % 2] for k in range(6)]
            p0 = pos
            for k in range(1, 17):
                t = k / 16; u = 1 - t
                cur.append((u ** 3 * p0[0] + 3 * u * u * t * v[0] + 3 * u * t * t * v[2] + t ** 3 * v[4],
                            u ** 3 * p0[1] + 3 * u * u * t * v[1] + 3 * u * t * t * v[3] + t ** 3 * v[5]))
            pos = (v[4], v[5])
        elif C == 'Q':
            v = [num() for _ in range(4)]
            if rel: v = [v[k] + pos[k % 2] for k in range(4)]
            p0 = pos
            for k in range(1, 13):
                t = k / 12; u = 1 - t
                cur.append((u * u * p0[0] + 2 * u * t * v[0] + t * t * v[2], u * u * p0[1] + 2 * u * t * v[1] + t * t * v[3]))
            pos = (v[2], v[3])
        elif C == 'A':
            rx, ry, phi, fa, fs, x, y = [num() for _ in range(7)]
            if rel: x += pos[0]; y += pos[1]
            cur += _arc(pos, rx, ry, phi, int(fa), int(fs), (x, y)); pos = (x, y)
    if cur: subs.append((cur, False))
    return subs


# ---------- geometry helpers
def line(d, w):
    return unary_union([LineString(p).buffer(w / 2, quad_segs=10, cap_style=1, join_style=1) for p, _ in sample(d) if len(p) > 1])

def fill(d):
    return unary_union([Polygon(p).buffer(0) for p, _ in sample(d) if len(p) > 2])

def circle(cx, cy, r): return Point(cx, cy).buffer(r, quad_segs=24)

def rrect(x, y, w, h, r): return box(x + r, y + r, x + w - r, y + h - r).buffer(r, quad_segs=12)

def poly(pts): return Polygon(pts)

def soften(g, r=0.8): return g.buffer(-r, join_style=1).buffer(r, quad_segs=10, join_style=1)

def U(*gs): return unary_union(list(gs))

def sub(a, *bs): return a.difference(unary_union(list(bs)))

def heart(x0, y0, s):
    def P(x, y): return f'{x0 + s * x:.3f} {y0 + s * y:.3f}'
    r = s * 3.7
    return fill(f'M{P(8, 15)}C{P(8, 15)} {P(1, 11)} {P(1, 6.2)}A{r:.3f} {r:.3f} 0 0 1 {P(8, 4.8)}A{r:.3f} {r:.3f} 0 0 1 {P(15, 6.2)}C{P(15, 11)} {P(8, 15)} {P(8, 15)}z')

def gear(cx, cy, ro, ri, teeth=8):
    pts = []; step = 2 * math.pi / teeth
    for i in range(teeth):
        a = i * step
        for da, r in ((-0.30, ri), (-0.17, ro), (0.17, ro), (0.30, ri)):
            pts.append((cx + r * math.cos(a + da * step * 1.6), cy + r * math.sin(a + da * step * 1.6)))
    return Polygon(pts)

def sparkle(cx, cy, r):
    k = r * 0.28
    return fill(f'M{cx} {cy - r}Q{cx + k} {cy - k} {cx + r} {cy}Q{cx + k} {cy + k} {cx} {cy + r}Q{cx - k} {cy + k} {cx - r} {cy}Q{cx - k} {cy - k} {cx} {cy - r}z')

BUBBLE = 'M7 4.5h10A3.5 3.5 0 0 1 20.5 8v6A3.5 3.5 0 0 1 17 17.5h-5.2L8 20.6V17.5H7A3.5 3.5 0 0 1 3.5 14V8A3.5 3.5 0 0 1 7 4.5z'
CHECK = 'M7.4 12.4L10.6 15.6L16.8 8.8'

I = {}
def add(name, fn, old=()): I[name] = (fn, list(old))

# ---- navigation & actions
add('ic_check', lambda: line('M5 12.5L9.5 17L19 7.5', 3), ['ic_check'])
add('ic_close', lambda: line('M6 6L18 18M18 6L6 18', 3), ['round_close_24'])
add('ic_arrow_up', lambda: line('M12 19.5V5M5.8 11L12 4.8L18.2 11', 3), ['ic_arrow_up'])
add('ic_chevron_down', lambda: line('M5.5 9L12 15.5L18.5 9', 3), ['ic_arrow_down'])
add('ic_chevron_left', lambda: line('M15 5.5L8.5 12L15 18.5', 3), ['ic_back_left'])
add('ic_chevron_right', lambda: line('M9 5.5L15.5 12L9 18.5', 3), ['round_arrow_forward_ios_24'])
add('ic_more_vert', lambda: U(circle(12, 5.5, 2.1), circle(12, 12, 2.1), circle(12, 18.5, 2.1)), ['ic_more_vert'])
add('ic_menu', lambda: U(circle(5.5, 12, 2.1), circle(12, 12, 2.1), circle(18.5, 12, 2.1)), ['ic_menu'])
add('ic_search', lambda: U(sub(circle(10.3, 10.3, 7.6), circle(10.3, 10.3, 4.5)), line('M15.8 15.8L20.4 20.4', 3.2)), ['search'])

# ---- edit / file
def edit():
    body = soften(poly([(3.6, 20.4), (5.2, 15.0), (15.7, 4.5), (19.5, 8.3), (9.0, 18.8)]), 0.9)
    return sub(body, line('M13.4 7.0L17.0 10.6', 1.0))
add('ic_edit', edit, ['ic_edit'])

def delete():
    lid = rrect(3.8, 5.8, 16.4, 2.4, 1.2)
    handle = rrect(8.8, 2.6, 6.4, 3.8, 1.4)
    body = soften(fill('M5.7 9.8H18.3L17.4 19.2A2.4 2.4 0 0 1 15 21.4H9A2.4 2.4 0 0 1 6.6 19.2z'), 0.9)
    return sub(U(lid, handle, body), line('M10 12.8V17.6M14 12.8V17.6', 1.6))
add('ic_delete', delete, ['ic_delete'])

def copy():
    front = rrect(3.5, 8, 12.5, 12.5, 3)
    back = sub(rrect(8, 3.5, 12.5, 12.5, 3), front.buffer(1.5))
    return U(front, back)
add('ic_copy', copy, ['ic_copy'])

def paste():
    board = rrect(4.5, 4.5, 15, 17, 3.2)
    clip = rrect(8.5, 2.2, 7, 4.2, 1.8)
    return sub(U(sub(board, clip.buffer(1.1)), clip), line('M8.6 11.6H15.4M8.6 15.6H13', 1.7))
add('ic_paste', paste, ['ic_paste'])

add('ic_note', lambda: sub(rrect(4.8, 2.8, 14.4, 18.4, 3.6), line('M8.8 8.6H15.2M8.8 12.6H15.2M8.8 16.6H12', 1.8)), ['ic_note'])

add('ic_share', lambda: U(line('M12 15.5V4.6M8 8.4L12 4.4L16 8.4', 3), line('M5.5 12V18A2.3 2.3 0 0 0 7.8 20.3H16.2A2.3 2.3 0 0 0 18.5 18V12', 3)), ['ic_share'])

def send():
    plane = soften(poly([(21.2, 2.8), (2.8, 10.4), (9.8, 13.3), (12.8, 20.4)]), 1.1)
    return sub(plane, line('M10.4 12.9L16.2 7.6', 1.2))
add('ic_send', send, ['ic_send'])


# ---- chat & content
add('ic_talk_bubble', lambda: fill('M12 3.6C16.8 3.6 20.6 6.7 20.6 10.6C20.6 14.5 16.8 17.6 12 17.6C11.2 17.6 10.4 17.5 9.7 17.3L5.3 19.6L6.4 15.9C4.7 14.7 3.4 12.8 3.4 10.6C3.4 6.7 7.2 3.6 12 3.6z'), ['talk_bubble'])
def think():
    cloud = soften(U(circle(7.9, 11.4, 3.7), circle(12.2, 8.6, 4.7), circle(16.5, 11.2, 3.9), rrect(7.9, 10.6, 8.6, 4.6, 2.3)), 0.4)
    return U(cloud, circle(8, 18.6, 1.6), circle(4.8, 21, 1.05))
add('ic_think', think, ['think_icon'])
add('ic_sync', lambda: sub(fill(BUBBLE), line('M8.4 9.5H15.6M8.4 12.9H12.6', 1.8)), ['ic_sync'])
add('ic_faq', lambda: sub(fill(BUBBLE), line('M9.9 9.5A2.2 2.2 0 1 1 13 11.5C12.4 11.9 12 12.3 12 13.1', 1.8), circle(12, 15.1, 1.0)), ['ic_faq'])
add('ic_censor', lambda: sub(fill(BUBBLE), line('M12 7.9V12.7M9.8 9.1L14.2 11.5M14.2 9.1L9.8 11.5', 1.7)), ['ic_censor'])

def relationship():
    b = heart(7.8, 3.4, 1.05)
    a = sub(heart(0.7, 7.2, 1.0), b.buffer(1.2))
    return U(a, b)
add('ic_relationship', relationship, ['ic_relationship'])

add('ic_review', lambda: U(sub(circle(10.4, 10.4, 7.8), sparkle(10.4, 10.4, 4.0)), line('M16 16L20.4 20.4', 3.2)), ['ic_review'])

def idea():
    bulb = fill('M12 2.6A6.6 6.6 0 0 0 8 14.5C8.7 15.1 9 15.7 9 16.5H15C15 15.7 15.3 15.1 16 14.5A6.6 6.6 0 0 0 12 2.6z')
    return U(bulb, rrect(9, 17.9, 6, 2, 1), rrect(10, 20.5, 4, 1.5, 0.75))
add('ic_idea', idea, ['ic_idea'])

# ---- status & safety
add('ic_check_circle', lambda: sub(circle(12, 12, 10.2), line(CHECK, 2.4)), ['ic_check_circle'])

def warning():
    tri = poly([(12, 3.3), (21.6, 19.9), (2.4, 19.9)]).buffer(-1.5, join_style=1).buffer(1.5, quad_segs=10, join_style=1)
    return sub(tri, line('M12 9.4V13.8', 2.1), circle(12, 16.9, 1.2))
add('ic_warning', warning, ['ic_warning'])

def violation():
    tops = ((8.7, 7.4), (12.1, 4.0), (15.5, 4.9), (18.9, 8.6))
    fingers = [line(f'M{x} {y}V13', 2.7) for x, y in tops]
    palm = soften(fill('M7.3 11.2H20.3V15.2C20.3 18.9 17.7 21.5 14.2 21.5H12.8C10.7 21.5 9.2 20.5 8 18.8L6.6 16.4z'), 0.9)
    thumb = line('M7.9 15.6L4.3 11.7', 2.7)
    hand = U(*fingers, palm, thumb)
    return sub(hand, line('M10.4 9.6V13.4M13.8 8.6V13.4M17.2 9.8V13.4', 0.7))
add('ic_violation', violation, ['ic_violation'])

def age():
    head = circle(9.2, 7.6, 4.0)
    shield = soften(fill('M17.6 10.6L22.2 12.4V16.2C22.2 19.1 20.3 21 17.6 22C14.9 21 13 19.1 13 16.2V12.4z'), 0.8)
    body = sub(fill('M2.2 21C2.2 16.9 5.2 14 9.2 14C10.3 14 11.2 14.1 12 14.4V21z'), shield.buffer(1.3))
    return U(head, body, sub(shield, line('M15.2 16.2L16.9 17.9L20 14.6', 1.7)))
add('ic_age', age, ['ic_age'])

def bug():
    body = fill('M12 8C9.2 8 7 10.2 7 13V15.6A5 5 0 0 0 17 15.6V13C17 10.2 14.8 8 12 8z')
    head = circle(12, 6.4, 2.7)
    legs = line('M7 12.6H3.4M17 12.6H20.6M7.9 17.3L4.6 19.2M16.1 17.3L19.4 19.2M8 9.9L5 7.6M16 9.9L19 7.6', 1.9)
    return sub(U(body, head, legs), line('M12 11.6V19.4', 1.3))
add('ic_bug', bug, ['ic_bug'])

def inject():
    local = U(line('M2.6 12H7', 1.4), rrect(7, 9.4, 9.4, 5.2, 1.5), line('M16.4 12H20', 2), line('M20.2 8.6V15.4', 2.4), line('M7.4 7.6V16.4', 2.2))
    return affinity.rotate(local, -45, origin=(12, 12))
add('ic_inject', inject, ['ic_inject'])

def key():
    head = sub(circle(15.6, 8.4, 5.4), circle(16.9, 7.1, 1.5))
    return U(head, line('M11.8 12.2L4.6 19.4', 3.2), line('M7.6 16.4L10 18.8', 2.6))
add('ic_key', key, ['ic_key'])

def globe():
    disc = circle(12, 12, 10.2)
    merid = fill('M12 1.8C14.7 4.5 16 8 16 12C16 16 14.7 19.5 12 22.2C9.3 19.5 8 16 8 12C8 8 9.3 4.5 12 1.8z')
    cut = U(line('M1.8 12H22.2', 1.5), merid.boundary.buffer(0.75))
    return sub(disc, cut)
add('ic_globe', globe, ['ic_globe'])

add('ic_settings', lambda: sub(soften(gear(12, 12, 10, 8), 0.9), circle(12, 12, 3.4)), ['ic_settings', 'round_settings_24'])

# ---- media & audio
add('ic_mic', lambda: U(rrect(8.3, 2.4, 7.4, 12.6, 3.7), line('M5.6 11A6.4 6.4 0 0 0 18.4 11', 2.3), line('M12 17.4V21', 2.3)), ['ic_mic'])
add('ic_headset', lambda: U(line('M4.6 14V12A7.4 7.4 0 0 1 19.4 12V14', 2.6), rrect(2.8, 12.4, 5.2, 8.4, 2.4), rrect(16, 12.4, 5.2, 8.4, 2.4)), ['ic_headset'])
add('ic_play', lambda: soften(poly([(7, 4.2), (20, 12), (7, 19.8)]), 1.7), ['round_play_arrow_24'])
add('ic_pause', lambda: U(rrect(5.8, 4.5, 4.6, 15, 2), rrect(13.6, 4.5, 4.6, 15, 2)), ['round_pause_24'])
add('ic_stop', lambda: rrect(5, 5, 14, 14, 3.6), ['ic_stop'])
add('ic_next', lambda: U(soften(poly([(3.6, 6), (12.4, 12), (3.6, 18)]), 1.4), soften(poly([(11.6, 6), (20.4, 12), (11.6, 18)]), 1.4)), ['ic_next'])
add('ic_previous', lambda: U(soften(poly([(20.4, 6), (11.6, 12), (20.4, 18)]), 1.4), soften(poly([(12.4, 6), (3.6, 12), (12.4, 18)]), 1.4)), ['ic_previous'])
add('ic_queue_music', lambda: U(line('M4 7H13M4 11H13M4 15H9', 2.3), circle(15.2, 17.6, 3), line('M17.6 17.6V6.6L21 8.2', 2.3)), ['round_queue_music_24'])
add('ic_replay', lambda: line('M19.4 12A7.4 7.4 0 1 1 17.2 6.8M19.6 4.4V8.6H15.4', 2.7), ['ic_replay_arrow', 'baseline_refresh_24'])
add('ic_restore', lambda: line('M3.9 12A8.1 8.1 0 1 0 6.3 6.2M3.6 4.3V8.7H8M12 7.8V12L15 14', 2.6), ['ic_restore'])


# ---------- export
def ring_d(coords):
    pts = [f'{x:.2f} {y:.2f}'.replace('-0.00', '0.00') for x, y in coords[:-1]]
    return 'M' + ' L'.join(pts) + 'z'

def to_d(g):
    g = g.simplify(0.012, preserve_topology=True)
    polys = [g] if isinstance(g, Polygon) else [p for p in getattr(g, 'geoms', []) if isinstance(p, Polygon)]
    out = []
    for p in polys:
        if p.is_empty or p.area < 0.05: continue
        out.append(ring_d(list(p.exterior.coords)))
        for r in p.interiors: out.append(ring_d(list(r.coords)))
    return ''.join(out)

def trim(d): return re.sub(r'(\.\d*?)0+(?=[ Lz]|$)', r'\1', d).replace('.0 ', ' ').replace('.z', 'z')

def svg_of(name, size=24):
    d = to_d(I[name][0]())
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="{size}" height="{size}" fill="currentColor"><path fill-rule="evenodd" d="{d}"/></svg>'

def vd_of(name):
    d = to_d(I[name][0]())
    return ('<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
            '    android:width="24dp"\n    android:height="24dp"\n'
            '    android:viewportWidth="24"\n    android:viewportHeight="24"\n'
            '    android:tint="?attr/colorControlNormal">\n'
            '    <path\n        android:fillColor="@android:color/white"\n        android:fillType="evenOdd"\n'
            f'        android:pathData="{d}"/>\n</vector>\n')

if __name__ == '__main__':
    os.makedirs(f'{OUT}/svg', exist_ok=True); os.makedirs(f'{OUT}/android/drawable', exist_ok=True)
    tot = 0
    for n in I:
        s = svg_of(n); x = vd_of(n); tot += len(x)
        open(f'{OUT}/svg/{n}.svg', 'w').write(s + '\n'); open(f'{OUT}/android/drawable/{n}.xml', 'w').write(x)
    json.dump({n: v[1] for n, v in I.items()}, open(f'{OUT}/mapping.json', 'w'), indent=1)
    print(len(I), 'icons', tot // len(I), 'bytes avg')
