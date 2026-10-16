#!/usr/bin/env python3
"""Extract every `<genre>_visual_config` from a Firebase Remote Config template JSON.

Usage: extract_genre_configs.py <rc_template.json> <out.json>

The template comes from the Firebase MCP `remoteconfig_get_template` tool. It is large, so the tool
saves it to a file and prints the path; pass that path here. The configs live inside
`parameterGroups.<group>.parameters`, NOT in the top-level `parameters`.
"""
import json
import sys


def main(template_path: str, out_path: str) -> None:
    template = json.load(open(template_path))
    configs = {}
    for group in template.get("parameterGroups", {}).values():
        for key, param in group.get("parameters", {}).items():
            if key.endswith("_visual_config"):
                configs[key.removesuffix("_visual_config")] = json.loads(param["defaultValue"]["value"])
    json.dump(configs, open(out_path, "w"), indent=2, ensure_ascii=False)
    print(f"RC v{template['version']['versionNumber']}: {len(configs)} genres -> {out_path}")
    for genre, cfg in configs.items():
        print(f"  {genre:13} primary={cfg.get('primaryColor')} radius={cfg.get('cornerSizeDp')} palette={len(cfg.get('colorPalette', []))}")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2])
