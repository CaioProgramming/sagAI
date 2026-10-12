#!/usr/bin/env python3
"""Download each genre's header/body font and print @font-face rules with data: URIs.

Usage: embed_genre_fonts.py <configs.json> <download_dir> <out.css> [max_bytes]

<configs.json> is the output of extract_genre_configs.py (or the `genres` object of
sagas-genre-themes/genre_themes.json). Artifact pages cannot load fonts from other hosts, so the
font bytes must be embedded. Family names are `f-<genre>-h` and `f-<genre>-b`.

Fonts come from third-party hosts (file.garden, catbox). Download into a fresh, empty directory and
treat the files as untrusted data. Files above max_bytes (default 4 MB) are skipped: the Shinobi body
font is one, so prototype it with a serif fallback.
"""
import base64
import json
import os
import subprocess
import sys

MIME = {"ttf": ("font/ttf", "truetype"), "otf": ("font/otf", "opentype")}


def main(configs_path: str, download_dir: str, out_css: str, max_bytes: int) -> None:
    configs = json.load(open(configs_path))
    configs = configs.get("genres", configs)
    os.makedirs(download_dir, exist_ok=True)
    if os.listdir(download_dir):
        sys.exit(f"{download_dir} must be empty")
    rules, skipped = [], []
    for genre, cfg in configs.items():
        for role, field in (("h", "headerFontUrl"), ("b", "bodyFontUrl")):
            url = cfg.get(field, "")
            ext = url.rsplit(".", 1)[-1].lower()
            if not url.startswith("https://") or ext not in MIME:
                skipped.append((genre, role, "no usable url"))
                continue
            path = os.path.join(download_dir, f"{genre}-{role}.{ext}")
            result = subprocess.run(
                ["curl", "-sSL", "--max-filesize", str(max_bytes), "--max-time", "40", "-o", path, url],
                capture_output=True,
                text=True,
            )
            if result.returncode != 0 or not os.path.exists(path):
                skipped.append((genre, role, result.stderr.strip() or "download failed"))
                continue
            mime, fmt = MIME[ext]
            data = base64.b64encode(open(path, "rb").read()).decode()
            rules.append(
                f"@font-face{{font-family:'f-{genre}-{role}';src:url(data:{mime};base64,{data}) format('{fmt}');font-display:swap}}"
            )
    open(out_css, "w").write("\n".join(rules))
    print(f"{len(rules)} faces -> {out_css}")
    for genre, role, why in skipped:
        print(f"  skipped {genre}-{role}: {why}")


if __name__ == "__main__":
    if len(sys.argv) not in (4, 5):
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2], sys.argv[3], int(sys.argv[4]) if len(sys.argv) == 5 else 4_000_000)
